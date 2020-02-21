/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * Unary Integer Parser, along with Unary string parser, one of the two most
 * used parsers in TSDL. Converts a string representation of an integer into a
 * java {@link Long}
 *
 * @author Matthew Khouzam
 *
 */
public final class UnaryIntegerParser implements ICommonTreeParser {

    /**
     * Instance
     */
    public static final UnaryIntegerParser INSTANCE = new UnaryIntegerParser();

    private UnaryIntegerParser() {
    }

    /**
     * Parses an unary integer (dec, hex or oct).
     *
     * @param unaryInteger
     *            An unary integer node.
     *
     * @return The integer value.
     * @throws ParseException
     *             on an invalid integer format ("bob" for example)
     */
    @Override
    public Long parse(CommonTree unaryInteger, ICommonTreeParserParameter notUsed) throws ParseException {
        List<CommonTree> children = unaryInteger.getChildren();
        CommonTree value = children.get(0);
        String strval = value.getText();

        long intval;
        try {
            intval = Long.decode(strval);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid integer format: " + strval, e); //$NON-NLS-1$
        }

        /* The rest of children are sign */
        if ((children.size() % 2) == 0) {
            return -intval;
        }
        return intval;
    }

}
