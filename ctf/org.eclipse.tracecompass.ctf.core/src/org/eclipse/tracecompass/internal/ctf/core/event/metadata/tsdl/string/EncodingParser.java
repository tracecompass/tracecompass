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

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.string;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isUnaryString;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * Parse the encoding field. This can be "ascii", "utf8" or "none"
 *
 * @author Matthew Khouzam
 */
public final class EncodingParser implements ICommonTreeParser {

    /**
     * Instance
     */
    public static final EncodingParser INSTANCE = new EncodingParser();

    private EncodingParser() { }

    private static final String INVALID_VALUE_FOR_ENCODING = "Invalid value for encoding"; //$NON-NLS-1$

    /**
     * Gets the value of an "encoding" integer attribute.
     *
     * @return The "encoding" value.
     * @throws ParseException
     *             for unknown or malformed encoding
     */
    @Override
    public Encoding parse(CommonTree tree, ICommonTreeParserParameter param) throws ParseException {
        CommonTree firstChild = (CommonTree) tree.getChild(0);

        if (isUnaryString(firstChild)) {
            String strval = concatenateUnaryStrings(tree.getChildren());

            if (strval.equals(MetadataStrings.UTF8)) {
                return Encoding.UTF8;
            } else if (strval.equals(MetadataStrings.ASCII)) {
                return Encoding.ASCII;
            } else if (strval.equals(MetadataStrings.NONE)) {
                return Encoding.NONE;
            } else {
                throw new ParseException(INVALID_VALUE_FOR_ENCODING);
            }
        }
        throw new ParseException(INVALID_VALUE_FOR_ENCODING);

    }

}
