/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift.restclient.capability.resources;

import com.openshift.restclient.capability.ICapability;

import io.fabric8.kubernetes.api.model.HasMetadata;

/**
 * Capability to provide the resource as a fabric8 type
 * @author jeff.cantrill
 *
 */
public interface IFabric8ioResourceCapability extends ICapability {

	HasMetadata getResource();
	void setResource(HasMetadata resource);
}
