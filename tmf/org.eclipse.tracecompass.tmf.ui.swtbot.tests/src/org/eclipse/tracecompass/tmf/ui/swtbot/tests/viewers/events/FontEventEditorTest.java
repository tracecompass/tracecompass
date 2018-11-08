/*******************************************************************************
 * Copyright (c) 2015, 2017 Ericsson
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for testing movable column feature.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class FontEventEditorTest {

    private static final String TRACE_PROJECT_NAME = "test";
    private static final String COLUMN_TRACE = "syslog_collapse";
    private static final String COLUMN_TRACE_PATH = "testfiles/" + COLUMN_TRACE;
    private static final String COLUMN_TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";

    private static File fTestFile = null;

    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();

        /* set up test trace */
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(COLUMN_TRACE_PATH), null);
        URI uri;
        try {
            uri = FileLocator.toFileURL(location).toURI();
            fTestFile = new File(uri);
        } catch (URISyntaxException | IOException e) {
            fail(e.getMessage());
        }

        assumeTrue(fTestFile.exists());

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
        fLogger.removeAllAppenders();
    }

    /**
     * Switch the font to system then back to default.
     */
    @Test
    public void testChangeFont() {
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);

        // Open the actual trace
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), COLUMN_TRACE_TYPE);
        SWTBotEditor editorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());

        SWTBotTable tableBot = editorBot.bot().table();

        // Maximize editor area
        SWTBotUtils.maximize(editorBot.getReference(), tableBot);
        tableBot.contextMenu("Show Raw").click();
        tableBot.setFocus();
        tableBot.click(4, 1);
        tableBot.select(4);

        SWTBotStyledText rawText = editorBot.bot().styledText();

        FontData font = getFont(rawText);

        SWTBotShell preferencesShell = SWTBotUtils.openPreferences(fBot);

        SWTBot bot = preferencesShell.bot();
        preferencesShell.activate();
        bot.text().setText("color");

        SWTBotTreeItem generalItem = bot.tree().getTreeItem("General");
        generalItem.click();
        generalItem.select();
        bot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable("Appearance", generalItem));
        SWTBotTreeItem appearanceNode = generalItem.getNode("Appearance");
        appearanceNode.click();
        appearanceNode.select();
        bot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable("Colors and Fonts", appearanceNode));
        SWTBotTreeItem colorAndFontNode = appearanceNode.getNode("Colors and Fonts");
        colorAndFontNode.click();
        colorAndFontNode.select();
        SWTBotTreeItem tracingItem = bot.tree(1).getTreeItem("Tracing");
        tracingItem.click();
        tracingItem.select();
        tracingItem.expand();
        // Get the raw viewer default which is "Text Font" and change it to "System Font"
        SWTBotTreeItem traceEventTableFont = tracingItem.getNode("Trace event raw text font (set to default: Text Font)");
        traceEventTableFont.click();
        traceEventTableFont.select();
        bot.button("Use System Font").click();
        bot.button("Apply").click();
        FontData font2 = getFont(rawText);
        assertFalse(font2.equals(font));
        // Reset the raw viewer to the "Text Font"
        bot.button("Reset").click();
        SWTBotUtils.pressOKishButtonInPreferences(fBot);
        assertEquals(getFont(rawText), font);
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
    }

    private static FontData getFont(final SWTBotStyledText rawText) {
        return UIThreadRunnable.syncExec(new Result<FontData>() {
            @Override
            public FontData run() {
                return rawText.widget.getFont().getFontData()[0];
            }
        });
    }
}
