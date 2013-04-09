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
 * The generic event type in TMF. It contains a reference to the full field structure
 * for that event type.
 * <p>
 * Types are unique within their context space.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfEventField
 */
public interface ITmfEventType {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The default event type content
     */
    public static final String DEFAULT_CONTEXT_ID = "TmfContext"; //$NON-NLS-1$

    /**
     * The default event type name
     */
    public static final String DEFAULT_TYPE_ID = "TmfType"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the event type context
     */
    public String getContext();

    /**
     * @return the event type ID
     */
    public String getName();

    /**
     * @return the event type root field
     */
    public ITmfEventField getRootField();

    /**
     * @return the event field names (labels)
     */
    public String[] getFieldNames();

    /**
     * @param index the event field index
     * @return the corresponding event field label
     */
    public String getFieldName(int index);
}
