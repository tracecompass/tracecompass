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

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.childTypeError;

import java.util.List;
import java.util.Set;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 *
 * A CTF variant is a selection between different types. A CTF variant must
 * always be defined within the scope of a structure or within fields contained
 * within a structure (defined recursively). A _tag_ enumeration field must
 * appear in either the same static scope, prior to the variant field (in field
 * declaration order), in an upper static scope, or in an upper dynamic scope
 * (see [Static and dynamic scopes](#spec7.3.2)). The type selection is
 * indicated by the mapping from the enumeration value to the string used as
 * variant type selector. The field to use as tag is specified by the
 * `tag_field`, specified between `< >` after the `variant` keyword for unnamed
 * variants, and after _variant name_ for named variants. It is not required
 * that each enumeration mapping appears as variant type tag field. It is also
 * not required that each variant type tag appears as enumeration mapping.
 * However, it is required that any enumeration mapping encountered within a
 * stream has a matching variant type tag field. <br>
 * The alignment of the variant is the alignment of the type as selected by the
 * tag value for the specific instance of the variant. The size of the variant
 * is the size as selected by the tag value for the specific instance of the
 * variant. <br>
 * The alignment of the type containing the variant is independent of the
 * variant alignment. For instance, if a structure contains two fields, a 32-bit
 * integer, aligned on 32 bits, and a variant, which contains two choices:
 * either a 32-bit field, aligned on 32 bits, or a 64-bit field, aligned on 64
 * bits, the alignment of the outmost structure will be 32-bit (the alignment of
 * its largest field, disregarding the alignment of the variant). The alignment
 * of the variant will depend on the selector: if the variant's 32-bit field is
 * selected, its alignment will be 32-bit, or 64-bit otherwise. It is important
 * to note that variants are specifically tailored for compactness in a stream.
 * Therefore, the relative offsets of compound type fields can vary depending on
 * the offset at which the compound type starts if it contains a variant that
 * itself contains a type with alignment larger than the largest field contained
 * within the compound type. This is caused by the fact that the compound type
 * may contain the enumeration that select the variant's choice, and therefore
 * the alignment to be applied to the compound type cannot be determined before
 * encountering the enumeration. <br>
 * Each variant type selector possess a field name, which is a unique identifier
 * within the variant. The identifier is not allowed to use any [reserved
 * keyword](#C.1.2). Replacing reserved keywords with underscore-prefixed field
 * names is recommended. Fields starting with an underscore should have their
 * leading underscore removed by the CTF trace readers. <br>
 * A named variant declaration followed by its definition within a structure
 * declaration: <br>
 *
 * <pre>
variant name {
    field_type sel1;
    field_type sel2;
    field_type sel3;
    // ...
};

struct {
    enum : integer_type { sel1, sel2, sel3, <em>more</em> } tag_field;
    // ...
    variant name <tag_field> v;
}
 * </pre>
 *
 * An unnamed variant definition within a structure is expressed by the
 * following TSDL metadata: <br>
 *
 * <pre>
struct {
    enum : integer_type { sel1, sel2, sel3, <em>more</em> } tag_field;
    // ...
    variant <tag_field> {
        field_type sel1;
        field_type sel2;
        field_type sel3;
        // ...
    } v;
}
 * </pre>
 *
 * Example of a named variant within a sequence that refers to a single tag
 * field: <br>
 *
 * <pre>
variant example {
    uint32_t a;
    uint64_t b;
    short c;
};

struct {
    enum : uint2_t { a, b, c } choice;
    unsigned int seqlen;
    variant example <choice> v[seqlen];
}
 * </pre>
 *
 * Example of an unnamed variant: <br>
 *
 * <pre>
struct {
    enum : uint2_t { a, b, c, d } choice;

    // Unrelated fields can be added between the variant and its tag
    int32_t somevalue;
    variant <choice> {
        uint32_t a;
        uint64_t b;
        short c;
        struct {
            unsigned int field1;
            uint64_t field2;
        } d;
    } s;
}
 * </pre>
 *
 * Example of an unnamed variant within an array: <br>
 *
 * <pre>
struct {
    enum : uint2_t { a, b, c } choice;
    variant <choice> {
        uint32_t a;
        uint64_t b;
        short c;
    } v[10];
}
 * </pre>
 *
 * <br>
 * Example of a variant type definition within a structure, where the defined
 * type is then declared within an array of structures. This variant refers to a
 * tag located in an upper static scope. This example clearly shows that a
 * variant type definition referring to the tag `x` uses the closest preceding
 * field from the static scope of the type definition. <br>
 *
 * <pre>
struct {
    enum : uint2_t { a, b, c, d } x;

     // "x" refers to the preceding "x" enumeration in the
     // static scope of the type definition.

    typedef variant <x> {
      uint32_t a;
      uint64_t b;
      short c;
    } example_variant;

    struct {
      enum : int { x, y, z } x; // This enumeration is not used by "v".

      // "v" uses the "enum : uint2_t { a, b, c, d }" tag.
      example_variant v;
    } a[10];
}
 * </pre>
 *
 * @author Matthew Khouzam
 * @author Efficios - Javadoc preamble
 *
 *
 */
public final class VariantParser extends AbstractScopedCommonTreeParser {

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
    public static final VariantParser INSTANCE = new VariantParser();

    private VariantParser() {
    }

    /**
     * Parse the variant
     *
     * @param variant
     *            the variant AST node
     * @param param
     *            the {@link Param} parameter object
     * @return a populated {@link VariantDeclaration}
     * @throws ParseException
     *             the AST is malformed
     */
    @Override
    public VariantDeclaration parse(CommonTree variant, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        final DeclarationScope scope = ((Param) param).fDeclarationScope;

        List<CommonTree> children = variant.getChildren();
        VariantDeclaration variantDeclaration = null;

        boolean hasName = false;
        String variantName = null;

        boolean hasBody = false;
        CommonTree variantBody = null;

        boolean hasTag = false;
        String variantTag = null;

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.VARIANT_NAME:

                hasName = true;

                CommonTree variantNameIdentifier = (CommonTree) child.getChild(0);

                variantName = variantNameIdentifier.getText();

                break;
            case CTFParser.VARIANT_TAG:

                hasTag = true;

                CommonTree variantTagIdentifier = (CommonTree) child.getChild(0);

                variantTag = variantTagIdentifier.getText();

                break;
            case CTFParser.VARIANT_BODY:

                hasBody = true;

                variantBody = child;

                break;
            default:
                throw childTypeError(child);
            }
        }

        if (hasBody) {
            /*
             * If variant has a name, check if already defined in the current
             * scope.
             */
            if (hasName
                    && (scope.lookupVariant(variantName) != null)) {
                throw new ParseException("variant " + variantName //$NON-NLS-1$
                        + " already defined."); //$NON-NLS-1$
            }

            /* Create the declaration */
            variantDeclaration = new VariantDeclaration();

            CTFTrace trace = ((Param) param).fTrace;
            /* Parse the body */
            VariantBodyParser.INSTANCE.parse(variantBody, new VariantBodyParser.Param(variantDeclaration, trace, variantName, scope));

            /* If variant has name, add it to the current scope. */
            if (hasName) {
                scope.registerVariant(variantName, variantDeclaration);
            }
        } else /* !hasBody */ {
            if (hasName) {
                /* Name and !body */

                /* Lookup the name in the current scope. */
                variantDeclaration = scope.lookupVariantRecursive(variantName);

                /*
                 * If not found, it means that a struct with such name has not
                 * been defined
                 */
                if (variantDeclaration == null) {
                    throw new ParseException("variant " + variantName //$NON-NLS-1$
                            + " is not defined"); //$NON-NLS-1$
                }
            } else {
                /* !Name and !body */

                /* We can't do anything with that. */
                throw new ParseException("variant with no name and no body"); //$NON-NLS-1$
            }
        }

        if (hasTag) {
            variantDeclaration.setTag(variantTag);

            IDeclaration decl = scope.lookupIdentifierRecursive(variantTag);
            if (decl == null) {
                throw new ParseException("Variant tag not found: " + variantTag); //$NON-NLS-1$
            }
            if (!(decl instanceof EnumDeclaration)) {
                throw new ParseException("Variant tag must be an enum: " + variantTag); //$NON-NLS-1$
            }
            EnumDeclaration tagDecl = (EnumDeclaration) decl;
            if (!intersects(tagDecl.getLabels(), variantDeclaration.getFields().keySet())) {
                throw new ParseException("Variant contains no values of the tag, impossible to use: " + variantName); //$NON-NLS-1$
            }
        }

        return variantDeclaration;
    }

    /**
     * Method to compute if the two sets intersect. Faster than computing the
     * intersection of a set as we break as soon as a common element is found.
     *
     * @param set
     *            first set
     * @param fasterSet
     *            second set, with faster lookups
     * @return true if a string is in both the set and the map's keySet
     */
    private static boolean intersects(Set<String> set, Set<String> fasterSet) {
        for (String setString : set) {
            if (fasterSet.contains(setString)) {
                return true;
            }
        }
        return false;
    }

}
