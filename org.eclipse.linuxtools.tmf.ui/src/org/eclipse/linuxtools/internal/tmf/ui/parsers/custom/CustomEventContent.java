/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tassé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

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
        super(ITmfEventField.ROOT_FIELD_ID, content);
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

    /**
     * Modify the fields to the given value.
     *
     * @param fields
     *            The array of fields to use as event content
     */
    public void setFields(ITmfEventField[] fields) {
        super.setValue(getValue(), fields);
    }

}
