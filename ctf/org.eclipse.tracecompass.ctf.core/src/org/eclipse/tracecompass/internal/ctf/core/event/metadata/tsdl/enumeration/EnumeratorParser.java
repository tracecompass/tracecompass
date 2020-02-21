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

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.enumeration;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.childTypeError;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isAnyUnaryString;

import java.math.BigInteger;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.Activator;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryIntegerParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryStringParser;

/**
 * The parser for individual enumerators within an enum body
 *
 * @author Matthew Khouzam - Initial API and implementation
 *
 */
public final class EnumeratorParser implements ICommonTreeParser {

    /**
     * A parameter containing an enum declaration
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final EnumDeclaration fEnumDeclaration;

        /**
         * Constructor
         *
         * @param enumDeclaration
         *            the enum declaration to populate
         */
        public Param(EnumDeclaration enumDeclaration) {
            fEnumDeclaration = enumDeclaration;
        }
    }

    /**
     * Instance
     */
    public static final EnumeratorParser INSTANCE = new EnumeratorParser();

    private EnumeratorParser() {
    }

    /**
     * Parses an enumerator node and adds an enumerator declaration to an
     * enumeration declaration.
     *
     * The high value of the range of the last enumerator is needed in case the
     * current enumerator does not specify its value.
     *
     * @param enumerator
     *            An ENUM_ENUMERATOR node.
     * @param param
     *            an enumeration declaration to which will be added the
     *            enumerator.
     * @return The high value of the value range of the current enumerator.
     * @throws ParseException
     *             if the element failed to add
     */
    @Override
    public Long parse(CommonTree enumerator, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        EnumDeclaration enumDeclaration = ((Param) param).fEnumDeclaration;

        List<CommonTree> children = enumerator.getChildren();

        long low = 0, high = 0;
        boolean valueSpecified = false;
        String label = null;

        for (CommonTree child : children) {
            if (isAnyUnaryString(child)) {
                label = UnaryStringParser.INSTANCE.parse(child, null);
            } else if (child.getType() == CTFParser.ENUM_VALUE) {

                valueSpecified = true;

                low = UnaryIntegerParser.INSTANCE.parse((CommonTree) child.getChild(0), null);
                high = low;
            } else if (child.getType() == CTFParser.ENUM_VALUE_RANGE) {

                valueSpecified = true;

                low = UnaryIntegerParser.INSTANCE.parse((CommonTree) child.getChild(0), null);
                high = UnaryIntegerParser.INSTANCE.parse((CommonTree) child.getChild(1), null);
            } else {
                throw childTypeError(child);
            }
        }

        if (low > high) {
            throw new ParseException("enum low value greater than high value"); //$NON-NLS-1$
        }
        if (valueSpecified && !enumDeclaration.add(low, high, label)) {
            Activator.log(IStatus.WARNING, "enum declarator values overlap. " + enumDeclaration.getLabels() + " and " + label); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (!valueSpecified && !enumDeclaration.add(label)) {
            throw new ParseException("enum cannot add element " + label); //$NON-NLS-1$
        }

        if (valueSpecified && (BigInteger.valueOf(low).compareTo(enumDeclaration.getContainerType().getMinValue()) < 0 ||
                BigInteger.valueOf(high).compareTo(enumDeclaration.getContainerType().getMaxValue()) > 0)) {
            throw new ParseException("enum value is not in range"); //$NON-NLS-1$
        }

        return high;
    }

}
