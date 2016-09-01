/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test refreshing a text trace after new content was added.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestRefreshTextTrace {

    private static final String PROJECT_NAME = "Test";
    private static final String TRACE_TYPE_SYSLOG = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";
    private static final String TIMESTAMP_FORMAT = "MMM dd HH:mm:ss";

    private static final long INITIAL_NB_EVENTS = 100;
    private static final int NEW_EVENTS_PER_REFRESH = 40000;
    private static final int NB_REFRESH = 3;
    private static final long SECOND_TO_MILLISECOND = 1000;
    private static final long MICROSECOND_TO_NANOSECOND = 1000000;
    private static final int INDEXING_TIMEOUT = 300000;

    private static final Calendar CURRENT = Calendar.getInstance();
    private static final String TRACE_LOCATION = TmfTraceManager.getTemporaryDirPath() + File.separator + "test.txt";
    private static SWTWorkbenchBot fBot;

    private long fNbWrittenEvents = 0;

    /** Test Class setup */
    @BeforeClass
    public static void init() {
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        SWTBotUtils.initialize();
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        Logger.getRootLogger().addAppender(new NullAppender());
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Test setup
     *
     * @throws Exception on error
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
     * Test refreshing a trace after new content was added.
     *
     * @throws IOException
     *             on error
     */
    @Test
    public void testRefresh() throws IOException {
        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(activeTrace);

        fBot.waitUntil(new NumberOfEventsCondition(activeTrace, INITIAL_NB_EVENTS));

        for (int i = 0; i < NB_REFRESH; i++) {
            appendToTrace(NEW_EVENTS_PER_REFRESH);

            // Refresh
            SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
            SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, activeTrace.getName());
            traceItem.contextMenu("Refresh").click();

            // Make sure the refresh is completed
            fBot.waitUntil(new NumberOfEventsCondition(activeTrace, getNbWrittenEvents()), INDEXING_TIMEOUT);
        }

        // Make sure the end of the table matches what we expect
        goToTableEnd();
        fBot.waitUntil(ConditionHelpers.selectionInEventsTable(fBot, getExpectedEndTimeStamp()));
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

    /**
     * Append a number of events to the trace.
     *
     * @param nbEvents
     *            the number of events to append
     * @throws IOException
     *             on error
     */
    protected void appendToTrace(long nbEvents) throws IOException {
        writeToTrace(nbEvents, true);
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

    /**
     * Get the expected time in nanosecs at the end of the trace, after
     * refreshing.
     *
     * @return the expected time in nanosecs at the end of the trace
     */
    protected long getExpectedEndTimeStamp() {
        Date date = new Date((fNbWrittenEvents - 1) * SECOND_TO_MILLISECOND);
        // Syslog fills in the year when parsing so we have to do it for the
        // expected time stamp as well
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, CURRENT.get(Calendar.YEAR));
        if (calendar.after(CURRENT)) {
            calendar.set(Calendar.YEAR, CURRENT.get(Calendar.YEAR) - 1);
        }
        return calendar.getTimeInMillis() * MICROSECOND_TO_NANOSECOND;
    }

    private static void goToTableEnd() {
        SWTBotEditor eventsEditor = SWTBotUtils.activeEventsEditor(fBot);
        eventsEditor.setFocus();
        eventsEditor.bot().table().pressShortcut(Keystrokes.END);
    }

    /**
     * Get the number of events written so far.
     *
     * @return the number of events written so far
     */
    protected long getNbWrittenEvents() {
        return fNbWrittenEvents;
    }
}
