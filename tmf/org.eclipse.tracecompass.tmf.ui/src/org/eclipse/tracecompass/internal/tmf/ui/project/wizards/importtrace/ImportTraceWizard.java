/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The import trace wizard implementation.
 * <p>
 * @author Francois Chouinard
 */
public class ImportTraceWizard extends Wizard implements IImportWizard {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String PLUGIN_ID = Activator.PLUGIN_ID;
    private static final String IMPORT_WIZARD = "ImportTraceWizard"; //$NON-NLS-1$
    private static final String ICON_PATH = "icons/wizban/trace_import_wiz.png"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private IStructuredSelection fSelection;
    private ImportTraceWizardPage fTraceImportWizardPage;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public ImportTraceWizard() {
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
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
        fSelection = selection;

        setWindowTitle(Messages.ImportTraceWizard_DialogTitle);
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH));
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        fTraceImportWizardPage = new ImportTraceWizardPage(fSelection);
        addPage(fTraceImportWizardPage);
    }

    @Override
    public boolean performFinish() {
        return fTraceImportWizardPage.finish();
    }

}
