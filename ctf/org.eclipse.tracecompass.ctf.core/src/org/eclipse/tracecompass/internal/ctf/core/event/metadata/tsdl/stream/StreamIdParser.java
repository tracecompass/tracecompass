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
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.stream;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isUnaryInteger;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryIntegerParser;

/**
 * <strong>Stream ID</strong>, used as reference to stream description in
 * metadata. This field is optional if there is only one stream description in
 * the metadata, but becomes required if there are more than one stream in the
 * TSDL metadata description.
 *
 * @author Matthew Khouzam
 * @author Efficios - Javadoc
 *
 */
public final class StreamIdParser implements ICommonTreeParser {

    /** Instance */
    public static final StreamIdParser INSTANCE = new StreamIdParser();

    private StreamIdParser() {
    }

    /**
     * Parses a stream id
     *
     * @param tree
     *            the AST node with "id = N;"
     * @return the value of the stream as a {@link Long}
     */
    @Override
    public Long parse(CommonTree tree, ICommonTreeParserParameter param) throws ParseException {
        CommonTree firstChild = (CommonTree) tree.getChild(0);
        if (isUnaryInteger(firstChild)) {
            if (tree.getChildCount() > 1) {
                throw new ParseException("invalid value for stream id"); //$NON-NLS-1$
            }
            long intval = UnaryIntegerParser.INSTANCE.parse(firstChild, null);
            return intval;
        }
        throw new ParseException("invalid value for stream id"); //$NON-NLS-1$
    }

}
