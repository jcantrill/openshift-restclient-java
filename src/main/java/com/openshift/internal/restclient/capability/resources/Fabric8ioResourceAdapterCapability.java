/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift.internal.restclient.capability.resources;

import org.apache.commons.lang.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.restclient.OpenShiftException;
import com.openshift.restclient.capability.resources.IFabric8ioResourceCapability;
import com.openshift.restclient.internal.fabric8io.ResourceFactoryAdapter;
import com.openshift.restclient.model.IResource;

import io.fabric8.kubernetes.api.model.HasMetadata;

public class Fabric8ioResourceAdapterCapability implements IFabric8ioResourceCapability {

	private final ObjectMapper mapper = new ObjectMapper();
	private IResource resource;
	private Class<?> klass;
	private ResourceFactoryAdapter factory;

	public Fabric8ioResourceAdapterCapability(IResource resource, ResourceFactoryAdapter factory) {
		this.resource = resource;
		this.klass = ResourceFactoryAdapter.classForKind(resource.getKind());
		this.factory = factory;
	}

	@Override
	public boolean isSupported() {
		return true;
	}
	
	@Override
	public String getName() {
		return IFabric8ioResourceCapability.class.getSimpleName();
	}
	
	@Override
	public HasMetadata getResource() {
		try {
			return (HasMetadata) mapper.readValue(resource.toJson(true), klass);
		} catch (Exception e) {
			throw new OpenShiftException(e, "Unable to deserialize into fabric8 type from %s", ObjectUtils.defaultIfNull(resource, "null"));
		}
	}

	@Override
	public void setResource(HasMetadata fabric8Resource) {
		try {
			this.resource = factory.create(mapper.writeValueAsString(fabric8Resource)); 
		} catch (Exception e) {
			throw new OpenShiftException(e, "Unable to deserialize %s from %s", fabric8Resource.getKind(), fabric8Resource);
		}
	}

}
