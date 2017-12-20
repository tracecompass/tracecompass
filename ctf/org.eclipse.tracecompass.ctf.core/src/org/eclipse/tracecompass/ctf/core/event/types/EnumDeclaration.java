/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import java.nio.ByteOrder;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * A CTF enum declaration.
 *
 * The definition of a enum point basic data type. It will take the data from a
 * trace and store it (and make it fit) as an integer and a string.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public final class EnumDeclaration extends Declaration implements ISimpleDatatypeDeclaration {

    /**
     * A pair of longs class
     *
     * @since 1.1
     */
    public static class Pair {
        private final long fFirst;
        private final long fSecond;

        private Pair(long first, long second) {
            fFirst = first;
            fSecond = second;
        }

        /**
         * @return the first element
         */
        public long getFirst() {
            return fFirst;
        }

        /**
         * @return the second element
         */
        public long getSecond() {
            return fSecond;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Pair other = (Pair) obj;
            return fFirst == other.fFirst && fSecond == other.fSecond;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fFirst, fSecond);
        }
    }

    /**
     * Out of specification {@link Comparator} which returns 0 if the
     * {@link Pair}s overlap even if they are not equal.
     */
    private static final Comparator<Pair> OVERLAP_COMPARATOR = (interval1, interval2) -> {
        if (interval1.fSecond < interval2.fFirst) {
            return -1;
        }
        if (interval1.fFirst > interval2.fSecond) {
            return 1;
        }
        return 0;
    };

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * fEnumTree's key is the Pair of low and high, value is the label. This tree
     * uses an out of specification comparator to make its behavior mimic that of a
     * non overlapping interval tree. The get method will return any value who's key
     * overlaps the queried interval.
     */
    private final @NonNull Map<Pair, String> fEnumTree = new TreeMap<>(OVERLAP_COMPARATOR);
    private final IntegerDeclaration fContainerType;
    private Pair fLastAdded = new Pair(-1, -1);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * constructor
     *
     * @param containerType
     *            the enum is an int, this is the type that the data is
     *            contained in. If you have 1000 possible values, you need at
     *            least a 10 bit enum. If you store 2 values in a 128 bit int,
     *            you are wasting space.
     */
    public EnumDeclaration(IntegerDeclaration containerType) {
        fContainerType = containerType;
    }

    /**
     * Constructor
     *
     * @param containerType
     *            the enum is an int, this is the type that the data is
     *            contained in. If you have 1000 possible values, you need at
     *            least a 10 bit enum. If you store 2 values in a 128 bit int,
     *            you are wasting space.
     * @param enumTree
     *            Existing enum declaration table
     * @since 2.3
     */
    public EnumDeclaration(IntegerDeclaration containerType, Map<Pair, String> enumTree){
        fContainerType = containerType;
        fEnumTree.putAll(enumTree);
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     *
     * @return The container type
     */
    public IntegerDeclaration getContainerType() {
        return fContainerType;
    }

    @Override
    public long getAlignment() {
        return getContainerType().getAlignment();
    }

    @Override
    public int getMaximumSize() {
        return fContainerType.getMaximumSize();
    }

    /**
     * @since 2.0
     */
    @Override
    public boolean isByteOrderSet() {
        return fContainerType.isByteOrderSet();
    }

    /**
     * @since 2.0
     */
    @Override
    public ByteOrder getByteOrder() {
        return fContainerType.getByteOrder();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public EnumDefinition createDefinition(@Nullable IDefinitionScope definitionScope, String fieldName, BitBuffer input) throws CTFException {
        alignRead(input);
        IntegerDefinition value = getContainerType().createDefinition(definitionScope, fieldName, input);
        return new EnumDefinition(this, definitionScope, fieldName, value);
    }

    /**
     * Add a value. Do not overlap, this is <em><strong>not</strong></em> an
     * interval tree.
     *
     * @param low
     *            lowest value that this int can be to have label as a return
     *            string
     * @param high
     *            highest value that this int can be to have label as a return
     *            string
     * @param label
     *            the name of the value.
     * @return was the value be added? true == success
     */
    public boolean add(long low, long high, @Nullable String label) {
        if (high < low) {
            return false;
        }
        Pair key = new Pair(low, high);
        if (!fEnumTree.containsKey(key)) {
            fEnumTree.put(key, label);
            fLastAdded = key;
            return true;
        }
        return false;
    }

    /**
     * Add a value. Do not overlap, this is <em><strong>not</strong></em> an
     * interval tree. This could be seen more as a collection of segments.
     *
     * @param label
     *            the name of the value.
     * @return was the value be added? true == success
     * @since 2.0
     */
    public boolean add(@Nullable String label) {
        return add(fLastAdded.fFirst + 1, fLastAdded.fSecond + 1, label);
    }

    /**
     * Check if the label for a value (enum a{day=0,night=1} would return "day"
     * for query(0)
     *
     * @param value
     *            the value to lookup
     * @return the label of that value, can be null
     */
    public @Nullable String query(long value) {
        return fEnumTree.get(new Pair(value, value));
    }

    /**
     * Get the lookup table
     *
     * @return the lookup table
     * @since 1.1
     * @deprecated use {@link #getLookupTable()} instead
     */
    @Deprecated
    public Map<String, Pair> getEnumTable() {
        return ImmutableBiMap.copyOf(fEnumTree).inverse();
    }

    /**
     * Get a copy of the lookup table.
     *
     * @return a copy of the Enum declaration entry map.
     *
     * @since 2.3
     */
    public Map<Pair, String> getLookupTable() {
        return ImmutableMap.copyOf(fEnumTree);
    }

    /**
     * Gets a set of labels of the enum
     *
     * @return A set of labels of the enum, can be empty but not null
     */
    public Set<String> getLabels() {
        return ImmutableSet.copyOf(fEnumTree.values());
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        StringBuilder sb = new StringBuilder();
        sb.append("[declaration] enum["); //$NON-NLS-1$
        for (String label : fEnumTree.values()) {
            sb.append("label:").append(label).append(' '); //$NON-NLS-1$
        }
        sb.append("type:").append(fContainerType.toString()); //$NON-NLS-1$
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(fContainerType, fEnumTree);
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
        EnumDeclaration other = (EnumDeclaration) obj;
        if (!fContainerType.equals(other.fContainerType)) {
            return false;
        }
        /*
         * Must iterate through the entry sets as the comparator used in the enum tree
         * does not respect the contract
         */
        return Iterables.elementsEqual(fEnumTree.entrySet(), other.fEnumTree.entrySet());
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
        EnumDeclaration other = (EnumDeclaration) obj;
        if (!fContainerType.isBinaryEquivalent(other.fContainerType)) {
            return false;
        }
        /*
         * Must iterate through the entry sets as the comparator used in the enum tree
         * does not respect the contract
         */
        return Iterables.elementsEqual(fEnumTree.entrySet(), other.fEnumTree.entrySet());
    }

}
