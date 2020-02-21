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

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.enumeration;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.childTypeError;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 *
 * Enumerations are a mapping between an integer type and a table of strings.
 * The numerical representation of the enumeration follows the integer type
 * specified by the metadata. The enumeration mapping table is detailed in the
 * enumeration description within the metadata. The mapping table maps inclusive
 * value ranges (or single values) to strings. Instead of being limited to
 * simple `value -> string` mappings, these enumerations map `[ start_value ...
 * end_value ] -> string`, which map inclusive ranges of values to strings. An
 * enumeration from the C language can be represented in this format by having
 * the same `start_value` and `end_value` for each mapping, which is in fact a
 * range of size 1. This single-value range is supported without repeating the
 * start and end values with the `value = string` declaration. Enumerations need
 * to contain at least one entry. <br>
 *
 * <pre>
enum name : integer_type {
    somestring          =  start_value1 ... end_value1 ,
    "other string"      = start_value2 ... end_value2 ,
    yet_another_string,   will be assigned to end_value2 + 1 ,
    "some other string" = value,
    //...
}
 * </pre>
 *
 * If the values are omitted, the enumeration starts at 0 and increment of 1 for
 * each entry. An entry with omitted value that follows a range entry takes as
 * value the `end_value` of the previous range + 1:
 *
 * <pre>
enum name : unsigned int {
 ZERO,
 ONE,
 TWO,
 TEN = 10,
 ELEVEN,
}
 * </pre>
 *
 * Overlapping ranges within a single enumeration are implementation defined.
 * <br>
 * A nameless enumeration can be declared as a field type or as part of a
 * `typedef`:
 *
 * <pre>
enum : integer_type {
 // ...
}
 * </pre>
 *
 * Enumerations omitting the container type`:integer_type`use the`int`type(for
 * compatibility with C99).The`int`type _must be_ previously declared,e.g.:
 *
 * <pre>
 * typealias integer{size=32;align=32;signed=true;}:=int;

enum{
// ...
}
 * </pre>
 *
 * @author Matthew Khouzam
 * @author Efficios - Description
 *
 */
public final class EnumParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter Object
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {

        private final DeclarationScope fCurrentScope;
        private final CTFTrace fTrace;

        /**
         * Parameter object constructor
         *
         * @param trace
         *            Trace to populate
         * @param currentScope
         *            the current scope
         */
        public Param(CTFTrace trace, DeclarationScope currentScope) {
            fTrace = trace;
            fCurrentScope = currentScope;
        }

    }

    /**
     * Instance
     */
    public static final EnumParser INSTANCE = new EnumParser();

    private EnumParser() {
    }

    /**
     * Parses an enum declaration and returns the corresponding declaration.
     *
     * @param theEnum
     *            An ENUM node.
     *
     * @return The corresponding enum declaration.
     * @throws ParseException
     *             there was an error parsing the enum. Probably a mal-formed
     *             tree.
     */
    @Override
    public EnumDeclaration parse(CommonTree theEnum, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        Param parameter = (Param) param;
        DeclarationScope scope = parameter.fCurrentScope;

        List<CommonTree> children = theEnum.getChildren();

        /* The return value */
        EnumDeclaration enumDeclaration = null;

        /* Name */
        @Nullable String enumName = null;

        /* Body */
        CommonTree enumBody = null;

        /* Container type */
        IntegerDeclaration containerTypeDeclaration = null;

        /* Loop on all children and identify what we have to work with. */
        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.ENUM_NAME: {
                CommonTree enumNameIdentifier = (CommonTree) child.getChild(0);
                enumName = enumNameIdentifier.getText();
                break;
            }
            case CTFParser.ENUM_BODY: {
                enumBody = child;
                break;
            }
            case CTFParser.ENUM_CONTAINER_TYPE: {
                CTFTrace trace = ((Param) param).fTrace;
                containerTypeDeclaration = EnumContainerParser.INSTANCE.parse(child, new EnumContainerParser.Param(trace, scope));
                break;
            }
            default:
                throw childTypeError(child);
            }
        }

        /*
         * If the container type has not been defined explicitly, we assume it
         * is "int".
         */
        if (containerTypeDeclaration == null) {
            IDeclaration enumDecl;
            /*
             * it could be because the enum was already declared.
             */
            if (enumName != null) {
                scope.setName(enumName);
                enumDecl = scope.lookupEnumRecursive(enumName);
                if (enumDecl != null) {
                    return (EnumDeclaration) enumDecl;
                }
            }

            IDeclaration decl = scope.lookupTypeRecursive("int"); //$NON-NLS-1$

            if (decl == null) {
                throw new ParseException("enum container type implicit and type int not defined"); //$NON-NLS-1$
            } else if (!(decl instanceof IntegerDeclaration)) {
                throw new ParseException("enum container type implicit and type int not an integer"); //$NON-NLS-1$
            }

            containerTypeDeclaration = (IntegerDeclaration) decl;
        }

        /*
         * If it has a body, it's a new declaration, otherwise it's a reference
         * to an existing declaration. Same logic as struct.
         */
        if (enumBody != null) {
            /*
             * If enum has a name, check if already defined in the current
             * scope.
             */
            if ((enumName != null)
                    && (scope.lookupEnum(enumName) != null)) {
                throw new ParseException("enum " + enumName //$NON-NLS-1$
                        + " already defined"); //$NON-NLS-1$
            }

            /* Create the declaration */
            enumDeclaration = new EnumDeclaration(containerTypeDeclaration);

            /* Parse the body */
            EnumBodyParser.INSTANCE.parse(enumBody, new EnumBodyParser.Param(enumDeclaration));

            /* If the enum has name, add it to the current scope. */
            if (enumName != null) {
                scope.registerEnum(enumName, enumDeclaration);
            }
        } else {
            if (enumName != null) {
                /* Name and !body */

                /* Lookup the name in the current scope. */
                enumDeclaration = scope.lookupEnumRecursive(enumName);

                /*
                 * If not found, it means that an enum with such name has not
                 * been defined
                 */
                if (enumDeclaration == null) {
                    throw new ParseException("enum " + enumName //$NON-NLS-1$
                            + " is not defined"); //$NON-NLS-1$
                }
            } else {
                /* !Name and !body */
                throw new ParseException("enum with no name and no body"); //$NON-NLS-1$
            }
        }

        return enumDeclaration;

    }

}
