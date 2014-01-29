/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.tests.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for the {@link XmlUtils} class
 *
 * @author Geneviève Bastien
 */
public class XmlUtilsTest {

    /**
     * Empty the XML directory after the test
     */
    @After
    public void emptyXmlFolder() {
        File fFolder = XmlUtils.getXmlFilesPath().toFile();
        if (!(fFolder.isDirectory() && fFolder.exists())) {
            return;
        }
        for (File xmlFile : fFolder.listFiles()) {
            xmlFile.delete();
        }
    }

    /**
     * Test the {@link XmlUtils#getXmlFilesPath()} method
     */
    @Test
    public void testXmlPath() {
        IPath xmlPath = XmlUtils.getXmlFilesPath();

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IPath workspacePath = workspace.getRoot().getRawLocation();
        workspacePath = workspacePath.addTrailingSeparator()
                .append(".metadata").addTrailingSeparator().append(".plugins")
                .addTrailingSeparator()
                .append("org.eclipse.linuxtools.tmf.analysis.xml.core")
                .addTrailingSeparator().append("xml_files");

        assertEquals(xmlPath, workspacePath);
    }

    /**
     * test the {@link XmlUtils#xmlValidate(File)} method
     */
    @Test
    public void testXmlValidate() {
        File testXmlFile = TmfXmlTestFiles.VALID_FILE.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("XML test file does not exist");
        }
        IStatus status = XmlUtils.xmlValidate(testXmlFile);
        if (!status.isOK()) {
            fail(status.getMessage());
        }

        testXmlFile = TmfXmlTestFiles.INVALID_FILE.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("XML test file does not exist");
        }
        assertFalse(XmlUtils.xmlValidate(testXmlFile).isOK());
    }

    /**
     * test the {@link XmlUtils#addXmlFile(File)} method
     */
    @Test
    public void testXmlAddFile() {
        /* Check the file does not exist */
        IPath xmlPath = XmlUtils.getXmlFilesPath().addTrailingSeparator().append("test_valid.xml");
        File destFile = xmlPath.toFile();
        assertFalse(destFile.exists());

        /* Add test_valid.xml file */
        File testXmlFile = TmfXmlTestFiles.VALID_FILE.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("XML test file does not exist");
        }

        XmlUtils.addXmlFile(testXmlFile);
        assertTrue(destFile.exists());
    }

}
