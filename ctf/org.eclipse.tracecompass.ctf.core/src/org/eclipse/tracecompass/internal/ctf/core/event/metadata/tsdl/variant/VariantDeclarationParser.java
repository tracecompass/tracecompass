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
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.variant;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypeDeclaratorParser;

/**
 * This parses the (sub)declarations located IN a variant declaration.
 *
 * @author Matthew Khouzam
 */
public final class VariantDeclarationParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter Object
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final VariantDeclaration fVariant;
        private final DeclarationScope fDeclarationScope;
        private final CTFTrace fTrace;

        /**
         * Parameter Object Contructor
         *
         * @param variant
         *            variant declaration to populate
         * @param trace
         *            trace
         * @param scope
         *            current scope
         */
        public Param(VariantDeclaration variant, CTFTrace trace, DeclarationScope scope) {
            fVariant = variant;
            fTrace = trace;
            fDeclarationScope = scope;
        }
    }

    /**
     * Instance
     */
    public static final VariantDeclarationParser INSTANCE = new VariantDeclarationParser();

    private VariantDeclarationParser() {
    }

    /**
     * Parses the variant declaration and gets a {@link VariantDeclaration}
     * back.
     *
     * @param declaration
     *            the variant declaration AST node
     * @param param
     *            the {@link Param} parameter object
     * @return the {@link VariantDeclaration}
     * @throws ParseException
     *             if the AST is malformed
     */
    @Override
    public VariantDeclaration parse(CommonTree declaration, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        VariantDeclaration variant = ((Param) param).fVariant;
        final DeclarationScope scope = ((Param) param).fDeclarationScope;
        /* Get the type specifier list node */
        CommonTree typeSpecifierListNode = (CommonTree) declaration.getFirstChildWithType(CTFParser.TYPE_SPECIFIER_LIST);
        if (typeSpecifierListNode == null) {
            throw new ParseException("Variant need type specifiers"); //$NON-NLS-1$
        }

        /* Get the type declarator list node */
        CommonTree typeDeclaratorListNode = (CommonTree) declaration.getFirstChildWithType(CTFParser.TYPE_DECLARATOR_LIST);
        if (typeDeclaratorListNode == null) {
            throw new ParseException("Cannot have empty variant"); //$NON-NLS-1$
        }
        /* Get the type declarator list */
        List<CommonTree> typeDeclaratorList = typeDeclaratorListNode.getChildren();

        /*
         * For each type declarator, parse the declaration and add a field to
         * the variant
         */
        for (CommonTree typeDeclaratorNode : typeDeclaratorList) {

            StringBuilder identifierSB = new StringBuilder();
            CTFTrace trace = ((Param) param).fTrace;
            IDeclaration decl = TypeDeclaratorParser.INSTANCE.parse(typeDeclaratorNode,
                    new TypeDeclaratorParser.Param(trace, typeSpecifierListNode, scope, identifierSB));

            String name = identifierSB.toString();

            if (variant.hasField(name)) {
                throw new ParseException("variant: duplicate field " //$NON-NLS-1$
                        + name);
            }

            scope.registerIdentifier(name, decl);

            variant.addField(name, decl);
        }
        return variant;
    }

}
