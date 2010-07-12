package org.eclipse.linuxtools.tmf.ui.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class CustomTxtParserWizard extends Wizard implements INewWizard {

    CustomTxtParserInputWizardPage inputPage;
    CustomTxtParserOutputWizardPage outputPage;
    private ISelection selection;
    CustomTxtTraceDefinition definition;

    public CustomTxtParserWizard() {
        super();
    }

    public CustomTxtParserWizard(CustomTxtTraceDefinition definition) {
        super();
        this.definition = definition;
    }

    @Override
    public boolean performFinish() {
        CustomTxtTraceDefinition def = outputPage.getDefinition();
        if (definition != null && !definition.definitionName.equals(def.definitionName)) {
            CustomTxtTraceDefinition.delete(definition.definitionName);
        }
        def.save();
        /*
        if (this.selection instanceof IStructuredSelection) {
            Object selection = ((IStructuredSelection)this.selection).getFirstElement();
            if (selection instanceof IFile) {
                IFile file = (IFile)selection;
                IEditorInput editorInput = new FileEditorInput(file);
                IWorkbench wb = PlatformUI.getWorkbench();
                IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
        
                IEditorPart editor = activePage.findEditor(editorInput);
                if (editor != null && editor instanceof GenericTableEditor) {
                    activePage.reuseEditor((IReusableEditor)editor, editorInput);
                    activePage.activate(editor);
                } else {
                    try {
                        editor = activePage.openEditor(editorInput, GenericTableEditor.ID);
                    } catch (PartInitException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        */
        return true;
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {
        inputPage = new CustomTxtParserInputWizardPage(selection, definition);
        addPage(inputPage);
        outputPage = new CustomTxtParserOutputWizardPage(this);
        addPage(outputPage);
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

}
