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
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.struct;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.childTypeError;

import java.util.Collections;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypeAliasParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypedefParser;

/**
 * A struct body can have any of the following elements, even though some of
 * them do not make sense:
 * <ul>
 * <li>align</li>
 * <li>callsite</li>
 * <li>const</li>
 * <li>char</li>
 * <li>clock</li>
 * <li>double</li>
 * <li>enum</li>
 * <li>env</li>
 * <li>event</li>
 * <li>floating_point</li>
 * <li>float</li>
 * <li>integer</li>
 * <li>int</li>
 * <li>long</li>
 * <li>short</li>
 * <li>signed</li>
 * <li>stream</li>
 * <li>string</li>
 * <li>struct</li>
 * <li>trace</li>
 * <li>typealias</li>
 * <li>typedef</li>
 * <li>unsigned</li>
 * <li>variant</li>
 * <li>void</li>
 * <li>_Bool</li>
 * <li>_Complex</li>
 * <li>_Imaginary</li>
 * </ul>
 *
 * @author Matthew Khouzam
 *
 */
public final class StructBodyParser extends AbstractScopedCommonTreeParser {

    /**
     * The parameter object
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final DeclarationScope fDeclarationScope;
        private final @Nullable String fName;
        private final StructDeclaration fStructDeclaration;
        private final CTFTrace fTrace;

        /**
         * Constructor
         *
         * @param structDeclaration
         *            struct declaration to populate
         * @param trace
         *            the trace
         * @param name
         *            the struct name
         * @param scope
         *            the current scope
         */
        public Param(StructDeclaration structDeclaration, CTFTrace trace, @Nullable String name, DeclarationScope scope) {
            fStructDeclaration = structDeclaration;
            fTrace = trace;
            fDeclarationScope = scope;
            fName = name;
        }
    }

    /**
     * The instance
     */
    public static final StructBodyParser INSTANCE = new StructBodyParser();

    private StructBodyParser() {
    }

    /**
     * Parse the body of a struct, so anything between the '{' '}'
     *
     * @param structBody
     *            the struct body AST node
     * @param param
     *            the struct body parameters
     * @return {@link StructDeclaration} that is now populated
     * @throws ParseException
     *             The AST is malformed
     */
    @Override
    public StructDeclaration parse(CommonTree structBody, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        String structName = ((Param) param).fName;
        final DeclarationScope scope = new DeclarationScope(((Param) param).fDeclarationScope, structName == null ? MetadataStrings.STRUCT : structName);
        StructDeclaration structDeclaration = ((Param) param).fStructDeclaration;
        List<CommonTree> structDeclarations = structBody.getChildren();
        if (structDeclarations == null) {
            structDeclarations = Collections.emptyList();
        }

        /*
         * If structDeclaration is null, structBody has no children and the
         * struct body is empty.
         */

        CTFTrace trace = ((Param) param).fTrace;

        for (CommonTree declarationNode : structDeclarations) {
            switch (declarationNode.getType()) {
            case CTFParser.TYPEALIAS:
                TypeAliasParser.INSTANCE.parse(declarationNode, new TypeAliasParser.Param(trace, scope));
                break;
            case CTFParser.TYPEDEF:
                TypedefParser.INSTANCE.parse(declarationNode, new TypedefParser.Param(trace, scope));
                StructDeclarationParser.INSTANCE.parse(declarationNode, new StructDeclarationParser.Param(structDeclaration, trace, scope));
                break;
            case CTFParser.SV_DECLARATION:
                StructDeclarationParser.INSTANCE.parse(declarationNode, new StructDeclarationParser.Param(structDeclaration, trace, scope));
                break;
            default:
                throw childTypeError(declarationNode);
            }
        }
        return structDeclaration;
    }

}
