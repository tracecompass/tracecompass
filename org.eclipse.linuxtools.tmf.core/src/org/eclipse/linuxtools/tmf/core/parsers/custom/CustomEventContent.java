/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tassé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.parsers.custom;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

/**
 * Event content for custom text parsers
 *
 * @author Patrick Tassé
 * @since 3.0
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
    public CustomEventContent(String name, Object content, ITmfEventField[] fields) {
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
        if (!(obj instanceof CustomEventContent)) {
            return false;
        }
        return true;
    }
}
