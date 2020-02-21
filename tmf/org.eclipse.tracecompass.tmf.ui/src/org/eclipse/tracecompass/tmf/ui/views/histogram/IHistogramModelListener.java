/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.histogram;

/**
 * Listener interface for receiving histogram data model notifications.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public interface IHistogramModelListener {
    /**
     * Method to implement to receive notification about model updates.
     */
    void modelUpdated();
}
