/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.experiment.type;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventsTable;

/**
 * Event editor stub for experiment type unit tests
 *
 * @author Geneviève Bastien
 */
public class TmfEventsEditorStub extends TmfEventsEditor {

    private Composite fParent;

    @Override
    public void createPartControl(final Composite parent) {
        super.createPartControl(parent);
        fParent = parent;
    }

    /**
     * Get a new event table, because the one from the parent events editor is
     * not available.
     *
     * This function is meant to be used for unit tests only
     *
     * @return A new event table
     */
    public TmfEventsTable getNewEventsTable() {
        TmfEventsTable table = createEventsTable(fParent, getTrace().getCacheSize());
        return table;
    }

}
