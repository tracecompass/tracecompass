/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
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

import java.util.LinkedList;
import java.util.List;

/**
 * A CTF enum declaration.
 *
 * The definition of a enum point basic data type. It will take the data
 * from a trace and store it (and make it fit) as an integer and a string.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class EnumDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final EnumTable table = new EnumTable();
    private IntegerDeclaration containerType = null;

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
        this.containerType = containerType;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     *
     * @return The container type
     */
    public IntegerDeclaration getContainerType() {
        return containerType;
    }

    @Override
    public long getAlignment() {
        return this.getContainerType().getAlignment();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public EnumDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        return new EnumDefinition(this, definitionScope, fieldName);
    }

    /**
     * Add a value. Do not overlap, this is <i><u><b>not</i></u></b> an interval tree.
     * @param low lowest value that this int can be to have label as a return string
     * @param high highest value that this int can be to have label as a return string
     * @param label the name of the value.
     * @return was the value be added? true == success
     */
    public boolean add(long low, long high, String label) {
        return table.add(low, high, label);
    }

    /**
     * Check if the label for a value (enum a{day=0,night=1} would return "day" for query(0)
     * @param value the value to lookup
     * @return the label of that value, can be null
     */
    public String query(long value) {
        return table.query(value);
    }

    /*
     * Maps integer range -> string. A simple list for now, but feel free to
     * optimize it. Babeltrace suggests an interval tree.
     */
    private static class EnumTable {

        private List<Range> ranges = new LinkedList<Range>();

        public EnumTable() {
        }

        public boolean add(long low, long high, String label) {
            Range newRange = new Range(low, high, label);

            for (Range r : ranges) {
                if (r.intersects(newRange)) {
                    return false;
                }
            }

            ranges.add(newRange);

            return true;
        }

        /**
         * Return the first label that matches a value
         * @param value the value to query
         * @return the label corresponding to that value
         */
        public String query(long value) {
            for (Range r : ranges) {
                if (r.intersects(value)) {
                    return r.str;
                }
            }
            return null;
        }

        private static class Range {

            private long low, high;
            private String str;

            public Range(long low, long high, String str) {
                this.low = low;
                this.high = high;
                this.str = str;
            }

            public boolean intersects(long i) {
                return (i >= this.low) && (i <= this.high);
            }

            public boolean intersects(Range other) {
                return this.intersects(other.low)
                        || this.intersects(other.high);
            }
        }
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] enum[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
