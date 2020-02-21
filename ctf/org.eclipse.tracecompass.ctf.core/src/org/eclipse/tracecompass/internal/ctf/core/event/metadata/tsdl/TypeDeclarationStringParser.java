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
 * Type declaration String parser
 *
 * @author Matthew Khouzam
 *
 */
public final class TypeDeclarationStringParser implements ICommonTreeParser {

    /**
     * Parameter Object with a list of common trees
     *
     * @author Matthew Khouzam
     *
     */
    public static final class Param implements ICommonTreeParserParameter {
        private final List<CommonTree> fList;

        /**
         * Constructor
         *
         * @param list
         *            List of trees
         */
        public Param(List<CommonTree> list) {
            fList = list;
        }
    }

    /**
     * Instance
     */
    public static final TypeDeclarationStringParser INSTANCE = new TypeDeclarationStringParser();

    private TypeDeclarationStringParser() {
    }

    /**
     * Creates the string representation of a type specifier.
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER node.
     *
     * @return A StringBuilder to which will be appended the string.
     * @throws ParseException
     *             invalid node
     */
    @Override
    public String parse(CommonTree typeSpecifierList, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        List<CommonTree> pointers = ((Param) param).fList;
        StringBuilder sb = new StringBuilder();
        sb.append(TypeSpecifierListStringParser.INSTANCE.parse(typeSpecifierList, null));
        if (pointers != null) {
            CommonTree temp = new CommonTree();
            for (CommonTree pointer : pointers) {
                temp.addChild(pointer);
            }
            sb.append(PointerListStringParser.INSTANCE.parse(temp, null));
        }
        return sb.toString();
    }

}
