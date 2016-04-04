 /*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift.restclient.internal.fabric8io;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.internal.restclient.capability.resources.Fabric8ioResourceAdapterCapability;
import com.openshift.internal.restclient.model.KubernetesResource;
import com.openshift.restclient.IClient;
import com.openshift.restclient.IResourceFactory;
import com.openshift.restclient.OpenShiftException;
import com.openshift.restclient.capability.resources.IFabric8ioResourceCapability;
import com.openshift.restclient.internal.fabric8io.model.HasMetadataImpl;
import com.openshift.restclient.model.IList;
import com.openshift.restclient.model.IResource;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;

public class ResourceFactoryAdapter implements IResourceFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(ResourceFactoryAdapter.class);
	private static Map<String, Class<?>> models = Collections.synchronizedMap(new HashMap<>());
	
	private final ObjectMapper mapper = new ObjectMapper();
	private final IResourceFactory dmrFactory;
	private IClient client;
	
	public ResourceFactoryAdapter(IResourceFactory dmrFactory) {
		this.dmrFactory = dmrFactory;
	}

	@Override
	public List<IResource> createList(String json, String kind) {
		List<IResource> list = dmrFactory.createList(json, kind);
		list.forEach(r->addCapabilities((KubernetesResource)r));
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IResource> T create(String response) {
		KubernetesResource resource = dmrFactory.create(response);
		addCapabilities(resource);
		return (T) resource;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IResource> T create(InputStream input) {
		KubernetesResource resource = dmrFactory.create(input);
		addCapabilities(resource);
		return (T) resource;
	}

	@SuppressWarnings("unchecked")
	public <T extends IResource> T create(HasMetadata meta) {
		try {
			KubernetesResource resource = dmrFactory.create(mapper.writeValueAsString(meta));
			addCapabilities(resource);
			return (T) resource;
		} catch (JsonProcessingException e) {
			throw new OpenShiftException(e, "Unable to create resource from %s", meta);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends IResource> T create(String version, String kind) {
		KubernetesResource resource = dmrFactory.create(version, kind);
		addCapabilities(resource);
		return (T) resource;
	}
	
	@SuppressWarnings("rawtypes")
	public IList create(KubernetesResourceList list) {
		try {
			KubernetesResource resource = dmrFactory.create(mapper.writeValueAsString(list));
			addCapabilities(resource);
			return (IList) resource;
		} catch (JsonProcessingException e) {
			throw new OpenShiftException(e, "Unable to create resource from %s", list);
		}
	}

	
	private void addCapabilities(IResource resource) {
		if(resource instanceof KubernetesResource) {
			KubernetesResource dmrResource = (KubernetesResource) resource;
			dmrResource.getModifiableCapabilities().put(IFabric8ioResourceCapability.class, new Fabric8ioResourceAdapterCapability(dmrResource, this));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IResource> T stub(String kind, String name) {
		final String apiVersion = client.getOpenShiftAPIVersion();
		KubernetesResource resource = create(apiVersion, kind);
		resource.setName(name);
		return (T) resource;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IResource> T stub(String kind, String name, String namespace) {
		final String apiVersion = client.getOpenShiftAPIVersion();
		KubernetesResource resource = create(apiVersion, kind);
		resource.setName(name);
		resource.setNamespace(namespace);
		return (T) resource;
	}

	@Override
	public void setClient(IClient client) {
		this.client = client;
		this.dmrFactory.setClient(client);
	}
	
	public static Class<?> classForKind(String kind){
		Class<?> klass = models.get(kind);
		if(klass != null) {
			return klass;
		}
		String className = "io.fabric8.kubernetes.api.model." + StringUtils.capitalize(kind);
		try {
			klass = Class.forName(className);
			models.put(kind, klass);
			return klass;
		} catch (ClassNotFoundException e) {
			try {
				LOG.debug("No implementation for: {}. trying openshift packages", className);
				className = "io.fabric8.openshift.api.model." + StringUtils.capitalize(kind);
				klass = Class.forName(className);
				models.put(kind, klass);
				return klass;
			}catch(ClassNotFoundException ex) {
				LOG.debug("No implementation for: {}. returning generic impl", className);
				return HasMetadataImpl.class;
			}
		}
	}

}
