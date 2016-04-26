/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysisReport;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views.LamiReportViewFactory;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.IOnDemandAnalysisReport;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfReportElement;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The command handler for the "Open Report" menu option for Report project
 * model elements.
 *
 * Double-clicking should also call this handler.
 *
 * @author Alexandre Montplaisir
 */
public class OpenReportHandler extends AbstractHandler {

    @Override
    public @Nullable Object execute(@Nullable ExecutionEvent event) throws ExecutionException {

        /* Types should have been checked by the plugin.xml already */
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        List<?> elements = ((IStructuredSelection) selection).toList();
        List<TmfReportElement> reportElements = elements.stream()
            .filter(elem -> elem instanceof TmfReportElement)
            .map(elem -> (TmfReportElement) elem)
            .collect(Collectors.toList());

        for (TmfReportElement reportElem : reportElements) {
            IOnDemandAnalysisReport report = reportElem.getReport();
            if (!(report instanceof LamiAnalysisReport)) {
                /* This handler deals with LAMI reports only */
                continue;
            }
            LamiAnalysisReport lamiReport = (LamiAnalysisReport) report;

            Display.getDefault().syncExec(() -> {
                try {
                    LamiReportViewFactory.createNewViews(lamiReport);
                } catch (PartInitException e) {
                }
            });
        }

        return null;
    }
}
