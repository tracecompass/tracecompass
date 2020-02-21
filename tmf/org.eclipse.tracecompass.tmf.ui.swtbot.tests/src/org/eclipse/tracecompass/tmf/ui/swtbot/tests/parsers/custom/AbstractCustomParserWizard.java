/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.parsers.custom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Abstract custom parser
 *
 * @author Matthew Khouzam
 */
public class AbstractCustomParserWizard {

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * The SWTBot running the test
     */
    protected static SWTWorkbenchBot fBot;

    /** Test Class setup */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Test Class teardown
     */
    @AfterClass
    public static void terminate() {
        fLogger.removeAllAppenders();
    }

    /**
     * Extract test XML file
     *
     * @param xmlFile
     *            the XML file to open
     * @param category
     *            the category of the parser
     * @param definitionName
     *            the name of the definition
     * @return an XML string of the definition
     * @throws IOException
     *             Error reading the file
     * @throws FileNotFoundException
     *             File is not found
     */
    protected static String extractTestXml(File xmlFile, String category, String definitionName) throws IOException, FileNotFoundException {
        StringBuilder xmlPart = new StringBuilder();
        boolean started = false;
        try (BufferedRandomAccessFile raf = new BufferedRandomAccessFile(xmlFile, "r");) {
            String s = raf.readLine();
            while (s != null) {
                if (s.equals("<Definition category=\"" + category + "\" name=\"" + definitionName + "\">")) {
                    started = true;
                }
                if (started) {
                    if (s.equals("</Definition>")) {
                        break;
                    }
                    xmlPart.append(s);
                    xmlPart.append('\n');
                }
                s = raf.readLine();
            }
        }
        return xmlPart.toString();
    }

    /**
     * Waits until the XML file containing custom parser defintions contains the
     * expected content for the specified trace type
     */
    protected static class CustomDefinitionHasContent extends DefaultCondition {

        private final File fDefinitionFile;
        private final String fCategoryName;
        private final String fTypeName;
        private final String fExpectedContent;

        /**
         * Creates a condition that waits until the XML file hast the expected
         * content.
         *
         * @param definitionFile
         *            the XML definition file
         * @param categoryName
         *            the category name
         * @param typeName
         *            the trace type name
         * @param expectedContent
         *            the expected content
         */
        protected CustomDefinitionHasContent(File definitionFile, String categoryName, String typeName, String expectedContent) {
            fDefinitionFile = definitionFile;
            fCategoryName = categoryName;
            fTypeName = typeName;
            fExpectedContent = expectedContent;
        }

        @Override
        public boolean test() throws Exception {
            return extractTestXml(fDefinitionFile, fCategoryName, fTypeName).equals(fExpectedContent);
        }

        @Override
        public String getFailureMessage() {
            return "The file " +fDefinitionFile + " did not contain expected content for " + fCategoryName + ":" + fTypeName + ", Expected:" + fExpectedContent;
        }
    }
}