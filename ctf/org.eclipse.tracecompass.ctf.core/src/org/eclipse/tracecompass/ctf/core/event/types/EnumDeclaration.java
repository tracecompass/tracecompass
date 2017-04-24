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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;

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

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * fEnumTree's key is the Pair of low and high, value is the label.
     */
    private final TreeMap<Pair, String> fEnumTree = new TreeMap<>(Comparator.comparingLong(Pair::getFirst).thenComparingLong(Pair::getSecond));
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
        return this.getContainerType().getAlignment();
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
        /**
         * Iterate over a collection of Entries, such that: entry.low <=
         * high, sorted by decreasing low.
         */
        Set<Entry<Pair, String>> descendingSet = fEnumTree.descendingMap().tailMap(new Pair(high, Long.MAX_VALUE), true).entrySet();
        for (Entry<Pair, String> entry : descendingSet) {
            if (entry.getKey().fSecond >= low) {
                /* if an entry overlaps */
                return false;
            }
            /*
             * No more entries can overlap as sorted by decreasing low and high.
             */
            break;
        }
        fLastAdded = new Pair(low, high);
        fEnumTree.put(fLastAdded, label);
        return true;
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
        /*
         * Find an entry with the highest fLow <= value, and check that it
         * intersects value.
         */
        Entry<Pair, String> floorEntry = fEnumTree.floorEntry(new Pair(value, Long.MAX_VALUE));
        if (floorEntry != null && floorEntry.getKey().fSecond >= value) {
            return floorEntry.getValue();
        }
        return null;
    }

    /**
     * Get the lookup table
     *
     * @return the lookup table
     * @since 1.1
     */
    public Map<String, Pair> getEnumTable() {
        return ImmutableBiMap.copyOf(fEnumTree).inverse();
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
        final int prime = 31;
        int result = prime + fContainerType.hashCode();
        for (String label : fEnumTree.values()) {
            result = prime * result + label.hashCode();
        }
        result = prime * result + fEnumTree.hashCode();
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
        EnumDeclaration other = (EnumDeclaration) obj;
        if (!fContainerType.equals(other.fContainerType)) {
            return false;
        }
        if (!fEnumTree.equals(other.fEnumTree)) {
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
        EnumDeclaration other = (EnumDeclaration) obj;
        if (!fContainerType.isBinaryEquivalent(other.fContainerType)) {
            return false;
        }
        if (!fEnumTree.equals(other.fEnumTree)) {
            return false;
        }
        return true;
    }

}
