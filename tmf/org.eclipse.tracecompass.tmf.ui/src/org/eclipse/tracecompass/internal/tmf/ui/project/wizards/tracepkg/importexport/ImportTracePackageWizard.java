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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for importing a trace package
 *
 * @author Marc-Andre Laperle
 */
public class ImportTracePackageWizard extends Wizard implements IImportWizard {

    private static final String STORE_IMPORT_TRACE_PKG_WIZARD = "ImportTracePackageWizard"; //$NON-NLS-1$
    private IStructuredSelection fSelection;
    private ImportTracePackageWizardPage fPage;

    /**
     * Constructs the import trace package wizard
     */
    public ImportTracePackageWizard() {
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings
                .getSection(STORE_IMPORT_TRACE_PKG_WIZARD);
        if (section == null) {
            section = workbenchSettings.addNewSection(STORE_IMPORT_TRACE_PKG_WIZARD);
        }
        setDialogSettings(section);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        fSelection = selection;
        setWindowTitle(Messages.ImportTracePackageWizardPage_Title);
        setNeedsProgressMonitor(true);
    }

    @Override
    public boolean performFinish() {
        return fPage.finish();
    }

    @Override
    public void addPages() {
        super.addPages();
        fPage = new ImportTracePackageWizardPage(fSelection);
        addPage(fPage);
    }
}
