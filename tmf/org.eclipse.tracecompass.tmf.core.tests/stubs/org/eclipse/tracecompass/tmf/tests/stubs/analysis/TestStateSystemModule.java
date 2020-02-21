/*******************************************************************************
 * Copyright (c) 2013, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

/**
 * Test State System module
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class TestStateSystemModule extends TmfStateSystemAnalysisModule {

    private final boolean fOnDisk;
    private boolean fThrottleEvents = false;
    private @Nullable TestStateSystemProvider fProvider = null;
    private Function<ITmfEvent, ITmfEvent> fRequestAction = e -> e;

    /**
     * Constructor
     */
    public TestStateSystemModule() {
        this(false);
    }

    /**
     * Constructor specifying the backend type
     *
     * @param onDisk <code>true</code> if the state system should be built on disk
     */
    public TestStateSystemModule(boolean onDisk) {
        super();
        fOnDisk = onDisk;
        setName("Test Analysis");
    }

    @Override
    protected ITmfStateProvider createStateProvider() {

        TestStateSystemProvider provider = new TestStateSystemProvider(checkNotNull(getTrace()));
        fProvider = provider;
        boolean throttle = fThrottleEvents;
        provider.setThrottling(throttle);
        return provider;
    }

    @Override
    protected StateSystemBackendType getBackendType() {
        return fOnDisk ? StateSystemBackendType.FULL : StateSystemBackendType.INMEM;
    }

    /**
     * Get the name of the backend
     *
     * @return The name of the backend used
     */
    public String getBackendName() {
        return StateSystemBackendType.INMEM.name();
    }

    /**
     * Set whether events are processed one at a time
     *
     * @param throttleEvent A value of <code>true</code> will have the events processed one a time instead of adding them all to the queue. To process the next event, one must call the {@link #signalNextEvent()} method. A value of <code>false</code> will return to default behavior.
     */
    public void setPerEventSignalling(boolean throttleEvent) {
        fThrottleEvents = throttleEvent;
        TestStateSystemProvider provider = fProvider;
        if (provider != null) {
            provider.setThrottling(throttleEvent);
        }
    }

    /**
     * Signal for the next event to be processed. This makes sense only if
     * {@link #setPerEventSignalling(boolean)} method has been set to true
     */
    public void signalNextEvent() {
        TestStateSystemProvider provider = fProvider;
        if (provider != null) {
            provider.signalNextEvent();
        }
    }

    @Override
    public @Nullable File getSsFile() {
        return super.getSsFile();
    }

    /**
     * Set a function that will be executed on an event and return an event.
     * This action will be executed in the event request, before calling the
     * parent's handleData method.
     *
     * @param requestAction
     *            The function to execute on the event
     */
    public void setRequestAction(Function<ITmfEvent, ITmfEvent> requestAction) {
        fRequestAction = requestAction;
    }

    @Override
    protected @NonNull ITmfEventRequest createEventRequest(@NonNull ITmfStateProvider stateProvider, @NonNull TmfTimeRange timeRange, int nbRead) {
        return new TestStateSystemRequest(stateProvider, timeRange, nbRead, fRequestAction);
    }

    private class TestStateSystemRequest extends StateSystemEventRequest {

        private final Function<ITmfEvent, ITmfEvent> fAction;

        public TestStateSystemRequest(ITmfStateProvider sp, TmfTimeRange timeRange, int index, Function<ITmfEvent, ITmfEvent> requestAction) {
            super(sp, timeRange, index);
            fAction = requestAction;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            ITmfEvent ev = NonNullUtils.checkNotNull(fAction.apply(event));
            super.handleData(ev);
        }

    }

}
