/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
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

package org.eclipse.tracecompass.tmf.core.tests.parsers.custom;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Malformed xml test, dangerous errors
 * @author Matthew Khouzam
 *
 */
@RunWith(Parameterized.class)
public class CustomXmlTraceBadlyFormedTest extends CustomXmlTraceTest {

    private final static String pathname = "testfiles/xml/malformed";

    /**
     * This should create the parameters to launch the project
     *
     * @return the path of the parameters
     */
    @Parameters(name = "{index}: path {0}")
    public static Collection<Object[]> getFiles() {
        File[] malformedFiles = (new File(pathname)).listFiles();
        Collection<Object[]> params = new ArrayList<>();
        for (File f : malformedFiles) {
            Object[] arr = new Object[] { f.getAbsolutePath() };
            params.add(arr);
        }
        return params;
    }

    /**
     * Test all the invalid xml files
     */
    @Test
    public void testBadlyFormed() {
        IStatus invalid = getTrace().validate(null, getPath());
        // Validation doesn't check for syntax errors. It returns a confidence
        // of  0 and status OK if it is a text file for malformed xml files.
        if ((IStatus.ERROR == invalid.getSeverity() ||
                ((IStatus.OK == invalid.getSeverity() && (invalid instanceof TraceValidationStatus) && ((TraceValidationStatus) invalid).getConfidence() == 0)))) {
            return;
        }
        fail(getPath());
    }

    /**
     * ctor
     *
     * @param filePath
     *            the path
     */
    public CustomXmlTraceBadlyFormedTest(String filePath) {
        this.setPath(filePath);
    }

}
