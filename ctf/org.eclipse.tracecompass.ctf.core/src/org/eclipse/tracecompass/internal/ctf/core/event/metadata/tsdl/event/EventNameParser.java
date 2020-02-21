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

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.event;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isAnyUnaryString;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * Parser for event names
 *
 * @author Matthew Khouzam
 *
 */
public final class EventNameParser implements ICommonTreeParser {
    /**
     * Instance
     */
    public static final EventNameParser INSTANCE = new EventNameParser();

    private EventNameParser() {
    }

    @Override
    public String parse(CommonTree tree, ICommonTreeParserParameter param) throws ParseException {
        CommonTree firstChild = (CommonTree) tree.getChild(0);

        if (isAnyUnaryString(firstChild)) {
            return concatenateUnaryStrings(tree.getChildren());
        }
        throw new ParseException("invalid value for event name"); //$NON-NLS-1$
    }

}
