/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test reading a trace in raw and event modes.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class RawTextEditorTest {

    private static final RGB WHITE = new RGB(255, 255, 255);
    private static final RGB HIGHLIGHT_COLOR = new RGB(231, 246, 254);
    private static final String PROJECT_NAME = "Test";
    private static final String TRACE_TYPE_SYSLOG = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";
    private static final String TIMESTAMP_FORMAT = "MMM dd HH:mm:ss";

    private static final long INITIAL_NB_EVENTS = 100;
    private static final long SECOND_TO_MILLISECOND = 1000;

    private static final String TRACE_LOCATION = TmfTraceManager.getTemporaryDirPath() + File.separator + "test.txt";
    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private long fNbWrittenEvents = 0;

    /** Test Class setup */
    @BeforeClass
    public static void init() {
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        SWTBotUtils.initialize();
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new NullAppender());
        fBot = new SWTWorkbenchBot();

        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Clean up
     */
    @AfterClass
    public static void afterClass() {
        fLogger.removeAllAppenders();
    }

    /**
     * Test setup
     *
     * @throws Exception
     *             on error
     */
    @Before
    public void before() throws Exception {
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotUtils.openTrace(PROJECT_NAME, createTrace(INITIAL_NB_EVENTS), getTraceType());
        WaitUtils.waitForJobs();
    }

    /**
     * Test tear down
     */
    @After
    public void after() {
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    /**
     * Test going to raw and back
     */
    @Test
    public void testRead() {
        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();

        fBot.waitUntil(new NumberOfEventsCondition(activeTrace, INITIAL_NB_EVENTS));

        SWTBotEditor eventsEditor = SWTBotUtils.activeEventsEditor(fBot);
        eventsEditor.setFocus();
        assertFalse(eventsEditor.bot().table().rowCount() == 0);
        eventsEditor.bot().table().select(4);
        eventsEditor.bot().table().getTableItem(4).contextMenu("Show Raw").click();
        eventsEditor.bot().table().getTableItem(4).click();
        final SWTBotStyledText rawViewer = eventsEditor.bot().styledText();

        String selection = rawViewer.getText();
        assertEquals(":03 HostF LoggerF: SourceFileF:9 Message F", selection.substring(12, 54));
        rawViewer.pressShortcut(Keystrokes.DOWN);
        final ICondition colorIsNotHighlight = new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return !HIGHLIGHT_COLOR.equals(rawViewer.getLineBackground(0));
            }

            @Override
            public String getFailureMessage() {
                return "Timed out";
            }
        };
        final ICondition colorIsHighlight = new DefaultCondition() {
            @Override
            public boolean test() {
                return HIGHLIGHT_COLOR.equals(rawViewer.getLineBackground(0));
            }

            @Override
            public String getFailureMessage() {
                return "Timed out";
            }
        };
        fBot.waitUntil(colorIsNotHighlight);
        assertEquals("Non-highlighted color", WHITE, rawViewer.getLineBackground(0));
        assertEquals("Highlighted color", HIGHLIGHT_COLOR, rawViewer.getLineBackground(1));
        rawViewer.pressShortcut(Keystrokes.UP);
        fBot.waitUntil(colorIsHighlight);
        assertEquals("Highlighted color", HIGHLIGHT_COLOR, rawViewer.getLineBackground(0));
        rawViewer.pressShortcut(Keystrokes.DOWN);
        fBot.waitUntil(colorIsNotHighlight);
        assertEquals("Non-highlighted color", WHITE, rawViewer.getLineBackground(0));
        assertEquals("Highlighted color", HIGHLIGHT_COLOR, rawViewer.getLineBackground(1));
        rawViewer.pressShortcut(Keystrokes.PAGE_UP);
        fBot.waitUntil(colorIsHighlight);
        assertEquals("Highlighted color", HIGHLIGHT_COLOR, rawViewer.getLineBackground(0));
        rawViewer.pressShortcut(SWT.CTRL, SWT.END, ' ');
        assertEquals("Highlighted color", HIGHLIGHT_COLOR, rawViewer.getLineBackground(0));
        rawViewer.pressShortcut(Keystrokes.UP);
        rawViewer.pressShortcut(Keystrokes.PAGE_DOWN);
        fBot.waitUntil(colorIsNotHighlight);
        assertEquals("Non-highlighted color", WHITE, rawViewer.getLineBackground(0));
        assertEquals("Highlighted color", HIGHLIGHT_COLOR, rawViewer.getLineBackground(1));
        rawViewer.pressShortcut(SWT.CTRL, SWT.HOME, ' ');
        fBot.waitUntil(colorIsHighlight);
        assertEquals("Highlighted color", HIGHLIGHT_COLOR, rawViewer.getLineBackground(0));
        eventsEditor.bot().table().getTableItem(5).click();
        eventsEditor.bot().table().getTableItem(4).contextMenu("Hide Raw").click();
        assertFalse(rawViewer.isActive());
    }

    private static class NumberOfEventsCondition extends DefaultCondition {

        private ITmfTrace fTrace;
        private long fNbEvents;

        private NumberOfEventsCondition(ITmfTrace trace, long nbEvents) {
            fTrace = trace;
            fNbEvents = nbEvents;
        }

        @Override
        public boolean test() throws Exception {
            return fTrace.getNbEvents() == fNbEvents;
        }

        @Override
        public String getFailureMessage() {
            return fTrace.getName() + " did not contain the expected number of " + fNbEvents + " events. Current: " + fTrace.getNbEvents();
        }
    }

    /**
     * Create a trace with a number of events.
     *
     * @param nbEvents
     *            the number of events to generate
     * @return the path to the created trace
     * @throws Exception
     *             on error
     */
    protected String createTrace(long nbEvents) throws Exception {
        writeToTrace(nbEvents, false);
        return TRACE_LOCATION;
    }

    private void writeToTrace(long nbEvents, boolean append) throws IOException {
        final File file = new File(TRACE_LOCATION);
        try (FileWriter writer = new FileWriter(file, append)) {
            for (int i = 0; i < nbEvents; ++i) {
                writeEvent(writer);
            }
        }
    }

    private void writeEvent(FileWriter fw) throws IOException {
        SimpleDateFormat f = new SimpleDateFormat(TIMESTAMP_FORMAT);
        String timeStampStr = f.format(new Date(fNbWrittenEvents * SECOND_TO_MILLISECOND));
        fw.write(timeStampStr + " HostF LoggerF: SourceFileF:9 Message F\n");
        fNbWrittenEvents++;
    }

    /**
     * Get the trace type for the test.
     *
     * @return the trace type
     */
    protected String getTraceType() {
        return TRACE_TYPE_SYSLOG;
    }
}
