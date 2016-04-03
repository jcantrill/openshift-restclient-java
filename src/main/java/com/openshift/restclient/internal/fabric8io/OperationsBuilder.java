/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift.restclient.internal.fabric8io;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.openshift.restclient.OpenShiftException;
import com.openshift.restclient.ResourceKind;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.Filterable;
import io.fabric8.kubernetes.client.dsl.Gettable;
import io.fabric8.kubernetes.client.dsl.Listable;
import io.fabric8.kubernetes.client.dsl.Nameable;
import io.fabric8.kubernetes.client.dsl.Namespaceable;
import io.fabric8.kubernetes.client.dsl.Replaceable;
import io.fabric8.openshift.client.OpenShiftClient;

/**
 * Builder class to facilitate adapting to the OpenShiftClient
 * 
 * @author jeff.cantrill
 *
 */
class OperationsBuilder{

	private Object operation;
	
	OperationsBuilder(OpenShiftClient client, String kind) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		final String kinds = ResourceKind.pluralize(kind);
		Method  method = client.getClass().getMethod(kinds);
		operation =  method.invoke(client);
	}
	
	@SuppressWarnings("rawtypes")
	public OperationsBuilder inNameSpace(String namespace) {
		if(operation instanceof Namespaceable) {
			operation =  ((Namespaceable)operation).inNamespace(namespace);
		}
		return this;
	}
	
	@SuppressWarnings("rawtypes")
	public OperationsBuilder withName(String name) {
		operation = ((Nameable) operation).withName(name);
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public OperationsBuilder withLabels(Map<String, String> labels) {
		operation = ((Filterable) operation).withLabels(labels);
		return this;
	}
	
	public HasMetadata create(HasMetadata resource) {
		return (HasMetadata) executeOperation("create", resource);
	}

	public Boolean delete(HasMetadata resource) {
		return (Boolean) executeOperation("delete", resource);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HasMetadata update(HasMetadata resource) {
		if(operation instanceof Replaceable) {
			//prefetch resource for update
			inNameSpace(resource.getMetadata().getNamespace())
			.withName(resource.getMetadata().getName())
			.get();
			return (HasMetadata) ((Replaceable) operation).replace(resource);
		}
		return resource;
	}
	
	private Object executeOperation(String methodName, HasMetadata resource) {
		try {
			Class<?> klass = ResourceFactoryAdapter.classForKind(resource.getKind());
			Object klassArray = Array.newInstance(klass, 1);
			Array.set(klassArray, 0, klass.cast(resource));
			//find create method
			Method method = getMethod(methodName, operation.getClass(), klassArray.getClass());
			
			//execute
			return method.invoke(operation, klassArray);
			
		}catch(Exception e) {
			throw new OpenShiftException(e, "Unable to %s %s", methodName, resource);
		}
	}

	@SuppressWarnings("rawtypes")
	public HasMetadata get() {
		return (HasMetadata)((Gettable) operation).get();
	}
	
	@SuppressWarnings("rawtypes")
	public KubernetesResourceList list() {
		return (KubernetesResourceList) ((Listable) operation).list();
	}

	private Method getMethod(final String name, final Class<?> klass, final Class<?> args) throws NoSuchMethodException, SecurityException {
		try {
			if(args == null) {
				return klass.getMethod(name);
			}
			return klass.getMethod(name, args);
		}catch(NoSuchMethodException e) {
			return klass.getMethod(name, Object[].class);
		}
	}


}