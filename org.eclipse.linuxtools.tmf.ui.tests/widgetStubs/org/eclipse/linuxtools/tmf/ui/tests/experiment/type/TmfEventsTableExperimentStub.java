/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.experiment.type;

import java.util.Collection;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableList;

/**
 * Event table stub for experiment type unit tests
 *
 * @author Geneviève Bastien
 */
public class TmfEventsTableExperimentStub extends TmfEventsTable {

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    private static final Collection<TmfEventTableColumn> EXPERIMENT_COLUMNS =
            ImmutableList.<TmfEventTableColumn> of(new SourceTraceColumn());

    private static class SourceTraceColumn extends TmfEventTableColumn {

        public SourceTraceColumn() {
            super("Trace");
        }

        @Override
        public String getItemString(ITmfEvent event) {
            String ret = event.getTrace().getName();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterFieldId() {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param cacheSize
     *            The size of the rows cache
     */
    public TmfEventsTableExperimentStub(Composite parent, int cacheSize) {
        super(parent, cacheSize, EXPERIMENT_COLUMNS);
    }
}
