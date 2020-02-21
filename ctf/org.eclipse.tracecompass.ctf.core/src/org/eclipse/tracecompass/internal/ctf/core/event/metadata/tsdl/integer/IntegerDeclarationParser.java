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

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.childTypeError;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isAnyUnaryString;

import java.nio.ByteOrder;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.Activator;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.Messages;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.AlignmentParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.ByteOrderParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.SizeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.string.EncodingParser;

/**
 * Signed integers are represented in two-complement. Integer alignment, size,
 * signedness and byte ordering are defined in the TSDL metadata. Integers
 * aligned on byte size (8-bit) and with length multiple of byte size (8-bit)
 * correspond to the C99 standard integers. In addition, integers with alignment
 * and/or size that are not a multiple of the byte size are permitted; these
 * correspond to the C99 standard bitfields, with the added specification that
 * the CTF integer bitfields have a fixed binary representation. Integer size
 * needs to be a positive integer. Integers of size 0 are forbidden. An
 * MIT-licensed reference implementation of the CTF portable bitfields is
 * available here.
 *
 * Binary representation of integers:
 * <ul>
 * <li>On little and big endian: Within a byte, high bits correspond to an
 * integer high bits, and low bits correspond to low bits</li>
 * <li>On little endian: Integer across multiple bytes are placed from the less
 * significant to the most significant Consecutive integers are placed from
 * lower bits to higher bits (even within a byte)</li>
 * <li>On big endian: Integer across multiple bytes are placed from the most
 * significant to the less significant Consecutive integers are placed from
 * higher bits to lower bits (even within a byte)</li>
 * </ul>
 *
 * This binary representation is derived from the bitfield implementation in GCC
 * for little and big endian. However, contrary to what GCC does, integers can
 * cross units boundaries (no padding is required). Padding can be explicitly
 * added to follow the GCC layout if needed.
 *
 * @author Matthew Khouzam
 * @author Efficios - javadoc preamble
 *
 */
public final class IntegerDeclarationParser implements ICommonTreeParser {

    /**
     * Parameter Object with a trace
     *
     * @author Matthew Khouzam
     */
    @NonNullByDefault
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
    public static final IntegerDeclarationParser INSTANCE = new IntegerDeclarationParser();

    private static final @NonNull String ENCODING = "encoding"; //$NON-NLS-1$
    private static final @NonNull String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final int DEFAULT_INT_BASE = 10;
    private static final @NonNull String MAP = "map"; //$NON-NLS-1$
    private static final @NonNull String BASE = "base"; //$NON-NLS-1$
    private static final @NonNull String SIZE = "size"; //$NON-NLS-1$
    private static final @NonNull String SIGNED = "signed"; //$NON-NLS-1$

    private IntegerDeclarationParser() {
    }

    /**
     * Parses an integer declaration node.
     *
     * @param parameter
     *            parent trace, for byte orders
     *
     * @return The corresponding integer declaration.
     */
    @Override
    public IntegerDeclaration parse(CommonTree integer, ICommonTreeParserParameter parameter) throws ParseException {
        if (!(parameter instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        CTFTrace trace = ((Param) parameter).fTrace;
        List<CommonTree> children = integer.getChildren();

        /*
         * If the integer has no attributes, then it is missing the size
         * attribute which is required
         */
        if (children == null) {
            throw new ParseException("integer: missing size attribute"); //$NON-NLS-1$
        }

        /* The return value */
        IntegerDeclaration integerDeclaration = null;
        boolean signed = false;
        ByteOrder byteOrder = trace.getByteOrder();
        long size = 0;
        long alignment = 0;
        int base = DEFAULT_INT_BASE;
        @NonNull
        String clock = EMPTY_STRING;

        Encoding encoding = Encoding.NONE;

        /* Iterate on all integer children */
        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.CTF_EXPRESSION_VAL:
                /*
                 * An assignment expression must have 2 children, left and right
                 */

                CommonTree leftNode = (CommonTree) child.getChild(0);
                CommonTree rightNode = (CommonTree) child.getChild(1);

                List<CommonTree> leftStrings = leftNode.getChildren();

                if (!isAnyUnaryString(leftStrings.get(0))) {
                    throw new ParseException("Left side of ctf expression must be a string"); //$NON-NLS-1$
                }
                String left = concatenateUnaryStrings(leftStrings);

                switch (left) {
                case SIGNED:
                    signed = SignedParser.INSTANCE.parse(rightNode, null);
                    break;
                case MetadataStrings.BYTE_ORDER:
                    byteOrder = ByteOrderParser.INSTANCE.parse(rightNode, new ByteOrderParser.Param(trace));
                    break;
                case SIZE:
                    size = SizeParser.INSTANCE.parse(rightNode, null);
                    break;
                case MetadataStrings.ALIGN:
                    alignment = AlignmentParser.INSTANCE.parse(rightNode, null);
                    break;
                case BASE:
                    base = BaseParser.INSTANCE.parse(rightNode, null);
                    break;
                case ENCODING:
                    encoding = EncodingParser.INSTANCE.parse(rightNode, null);
                    break;
                case MAP:
                    clock = ClockMapParser.INSTANCE.parse(rightNode, null);
                    break;
                default:
                    Activator.log(IStatus.WARNING, Messages.IOStructGen_UnknownIntegerAttributeWarning + " " + left); //$NON-NLS-1$
                    break;
                }

                break;
            default:
                throw childTypeError(child);
            }
        }

        if (size <= 0) {
            throw new ParseException("Invalid size attribute in Integer: " + size); //$NON-NLS-1$
        }

        if (alignment == 0) {
            alignment = 1;
        }

        integerDeclaration = IntegerDeclaration.createDeclaration((int) size, signed, base,
                byteOrder, encoding, clock, alignment);

        return integerDeclaration;
    }
}
