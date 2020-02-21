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

import com.google.common.collect.Multimap;

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
    public boolean test(Multimap<String, Object> data) {
        return !super.test(data);
    }

}
