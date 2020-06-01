/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.model.IStylePresentationProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;

/**
 * Extension of the style presentation provider for the time graph model.
 *
 * @author Geneviève Bastien
 */
public interface ITimeGraphStylePresentationProvider extends IStylePresentationProvider {

    /**
     * Get the element style for the specified event.
     *
     * @param event
     *            the event
     * @return the element style, or null to ignore event
     */
    @Nullable OutputElementStyle getElementStyle(ITimeEvent event);

}
