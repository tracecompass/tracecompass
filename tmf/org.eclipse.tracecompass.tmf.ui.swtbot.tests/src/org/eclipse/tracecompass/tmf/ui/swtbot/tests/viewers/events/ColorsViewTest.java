/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.colors.ColorSetting;
import org.eclipse.tracecompass.tmf.ui.views.colors.ColorSettingsManager;
import org.eclipse.tracecompass.tmf.ui.views.colors.ColorsView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for Colors views in trace compass
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ColorsViewTest {

    private static final class PassAll implements ITmfFilterTreeNode {
        @Override
        public boolean matches(ITmfEvent event) {
            return true;
        }

        @Override
        public void setParent(ITmfFilterTreeNode parent) {

        }

        @Override
        public ITmfFilterTreeNode replaceChild(int index, ITmfFilterTreeNode node) {
            return null;
        }

        @Override
        public ITmfFilterTreeNode removeChild(ITmfFilterTreeNode node) {
            return null;
        }

        @Override
        public ITmfFilterTreeNode remove() {
            return null;
        }

        @Override
        public boolean hasChildren() {
            return false;
        }

        @Override
        public List<String> getValidChildren() {
            return Collections.emptyList();
        }

        @Override
        public ITmfFilterTreeNode getParent() {
            return null;
        }

        @Override
        public String getNodeName() {
            return "YES";
        }

        @Override
        public int getChildrenCount() {
            return 0;
        }

        @Override
        public @NonNull ITmfFilterTreeNode[] getChildren() {
            return null;
        }

        @Override
        public ITmfFilterTreeNode getChild(int index) {
            return null;
        }

        @Override
        public int addChild(ITmfFilterTreeNode node) {
            return 0;
        }

        @Override
        public ITmfFilterTreeNode clone() {
            return null;
        }

        @Override
        public String toString(boolean explicit) {
            return toString();
        }

        @Override
        public String toString() {
            return getNodeName();
        }
    }

    private static final String XMLSTUB_ID = "org.eclipse.linuxtools.tmf.core.tests.xmlstub";

    private static final String TRACE_START = "<trace>";
    private static final String EVENT_BEGIN = "<event timestamp=\"";
    private static final String EVENT_MIDDLE = " \" name=\"event\"><field name=\"field\" value=\"";
    private static final String EVENT_END = "\" type=\"int\" />" + "</event>";
    private static final String TRACE_END = "</trace>";

    private static final String PROJECT_NAME = "TestForFiltering";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    private static String makeEvent(int ts, int val) {
        return EVENT_BEGIN + Integer.toString(ts) + EVENT_MIDDLE + Integer.toString(val) + EVENT_END + "\n";
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
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(fFileLocation, "rw")) {
            braf.writeBytes(TRACE_START);
            for (int i = 0; i < 100; i++) {
                braf.writeBytes(makeEvent(i * 100, i % 4));
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
        SWTBotUtils.openView(ColorsView.ID);
    }

    /**
     * Delete the file
     */
    @AfterClass
    public static void cleanUp() {
        fLogger.removeAllAppenders();
        fFileLocation.delete();
    }

    /**
     * Close the editor
     */
    @After
    public void tearDown() {
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
        SWTBotUtils.closeViewById(ColorsView.ID, fBot);
    }

    /**
     * Test color by making all events yellow
     */
    @Test
    public void testYellow() {
        SWTBotView viewBot = fBot.viewById(ColorsView.ID);
        viewBot.setFocus();
        final String insert = "Insert new color setting";
        final String increasePriority = "Increase priority";
        final String decreasePriority = "Decrease priority";
        final String delete = "Delete color setting";
        viewBot.toolbarButton(insert).click();
        viewBot.toolbarButton(insert).click();
        viewBot.toolbarButton(insert).click();
        viewBot.toolbarButton(insert).click();
        viewBot.toolbarButton(increasePriority).click();
        viewBot.toolbarButton(decreasePriority).click();
        viewBot.toolbarButton(delete).click();
        viewBot.bot().label(0).setFocus();
        viewBot.toolbarButton(delete).click();
        viewBot.bot().label(0).setFocus();
        viewBot.toolbarButton(delete).click();
        final RGB foreground = new RGB(0, 0, 0);
        final RGB background = new RGB(255, 255, 0);
        // Simulate the side effects of picking a color because we cannot
        // control native Color picker dialog in SWTBot.
        final ColorSetting[] cs = new ColorSetting[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                cs[0] = new ColorSetting(foreground, background, foreground, new PassAll());
                ColorSettingsManager.setColorSettings(cs);
            }
        });
        final SWTBotTable eventsEditor = SWTBotUtils.activeEventsEditor(fBot).bot().table();
        // should fix race condition of loading the trace
        WaitUtils.waitForJobs();
        eventsEditor.select(2);
        final SWTBotTableItem tableItem = eventsEditor.getTableItem(2);
        RGB fgc = UIThreadRunnable.syncExec(new Result<RGB>() {
            @Override
            public RGB run() {
                return tableItem.widget.getForeground().getRGB();
            }
        });
        RGB bgc = UIThreadRunnable.syncExec(new Result<RGB>() {
            @Override
            public RGB run() {
                return tableItem.widget.getBackground().getRGB();
            }
        });
        assertEquals("Fg", foreground, fgc);
        assertEquals("Bg", background, bgc);
        // reset color settings
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                ColorSettingsManager.setColorSettings(new ColorSetting[0]);
            }
        });
    }
}
