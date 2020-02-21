/*******************************************************************************
 * Copyright (c) 2015 Keba AG
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian Mansky - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * Interface containing information for an additional view-specific active
 * dependent button to be used in TimeGraphFilterDialog.
 *
 * @since 1.0
 */
public interface ITimeGraphEntryActiveProvider {

    /**
     * @return Name of the button label.
     */
    String getLabel();

    /**
     * @return Tooltip of the button.
     */
    String getTooltip();

    /**
     * @param entry
     *            An Element in the TimeGraphFilterDialog to check against
     *            selecting/ticking
     * @return True if this element is active.
     */
    boolean isActive(ITimeGraphEntry entry);

}
