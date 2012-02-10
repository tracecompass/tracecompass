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
 * <b><u>ITmfEventType</u></b>
 * <p>
 */
public interface ITmfEventType extends Cloneable {

    /**
     * @return the event type context
     */
    public String getContext();

    /**
     * @return the event type name
     */
    public String getName();

    /**
     * @return the event fields
     */
    public ITmfEventField[] getFields();

    /**
     * @return a clone of the event content
     */
    public ITmfEventType clone();

}
