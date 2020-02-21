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

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.childTypeError;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * Type alias targets are the nodes of the declaration of the typealiases.
 * Typealiases are a superset of typedef defined in TSDL.
 *
 *
 * @author Matthew Khouzam
 *
 */
public final class TypeAliasTargetParser extends AbstractScopedCommonTreeParser {

    /**
     * A parameter object with a trace and a scope
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
    public static final TypeAliasTargetParser INSTANCE = new TypeAliasTargetParser();

    private TypeAliasTargetParser() {
    }

    /**
     * Parses the target part of a typealias and gets the corresponding
     * declaration. In typealias integer{ blabla } := int, the alias is the
     * <em>integer{ blabla }</em> part, and the target is the <em>int</em> part.
     *
     * Typealiases only allow one declarator.
     *
     * eg: "typealias uint8_t *, ** := puint8_t;" is not permitted, otherwise
     * the new type puint8_t would maps to two different types.
     *
     * @param target
     *            A TYPEALIAS_TARGET node.
     *
     * @return The corresponding declaration.
     * @throws ParseException
     *             an invalid child in the tree
     */
    @Override
    public IDeclaration parse(CommonTree target, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        DeclarationScope scope = ((Param) param).fDeclarationScope;

        List<CommonTree> children = target.getChildren();

        CommonTree typeSpecifierList = null;
        CommonTree typeDeclaratorList = null;
        CommonTree typeDeclarator = null;
        StringBuilder identifierSB = new StringBuilder();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPE_SPECIFIER_LIST:
                typeSpecifierList = child;
                break;
            case CTFParser.TYPE_DECLARATOR_LIST:
                typeDeclaratorList = child;
                break;
            default:
                throw childTypeError(child);
            }
        }

        if (typeDeclaratorList != null) {
            /*
             * Only allow one declarator
             *
             * eg: "typealias uint8_t *, ** := puint8_t;" is not permitted,
             * otherwise the new type puint8_t would maps to two different
             * types.
             */
            if (typeDeclaratorList.getChildCount() != 1) {
                throw new ParseException("Only one type declarator is allowed in the typealias target"); //$NON-NLS-1$
            }

            typeDeclarator = (CommonTree) typeDeclaratorList.getChild(0);
        }
        if (typeSpecifierList == null) {
            throw new ParseException("Cannot have a typealias with no specifiers"); //$NON-NLS-1$
        }
        CTFTrace trace = ((Param) param).fTrace;
        /* Parse the target type and get the declaration */
        IDeclaration targetDeclaration = TypeDeclaratorParser.INSTANCE.parse(typeDeclarator,
                new TypeDeclaratorParser.Param(trace, typeSpecifierList, scope, identifierSB));

        /*
         * We don't allow identifier in the target
         *
         * eg: "typealias uint8_t* hello := puint8_t;", the "hello" is not
         * permitted
         */
        if (identifierSB.length() > 0) {
            throw new ParseException("Identifier (" + identifierSB.toString() //$NON-NLS-1$
                    + ") not expected in the typealias target"); //$NON-NLS-1$
        }

        return targetDeclaration;
    }

}
