/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.Activator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Provides some test XML files to use
 *
 * @author Geneviève Bastien
 */
public enum TmfXmlTestFiles {
    /** A valid XML test file */
    VALID_FILE("test_xml_files/test_valid/test_valid.xml"),
    /** An invalid test file */
    INVALID_FILE("test_xml_files/test_invalid/test_invalid.xml"),
    /** A valid file for state attribute tests */
    ATTRIBUTE_FILE("test_xml_files/test_valid/test_attributes.xml"),
    /** A valid file for conditions tests */
    CONDITION_FILE("test_xml_files/test_valid/test_conditions.xml"),
    /** A valid file for pattern tests */
    VALID_PATTERN_FILE("test_xml_files/test_valid/test_valid_pattern.xml");

    private final String fPath;

    private TmfXmlTestFiles(String file) {
        fPath = file;
    }

    /**
     * Get the absolute path of this test file
     *
     * @return The absolute path of this test file
     */
    public IPath getPath() {
        IPath absPath = Activator.getAbsolutePath(new Path(fPath));
        if (absPath == null) {
            fail("Cannot find file path for '" + fPath + "'");
        }
        return absPath;
    }

    /**
     * Returns the file object corresponding to the test XML file
     *
     * @return The file object for this test file
     */
    public File getFile() {
        return getPath().toFile();
    }

    /**
     * Get the XML {@link Document} for this test xml file
     *
     * @return The XML {@link Document}
     */
    public Document getXmlDocument() {
        /* Initialize the state provider module */
        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(getFile());
            doc.getDocumentElement().normalize();
        } catch (ParserConfigurationException e) {
            fail("Xml document parse exception");
        } catch (SAXException e) {
            fail("Exception parsing xml file");
        } catch (IOException e) {
            fail("File io exception");
        }
        return doc;
    }

}
