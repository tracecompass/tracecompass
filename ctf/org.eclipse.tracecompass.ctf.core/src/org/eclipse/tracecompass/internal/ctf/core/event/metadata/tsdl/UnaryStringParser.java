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

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * Unary String Parser, along with Unary integer parser, one of the two most
 * used parsers in TSDL. Takes a basic node of the AST and converts it to a java
 * {@link String}.]
 *
 * @author Matthew Khouzam
 *
 */
public final class UnaryStringParser implements ICommonTreeParser {

    /** Instance */
    public static final UnaryStringParser INSTANCE = new UnaryStringParser();

    private UnaryStringParser() {
    }

    /**
     * Parses a unary string node and return the string value.
     *
     * @param unaryString
     *            The unary string node to parse (type UNARY_EXPRESSION_STRING
     *            or UNARY_EXPRESSION_STRING_QUOTES).
     *
     * @return The string value.
     * @throws ParseException
     *             The tree node (unaryString) is not a unary string.
     */
    /*
     * It would be really nice to remove the quotes earlier, such as in the
     * parser.
     */
    @Override
    public String parse(CommonTree unaryString, ICommonTreeParserParameter notUsed) throws ParseException {
        CommonTree value = (CommonTree) unaryString.getChild(0);
        if (value.getType() == CTFParser.UNARY_EXPRESSION_STRING) {
            value = (CommonTree) value.getChild(0);
        }
        String strval = value.getText();
        if (strval == null) {
            throw new ParseException("Unary String was null"); //$NON-NLS-1$
        }

        /* Remove quotes */
        if (unaryString.getType() == CTFParser.UNARY_EXPRESSION_STRING_QUOTES) {
            strval = strval.substring(1, strval.length() - 1);
        }

        return strval;
    }

}
