/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http:/www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Fix editor handling
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test trace offsetting
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestTraceOffsetting {

    private static final String TRACE_START = "<trace>";
    private static final String EVENT_BEGIN = "<event timestamp=\"";
    private static final String EVENT_MIDDLE = " \" name=\"event\"><field name=\"field\" value=\"";
    private static final String EVENT_END = "\" type=\"int\" />" + "</event>";
    private static final String TRACE_END = "</trace>";

    private static final String PROJET_NAME = "TestForOffsetting";
    private static final int NUM_EVENTS = 100;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    private static String makeEvent(int ts, int val) {
        return EVENT_BEGIN + Integer.toString(ts) + EVENT_MIDDLE + Integer.toString(val) + EVENT_END + "\n";
    }

    private File fLocation;

    /**
     * Initialization, creates a temp trace
     *
     * @throws IOException
     *             should not happen
     */
    @Before
    public void init() throws IOException {
        SWTBotUtils.failIfUIThread();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();
        fLocation = File.createTempFile("sample", ".xml");
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(fLocation, "rw")) {
            braf.writeBytes(TRACE_START);
            for (int i = 0; i < NUM_EVENTS; i++) {
                braf.writeBytes(makeEvent(i * 100, i % 4));
            }
            braf.writeBytes(TRACE_END);
        }
    }

    /**
     * Delete file
     */
    @After
    public void cleanup() {
        fLocation.delete();
        fLogger.removeAllAppenders();
    }

    /**
     * Test offsetting by 99 ns
     */
    @Test
    public void testOffsetting() {
        SWTBotUtils.createProject(PROJET_NAME);
        SWTBotTreeItem traceFolderItem = SWTBotUtils.selectTracesFolder(fBot, PROJET_NAME);
        SWTBotUtils.openTrace(PROJET_NAME, fLocation.getAbsolutePath(), "org.eclipse.linuxtools.tmf.core.tests.xmlstub");
        SWTBotEditor editor = fBot.editorByTitle(fLocation.getName());
        SWTBotTable eventsTableBot = editor.bot().table();
        String timestamp = eventsTableBot.cell(1, 1);
        assertEquals("19:00:00.000 000 000", timestamp);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, traceFolderItem, fLocation.getName());
        traceItem.select();
        traceItem.contextMenu("Apply Time Offset...").click();
        SWTBotUtils.waitForJobs();
        // set offset to 99 ns
        SWTBotShell shell = fBot.shell("Apply time offset");
        shell.setFocus();
        SWTBotTreeItem[] allItems = fBot.tree().getAllItems();
        final SWTBotTreeItem swtBotTreeItem = allItems[0];
        swtBotTreeItem.select();
        swtBotTreeItem.click(1);
        swtBotTreeItem.pressShortcut(KeyStroke.getInstance('9'));
        swtBotTreeItem.pressShortcut(KeyStroke.getInstance('9'));
        swtBotTreeItem.pressShortcut(KeyStroke.getInstance('\n'));
        SWTBotUtils.waitForJobs();
        fBot.button("OK").click();

        // wait for trace to close
        fBot.waitWhile(ConditionHelpers.isEditorOpened(fBot, fLocation.getName()));

        // re-open trace
        SWTBotUtils.openTrace(PROJET_NAME, fLocation.getAbsolutePath(), "org.eclipse.linuxtools.tmf.core.tests.xmlstub");
        editor = fBot.editorByTitle(fLocation.getName());
        eventsTableBot = editor.bot().table();
        timestamp = eventsTableBot.cell(1, 1);
        assertEquals("19:00:00.000 000 099", timestamp);
        SWTBotUtils.deleteProject(PROJET_NAME, fBot);
    }

}