/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * The wizard page for creation of a new TMF tracing project.
 * <p>
 * @version 1.0
 * @author Francois Chouinard

 */
public class NewTmfProjectMainWizardPage extends WizardNewProjectCreationPage {

    /**
     * Constructor
     *
     * @param pageName
     *            The name of this wizard page
     */
    public NewTmfProjectMainWizardPage(String pageName) {
        super(pageName);
    }

}
