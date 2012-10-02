/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

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
    public void treeCollapsed(TimeGraphTreeExpansionEvent event);

    /**
     * Notifies that an entry in the timegraph has been expanded.
     *
     * @param event event object describing details
     */
    public void treeExpanded(TimeGraphTreeExpansionEvent event);
}
