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
 * <b><u>ITmfEventField</u></b>
 * <p>
 * The TMF event payload structure. Each field can be either a terminal or
 * further decomposed into subfields.
 */
public interface ITmfEventField extends Cloneable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The root field id (the main container)
     */
    public static final String ROOT_FIELD_ID = ":root:"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the field name
     */
    public String getName();

    /**
     * @return the field value
     */
    public Object getValue();

    /**
     * @return the list of subfield names (empty if none)
     */
    public String[] getFieldNames();

    /**
     * @return the nth field name (null if absent or inexistent)
     */
    public String getFieldName(int index);

    /**
     * @return the list of subfields (null if none)
     */
    public ITmfEventField[] getFields();

    /**
     * @return a specific subfield by name (null if absent or inexistent)
     */
    public ITmfEventField getField(String name);

    /**
     * @return a specific subfield by index (null if absent or inexistent)
     */
    public ITmfEventField getField(int index);

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /**
     * @return a clone of the event type
     */
    public ITmfEventField clone();

}
