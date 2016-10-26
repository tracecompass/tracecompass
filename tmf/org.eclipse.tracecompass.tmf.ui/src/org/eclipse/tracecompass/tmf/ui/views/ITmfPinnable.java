/*******************************************************************************
 * Copyright (c) 2016, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views;

/**
 * An interface that adds the pin feature to a view.
 *
 * @since 3.2
 */
public interface ITmfPinnable {

    /**
     * Set the pin state.
     *
     * @param pinned
     *            The pin state to take action on
     */
    void setPinned(boolean pinned);
}
