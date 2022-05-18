/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.tests.stubs.trace;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.tracecompass.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.tracecompass.tmf.core.component.TmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;

/**
 * <b><u>TmfExperimentStub</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
@SuppressWarnings("javadoc")
public class TmfExperimentStub extends TmfExperiment {

    /**
     * Default constructor. Should not be called directly by the code, but
     * needed for the extension point.
     *
     * Do not call this directly (but do not remove it either!)
     */
    public TmfExperimentStub() {
        super();
    }

    public TmfExperimentStub(String name, ITmfTrace[] traces, int blockSize) {
        super(ITmfEvent.class, name, traces, blockSize, null);
    }

    @Override
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfIndexerStub(this, interval);
    }

    @Override
    public TmfIndexerStub getIndexer() {
        return (TmfIndexerStub) super.getIndexer();
    }

    @Override
    public void initExperiment(final Class<? extends ITmfEvent> type, final String path, final ITmfTrace[] traces, final int indexPageSize, IResource resource) {
        super.initExperiment(type, path, traces, indexPageSize, resource);
    }

    /**
     * @return a copy of the pending request list
     * @throws Exception
     *             if java reflection failed
     */
    public List<TmfCoalescedEventRequest> getAllPendingRequests() throws Exception {
        Method m = TmfEventProvider.class.getDeclaredMethod("getPendingRequests");
        m.setAccessible(true);
        LinkedList<?> list = (LinkedList<?>) m.invoke(this);
        LinkedList<TmfCoalescedEventRequest> retList = new LinkedList<>();
        for (Object element : list) {
            retList.add((TmfCoalescedEventRequest) element);
        }

        return retList;
    }

    /**
     * Clears the pending request list
     *
     * @throws Exception
     *             if java reflection failed
     */
    public void clearAllPendingRequests() throws Exception {
        Method m = TmfEventProvider.class.getDeclaredMethod("clearPendingRequests");
        m.setAccessible(true);
        m.invoke(this);
    }

    /**
     * Sets the timer flag
     *
     * @param enabled
     *            flag to set
     * @throws Exception
     *             if java reflection failed
     */
    public void setTimerEnabledFlag(boolean enabled) throws Exception {
        Class<?>[] paramTypes = new Class[1];
        paramTypes[0] = Boolean.class;
        Method m = TmfEventProvider.class.getDeclaredMethod("setTimerEnabled", paramTypes);

        Object[] params = new Object[1];
        params[0] = Boolean.valueOf(enabled);
        m.setAccessible(true);
        m.invoke(this, params);
    }
}
