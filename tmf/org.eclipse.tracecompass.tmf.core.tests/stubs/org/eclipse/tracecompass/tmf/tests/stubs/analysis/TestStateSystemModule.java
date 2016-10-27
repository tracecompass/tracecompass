/*******************************************************************************
 * Copyright (c) 2013, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * Test State System module
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class TestStateSystemModule extends TmfStateSystemAnalysisModule {

    private @Nullable TestStateSystemProvider fProvider = null;
    private boolean fThrottleEvents = false;
    private final boolean fOnDisk;

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

}
