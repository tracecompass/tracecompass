/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;

/**
 * Aspect for LAMI table entries, which normally correspond to one "row"
 * of JSON output.
 *
 * It is not the same as a "Event aspect" used for trace events, but it is
 * heavily inspired from it.
 *
 * @author Alexandre Montplaisir
 * @see org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect
 */
public abstract class LamiTableEntryAspect {

    private final String fName;
    private final @Nullable String fUnits;

    /**
     * Constructor
     *
     * @param name
     *            Aspect name, will be used as column name in the UI
     * @param units
     *            The units of the value in this column
     */
    protected LamiTableEntryAspect(String name, @Nullable String units) {
        fUnits = units;
        fName = name;
    }

    /**
     * Get the label of this aspect.
     *
     * The label is composed of the name followed by the units in parentheses.
     *
     * @return The label
     */
    public String getLabel() {
        if (getUnits() == null) {
            return getName();
        }
        return (getName() + " (" + getUnits() + ')'); //$NON-NLS-1$
    }

    /**
     * Get the name of this aspect
     *
     * @return The name
     */
    public String getName() {
        return fName;
    }

    /**
     * Get the units of this aspect.
     *
     * @return The units
     */
    public @Nullable String getUnits() {
        return fUnits;
    }

    /**
     * Indicate if this aspect is numerical or not. This is used, among other
     * things, to align the text in the table cells.
     *
     * @return If this aspect is numerical or not
     */
    public abstract boolean isContinuous();


    /**
     * Indicate if this aspect represent timestamp or not. This can be used in chart
     * for axis labeling etc.
     * @return  If this aspect represent a timestamp or not
     */
    public abstract boolean isTimeStamp();

    /**
     * Indicate if this aspect represent a time duration or not. This can be used in
     * chart for axis labeling etc.
     * @return  If this aspect represent a time duration or not
     */
    public boolean isTimeDuration() {
        return false;
    }

    /**
     * Resolve this aspect for the given entry.
     *
     * @param entry
     *            The table row
     * @return The string to display for the given cell
     */
    public abstract @Nullable String resolveString(LamiTableEntry entry);

    /**
     * Resolve this aspect double representation for the given entry
     *
     * Returned value does not matter if isNumerical() is false.
     *
     * @param entry
     *            The table row
     * @return The double value for the given cell
     */
    public abstract @Nullable Double resolveDouble(LamiTableEntry entry);

    /**
     * Get the comparator that should be used to compare this entry (or table
     * row) with the other rows in the table. This will be passed on to the
     * table's content provider.
     *
     * @return The entry comparator
     */
    public abstract Comparator<LamiTableEntry> getComparator();

    /**
     * Check if an aspect have the same properties.
     *
     * FIXME:Might want to compare the units if necessary.
     *
     * @param aspect
     *            The aspect to compare to
     * @return If all aspect's properties are equal
     */
    public boolean arePropertiesEqual(LamiTableEntryAspect aspect) {
        boolean timestamp = (this.isTimeStamp() == aspect.isTimeStamp());
        boolean numerical = (this.isContinuous() == aspect.isContinuous());
        return (timestamp && numerical);
    }
}
