/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysisReport;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Factory to instantiate and display new Lami report views.
 *
 * It works by setting a static field, then having the view access it.
 *
 * @author Alexandre Montplaisir
 */
public final class LamiReportViewFactory {

    private LamiReportViewFactory() {
    }

    private static @Nullable LamiResultTable currentTable;
    private static int secondaryViewId = 1;

    /**
     * Return the current result table
     *
     * @return The current result table
     */
    public static @Nullable LamiResultTable getCurrentResultTable() {
        return currentTable;
    }

    /**
     * Create all the views from a given report
     *
     * @param report
     *            The report to open
     * @throws PartInitException
     *             If there was a problem initializing a view
     */
    public static synchronized void createNewViews(LamiAnalysisReport report) throws PartInitException {
        boolean firstView = true;

        for (LamiResultTable table : report.getTables()) {
            currentTable = table;

            int mode = (firstView ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_VISIBLE);

            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            page.showView(LamiReportView.VIEW_ID, String.valueOf(secondaryViewId), mode);
            secondaryViewId++;

            currentTable = null;
            firstView = false;
        }
    }

}
