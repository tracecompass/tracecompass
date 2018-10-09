/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCanvas;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDView;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.core.Frame;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for Sequence Diagram view in trace compass
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class SDViewTest {

    private static final String UML2DVIEW_ID = "org.eclipse.linuxtools.tmf.ui.tmfUml2SDSyncView";

    private static final String XMLSTUB_ID = "org.eclipse.linuxtools.tmf.core.tests.xmlstub";

    private static final String TRACE_START = "<trace>";
    private static final String EVENT_BEGIN = "<event timestamp=\"";
    private static final String EVENT_MIDDLE1 = " \" name=\"";
    private static final String EVENT_MIDDLE2 = "\">";
    private static final String FIELD_SENDER = "<field name=\"sender\" value=\"";
    private static final String FIELD_RECEIVER = "<field name=\"receiver\" value=\"";
    private static final String FIELD_SIGNAL = "<field name=\"signal\" value=\"";
    private static final String FIELD_END = "\" type=\"string\" />";
    private static final String EVENT_END = "</event>";
    private static final String TRACE_END = "</trace>";

    private static final String PROJECT_NAME = "TestForFiltering";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    private static String makeEvent(int ts, String eventName, String send, String recv, String signal) {
        return EVENT_BEGIN + Integer.toString(ts) + EVENT_MIDDLE1 + eventName + EVENT_MIDDLE2 + FIELD_SENDER + send + FIELD_END + FIELD_RECEIVER + recv + FIELD_END + FIELD_SIGNAL + signal + FIELD_END + EVENT_END + "\n";
    }

    private static File fFileLocation;

    /**
     * Initialization, creates a temp trace
     *
     * @throws IOException
     *             should not happen
     */
    @BeforeClass
    public static void init() throws IOException {
        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
        fFileLocation = File.createTempFile("sample", ".xml");
        String eventNames[] = { "test:SEND", "test:RECEIVE" };
        String targets[] = { "peer1", "peer2" };

        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(fFileLocation, "rw")) {
            braf.writeBytes(TRACE_START);
            for (int i = 0; i < 20000; i++) {
                braf.writeBytes(makeEvent(i * 100, eventNames[i % 2], targets[i % 2], targets[(i + 1) % 2], Integer.toString(i % 2 + 1000)));
            }
            braf.writeBytes(TRACE_END);
        }
    }

    /**
     * Open a trace in an editor
     */
    @Before
    public void beforeTest() {
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        assertNotNull(treeItem);
        SWTBotUtils.openTrace(PROJECT_NAME, fFileLocation.getAbsolutePath(), XMLSTUB_ID);
        SWTBotUtils.openView(UML2DVIEW_ID);
    }

    /**
     * Delete the file
     */
    @AfterClass
    public static void cleanUp() {
        SWTBotUtils.closeViewById(UML2DVIEW_ID, fBot);
        fFileLocation.delete();
        fLogger.removeAllAppenders();
    }

    /**
     * Close the editor
     */
    @After
    public void tearDown() {
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    /**
     * Test Sequence diagram view, counting the columns
     */
    @Test
    public void testSDView() {
        SWTBotView viewBot = fBot.viewById(UML2DVIEW_ID);
        SWTBotCanvas timeCompressionBar = viewBot.bot().canvas(0);
        SWTBotCanvas sdWidget = viewBot.bot().canvas(1);

        assertNotNull(viewBot);
        viewBot.setFocus();
        WaitUtils.waitForJobs();
        List<SWTBotToolbarButton> viewButtons = viewBot.getToolbarButtons();
        List<String> titles = new ArrayList<>();
        for (SWTBotToolbarButton buttonBot : viewButtons) {
            titles.add(buttonBot.getToolTipText());
        }

        final char commandKeyChar = (char) 0x2318;
        final String findShortcut = (Platform.getOS().equals(Platform.OS_MACOSX) ? commandKeyChar : "Ctrl+") + "F";
        String[] expected = { "Reset zoom factor", "Select",
                "Zoom in the diagram", "Zoom out the diagram",
                "Go to next page", "Go to previous page",
                "Go to first page", "Go to last page",
                "Find... (" + findShortcut + ")"
        };
        assertArrayEquals("Buttons", expected, titles.toArray(new String[0]));
        SDView view = (SDView) viewBot.getViewReference().getPart(false);
        ISDAdvancedPagingProvider pagingProvider = (ISDAdvancedPagingProvider) view.getSDPagingProvider();
        Frame frame = view.getFrame();
        assertEquals(2, frame.lifeLinesCount());

        timeCompressionBar.click();

        viewBot.toolbarButton("Select").click();
        sdWidget.click(0, 0);

        viewBot.toolbarButton("Zoom in the diagram").click();
        sdWidget.click();
        sdWidget.click();

        viewBot.toolbarButton("Zoom out the diagram").click();
        sdWidget.click();

        viewBot.toolbarButton("Reset zoom factor").click();
        sdWidget.click();

        assertEquals(0, pagingProvider.currentPage());
        viewBot.toolbarButton("Find... (" + findShortcut + ")").click();
        SWTBot findDialogBot = fBot.shell("Sequence Diagram Find").bot();
        findDialogBot.comboBox().setText("peer2");
        findDialogBot.checkBox("Lifeline").select();
        findDialogBot.checkBox("Interaction").deselect();
        findDialogBot.button("Find").click();
        findDialogBot.button("Close").click();
        SWTBotUtils.waitUntil(f -> f.getLifeline(1).isSelected(), frame, "Did not find lifeline");

        assertEquals(0, pagingProvider.currentPage());
        viewBot.toolbarButton("Find... (" + findShortcut + ")").click();
        findDialogBot = fBot.shell("Sequence Diagram Find").bot();
        findDialogBot.comboBox().setText("1001");
        findDialogBot.checkBox("Lifeline").deselect();
        findDialogBot.checkBox("Interaction").select();
        findDialogBot.button("Find").click();
        findDialogBot.button("Close").click();
        SWTBotUtils.waitUntil(f -> f.getSyncMessage(1).isSelected(), frame, "Did not find interaction");

        viewBot.viewMenu("Hide Patterns...").click();
        SWTBot hideDialogBot = fBot.shell("Sequence Diagram Hide Patterns").bot();
        hideDialogBot.button("Add...").click();
        SWTBot definitionBot = fBot.shell("Definition of Hide Pattern").bot();
        definitionBot.comboBox().setText("peer2");
        definitionBot.checkBox("Lifeline").select();
        definitionBot.checkBox("Interaction").deselect();
        definitionBot.button("Create").click();
        hideDialogBot.button("OK").click();
        SWTBotUtils.waitUntil(v -> v.getFrame().lifeLinesCount() == 1, view, "Did not hide lifeline");

        viewBot.viewMenu("Hide Patterns...").click();
        hideDialogBot = fBot.shell("Sequence Diagram Hide Patterns").bot();
        hideDialogBot.table().select("hide peer2 [Lifeline]");
        hideDialogBot.button("Remove").click();
        hideDialogBot.button("OK").click();
        SWTBotUtils.waitUntil(v -> v.getFrame().lifeLinesCount() == 2, view, "Did not show lifeline");

        viewBot.viewMenu("Configure Min Max...").click();
        SWTBot configurationBot = fBot.shell("TimeCompression bar configuration").bot();
        configurationBot.textWithLabel("Max time").setText("200");
        configurationBot.button("OK").click();
        viewBot.viewMenu("Configure Min Max...").click();
        configurationBot = fBot.shell("TimeCompression bar configuration").bot();
        configurationBot.button("Default").click();
        configurationBot.button("OK").click();

        assertEquals(0, pagingProvider.currentPage());
        viewBot.toolbarButton("Go to next page").click();
        SWTBotUtils.waitUntil(pp -> pp.currentPage() == 1, pagingProvider, "Did not change page");
        viewBot.toolbarButton("Go to previous page").click();
        SWTBotUtils.waitUntil(pp -> pp.currentPage() == 0, pagingProvider, "Did not change page");
        viewBot.toolbarButton("Go to last page").click();
        SWTBotUtils.waitUntil(pp -> pp.currentPage() == 1, pagingProvider, "Did not change page");
        viewBot.toolbarButton("Go to first page").click();
        SWTBotUtils.waitUntil(pp -> pp.currentPage() == 0, pagingProvider, "Did not change page");

        viewBot.viewMenu("Pages...").click();
        SWTBot pagesBot = fBot.shell("Sequence Diagram Pages").bot();
        pagesBot.text().setText("2");
        pagesBot.button("OK").click();
        SWTBotUtils.waitUntil(pp -> pp.currentPage() == 1, pagingProvider, "Did not change page");
    }
}
