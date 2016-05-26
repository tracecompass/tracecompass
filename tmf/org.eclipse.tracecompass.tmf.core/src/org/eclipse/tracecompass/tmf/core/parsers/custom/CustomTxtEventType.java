/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Event type for custom text traces.
 *
 * @author Patrick Tassé
 */
public class CustomTxtEventType extends CustomEventType {

    /**
     * Constructor
     *
     * @param definition
     *            Custom text trace definition
     * @deprecated Use {@link #CustomTxtEventType(String, ITmfEventField)}
     *             instead.
     */
    @Deprecated
    public CustomTxtEventType(CustomTxtTraceDefinition definition) {
        super(definition);
    }

    /**
     * Constructor
     *
     * @param eventName
     *            the event name
     * @param root
     *            the root field
     * @since 2.1
     */
    public CustomTxtEventType(@NonNull String eventName, ITmfEventField root) {
        super(eventName, root);
    }

}
