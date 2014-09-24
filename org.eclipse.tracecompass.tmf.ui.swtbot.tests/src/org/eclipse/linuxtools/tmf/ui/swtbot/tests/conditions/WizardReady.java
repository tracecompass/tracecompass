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

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

/**
 * Is a given wizard ready?
 *
 * @author Matthew Khouzam
 */
class WizardReady implements ICondition {

    private final Wizard fWizard;

    public WizardReady(Wizard wizard) {
        fWizard = wizard;
    }

    @Override
    public boolean test() throws Exception {
        if (fWizard.getShell() == null) {
            return false;
        }
        return true;
    }

    @Override
    public void init(SWTBot bot) {
    }

    @Override
    public String getFailureMessage() {
        return null;
    }

}
