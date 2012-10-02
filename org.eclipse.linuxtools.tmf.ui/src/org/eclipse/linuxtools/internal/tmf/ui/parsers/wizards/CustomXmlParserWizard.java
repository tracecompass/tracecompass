package org.eclipse.linuxtools.internal.tmf.ui.parsers.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
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
