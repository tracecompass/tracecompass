/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Top Level Preference Page for Tracing.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class TmfTracingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        // No content yet!
        return composite;
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to do yet!
    }
}
