/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.views;

/**
 * Interface with a method for time navigation in time-based views.
 *
 * @author Bernd Hufmann
 *
 */
public interface ITmfTimeNavigationProvider {
    /**
     * Method to implement to scroll left or right
     *
     * @param left
     *            true to scroll left else false
     */
    void horizontalScroll(boolean left);
}