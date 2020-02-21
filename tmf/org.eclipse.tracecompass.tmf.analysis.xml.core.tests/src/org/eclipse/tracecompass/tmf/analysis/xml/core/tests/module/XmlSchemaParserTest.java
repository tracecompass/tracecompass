/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.ITmfXmlSchemaParser;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stubs.ExternalAnalysisModuleStub;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * Test the extended XSD analysis mechanisms and {@link ITmfXmlSchemaParser}
 * with stub analyzes
 *
 * @author Geneviève Bastien
 */
public class XmlSchemaParserTest {

    private static final @NonNull String TEST_TRACE = "test_traces/testTrace4.xml";
    private static final @NonNull String MY_MODULE = "extended.my";
    private static final @NonNull String ABC_MODULE = "extended.abc";

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
     * Test getting the extra schema parsers from the extension point
     */
    @Test
    public void testGettingSchemaParser() {
        Collection<ITmfXmlSchemaParser> parsers = XmlUtils.getExtraSchemaParsers();

        assertTrue(!parsers.isEmpty());
    }

    /**
     * Test the {@link XmlAnalysisModuleSource#getAnalysisModules()} method
     */
    @Test
    public void testPopulateExtendedModules() {
        XmlAnalysisModuleSource source = new XmlAnalysisModuleSource();

        Iterable<IAnalysisModuleHelper> modules = source.getAnalysisModules();
        assertNull("Module not present", findModule(modules, MY_MODULE));

        /* use the valid extended XML test file */
        File testXmlFile = TmfXmlTestFiles.VALID_FILE_EXTENDED.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("XML test file does not exist");
        }

        XmlUtils.addXmlFile(testXmlFile);
        XmlAnalysisModuleSource.notifyModuleChange();
        modules = source.getAnalysisModules();

        assertTrue("Modules available from source", modules.iterator().hasNext());
        assertNotNull("'My' module present", findModule(modules, MY_MODULE));
        assertNotNull("'abc' module present", findModule(modules, ABC_MODULE));
    }

    private static @Nullable IAnalysisModuleHelper findModule(Iterable<IAnalysisModuleHelper> modules, String moduleName) {
        return Iterables.tryFind(modules, helper -> moduleName.equals(helper.getId())).orNull();
    }

    /**
     * Test the {@link XmlAnalysisModuleSource#getAnalysisModules()} method
     *
     * @throws TmfAnalysisException
     *             Propagates exceptions
     */
    @Test
    public void testExtendedModuleCreated() throws TmfAnalysisException {
        XmlAnalysisModuleSource source = new XmlAnalysisModuleSource();

        /* use the valid extended XML test file */
        File testXmlFile = TmfXmlTestFiles.VALID_FILE_EXTENDED.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("XML test file does not exist");
        }
        XmlUtils.addXmlFile(testXmlFile);
        XmlAnalysisModuleSource.notifyModuleChange();
        Iterable<IAnalysisModuleHelper> modules = source.getAnalysisModules();

        ITmfTrace trace = XmlUtilsTest.initializeTrace(TEST_TRACE);
        try {
            IAnalysisModuleHelper helper = findModule(modules, MY_MODULE);
            assertNotNull(helper);

            IAnalysisModule newModule = helper.newModule(trace);
            try {
                assertTrue("Extended module class", newModule instanceof ExternalAnalysisModuleStub);
                assertEquals("Extended module ID", MY_MODULE, newModule.getId());
            } finally {
                if (newModule != null) {
                    newModule.dispose();
                }
            }

            helper = findModule(modules, ABC_MODULE);
            assertNotNull(helper);

            newModule = helper.newModule(trace);
            try {
                assertTrue("Extended module class", newModule instanceof ExternalAnalysisModuleStub);
                assertEquals("Extended module ID", ABC_MODULE, newModule.getId());
            } finally {
                if (newModule != null) {
                    newModule.dispose();
                }
            }
        } finally {
            trace.dispose();
        }
    }

}
