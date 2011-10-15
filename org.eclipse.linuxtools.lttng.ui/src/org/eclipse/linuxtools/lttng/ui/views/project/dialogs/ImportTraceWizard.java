/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.project.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * <b><u>ImportTraceWizard</u></b>
 * <p>
 * 
 * TODO: Implement me. Please.
 */
@Deprecated
@SuppressWarnings("restriction")
public class ImportTraceWizard extends Wizard implements IImportWizard {

    private IWorkbench fWorkbench;
    private IStructuredSelection fSelection;
    private ImportTraceWizardPage fMainPage;

    /**
     * 
     */
    private final String IMPORT_WIZARD = "LTTngTraceImportWizard"; //$NON-NLS-1$

    public ImportTraceWizard() {
	IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	IDialogSettings section = workbenchSettings.getSection(IMPORT_WIZARD);
	if (section == null) {
	    section = workbenchSettings.addNewSection(IMPORT_WIZARD);
	}

	setDialogSettings(section);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
	super.addPages();
	fMainPage = new ImportTraceWizardPage(fWorkbench, fSelection);
	addPage(fMainPage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void init(IWorkbench workbench, IStructuredSelection selection) {
	fWorkbench = workbench;
	fSelection = selection;

	List selectedResources = IDE.computeSelectedResources(selection);
	if (!selectedResources.isEmpty()) {
	    fSelection = new StructuredSelection(selectedResources);
	}

	setWindowTitle(""); //$NON-NLS-1$
	setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/importdir_wiz.png")); //$NON-NLS-1$
	setNeedsProgressMonitor(true);
    }

    public ImportTraceWizardPage getMainPage() {
	return fMainPage;
    }

    /**
     * performFinish is called after the "finish" button is pressed in the
     * import wizard If we return "false", the wizard will not close.
     * 
     * We perform here version check on the imported LTTng trace
     * 
     */
    @Override
    public boolean performFinish() {
	if (fMainPage.getDestination().equals(fMainPage.getInitialContainerString())) {
	    String errMessage[] = { org.eclipse.linuxtools.lttng.ui.views.project.dialogs.DataTransferMessages.ImportTraceWizard_LocationError };
	    errMessage = fMainPage.extendErrorMessage(errMessage, ""); //$NON-NLS-1$
	    errMessage = fMainPage.extendErrorMessage(errMessage, org.eclipse.linuxtools.lttng.ui.views.project.dialogs.DataTransferMessages.ImportTraceWizard_LocationErrorMsg1 + fMainPage.getInitialContainerString() + "\""); //$NON-NLS-1$
	    errMessage = fMainPage.extendErrorMessage(errMessage, org.eclipse.linuxtools.lttng.ui.views.project.dialogs.DataTransferMessages.ImportTraceWizard_LocationErrorMsg2 + fMainPage.getInitialContainerString() + "/MyTrace\""); //$NON-NLS-1$
	    errMessage = fMainPage.extendErrorMessage(errMessage, ""); //$NON-NLS-1$
	    errMessage = fMainPage.extendErrorMessage(errMessage, org.eclipse.linuxtools.lttng.ui.views.project.dialogs.DataTransferMessages.ImportTraceWizard_LocationErrorMsg3);
	    fMainPage.showVersionErrorPopup(errMessage);
	    return false;
	}
	return fMainPage.finish();
    }

    @Override
    public boolean canFinish() {
	return fMainPage.isSelectedElementsValidLttngTraces();
    }

}
