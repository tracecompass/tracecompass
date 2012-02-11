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
 * <b><u>ITmfEventContent</u></b>
 * <p>
 */
public interface ITmfEventContent extends Cloneable {

    /**
     * @return the raw event content
     */
    public Object getRawContent();

    /**
     * @return the formatted event content (string'ified)
     */
    public String getFmtContent();

    /**
     * @return the event type
     */
    public ITmfEventType getType();

    /**
     * @return the list of event fields
     */
    public ITmfEventField[] getFields();

    /**
     * @param index the field index
     * @return the corresponding field
     */
    public ITmfEventField getField(int index) throws TmfNoSuchFieldException;

    /**
     * @param name the field name
     * @return the corresponding field
     */
    public ITmfEventField getField(String name) throws TmfNoSuchFieldException;

    /**
     * @return a clone of the event content
     */
    public ITmfEventContent clone();
}
