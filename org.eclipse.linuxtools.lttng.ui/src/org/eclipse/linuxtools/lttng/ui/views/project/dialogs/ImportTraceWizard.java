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
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;

/**
 * <b><u>ImportTraceWizard</u></b>
 * <p>
 *
 * TODO: Implement me. Please.
 */
@SuppressWarnings("restriction")
public class ImportTraceWizard extends Wizard implements IImportWizard {

    private IWorkbench fWorkbench;
    private IStructuredSelection fSelection;
    private ImportTraceWizardPage fMainPage;

	/**
     * 
     */
    public ImportTraceWizard() {
        IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("LTTngTraceImportWizard");
        if (section == null) {
			section = workbenchSettings.addNewSection("LTTngTraceImportWizard");
		}
        
        setDialogSettings(section);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
	public void addPages() {
        super.addPages();
        fMainPage = new ImportTraceWizardPage(fWorkbench, fSelection);
        addPage(fMainPage);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
        fWorkbench = workbench;
        fSelection = selection;

        List<?> selectedResources = IDE.computeSelectedResources(selection);
        if (!selectedResources.isEmpty()) {
            fSelection = new StructuredSelection(selectedResources);
        }
        
        setWindowTitle(DataTransferMessages.DataTransfer_importTitle);
        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/importdir_wiz.png"));
        setNeedsProgressMonitor(true);
    }
	
    public ImportTraceWizardPage getMainPage() {
		return fMainPage;
	}
	
	/**
	 * performFinish is called after the "finish" button is pressed in the import wizard
	 * If we return "false", the wizard will not close. 
	 * 
	 * We perform here version check on the imported LTTng trace
	 * 
	 */
    @Override
	public boolean performFinish() {
    	
    	if ( fMainPage.getDestination().equals( fMainPage.getInitialContainerString() ) ) {
    		
    		String errMessage[] = { "Error : import destination is wrong." }; 
			errMessage = fMainPage.extendErrorMessage(errMessage, "");
			errMessage = fMainPage.extendErrorMessage(errMessage, "You cannot import your trace directly into the \"" + fMainPage.getInitialContainerString() + "\"");
			errMessage = fMainPage.extendErrorMessage(errMessage, "The trace has to be into a subdirectly, like \"" + fMainPage.getInitialContainerString() + "/MyTrace\"" );
			errMessage = fMainPage.extendErrorMessage(errMessage, "");
			errMessage = fMainPage.extendErrorMessage(errMessage, "Please change the destination folder.");
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
