/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;

/**
 * Text control for selection end time
 */
public class HistogramSelectionEndControl extends HistogramCurrentTimeControl {

    /**
     * Standard constructor
     *
     * @param parentView A parent histogram view
     * @param parent A parent composite to draw in
     * @param label A label
     * @param value A value
     */
    public HistogramSelectionEndControl(HistogramView parentView, Composite parent, String label, long value) {
        super(parentView, parent, label, value);
    }

    @Override
    protected void updateSelectionTime(long time) {
        long begin = Math.min(fParentView.getSelectionBegin(), time);
        long end = Math.max(fParentView.getSelectionBegin(), time);
        fParentView.updateSelectionTime(begin, end);
    }

}
