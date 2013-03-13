/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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
 * The generic event payload in TMF. Each field can be either a terminal or
 * further decomposed into subfields.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfEventType
 */
public interface ITmfEventField {

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
     * @return the value formatted as string
     * @since 2.0
     */
    public String getFormattedValue();

    /**
     * @return the list of subfield names (empty array if none)
     */
    public String[] getFieldNames();

    /**
     * @param index The index of the field
     * @return the nth field name (null if absent or inexistent)
     */
    public String getFieldName(int index);

    /**
     * @return the list of subfields (empty array if none)
     */
    public ITmfEventField[] getFields();

    /**
     * @param name The name of the field
     * @return a specific subfield by name (null if absent or inexistent)
     */
    public ITmfEventField getField(String name);

    /**
     * @param index The index of the field to return
     * @return a specific subfield by index (null if absent or inexistent)
     */
    public ITmfEventField getField(int index);

}
