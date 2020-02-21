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

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isUnaryInteger;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryIntegerParser;

/**
 * A version number parser looking up major/minor
 *
 * @author Matthew Khouzam
 *
 */
public final class VersionNumberParser implements ICommonTreeParser {

    private static final String ERROR = "Invalid value for major/minor"; //$NON-NLS-1$

    /**
     * The instance
     */
    public static final VersionNumberParser INSTANCE = new VersionNumberParser();

    private VersionNumberParser() {
    }

    /**
     * Parse a version and get the major or minor value
     *
     * @param tree
     *            the AST node of the version
     * @param unused
     *            unused
     * @return the version value as a {@link Long}
     * @throws ParseException
     *             the AST is malformed
     */
    @Override
    public Long parse(CommonTree tree, ICommonTreeParserParameter unused) throws ParseException {

        CommonTree firstChild = (CommonTree) tree.getChild(0);

        if (isUnaryInteger(firstChild)) {
            if (tree.getChildCount() > 1) {
                throw new ParseException(ERROR);
            }
            long version = UnaryIntegerParser.INSTANCE.parse(firstChild, null);
            if (version < 0) {
                throw new ParseException(ERROR);
            }
            return version;
        }
        throw new ParseException(ERROR);
    }

}
