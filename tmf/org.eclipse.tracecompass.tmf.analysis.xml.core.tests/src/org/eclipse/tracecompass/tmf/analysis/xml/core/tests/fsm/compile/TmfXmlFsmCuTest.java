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
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.AnalysisCompilationData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlActionCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlConditionCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlFsmStateCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlFsmStateCu.TmfXmlFsmCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionResetStoredFields;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionSegment;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionUpdateStoredFields;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition.ConditionOperator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsm;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmSimpleState;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmStateTransition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueConstant;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueEventField;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueEventName;
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
 * Test the DataDrivenAction compilation from XML
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class TmfXmlFsmCuTest {

    // String names for actions, tests and states
    private static final @NonNull String STATE1_NAME = "state1";
    private static final @NonNull String STATE2_NAME = "state2";
    private static final @NonNull String STATE_FINAL_NAME = "final";
    private static final @NonNull String ACTION1_NAME = "action1";
    private static final @NonNull String ACTION2_NAME = "action2";
    private static final @NonNull String TEST1_NAME = "test1";
    private static final @NonNull String TEST2_NAME = "test2";

    private static final @NonNull String FSM_START = "<fsm id=\"%s\" %s>";
    private static final @NonNull String FSM_END = "</fsm>";
    private static final @NonNull String STATE_START = "<state id=\"%s\">";
    private static final @NonNull String STATE_END = "</state>";
    private static final @NonNull String TRANSITION_STRING = "<transition %s %s %s %s %s %s/>";
    private static final @NonNull String SAVE_STORED_FIELDS = TmfXmlStrings.SAVE_STORED_FIELDS + "=\"true\"";
    private static final @NonNull String CLEAR_STORED_FIELDS = TmfXmlStrings.CLEAR_STORED_FIELDS + "=\"true\"";
    private static final @NonNull String EVENTS_STRING = TmfXmlStrings.EVENT + "=\"sys*\"";
    private static final @NonNull String ACTION_STRING = TmfXmlStrings.ACTION + "=\"%s\"";
    private static final @NonNull String TARGET_STRING = TmfXmlStrings.TARGET + "=\"%s\"";
    private static final @NonNull String TEST_STRING = TmfXmlStrings.COND + "=\"%s\"";
    private static final @NonNull String WRAPPER_STRING = "<doc>%s</doc>";

    // Dummy actions and conditions to use in the FSM strings
    private static final @NonNull String ACTION1 = "<action id=\"" + ACTION1_NAME + "\"><segment><segType segName=\"test\"/></segment></action>";
    private static final @NonNull String ACTION2 = "<action id=\"" + ACTION2_NAME + "\"><segment><segType segName=\"hello\"/></segment></action>";
    private static final @NonNull String TEST1 = "<test id=\"" + TEST1_NAME + "\"><if><condition><stateValue type=\"int\" value=\"2\"/><stateValue type=\"eventField\" value=\"cpu\" /></condition></if></test>";
    private static final @NonNull String TEST2 = "<test id=\"" + TEST2_NAME + "\"><if><condition><stateValue type=\"int\" value=\"0\"/><stateValue type=\"eventField\" value=\"cpu\" /></condition></if></test>";

    // Their generated values
    private static final @NonNull DataDrivenAction ACTION1_DD = new DataDrivenActionSegment(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "test"),
            null, null, null, Collections.emptyMap());
    private static final @NonNull DataDrivenAction ACTION2_DD = new DataDrivenActionSegment(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "hello"),
            null, null, null, Collections.emptyMap());
    private static final @NonNull DataDrivenCondition EVENT_CONDITION = new DataDrivenCondition.DataDrivenRegexCondition(Pattern.compile("sys.*"),
            new DataDrivenValueEventName(null));
    private static final @NonNull DataDrivenCondition TEST1_DD = new DataDrivenCondition.DataDrivenComparisonCondition(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, 2),
            new DataDrivenValueEventField(null, ITmfStateValue.Type.NULL, "cpu"), ConditionOperator.EQ);
    private static final @NonNull DataDrivenCondition TEST2_DD = new DataDrivenCondition.DataDrivenComparisonCondition(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, 0),
            new DataDrivenValueEventField(null, ITmfStateValue.Type.NULL, "cpu"), ConditionOperator.EQ);

    // Describe some test cases of valid actions

    private static abstract class CompilationResult {

        public abstract @NonNull String getName();

        public abstract @NonNull String getXmlString();

        public abstract @Nullable DataDrivenFsm getResult();

        @Override
        public String toString() {
            return getName();
        }
    }

    private static final @NonNull CompilationResult VALID_ONE_STATE_FINAL = new CompilationResult() {

        @Override
        public String getName() {
            return "one_state_final";
        }

        @Override
        public String getXmlString() {
            return String.format(FSM_START, getName(), "initial=\"" + STATE1_NAME + "\"") +
                    String.format(STATE_START, STATE1_NAME) +
                    STATE_END +
                    FSM_END;
        }

        @Override
        public DataDrivenFsm getResult() {
            DataDrivenFsmSimpleState state = new DataDrivenFsmSimpleState(STATE1_NAME, Collections.emptyList(), DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            return new DataDrivenFsm(getName(), state,
                    ImmutableMap.of(STATE1_NAME, state), DataDrivenCondition.TRUE_CONDITION, true, true);
        }

    };

    private static final @NonNull CompilationResult VALID_ONE_STATE_LOOP = new CompilationResult() {

        @Override
        public String getName() {
            return "one_state_loop";
        }

        @Override
        public String getXmlString() {
            return String.format(WRAPPER_STRING,
                    TEST1 + ACTION1 + String.format(FSM_START, getName(), "initial=\"" + STATE1_NAME + "\"") +
                            String.format(STATE_START, STATE1_NAME) +
                            String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, ACTION1_NAME), String.format(TEST_STRING, TEST1_NAME), String.format(TARGET_STRING, STATE1_NAME), StringUtils.EMPTY, StringUtils.EMPTY) +
                            STATE_END +
                            FSM_END);
        }

        @Override
        public DataDrivenFsm getResult() {
            DataDrivenFsmSimpleState state = new DataDrivenFsmSimpleState(STATE1_NAME,
                    Collections.singletonList(new DataDrivenFsmStateTransition(EVENT_CONDITION,
                            TEST1_DD,
                            STATE1_NAME,
                            Collections.singletonList(ACTION1_DD))),
                    DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            return new DataDrivenFsm(getName(), state,
                    ImmutableMap.of(STATE1_NAME, state), DataDrivenCondition.TRUE_CONDITION, true, true);
        }

    };

    private static final @NonNull CompilationResult VALID_SAVE_CLEAR_FIELDS = new CompilationResult() {

        @Override
        public String getName() {
            return "save_and_clear_fields";
        }

        @Override
        public String getXmlString() {
            return String.format(WRAPPER_STRING,
                    TEST1 + ACTION1 + String.format(FSM_START, getName(), "initial=\"" + STATE1_NAME + "\"") +
                            String.format(STATE_START, STATE1_NAME) +
                            String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, ACTION1_NAME), String.format(TEST_STRING, TEST1_NAME), String.format(TARGET_STRING, STATE1_NAME), SAVE_STORED_FIELDS, CLEAR_STORED_FIELDS) +
                            STATE_END +
                            FSM_END);
        }

        @Override
        public DataDrivenFsm getResult() {
            DataDrivenFsmSimpleState state = new DataDrivenFsmSimpleState(STATE1_NAME,
                    Collections.singletonList(new DataDrivenFsmStateTransition(EVENT_CONDITION,
                            TEST1_DD,
                            STATE1_NAME,
                            ImmutableList.of(ACTION1_DD, DataDrivenActionUpdateStoredFields.getInstance(), DataDrivenActionResetStoredFields.getInstance()))),
                    DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            return new DataDrivenFsm(getName(), state,
                    ImmutableMap.of(STATE1_NAME, state), DataDrivenCondition.TRUE_CONDITION, true, true);
        }

    };

    private static final @NonNull CompilationResult VALID_MULTIPLE_ACTIONS_TESTS = new CompilationResult() {

        @Override
        public String getName() {
            return "multiple_actions_and_tests";
        }

        @Override
        public String getXmlString() {
            return String.format(WRAPPER_STRING,
                    TEST1 + TEST2 + ACTION1 + ACTION2 +
                            String.format(FSM_START, getName(), "initial=\"" + STATE1_NAME + "\"") +
                            String.format(STATE_START, STATE1_NAME) +
                            String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, ACTION1_NAME + ':' + ACTION2_NAME), String.format(TEST_STRING, TEST1_NAME + ':' + TEST2_NAME), String.format(TARGET_STRING, STATE1_NAME),
                                    StringUtils.EMPTY, StringUtils.EMPTY)
                            +
                            STATE_END +
                            FSM_END);
        }

        @Override
        public DataDrivenFsm getResult() {
            DataDrivenFsmSimpleState state = new DataDrivenFsmSimpleState(STATE1_NAME,
                    Collections.singletonList(new DataDrivenFsmStateTransition(EVENT_CONDITION,
                            new DataDrivenCondition.DataDrivenAndCondition(ImmutableList.of(TEST1_DD, TEST2_DD)),
                            STATE1_NAME,
                            ImmutableList.of(ACTION1_DD, ACTION2_DD))),
                    DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            return new DataDrivenFsm(getName(), state,
                    ImmutableMap.of(STATE1_NAME, state), DataDrivenCondition.TRUE_CONDITION, true, true);
        }

    };

    private static final @NonNull CompilationResult VALID_MULTIPLE_STATES_TRANSITIONS = new CompilationResult() {

        @Override
        public String getName() {
            return "multiple_states_and_transitions";
        }

        @Override
        public String getXmlString() {
            return String.format(WRAPPER_STRING,
                    TEST1 + TEST2 + ACTION1 + ACTION2 +
                            String.format(FSM_START, getName(), "initial=\"" + STATE1_NAME + "\"") +
                            String.format(STATE_START, STATE1_NAME) +
                            String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, ACTION2_NAME), String.format(TEST_STRING, TEST1_NAME + ':' + TEST2_NAME), String.format(TARGET_STRING, STATE1_NAME), StringUtils.EMPTY,
                                    StringUtils.EMPTY)
                            +
                            String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, ACTION1_NAME), String.format(TEST_STRING, TEST1_NAME), String.format(TARGET_STRING, STATE2_NAME), StringUtils.EMPTY, StringUtils.EMPTY) +
                            STATE_END +
                            String.format(STATE_START, STATE2_NAME) +
                            String.format(TRANSITION_STRING, EVENTS_STRING, StringUtils.EMPTY, String.format(TEST_STRING, TEST1_NAME + ':' + TEST2_NAME), String.format(TARGET_STRING, STATE_FINAL_NAME), StringUtils.EMPTY, StringUtils.EMPTY) +
                            String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, ACTION1_NAME), String.format(TEST_STRING, TEST2_NAME), String.format(TARGET_STRING, STATE1_NAME), StringUtils.EMPTY, StringUtils.EMPTY) +
                            STATE_END +
                            "<final id=\"" + STATE_FINAL_NAME + "\"/>" +
                            FSM_END);
        }

        @Override
        public DataDrivenFsm getResult() {
            DataDrivenFsmSimpleState state = new DataDrivenFsmSimpleState(STATE1_NAME,
                    ImmutableList.of(
                            new DataDrivenFsmStateTransition(EVENT_CONDITION,
                                    new DataDrivenCondition.DataDrivenAndCondition(ImmutableList.of(TEST1_DD, TEST2_DD)),
                                    STATE1_NAME,
                                    Collections.singletonList(ACTION2_DD)),
                            new DataDrivenFsmStateTransition(EVENT_CONDITION,
                                    TEST1_DD,
                                    STATE2_NAME,
                                    Collections.singletonList(ACTION1_DD))),
                    DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);

            DataDrivenFsmSimpleState state2 = new DataDrivenFsmSimpleState(STATE2_NAME,
                    ImmutableList.of(
                            new DataDrivenFsmStateTransition(EVENT_CONDITION,
                                    new DataDrivenCondition.DataDrivenAndCondition(ImmutableList.of(TEST1_DD, TEST2_DD)),
                                    STATE_FINAL_NAME,
                                    Collections.emptyList()),
                            new DataDrivenFsmStateTransition(EVENT_CONDITION,
                                    TEST2_DD,
                                    STATE1_NAME,
                                    Collections.singletonList(ACTION1_DD))),
                    DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            DataDrivenFsmSimpleState finalState = new DataDrivenFsmSimpleState(STATE_FINAL_NAME, Collections.emptyList(), DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            return new DataDrivenFsm(getName(), state,
                    ImmutableMap.of(STATE1_NAME, state, STATE2_NAME, state2, STATE_FINAL_NAME, finalState), DataDrivenCondition.TRUE_CONDITION, true, true);
        }

    };

    private static final @NonNull CompilationResult VALID_ON_ENTRY_EXIT = new CompilationResult() {

        @Override
        public String getName() {
            return "on_entry_exit";
        }

        @Override
        public String getXmlString() {
            return String.format(WRAPPER_STRING,
                    TEST1 + TEST2 + ACTION1 + ACTION2 +
                            String.format(FSM_START, getName(), "initial=\"" + STATE1_NAME + "\"") +
                            String.format(STATE_START, STATE1_NAME) +
                            "<onentry action=\"" + ACTION1_NAME + "\"/>" +
                            "<onexit action=\"" + ACTION2_NAME + "\"/>" +
                            String.format(TRANSITION_STRING, EVENTS_STRING, StringUtils.EMPTY, String.format(TEST_STRING, TEST1_NAME), String.format(TARGET_STRING, STATE_FINAL_NAME), StringUtils.EMPTY, StringUtils.EMPTY) +
                            STATE_END +
                            "<final id=\"" + STATE_FINAL_NAME + "\"/>" +
                            FSM_END);
        }

        @Override
        public DataDrivenFsm getResult() {
            DataDrivenFsmSimpleState state = new DataDrivenFsmSimpleState(STATE1_NAME,
                    Collections.singletonList(new DataDrivenFsmStateTransition(EVENT_CONDITION,
                            TEST1_DD,
                            STATE_FINAL_NAME,
                            Collections.emptyList())),
                    ACTION1_DD, ACTION2_DD);
            DataDrivenFsmSimpleState finalState = new DataDrivenFsmSimpleState(STATE_FINAL_NAME, Collections.emptyList(), DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            return new DataDrivenFsm(getName(), state,
                    ImmutableMap.of(STATE1_NAME, state, STATE_FINAL_NAME, finalState), DataDrivenCondition.TRUE_CONDITION, true, true);
        }

    };

    private static final @NonNull CompilationResult VALID_PRECONDITION = new CompilationResult() {

        @Override
        public String getName() {
            return "with_precondition";
        }

        @Override
        public String getXmlString() {
            return String.format(WRAPPER_STRING,
                    TEST1 + TEST2 + ACTION1 + ACTION2 +
                            String.format(FSM_START, getName(), "initial=\"" + STATE1_NAME + "\"") +
                            "<" + TmfXmlStrings.PRECONDITION + " " + EVENTS_STRING + " " + String.format(TEST_STRING, TEST1_NAME) + "/>" +
                            "<" + TmfXmlStrings.PRECONDITION + " " + TmfXmlStrings.EVENT + "=\"test\" " + String.format(TEST_STRING, TEST1_NAME) + " " + String.format(ACTION_STRING, ACTION1_NAME) + "/>" +
                            String.format(STATE_START, STATE1_NAME) +
                            String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, ACTION1_NAME + ':' + ACTION2_NAME), String.format(TEST_STRING, TEST1_NAME + ':' + TEST2_NAME), String.format(TARGET_STRING, STATE1_NAME),
                                    StringUtils.EMPTY, StringUtils.EMPTY) +
                            STATE_END +
                            FSM_END);
        }

        @Override
        public DataDrivenFsm getResult() {
            DataDrivenFsmSimpleState state = new DataDrivenFsmSimpleState(STATE1_NAME,
                    Collections.singletonList(new DataDrivenFsmStateTransition(EVENT_CONDITION,
                            new DataDrivenCondition.DataDrivenAndCondition(ImmutableList.of(TEST1_DD, TEST2_DD)),
                            STATE1_NAME,
                            ImmutableList.of(ACTION1_DD, ACTION2_DD))),
                    DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            DataDrivenCondition preCond = new DataDrivenCondition.DataDrivenOrCondition(
                    ImmutableList.of(
                            new DataDrivenCondition.DataDrivenAndCondition(ImmutableList.of(EVENT_CONDITION, TEST1_DD)),
                            new DataDrivenCondition.DataDrivenAndCondition(ImmutableList.of(
                                    new DataDrivenCondition.DataDrivenRegexCondition(Pattern.compile("test"),
                                    new DataDrivenValueEventName(null)), TEST1_DD))
                            ));
            return new DataDrivenFsm(getName(), state,
                    ImmutableMap.of(STATE1_NAME, state), preCond, true, true);
        }

    };

    private static final @NonNull CompilationResult VALID_INITIAL_STATE = new CompilationResult() {

        @Override
        public String getName() {
            return "initial state";
        }

        @Override
        public String getXmlString() {
            return String.format(WRAPPER_STRING,
                    TEST1 + TEST2 + ACTION1 + ACTION2 +
                            String.format(FSM_START, getName(), StringUtils.EMPTY) +
                            "<initialState>" +
                            String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, ACTION1_NAME), String.format(TEST_STRING, TEST1_NAME), String.format(TARGET_STRING, STATE1_NAME),
                                    StringUtils.EMPTY, StringUtils.EMPTY) +
                            "</initialState>" +
                            String.format(STATE_START, STATE1_NAME) +
                            String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, ACTION1_NAME + ':' + ACTION2_NAME), String.format(TEST_STRING, TEST1_NAME + ':' + TEST2_NAME), String.format(TARGET_STRING, STATE1_NAME),
                                    StringUtils.EMPTY, StringUtils.EMPTY) +
                            STATE_END +
                            FSM_END);
        }

        @Override
        public DataDrivenFsm getResult() {
            DataDrivenFsmSimpleState initial = new DataDrivenFsmSimpleState(TmfXmlStrings.INITIAL_STATE,
                    Collections.singletonList(new DataDrivenFsmStateTransition(EVENT_CONDITION,
                            TEST1_DD,
                            STATE1_NAME,
                            Collections.singletonList(ACTION1_DD))),
                    DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            DataDrivenFsmSimpleState state = new DataDrivenFsmSimpleState(STATE1_NAME,
                    Collections.singletonList(new DataDrivenFsmStateTransition(EVENT_CONDITION,
                            new DataDrivenCondition.DataDrivenAndCondition(ImmutableList.of(TEST1_DD, TEST2_DD)),
                            STATE1_NAME,
                            ImmutableList.of(ACTION1_DD, ACTION2_DD))),
                    DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            return new DataDrivenFsm(getName(), initial,
                    ImmutableMap.of(STATE1_NAME, state), DataDrivenCondition.TRUE_CONDITION, true, true);
        }

    };

    private static final @NonNull CompilationResult VALID_INITIAL = new CompilationResult() {

        @Override
        public String getName() {
            return "initial_element";
        }

        @Override
        public String getXmlString() {
            return String.format(WRAPPER_STRING,
                    TEST1 + ACTION1 + String.format(FSM_START, getName(), StringUtils.EMPTY) +
                            "<initial>" +
                            String.format(TRANSITION_STRING, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, String.format(TARGET_STRING, STATE1_NAME),
                                    StringUtils.EMPTY, StringUtils.EMPTY) +
                            "</initial>" +
                            String.format(STATE_START, STATE1_NAME) +
                            String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, ACTION1_NAME), String.format(TEST_STRING, TEST1_NAME), String.format(TARGET_STRING, STATE1_NAME), StringUtils.EMPTY, StringUtils.EMPTY) +
                            STATE_END +
                            FSM_END);
        }

        @Override
        public DataDrivenFsm getResult() {
            DataDrivenFsmSimpleState state = new DataDrivenFsmSimpleState(STATE1_NAME,
                    Collections.singletonList(new DataDrivenFsmStateTransition(EVENT_CONDITION,
                            TEST1_DD,
                            STATE1_NAME,
                            Collections.singletonList(ACTION1_DD))),
                    DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            return new DataDrivenFsm(getName(), state,
                    ImmutableMap.of(STATE1_NAME, state), DataDrivenCondition.TRUE_CONDITION, true, true);
        }

    };

    private static final @NonNull CompilationResult VALID_NO_INITIAL = new CompilationResult() {

        @Override
        public String getName() {
            return "valid_no_initial";
        }

        @Override
        public String getXmlString() {
            return String.format(FSM_START, getName(), StringUtils.EMPTY) +
                    String.format(STATE_START, STATE1_NAME) +
                    STATE_END +
                    FSM_END;
        }

        @Override
        public DataDrivenFsm getResult() {
            DataDrivenFsmSimpleState state = new DataDrivenFsmSimpleState(STATE1_NAME, Collections.emptyList(), DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
            return new DataDrivenFsm(getName(), state,
                    ImmutableMap.of(STATE1_NAME, state), DataDrivenCondition.TRUE_CONDITION, true, true);
        }

    };

    private static final @NonNull CompilationResult INVALID_UNDEFINED_ACTION = new CompilationResult() {

        @Override
        public String getName() {
            return "undefined_action";
        }

        @Override
        public String getXmlString() {
            return String.format(FSM_START, getName(), "initial=\"" + STATE1_NAME + "\"") +
                    String.format(STATE_START, STATE1_NAME) +
                    String.format(TRANSITION_STRING, EVENTS_STRING, String.format(ACTION_STRING, "action1"), StringUtils.EMPTY, String.format(TARGET_STRING, "state1"), StringUtils.EMPTY, StringUtils.EMPTY) +
                    STATE_END +
                    FSM_END;
        }

        @Override
        public DataDrivenFsm getResult() {
            return null;
        }

    };

    private static final @NonNull CompilationResult INVALID_UNDEFINED_CONDITION = new CompilationResult() {

        @Override
        public String getName() {
            return "undefined_condition";
        }

        @Override
        public String getXmlString() {
            return String.format(FSM_START, getName(), "initial=\"" + STATE1_NAME + "\"") +
                    String.format(STATE_START, STATE1_NAME) +
                    String.format(TRANSITION_STRING, EVENTS_STRING, String.format(TEST_STRING, TEST1_NAME), StringUtils.EMPTY, String.format(TARGET_STRING, STATE1_NAME), StringUtils.EMPTY, StringUtils.EMPTY) +
                    STATE_END +
                    FSM_END;
        }

        @Override
        public DataDrivenFsm getResult() {
            return null;
        }

    };

    private static final @NonNull CompilationResult INVALID_UNDEFINED_TARGET = new CompilationResult() {

        @Override
        public String getName() {
            return "undefined_target";
        }

        @Override
        public String getXmlString() {
            return String.format(FSM_START, getName(), "initial=\"" + STATE1_NAME + "\"") +
                    String.format(STATE_START, STATE1_NAME) +
                    String.format(TRANSITION_STRING, EVENTS_STRING, String.format(TEST_STRING, TEST1_NAME), StringUtils.EMPTY, String.format(TARGET_STRING, "state2"), StringUtils.EMPTY, StringUtils.EMPTY) +
                    STATE_END +
                    FSM_END;
        }

        @Override
        public DataDrivenFsm getResult() {
            return null;
        }

    };

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { VALID_ONE_STATE_FINAL },
                { VALID_ONE_STATE_LOOP },
                { VALID_SAVE_CLEAR_FIELDS },
                { VALID_MULTIPLE_ACTIONS_TESTS },
                { VALID_MULTIPLE_STATES_TRANSITIONS },
                { VALID_ON_ENTRY_EXIT },
                { VALID_PRECONDITION },
                { VALID_INITIAL_STATE },
                { VALID_INITIAL },
                { INVALID_UNDEFINED_ACTION },
                { INVALID_UNDEFINED_CONDITION },
                { INVALID_UNDEFINED_TARGET },
                { VALID_NO_INITIAL },
        });
    }

    private final CompilationResult fExpected;

    /**
     * Constructor
     *
     * @param expected
     *            The expected result
     */
    public TmfXmlFsmCuTest(CompilationResult expected) {
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
    public void testFsm() throws SAXException, IOException, ParserConfigurationException {
        AnalysisCompilationData data = new AnalysisCompilationData();

        compileActionsAndTests(fExpected.getXmlString(), data);
        Element xmlElement = TmfXmlTestUtils.getXmlElement(TmfXmlStrings.FSM, fExpected.getXmlString());
        assertNotNull(xmlElement);
        TmfXmlFsmCu compiledFsm = TmfXmlFsmStateCu.compileFsm(data, xmlElement);
        if (fExpected.getResult() == null) {
            assertNull("Expected null action" + fExpected.getName(), compiledFsm);
        } else {
            assertNotNull("Expected non null " + fExpected.getName(), compiledFsm);
            assertEquals(fExpected.getName() + " generated", fExpected.getResult(), compiledFsm.generate());
        }
    }

    private static void compileActionsAndTests(@NonNull String xmlString, @NonNull AnalysisCompilationData data) throws SAXException, IOException, ParserConfigurationException {
        List<@NonNull Element> xmlElements = TmfXmlTestUtils.getXmlElements(TmfXmlStrings.ACTION, xmlString);
        for (Element xmlElement : xmlElements) {
            assertNotNull(TmfXmlActionCu.compileNamedAction(data, xmlElement));
        }
        xmlElements = TmfXmlTestUtils.getXmlElements(TmfXmlStrings.TEST, xmlString);
        for (Element xmlElement : xmlElements) {
            assertNotNull(TmfXmlConditionCu.compileNamedCondition(data, xmlElement));
        }
    }

}
