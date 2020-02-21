/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

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
     * The default event type name
     */
    @NonNull String DEFAULT_TYPE_ID = "TmfType"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the event type ID
     */
    @NonNull String getName();

    /**
     * @return the event type root field
     */
    ITmfEventField getRootField();

    /**
     * @return the event field names (labels)
     */
    Collection<String> getFieldNames();
}
