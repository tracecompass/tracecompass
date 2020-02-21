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

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.integer;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isUnaryInteger;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isUnaryString;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryIntegerParser;

/**
 * Singed status, whether an integer is capable of accepting negative values or
 * not.
 *
 * @author Matthew Khouzam
 */
public final class SignedParser implements ICommonTreeParser {
    /**
     * Instance
     */
    public static final SignedParser INSTANCE = new SignedParser();

    private static final String INVALID_BOOLEAN_VALUE = "Invalid boolean value "; //$NON-NLS-1$

    private SignedParser() {
    }

    /**
     * Parses whether the parent is signed or not. Typical syntax would be
     * "signed = true;" or "signed = false;"
     *
     * @param tree
     *            the AST node containing "signed = boolean;"
     * @param unused
     *            unused
     * @return @link {@link Boolean#TRUE} if signed, {@link Boolean#FALSE} if
     *         unsigned
     * @throws ParseException
     *             on a malformed tree
     */
    @Override
    public Boolean parse(CommonTree tree, ICommonTreeParserParameter unused) throws ParseException {
        boolean ret = false;
        CommonTree firstChild = (CommonTree) tree.getChild(0);

        if (isUnaryString(firstChild)) {
            String strval = concatenateUnaryStrings(tree.getChildren());

            if (strval.equals(MetadataStrings.TRUE)
                    || strval.equals(MetadataStrings.TRUE2)) {
                ret = true;
            } else if (strval.equals(MetadataStrings.FALSE)
                    || strval.equals(MetadataStrings.FALSE2)) {
                ret = false;
            } else {
                throw new ParseException(INVALID_BOOLEAN_VALUE
                        + firstChild.getChild(0).getText());
            }
        } else if (isUnaryInteger(firstChild)) {
            /* Happens if the value is something like "1234.hello" */
            if (tree.getChildCount() > 1) {
                throw new ParseException(INVALID_BOOLEAN_VALUE);
            }

            long intval = UnaryIntegerParser.INSTANCE.parse(firstChild, null);

            if (intval == 1) {
                ret = true;
            } else if (intval == 0) {
                ret = false;
            } else {
                throw new ParseException(INVALID_BOOLEAN_VALUE
                        + firstChild.getChild(0).getText());
            }
        } else {
            throw new ParseException(INVALID_BOOLEAN_VALUE);
        }
        return ret;
    }

}
