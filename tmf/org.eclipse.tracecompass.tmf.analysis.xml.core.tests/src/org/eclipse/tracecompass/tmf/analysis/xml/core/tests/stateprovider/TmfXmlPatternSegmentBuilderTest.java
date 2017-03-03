/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlPatternSegmentBuilder;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.segment.TmfXmlPatternSegment;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module.XmlUtilsTest;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stubs.PatternSegmentFactoryStub;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stubs.StateSystemContainerStub;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Test suite for the XML pattern segment builder. The builder should create the
 * corresponding pattern segment :
 *
 * <p>
 * -name : "test"
 * </p>
 * <p>
 * -content :
 * </p>
 * <p>
 * &nbsp;&nbsp;&nbsp;&nbsp;-field1 : 5
 * </p>
 * <p>
 * &nbsp;&nbsp;&nbsp;&nbsp;-field2 : "test"
 * </p>
 * <p>
 * &nbsp;&nbsp;&nbsp;&nbsp;-field3 : 1
 * </p>
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlPatternSegmentBuilderTest {

    private static final @NonNull String ANALYSIS_ID = "xml test pattern segment";
    private final @NonNull StateSystemContainerStub fContainer = new StateSystemContainerStub();
    private final @NonNull ITmfXmlModelFactory fModelFactory = TmfXmlReadWriteModelFactory.getInstance();
    private File fTestXmlFile;

    /**
     * test the {@link XmlUtils#xmlValidate(File)} method
     */
    @Before
    public void testXmlValidate() {
        fTestXmlFile = TmfXmlTestFiles.VALID_PATTERN_SEGMENT.getFile();
        if ((fTestXmlFile == null) || !fTestXmlFile.exists()) {
            fail("XML pattern test file does not exist");
        }
        IStatus status = XmlUtils.xmlValidate(fTestXmlFile);
        if (!status.isOK()) {
            fail(status.getMessage());
        }
    }

    /**
     * Create a pattern segment builder that will generate a pattern segment.
     * This method test the data of the pattern segment created.
     */
    @Test
    public void testBuilder() {
        Element doc = TmfXmlUtils.getElementInFile(fTestXmlFile.getPath(), TmfXmlStrings.PATTERN, ANALYSIS_ID);
        NodeList patternSegments = doc.getElementsByTagName(TmfXmlStrings.SEGMENT);
        assertEquals("Number of pattern segments", 2, patternSegments.getLength());

        final Node item2 = patternSegments.item(1);
        assertNotNull("pattern segment 2", item2);
        // create a pattern segment builder using the second pattern segment description in the XML pattern file
        TmfXmlPatternSegmentBuilder builder = new TmfXmlPatternSegmentBuilder(fModelFactory, (Element) item2, fContainer);
        assertNotNull("builder", builder);

        //Create a pattern segment and test its content
        TmfXmlPatternSegment segment = builder.generatePatternSegment(PatternSegmentFactoryStub.TEST_2_END_EVENT,
                PatternSegmentFactoryStub.TEST_2_START_EVENT.getTimestamp(),
                PatternSegmentFactoryStub.TEST_2_END_EVENT.getTimestamp(),
                null);
        XmlUtilsTest.testPatternSegmentData(PatternSegmentFactoryStub.TEST_2, segment);
    }
}
