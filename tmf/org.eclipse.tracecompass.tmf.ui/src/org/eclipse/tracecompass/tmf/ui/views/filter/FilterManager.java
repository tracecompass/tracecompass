/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Ensure backwards compatibility to Linux Tools
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.filter;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;

/**
 * Central filter manager
 *
 * @version 1.0
 * @author Patrick Tasse
 *
 * @deprecated Use
 *             {@link org.eclipse.tracecompass.tmf.core.filter.FilterManager}
 *             instead
 */
@Deprecated
public class FilterManager {
    /**
     * Retrieve the currently saved filters
     *
     * @return The array of filters
     */
    public static @NonNull ITmfFilterTreeNode[] getSavedFilters() {
        return org.eclipse.tracecompass.tmf.core.filter.FilterManager.getSavedFilters();
    }

    /**
     * Set the passed filters as the currently saved ones.
     *
     * @param filters
     *            The filters to save
     */
    public static void setSavedFilters(ITmfFilterTreeNode[] filters) {
        org.eclipse.tracecompass.tmf.core.filter.FilterManager.setSavedFilters(filters);
    }
}
