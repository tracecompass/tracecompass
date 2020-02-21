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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * C typedef parser
 *
 * @author Matthew Khouzam
 *
 */
public final class TypedefParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter object with a trace and current scope
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final DeclarationScope fDeclarationScope;
        private final CTFTrace fTrace;

        /**
         * Constructor
         *
         * @param trace
         *            the trace
         * @param scope
         *            the current scope
         */
        public Param(CTFTrace trace, DeclarationScope scope) {
            fTrace = trace;
            fDeclarationScope = scope;
        }
    }

    /**
     * The instance
     */
    public static final TypedefParser INSTANCE = new TypedefParser();

    private TypedefParser() {
    }

    /**
     * Parses a typedef node. This creates and registers a new declaration for
     * each declarator found in the typedef.
     *
     * @param typedef
     *            A TYPEDEF node.
     *
     * @return map of type name to type declaration
     * @throws ParseException
     *             If there is an error creating the declaration.
     */
    @Override
    public Map<String, IDeclaration> parse(CommonTree typedef, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        DeclarationScope scope = ((Param) param).fDeclarationScope;

        CommonTree typeDeclaratorListNode = (CommonTree) typedef.getFirstChildWithType(CTFParser.TYPE_DECLARATOR_LIST);
        if (typeDeclaratorListNode == null) {
            throw new ParseException("Cannot have a typedef without a declarator"); //$NON-NLS-1$
        }
        CommonTree typeSpecifierListNode = (CommonTree) typedef.getFirstChildWithType(CTFParser.TYPE_SPECIFIER_LIST);
        if (typeSpecifierListNode == null) {
            throw new ParseException("Cannot have a typedef without specifiers"); //$NON-NLS-1$
        }
        List<CommonTree> typeDeclaratorList = typeDeclaratorListNode.getChildren();

        Map<String, IDeclaration> declarations = new HashMap<>();

        for (CommonTree typeDeclaratorNode : typeDeclaratorList) {
            StringBuilder identifierSB = new StringBuilder();
            CTFTrace trace = ((Param) param).fTrace;
            IDeclaration typeDeclaration = TypeDeclaratorParser.INSTANCE.parse(typeDeclaratorNode, new TypeDeclaratorParser.Param(trace, typeSpecifierListNode, scope, identifierSB));

            if ((typeDeclaration instanceof VariantDeclaration)
                    && !((VariantDeclaration) typeDeclaration).isTagged()) {
                throw new ParseException("Typealias of untagged variant is not permitted"); //$NON-NLS-1$
            }

            scope.registerType(identifierSB.toString(), typeDeclaration);

            declarations.put(identifierSB.toString(), typeDeclaration);
        }
        return declarations;
    }

}
