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
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimePreferences;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
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
        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.TIME_ZONE, "GMT-05:00");
        TmfTimestampFormat.updateDefaultFormats();

        SWTBotUtils.closeView("welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
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

        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.TIME_ZONE, TmfTimePreferences.getDefaultPreferenceMap().get(ITmfTimePreferencesConstants.TIME_ZONE));
        TmfTimestampFormat.updateDefaultFormats();
    }

    /**
     * Test offsetting by 99 ns
     */
    @Test
    public void testOffsetting() {
        // Skip this test on Mac OS X 10.11.1 because of bug 481611
        // FIXME: Remove this work around once bug 481611 is fixed
        MacOsVersion macOsVersion = MacOsVersion.getMacOsVersion();
        boolean macBugPresent = macOsVersion != null && macOsVersion.compareTo(new MacOsVersion(10, 11, 1)) >= 0;
        assumeTrue(!macBugPresent);

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
        WaitUtils.waitForJobs();
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
        WaitUtils.waitForJobs();
        fBot.button("OK").click();

        // wait for trace to close
        fBot.waitWhile(ConditionHelpers.isEditorOpened(fBot, fLocation.getName()));

        // re-open trace
        SWTBotUtils.openTrace(PROJET_NAME, fLocation.getAbsolutePath(), "org.eclipse.linuxtools.tmf.core.tests.xmlstub");
        editor = fBot.editorByTitle(fLocation.getName());
        eventsTableBot = editor.bot().table();
        timestamp = eventsTableBot.cell(1, 1);
        assertEquals("19:01:39.000 000 000", timestamp);
        SWTBotUtils.deleteProject(PROJET_NAME, fBot);
    }

    /**
     * Class to store the Mac OS version.
     *
     * This could be moved if other tests start using this.
     */
    private static class MacOsVersion implements Comparable<MacOsVersion> {
        private static final Pattern MAC_OS_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

        private int fMajorVersion;
        private int fMinorVersion;
        private int fBugFixVersion;

        private static MacOsVersion fRunningMacOsVersion;

        private MacOsVersion(int version, int majorVersion, int patchVersion) {
            this.fMajorVersion = version;
            this.fMinorVersion = majorVersion;
            this.fBugFixVersion = patchVersion;
        }

        private int getMajorVersion() {
            return fMajorVersion;
        }

        private int getMinorVersion() {
            return fMinorVersion;
        }

        private int getBugFixVersion() {
            return fBugFixVersion;
        }

        private static MacOsVersion getMacOsVersion() {
            if (fRunningMacOsVersion != null) {
                return fRunningMacOsVersion;
            }

            if (!SWTUtils.isMac()) {
                return null;
            }

            String osVersion = System.getProperty("os.version");
            if (osVersion == null) {
                return null;
            }
            Matcher matcher = MAC_OS_VERSION_PATTERN.matcher(osVersion);
            if (matcher.matches()) {
                try {
                    fRunningMacOsVersion = new MacOsVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
                    return fRunningMacOsVersion;
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            return null;
        }

        @Override
        public int compareTo(MacOsVersion o) {
            int compareTo = Integer.compare(fMajorVersion, o.getMajorVersion());
            if (compareTo != 0) {
                return compareTo;
            }

            compareTo = Integer.compare(fMinorVersion, o.getMinorVersion());
            if (compareTo != 0) {
                return compareTo;
            }

            return Integer.compare(fBugFixVersion, o.getBugFixVersion());
        }
    }
}