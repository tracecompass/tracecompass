/*******************************************************************************
 * Copyright (c) 2010, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.parsers.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.parsers.CustomParserUtils;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
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
    String initialCategoryName;
    String initialDefinitionName;

    /**
     * Default constructor
     */
    public CustomXmlParserWizard() {
        this(null);
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
        if (definition != null) {
            initialCategoryName = definition.categoryName;
            initialDefinitionName = definition.definitionName;
        }
        setWindowTitle(Messages.CustomXmlParserInputWizardPage_windowTitle);
    }

    @Override
    public boolean performFinish() {
        CustomXmlTraceDefinition def = outputPage.getDefinition();
        if (definition != null) {
            if (!initialCategoryName.equals(def.categoryName) || !initialDefinitionName.equals(def.definitionName)) {
                CustomXmlTraceDefinition.delete(initialCategoryName, initialDefinitionName);
            }
            CustomParserUtils.cleanup(CustomXmlTrace.buildTraceTypeId(initialCategoryName, initialDefinitionName));
        }
        def.save();
        CustomParserUtils.cleanup(CustomTxtTrace.buildTraceTypeId(def.categoryName, def.definitionName));
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
