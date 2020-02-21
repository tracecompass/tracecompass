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
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * The "typealias" declaration can be used to give a name (including pointer
 * declarator specifier) to a type. It should also be used to map basic C types
 * (float, int, unsigned long, ...) to a CTF type. Typealias is a superset of
 * "typedef": it also allows assignment of a simple variable identifier to a
 * type.
 *
 * @author Matthew Khouzam - Inital API and implementation
 * @author Efficios - Documentation
 *
 */
public final class TypeAliasParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameters for the typealias parser
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final DeclarationScope fDeclarationScope;
        private final CTFTrace fTrace;

        /**
         * Parameter constructor
         *
         * @param trace
         *            the trace
         * @param scope
         *            the scope
         */
        public Param(CTFTrace trace, DeclarationScope scope) {
            fTrace = trace;
            fDeclarationScope = scope;
        }
    }

    /**
     * Instance
     */
    public static final TypeAliasParser INSTANCE = new TypeAliasParser();

    private TypeAliasParser() {
    }

    @Override
    public IDeclaration parse(CommonTree typealias, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        DeclarationScope scope = ((Param) param).fDeclarationScope;

        List<CommonTree> children = typealias.getChildren();

        CommonTree target = null;
        CommonTree alias = null;

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEALIAS_TARGET:
                target = child;
                break;
            case CTFParser.TYPEALIAS_ALIAS:
                alias = child;
                break;
            default:
                throw childTypeError(child);
            }
        }
        CTFTrace trace = ((Param) param).fTrace;
        IDeclaration targetDeclaration = TypeAliasTargetParser.INSTANCE.parse(target, new TypeAliasTargetParser.Param(trace, scope));

        if ((targetDeclaration instanceof VariantDeclaration)
                && ((VariantDeclaration) targetDeclaration).isTagged()) {
            throw new ParseException("Typealias of untagged variant is not permitted"); //$NON-NLS-1$
        }

        String aliasString = TypeAliasAliasParser.INSTANCE.parse(alias, null);

        scope.registerType(aliasString, targetDeclaration);
        return targetDeclaration;
    }

}
