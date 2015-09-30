/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
