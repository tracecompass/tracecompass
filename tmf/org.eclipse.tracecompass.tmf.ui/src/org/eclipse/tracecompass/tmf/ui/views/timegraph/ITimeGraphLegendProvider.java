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

package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;

/**
 * Legend provider
 *
 * @author Matthew Khouzam
 * @since 3.3
 */
public interface ITimeGraphLegendProvider {

    /**
     * Show a legend,
     *
     * @param shell
     *            the shell to draw in
     * @param presentationProvider
     *            the presentation provider that gives the states to draw.
     */
    void showLegend(Shell shell, ITimeGraphPresentationProvider presentationProvider);
}
