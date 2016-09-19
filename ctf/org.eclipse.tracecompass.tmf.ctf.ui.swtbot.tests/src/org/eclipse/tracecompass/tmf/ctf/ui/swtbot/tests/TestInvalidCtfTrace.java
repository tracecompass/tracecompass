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

package org.eclipse.tracecompass.tmf.ctf.ui.swtbot.tests;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertContains;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test invalid trace openning
 *
 * @author Matthew Khouzam
 */
@RunWith(Parameterized.class)
public class TestInvalidCtfTrace {

    private static final String PROJET_NAME = "TestInvalidCtfTraces";
    private static final Path BASE_PATH = Paths.get("../../ctf/org.eclipse.tracecompass.ctf.core.tests", "traces", "ctf-testsuite", "tests", "1.8");

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private static SWTWorkbenchBot fBot;

    private final File fLocation;

    private final String fExpectedMessage;

    private static final Map<String, String> ERRORS = new HashMap<>();
    static {
        // metadata
        ERRORS.put("array-redefinition", "MismatchedTokenException(56!=71)");
        ERRORS.put("array-size-identifier", "Is not an integer: x");
        ERRORS.put("array-size-keyword", "NoViableAltException(74@[])");
        ERRORS.put("array-size-negative", "Array length is negative");
        ERRORS.put("array-size-not-present", "NoViableAltException(13@[])");
        ERRORS.put("array-size-string", "Is not an integer: x");
        ERRORS.put("array-size-type-field", "Is not an integer: uint32_t");
        ERRORS.put("array-size-type", "Is not an integer: uint32_t");
        ERRORS.put("integer-encoding-as-string", "Invalid value for encoding");
        ERRORS.put("integer-encoding-invalid", "Invalid value for encoding");
        ERRORS.put("integer-negative-bit-size", "Invalid value for size");
        ERRORS.put("integer-range", "Invalid integer format: 23452397856348975623897562893746589237465289374658923764598237645897234658723648579236");
        ERRORS.put("integer-signed-as-string", "Invalid boolean value");
        ERRORS.put("integer-signed-invalid", "Invalid boolean value svp");
        ERRORS.put("integer-size-as-string", "Invalid value for size");
        ERRORS.put("integer-size-missing", "Invalid boolean value");
        ERRORS.put("struct-align-enum", "Invalid value for alignment");
        ERRORS.put("struct-align-huge", "Invalid integer format: 0xFFFFFFFFU");
        ERRORS.put("struct-align-negative", "Invalid value for alignment : -8");
        ERRORS.put("struct-align-string", "Invalid value for alignment");
        ERRORS.put("struct-align-zero", "Invalid value for alignment : 0");
        ERRORS.put("struct-duplicate-field-name", "Identifier has already been defined:xxx");
        ERRORS.put("struct-duplicate-struct-name", "struct a already defined.");
        ERRORS.put("struct-field-name-keyword", "NoViableAltException(72@[])");
        // streams
        ERRORS.put("content-size-larger-than-packet-size", "UNKNOWN"); //FIXME
        ERRORS.put("cross-packet-event-alignment-empty-struct", "UNKNOWN"); //FIXME
        ERRORS.put("cross-packet-event-alignment-integer", "UNKNOWN"); //FIXME
        ERRORS.put("cross-packet-event-array-of-integers", "UNKNOWN"); //FIXME
        ERRORS.put("cross-packet-event-float", "UNKNOWN"); //FIXME
        ERRORS.put("cross-packet-event-integer", "UNKNOWN"); //FIXME
        ERRORS.put("cross-packet-event-len-of-sequence", "UNKNOWN"); //FIXME
        ERRORS.put("cross-packet-event-sequence-between-elements", "UNKNOWN"); //FIXME
        ERRORS.put("cross-packet-event-sequence-start", "UNKNOWN"); //FIXME
        ERRORS.put("out-of-bound-empty-event-with-aligned-struct", "UNKNOWN"); //FIXME
        ERRORS.put("out-of-bound-float", "UNKNOWN"); //FIXME
        ERRORS.put("out-of-bound-integer", "UNKNOWN"); //FIXME
        ERRORS.put("out-of-bound-large-sequence-length", "UNKNOWN"); //FIXME
        ERRORS.put("out-of-bound-len-of-sequence", "UNKNOWN"); //FIXME
        ERRORS.put("out-of-bound-packet-header", "UNKNOWN"); //FIXME
        ERRORS.put("out-of-bound-sequence-between-elements", "UNKNOWN"); //FIXME
        ERRORS.put("out-of-bound-sequence-start", "UNKNOWN"); //FIXME
        ERRORS.put("out-of-bound-sequence-within-element", "UNKNOWN"); //FIXME

    }

    /**
     * Populate the parameters
     *
     * @return the parameters. Basically all the errors with lookuped paths
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getTracePaths() {
        final List<Object[]> dirs = new LinkedList<>();

        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                final Path fileName = dir.getFileName();
                String res = ERRORS.get(fileName.toString());
                if (res != null) {
                    dirs.add(new Object[] { dir.toFile(), res });
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

        };
        /* TODO: add way of handling an error in a trace during a run */
        /* when that is done, we can add regression/stream/fail */

        Path badMetadata = BASE_PATH.resolve(Paths.get("regression", "metadata", "fail"));
        try {
            Files.walkFileTree(badMetadata, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dirs;
    }

    /**
     * Constructor
     *
     * @param location
     *            trace file
     * @param errorMessage
     *            error message
     */
    public TestInvalidCtfTrace(File location, String errorMessage) {
        fLocation = location;
        fExpectedMessage = errorMessage;
    }

    /**
     * Initialization
     */
    @BeforeClass
    public static void beforeClass() {
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
        SWTBotUtils.waitForJobs();
        SWTBotUtils.createProject(PROJET_NAME);

    }

    /**
     * Delete traces
     */
    @After
    public void teardown() {
        SWTBotUtils.clearTracesFolder(fBot, PROJET_NAME);
        SWTBotUtils.closeSecondaryShells(fBot);
    }

    /**
     * Delete project
     */
    @AfterClass
    public static void afterClass() {
        SWTBotUtils.deleteProject(PROJET_NAME, fBot);
        fLogger.removeAllAppenders();
    }

    /**
     * Open an invalid trace and see the message
     */
    @Test
    public void testOpen() {
        SWTBotUtils.selectTracesFolder(fBot, PROJET_NAME);
        SWTBotUtils.openTrace(PROJET_NAME, fLocation.getAbsolutePath(), "org.eclipse.linuxtools.tmf.ui.type.ctf", false);
        fBot.waitUntil(Conditions.shellIsActive("Open Trace"));
        final SWTBotShell shell = fBot.activeShell();
        final SWTBot dialogBot = shell.bot();
        String text = dialogBot.label(1).getText();
        dialogBot.button().click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        assertContains(fExpectedMessage, text);

    }

}