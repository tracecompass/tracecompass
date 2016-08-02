/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.swtchart;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class handles the selection of points for a SWT chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class SwtChartSelection {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private Set<SwtChartPoint> fSelection = new HashSet<>();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * This method selects/unselects a point from the selected points.
     *
     * @param target
     *            The targeted point that has been touched
     * @param multiple
     *            Whether to allow multiple points
     * @return {@code true} if the point has been selection, else {@code false}
     */
    public boolean touch(SwtChartPoint target, boolean multiple) {
        boolean selected = false;

        /* Check if the point is already selected. */
        if (fSelection.contains(target)) {
            selected = true;
        }

        /* Update the selected points */
        if (multiple) {
            if (selected) {
                fSelection.remove(target);

                return false;
            }
        } else {
            fSelection.clear();
        }

        /* Add the new point */
        SwtChartPoint point = new SwtChartPoint(target);
        fSelection.add(point);

        return true;
    }

    /**
     * This method adds a point to the set of selected points. It should be used
     * when select/unselect isn't important.
     *
     * @param point
     *            The point to add
     */
    public void add(SwtChartPoint point) {
        fSelection.add(point);
    }

    /**
     * This method clears the set of selected points.
     */
    public void clear() {
        fSelection.clear();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns a read-only set of selected points.
     *
     * @return The set of selection points
     */
    public Set<SwtChartPoint> getPoints() {
        return Collections.unmodifiableSet(fSelection);
    }

}
