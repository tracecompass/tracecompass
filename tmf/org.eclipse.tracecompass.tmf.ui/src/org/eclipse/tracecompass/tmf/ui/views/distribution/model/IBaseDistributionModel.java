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

package org.eclipse.tracecompass.tmf.ui.views.distribution.model;

/**
 * Base distribution model interface.
 *
 * Distribution models such histograms need to implement this interface.
 *
 * @version 1.0
 * @author Bernd Hufmann
 *
 */
public interface IBaseDistributionModel {
    /**
     * Complete the model (all data received)
     */
    void complete();

    /**
     * Clear the model (delete all data).
     */
    void clear();
}