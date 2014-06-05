/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for custom XML trace parsers.
 *
 * @author Patrick Tasse
 */
public class CustomXmlParserWizard extends Wizard implements INewWizard {

    CustomXmlParserInputWizardPage inputPage;
    CustomXmlParserOutputWizardPage outputPage;
    private ISelection selection;
    CustomXmlTraceDefinition definition;
    String initialDefinitionName;

    /**
     * Default constructor
     */
    public CustomXmlParserWizard() {
        super();
    }

    /**
     * Constructor
     *
     * @param definition
     *            The trace definition
     */
    public CustomXmlParserWizard(CustomXmlTraceDefinition definition) {
        super();
        this.definition = definition;
        this.initialDefinitionName = definition.definitionName;
    }

    @Override
    public boolean performFinish() {
        CustomXmlTraceDefinition def = outputPage.getDefinition();
        if (definition != null && !initialDefinitionName.equals(def.definitionName)) {
            CustomXmlTraceDefinition.delete(initialDefinitionName);
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
    public void init(IWorkbench workbench, IStructuredSelection sel) {
        this.selection = sel;
    }

}
