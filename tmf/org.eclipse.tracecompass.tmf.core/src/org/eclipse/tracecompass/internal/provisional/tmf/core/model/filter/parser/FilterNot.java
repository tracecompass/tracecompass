/*******************************************************************************
* Copyright (c) 2018 Ericsson
*
* All rights reserved. This program and the accompanying materials are
* made available under the terms of the Eclipse Public License v1.0 which
* accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.util.Map;

/**
 * This class represents a filter expression negation
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterNot extends Filter {

    /**
     * Constructor
     *
     * @param expressions
     *            The list of filter expression to test
     */
    public FilterNot(Iterable<FilterExpression> expressions) {
        super(expressions);
    }

    @Override
    public boolean test(Map<String, String> data) {
        return !super.test(data);
    }
}
