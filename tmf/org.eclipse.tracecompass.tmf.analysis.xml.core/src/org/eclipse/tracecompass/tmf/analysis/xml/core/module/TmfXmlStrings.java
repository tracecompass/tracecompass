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

package org.eclipse.tracecompass.tmf.analysis.xml.core.module;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This file defines all name in the XML Structure for the State Provider and UI Elements
 *
 * @author Florian Wininger
 * @noimplement This interface only contains static defines
 * @since 2.2
 */
@SuppressWarnings({ "javadoc", "nls" })
@NonNullByDefault
public interface TmfXmlStrings {

    // ------------------------------------------------------------------------
    // XML Structure for UI elements
    // ------------------------------------------------------------------------

    /* XML generic Element attribute names */
    /**
     * @since 2.3
     */
    static final String TIME_GRAPH_VIEW = "timeGraphView";
    /**
     * @since 2.3
     */
    static final String XY_VIEW = "xyView";

    /* View elements and attributes */
    /**
     * @since 2.3
     */
    static final String ENTRY_ELEMENT = "entry";

    /* Elements and attributes of view entries */
    /**
     * @since 2.3
     */
    static final String PATH = "path";
    /**
     * @since 2.3
     */
    static final String ANALYSIS_ID = "analysisId";
    /**
     * @since 2.4
     */
    static final String DISPLAY_TEXT = "displayText";
    /**
     * @since 2.3
     */
    static final String DISPLAY_ELEMENT = "display";
    /**
     * @since 2.3
     */
    static final String PARENT_ELEMENT = "parent";
    /**
     * @since 2.3
     */
    static final String NAME_ELEMENT = "name";
    /**
     * @since 2.3
     */
    static final String ID_ELEMENT = "id";
    /**
     * @since 2.3
     */
    static final String DISPLAY_TYPE = "displayType";
    /**
     * @since 2.3
     */
    static final String DISPLAY_TYPE_ABSOLUTE = "absolute";
    /**
     * @since 2.3
     */
    static final String DISPLAY_TYPE_DELTA = "delta";

    /* Generic strings for the XML module */
    /**
     * @since 2.3
     */
    static final String XML_OUTPUT_DATA = "xmlOutputData";

    /**
     * @since 2.3
     */
    static final String XML_LATENCY_OUTPUT_DATA = "xmlLatencyOutputData";


    // ------------------------------------------------------------------------
    // XML Structure for State Provider
    // ------------------------------------------------------------------------

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
    String TYPE_DOUBLE = "double";
    String TYPE_STRING = "string";
    String TYPE_EVENT_NAME = "eventName";
    String TYPE_DELETE = "delete";
    /**
     * @since 2.3
     */
    String TYPE_SCRIPT = "script";
    /**
     * @since 2.3
     */
    String TYPE_CUSTOM = "custom";
    /**
     * @since 2.3
     */
    String SCRIPT_ENGINE = "scriptEngine";
    /**
     * @since 2.4
     */
    String TYPE_POOL = "pool";
    String INCREMENT = "increment";
    String UPDATE = "update";
    String FORCED_TYPE = "forcedType";
    String ATTRIBUTE_STACK = "stack";
    String STACK_POP = "pop";
    String STACK_POPALL = "popAll";
    String STACK_PUSH = "push";
    String STACK_PEEK = "peek";
    String CPU = "cpu";
    String HOSTID = "hostId";

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
    String PATTERN = "pattern";

    String STORED_FIELD = "storedField";
    String STORED_FIELDS = "storedFields";

    String PATTERN_HANDLER = "patternHandler";

    /* XML synthetic event elements */
    String SEGMENT_NAME = "segName";
    String SEGMENT = "segment";
    String SEGMENT_TYPE = "segType";
    String SEGMENT_CONTENT = "segContent";
    String SEGMENT_FIELD = "segField";

    String INITIAL = "initial";
    String TEST = "test";
    String ACTION = "action";
    String FSM = "fsm";
    String STATE = "state";
    String EVENT_INFO = "eventInfo";
    String TIME_INFO = "timeInfo";
    String EVENT = "event";
    String CONSTANT_PREFIX = "#";
    String FSM_SCHEDULE_ACTION = "fsmScheduleAction";
    String MULTIPLE = "multiple";
    String PRECONDITION = "precondition";
    String COND = "cond";
    String FINAL = "final";
    String ABANDON_STATE = "abandonState";
    String STATE_TABLE = "stateTable";
    String STATE_DEFINITION = "stateDefinition";
    String EMPTY_STRING = "";
    String TRANSITION = "transition";
    String TARGET = "target";
    String SAVE_STORED_FIELDS = "saveStoredFields";
    String CLEAR_STORED_FIELDS = "clearStoredFields";

    /* Time conditions */
    String TIME_RANGE = "timerange";
    String ELAPSED_TIME = "elapsedTime";
    String NS = "ns";
    String US = "us";
    String MS = "ms";
    String S = "s";
    String UNIT = "unit";
    String IN = "in";
    String OUT = "out";
    String BEGIN = "begin";
    String END = "end";
    String LESS = "less";
    String EQUAL = "equal";
    String MORE = "more";
    String SINCE = "since";

    String ARG = "arg";
    String SCENARIOS = "scenarios";
    String ONENTRY = "onentry";
    String ONEXIT = "onexit";
    String OR_SEPARATOR = "\\|";
    String AND_SEPARATOR = ":";
    String ALIAS = "alias";
    String ABANDON = "abandon";
    String CONSUMING = "consuming";
    String MAPPING_GROUP = "mappingGroup";
    String ENTRY = "entry";
    String INITIAL_STATE = "initialState";
    String VIEW_LABEL_PREFIX = "viewLabelPrefix";
}