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
 * Is a class adaptable to
 * another
 * @author jeff.cantrill
 *
 */
public interface IAdaptable {
	
	/**
	 * Adapt this to the given class
	 * @param klass
	 * @return an instance of the given class or null if it is not adaptable;
	 */
	<T> T adapt(Class<T> klass);
	
	/**
	 * Is this class adaptable to the given type
	 * @param klass
	 * @return
	 */
	boolean isAdatable(Class<?> klass);
}
