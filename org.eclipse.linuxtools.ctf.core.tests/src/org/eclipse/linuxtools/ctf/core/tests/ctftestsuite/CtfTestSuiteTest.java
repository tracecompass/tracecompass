/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Moved out of CTFTestTrace
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.ctftestsuite;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.junit.Test;

/**
 * Test class running the CTF Test Suite
 * (from https://github.com/efficios/ctf-testsuite).
 *
 * @author Matthew Khouzam
 */
public class CtfTestSuiteTest {

    private static final String TRACES_DIRECTORY = "../org.eclipse.linuxtools.ctf.core.tests/traces";
    private static final String METADATA_FILENAME = "metadata";

    private static final String CTF_VERSION_NUMBER = "1.8";
    private static final String CTF_SUITE_TEST_DIRECTORY = "ctf-testsuite/tests/" + CTF_VERSION_NUMBER;

    /**
     * Open traces in specified directories and expect them to fail
     *
     * @throws CTFReaderException not expected
     */
    @Test
    public void testFailedParse() throws CTFReaderException {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/fail"), true);
    }

    /**
     * Open traces in specified directories and expect them to succeed
     *
     * @throws CTFReaderException not expected
     */
    @Test
    public void testSuccessfulParse() throws CTFReaderException {
        parseTracesInDirectory(getTestTracesSubDirectory("kernel"), false);
        parseTracesInDirectory(getTestTracesSubDirectory("trace2"), false);
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/pass"), false);
    }


    /**
     * Get the File object for the subDir in the traces directory. If the sub directory doesn't exist, the test is skipped.
     */
    private static File getTestTracesSubDirectory(String subDir) {
        File file = new File(TRACES_DIRECTORY + "/" + subDir);
        assumeTrue(file.isDirectory());
        return file;
    }

    /**
     * Parse the traces in given directory recursively
     *
     * @param directory The directory to search in
     * @param expectException Whether or not traces in this directory are expected to throw an exception when parsed
     * @throws CTFReaderException
     */
    void parseTracesInDirectory(File directory, boolean expectException) throws CTFReaderException {
        for (File file : directory.listFiles()) {
            if (file.getName().equals(METADATA_FILENAME)) {
                try {
                    new CTFTrace(directory);
                    if (expectException) {
                        fail("Trace was expected to fail parsing: " + directory);
                    }
                } catch (RuntimeException e) {
                    if (!expectException) {
                        throw new CTFReaderException("Failed parsing " + directory, e);
                    }
                } catch (CTFReaderException e) {
                    if (!expectException) {
                        throw new CTFReaderException("Failed parsing " + directory, e);
                    }
                }
                return;
            }

            if (file.isDirectory()) {
                parseTracesInDirectory(file, expectException);
            }
        }
    }
}
