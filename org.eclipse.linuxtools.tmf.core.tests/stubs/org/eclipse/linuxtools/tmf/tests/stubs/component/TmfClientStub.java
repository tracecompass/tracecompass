/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.component;

import org.eclipse.linuxtools.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.component.TmfDataProvider;
import org.eclipse.linuxtools.tmf.tests.stubs.event.TmfSyntheticEventStub;

/**
 * <b><u>TmfClientStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("javadoc")
public class TmfClientStub extends TmfComponent {

    private TmfDataProvider[] fProviders;

    public TmfClientStub() {
        super("TmfClientStub");
    }

    public void findProvider() {
        fProviders = TmfProviderManager.getProviders(TmfSyntheticEventStub.class, TmfSyntheticEventProviderStub.class);
        //		TmfEventRequest<TmfEventStub> request;
        System.out.println(fProviders.length);
    }

    public void triggeRequest() {
        //		TmfEventRequest<TmfEventStub> request;
    }

}
