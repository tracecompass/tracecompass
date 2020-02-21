/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.fsm.compile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.AnalysisCompilationData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlStateValueCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.IBaseQuarkProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueConstant;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueEventField;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueEventName;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueQuery;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueScript;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Test the compilation of state values
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class TmfXmlStateValueCuTest {

    private static final @NonNull AnalysisCompilationData ANALYSIS_DATA = new AnalysisCompilationData();

    private static final DataDrivenValue QUERY_VALUE = new DataDrivenValueQuery(null, ITmfStateValue.Type.NULL,
            new DataDrivenStateSystemPath(ImmutableList.of(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "queryPath")), IBaseQuarkProvider.IDENTITY_BASE_QUARK));

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "Null state value", "<stateValue type=\"null\" />", TmfXmlTestUtils.NULL_VALUE },
                { "Valid integer 1", "<stateValue type=\"int\" value=\"42\" />", new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, 42) },
                { "Valid integer 2 with forced type", "<stateValue type=\"int\" value=\"42\" increment=\"bla\" forcedType=\"long\"/>", new DataDrivenValueConstant(null, ITmfStateValue.Type.LONG, 42) },
                { "Invalid integer", "<stateValue type=\"int\" value=\"abc\" />", null },
                { "Int no value", "<stateValue type=\"int\" />", null },
                { "Valid long 1", "<stateValue type=\"long\" value=\"4242424242\" />", new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, 4242424242L) },
                { "Valid long 2", "<stateValue type=\"long\" value=\"42\" increment=\"bla\" update=\"oui\"/>", new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, 42L) },
                { "Invalid long", "<stateValue type=\"long\" value=\"abc\" />", null },
                { "Long no value", "<stateValue type=\"long\" />", null },
                { "Valid string 1", "<stateValue type=\"string\" value=\"hello there my friend!\" />", new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "hello there my friend!") },
                { "Valid string 2", "<stateValue type=\"string\" value=\"oh! hello!\" increment=\"bla\" forcedType=\"int\"/>", new DataDrivenValueConstant(null, ITmfStateValue.Type.INTEGER, "oh! hello!") },
                { "String no value", "<stateValue type=\"string\" />", new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "") },
                { "Delete value", "<stateValue type=\"delete\" />", TmfXmlTestUtils.NULL_VALUE },
                { "Delete value with ignored params", "<stateValue type=\"delete\" forcedType=\"null\" id=\"ignored\" />", TmfXmlTestUtils.NULL_VALUE },
                { "Event name", "<stateValue type=\"eventName\" forcedType=\"null\" id=\"ignored\" />", new DataDrivenValueEventName(null) },
                { "Event name with params", "<stateValue type=\"eventName\" value=\"null\" id=\"ignored\" stack=\"push\"/>", new DataDrivenValueEventName(null) },
                { "Event field", "<stateValue type=\"eventField\" value=\"field\" forcedType=\"int\" />", new DataDrivenValueEventField(null, ITmfStateValue.Type.INTEGER, "field") },
                { "Unknown mapping group", "<stateValue type=\"eventField\" value=\"field\" mappingGroup=\"undefined\" />", null },
                { "Valid script", "<stateValue type=\"script\" value=\"a + b\" />", new DataDrivenValueScript(null, ITmfStateValue.Type.NULL, Collections.emptyMap(), "a + b", DataDrivenValueScript.DEFAULT_SCRIPT_ENGINE) },
                { "Script with children", "<stateValue type=\"script\" value=\"a + b\" ><stateValue id=\"a\" type=\"null\"/></stateValue>",
                        new DataDrivenValueScript(null, ITmfStateValue.Type.NULL, ImmutableMap.of("a", TmfXmlTestUtils.NULL_VALUE), "a + b", DataDrivenValueScript.DEFAULT_SCRIPT_ENGINE) },
                { "Script with invalid children", "<stateValue type=\"script\" value=\"a + b\" ><stateValue id=\"a\" type=\"int\" value=\"not int\"/></stateValue>", null },
                { "Query", "<stateValue type=\"query\" ><stateAttribute type=\"constant\" value=\"queryPath\"/></stateValue>", QUERY_VALUE },
                { "Query no children", "<stateValue type=\"query\" />", null },
                { "Query with invalid children", "<stateValue type=\"query\" ><stateAttribute type=\"constant\" /></stateValue>", null },

        });
    }

    private final String fXmlString;
    private final @Nullable Object fCompiles;
    private final String fTestName;

    /**
     * Constructor
     *
     * @param testName
     *            Explanation of the test
     * @param xmlString
     *            The XML state value to compile. It must be valid XML
     * @param generated
     *            The data value to be generated, can be <code>null</code> for
     *            invalid strings
     */
    public TmfXmlStateValueCuTest(String testName, String xmlString, @Nullable Object generated) {
        fTestName = testName;
        fXmlString = xmlString;
        fCompiles = generated;
    }

    /**
     * Test the compilation of a state value string
     *
     * @throws SAXException
     *             Exception thrown by parser
     * @throws IOException
     *             Exception thrown by parser
     * @throws ParserConfigurationException
     *             Exception thrown by parser
     */
    @Test
    public void testStateValueCompilation() throws SAXException, IOException, ParserConfigurationException {
        Element xmlElement = TmfXmlTestUtils.getXmlElement(TmfXmlStrings.STATE_VALUE, fXmlString);
        assertNotNull(xmlElement);
        TmfXmlStateValueCu compiledValue = TmfXmlStateValueCu.compileValue(ANALYSIS_DATA, xmlElement);
        if (fCompiles == null) {
            assertNull(compiledValue);
            return;
        }
        assertNotNull(compiledValue);
        assertEquals(fTestName, fCompiles, compiledValue.generate());
    }

}
