/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.swtbot.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.junit.BeforeClass;

/**
 * Abstract custom parser
 * @author Matthew Khouzam
 *
 */
public class AbstractCustomParserWizard {

    /** The Log4j logger instance. */
    static final Logger fLogger = Logger.getRootLogger();

    /**
     * The SWTBot running the test
     */
    protected static SWTWorkbenchBot fBot;

    /** Test Class setup */
    @BeforeClass
    public static void init() {
        SWTBotUtil.failIfUIThread();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtil.closeView("welcome", fBot);

        SWTBotUtil.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        SWTBotUtil.waitForJobs();

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
        try (RandomAccessFile raf = new RandomAccessFile(xmlFile, "r");) {
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
}