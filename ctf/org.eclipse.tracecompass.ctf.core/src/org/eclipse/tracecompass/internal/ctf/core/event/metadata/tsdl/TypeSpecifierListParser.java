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

import java.nio.ByteOrder;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.enumeration.EnumParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.floatingpoint.FloatDeclarationParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.integer.IntegerDeclarationParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.string.StringDeclarationParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.struct.StructParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.variant.VariantParser;
import org.eclipse.tracecompass.internal.ctf.core.event.types.composite.EventHeaderCompactDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.types.composite.EventHeaderLargeDeclaration;

/**
 * Parse the specifiers. Make sure they are known types. This can be seen as a
 * declaration factory.
 *
 * @author Matthew Khouzam
 *
 */
public final class TypeSpecifierListParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter Object
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final DeclarationScope fDeclarationScope;
        private final @Nullable List<CommonTree> fListNode;
        private final CTFTrace fTrace;
        private final @Nullable CommonTree fIdentifier;

        /**
         * Constructor
         *
         * @param trace
         *            The trace
         * @param listNode
         *            A list of POINTER nodes that apply to the specified type.
         * @param identifier
         *            the identifier node
         * @param scope
         *            the current scope
         */
        public Param(CTFTrace trace, @Nullable List<CommonTree> listNode, @Nullable CommonTree identifier, DeclarationScope scope) {
            fTrace = trace;
            fListNode = listNode;
            fIdentifier = identifier;
            fDeclarationScope = scope;
        }
    }

    /**
     * The instance
     */
    public static final TypeSpecifierListParser INSTANCE = new TypeSpecifierListParser();

    private TypeSpecifierListParser() {
    }

    /**
     * Parses a type specifier list and returns the corresponding declaration.
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node.
     * @param param
     *            The paramer object
     *
     * @return The corresponding declaration.
     * @throws ParseException
     *             If the type has not been defined or if there is an error
     *             creating the declaration.
     */
    @Override
    public IDeclaration parse(CommonTree typeSpecifierList, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        final DeclarationScope scope = ((Param) param).fDeclarationScope;
        List<@NonNull CommonTree> pointerList = ((Param) param).fListNode;
        CTFTrace trace = ((Param) param).fTrace;
        CommonTree identifier = ((Param) param).fIdentifier;
        IDeclaration declaration = null;

        /*
         * By looking at the first element of the type specifier list, we can
         * determine which type it belongs to.
         */
        CommonTree firstChild = (CommonTree) typeSpecifierList.getChild(0);

        switch (firstChild.getType()) {
        case CTFParser.FLOATING_POINT:
            declaration = FloatDeclarationParser.INSTANCE.parse(firstChild, new FloatDeclarationParser.Param(trace));
            break;
        case CTFParser.INTEGER:
            declaration = IntegerDeclarationParser.INSTANCE.parse(firstChild, new IntegerDeclarationParser.Param(trace));
            break;
        case CTFParser.STRING:
            declaration = StringDeclarationParser.INSTANCE.parse(firstChild, null);
            break;
        case CTFParser.STRUCT:
            declaration = StructParser.INSTANCE.parse(firstChild, new StructParser.Param(trace, identifier, scope));
            StructDeclaration structDeclaration = (StructDeclaration) declaration;
            if (structDeclaration.hasField(MetadataStrings.ID)) {
                IDeclaration idEnumDecl = structDeclaration.getField(MetadataStrings.ID);
                if (idEnumDecl instanceof EnumDeclaration) {
                    EnumDeclaration enumDeclaration = (EnumDeclaration) idEnumDecl;
                    ByteOrder bo = enumDeclaration.getContainerType().getByteOrder();
                    if (EventHeaderCompactDeclaration.getEventHeader(bo).isCompactEventHeader(structDeclaration)) {
                        declaration = EventHeaderCompactDeclaration.getEventHeader(bo);
                    } else if (EventHeaderLargeDeclaration.getEventHeader(bo).isLargeEventHeader(structDeclaration)) {
                        declaration = EventHeaderLargeDeclaration.getEventHeader(bo);
                    }
                }
            }
            break;
        case CTFParser.VARIANT:
            declaration = VariantParser.INSTANCE.parse(firstChild, new VariantParser.Param(trace, scope));
            break;
        case CTFParser.ENUM:
            declaration = EnumParser.INSTANCE.parse(firstChild, new EnumParser.Param(trace, scope));
            break;
        case CTFParser.IDENTIFIER:
        case CTFParser.FLOATTOK:
        case CTFParser.INTTOK:
        case CTFParser.LONGTOK:
        case CTFParser.SHORTTOK:
        case CTFParser.SIGNEDTOK:
        case CTFParser.UNSIGNEDTOK:
        case CTFParser.CHARTOK:
        case CTFParser.DOUBLETOK:
        case CTFParser.VOIDTOK:
        case CTFParser.BOOLTOK:
        case CTFParser.COMPLEXTOK:
        case CTFParser.IMAGINARYTOK:
            declaration = TypeDeclarationParser.INSTANCE.parse(typeSpecifierList, new TypeDeclarationParser.Param(pointerList, scope));
            break;
        default:
            throw childTypeError(firstChild);
        }

        return declaration;
    }
}
