/*
 * Copyright (c) 2010-2011, Monash e-Research Centre
 * (Monash University, Australia)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright
 * 	  notice, this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright
 * 	  notice, this list of conditions and the following disclaimer in the
 * 	  documentation and/or other materials provided with the distribution.
 * 	* Neither the name of the Monash University nor the names of its
 * 	  contributors may be used to endorse or promote products derived from
 * 	  this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openmicroscopy.shoola.agents.monash.view;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.openmicroscopy.shoola.agents.monash.DataCollectionLoader;
import org.openmicroscopy.shoola.agents.monash.FilterByTag;
import org.openmicroscopy.shoola.agents.monash.PublishAgent;
import org.openmicroscopy.shoola.agents.monash.TagValueSaver;
import org.openmicroscopy.shoola.agents.monash.TagsLoader;
import org.openmicroscopy.shoola.agents.monash.service.MonashServices;
import org.openmicroscopy.shoola.agents.monash.service.MonashSvcReply;
import org.openmicroscopy.shoola.agents.monash.service.ServiceFactory;
import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.agents.monash.util.Unmarshaller;
import org.openmicroscopy.shoola.agents.monash.view.data.LicenceBean;
import org.openmicroscopy.shoola.agents.monash.view.data.MonashData;
import org.openmicroscopy.shoola.agents.monash.view.data.PartyBean;
import org.openmicroscopy.shoola.svc.transport.TransportException;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.TagAnnotationData;

/** 
 * The Model component in the <code>AndsPublish</code> MVC triad.
 * This class keeps the <code>AndsPublish</code>'s state and data. 
 * The {@link AndsPublishComponent} intercepts the 
 * results of data loadings, feeds them back to this class.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class AndsPublishModel {

	/** Holds one of the state flags defined by {@link PublishAgent}. */
	private int                 state;

	/** The ID of the root. */
	private long				rootID;

	/** The id of the selected group of the current user. */
	private long				userGroupID;

	/** The currently selected experimenter. */
	private ExperimenterData	experimenter;
	
	/** Party info for the logged in user */
	private PartyBean 			partybean;

	/** Flag indicating if the {@link TreeViewer} is recycled or not. */
	private boolean				recycled;

	/** Reference to the component that embeds this model. */
	protected AndsPublish		component;

	/** 
	 * Loader to load data collection asynchronously or
	 * <code>null</code> depending on the current state. 
	 */
	private DataCollectionLoader dataLoader;

	/** Collection of Project/ Dataset tagged {@link Constants.REGISTER_RDA_TAG} */
	private Collection 			dataCollection = null;

	/** Cookie to connect to Monash DS */
	private String 				cookie;
	
	/** The license associated with the data collection. */
	private LicenceBean 		license;

	/** The metadata of the data collection */
	private MonashData 			metadata;
	
	/** Hashtable containing party information */
	private Hashtable<String, PartyBean>	partyHtable;

	/** The number of filters.*/
	private List<DataObject> filteredData;
	
	/** Collection of existing tags if any. */
    private Collection existingTags;
    
	/**
	 * Creates a new instance and sets the state to {@link AndsPublish#NEW}.
	 */
	protected AndsPublishModel()
	{
		initialize();
	}

	/**
	 * Creates a new instance and sets the state to {@link AndsPublish#NEW}.
	 * 
	 * @param exp			The experimenter this model is for. 
	 * @param userGroupID 	The id to the group selected for the current user.
	 */
	public AndsPublishModel(ExperimenterData exp, long userGroupID) {
		initialize();
		this.experimenter = exp;
		setHierarchyRoot(exp.getId(), userGroupID);
	}

	/** Initializes. */
	private void initialize()
	{
		state = AndsPublish.NEW;
		partyHtable = new Hashtable<String, PartyBean>();
		filteredData = new ArrayList<DataObject>();
		license = null;
		metadata = null;
		recycled = false;
	}

	/**
	 * Sets the root of the retrieved hierarchies. 
	 * 
	 * @param rootID    	The Id of the root. By default it is the 
	 * 						id of the current user.
	 * @param userGroupID 	The id to the group selected for the current user.
	 */
	void setHierarchyRoot(long rootID, long userGroupID)
	{
		this.rootID = rootID;
		this.userGroupID = userGroupID;
	}

	/**
	 * Compares another model to this one to tell if they would result in
	 * having the same display.
	 *  
	 * @param other The other model to compare.
	 * @return <code>true</code> if <code>other</code> would lead to a viewer
	 *          with the same display as the one in which this model belongs;
	 *          <code>false</code> otherwise.
	 */
	protected boolean isSameDisplay(AndsPublishModel other) {
		if (other == null) return false;
		return ((other.rootID == rootID) && (other.userGroupID == userGroupID));
	}

	/**
	 * Returns <code>true</code> if the {@link AndsPublish} is recycled,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isRecycled() { return recycled; }

	/**
	 * Sets to <code>true</code> if the {@link AndsPublish} is recycled,
	 * <code>false</code> otherwise.
	 * 
	 * @param b The value to set.
	 */
	void setRecycled(boolean b) { recycled = b; }

	/**
	 * Called by the <code>AndsPublish</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(AndsPublish component)
	{ 
		this.component = component; 
	}

	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link AndsPublish} interface.  
	 */
	protected int getState() {
		return state;
	}

	/**
	 * Sets the current state.
	 * 
	 * @param state The state to set.
	 */
	void setState(int state) { this.state = state; }

	/**
	 * Sets the object in the {@link AndsPublish#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		cancel();
		state = AndsPublish.DISCARDED;
	}

	/**
	 * Cancels any ongoing data loading and sets the state 
	 * to {@link AndsPublish#READY} state.
	 */
	void cancel()
	{
		if (dataLoader != null) {
			dataLoader.cancel();
			dataLoader = null;
		}
		state = AndsPublish.READY;
	}

	protected ExperimenterData getExperimenter() {
		return experimenter;
	}

	protected long getRootID() {
		return rootID;
	}

	protected void setRootID(long rootID) {
		this.rootID = rootID;
	}

	protected long getUserGroupID() {
		return userGroupID;
	}

	protected void setUserGroupID(long userGroupID) {
		this.userGroupID = userGroupID;
	}

	/**
	 * 
	 * @return	the data collections to register with RDA
	 */
	protected Collection getDataCollection() {
		return dataCollection;
	}

	/**
	 * Loads the data collections to register with RDA
	 */
	protected void loadDataCollection() {
		state = AndsPublish.LOADING_DATA;
		dataLoader = new DataCollectionLoader(component);
		dataLoader.load();
	}

	/**
	 * @param dataCollection	the data collections to register with RDA
	 */
	protected void setDataCollection(Collection dataCollection) {
		this.dataCollection = dataCollection;
		filteredData.clear();
	}

	/**
	 * Cookie to connect to Monash DS
	 * @param cookie
	 */
	protected void setCookie(String cookie) {
		this.cookie = cookie;
	}

	/**
	 * @return the cookie
	 */
	protected String getCookie() {
		return cookie;
	}

	/**
	 * Adds the given <code>PartyBean</code> to the {@link #partyHtable}.
	 * 
	 * @param key   the party key
	 * @param pb	the PartyBean to add
	 */
	protected void addParty(String key, PartyBean pb) {
		partyHtable.put(key, pb);
	}

	/**
	 * Removes the <code>PartyBean</code> for the given key from 
	 * the {@link #partyHtable}.
	 * 
	 * @param key   the party key
	 */
	protected void removeParty(String key) {
		partyHtable.remove(key);
	}

	/**
	 * Sets the license associated with the data collection.
	 * @param ccl	the <code>LicenceBean</code> to set
	 */
	protected void setLicense(LicenceBean ccl) {
		this.license = ccl;
	}

	/**
	 * returns true if all the data necessary for registering
	 * collection with RDA is available as well as authentication
	 * token available to connect to Monash DS, false otherwise.
	 * 
	 * @return see above
	 */
	protected boolean hasAllData() {
		if (cookie != null && license != null && partyHtable.size() > 0)
			return true;
		return false;
	}

	/**
	 * Returns all required parameters to register collection
	 * TODO add dataset_id, collection title, desc and authcate_id
	 * 
	 * @return see above
	 */
	protected NameValuePair[] getMdRegParams() 
	{
		if (!hasAllData()) return null; // TODO show error msg
		
		List<NameValuePair> nvp = getLicenseParams();
		getPartyParams(nvp);
		getMetadataParams(nvp);
		
		Iterator<NameValuePair> j = nvp.iterator();
    	NameValuePair[] values = new NameValuePair[nvp.size()];
    	int index = 0;
    	while (j.hasNext()) {
    		values[index] = j.next();
			index++;
		}
    	return values;
	}

	/**
	 * Return the <code>NameValuePair</code> for the collection metadata
	 * 
	 * @return see above
	 */
	private void getMetadataParams(List<NameValuePair> nvp) {
		long datasetId = metadata.getId();
		nvp.add(new NameValuePair("regMetadata.datasetId", String.valueOf(datasetId)));
		nvp.add(new NameValuePair("regMetadata.description", metadata.getDescription()));
		nvp.add(new NameValuePair("regMetadata.title", metadata.getTitle()));
		nvp.add(new NameValuePair("regMetadata.uid", experimenter.getUserName()));
	}

	/**
	 * Return the <code>NameValuePair</code> for <code>PartyBean</code>
	 * @param nvp 
	 * 
	 * @return see above
	 */
	private void getPartyParams(List<NameValuePair> nvp) 
	{
		int i=0;
		for (PartyBean pb : partyHtable.values()) 
		{
			if (pb.getGroupName() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].groupName", pb.getGroupName()));
			if (pb.getPartyKey() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].partyKey", pb.getPartyKey()));
			if (pb.getOriginateSourceType() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].originateSourceType", pb.getOriginateSourceType()));
			if (pb.getOriginateSourceValue() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].originateSourceValue", pb.getOriginateSourceValue()));
			if (pb.getIdentifierType() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].identifierType", pb.getIdentifierType()));
			if (pb.getIdentifierValue() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].identifierValue", pb.getIdentifierValue()));
			if (pb.getPersonTitle() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].personTitle", pb.getPersonTitle()));
			if (pb.getPersonGivenName() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].personGivenName", pb.getPersonGivenName()));
			if (pb.getPersonFamilyName() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].personFamilyName", pb.getPersonFamilyName()));
			if (pb.getUrl() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].url", pb.getUrl()));
			if (pb.getEmail() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].email", pb.getEmail()));
			if (pb.getAddress() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].address", pb.getAddress()));
			if (pb.getRifcsContent() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].rifcsContent", pb.getRifcsContent()));
			if (pb.getFromRm() != null)
				nvp.add(new NameValuePair("partyBeans[" + i + "].fromRm", pb.getFromRm()));
			i++;
			
		}
	}

	/**
	 * Return the <code>NameValuePair</code> for <code>LicenceBean</code>
	 * 
	 * @return see above
	 */
	private List<NameValuePair> getLicenseParams() 
	{
		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		String type = license.getLicenceType();
		nvp.add(new NameValuePair("licenceBean.licenceType", type));
		nvp.add(new NameValuePair("licenceBean.licenceContents", license.getLicenceContents()));
		if(type.equals(Constants.LICENSE_CCCL_TYPE)) {
			nvp.add(new NameValuePair("licenceBean.commercial", license.getCommercial()));
			nvp.add(new NameValuePair("licenceBean.derivatives", license.getDerivatives()));
			nvp.add(new NameValuePair("licenceBean.jurisdiction", license.getJurisdiction()));
		}
		return nvp;
	}

	/**
	 * Starts an asynchronous call to update the tag from
	 * {@link Constants#REGISTER_RDA_TAG} to {@link Constants#SUCCESS_RDA_TAG}.
	 */
	protected void changeTag() {
		state = AndsPublish.SAVING;
		DataObject object = metadata.getDataObject();
		if (object == null) return;
		if (existingTags == null) {
			loadExistingTags();
			return;//This should not happen.
		}
		Iterator i = existingTags.iterator();
		//to be on the safe side, 
		//in case several tags with same name were created 
		List<AnnotationData> registerTag = new ArrayList<AnnotationData>();
		List<AnnotationData> registeredTag = new ArrayList<AnnotationData>();;
		while (i.hasNext()) {
			TagAnnotationData data = (TagAnnotationData) i.next();
			if (Constants.REGISTER_RDA_TAG.equals(data.getTagValue())) {
				registerTag.add(data);
			} else if (Constants.SUCCESS_RDA_TAG.equals(data.getTagValue())) {
				registeredTag.add(data);
			}
		}
		if (registeredTag.size() == 0) { //not created so we created it.
			registeredTag.add(new TagAnnotationData(Constants.SUCCESS_RDA_TAG));
		}
		TagValueSaver loader = new TagValueSaver(component, object,
				registeredTag, registerTag);
		loader.load();
	}

	/**
	 * Sets the collection metadata
	 * @param metadata the metadata to set
	 */
	protected void setMetadata(MonashData metadata) {
		this.metadata = metadata;
	}

	/**
	 * @return the collection metadata
	 */
	protected MonashData getMetadata() {
		return metadata;
	}

	/**
	 * Returns <code>true</code> if the passed object is the reference object,
	 * <code>false</code> otherwise.
	 * 
	 * @param uo The object to compare.
	 * @return See above.
	 */
	boolean isSameObject(DataObject uo)
	{
		DataObject refObject = metadata.getDataObject();
		if (uo == null || !(refObject instanceof DataObject)) return false;
		if (uo.getId() != refObject.getId()) return false;
		return true;
	}

	protected PartyBean searchParty() 
	{
		try {
			MonashSvcReply reply = searchRM(experimenter.getEmail());
			String result = reply.getReply();
			return Unmarshaller.getPartyBean(result);
		} catch (TransportException e) {
			return null;
		}
	}

	public MonashSvcReply searchRM(String party) throws TransportException 
	{
		String wsURL = PublishAgent.getPartyToken();
		MonashServices mSvc = ServiceFactory.getMonashServices(wsURL, -1);
		Map<String, String> params = new HashMap<String, String>();
		params.put("party", party);
		return mSvc.searchRM(cookie, params);
	}

	/**
	 * Filters the passed objects tagged with {@link Constants#REGISTER_RDA_TAG}.
	 * 
	 * @param type The type of object to handle.
	 * @param ids The identifiers of the objects.
	 */
	void filterData(Class type, List<Long> ids)
	{
		FilterByTag  loader = new FilterByTag(component, type, ids);
		loader.load();
	}
	
	/**
	 * Adds the nodes to the list.
	 * 
	 * @param nodes The nodes to handle.
	 */
	void setFilteredData(List<DataObject> nodes)
	{
		filteredData.addAll(nodes);
	}
	
	/**
	 * Returns the filtered data.
	 * @return
	 */
	List<DataObject> getFilteredData()
	{
		return filteredData;
	}

	/** clear the filtered data when data reloads */
	void clearFilteredData() {
		filteredData.clear();
	}

	void clearParty() {
		if (partybean != null && partyHtable.containsKey(partybean.getPartyKey())) {
			if (partyHtable.size() > 1) {
				partyHtable.clear();
				partyHtable.put(partybean.getPartyKey(), partybean);
			}
		} else {
			partyHtable.clear();
		}
	}

	/**
	 * Sets the collection of existing tags.
	 * 
	 * @param tags The value to set.
	 */
	void setExistingTags(Collection tags)
	{
		existingTags = tags;
	}
	
	/** Loads the existing in the tags.*/
	void loadExistingTags()
	{
		TagsLoader loader = new TagsLoader(component);
		loader.load();
	}

	/**
	 * @return the partybean
	 */
	PartyBean getPartybean() {
		return partybean;
	}

	/**
	 * @param partybean the partybean to set
	 */
	void setPartybean(PartyBean partybean) {
		this.partybean = partybean;
	}

}
