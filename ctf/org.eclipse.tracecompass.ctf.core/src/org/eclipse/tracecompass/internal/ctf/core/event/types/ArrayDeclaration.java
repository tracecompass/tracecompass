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

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.CompoundDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * A CTF array declaration
 *
 * Arrays are fixed-length. Their length is declared in the type declaration
 * within the meta-data. They contain an array of "inner type" elements, which
 * can refer to any type not containing the type of the array being declared (no
 * circular dependency). The length is the number of elements in an array.
 *
 * @author Matthew Khouzam
 */
public final class ArrayDeclaration extends CompoundDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final int fLength;
    private final IDeclaration fElemType;

    /**
     * <pre>
     * Cache where we can pre-generate the children names
     * Key&colon; parent name
     * Value&colon; children names
     * ex: field &#8594; &lbrace;field&lbrack;0&rbrack;, field&lbrack;1&rbrack;, &hellip; field&lbrack;n&rbrack;&rbrace;
     * </pre>
     *
     * TODO: investigate performance
     */
    private final transient ArrayListMultimap<String, String> fChildrenNames = ArrayListMultimap.create();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param length
     *            how many elements in the array
     * @param elemType
     *            what type of element is in the array
     */
    public ArrayDeclaration(int length, IDeclaration elemType) {
        fLength = length;
        fElemType = elemType;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public IDeclaration getElementType() {
        return fElemType;
    }

    /**
     * Get the length of the array
     *
     * @return the length of the array
     */
    public int getLength() {
        return fLength;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public AbstractArrayDefinition createDefinition(@Nullable IDefinitionScope definitionScope,
            @NonNull String fieldName, BitBuffer input) throws CTFException {
        alignRead(input);
        if (isAlignedBytes()) {
            byte[] data = new byte[fLength];
            if (input.getByteBuffer().remaining() < fLength) {
                throw new CTFException("Buffer underflow"); //$NON-NLS-1$
            }
            input.get(data);

            return new ByteArrayDefinition(this, definitionScope, fieldName, data);
        }
        @NonNull List<@NonNull Definition> definitions = read(input, definitionScope, fieldName);
        return new ArrayDefinition(this, definitionScope, fieldName, definitions);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] array[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

    private @NonNull List<@NonNull Definition> read(@NonNull BitBuffer input, @Nullable IDefinitionScope definitionScope, String fieldName) throws CTFException {
        Builder<@NonNull Definition> definitions = new ImmutableList.Builder<>();
        if (!fChildrenNames.containsKey(fieldName)) {
            for (int i = 0; i < fLength; i++) {
                fChildrenNames.put(fieldName, fieldName + '[' + i + ']');
            }
        }
        List<String> elemNames = fChildrenNames.get(fieldName);
        for (int i = 0; i < fLength; i++) {
            String name = elemNames.get(i);
            if (name == null) {
                throw new IllegalStateException();
            }
            definitions.add(fElemType.createDefinition(definitionScope, name, input));
        }
        return definitions.build();
    }

    @Override
    public int getMaximumSize() {
        long val = (long) fLength * fElemType.getMaximumSize();
        return (int) Math.min(Integer.MAX_VALUE, val);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fElemType.hashCode();
        result = prime * result + fLength;
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArrayDeclaration other = (ArrayDeclaration) obj;
        if (!fElemType.equals(other.fElemType)) {
            return false;
        }
        if (fLength != other.fLength) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isBinaryEquivalent(@Nullable IDeclaration obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArrayDeclaration other = (ArrayDeclaration) obj;
        if (!fElemType.isBinaryEquivalent(other.fElemType)) {
            return false;
        }
        if (fLength != other.fLength) {
            return false;
        }
        return true;
    }

}