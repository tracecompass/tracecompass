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

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.integer;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;

/**
 * A reference to the clock map in a given integer.
 *
 * @author Matthew Khouzam
 *
 */
public final class ClockMapParser implements ICommonTreeParser {

    private static final @NonNull String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * Instance
     */
    public static final ClockMapParser INSTANCE = new ClockMapParser();

    private ClockMapParser() {
    }

    @Override
    public String parse(CommonTree tree, ICommonTreeParserParameter param) {
        String clock = tree.getChild(1).getChild(0).getChild(0).getText();
        return clock == null ? EMPTY_STRING : clock;
    }

}
