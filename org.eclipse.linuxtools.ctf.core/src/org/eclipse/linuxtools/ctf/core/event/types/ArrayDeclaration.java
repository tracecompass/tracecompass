/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Multimap;

/**
 * A CTF array declaration
 *
 * Arrays are fixed-length. Their length is declared in the type declaration
 * within the meta-data. They contain an array of "inner type" elements, which
 * can refer to any type not containing the type of the array being declared (no
 * circular dependency). The length is the number of elements in an array.
 *
 * @deprecated use
 *             {@link org.eclipse.linuxtools.internal.ctf.core.event.types.ArrayDeclaration}
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
@Deprecated
public class ArrayDeclaration extends CompoundDeclaration {

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
    private final Multimap<String, String> fChildrenNames = ArrayListMultimap.<String, String> create();

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
     *
     * @return how many elements in the array
     */
    public int getLength() {
        return fLength;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Deprecated
    @Override
    public ArrayDefinition createDefinition(IDefinitionScope definitionScope,
            @NonNull String fieldName, BitBuffer input) throws CTFReaderException {
        alignRead(input);
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
        List<String> elemNames = (List<String>) fChildrenNames.get(fieldName);
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

    /**
     * @since 3.0
     */
    @Override
    public int getMaximumSize() {
        long val = (long) fLength * fElemType.getMaximumSize();
        return (int) Math.min(Integer.MAX_VALUE, val);
    }

}
