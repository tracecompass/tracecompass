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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

import com.google.common.collect.ImmutableMap;

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
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final EnumTable fTable = new EnumTable();
    private final IntegerDeclaration fContainerType;
    private final Set<String> fLabels = new HashSet<>();

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
        fLabels.add(label);
        return fTable.add(low, high, label);
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
        fLabels.add(label);
        return fTable.add(label);
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
        return fTable.query(value);
    }

    /**
     * Get the lookup table
     *
     * @return the lookup table
     * @since 1.1
     */
    public Map<String, Pair> getEnumTable() {
        ImmutableMap.Builder<String, Pair> builder = new ImmutableMap.Builder<>();
        for (LabelAndRange range : fTable.ranges) {
            builder.put(range.getLabel(), new Pair(range.low, range.high));
        }
        return builder.build();

    }

    /**
     * Gets a set of labels of the enum
     *
     * @return A set of labels of the enum, can be empty but not null
     */
    public Set<String> getLabels() {
        return Collections.unmodifiableSet(fLabels);
    }

    /*
     * Maps integer range -> string. A simple list for now, but feel free to
     * optimize it. Babeltrace suggests an interval tree.
     */
    private class EnumTable {

        private final List<LabelAndRange> ranges = new LinkedList<>();

        public EnumTable() {
        }

        public synchronized boolean add(@Nullable String label) {
            LabelAndRange lastAdded = ranges.isEmpty() ? new LabelAndRange(-1, -1, "") : ranges.get(ranges.size() - 1); //$NON-NLS-1$
            return add(lastAdded.low + 1, lastAdded.high + 1, label);
        }

        public synchronized boolean add(long low, long high, @Nullable String label) {
            LabelAndRange newRange = new LabelAndRange(low, high, label);

            for (LabelAndRange r : ranges) {
                if (r.intersects(newRange)) {
                    return false;
                }
            }

            ranges.add(newRange);

            return true;
        }

        /**
         * Return the first label that matches a value
         *
         * @param value
         *            the value to query
         * @return the label corresponding to that value
         */
        public synchronized @Nullable String query(long value) {
            for (LabelAndRange r : ranges) {
                if (r.intersects(value)) {
                    return r.getLabel();
                }
            }
            return null;
        }

        @Override
        public synchronized int hashCode() {
            final int prime = 31;
            int result = 1;
            for (LabelAndRange range : ranges) {
                result = prime * result + range.hashCode();
            }
            return result;
        }

        @Override
        public synchronized boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            EnumTable other = (EnumTable) obj;
            if (ranges.size() != other.ranges.size()) {
                return false;
            }
            for (int i = 0; i < ranges.size(); i++) {
                if (!ranges.get(i).equals(other.ranges.get(i))) {
                    return false;
                }
            }
            return true;
        }

    }

    private static class LabelAndRange {

        private final long low, high;
        private final @Nullable String fLabel;

        /**
         * Get the label
         *
         * @return the label
         */
        public @Nullable String getLabel() {
            return fLabel;
        }

        public LabelAndRange(long low, long high, @Nullable String str) {
            this.low = low;
            this.high = high;
            this.fLabel = str;
        }

        public boolean intersects(long i) {
            return (i >= this.low) && (i <= this.high);
        }

        public boolean intersects(LabelAndRange other) {
            return this.intersects(other.low)
                    || this.intersects(other.high);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            final String label = fLabel;
            result = prime * result + ((label == null) ? 0 : label.hashCode());
            result = prime * result + (int) (high ^ (high >>> 32));
            result = prime * result + (int) (low ^ (low >>> 32));
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
            LabelAndRange other = (LabelAndRange) obj;
            final String label = fLabel;
            if (label == null) {
                if (other.fLabel != null) {
                    return false;
                }
            } else if (!label.equals(other.fLabel)) {
                return false;
            }
            if (high != other.high) {
                return false;
            }
            if (low != other.low) {
                return false;
            }
            return true;
        }
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        StringBuilder sb = new StringBuilder();
        sb.append("[declaration] enum["); //$NON-NLS-1$
        for (String label : fLabels) {
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
        for (String label : fLabels) {
            result = prime * result + label.hashCode();
        }
        result = prime * result + fTable.hashCode();
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
        if (fLabels.size() != other.fLabels.size()) {
            return false;
        }
        if (!fLabels.containsAll(other.fLabels)) {
            return false;
        }
        if (!fTable.equals(other.fTable)) {
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
        if (fLabels.size() != other.fLabels.size()) {
            return false;
        }
        if (!fLabels.containsAll(other.fLabels)) {
            return false;
        }
        if (!fTable.equals(other.fTable)) {
            return false;
        }
        return true;
    }

}
