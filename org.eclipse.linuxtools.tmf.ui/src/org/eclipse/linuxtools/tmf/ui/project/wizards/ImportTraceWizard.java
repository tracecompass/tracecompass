/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;

/**
 * <b><u>ImportTraceWizard</u></b>
 * <p>
 */
public class ImportTraceWizard extends Wizard implements IImportWizard {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    static private final String PLUGIN_ID = TmfUiPlugin.PLUGIN_ID;
    static private final String IMPORT_WIZARD = "ImportTraceWizard"; //$NON-NLS-1$
    static private final String ICON_PATH = "icons/wizban/trace_import_wiz.png"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private IWorkbench fWorkbench;
    private IStructuredSelection fSelection;
    private ImportTraceWizardPage fTraceImportWizardPage;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public ImportTraceWizard() {
        IDialogSettings workbenchSettings = TmfUiPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(IMPORT_WIZARD);
        if (section == null) {
            section = workbenchSettings.addNewSection(IMPORT_WIZARD);
        }
        setDialogSettings(section);
    }

    // ------------------------------------------------------------------------
    // Wizard
    // ------------------------------------------------------------------------

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        fWorkbench = workbench;
        fSelection = selection;

        List<?> selectedResources = IDE.computeSelectedResources(selection);
        if (!selectedResources.isEmpty()) {
            fSelection = new StructuredSelection(selectedResources);
        }

        setWindowTitle(Messages.ImportTraceWizard_DialogTitle);
        setDefaultPageImageDescriptor(TmfUiPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH));
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        fTraceImportWizardPage = new ImportTraceWizardPage(fWorkbench, fSelection);
        addPage(fTraceImportWizardPage);
    }

    @Override
    public boolean performFinish() {
        return fTraceImportWizardPage.finish();
    }

}
