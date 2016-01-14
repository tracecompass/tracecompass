/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
