#!/usr/bin/env python
"""
   Plugin for viewing and controlling active sessions for a local user.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""


import exceptions, subprocess, optparse, os, sys, time
import getpass, pickle
import omero.java

from omero.util.sessions import SessionsStore
from omero.cli import BaseControl, CLI
from path import path

HELP = """Control and create user sessions

Sessions are stored locally on disk. Several can
be active simultaneously, but only one will be used
for a single invocation of bin/omero.

"""

LONGHELP = """
    Uses the login parameters from %(prog)s to login.

    To list these options, use "%(prog)s -h"

    Sample session:
        $ bin/omero -s localhost sessions login
        Username:
        Password:
        $ bin/omero -s localhost -u john sessions login
        Password
        $ bin/omero -s localhost -k 8afe443f-19fc-4cc4-bf4a-850ec94f4650 sessions login
        $ bin/omero sessions login
        Server:
        Username:
        Password:
        $ bin/omero sessions login user@omero.example.com
        Password:
        $ bin/omero sessions logout
        $ bin/omero sessions login
        Reuse current session? [Y/n]
        $ bin/omero sessions list
        $ bin/omero sessions logout
        $ bin/omero sessions login omero.example.com
        Username:
        Password:
        $ bin/omero sessions logout
        $ bin/omero -p 24063 sessions login
        Server:
        Username:
        Password:
        $ bin/omero sessions login my.email@example.com@omero.example.com
        Password:
        $ bin/omero -k 8afe443f-19fc-4cc4-bf4a-850ec94f4650 sessions login
        $ bin/omero sessions clear
"""

class SessionsControl(BaseControl):

    FACTORY = SessionsStore

    def store(self, args):
        dir = getattr(args, "dir", None)
        return self.FACTORY(dir)

    def _configure(self, parser):
        sub = parser.sub()
        help = parser.add(sub, self.help, "Extended help")
        login = parser.add(sub, self.login, "Login to a given server, and store session key locally")
        logout = parser.add(sub, self.logout, "Logout and remove current session key")
        self._configure_login(login, logout)
        list = parser.add(sub, self.list, "List all locally stored sessions")
        list.add_argument("--purge", action="store_true", help="Remove inactive sessions")
        keepalive = parser.add(sub, self.keepalive, "Keeps the current session alive")
        keepalive.add_argument("-f", "--frequency", type=int, default=60, help="Time in seconds between keep alive calls", metavar="SECS")
        clear = parser.add(sub, self.clear, "Close and remove locally stored sessions")
        clear.add_argument("--all", action="store_true", help="Remove all locally stored sessions not just inactive ones")
        file = parser.add(sub, self.file, "Print the path to the current session file")

        for x in (file, logout, keepalive, list, clear):
            self._configure_dir(x)

    def _configure_login(self, login, logout = None):
        login.add_argument("-t", "--timeout", help="Timeout for session. After this many inactive seconds, the session will be closed")
        login.add_argument("-C", "--create", action="store_true", help="Create a new session regardless of existing ones")
        login.add_argument("connection", nargs="?", help="Connection string. See extended help for examples")
        self._configure_dir(login)

    def _configure_dir(self, parser):
        parser.add_argument("-d", "--dir", help="Use a different sessions directory (Default: $HOME/omero/sessions)")

    def help(self, args):
        self.ctx.err(LONGHELP % {"prog":args.prog})

    def login(self, args):

        if self.ctx.conn():
            self.ctx.err("Active client found")
            return # EARLY EXIT

        create = getattr(args, "create", None)
        store = self.store(args)
        previous = store.get_current()

        #
        # Retrieving the parameters as set by the user
        # If these are set and different from the current
        # connection, then a new one may be created.
        #
        server = getattr(args, "connection", None) # May be called by another plugin
        name = None

        if args.server:
            if server:
                self.ctx.die(3, "Server specified twice: %s and %s" % (server, args.server))
            else:
                server = args.server

        if server: server, name = self._parse_conn(server)

        if args.user:
            if name:
                self.ctx.die(4, "Username specified twice: %s and %s" % (name, args.user))
            else:
                name = args.user

        server_differs = (server is not None and server != previous[0])
        name_differs = (name is not None and name != previous[1])

        if not create and not server_differs and not name_differs:
            try:
                rv = store.attach(*previous)
                return self.handle(rv, "Using")
            except exceptions.Exception, e:
                self.ctx.dbg("Exception on attach: %s" % e)

        if previous[0] and previous[1]:
            self.ctx.out("Previously logged in to %s as %s" % (previous[0], previous[1]))

        #
        # If we've reached here, then the user either does not have
        # an active session or has requested another (different options)
        # If they've omitted some required value, we must ask for it.
        #
        if not server: server, name = self._get_server(store)
        if not name: name = self._get_username()

        pasw = args.password
        if args.key:

            if name:
                self.ctx.err("Overriding name since session set")
            name = args.key

            if args.password:
                self.ctx.err("Ignoring password since key set")
            pasw = args.key

        if not name:
            name = self._username()

        props = {}
        props["omero.host"] = server
        props["omero.user"] = name
        if args.port:
            props["omero.port"] = args.port
        if args.group:
            props["omero.group"] = args.group

        rv = None
        if not create:
            available = store.available(server, name)
            for uuid in available:
                conflicts = store.conflicts(server, name, uuid, props)
                if conflicts:
                    self.ctx.dbg("Skipping %s due to conflicts: %s" % (uuid, conflicts))
                    continue
                try:
                    rv = store.attach(server, name, uuid)
                    store.set_current(server, name, uuid)
                    action = "Reconnected to"
                    break
                except:
                    self.ctx.dbg("Removing %s" % uuid)
                    store.clear(server, name, uuid)
                    continue

        if not rv:
            if not pasw:
                pasw = self.ctx.input("Password:", hidden = True, required = True)
            rv = store.create(name, pasw, props)
            action = "Created"

        return self.handle(rv, action)

    def handle(self, rv, action):
        """
        Handles a new connection
        """
        client, uuid, idle, live = rv
        self.ctx._client = client
        ec = self.ctx._client.sf.getAdminService().getEventContext()
        self.ctx._event_context = ec

        msg = "%s session %s (%s@%s)." % (action, uuid, ec.userName, client.getProperty("omero.host"))
        if idle:
            msg = msg + " Idle timeout: %s min." % (float(idle)/60/1000)
        if live:
            msg = msg + " Expires in %s min." % (float(live)/60/1000)

        msg += (" Current group: %s" % self.ctx._client.sf.getAdminService().getEventContext().groupName)

        self.ctx.err(msg)

    def logout(self, args):
        store = self.store(args)
        previous = store.get_current()

        import Glacier2
        try:
            rv = store.attach(*previous)
            rv[0].killSession()
        except exceptions.Exception, e:
            self.ctx.dbg("Exception on logout: %s" % e)
        store.set_current("", "", "")

    def list(self, args):
        import Glacier2
        store = self.store(args)
        s = store.contents()
        previous = store.get_current()

        #fmt = "%-16.16s\t%-12.12s\t%-12.12s\t%-40.40s\t%-30.30s\t%s"
        #self.ctx.out(fmt % ("Server","User","Group", "Session","Active", "Started"))
        #self.ctx.out("-"*136)
        headers = ("Server", "User", "Group", "Session", "Active", "Started")
        results = dict([(x,[]) for x in headers])
        for server, names in s.items():
            for name, sessions in names.items():
                for uuid, props in sessions.items():
                    rv = None
                    msg = "True"
                    grp = "Unknown"
                    started = "Unknown"
                    try:
                        rv = store.attach(server, name, uuid)
                        grp = rv[0].sf.getAdminService().getEventContext().groupName
                        started = rv[0].sf.getSessionService().getSession(uuid).started.val
                        rv[0].closeSession()
                    except Glacier2.PermissionDeniedException, pde:
                        msg = pde.reason
                    except exceptions.Exception, e:
                        self.ctx.dbg("Exception on attach: %s" % e)
                        msg = "Unknown exception"
                    if rv is None and args.purge:
                        self.ctx.dbg("Purging %s / %s / %s" % (server, name, uuid))
                        store.remove(server, name, uuid)

                    if server == previous[0] and name == previous[1] and uuid == previous[2]:
                            msg = "Logged in"

                    started = time.ctime(started / 1000.0)
                    results["Server"].append(server)
                    results["User"].append(name)
                    results["Group"].append(grp)
                    results["Session"].append(uuid)
                    results["Active"].append(msg)
                    results["Started"].append(started)

        from omero.util.text import Table, Column
        columns = tuple([Column(x, results[x]) for x in headers])
        self.ctx.out(str(Table(*columns)))

    def clear(self, args):
        store = self.store(args)
        count = store.count()
        store.clear()
        self.ctx.out("%s session(s) cleared" % count)

    def keepalive(self, args):
        import threading
        from omero.util.concurrency import get_event as get_event
        class T(threading.Thread):
            def run(self):
                while self.client:
                    try:
                        self.client.sf.keepAlive(None)
                        self.event.wait(args.frequency)
                    except exceptions.Exception, e:
                        self.err("Keep alive failed: %s" % str(e))
                        return
        t = T()
        t.client = self.ctx.conn(args)
        t.event = get_event()
        t.start()
        try:
            self.ctx.out("Running keep alive every %s seconds" % args.frequency)
            self.ctx.input("Press enter to cancel.")
        finally:
            t.client = None
            t.event.set()

    def file(self, args):
        store = self.store(args)
        srv, usr, uuid = store.get_current()
        if srv and usr and uuid:
            self.ctx.out(str(store.dir / srv / usr / uuid))

    def conn(self, properties={}, profile=None, args=None):
        """
        Either creates or returns the exiting omero.client instance.
        Uses the comm() method with the same signature.
        """

        if self._client:
            return self._client

        import omero
        try:
            data = self.initData(properties)
            self._client = omero.client(sys.argv, id = data)
            self._client.setAgent("OMERO.cli")
            self._client.createSession()
            return self._client
        except Exc, exc:
            self._client = None
            raise

    #
    # Private methods
    #

    def _parse_conn(self, server):
        try:
            idx = server.rindex("@")
            return  server[idx+1:], server[0:idx] # server, user which may also contain an @
        except ValueError:
            return server, None

    def _get_server(self, store):
        defserver = store.last_host()
        rv = self.ctx.input("Server: [%s]" % defserver)
        if not rv:
            return defserver, None
        else:
            return self._parse_conn(rv)

    def _get_username(self):
        defuser = getpass.getuser()
        rv = self.ctx.input("Username: [%s]" % defuser)
        if not rv:
            return defuser
        else:
            return rv


try:
    register("sessions", SessionsControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("sessions", SessionsControl, HELP)
        cli.invoke(sys.argv[1:])