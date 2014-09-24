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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
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
