/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

/**
 * A listener which is notified when a timegraph expands or collapses an entry.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public interface ITimeGraphTreeListener {

    /**
     * Notifies that an entry in the timegraph has been collapsed.
     *
     * @param event event object describing details
     */
    void treeCollapsed(TimeGraphTreeExpansionEvent event);

    /**
     * Notifies that an entry in the timegraph has been expanded.
     *
     * @param event event object describing details
     */
    void treeExpanded(TimeGraphTreeExpansionEvent event);
}
