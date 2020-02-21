/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

/**
 * Interface for time event that allows to specify the destination entry of the
 * event
 */
public interface ILinkEvent extends ITimeEvent {

    /**
     * Get this event's destination entry
     *
     * @return The destination entry
     */
    ITimeGraphEntry getDestinationEntry();
}
