/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

/**
 * Is the wizard on the page you want?
 * @author Matthew Khouzam
 */
class WizardOnPage implements ICondition {

    private final Wizard fWizard;
    private final IWizardPage fPage;

    public WizardOnPage(Wizard wizard, IWizardPage desiredPage) {
        fWizard = wizard;
        fPage = desiredPage;
    }

    @Override
    public boolean test() throws Exception {
        if (fWizard == null || fPage == null) {
            return false;
        }
        final IWizardContainer container = fWizard.getContainer();
        if (container == null) {
            return false;
        }
        IWizardPage currentPage = container.getCurrentPage();
        return fPage.equals(currentPage);
    }

    @Override
    public void init(SWTBot bot) {
    }

    @Override
    public String getFailureMessage() {
        return null;
    }

}
