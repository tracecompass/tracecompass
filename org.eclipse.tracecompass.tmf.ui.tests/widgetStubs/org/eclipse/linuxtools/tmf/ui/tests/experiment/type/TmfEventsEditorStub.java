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

import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.widgets.Composite;

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
