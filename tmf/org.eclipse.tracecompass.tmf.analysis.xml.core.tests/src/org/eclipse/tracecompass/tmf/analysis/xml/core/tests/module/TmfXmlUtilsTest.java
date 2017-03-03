/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Test the {@link TmfXmlUtils} utility class
 *
 * @author Geneviève Bastien
 */
public class TmfXmlUtilsTest {

    private static final @NonNull String ANALYSIS_ID = "kernel.linux.sp";

    /**
     * Test the {@link TmfXmlUtils#getElementInFile(String, String, String)} method
     */
    @Test
    public void testGetElementInFile() {
        File testXmlFile = TmfXmlTestFiles.VALID_FILE.getFile();
        assertNotNull("XML test file does not exist", testXmlFile);
        assertTrue("XML test file does not exist", testXmlFile.exists());
        Element analysis = TmfXmlUtils.getElementInFile(testXmlFile.getAbsolutePath(), TmfXmlStrings.STATE_PROVIDER, ANALYSIS_ID);
        assertNotNull(analysis);
    }

    /**
     * Test the {@link TmfXmlUtils#getChildElements(Element, String)} method
     */
    @Test
    public void testGetChildElements() {
        File testXmlFile = TmfXmlTestFiles.VALID_FILE.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("XML test file does not exist");
        }
        /*
         * This sounds useless, but I get a potential null pointer warning
         * otherwise
         */
        if (testXmlFile == null) {
            return;
        }

        Element analysis = TmfXmlUtils.getElementInFile(testXmlFile.getAbsolutePath(), TmfXmlStrings.STATE_PROVIDER, ANALYSIS_ID);

        List<Element> values = TmfXmlUtils.getChildElements(analysis, TmfXmlStrings.LOCATION);
        assertEquals(5, values.size());

        Element aLocation = values.get(0);
        List<Element> attributes = TmfXmlUtils.getChildElements(aLocation, TmfXmlStrings.STATE_ATTRIBUTE);
        assertEquals(2, attributes.size());

        values = TmfXmlUtils.getChildElements(analysis, TmfXmlStrings.HEAD);
        assertEquals(1, values.size());

    }

}
