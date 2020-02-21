/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.component;

import org.eclipse.tracecompass.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.tracecompass.tmf.core.component.TmfComponent;
import org.eclipse.tracecompass.tmf.core.component.TmfEventProvider;
import org.eclipse.tracecompass.tmf.tests.stubs.event.TmfSyntheticEventStub;

/**
 * <b><u>TmfClientStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("javadoc")
public class TmfClientStub extends TmfComponent {

    private TmfEventProvider[] fProviders;

    public TmfClientStub() {
        super("TmfClientStub");
    }

    public void findProvider() {
        fProviders = TmfProviderManager.getProviders(TmfSyntheticEventStub.class, TmfSyntheticEventProviderStub.class);
        //        TmfEventRequest<TmfEventStub> request;
        System.out.println(fProviders.length);
    }

    public void triggeRequest() {
        //        TmfEventRequest<TmfEventStub> request;
    }

}
