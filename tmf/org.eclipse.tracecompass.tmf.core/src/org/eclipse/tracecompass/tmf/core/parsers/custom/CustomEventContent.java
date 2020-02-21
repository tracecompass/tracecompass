/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tassé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;

/**
 * Event content for custom text parsers
 *
 * @author Patrick Tassé
 */
public class CustomEventContent extends TmfEventField {

    /**
     * Constructor.
     *
     * @param parent
     *            Parent event
     * @param content
     *            Event content
     */
    public CustomEventContent(CustomEvent parent, StringBuffer content) {
        super(ITmfEventField.ROOT_FIELD_ID, content, null);
    }

    /**
     * Create a new event field with sub-fields.
     *
     * @param name
     *            Field name
     * @param content
     *            Event content
     * @param fields
     *            The array of sub-fields
     */
    public CustomEventContent(@NonNull String name, Object content, ITmfEventField[] fields) {
        super(name, content, fields);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return ((obj instanceof CustomEventContent));
    }
}
