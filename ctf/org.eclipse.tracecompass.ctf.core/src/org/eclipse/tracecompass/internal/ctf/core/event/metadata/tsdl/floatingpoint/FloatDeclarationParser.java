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

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.floatingpoint;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.childTypeError;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isAnyUnaryString;

import java.nio.ByteOrder;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.AlignmentParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.ByteOrderParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryIntegerParser;

/**
 *
 * The floating point values byte ordering is defined in the TSDL metadata.
 * <p>
 * Floating point values follow the IEEE 754-2008 standard interchange formats.
 * Description of the floating point values include the exponent and mantissa
 * size in bits. Some requirements are imposed on the floating point values:
 * <ul>
 * <li>FLT_RADIX must be 2.</li>
 * <li>mant_dig is the number of digits represented in the mantissa. It is
 * specified by the ISO C99 standard, section 5.2.4, as FLT_MANT_DIG,
 * DBL_MANT_DIG and LDBL_MANT_DIG as defined by <float.h>.</li>
 * <li>exp_dig is the number of digits represented in the exponent. Given that
 * mant_dig is one bit more than its actual size in bits (leading 1 is not
 * needed) and also given that the sign bit always takes one bit, exp_dig can be
 * specified as:</li>
 * <ul>
 * <li>sizeof(float) * CHAR_BIT - FLT_MANT_DIG</li>
 * <li>sizeof(double) * CHAR_BIT - DBL_MANT_DIG</li>
 * <li>sizeof(long double) * CHAR_BIT - LDBL_MANT_DIG</li>
 * <li>
 * </ul>
 * </ul>
 *
 * @author Matthew Khouzam - initial API and implementation
 * @auttor Efficios - Description
 *
 */
public final class FloatDeclarationParser implements ICommonTreeParser {

    private static final String FLOAT_UNKNOWN_ATTRIBUTE = "Float: unknown attribute "; //$NON-NLS-1$
    private static final String FLOAT_MISSING_SIZE_ATTRIBUTE = "Float: missing size attribute"; //$NON-NLS-1$
    private static final String IDENTIFIER_MUST_BE_A_STRING = "Left side of ctf expression must be a string"; //$NON-NLS-1$

    /**
     * Parameter object with only a trace in it
     *
     * @author Matthew Khouzam
     *
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
    public static final FloatDeclarationParser INSTANCE = new FloatDeclarationParser();

    private static final int DEFAULT_FLOAT_EXPONENT = 8;
    private static final int DEFAULT_FLOAT_MANTISSA = 24;

    private FloatDeclarationParser() {
    }

    /**
     * Parses a float node and returns a floating point declaration of the
     * type @link {@link FloatDeclaration}.
     *
     * @param floatingPoint
     *            AST node of type FLOAT
     * @param param
     *            parameter containing the trace
     * @return the float declaration
     * @throws ParseException
     *             if a float AST is malformed
     */
    @Override
    public FloatDeclaration parse(CommonTree floatingPoint, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        CTFTrace trace = ((Param) param).fTrace;
        List<CommonTree> children = floatingPoint.getChildren();

        /*
         * If the integer has no attributes, then it is missing the size
         * attribute which is required
         */
        if (children == null) {
            throw new ParseException(FLOAT_MISSING_SIZE_ATTRIBUTE);
        }

        /* The return value */
        ByteOrder byteOrder = trace.getByteOrder();
        long alignment = 0;

        int exponent = DEFAULT_FLOAT_EXPONENT;
        int mantissa = DEFAULT_FLOAT_MANTISSA;

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
                    throw new ParseException(IDENTIFIER_MUST_BE_A_STRING);
                }
                String left = concatenateUnaryStrings(leftStrings);

                if (left.equals(MetadataStrings.EXP_DIG)) {
                    exponent = UnaryIntegerParser.INSTANCE.parse((CommonTree) rightNode.getChild(0), null).intValue();
                } else if (left.equals(MetadataStrings.BYTE_ORDER)) {
                    byteOrder = ByteOrderParser.INSTANCE.parse(rightNode, new ByteOrderParser.Param(trace));
                } else if (left.equals(MetadataStrings.MANT_DIG)) {
                    mantissa = UnaryIntegerParser.INSTANCE.parse((CommonTree) rightNode.getChild(0), null).intValue();
                } else if (left.equals(MetadataStrings.ALIGN)) {
                    alignment = AlignmentParser.INSTANCE.parse(rightNode, null);
                } else {
                    throw new ParseException(FLOAT_UNKNOWN_ATTRIBUTE + left);
                }

                break;
            default:
                throw childTypeError(child);
            }
        }
        int size = mantissa + exponent;
        if (size == 0) {
            throw new ParseException(FLOAT_MISSING_SIZE_ATTRIBUTE);
        }

        if (alignment == 0) {
            alignment = 1;
        }

        return new FloatDeclaration(exponent, mantissa, byteOrder, alignment);

    }

}
