#!/usr/bin/env python
"""
   User administration plugin

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

from omero.cli import BaseControl, CLI

HELP = "Support for adding and managing users"

class UserControl(BaseControl):

    def _configure(self, parser):
        sub = parser.sub()

        add = parser.add(sub, self.add, help = "Add users")
        add.add_argument("-m", "--middlename", help = "Middle name, if available")
        add.add_argument("-e", "--email")
        add.add_argument("-i", "--institution")
        # Capitalized since conflict with main values
        add.add_argument("-P", "--userpassword", help = "Password for user")
        add.add_argument("-a", "--admin", help = "Whether the user should be an admin")
        add.add_argument("username", help = "User's login name")
        add.add_argument("firstname", help = "User's given name")
        add.add_argument("lastname", help = "User's surname name")
        add.add_argument("member_of", nargs="+", help = "Groups which the user is to be a member of")

        list = parser.add(sub, self.list, help = "List current users")

        password = parser.add(sub, self.password, help = "Set user's password")
        password.add_argument("username", nargs="?", help = "Username if not the current user")

    def password(self, args):
        from omero.rtypes import rstring
        client = self.ctx.conn(args)
        admin = client.sf.getAdminService()
        pw = self._ask_for_password(" to be set")
        pw = rstring(pw)
        if args.username:
            admin.changeUserPassword(args.username, pw)
        else:
            admin.changePassword(pw)
        self.ctx.out("Password changed")

    def list(self, args):
        c = self.ctx.conn(args)
        users = c.sf.getAdminService().lookupExperimenters()
        from omero.util.text import TableBuilder
        tb = TableBuilder("id", "omeName", "firstName", "lastName", "email", "member of", "leader of")
        for user in users:
            row = [user.id.val, user.omeName.val, user.firstName.val, user.lastName.val]
            row.append(user.email and user.email.val or "")
            member_of = [str(x.parent.id.val) for x in user.copyGroupExperimenterMap() if not x.owner.val]
            leader_of = [str(x.parent.id.val) for x in user.copyGroupExperimenterMap() if x.owner.val]
            if member_of:
                row.append(",".join(member_of))
            else:
                row.append("")
            if leader_of:
                row.append(",".join(leader_of))
            else:
                row.append("")

            tb.row(*tuple(row))
        self.ctx.out(str(tb.build()))

    def add(self, args):
        email = args.email
        login = args.username
        first = args.firstname
        middle = args.middlename
        last = args.lastname
        inst = args.institution
        pasw = args.userpassword

        from omero.rtypes import rstring
	from omero_model_ExperimenterI import ExperimenterI as Exp
	from omero_model_ExperimenterGroupI import ExperimenterGroupI as Grp
        c = self.ctx.conn(args)
	p = c.ic.getProperties()
	e = Exp()
	e.omeName = rstring(login)
	e.firstName = rstring(first)
	e.lastName = rstring(last)
	e.middleName = rstring(middle)
	e.email = rstring(email)
	e.institution = rstring(inst)
	admin = c.getSession().getAdminService()

        groups = [admin.lookupGroup(group) for group in args.member_of]
        roles = admin.getSecurityRoles()
        groups.append(Grp(roles.userGroupId, False))
        if args.admin:
            groups.append(Grp(roles.systemGroupId, False))

        group = groups.pop(0)

        if pasw is None:
            id = admin.createExperimenter(e, group, groups)
            self.ctx.out("Added user %s" % id)
        else:
            id = admin.createExperimenterWithPassword(e, rstring(pasw), group, groups)
            self.ctx.out("Added user %s with password" % id)

try:
    register("user", UserControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("user", UserControl, HELP)
        cli.invoke(sys.argv[1:])
