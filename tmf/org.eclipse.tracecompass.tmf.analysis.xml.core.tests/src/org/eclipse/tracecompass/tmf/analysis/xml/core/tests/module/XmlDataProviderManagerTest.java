/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.DataDrivenAnalysisModule;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlTimeGraphEntryModel;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.timegraph.TmfTimeGraphCompositeDataProvider;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlDataProviderManager;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Test the XML data provider manager
 *
 * @author Geneviève Bastien
 */
public class XmlDataProviderManagerTest {

    private static final @NonNull String TEST_TRACE = "test_traces/testTrace4.xml";
    private static final @NonNull String TEST_TRACE2 = "test_traces/testTrace5.xml";

    private static final @NonNull String EXPERIMENT_VIEW_ID = "test.xml.experiment.timegraph";
    private static final @NonNull String TRACE_VIEW_ID = "org.eclipse.linuxtools.tmf.analysis.xml.ui.views.statesystem";

    /**
     * Load the XML files for the current test
     */
    @Before
    public void setUp() {
        XmlUtils.addXmlFile(TmfXmlTestFiles.EXPERIMENT.getFile());
        XmlUtils.addXmlFile(TmfXmlTestFiles.STATE_VALUE_FILE.getFile());

        XmlAnalysisModuleSource.notifyModuleChange();
    }

    /**
     * Clean
     */
    public void cleanUp() {
        XmlUtils.deleteFile(TmfXmlTestFiles.EXPERIMENT.getFile().getName());
        XmlUtils.deleteFile(TmfXmlTestFiles.STATE_VALUE_FILE.getFile().getName());
        XmlAnalysisModuleSource.notifyModuleChange();
    }

    /**
     * Test getting the XML data provider for one trace, with an analysis that
     * applies to a trace
     */
    @Test
    public void testOneTrace() {
        ITmfTrace trace = null;
        try {
            // Initialize the trace and module
            trace = XmlUtilsTest.initializeTrace(TEST_TRACE);
            TmfTraceOpenedSignal signal = new TmfTraceOpenedSignal(this, trace, null);
            ((TmfTrace) trace).traceOpened(signal);
            // The data provider manager uses opened traces from the manager
            TmfTraceManager.getInstance().traceOpened(signal);

            // Get the view element from the file
            Element viewElement = TmfXmlUtils.getElementInFile(TmfXmlTestFiles.STATE_VALUE_FILE.getPath().toOSString(), TmfXmlStrings.TIME_GRAPH_VIEW, TRACE_VIEW_ID);
            assertNotNull(viewElement);
            ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel> timeGraphProvider = XmlDataProviderManager.getInstance().getTimeGraphProvider(trace, viewElement);
            assertNotNull(timeGraphProvider);

        } finally {
            if (trace != null) {
                trace.dispose();
                TmfTraceManager.getInstance().traceClosed(new TmfTraceClosedSignal(this, trace));
            }
        }

    }

    /**
     * Test getting the XML data provider for one trace, with an analysis that
     * applies to an experiment
     */
    @Test
    public void testOneTraceWithExperimentAnalysis() {
        ITmfTrace trace = null;
        try {
            // Initialize the trace and module
            trace = XmlUtilsTest.initializeTrace(TEST_TRACE);
            TmfTraceOpenedSignal signal = new TmfTraceOpenedSignal(this, trace, null);
            ((TmfTrace) trace).traceOpened(signal);
            // The data provider manager uses opened traces from the manager
            TmfTraceManager.getInstance().traceOpened(signal);

            // Get the view element from the file
            Element viewElement = TmfXmlUtils.getElementInFile(TmfXmlTestFiles.EXPERIMENT.getPath().toOSString(), TmfXmlStrings.TIME_GRAPH_VIEW, EXPERIMENT_VIEW_ID);
            assertNotNull(viewElement);
            ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel> timeGraphProvider = XmlDataProviderManager.getInstance().getTimeGraphProvider(trace, viewElement);
            assertNull(timeGraphProvider);

        } finally {
            if (trace != null) {
                trace.dispose();
                TmfTraceManager.getInstance().traceClosed(new TmfTraceClosedSignal(this, trace));
            }
        }

    }

    /**
     * Test getting the XML data provider for an experiment, with an analysis that
     * applies to a trace
     */
    @Test
    public void testExperimentWithTraceAnalysis() {
        ITmfTrace trace = null;
        ITmfTrace trace2 = null;
        ITmfTrace experiment = null;
        try {
            // Initialize the trace and module
            trace = XmlUtilsTest.initializeTrace(TEST_TRACE);
            trace2 = XmlUtilsTest.initializeTrace(TEST_TRACE2);
            ITmfTrace[] traces = { trace, trace2 };
            experiment = new TmfExperiment(ITmfEvent.class, "Xml Experiment", traces,
                    TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
            TmfTraceOpenedSignal signal = new TmfTraceOpenedSignal(this, experiment, null);
            ((TmfTrace) trace).traceOpened(signal);
            ((TmfTrace) trace2).traceOpened(signal);
            ((TmfTrace) experiment).traceOpened(signal);
            // The data provider manager uses opened traces from the manager
            TmfTraceManager.getInstance().traceOpened(signal);

            Iterable<@NonNull DataDrivenAnalysisModule> modules = TmfTraceUtils.getAnalysisModulesOfClass(experiment, DataDrivenAnalysisModule.class);
            modules.forEach(module -> {
                module.schedule();
                assertTrue(module.waitForCompletion());
            });

            // Get the view element from the file
            Element viewElement = TmfXmlUtils.getElementInFile(TmfXmlTestFiles.STATE_VALUE_FILE.getPath().toOSString(), TmfXmlStrings.TIME_GRAPH_VIEW, TRACE_VIEW_ID);
            assertNotNull(viewElement);
            ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel> timeGraphProvider = XmlDataProviderManager.getInstance().getTimeGraphProvider(experiment, viewElement);
            assertNotNull(timeGraphProvider);
            assertTrue(timeGraphProvider instanceof TmfTimeGraphCompositeDataProvider);

        } finally {
            if (trace != null) {
                trace.dispose();
            }
            if (trace2 != null) {
                trace2.dispose();
            }
            if (experiment != null) {
                experiment.dispose();
                TmfTraceManager.getInstance().traceClosed(new TmfTraceClosedSignal(this, experiment));
            }
        }

    }

    /**
     * Test getting the XML data provider for an experiment, with an analysis that
     * applies to an experiment
     */
    @Test
    public void testExperiment() {
        ITmfTrace trace = null;
        ITmfTrace trace2 = null;
        ITmfTrace experiment = null;
        try {
            // Initialize the trace and module
            trace = XmlUtilsTest.initializeTrace(TEST_TRACE);
            trace2 = XmlUtilsTest.initializeTrace(TEST_TRACE2);
            ITmfTrace[] traces = { trace, trace2 };
            experiment = new TmfExperiment(ITmfEvent.class, "Xml Experiment", traces,
                    TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
            TmfTraceOpenedSignal signal = new TmfTraceOpenedSignal(this, experiment, null);
            ((TmfTrace) trace).traceOpened(signal);
            ((TmfTrace) trace2).traceOpened(signal);
            ((TmfTrace) experiment).traceOpened(signal);
            // The data provider manager uses opened traces from the manager
            TmfTraceManager.getInstance().traceOpened(signal);

            Iterable<@NonNull DataDrivenAnalysisModule> modules = TmfTraceUtils.getAnalysisModulesOfClass(experiment, DataDrivenAnalysisModule.class);
            modules.forEach(module -> {
                module.schedule();
                assertTrue(module.waitForCompletion());
            });

            // Get the view element from the file
            Element viewElement = TmfXmlUtils.getElementInFile(TmfXmlTestFiles.EXPERIMENT.getPath().toOSString(), TmfXmlStrings.TIME_GRAPH_VIEW, EXPERIMENT_VIEW_ID);
            assertNotNull(viewElement);
            ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel> timeGraphProvider = XmlDataProviderManager.getInstance().getTimeGraphProvider(experiment, viewElement);
            assertNotNull(timeGraphProvider);
            assertFalse(timeGraphProvider instanceof TmfTimeGraphCompositeDataProvider);

        } finally {
            if (trace != null) {
                trace.dispose();
            }
            if (trace2 != null) {
                trace2.dispose();
            }
            if (experiment != null) {
                experiment.dispose();
                TmfTraceManager.getInstance().traceClosed(new TmfTraceClosedSignal(this, experiment));
            }
        }

    }

}
