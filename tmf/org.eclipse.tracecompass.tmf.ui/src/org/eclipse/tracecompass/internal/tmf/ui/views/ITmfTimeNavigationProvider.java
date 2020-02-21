/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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