/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.ui.swtbot.tests.trim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

/**
 * Test trimming experiments ({@link CtfTmfTrace#trim}).
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CtfTmfExperimentTrimmingTest {

    private static final String TRACE_TYPE = "org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace";

    private static final String PROJECT_NAME = "Test";

    /** The Log4j logger instance. */
    protected static final Logger fLogger = Logger.getRootLogger();

    /** Test timeout */
    @Rule
    public TestRule globalTimeout = new Timeout(2, TimeUnit.MINUTES);

    private TmfTimeRange fRequestedTraceCutRange;

    private ITmfTrace fOriginalExperiment;
    private ITmfTrace fNewExperiment;

    // ------------------------------------------------------------------------
    // Test instance maintenance
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public CtfTmfExperimentTrimmingTest() {
        // do nothing
    }

    /**
     * Setup before the test suite
     */
    @BeforeClass
    public static void beforeClass() {
        SWTBotUtils.initialize();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 50000; /* 50 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new NullAppender());

        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Test setup
     *
     * @throws IOException
     *             failed to load the file
     */
    @Before
    public void setup() throws IOException {
        String file = FileLocator.toFileURL(CtfTestTrace.TRACE_EXPERIMENT.getTraceURL()).getPath();
        File parentDir = new File(file);
        File[] traceFiles = parentDir.listFiles();
        ITmfTrace traceValidator = new CtfTmfTrace();
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotUtils.createProject(PROJECT_NAME);

        int openedTraces = 0;
        for (File traceFile : traceFiles) {
            String absolutePath = traceFile.getAbsolutePath();
            if (traceValidator.validate(null, absolutePath).isOK()) {
                SWTBotUtils.openTrace(PROJECT_NAME, absolutePath, TRACE_TYPE);
                bot.closeAllEditors();
                openedTraces++;
                if (openedTraces >= 16) {
                    break;
                }
            }
        }
        traceValidator.dispose();

        WaitUtils.waitForJobs();
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(bot, PROJECT_NAME);
        tracesFolder.contextMenu().menu("Open As Experiment...", "Generic Experiment").click();
        SWTBotUtils.activateEditor(bot, "Experiment");
        SWTBotTreeItem project = SWTBotUtils.selectProject(bot, PROJECT_NAME);
        SWTBotTreeItem experimentItem = SWTBotUtils.getTraceProjectItem(bot, project, "Experiments", "Experiment");
        experimentItem.select();
        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        ITmfTrace trace = traceManager.getActiveTrace();
        assertTrue(String.valueOf(trace), trace instanceof TmfExperiment);
        TmfExperiment experiment = (TmfExperiment) trace;
        assertNotNull(experiment);
        ITmfProjectModelElement elem = TmfProjectRegistry.findElement(experiment.getResource(), true);
        assertTrue(elem instanceof TmfExperimentElement);
        fOriginalExperiment = experiment;
        TmfTimeRange traceCutRange = getTraceCutRange(experiment);
        assertNotNull(traceCutRange);
        fRequestedTraceCutRange = traceCutRange;

        ITmfTimestamp requestedTraceCutEnd = traceCutRange.getEndTime();
        ITmfTimestamp requestedTraceCutStart = traceCutRange.getStartTime();
        assertTrue(experiment.getTimeRange().contains(traceCutRange));
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, requestedTraceCutStart, requestedTraceCutEnd, experiment));
        experimentItem.contextMenu("Export Time Selection as New Trace...").click();
        SWTBotShell shell = bot.shell("Export trace section to...").activate();
        SWTBot dialogBot = shell.bot();
        assertEquals("Experiment", dialogBot.text().getText());
        dialogBot.text().setText("Experiment-trimmed");
        dialogBot.button("OK").click();
        SWTBotEditor newExperiment = bot.editorByTitle("Experiment-trimmed");
        newExperiment.setFocus();
        fNewExperiment = traceManager.getActiveTrace();
        assertNotNull("No active trace", fNewExperiment);
        assertEquals("Incorrect active trace", "Experiment-trimmed", fNewExperiment.getName());
        WaitUtils.waitForJobs();
    }

    /**
     * Test teardown
     */
    @After
    public void tearDown() {
        if (fNewExperiment != null) {
            fNewExperiment = null;
        }
        if (fOriginalExperiment != null) {
            fOriginalExperiment = null;
        }
        new SWTWorkbenchBot().closeAllEditors();
    }

    /**
     * Get the range at which we should start cutting the trace. It should be
     * roughly 1/4 into the trace to 1/2 into the trace.
     */
    private static TmfTimeRange getTraceCutRange(ITmfTrace trace) {
        long start = trace.readStart().toNanos();
        long end = trace.readEnd().toNanos();

        long duration = end - start;
        return new TmfTimeRange(TmfTimestamp.fromNanos((duration / 4) + start), TmfTimestamp.fromNanos(((duration) / 2) + start));
    }

    // ------------------------------------------------------------------------
    // Test methods and helpers
    // ------------------------------------------------------------------------

    /**
     * Test that all expected events are present in the new trace.
     */
    @Test
    public void testTrimEvents() {
        ITmfTrace initialTrace = fOriginalExperiment;
        ITmfTrace trimmedTrace = fNewExperiment;
        assertNotNull(initialTrace);
        assertNotNull(trimmedTrace);
        ITmfContext trimmedContext = trimmedTrace.seekEvent(0);
        ITmfEvent trimmedEvent = trimmedTrace.getNext(trimmedContext);
        if (trimmedEvent == null) {
            // empty trace
            return;
        }

        /*
         * Verify the bounds of the new trace are fine. The actual trace can be
         * smaller than what was requested if there are no events exactly at the
         * bounds, but should not contain events outside of the requested range.
         */
        final long newTraceStartTime = trimmedTrace.readStart().toNanos();
        final long newTraceEndTime = trimmedTrace.readEnd().toNanos();

        assertTrue("Trimmed trace start time " + newTraceStartTime
                + " is earlier than the requested " + fRequestedTraceCutRange.getStartTime(),
                newTraceStartTime >= fRequestedTraceCutRange.getStartTime().toNanos());

        assertTrue("Trimmed trace end time " + newTraceEndTime
                + " is later than the requested " + fRequestedTraceCutRange.getEndTime(),
                newTraceEndTime <= fRequestedTraceCutRange.getEndTime().toNanos());

        /*
         * Verify that each trace event from the original trace in the given
         * time range is present in the new one.
         */
        TmfTimeRange traceCutRange = fRequestedTraceCutRange;
        ITmfTimestamp startTime = traceCutRange.getStartTime();
        ITmfContext initialContext = initialTrace.seekEvent(startTime);
        ITmfEvent initialEvent = initialTrace.getNext(initialContext);

        int count = 0;
        while (traceCutRange.contains(initialEvent.getTimestamp())) {
            assertNotNull("Expected event not present in trimmed trace: " + initialEvent+" at rank:" + count, trimmedEvent);
            assertEquals("Timestamp mismatch at rank:" + count, initialEvent.getTimestamp(), trimmedEvent.getTimestamp());

            initialEvent = initialTrace.getNext(initialContext);
            trimmedEvent = trimmedTrace.getNext(trimmedContext);
            count++;
        }

        assertTrue("Trimmed trace is too small", count <= trimmedTrace.getNbEvents());
    }
}
