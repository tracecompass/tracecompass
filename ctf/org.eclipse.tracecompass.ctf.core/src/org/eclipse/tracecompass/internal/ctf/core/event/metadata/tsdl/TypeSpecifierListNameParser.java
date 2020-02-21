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

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * Type specifier list name parser (is it a bool? a string... )
 *
 * @author Matthew Khouzam
 *
 */
public final class TypeSpecifierListNameParser implements ICommonTreeParser {

    /**
     * Instance
     */
    public static final TypeSpecifierListNameParser INSTANCE = new TypeSpecifierListNameParser();

    private TypeSpecifierListNameParser() {
    }

    /**
     * Creates the string representation of a type specifier.
     *
     * @param typeSpecifier
     *            A TYPE_SPECIFIER node.
     *
     * @return param unused
     * @throws ParseException
     *             invalid node
     */
    @Override
    public StringBuilder parse(CommonTree typeSpecifier, ICommonTreeParserParameter param) throws ParseException {
        StringBuilder sb = new StringBuilder();
        switch (typeSpecifier.getType()) {
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
        case CTFParser.CONSTTOK:
        case CTFParser.IDENTIFIER:
            parseSimple(typeSpecifier, sb);
            break;
        case CTFParser.STRUCT: {
            parseStruct(typeSpecifier, sb);
            break;
        }
        case CTFParser.VARIANT: {
            parseVariant(typeSpecifier, sb);
            break;
        }
        case CTFParser.ENUM: {
            parseEnum(typeSpecifier, sb);
            break;
        }
        case CTFParser.FLOATING_POINT:
        case CTFParser.INTEGER:
        case CTFParser.STRING:
            throw new ParseException("CTF type found in createTypeSpecifierString"); //$NON-NLS-1$
        default:
            throw childTypeError(typeSpecifier);
        }
        return sb;

    }

    private static void parseEnum(CommonTree typeSpecifier, StringBuilder sb) throws ParseException {
        CommonTree enumName = (CommonTree) typeSpecifier.getFirstChildWithType(CTFParser.ENUM_NAME);
        if (enumName == null) {
            throw new ParseException("nameless enum found in createTypeSpecifierString"); //$NON-NLS-1$
        }

        CommonTree enumNameIdentifier = (CommonTree) enumName.getChild(0);

        parseSimple(enumNameIdentifier, sb);
    }

    private static void parseVariant(CommonTree typeSpecifier, StringBuilder sb) throws ParseException {
        CommonTree variantName = (CommonTree) typeSpecifier.getFirstChildWithType(CTFParser.VARIANT_NAME);
        if (variantName == null) {
            throw new ParseException("nameless variant found in createTypeSpecifierString"); //$NON-NLS-1$
        }

        CommonTree variantNameIdentifier = (CommonTree) variantName.getChild(0);

        parseSimple(variantNameIdentifier, sb);
    }

    private static void parseSimple(CommonTree typeSpecifier, StringBuilder sb) {
        sb.append(typeSpecifier.getText());
    }

    private static void parseStruct(CommonTree typeSpecifier, StringBuilder sb) throws ParseException {
        CommonTree structName = (CommonTree) typeSpecifier.getFirstChildWithType(CTFParser.STRUCT_NAME);
        if (structName == null) {
            throw new ParseException("nameless struct found in createTypeSpecifierString"); //$NON-NLS-1$
        }

        CommonTree structNameIdentifier = (CommonTree) structName.getChild(0);

        parseSimple(structNameIdentifier, sb);
    }

}
