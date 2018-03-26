/*******************************************************************************
* Copyright (c) 2018 Ericsson
*
* All rights reserved. This program and the accompanying materials are
* made available under the terms of the Eclipse Public License v1.0 which
* accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.util.List;

import com.google.common.collect.Iterables;

/**
 * Compilation unit for a filter negation
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterNotCu extends FilterCu {

    /**
     * Constructor
     *
     * @param expressions
     *            The list of time event filter expression
     */
    public FilterNotCu(List<FilterExpressionCu> expressions) {
        super(expressions);
    }

    /**
     * Compile an item filter compilation unit
     *
     * @param regex
     *            The filter regex
     * @return The filter compilation unit
     */
    public static FilterCu compile(String regex) {
        FilterCu cu = FilterCu.compile(regex);
        return new FilterNotCu(cu.getExpressions());
    }

    @Override
    public Filter generate() {
        Iterable<FilterExpression> expressions = Iterables.transform(fExpressions, exp -> exp.generate());
        return new FilterNot(expressions);
    }
}
