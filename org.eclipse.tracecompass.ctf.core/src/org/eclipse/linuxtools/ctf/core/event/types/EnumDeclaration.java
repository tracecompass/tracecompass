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

package org.eclipse.linuxtools.ctf.core.event.types;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

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

    /**
     * @since 3.0
     */
    @Override
    public int getMaximumSize() {
        return fContainerType.getMaximumSize();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public EnumDefinition createDefinition(IDefinitionScope definitionScope, String fieldName, BitBuffer input) throws CTFReaderException {
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
    public boolean add(long low, long high, String label) {
        fLabels.add(label);
        return fTable.add(low, high, label);
    }

    /**
     * Check if the label for a value (enum a{day=0,night=1} would return "day"
     * for query(0)
     *
     * @param value
     *            the value to lookup
     * @return the label of that value, can be null
     */
    public String query(long value) {
        return fTable.query(value);
    }

    /**
     * Gets a set of labels of the enum
     *
     * @return A set of labels of the enum, can be empty but not null
     * @since 3.0
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

        public boolean add(long low, long high, String label) {
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
        public String query(long value) {
            for (LabelAndRange r : ranges) {
                if (r.intersects(value)) {
                    return r.getLabel();
                }
            }
            return null;
        }

    }

    private static class LabelAndRange {

        private final long low, high;
        private final String fLabel;

        /**
         * Get the label
         *
         * @return the label
         */
        public String getLabel() {
            return fLabel;
        }

        public LabelAndRange(long low, long high, String str) {
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
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] enum[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
