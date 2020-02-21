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

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.signal;

import java.util.Set;

import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;

import com.google.common.collect.ImmutableSet;

/**
 * This signal is used for updating the analysis that created the chart that
 * objects has been selected in the chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartSelectionUpdateSignal extends TmfSignal {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final IDataChartProvider<?> fDataProvider;
    // FIXME: The Object should be the same type as the ? above. See if we can
    // enforce it or not, maybe by making this class generic?
    private final Set<Object> fSelectedObject;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param source
     *            The object sending this signal
     * @param provider
     *            The data provider that owns the signal
     * @param selection
     *            The set of selected objects
     */
    public ChartSelectionUpdateSignal(Object source, IDataChartProvider<?> provider, Set<Object> selection) {
        super(source);

        fDataProvider = provider;
        fSelectedObject = selection;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the {@link IDataChartProvider} of the signal. It
     * differs from the source because it can be used for linking multiple
     * charts of the same provider together. For example, a chart and a table
     * can send signals to each other. They are two different sources, but both
     * of them represent data from the same provider.
     *
     * @return The data provider that owns the signal
     */
    public IDataChartProvider<?> getDataProvider() {
        return fDataProvider;
    }

    /**
     * Accessors that returns the set of selected objects.
     *
     * @return The set of selected objects
     */
    public Set<Object> getSelectedObject() {
        return ImmutableSet.copyOf(fSelectedObject);
    }

}
