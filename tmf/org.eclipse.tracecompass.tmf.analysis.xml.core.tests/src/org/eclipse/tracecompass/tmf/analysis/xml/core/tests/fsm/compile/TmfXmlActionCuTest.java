/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlActionCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionSegment;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueConstant;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueEventField;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueTypedWrapper;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Test the DataDrivenAction compilation from XML
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class TmfXmlActionCuTest {

    private static final @NonNull String ACTION_START = "<action id=\"%s\">";
    private static final @NonNull String ACTION_END = "</action>";
    private static final @NonNull String SEGMENT_TYPE = "<segType>" +
            "   <segName> " +
            "       <stateValue type=\"string\" value=\"seg1\"/>" +
            "   </segName>" +
            "</segType> ";
    private static final @NonNull String SEGMENT_START = "<begin type=\"eventField\" value=\"timestamp\"/>";
    private static final @NonNull String SEGMENT_END = "<end type=\"eventField\" value=\"testField\" />";
    private static final @NonNull String SEGMENT_DURATION = "<duration type=\"eventField\" value=\"testField\" />";

    // Common values
    private static final @NonNull DataDrivenValue SEGMENT_START_VALUE = new DataDrivenValueTypedWrapper(new DataDrivenValueEventField(null, ITmfStateValue.Type.NULL, "timestamp"), ITmfStateValue.Type.LONG);
    private static final @NonNull DataDrivenValue SEGMENT_TIME_VALUE = new DataDrivenValueTypedWrapper(new DataDrivenValueEventField(null, ITmfStateValue.Type.NULL, "testField"), ITmfStateValue.Type.LONG);

    private static abstract class CompilationResult {

        public abstract @NonNull String getName();

        public abstract @NonNull String getXmlString();

        public abstract @Nullable DataDrivenAction getResult();

        @Override
        public String toString() {
            return getName();
        }
    }

    // Valid test cases

    private static final @NonNull CompilationResult SEGMENT_TYPE_WITH_END = new CompilationResult() {

        @Override
        public String getName() {
            return "type_and_end";
        }

        @Override
        public String getXmlString() {
            return String.format(ACTION_START, getName()) +
                    "   <segment>" + SEGMENT_TYPE +
                    "       <segTime>" + SEGMENT_START + SEGMENT_END + "</segTime>" +
                    "   </segment>" +
                    ACTION_END;
        }

        @Override
        public DataDrivenAction getResult() {
            return new DataDrivenActionSegment(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "seg1"),
                    SEGMENT_START_VALUE,
                    null,
                    SEGMENT_TIME_VALUE,
                    Collections.emptyMap());
        }

    };

    private static final @NonNull CompilationResult SEGMENT_TYPE_NO_TIME = new CompilationResult() {

        @Override
        public String getName() {
            return "just_type";
        }

        @Override
        public String getXmlString() {
            return String.format(ACTION_START, getName()) +
                    "   <segment>" + SEGMENT_TYPE + "</segment>" +
                    ACTION_END;
        }

        @Override
        public DataDrivenAction getResult() {
            return new DataDrivenActionSegment(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "seg1"),
                    null,
                    null,
                    null,
                    Collections.emptyMap());
        }

    };

    private static final @NonNull CompilationResult SEGMENT_TYPE_WITH_DURATION = new CompilationResult() {

        @Override
        public String getName() {
            return "type_and_duration";
        }

        @Override
        public String getXmlString() {
            return String.format(ACTION_START, getName()) +
            "   <segment>" + SEGMENT_TYPE +
            "       <segTime>" + SEGMENT_START + SEGMENT_DURATION + "</segTime>" +
            "   </segment>" +
            ACTION_END;
        }

        @Override
        public DataDrivenAction getResult() {
            return new DataDrivenActionSegment(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "seg1"),
                    SEGMENT_START_VALUE,
                    SEGMENT_TIME_VALUE,
                    null,
                    Collections.emptyMap());
        }

    };

    private static final @NonNull CompilationResult SEGMENT_INLINE_TYPE = new CompilationResult() {

        @Override
        public String getName() {
            return "inline_name";
        }

        @Override
        public String getXmlString() {
            return String.format(ACTION_START, getName()) +
                    "<segment><segType segName=\"seg2\" /></segment>" +
                    ACTION_END;
        }

        @Override
        public DataDrivenAction getResult() {
            return new DataDrivenActionSegment(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "seg2"),
                    null,
                    null,
                    null,
                    Collections.emptyMap());
        }

    };

 // Test some invalid use cases

    private static final @NonNull CompilationResult SEGMENT_END_AND_DURATION = new CompilationResult() {

        @Override
        public String getName() {
            return "invalid_segment_and_duration";
        }

        @Override
        public String getXmlString() {
            return String.format(ACTION_START, getName()) +
                    "   <segment>" + SEGMENT_TYPE +
                    "       <segTime>" + SEGMENT_START + SEGMENT_DURATION + SEGMENT_END + "</segTime>" +
                    "   </segment>" +
                    ACTION_END;
        }

        @Override
        public DataDrivenAction getResult() {
            // Invalid
            return null;
        }

    };

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { SEGMENT_TYPE_WITH_END },
                { SEGMENT_TYPE_NO_TIME },
                { SEGMENT_TYPE_WITH_DURATION },
                { SEGMENT_INLINE_TYPE },
                { SEGMENT_END_AND_DURATION },
        });
    }

    private final CompilationResult fExpected;

    /**
     * Constructor
     *
     * @param expected
     *            The expected result
     */
    public TmfXmlActionCuTest(CompilationResult expected) {
        fExpected = expected;
    }

    /**
     * Test the compilation of a valid action strings
     *
     * @throws SAXException
     *             Exception thrown by parser
     * @throws IOException
     *             Exception thrown by parser
     * @throws ParserConfigurationException
     *             Exception thrown by parser
     */
    @Test
    public void testAction() throws SAXException, IOException, ParserConfigurationException {
        AnalysisCompilationData data = new AnalysisCompilationData();

        Element xmlElement = TmfXmlTestUtils.getXmlElement(TmfXmlStrings.ACTION, fExpected.getXmlString());
        assertNotNull(xmlElement);
        String compiledAction = TmfXmlActionCu.compileNamedAction(data, xmlElement);
        if (fExpected.getResult() == null) {
            assertNull("Expected null action" + fExpected.getName(), compiledAction);
        } else {
            assertNotNull("Expected non null " + fExpected.getName(), compiledAction);
            TmfXmlActionCu action = data.getAction(fExpected.getName());
            assertNotNull(action);
            assertEquals(fExpected.getName() + " generated", fExpected.getResult(), action.generate());
        }
    }

}
