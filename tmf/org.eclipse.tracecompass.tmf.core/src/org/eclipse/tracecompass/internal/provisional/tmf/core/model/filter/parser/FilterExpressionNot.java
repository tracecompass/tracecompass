/*******************************************************************************
* Copyright (c) 2018 Ericsson
*
* All rights reserved. This program and the accompanying materials are
* made available under the terms of the Eclipse Public License 2.0 which
* accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.util.List;

import com.google.common.collect.Multimap;

/**
 * This class implement a filter expression negation that could be tested against an
 * input
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterExpressionNot extends FilterExpression {

    /**
     * Constructor
     *
     * @param elements
     *            The list of element representing this experession
     *
     */
    public FilterExpressionNot(List<Object> elements) {
        super(elements);
    }

    @Override
    public boolean test(Multimap<String, Object> data) {
        return !super.test(data);
    }
}
