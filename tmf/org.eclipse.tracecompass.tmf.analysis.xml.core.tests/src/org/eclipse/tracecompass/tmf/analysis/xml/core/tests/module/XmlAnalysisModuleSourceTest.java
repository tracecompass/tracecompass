/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfAnalysisModuleHelperXml.XmlAnalysisModuleType;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.project.model.ITmfPropertiesProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Multimap;

/**
 * Test suite for the {@link XmlAnalysisModuleSource} class
 *
 * @author Geneviève Bastien
 */
public class XmlAnalysisModuleSourceTest {

    private static final String SS_MODULE = "kernel.linux.sp";
    private static final String BUILTIN_MODULE = "test.builtin.sp";

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
        assertFalse("Module present", findModule(modules, SS_MODULE));

        /* Test that the builtin module is present */
        assertTrue("builtin module present", findModule(modules, BUILTIN_MODULE));

        /* use the valid XML test file */
        File testXmlFile = TmfXmlTestFiles.VALID_FILE.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("XML test file does not exist");
        }

        XmlUtils.addXmlFile(testXmlFile);
        XmlAnalysisModuleSource.notifyModuleChange();
        modules = module.getAnalysisModules();

        assertTrue("Modules available from source", modules.iterator().hasNext());
        assertTrue("Module present after add file", findModule(modules, SS_MODULE));
        assertTrue("Builtin module present after add file", findModule(modules, BUILTIN_MODULE));
    }

    private static boolean findModule(Iterable<IAnalysisModuleHelper> modules, String moduleName) {
        for (IAnalysisModuleHelper helper : modules) {
            if (moduleName.equals(helper.getId())) {
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
        TmfAnalysisManager.initialize();

        Multimap<String, IAnalysisModuleHelper> modules = TmfAnalysisManager.getAnalysisModules();
        assertFalse(findModule(modules.values(), SS_MODULE));
        /* Test that the builtin module is present */
        assertTrue(findModule(modules.values(), BUILTIN_MODULE));

        /* use the valid XML test file */
        File testXmlFile = TmfXmlTestFiles.VALID_FILE.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("XML test file does not exist");
        }

        XmlUtils.addXmlFile(testXmlFile);
        XmlAnalysisModuleSource.notifyModuleChange();
        modules = TmfAnalysisManager.getAnalysisModules();
        assertTrue(findModule(modules.values(), SS_MODULE));
        assertTrue(findModule(modules.values(), BUILTIN_MODULE));
    }

    /**
     * Test that helper returns the right properties
     */
    @Test
    public void testProperties() {

        /* Get the helper for the builtin module */
        XmlAnalysisModuleSource module = new XmlAnalysisModuleSource();

        Iterable<IAnalysisModuleHelper> modules = module.getAnalysisModules();

        IAnalysisModuleHelper helper = StreamSupport.stream(modules.spliterator(), false)
                .filter(h -> h.getId().equals(BUILTIN_MODULE))
                .findFirst()
                .get();
        assertNotNull(helper);

        /* Verify the helper is a properties provider */
        assertTrue(helper instanceof ITmfPropertiesProvider);
        ITmfPropertiesProvider provider = (ITmfPropertiesProvider) helper;
        Map<String, String> properties = provider.getProperties();
        assertEquals(2, properties.size());

        /*
         * The text is externalized, so we're not sure about the property key,
         * but we know the values that should be present
         */
        assertTrue(properties.containsValue(XmlAnalysisModuleType.STATE_SYSTEM.name()));
        assertTrue(properties.containsValue("test_builtin.xml"));
    }

}
