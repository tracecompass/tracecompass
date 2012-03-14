/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.views.latency.model;

/**
 * <b><u>IGraphModelListener</u></b>
 * <p>
 */
public interface IGraphModelListener {
    
    /**
     * Method to notify listeners about model updates 
     */
    public void graphModelUpdated();

    /**
     * Method to inform listeners about current time updates 
     */
    public void currentEventUpdated(long currentEventTime);

}
