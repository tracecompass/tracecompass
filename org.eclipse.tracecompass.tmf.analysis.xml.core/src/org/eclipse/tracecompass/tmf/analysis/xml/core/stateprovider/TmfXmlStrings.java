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

    /**
     * @since 1.2
     */
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

}