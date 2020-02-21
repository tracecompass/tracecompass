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

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isUnaryInteger;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * Type size, in bits, for integers and floats is that returned by sizeof() in C
 * multiplied by CHAR_BIT. We require the size of char and unsigned char types
 * (CHAR_BIT) to be fixed to 8 bits for cross-endianness compatibility.
 *
 * TSDL metadata representation:
 *
 * <pre>
 * size = /* value is in bits * /
 * </pre>
 *
 * @author Matthew Khouzam
 * @author Efficios - javadoc preamble.
 */
public final class SizeParser implements ICommonTreeParser {
    private static final String INVALID_VALUE_FOR_SIZE = "Invalid value for size"; //$NON-NLS-1$

    /**
     * Instance
     */
    public static final SizeParser INSTANCE = new SizeParser();

    private SizeParser() {
    }

    /**
     * Gets the value of a "size" integer attribute.
     *
     * @param rightNode
     *            A CTF_RIGHT node.
     * @param param
     *            unused
     * @return The "size" value. Can be 4 bytes.
     * @throws ParseException
     *             if the size is not an int or a negative
     */
    @Override
    public Long parse(CommonTree rightNode, ICommonTreeParserParameter param) throws ParseException {
        CommonTree firstChild = (CommonTree) rightNode.getChild(0);
        if (isUnaryInteger(firstChild)) {
            if (rightNode.getChildCount() > 1) {
                throw new ParseException(INVALID_VALUE_FOR_SIZE);
            }
            long size = UnaryIntegerParser.INSTANCE.parse(firstChild, null);
            if (size < 1) {
                throw new ParseException(INVALID_VALUE_FOR_SIZE);
            }
            return size;
        }
        throw new ParseException(INVALID_VALUE_FOR_SIZE);
    }

}
