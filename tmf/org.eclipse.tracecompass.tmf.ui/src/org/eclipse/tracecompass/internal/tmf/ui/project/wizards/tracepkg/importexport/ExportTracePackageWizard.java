/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for exporting a trace package
 *
 * @author Marc-Andre Laperle
 */
public class ExportTracePackageWizard extends Wizard implements IExportWizard {

    private static final String STORE_EXPORT_TRACE_WIZARD = "ExportTraceWizard"; //$NON-NLS-1$
    private IStructuredSelection fSelection;
    private List<TmfCommonProjectElement> fSelectedTraces;
    private ExportTracePackageWizardPage fPage;

    /**
     * Constructor for the export trace wizard
     */
    public ExportTracePackageWizard() {
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings
                .getSection(STORE_EXPORT_TRACE_WIZARD);
        if (section == null) {
            section = workbenchSettings.addNewSection(STORE_EXPORT_TRACE_WIZARD);
        }
        setDialogSettings(section);
        fSelectedTraces = new ArrayList<>();
    }

    /**
     * Constructor for the export trace wizard with known selected traces
     *
     * @param selectedTraces
     *            the selected traces
     */
    public ExportTracePackageWizard(List<TmfTraceElement> selectedTraces) {
        this();
        fSelectedTraces = new ArrayList<>(selectedTraces);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        fSelection = selection;
        for (Object item : selection.toArray()) {
            if (item instanceof TmfExperimentElement) {
                TmfExperimentElement experimentElement = (TmfExperimentElement) item;
                fSelectedTraces.add(experimentElement);
                for (TmfTraceElement expTrace : experimentElement.getTraces()) {
                    TmfTraceElement trace = expTrace.getElementUnderTraceFolder();
                    if (!fSelectedTraces.contains(trace)) {
                        fSelectedTraces.add(trace);
                    }
                }
            }
        }
        setWindowTitle(Messages.ExportTracePackageWizardPage_Title);
        setNeedsProgressMonitor(true);
    }

    @Override
    public boolean performFinish() {
        return fPage.finish();
    }

    @Override
    public void addPages() {
        super.addPages();
        fPage = new ExportTracePackageWizardPage(fSelection, fSelectedTraces);
        if (fSelectedTraces.isEmpty()) {
            addPage(new ExportTracePackageSelectTraceWizardPage());
        }
        addPage(fPage);
    }
}
