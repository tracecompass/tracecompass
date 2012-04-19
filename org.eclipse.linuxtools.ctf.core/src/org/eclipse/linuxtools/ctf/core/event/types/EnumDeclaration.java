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
 * <b><u>EnumDeclaration</u></b>
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

    public EnumDeclaration(IntegerDeclaration containerType) {
        this.containerType = containerType;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

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

    public boolean add(long low, long high, String label) {
        return table.add(low, high, label);
    }

    public String query(long value) {
        return table.query(value);
    }

    public String getLabel(long i) {
        return table.getLabel(i);
    }

    /*
     * Maps integer range -> string. A simple list for now, but feel free to
     * optimize it. Babeltrace suggests an interval tree.
     */
    static private class EnumTable {

        List<Range> ranges = new LinkedList<Range>();

        public EnumTable() {
        }

        public String getLabel(long i) {
            for (Range r : ranges) {
                if (r.intersects(i)) {
                    return r.str;
                }
            }
            return null;
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

        public String query(long value) {
            for (Range r : ranges) {
                if (r.intersects(value)) {
                    return r.str;
                }
            }

            return null;
        }

        static private class Range {

            long low, high;
            String str;

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
