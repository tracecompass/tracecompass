/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.swtchart;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swtchart.ISeries;

/**
 * This class is used for storing informations about a point inside a SWT
 * series. Rather than using coordinates, it uses a reference to the series
 * itself and an index in order to decrease selection of multiple points that
 * have the same position.
 * <p>
 * The methods {@link #equals(Object)} and {@link #hashCode()} have been
 * overridden in order to allow two different objects that represent the same
 * selection to look like they are the same. It is useful when storing them
 * inside an hash data structure.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class SwtChartPoint {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final ISeries fSeries;
    private final int fIndex;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor.
     *
     * @param series
     *            The series that owns the point
     * @param index
     *            The index of the point in the series
     */
    public SwtChartPoint(ISeries series, int index) {
        fSeries = series;
        fIndex = index;
    }

    /**
     * Copy contructor.
     *
     * @param selection
     *            The selection to copy
     */
    public SwtChartPoint(SwtChartPoint selection) {
        fSeries = selection.fSeries;
        fIndex = selection.fIndex;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof SwtChartPoint)) {
            return false;
        }

        SwtChartPoint point = (SwtChartPoint) obj;
        return (point.fSeries == fSeries) && (point.fIndex == fIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fSeries, fIndex);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the series who owns the selection.
     *
     * @return The SWT series of the selection
     */
    public ISeries getSeries() {
        return fSeries;
    }

    /**
     * Accessor that returns the index of the selection in the series.
     *
     * @return The index of the selection
     */
    public int getIndex() {
        return fIndex;
    }

}
