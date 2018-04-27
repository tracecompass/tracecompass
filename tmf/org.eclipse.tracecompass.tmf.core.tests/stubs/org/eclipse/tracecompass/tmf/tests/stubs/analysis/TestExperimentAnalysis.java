/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal
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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Stubs for experiment analysis. This analysis is a state system analysis that
 * simply counts the number of traces for which events were received. The number
 * of traces is the value of attribute
 * {@link TestExperimentAnalysis#TRACE_QUARK_NAME}.
 *
 * @author Geneviève Bastien
 */
public class TestExperimentAnalysis extends TmfStateSystemAnalysisModule {

    /**
     * The quark counting the number of traces
     */
    public static final String TRACE_QUARK_NAME = "Traces";

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new TestExpStateSystemProvider(checkNotNull(getTrace()));
    }

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.INMEM;
    }

    private class TestExpStateSystemProvider extends AbstractTmfStateProvider {

        private static final int VERSION = 1;
        private final Set<ITmfTrace> fTraces = new HashSet<>();
        private int fCount = 0;

        /**
         * Constructor
         *
         * @param trace
         *            The LTTng 2.0 kernel trace directory
         */
        public TestExpStateSystemProvider(@NonNull ITmfTrace trace) {
            super(trace, "Stub State System for Experiment");
        }

        @Override
        public int getVersion() {
            return VERSION;
        }

        @Override
        public ITmfStateProvider getNewInstance() {
            return new TestExpStateSystemProvider(this.getTrace());
        }

        @Override
        protected void eventHandle(ITmfEvent event) {
            ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());
            if (!fTraces.contains(event.getTrace())) {
                try {
                    int quarkId = ss.getQuarkAbsoluteAndAdd(TRACE_QUARK_NAME);
                    ss.modifyAttribute(event.getTimestamp().getValue(), ++fCount, quarkId);
                    fTraces.add(event.getTrace());
                } catch (TimeRangeException | StateValueTypeException e) {

                }
            }
        }
    }
}
