/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.environment;

import java.util.List;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;

import com.google.common.collect.ImmutableMap;

/**
 * A scope containing pairs of values that are immutable to the trace.
 *
 * @author Matthew Khouzam - Initial API and implementation
 *
 */
public final class EnvironmentParser implements ICommonTreeParser {

    /**
     * The instance
     */
    public static final EnvironmentParser INSTANCE = new EnvironmentParser();

    private EnvironmentParser() {
    }

    @Override
    public Map<String, String> parse(CommonTree environment, ICommonTreeParserParameter param) {

        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
        List<CommonTree> children = environment.getChildren();
        for (CommonTree child : children) {
            String left;
            String right;
            left = child.getChild(0).getChild(0).getChild(0).getText();
            right = child.getChild(1).getChild(0).getChild(0).getText();
            builder.put(left, right);
        }
        return builder.build();
    }

}
