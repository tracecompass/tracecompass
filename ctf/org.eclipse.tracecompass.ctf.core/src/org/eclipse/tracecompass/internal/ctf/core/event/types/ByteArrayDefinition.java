/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.types;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.CompoundDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * A fixed length string definition
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public final class ByteArrayDefinition extends AbstractArrayDefinition {

    private final byte[] fContent;
    private transient @Nullable List<Definition> fDefs;

    /**
     * An fixed length string declaration, it's created by sequence or array
     * defintions
     *
     * @param declaration
     *            the declaration
     * @param definitionScope
     *            the definition scope
     * @param fieldName
     *            the field name
     * @param content
     *            the string content
     */
    public ByteArrayDefinition(CompoundDeclaration declaration,
            @Nullable IDefinitionScope definitionScope,
            String fieldName,
            byte[] content) {
        super(declaration, definitionScope, fieldName);
        fContent = content;

    }

    @Override
    public int getLength() {
        return fContent.length;
    }

    @Override
    public synchronized List<Definition> getDefinitions() {
        List<Definition> defs = fDefs;
        if (defs == null) {
            ImmutableList.Builder<Definition> builder = new ImmutableList.Builder<>();
            for (int i = 0; i < fContent.length; i++) {
                IntegerDeclaration charDecl = IntegerDeclaration.UINT_8_DECL;
                String fieldName = getFieldName() + '[' + i + ']';
                byte fieldValue = fContent[i];
                builder.add(new IntegerDefinition(charDecl, getDefinitionScope(), fieldName, fieldValue));
            }
            fDefs = NonNullUtils.checkNotNull(builder.build());
            return fDefs;
        }

        return defs;
    }

    @Override
    public String toString() {
        if (((CompoundDeclaration) getDeclaration()).isString()) {
            /*
             * the string is a byte array and may contain more than the string
             * plus a null char, this will truncate it back to a null char
             */
            int pos = -1;
            for (int i = 0; i < fContent.length; i++) {
                if (fContent[i] == 0) {
                    pos = i;
                    break;
                }
            }
            byte[] bytes = (pos != -1) ? (Arrays.copyOf(fContent, pos)) : fContent;
            return new String(bytes);
        }
        StringBuilder b = new StringBuilder();
        b.append('[');
        Joiner.on(", ").appendTo(b, Arrays.asList(fContent)); //$NON-NLS-1$
        b.append(']');
        return checkNotNull(b.toString());
    }
}