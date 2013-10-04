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

import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.core.component.ITmfEventProvider;
import org.eclipse.linuxtools.tmf.core.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.tests.stubs.event.TmfSyntheticEventStub;

/**
 * <b><u>TmfSyntheticEventProviderStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("javadoc")
public class TmfSyntheticEventProviderStub extends TmfEventProvider {

    public static final int NB_EVENTS  = 1000;

    public TmfSyntheticEventProviderStub() {
        super("TmfSyntheticEventProviderStub", TmfSyntheticEventStub.class);
    }

    @Override
    public ITmfContext armRequest(final ITmfEventRequest request) {

        // Get the TmfSyntheticEventStub provider
        final ITmfEventProvider[] eventProviders = TmfProviderManager.getProviders(ITmfEvent.class, TmfEventProviderStub.class);
        final ITmfEventProvider provider = eventProviders[0];

        final TmfTimeRange range = request.getRange();
        final TmfEventRequest subRequest =
                new TmfEventRequest(ITmfEvent.class, range, 0, NB_EVENTS, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                if (event != null) {
                    handleIncomingData(event);
                } else {
                    request.done();
                }
            }
        };
        provider.sendRequest(subRequest);

        // Return a dummy context
        return new TmfContext();
    }

    // Queue 2 synthetic events per base event
    private void handleIncomingData(final ITmfEvent e) {
        queueResult(new TmfSyntheticEventStub(e));
        queueResult(new TmfSyntheticEventStub(e));
    }

    private static final int TIMEOUT = 10000;

    @Override
    public TmfSyntheticEventStub getNext(final ITmfContext context) {
        TmfSyntheticEventStub data = null;
        try {
            data = (TmfSyntheticEventStub) fDataQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if (data == null) {
                throw new InterruptedException();
            }
        }
        catch (final InterruptedException e) {
        }
        return data;
    }

    public void queueResult(final TmfSyntheticEventStub data) {
        boolean ok = false;
        try {
            ok = fDataQueue.offer(data, TIMEOUT, TimeUnit.MILLISECONDS);
            if (!ok) {
                throw new InterruptedException();
            }
        }
        catch (final InterruptedException e) {
        }
    }

}
