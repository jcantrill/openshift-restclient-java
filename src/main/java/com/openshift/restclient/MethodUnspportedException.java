/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift.restclient;

/**
 * 
 * @author jeff.cantrill
 *
 */
public class MethodUnspportedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MethodUnspportedException(String message, Object... args) {
		super(String.format(message, args));
	}

}
