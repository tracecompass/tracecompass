package org.eclipse.linuxtools.internal.tmf.ui.parsers.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTraceDefinition;
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
        return true;
    }

    /**
     * Adding the page to the wizard.
     */

    @Override
	public void addPages() {
        inputPage = new CustomTxtParserInputWizardPage(selection, definition);
        addPage(inputPage);
        outputPage = new CustomTxtParserOutputWizardPage(this);
        addPage(outputPage);
    }

	@Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

}
