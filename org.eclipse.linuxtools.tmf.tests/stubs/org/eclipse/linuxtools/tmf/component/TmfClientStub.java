/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.component;

import org.eclipse.linuxtools.tmf.event.TmfSyntheticEventStub;

/**
 * <b><u>TmfClientStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("nls")
public class TmfClientStub extends TmfComponent {

	private TmfDataProvider<TmfSyntheticEventStub>[] fProviders;

	public TmfClientStub() {
		super("TmfClientStub");
	}

	@SuppressWarnings("unchecked")
	public void findProvider() {
		fProviders = (TmfDataProvider<TmfSyntheticEventStub>[]) TmfProviderManager.getProviders(TmfSyntheticEventStub.class, TmfSyntheticEventProviderStub.class);
//		TmfEventRequest<TmfEventStub> request;
		System.out.println(fProviders.length);
	}

	public void triggeRequest() {
//		TmfEventRequest<TmfEventStub> request;
	}

}
