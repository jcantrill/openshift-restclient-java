/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.openshift.internal.restclient.authorization;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Categories.ExcludeCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openshift.internal.restclient.DefaultClientIntegrationTest;
import com.openshift.internal.restclient.IntegrationTestHelper;
import com.openshift.internal.restclient.ResourceFactory;
import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.ClientBuilder.ClientType;
import com.openshift.restclient.IAuthorizeable;
import com.openshift.restclient.IClient;
import com.openshift.restclient.IResourceFactory;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.authorization.BasicAuthorizationStrategy;
import com.openshift.restclient.authorization.IAuthorizationContext;
import com.openshift.restclient.authorization.ResourceForbiddenException;
import com.openshift.restclient.authorization.TokenAuthorizationStrategy;
import com.openshift.restclient.authorization.UnauthorizedException;

/**
 * @author Jeff Cantrill
 */
public class OpenShiftClientAdapterAuthIntegrationTest {


	private static final Logger LOG = LoggerFactory.getLogger(OpenShiftClientAdapterAuthIntegrationTest.class);

	private IntegrationTestHelper helper = new IntegrationTestHelper();
	private IClient client;
//	private AuthorizationClient authClient;

	@Before
	public void setup () {
		client = helper.createClient(ClientType.FABRIC8IO);
	}

	/*---------- These are tests that should pass when server is configured for oauth auth. No expectations regarding others */

	/*
	 * Assume Basic Auth, invalid token
	 */
	@Test
	//@Environment(auth=oauth) //lets build this
	public void testBasicAuthFlow(){
		IAuthorizeable auth = client.adapt(IAuthorizeable.class);
		try {
			auth.authorize();
		}catch(ResourceForbiddenException e) {
			client.setAuthorizationStrategy(new BasicAuthorizationStrategy(helper.getDefaultClusterAdminUser(), helper.getDefaultClusterAdminPassword(), ""));
			auth.authorize();
			assertFalse("Exp. to be able to list projects after auth", client.list(ResourceKind.PROJECT).isEmpty());
		}
	}

	/*---------- These are tests that should pass when server is configured for basic auth. No expectations regarding others */

	@Test
	//@Environment(auth=oauth) //lets build this
	public void testOAuthFlow(){
		client = new ClientBuilder("https://10.3.9.15.xip.io:8443").build(ClientType.FABRIC8IO);
		IAuthorizeable auth = client.adapt(IAuthorizeable.class);
		final String token = "amuSW0F7wXFKhKHmVLFaGd1YGAsZor4hIYtcIufIgy8";
		try {
			auth.authorize();
		}catch(ResourceForbiddenException e) {
			client.setAuthorizationStrategy(new TokenAuthorizationStrategy(token));
			auth.authorize();
			assertFalse("Exp. to be able to list projects after auth", client.list(ResourceKind.PROJECT).isEmpty());
		}
	}

}
