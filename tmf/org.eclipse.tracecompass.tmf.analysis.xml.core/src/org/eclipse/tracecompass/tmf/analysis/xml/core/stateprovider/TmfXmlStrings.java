/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This file defines all name in the XML Structure for the State Provider
 *
 * @author Florian Wininger
 * @noimplement This interface only contains static defines
 */
@SuppressWarnings({ "javadoc", "nls" })
@NonNullByDefault
public interface TmfXmlStrings {

    /* XML generic Element attribute names */
    String VALUE = "value";
    String NAME = "name";
    String VERSION = "version";
    String TYPE = "type";

    /* XML header element */
    String HEAD = "head";
    String TRACETYPE = "traceType";
    String ID = "id";
    String LABEL = "label";
    String ANALYSIS = "analysis";

    /* XML String */
    String NULL = "";
    String WILDCARD = "*";
    String VARIABLE_PREFIX = "$";
    String COLOR = "color";
    String COLOR_PREFIX = "#";

    /* XML Element Name */
    String STATE_PROVIDER = "stateProvider";
    String DEFINED_VALUE = "definedValue";
    String LOCATION = "location";
    String EVENT_HANDLER = "eventHandler";
    String STATE_ATTRIBUTE = "stateAttribute";
    String STATE_VALUE = "stateValue";
    String STATE_CHANGE = "stateChange";
    String ELEMENT_FIELD = "field";

    /* XML Condition strings */
    String IF = "if";
    String CONDITION = "condition";
    String THEN = "then";
    String ELSE = "else";

    /* XML event handler strings */
    String HANDLER_EVENT_NAME = "eventName";

    /* XML constant for Type of Attribute and Value */
    String TYPE_NULL = "null";
    String TYPE_CONSTANT = "constant";
    String EVENT_FIELD = "eventField";
    String TYPE_LOCATION = "location";
    String TYPE_QUERY = "query";
    String TYPE_SELF = "self";
    String TYPE_INT = "int";
    String TYPE_LONG = "long";
    /** @since 2.0 */
    String TYPE_DOUBLE = "double";
    String TYPE_STRING = "string";
    String TYPE_EVENT_NAME = "eventName";
    String TYPE_DELETE = "delete";
    String INCREMENT = "increment";
    String FORCED_TYPE = "forcedType";
    String ATTRIBUTE_STACK = "stack";
    String STACK_POP = "pop";
    String STACK_PUSH = "push";
    String STACK_PEEK = "peek";
    String CPU = "cpu";

    String TIMESTAMP = "timestamp";

    /* Operator type */
    String NOT = "not";
    String AND = "and";
    String OR = "or";

    String OPERATOR = "operator";

    /* Comparison/Condition operator types */
    String EQ = "eq";
    String NE = "ne";
    String GE = "ge";
    String GT = "gt";
    String LE = "le";
    String LT = "lt";

    /* XML pattern elements */
    /**
     * @since 2.0
     */
    String PATTERN = "pattern";

    /**
     * @since 2.0
     */
    String STORED_FIELD = "storedField";

    /**
     * @since 2.0
     */
    String STORED_FIELDS = "storedFields";

    /**
     * @since 2.0
     */
    String PATTERN_HANDLER = "patternHandler";

    /* XML synthetic event elements */
    /**
     * @since 2.0
     */
    String SEGMENT_NAME = "segName";
    /**
     * @since 2.0
     */
    String SEGMENT = "segment";
    /**
     * @since 2.0
     */
    String SEGMENT_TYPE = "segType";
    /**
     * @since 2.0
     */
    String SEGMENT_CONTENT = "segContent";
    /**
     * @since 2.0
     */
    String SEGMENT_FIELD = "segField";

    /**
     * @since 2.0
     */
    String INITIAL = "initial";
    /**
     * @since 2.0
     */
    String TEST = "test";
    /**
     * @since 2.0
     */
    String ACTION = "action";
    /**
     * @since 2.0
     */
    String FSM = "fsm";
    /**
     * @since 2.0
     */
    String STATE = "state";
    /**
     * @since 2.0
     */
    String EVENT_INFO = "eventInfo";
    /**
     * @since 2.0
     */
    String TIME_INFO = "timeInfo";
    /**
     * @since 2.0
     */
    String EVENT = "event";
    /**
     * @since 2.0
     */
    String CONSTANT_PREFIX = "#";
    /**
     * @since 2.0
     */
    String FSM_SCHEDULE_ACTION = "fsmScheduleAction";
    /**
     * @since 2.0
     */
    String MULTIPLE = "multiple";
    /**
     * @since 2.0
     */
    String PRECONDITION = "precondition";
    /**
     * @since 2.0
     */
    String COND = "cond";
    /**
     * @since 2.0
     */
    String FINAL = "final";
    /**
     * @since 2.0
     */
    String ABANDON_STATE = "abandonState";
    /**
     * @since 2.0
     */
    String STATE_TABLE = "stateTable";
    /**
     * @since 2.0
     */
    String STATE_DEFINITION = "stateDefinition";
    /**
     * @since 2.0
     */
    String EMPTY_STRING = "";
    /**
     * @since 2.0
     */
    String TRANSITION = "transition";
    /**
     * @since 2.0
     */
    String TARGET = "target";
    /**
     * @since 2.0
     */
    String SAVE_STORED_FIELDS = "saveStoredFields";
    /**
     * @since 2.0
     */
    String CLEAR_STORED_FIELDS = "clearStoredFields";
    /**
     * @since 2.0
     */
    String TIME_RANGE = "timerange";
    /**
     * @since 2.0
     */
    String ELAPSED_TIME = "elapsedTime";
    /**
     * @since 2.0
     */
    String NS = "ns";
    /**
     * @since 2.0
     */
    String US = "us";
    /**
     * @since 2.0
     */
    String MS = "ms";
    /**
     * @since 2.0
     */
    String S = "s";
    /**
     * @since 2.0
     */
    String UNIT = "unit";
    /**
     * @since 2.0
     */
    String IN = "in";
    /**
     * @since 2.0
     */
    String OUT = "out";
    /**
     * @since 2.0
     */
    String BEGIN = "begin";
    /**
     * @since 2.0
     */
    String END = "end";
    /**
     * @since 2.0
     */
    String LESS = "less";
    /**
     * @since 2.0
     */
    String EQUAL = "equal";
    /**
     * @since 2.0
     */
    String MORE = "more";
    /**
     * @since 2.0
     */
    String SINCE = "since";
    /**
     * @since 2.0
     */
    String ARG = "arg";
    /**
     * @since 2.0
     */
    String SCENARIOS = "scenarios";
    /**
     * @since 2.0
     */
    String ONENTRY = "onentry";
    /**
     * @since 2.0
     */
    String ONEXIT = "onexit";
    /**
     * @since 2.0
     */
    String OR_SEPARATOR = "\\|";
    /**
     * @since 2.0
     */
    String AND_SEPARATOR = ":";
    /**
     * @since 2.0
     */
    String ALIAS = "alias";
    /**
     * @since 2.0
     */
    String ABANDON = "abandon";
}