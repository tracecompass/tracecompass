/*******************************************************************************
 * Copyright (c) 2009, 2017 Ericsson
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

import org.eclipse.tracecompass.tmf.core.component.TmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * <b><u>TmfEventProviderStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("javadoc")
public class TmfEventProviderStub extends TmfEventProvider {

    private TmfTraceStub fTrace;

    public TmfEventProviderStub(final String path) {
        super(path, ITmfEvent.class);
        try {
            fTrace = new TmfTraceStub(path, 0, true, null);
        } catch (final TmfTraceException e) {
            e.printStackTrace();
        }
    }

    public TmfEventProviderStub() {
        this(TmfTestTrace.A_TEST_10K.getFullPath());
    }

    @Override
    public void dispose() {
        fTrace.dispose();
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // TmfEventProvider
    // ------------------------------------------------------------------------

    @Override
    public ITmfContext armRequest(final ITmfEventRequest request) {
        final ITmfContext context = fTrace.seekEvent(request.getRange().getStartTime());
        return context;
    }

    @Override
    public ITmfEvent getNext(final ITmfContext context) {
        return fTrace.getNext(context);
    }

    @Override
    public boolean matches(ITmfEvent event) {
        return (super.matches(event) || event.getTrace() == fTrace);
    }

}
