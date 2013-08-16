/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model;

/**
 * Interface for time event that allows to specify the destination entry of the
 * event
 *
 * @since 2.1
 */
public interface ILinkEvent extends ITimeEvent {

    /**
     * Get this event's destination entry
     *
     * @return The destination entry
     */
    ITimeGraphEntry getDestinationEntry();
}
