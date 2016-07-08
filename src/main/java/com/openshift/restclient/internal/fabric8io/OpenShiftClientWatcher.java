/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift.restclient.internal.fabric8io;

import com.openshift.restclient.IOpenShiftWatchListener;
import com.openshift.restclient.IWatcher;

import io.fabric8.openshift.client.OpenShiftClient;

public class OpenShiftClientWatcher implements IWatcher {

	private final IOpenShiftWatchListener listener;
	private final OpenShiftClient client;

	public OpenShiftClientWatcher(OpenShiftClient client, IOpenShiftWatchListener listener) {
		this.client = client;
		this.listener = listener;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	public IWatcher watch(String... kinds) {
//		client.builds().wat
		return this;
	}

}
