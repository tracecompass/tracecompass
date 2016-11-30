/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.parsers.custom;

import java.io.File;

import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.junit.After;
import org.junit.Before;

/**
 * Abstract test parent
 *
 * @author Matthew Khouzam
 *
 */
public abstract class CustomXmlTraceTest {

    private static final String DEFINITION_PATH = "testfiles" + File.separator + "xml" + File.separator + "testDefinition.xml";

    private CustomXmlTraceDefinition cxtd;
    /**
     * The trace to use to "validate" the xml files
     */
    private CustomXmlTrace t;
    /**
     * The path of the trace
     */
    private String path;


    /**
     * set up definition
     */
    @Before
    public void init() {
        cxtd = createDefinition();
        t = new CustomXmlTrace(cxtd);
    }

    /**
     * clean up
     */
    @After
    public void cleanup() {
        if (t != null) {
            t.dispose();
        }
    }

    /**
     * @return the trace
     */
    public CustomXmlTrace getTrace() {
        return t;
    }


    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }


    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    private static CustomXmlTraceDefinition createDefinition() {
        CustomXmlTraceDefinition[] definitions = CustomXmlTraceDefinition.loadAll(new File(DEFINITION_PATH).toString());
        return definitions[0];
    }
}
