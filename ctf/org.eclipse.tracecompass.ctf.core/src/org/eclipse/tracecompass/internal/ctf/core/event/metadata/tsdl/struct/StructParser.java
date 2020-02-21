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

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.AlignmentParser;
import org.eclipse.tracecompass.internal.ctf.core.event.types.StructDeclarationFlattener;

/**
 *
 * Structures are aligned on the largest alignment required by basic types
 * contained within the structure. (This follows the ISO/C standard for
 * structures) <br>
 * TSDL metadata representation of a named structure:
 *
 * <pre>
struct name {
    field_type field_name;
    field_type field_name;
    // ...
};
 * </pre>
 *
 * Example:
 *
 * <pre>
struct example {
    integer {                   // nameless type
        size = 16;
        signed = true;
        align = 16;
    } first_field_name;
    uint64_t second_field_name; // named type declared in the metadata
};
 * </pre>
 *
 * The fields are placed in a sequence next to each other. They each possess a
 * field name, which is a unique identifier within the structure. The identifier
 * is not allowed to use any [reserved keyword](#specC.1.2). Replacing reserved
 * keywords with underscore-prefixed field names is <strong>recommended</strong>
 * . Fields starting with an underscore should have their leading underscore
 * removed by the CTF trace readers. <br>
 * A nameless structure can be declared as a field type or as part of a
 * `typedef`:
 *
 * <pre>
struct {
    // ...
}
 *
 * </pre>
 *
 * Alignment for a structure compound type can be forced to a minimum value by
 * adding an `align` specifier after the declaration of a structure body. This
 * attribute is read as: `align(value)`. The value is specified in bits. The
 * structure will be aligned on the maximum value between this attribute and the
 * alignment required by the basic types contained within the structure. e.g.
 *
 * <pre>
struct {
    // ...
} align(32)
}
 * </pre>
 *
 * @author Matthew Khouzam
 * @author Efficios - Javadoc preamble
 */
public final class StructParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter object
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final DeclarationScope fDeclarationScope;
        private final @Nullable CommonTree fIdentifier;
        private final CTFTrace fTrace;

        /**
         * Constructor
         *
         * @param trace
         *            the trace
         * @param identifier
         *            the identifier
         * @param scope
         *            the current scope
         */
        public Param(CTFTrace trace, @Nullable CommonTree identifier, DeclarationScope scope) {
            fTrace = trace;
            fIdentifier = identifier;
            fDeclarationScope = scope;
        }
    }

    /**
     * The instance
     */
    public static final StructParser INSTANCE = new StructParser();

    private StructParser() {
    }

    /**
     * Parse the struct AST node, So everything in "struct a {...};"
     *
     * @param struct
     *            the struct AST node
     * @param param
     *            the parameter object of {@link Param} type.
     * @return a {@link StructDeclaration} that is fully populated
     * @throws ParseException
     *             the AST is malformed
     */
    @Override
    public StructDeclaration parse(CommonTree struct, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        final DeclarationScope scope = ((Param) param).fDeclarationScope;
        CommonTree identifier = ((Param) param).fIdentifier;

        List<CommonTree> children = struct.getChildren();

        /* The return value */
        StructDeclaration structDeclaration = null;

        /* Name */
        String structName = null;
        boolean hasName = false;

        /* Body */
        CommonTree structBody = null;
        boolean hasBody = false;

        /* Align */
        long structAlign = 0;

        /* Loop on all children and identify what we have to work with. */
        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.STRUCT_NAME: {
                hasName = true;
                CommonTree structNameIdentifier = (CommonTree) child.getChild(0);
                structName = structNameIdentifier.getText();
                break;
            }
            case CTFParser.STRUCT_BODY: {
                hasBody = true;

                structBody = child;
                break;
            }
            case CTFParser.ALIGN: {
                CommonTree structAlignExpression = (CommonTree) child.getChild(0);

                structAlign = AlignmentParser.INSTANCE.parse(structAlignExpression, null);
                break;
            }
            default:
                throw childTypeError(child);
            }
        }

        if (!hasName && identifier != null) {
            structName = identifier.getText();
            hasName = true;
        }

        /*
         * If a struct has just a body and no name (just like the song,
         * "A Struct With No Name" by America (sorry for that...)), it's a
         * definition of a new type, so we create the type declaration and
         * return it. We can't add it to the declaration scope since there is no
         * name, but that's what we want because it won't be possible to use it
         * again to declare another field.
         *
         * If it has just a name, we look it up in the declaration scope and
         * return the associated declaration. If it is not found in the
         * declaration scope, it means that a struct with that name has not been
         * declared, which is an error.
         *
         * If it has both, then we create the type declaration and register it
         * to the current scope.
         *
         * If it has none, then what are we doing here ?
         */
        if (hasBody) {
            /*
             * If struct has a name, check if already defined in the current
             * scope.
             */
            if (hasName && (scope.lookupStruct(structName) != null)) {
                throw new ParseException("struct " + structName //$NON-NLS-1$
                        + " already defined."); //$NON-NLS-1$
            }
            /* Create the declaration */
            structDeclaration = new StructDeclaration(structAlign);

            CTFTrace trace = ((Param) param).fTrace;
            /* Parse the body */
            StructBodyParser.INSTANCE.parse(structBody, new StructBodyParser.Param(structDeclaration, trace, structName, scope));
            /* If struct has name, add it to the current scope. */
            if (hasName) {
                scope.registerStruct(structName, structDeclaration);
            }
        } else /* !hasBody */ {
            if (hasName) {
                /* Name and !body */

                /* Lookup the name in the current scope. */
                structDeclaration = scope.lookupStructRecursive(structName);

                /*
                 * If not found, it means that a struct with such name has not
                 * been defined
                 */
                if (structDeclaration == null) {
                    throw new ParseException("struct " + structName //$NON-NLS-1$
                            + " is not defined"); //$NON-NLS-1$
                }
            } else {
                /* !Name and !body */

                /* We can't do anything with that. */
                throw new ParseException("struct with no name and no body"); //$NON-NLS-1$
            }
        }
        return StructDeclarationFlattener.tryFlattenStruct(structDeclaration);
    }

}
