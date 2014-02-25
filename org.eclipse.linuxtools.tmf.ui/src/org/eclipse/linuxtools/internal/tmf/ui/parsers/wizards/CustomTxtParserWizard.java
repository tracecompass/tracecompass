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
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for custom text trace parsers.
 *
 * @author Patrick Tasse
 */
public class CustomTxtParserWizard extends Wizard implements INewWizard {

    CustomTxtParserInputWizardPage inputPage;
    CustomTxtParserOutputWizardPage outputPage;
    private ISelection selection;
    CustomTxtTraceDefinition definition;

    /**
     * Default constructor
     */
    public CustomTxtParserWizard() {
        super();
    }

    /**
     * Constructor
     *
     * @param definition
     *            The trace definition
     */
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

    @Override
    public void addPages() {
        inputPage = new CustomTxtParserInputWizardPage(selection, definition);
        addPage(inputPage);
        outputPage = new CustomTxtParserOutputWizardPage(this);
        addPage(outputPage);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection sel) {
        this.selection = sel;
    }

}
