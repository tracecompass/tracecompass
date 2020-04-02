/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.views.timegraph;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.ViewFilterDialog;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;

/**
 * This class implements a time event filter dialog.
 *
 * @author Jean-Christian Kouame
 */
public class TimeEventFilterDialog extends ViewFilterDialog {

    /**
     * Constructor
     *
     * @param parentShell
     *            The parent shell of the dialog
     * @param view
     *            The timegraph this dialog belongs to
     * @param control The timegraph control
     */
    public TimeEventFilterDialog(Shell parentShell, AbstractTimeGraphView view, TimeGraphControl control) {
        super(parentShell, view, control);
    }

}
