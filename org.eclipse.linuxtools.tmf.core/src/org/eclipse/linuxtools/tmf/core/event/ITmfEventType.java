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
     * @return the event type ID
     */
    public String getId();

    /**
     * @return the event field labels
     */
    public String[] getFieldLabels();

    /**
     * @param index the event field index
     * @return the corresponding event field label
     */
    public String getFieldLabel(int index) throws TmfNoSuchFieldException;

    /**
     * @param fieldId the event field ID
     * @return the corresponding event field index
     */
    public int getFieldIndex(String fieldId) throws TmfNoSuchFieldException;

    /**
     * @return a clone of the event content
     */
    public ITmfEventType clone();

}
