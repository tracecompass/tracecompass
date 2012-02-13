/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

/**
 * <b><u>ITmfEvent</u></b>
 * <p>
 * The ITmfEvent is essentially an ITmfDataEvent with a timestamp.
*/
public interface ITmfEvent extends ITmfDataEvent, Cloneable {

    /**
     * @return the event timestamp
     */
    public ITmfTimestamp getTimestamp();

}
