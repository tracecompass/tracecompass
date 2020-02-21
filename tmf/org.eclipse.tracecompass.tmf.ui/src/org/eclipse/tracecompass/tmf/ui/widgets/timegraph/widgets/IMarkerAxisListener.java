/*******************************************************************************
 * Copyright (c) 2016 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets;

/**
 * Interface for marker axis call-backs.
 * @since 2.0
 */
public interface IMarkerAxisListener {

    /**
     * Set the visibility of a marker category.
     *
     * @param category
     *            the marker category
     * @param visible
     *            true if the marker category should be visible, false otherwise
     */
    void setMarkerCategoryVisible(String category, boolean visible);
}
