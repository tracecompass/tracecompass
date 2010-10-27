package org.eclipse.linuxtools.tmf.ui.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class CustomXmlParserWizard extends Wizard implements INewWizard {

    CustomXmlParserInputWizardPage inputPage;
    CustomXmlParserOutputWizardPage outputPage;
    private ISelection selection;
    CustomXmlTraceDefinition definition;

    public CustomXmlParserWizard() {
        super();
    }

    public CustomXmlParserWizard(CustomXmlTraceDefinition definition) {
        super();
        this.definition = definition;
    }

    @Override
    public boolean performFinish() {
        CustomXmlTraceDefinition def = outputPage.getDefinition();
        if (definition != null && !definition.definitionName.equals(def.definitionName)) {
            CustomXmlTraceDefinition.delete(definition.definitionName);
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

    @Override
	public void addPages() {
        inputPage = new CustomXmlParserInputWizardPage(selection, definition);
        addPage(inputPage);
        outputPage = new CustomXmlParserOutputWizardPage(this);
        addPage(outputPage);
    }

    @Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

}
