/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.handler;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysis;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysisReport;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views.LamiReportViewFactory;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.IOnDemandAnalysis;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.IOnDemandAnalysisReport;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOnDemandAnalysisElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfReportsElement;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The command handler for the "Run External Analysis" menu option.
 *
 * @author Alexandre Montplaisir
 */
public class RunAnalysisHandler extends AbstractHandler {

    @Override
    public boolean isEnabled() {
        final Object element = HandlerUtils.getSelectedModelElement();
        if (element == null) {
            return false;
        }

        /*
         * plugin.xml should have done type verifications already
         */
        TmfOnDemandAnalysisElement elem = (TmfOnDemandAnalysisElement) element;
        return (elem.getAnalysis() instanceof LamiAnalysis && elem.canRun());
    }

    @Override
    public @Nullable Object execute(@Nullable ExecutionEvent event) throws ExecutionException {

        /* Types should have been checked by the plugin.xml already */
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        Object element = ((IStructuredSelection) selection).getFirstElement();
        final TmfOnDemandAnalysisElement analysisElem = (TmfOnDemandAnalysisElement) element;

        TmfCommonProjectElement traceElem = analysisElem.getParent().getParent();
        ITmfTrace trace = traceElem.getTrace();
        if (trace == null) {
            /* That trace is not currently opened */
            return null;
        }

        /* Retrieve and initialize the analysis module, aka read the script's metadata */
        IOnDemandAnalysis ondemandAnalysis = analysisElem.getAnalysis();
        if (!(ondemandAnalysis instanceof LamiAnalysis)) {
            return null;
        }
        LamiAnalysis analysis = (LamiAnalysis) ondemandAnalysis;

        /* Retrieve the current time range, will be used as parameters to the analysis */
        TmfTraceManager tm = TmfTraceManager.getInstance();
        TmfTimeRange timeRange = tm.getCurrentTraceContext().getSelectionRange();
        if (timeRange.getStartTime().equals(timeRange.getEndTime())) {
            timeRange = null;
        }
        /* Job below needs a final reference... */
        final TmfTimeRange tr = timeRange;

        /* Pop the dialog to ask for extra parameters */
        String baseCommand = analysis.getFullCommandAsString(trace, tr);

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        ParameterDialog dialog = new ParameterDialog(shell, Messages.ParameterDialog_ExternalParameters,
                Messages.ParameterDialog_ExternalParametersDescription,
                baseCommand, null);
        if (dialog.open() != Window.OK) {
            /* User clicked Cancel, don't run */
            return null;
        }
        String extraParams = nullToEmptyString(dialog.getValue());

        /* Execute the analysis and produce the reports */
        Job job = new Job(Messages.LamiAnalysis_MainTaskName) {
            @Override
            protected @Nullable IStatus run(@Nullable IProgressMonitor monitor) {
                IProgressMonitor mon = (monitor == null ? new NullProgressMonitor() : monitor);
                try {
                    List<LamiResultTable> results = analysis.execute(trace, tr, extraParams, mon);

                    String reportName = analysis.getName() +' ' + Messages.ParameterDialog_ReportNameSuffix;
                    LamiAnalysisReport report = new LamiAnalysisReport(reportName, results);
                    registerNewReport(analysisElem, report);

                    /* Automatically open the report for convenience */
                    Display.getDefault().syncExec(() -> {
                        try {
                            LamiReportViewFactory.createNewView(report);
                        } catch (PartInitException e) {
                        }
                    });
                    return Status.OK_STATUS;

                } catch (CoreException e) {
                    /*
                     * The analysis execution did not complete normally, we will
                     * report it to the user.
                     */
                    IStatus status = e.getStatus();

                    /* Don't display a dialog if it was simply cancelled by the user */
                    if (status.matches(IStatus.CANCEL)) {
                        return status;
                    }

                    String dialogTitle;
                    String dialogMessage;
                    if (status.matches(IStatus.ERROR)) {
                        dialogTitle = Messages.ErrorDialog_Error;
                        dialogMessage = Messages.ErrorDialog_ErrorMessage;
                    } else {
                        dialogTitle = Messages.ErrorDialog_Info;
                        dialogMessage = Messages.ErrorDialog_InfoMessage;
                    }

                    Display.getDefault().asyncExec(() -> {
                        ErrorDialog.openError(shell, dialogTitle, dialogMessage, status);
                    });

                    /*
                     * We showed our own error message, no need for the Job to
                     * show another one.
                     */
                    return Status.OK_STATUS;
                }
            }
        };
        job.schedule();

        return null;
    }

    /**
     * Register a new report
     *
     * @param analysisElem
     *            The analysis's project element
     * @param report
     *            The report to add
     */
    public void registerNewReport(TmfOnDemandAnalysisElement analysisElem, IOnDemandAnalysisReport report) {
        /* For now the TmfProjectReportsElement manages the reports. */
        TmfReportsElement reportsElement = analysisElem
                .getParent()
                .getParent()
                .getChildElementReports();

        reportsElement.addReport(report);
    }

}
