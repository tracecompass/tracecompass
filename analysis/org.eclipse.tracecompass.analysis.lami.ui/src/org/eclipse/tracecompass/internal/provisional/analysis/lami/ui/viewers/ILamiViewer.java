/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views.LamiReportViewTabPage;

/**
 * Common interface for all Lami viewers.
 *
 * @author Alexandre Montplaisir
 */
public interface ILamiViewer {

    /**
     * Dispose the viewer widget.
     */
    void dispose();

    /**
     * Factory method to create a new Table viewer.
     *
     * @param parent
     *            The parent composite
     * @param page
     *            The {@link LamiReportViewTabPage} parent page
     * @return The new viewer
     */
    static ILamiViewer createLamiTable(Composite parent, LamiReportViewTabPage page) {
        TableViewer tableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
        return new LamiTableViewer(tableViewer, page);
    }

}
