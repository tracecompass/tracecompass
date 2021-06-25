/*******************************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

/**
 * A listener which is notified when a timegraph hides or shows a marker
 * category.
 *
 * @author Patrick Tasse
 * @since 7.1
 */
public interface ITimeGraphMarkerListener {

    /**
     * Notifies that the timegraph has hidden or shown a marker category.
     */
    void markerCategoriesChanged();
}
