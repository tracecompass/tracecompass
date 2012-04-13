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

package org.eclipse.linuxtools.tmf.tests.stubs.component;

import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.tests.stubs.event.TmfSyntheticEventStub;

/**
 * <b><u>TmfSyntheticEventProviderStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("nls")
public class TmfSyntheticEventProviderStub extends TmfEventProvider<TmfSyntheticEventStub> {

    public static final int BLOCK_SIZE = 100;
    public static final int NB_EVENTS  = 1000;

    public TmfSyntheticEventProviderStub() {
        super("TmfSyntheticEventProviderStub", TmfSyntheticEventStub.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ITmfContext armRequest(final ITmfDataRequest<TmfSyntheticEventStub> request) {

        // Get the TmfSyntheticEventStub provider
        final ITmfDataProvider<TmfEvent>[] eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfEventProviderStub.class);
        final ITmfDataProvider<TmfEvent> provider = eventProviders[0];

        // make sure we have the right type of request
        if (!(request instanceof ITmfEventRequest<?>)) {
            request.cancel();
            return null;
        }

        final TmfEventRequest<TmfSyntheticEventStub> eventRequest = (TmfEventRequest<TmfSyntheticEventStub>) request;
        final TmfTimeRange range = eventRequest.getRange();
        final TmfEventRequest<TmfEvent> subRequest =
                new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData(final TmfEvent event) {
                super.handleData(event);
                if (event != null)
                    handleIncomingData(event);
                else
                    request.done();
            }
        };
        provider.sendRequest(subRequest);

        // Return a dummy context
        return new TmfContext();
    }

    // Queue 2 synthetic events per base event
    private void handleIncomingData(final TmfEvent e) {
        queueResult(new TmfSyntheticEventStub(e));
        queueResult(new TmfSyntheticEventStub(e));
    }

    private static final int TIMEOUT = 10000;

    @Override
    public TmfSyntheticEventStub getNext(final ITmfContext context) {
        TmfSyntheticEventStub data = null;
        try {
            data = fDataQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if (data == null)
                throw new InterruptedException();
        }
        catch (final InterruptedException e) {
        }
        return data;
    }

    public void queueResult(final TmfSyntheticEventStub data) {
        boolean ok = false;
        try {
            ok = fDataQueue.offer(data, TIMEOUT, TimeUnit.MILLISECONDS);
            if (!ok)
                throw new InterruptedException();
        }
        catch (final InterruptedException e) {
        }
    }

}
