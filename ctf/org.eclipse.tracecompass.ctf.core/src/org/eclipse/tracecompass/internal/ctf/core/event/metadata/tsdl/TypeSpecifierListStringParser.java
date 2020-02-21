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
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * Type specifier list string parser, parses a list of names
 *
 * @author Matthew Khouzam
 *
 */
public final class TypeSpecifierListStringParser implements ICommonTreeParser {

    /**
     * Instance
     */
    public static final TypeSpecifierListStringParser INSTANCE = new TypeSpecifierListStringParser();

    private TypeSpecifierListStringParser() {
    }

    /**
     * Creates the string representation of a type specifier.
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER node.
     * @param param
     *            unused
     *
     * @return A StringBuilder to which will be appended the string.
     * @throws ParseException
     *             invalid node
     */
    @Override
    public StringBuilder parse(CommonTree typeSpecifierList, ICommonTreeParserParameter param) throws ParseException {
        StringBuilder sb = new StringBuilder();
        List<CommonTree> children = typeSpecifierList.getChildren();

        boolean firstItem = true;

        for (CommonTree child : children) {
            if (!firstItem) {
                sb.append(' ');

            }

            firstItem = false;

            /* Append the string that represents this type specifier. */
            sb.append(TypeSpecifierListNameParser.INSTANCE.parse(child, null));
        }
        return sb;
    }

}
