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

import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
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
public final class TypeAliasAliasParser implements ICommonTreeParser {

    /**
     * Instance
     */
    public static final TypeAliasAliasParser INSTANCE = new TypeAliasAliasParser();

    private TypeAliasAliasParser() {
    }

    /**
     * Parses the alias part of a typealias. It parses the underlying specifier
     * list and declarator and creates the string representation that will be
     * used to register the type. In typealias integer{ blabla } := int, the
     * alias is the <em>integer{ blabla }</em> part, and the target is the
     * <em>int</em> part.
     *
     * @param typeSpecifier
     *            A TYPEALIAS_ALIAS node.
     * @param param
     *            unused
     *
     * @return The string representation of the alias.
     * @throws ParseException
     *             if an alias is not allowed here
     */
    @Override
    public String parse(CommonTree typeSpecifier, ICommonTreeParserParameter param) throws ParseException {

        List<CommonTree> children = typeSpecifier.getChildren();

        CommonTree typeSpecifierList = null;
        CommonTree typeDeclaratorList = null;
        CommonTree typeDeclarator = null;
        List<CommonTree> pointers = new LinkedList<>();

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

        /* If there is a type declarator list, extract the pointers */
        if (typeDeclaratorList != null) {
            /*
             * Only allow one declarator
             *
             * eg: "typealias uint8_t := puint8_t *, **;" is not permitted.
             */
            if (typeDeclaratorList.getChildCount() != 1) {
                throw new ParseException("Only one type declarator is allowed in the typealias alias"); //$NON-NLS-1$
            }

            typeDeclarator = (CommonTree) typeDeclaratorList.getChild(0);

            List<CommonTree> typeDeclaratorChildren = typeDeclarator.getChildren();

            for (CommonTree child : typeDeclaratorChildren) {
                switch (child.getType()) {
                case CTFParser.POINTER:
                    pointers.add(child);
                    break;
                case CTFParser.IDENTIFIER:
                    throw new ParseException("Identifier (" + child.getText() //$NON-NLS-1$
                            + ") not expected in the typealias target"); //$NON-NLS-1$
                default:
                    throw childTypeError(child);
                }
            }
        }

        return TypeDeclarationStringParser.INSTANCE.parse(typeSpecifierList, new TypeDeclarationStringParser.Param(pointers));
    }

}
