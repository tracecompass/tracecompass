/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

/**
 * List of string used in the filter syntax
 *
 * @author Jean-Christian Kouame
 *
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface IFilterStrings {

    // OPERATORS
    static final String EQUAL = "==";
    static final String NOT_EQUAL = "!=";
    static final String MATCHES = "matches";
    static final String CONTAINS = "contains";
    static final String PRESENT = "present";
    static final String NOT = "!";
    static final String GT = ">";
    static final String LT = "<";

    static final String WILDCARD = "*";
    static final String OR = "||";
    static final String AND = "&&";

}
