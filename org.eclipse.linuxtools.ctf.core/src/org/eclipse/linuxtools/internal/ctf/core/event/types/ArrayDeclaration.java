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

package org.eclipse.linuxtools.internal.ctf.core.event.types;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.CompoundDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

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
 * @since 3.1
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
    private final ArrayListMultimap<String, String> fChildrenNames = ArrayListMultimap.create();

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

    /**
     * Sometimes, strings are encoded as an array of 1-byte integers (each one
     * being an UTF-8 byte).
     *
     * @return true if this array is in fact an UTF-8 string. false if it's a
     *         "normal" array of generic Definition's.
     */
    public boolean isString() {
        if (fElemType instanceof IntegerDeclaration) {
            /*
             * If the first byte is a "character", we'll consider the whole
             * array a character string.
             */
            IntegerDeclaration elemInt = (IntegerDeclaration) fElemType;
            if (elemInt.isCharacter()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getAlignment() {
        return getElementType().getAlignment();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public AbstractArrayDefinition createDefinition(IDefinitionScope definitionScope,
            @NonNull String fieldName, BitBuffer input) throws CTFReaderException {
        alignRead(input);
        if (isString()) {
            byte[] data = new byte[fLength];
            input.get(data);
            return new ByteArrayDefinition(this, definitionScope, fieldName, data);
        }
        List<Definition> definitions = read(input, definitionScope, fieldName);
        return new ArrayDefinition(this, definitionScope, fieldName, definitions);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] array[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

    @NonNull
    private List<Definition> read(@NonNull BitBuffer input, IDefinitionScope definitionScope, String fieldName) throws CTFReaderException {
        Builder<Definition> definitions = new ImmutableList.Builder<>();
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
        @SuppressWarnings("null")
        @NonNull ImmutableList<Definition> ret = definitions.build();
        return ret;
    }

    @Override
    public int getMaximumSize() {
        long val = (long) fLength * fElemType.getMaximumSize();
        return (int) Math.min(Integer.MAX_VALUE, val);
    }

}