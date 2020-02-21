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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * Basic parser for all abstract data types
 *
 * @author Matthew Khouzam
 *
 */
public final class TypeDeclarationParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter object with a current scope and a list of pointers
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final DeclarationScope fDeclarationScope;
        private final @Nullable List<CommonTree> fPointerList;

        /**
         * Constructor
         *
         * @param pointerList
         *            the list of pointers
         * @param scope
         *            the current scope
         */
        public Param(@Nullable List<CommonTree> pointerList, DeclarationScope scope) {
            fPointerList = pointerList;
            fDeclarationScope = scope;
        }
    }

    /**
     * Instance
     */
    public static final TypeDeclarationParser INSTANCE = new TypeDeclarationParser();

    private TypeDeclarationParser() {
    }

    /**
     * Parses a type specifier list as a user-declared type.
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node containing a user-declared type.
     * @param param
     *            (pointerList, currentscope) A list of POINTER nodes that apply
     *            to the type specified in typeSpecifierList.
     *
     * @return The corresponding declaration.
     * @throws ParseException
     *             If the type does not exist (has not been found).
     */
    @Override
    public IDeclaration parse(CommonTree typeSpecifierList, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        DeclarationScope scope = ((Param) param).fDeclarationScope;

        List<CommonTree> pointerList = ((Param) param).fPointerList;
        /* Create the string representation of the type declaration */
        String typeStringRepresentation = TypeDeclarationStringParser.INSTANCE.parse(typeSpecifierList, new TypeDeclarationStringParser.Param(pointerList));

        /*
         * Use the string representation to search the type in the current scope
         */
        IDeclaration decl = scope.lookupTypeRecursive(typeStringRepresentation);

        if (decl == null) {
            throw new ParseException("Type " + typeStringRepresentation //$NON-NLS-1$
                    + " has not been defined."); //$NON-NLS-1$
        }

        return decl;
    }

}
