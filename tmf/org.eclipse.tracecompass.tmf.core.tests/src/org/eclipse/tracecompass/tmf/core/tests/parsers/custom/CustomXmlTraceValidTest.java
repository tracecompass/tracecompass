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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Well formed XML
 * @author Matthew Khouzam
 *
 */
@RunWith(Parameterized.class)
public class CustomXmlTraceValidTest extends CustomXmlTraceTest {

    private final static String pathname = "testfiles/xml/valid";


    /**
     * This should create the parameters to launch the project
     *
     * @return the path of the parameters
     */
    @Parameters(name = "{index}: path {0}")
    public static Collection<Object[]> getFiles() {
        File[] validFiles = (new File(pathname)).listFiles();
        Collection<Object[]> params = new ArrayList<>();
        for (File f : validFiles) {
            Object[] arr = new Object[] { f.getAbsolutePath() };
            params.add(arr);
        }
        return params;
    }

    /**
     * Test all the invalid xml files
     */
    @Test
    public void testValid() {
        IStatus valid = getTrace().validate(null, getPath());
        if (IStatus.OK != valid.getSeverity()) {
            fail(valid.toString());
        }
    }

    /**
     * ctor
     *
     * @param filePath
     *            the path
     */
    public CustomXmlTraceValidTest(String filePath) {
        this.setPath(filePath);
    }

}
