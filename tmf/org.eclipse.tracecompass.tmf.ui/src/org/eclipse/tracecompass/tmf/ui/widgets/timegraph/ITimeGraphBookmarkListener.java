/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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

import java.util.EventListener;

/**
 * A listener which is notified when a timegraph adds or removes a bookmark.
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public interface ITimeGraphBookmarkListener extends EventListener {

    /**
     * Notifies that the timegraph has added a bookmark.
     *
     * @param event event object describing details
     */
    void bookmarkAdded(TimeGraphBookmarkEvent event);

    /**
     * Notifies that the timegraph has removed a bookmark.
     *
     * @param event event object describing details
     */
    void bookmarkRemoved(TimeGraphBookmarkEvent event);
}
