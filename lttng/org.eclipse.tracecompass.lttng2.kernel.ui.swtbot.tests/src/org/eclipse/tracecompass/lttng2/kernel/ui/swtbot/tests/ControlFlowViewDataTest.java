/*******************************************************************************
 * Copyright (c) 2018 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowEntry;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeLinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * Test the {@link ControlFlowView} model (entry list, link list and the time
 * event values.
 *
 * FIXME consider removing me and replacing by a data provider test instead,
 * once it is merged.
 *
 * @author Loic Prieur-Drevon
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ControlFlowViewDataTest {

    private static final Bundle BUNDLE = Platform.getBundle("org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests");

    /** LTTng kernel trace type */
    private static final String KERNEL_TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    /** Default project name */
    private static final String TRACE_PROJECT_NAME = "test";

    private static final String EXPECTED_DIR = "resources" + File.separator + "controlFlowViewExpectedData" + File.separator;
    private static final String EXPECTED_LINK_LIST_PATH = EXPECTED_DIR + "getLinkList";
    private static final String EXPECTED_ENTRY_LIST_PATH = EXPECTED_DIR + "getEntryList";

    /** The workbench bot */
    private static SWTWorkbenchBot fBot;
    private static SWTBotView fViewBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Before Class
     */
    @BeforeClass
    public static void beforeClass() {
        SWTBotUtils.initialize();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", fBot);
        /* Switch perspectives */
        /* Create the trace project */
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        ITmfTrace kernelTestTrace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.CONTEXT_SWITCHES_KERNEL);
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, kernelTestTrace.getPath(), KERNEL_TRACE_TYPE);
        // wait for the Kernel Analysis to finish running
        WaitUtils.waitForJobs();

        SWTBotUtils.openView(ControlFlowView.ID);
        // wait for the ControlFlowView to build the entry list.
        WaitUtils.waitForJobs();
        fViewBot = fBot.viewByTitle("Control Flow");
        assertNotNull(fViewBot);
        fViewBot.show();
        fViewBot.setFocus();
    }

    /**
     * Close the editor
     */
    @AfterClass
    public static void tearDown() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.CONTEXT_SWITCHES_KERNEL);
        fLogger.removeAllAppenders();
    }

    /**
     * Test for the entry list, compares the list of entries for the trace,
     * asserting that their full path, start time, end time, PID, PPID and full
     * event list match the one in the expected file.
     *
     * @throws NoSuchMethodException
     *             if a matching method is not found.
     * @throws SecurityException
     *             If a security manager, s, is present and any of the following
     *             conditions is met:
     * @throws IllegalAccessException
     *             if this Method object is enforcing Java language access control
     *             and the underlying method is inaccessible.
     * @throws IllegalArgumentException
     *             if the specified object is null and the method is an instance
     *             method.
     * @throws InvocationTargetException
     *             if the underlying method throws an exception.
     * @throws IOException
     *             if an error occurred reading the expected values file.
     * @throws URISyntaxException
     *             if the test file URL is not formatted strictly according to to
     *             RFC2396 and cannot be converted to a URI.
     */
    @Test
    public void testEntryList() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, IOException, URISyntaxException {
        // get the trace (there should be only one)
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(trace);

        // get the ControlFlowView
        IWorkbenchPart part = fViewBot.getViewReference().getPart(false);
        assertTrue(part instanceof ControlFlowView);
        ControlFlowView controlFlowView = (ControlFlowView) part;

        // get and make the getEntryList method public with reflection
        Method getEntryList = AbstractTimeGraphView.class.getDeclaredMethod("getEntryList", ITmfTrace.class);
        getEntryList.setAccessible(true);
        Object entryList = getEntryList.invoke(controlFlowView, trace);
        assertTrue(entryList instanceof List<?>);

        Method zoomEntries = AbstractTimeGraphView.class.getDeclaredMethod("zoomEntries",
                Iterable.class, long.class, long.class, long.class, IProgressMonitor.class);
        zoomEntries.setAccessible(true);

        // read the expected values for the links from file
        URL entryListURL = BUNDLE.getResource(EXPECTED_ENTRY_LIST_PATH);
        List<String> expectedList = Files.readAllLines(Paths.get(FileLocator.toFileURL(entryListURL).toURI()));
        int i = 0;

        for (TimeGraphEntry root : Iterables.filter((List<?>) entryList, TimeGraphEntry.class)) {
            Iterable<TimeGraphEntry> flatten = Utils.flatten(root);
            // resample full entries to a controlled resolution that also works in CI
            zoomEntries.invoke(controlFlowView, flatten,
                    trace.getStartTime().toNanos(), trace.getEndTime().toNanos(), 1000000, new NullProgressMonitor());
            for (ControlFlowEntry entry : Iterables.filter(flatten, ControlFlowEntry.class)) {
                String entryString = getFullPath(entry) + "," + entry.getStartTime() + "," + entry.getEndTime()
                        + "," + entry.getThreadId() + "," + entry.getParentThreadId();
                entry.setZoomedEventList(null);

                Iterator<@NonNull ITimeEvent> iterator = entry.getTimeEventsIterator();
                Iterator<String> csv = Iterators.transform(iterator,
                        e -> e.getTime() + "," + e.getDuration() + "," + ((TimeEvent) e).getValue());
                String eventsString = Joiner.on(',').join(csv);
                assertEquals(expectedList.get(i), entryString + ":" + eventsString);
                // print the actual string above instead of asserting to generate new values
                i++;
            }
        }
        assertEquals("Wrong number of entries:", expectedList.size(), i);
    }

    /**
     * Test for the link list, asserting that the start, duration, value, source and
     * destination match the one in the expected test file.
     *
     * @throws NoSuchMethodException
     *             if a matching method is not found.
     * @throws SecurityException
     *             If a security manager, s, is present and any of the following
     *             conditions is met:
     * @throws IllegalAccessException
     *             if this Method object is enforcing Java language access control
     *             and the underlying method is inaccessible.
     * @throws IllegalArgumentException
     *             if the specified object is null and the method is an instance
     *             method.
     * @throws InvocationTargetException
     *             if the underlying method throws an exception.
     * @throws IOException
     *             if an error occurred reading the expected values file.
     * @throws URISyntaxException
     *             if the test file URL is not formatted strictly according to to
     *             RFC2396 and cannot be converted to a URI.
     */
    @Test
    public void testLinkList() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, IOException, URISyntaxException {
        // get the trace (there should only be one)
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(trace);

        // get the ControlFlowView
        IWorkbenchPart part = fViewBot.getViewReference().getPart(false);
        assertTrue(part instanceof ControlFlowView);
        ControlFlowView controlFlowView = (ControlFlowView) part;

        // get and make the getLinkList method public with reflection
        Method getLinkList = AbstractTimeGraphView.class.getDeclaredMethod("getLinkList", long.class, long.class, long.class, IProgressMonitor.class);
        getLinkList.setAccessible(true);

        /*
         * 1M is chosen to ensure that the output and inner lists are smaller than
         * Integer.MAX_VALUE.
         */
        Object linkList = getLinkList.invoke(controlFlowView, trace.getStartTime().toNanos(), trace.getEndTime().toNanos(), 1000000L, new NullProgressMonitor());
        assertTrue(linkList instanceof List<?>);

        // read the expected values for the links from file
        URL linkListURL = BUNDLE.getResource(EXPECTED_LINK_LIST_PATH);
        List<String> expectedList = Files.readAllLines(Paths.get(FileLocator.toFileURL(linkListURL).toURI()));
        int i = 0;

        /*
         * assert that each arrow has the right start, duration, value, origin and
         * destination
         */
        for (TimeLinkEvent arrow : Iterables.filter((List<?>) linkList, TimeLinkEvent.class)) {
            String actual = arrow + "," + getFullPath(arrow.getEntry()) + "," + getFullPath(arrow.getDestinationEntry());
            assertEquals(expectedList.get(i), actual);
            /*
             * print the actual string above instead of asserting to generate new test
             * results.
             */
            i++;
        }
        assertEquals("Wrong number of links:", expectedList.size(), i);
    }

    private static String getFullPath(ITimeGraphEntry entry) {
        StringBuilder b = new StringBuilder(entry.getName());
        ITimeGraphEntry parent = entry.getParent();
        while (parent != null) {
            b.insert(0, parent.getName() + '/');
            parent = parent.getParent();
        }
        return b.toString();
    }
}
