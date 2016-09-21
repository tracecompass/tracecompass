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

    private static @Nullable LamiAnalysisReport currentReport;
    private static int secondaryViewId = 1;

    /**
     * Return the current report. Should be accessed by the view currently being
     * built.
     *
     * @return The current report
     */
    public static synchronized @Nullable LamiAnalysisReport getCurrentReport() {
        return currentReport;
    }

    /**
     * Create all the views from a given report
     *
     * @param report
     *            The report to open
     * @throws PartInitException
     *             If there was a problem initializing a view
     */
    public static synchronized void createNewView(LamiAnalysisReport report)
            throws PartInitException {
        currentReport = report;

        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        /*
         * Doing this in two operations here, instead of using
         * IWorkbenchPage.VIEW_ACTIVATE, works around a bug where the contextual
         * menu would get "stuck" until the Project view is defocused and
         * refocused.
         */
        page.showView(LamiReportView.VIEW_ID, String.valueOf(secondaryViewId), IWorkbenchPage.VIEW_VISIBLE);
        page.activate(page.findView(LamiReportView.VIEW_ID));

        secondaryViewId++;

        currentReport = null;
    }

}
