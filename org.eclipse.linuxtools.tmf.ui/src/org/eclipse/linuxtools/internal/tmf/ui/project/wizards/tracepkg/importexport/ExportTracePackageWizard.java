/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
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
    private List<TmfTraceElement> fSelectedTraces;
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
        fSelectedTraces = new ArrayList<TmfTraceElement>();
    }

    /**
     * Constructor for the export trace wizard with known selected traces
     *
     * @param selectedTraces
     *            the selected traces
     */
    public ExportTracePackageWizard(List<TmfTraceElement> selectedTraces) {
        this();
        fSelectedTraces = selectedTraces;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        fSelection = selection;

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
