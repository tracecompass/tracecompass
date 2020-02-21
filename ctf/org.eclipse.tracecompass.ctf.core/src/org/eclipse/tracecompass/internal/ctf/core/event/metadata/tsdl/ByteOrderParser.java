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

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isUnaryString;

import java.nio.ByteOrder;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * By default, byte order of a basic type is the byte order described in the
 * trace description. It can be overridden by specifying a byte_order attribute
 * for a basic type. Typical use-case is to specify the network byte order (big
 * endian: be) to save data captured from the network into the trace without
 * conversion.
 *
 * TSDL metadata representation:
 *
 * <pre>
 * /* network and be are aliases * /
 * byte_order= /* native OR network OR be OR le * /;
 * </pre>
 *
 * The native keyword selects the byte order described in the trace description.
 * The network byte order is an alias for big endian.
 *
 * Even though the trace description section is not per se a type, for sake of
 * clarity, it should be noted that native and network byte orders are only
 * allowed within type declaration. The byte_order specified in the trace
 * description section only accepts be or le values.
 *
 * @author Matthew Khouzam
 * @author Efficios - Javadoc preamble
 *
 */
public final class ByteOrderParser implements ICommonTreeParser {

    /**
     * Parameter object with a trace
     *
     * @author Matthew Khouzam
     *
     */
    public static final class Param implements ICommonTreeParserParameter {
        private final CTFTrace fTrace;

        /**
         * Constructor
         *
         * @param trace
         *            the trace
         */
        public Param(CTFTrace trace) {
            fTrace = trace;
        }
    }

    /**
     * Instance
     */
    public static final ByteOrderParser INSTANCE = new ByteOrderParser();

    private static final String INVALID_VALUE_FOR_BYTE_ORDER = "Invalid value for byte order"; //$NON-NLS-1$

    private ByteOrderParser() {
    }

    /**
     * Gets the value of a "byte_order" integer attribute.
     *
     * @param byteOrderTree
     *            A CTF_RIGHT node.
     *
     * @return The "byte_order" value.
     * @throws ParseException
     *             if the value is invalid
     */
    @Override
    public final ByteOrder parse(CommonTree byteOrderTree, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        CTFTrace trace = ((Param) param).fTrace;
        CommonTree firstChild = (CommonTree) byteOrderTree.getChild(0);

        if (isUnaryString(firstChild)) {
            String strval = concatenateUnaryStrings(byteOrderTree.getChildren());

            if (strval.equals(MetadataStrings.LE)) {
                return ByteOrder.LITTLE_ENDIAN;
            } else if (strval.equals(MetadataStrings.BE)
                    || strval.equals(MetadataStrings.NETWORK)) {
                return ByteOrder.BIG_ENDIAN;
            } else if (strval.equals(MetadataStrings.NATIVE)) {
                ByteOrder byteOrder = trace.getByteOrder();
                return (byteOrder == null) ? ByteOrder.nativeOrder() : byteOrder;
            } else {
                throw new ParseException(INVALID_VALUE_FOR_BYTE_ORDER);
            }
        }
        throw new ParseException(INVALID_VALUE_FOR_BYTE_ORDER);
    }
}
