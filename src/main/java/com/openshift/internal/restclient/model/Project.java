/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift.internal.restclient.model;

import static com.openshift.internal.restclient.capability.CapabilityInitializer.initializeCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.dmr.ModelNode;

import com.openshift.restclient.IClient;
import com.openshift.restclient.model.IOpenShiftAnnotations;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IResource;

/**
 * DMR implementation of a project
 * @author Jeff Cantrill
 */
public class Project extends KubernetesResource implements IProject, IOpenShiftAnnotations{
	
	public Project(ModelNode node, IClient client, Map<String, String []> propertyKeys) {
		super(node, client, propertyKeys);
		initializeCapabilities(getModifiableCapabilities(), this, getClient());
	}
	
	
	@Override
	public IProject getProject() {
		return this;
	}

	@Override
	public String getNamespace() {
		if(StringUtils.isEmpty(super.getNamespace()))
			return getName();
		return super.getNamespace();
	}


	@Override
	public String getDisplayName(){
		return getAnnotation(DISPLAY_NAME);
	}
	
	public void setDisplayName(String name) {
		setAnnotation(DISPLAY_NAME, name);
	}
	

	@Override
	public String getDescription() {
		return getAnnotation(DESCRIPTION);
	}


	@Override
	public void setDescription(String value) {
		setAnnotation(DESCRIPTION, value);
	}


	@Override
	public <T extends IResource> List<T> getResources(String kind){
		if(getClient() == null) return new ArrayList<T>();
		return getClient().list(kind, getName());
	}
}
