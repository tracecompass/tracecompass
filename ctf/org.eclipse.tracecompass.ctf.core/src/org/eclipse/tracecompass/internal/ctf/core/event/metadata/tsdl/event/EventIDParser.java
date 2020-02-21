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

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isUnaryInteger;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryIntegerParser;

/**
 * An event identifier (ID) relates to the class (a type) of event within an
 * event stream, e.g. event irq_entry.
 *
 * @author Matthew Khouzam - Initial API and implementation
 * @author Efficios - Description
 *
 */
public final class EventIDParser implements ICommonTreeParser {

    private static final String INVALID_VALUE_ERROR = "Invalid value for event id"; //$NON-NLS-1$

    /**
     * The instance
     */
    public static final EventIDParser INSTANCE = new EventIDParser();

    private EventIDParser() {
    }

    @Override
    public Long parse(CommonTree tree, ICommonTreeParserParameter param) throws ParseException {

        CommonTree firstChild = (CommonTree) tree.getChild(0);

        if (isUnaryInteger(firstChild)) {
            if (tree.getChildCount() > 1) {
                throw new ParseException(INVALID_VALUE_ERROR);
            }
            long intval = UnaryIntegerParser.INSTANCE.parse(firstChild, null);
            if (intval > Integer.MAX_VALUE) {
                throw new ParseException("Event id larger than int.maxvalue, something is amiss"); //$NON-NLS-1$
            }
            return intval;
        }
        throw new ParseException(INVALID_VALUE_ERROR);
    }

}
