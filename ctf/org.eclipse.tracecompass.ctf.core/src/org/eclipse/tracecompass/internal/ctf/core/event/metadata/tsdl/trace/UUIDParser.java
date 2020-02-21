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

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.trace;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isAnyUnaryString;

import java.util.UUID;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryStringParser;

/**
 * <strong>Trace UUID</strong>, used to ensure the event packet match the
 * metadata used. Note: we cannot use a metadata checksum in every cases instead
 * of a UUID because metadata can be appended to while tracing is active. This
 * field is optional.
 *
 * @author Matthew Khouzam
 *
 */
public final class UUIDParser implements ICommonTreeParser {

    private static final String INVALID_FORMAT_FOR_UUID = "Invalid format for UUID"; //$NON-NLS-1$
    private static final String INVALID_VALUE_FOR_UUID = "Invalid value for UUID"; //$NON-NLS-1$
    /** Instance */
    public static final UUIDParser INSTANCE = new UUIDParser();

    private UUIDParser() {
    }

    /**
     * Parse a UUID String and get a {@link UUID} in return.
     *
     * @param tree
     *            the UUID AST
     * @param unused
     *            unused
     * @return a {@link UUID}
     * @throws ParseException
     *             the AST was malformed
     */
    @Override
    public UUID parse(CommonTree tree, ICommonTreeParserParameter unused) throws ParseException {

        CommonTree firstChild = (CommonTree) tree.getChild(0);

        if (isAnyUnaryString(firstChild)) {
            if (tree.getChildCount() > 1) {
                throw new ParseException(INVALID_VALUE_FOR_UUID);
            }

            String uuidstr = UnaryStringParser.INSTANCE.parse(firstChild, null);

            try {
                return UUID.fromString(uuidstr);
            } catch (IllegalArgumentException e) {
                throw new ParseException(INVALID_FORMAT_FOR_UUID, e);
            }
        }
        throw new ParseException(INVALID_VALUE_FOR_UUID);
    }

}
