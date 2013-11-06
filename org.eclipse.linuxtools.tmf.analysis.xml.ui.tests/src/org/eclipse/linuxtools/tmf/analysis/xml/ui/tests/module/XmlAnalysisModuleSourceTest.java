/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.ui.tests.module;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.linuxtools.tmf.analysis.xml.ui.module.XmlAnalysisModuleSource;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link XmlAnalysisModuleSource} class
 *
 * @author Geneviève Bastien
 */
public class XmlAnalysisModuleSourceTest {

    private static final String SS_MODULE = "polymtl.kernel.sp";

    private static void emptyXmlFolder() {
        File fFolder = XmlUtils.getXmlFilesPath().toFile();
        if (!(fFolder.isDirectory() && fFolder.exists())) {
            return;
        }
        for (File xmlFile : fFolder.listFiles()) {
            xmlFile.delete();
        }
        XmlAnalysisModuleSource.notifyModuleChange();
    }

    /**
     * Empty the XML directory before the test, just in case
     */
    @Before
    public void setUp() {
        emptyXmlFolder();
    }

    /**
     * Empty the XML directory after the test
     */
    @After
    public void cleanUp() {
        emptyXmlFolder();
    }

    /**
     * Test the {@link XmlAnalysisModuleSource#getAnalysisModules()} method
     */
    @Test
    public void testPopulateModules() {
        XmlAnalysisModuleSource module = new XmlAnalysisModuleSource();

        Iterable<IAnalysisModuleHelper> modules = module.getAnalysisModules();
        assertFalse(modules.iterator().hasNext());

        /* use the valid XML test file */
        File testXmlFile = TmfXmlTestFiles.VALID_FILE.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("XML test file does not exist");
        }

        XmlUtils.addXmlFile(testXmlFile);
        XmlAnalysisModuleSource.notifyModuleChange();
        modules = module.getAnalysisModules();

        assertTrue(modules.iterator().hasNext());
        assertTrue(findStateSystemModule(modules));
    }

    private static boolean findStateSystemModule(Iterable<IAnalysisModuleHelper> modules) {
        for (IAnalysisModuleHelper helper : modules) {
            if (SS_MODULE.equals(helper.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test that XML modules are available through the analysis manager
     */
    @Test
    public void testPopulateModulesWithAnalysisManager() {

        /*
         * Make sure module sources are initialized. When run as unit test, the
         * XML module source is sometimes missing
         */
        TmfAnalysisManager.initializeModuleSources();

        Map<String, IAnalysisModuleHelper> modules = TmfAnalysisManager.getAnalysisModules();
        assertFalse(findStateSystemModule(modules.values()));

        /* use the valid XML test file */
        File testXmlFile = TmfXmlTestFiles.VALID_FILE.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("XML test file does not exist");
        }

        XmlUtils.addXmlFile(testXmlFile);
        XmlAnalysisModuleSource.notifyModuleChange();
        modules = TmfAnalysisManager.getAnalysisModules();
        assertTrue(findStateSystemModule(modules.values()));
    }
}
