/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.core.event;

import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

/**
 * <b><u>LttngEventField</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventField.<p>
 * 
 * LttngEventField add a "name" attribute to the Tmf implementation This
 * mean the fields will have a name and a value.
 */
public class LttngEventField extends TmfEventField {

    public LttngEventField(String name, Object value, LttngEventField[] subfields) {
        super(name, value, subfields);
    }
    
    public LttngEventField(String name, Object value) {
        this(name, value, null);
    }
    
    public LttngEventField(String name) {
        this(name, null, null);
    }

    public LttngEventField(LttngEventField field) {
        super(field);
    }

    @Override
    public LttngEventField clone() {
        LttngEventField clone = null;
        clone = (LttngEventField) super.clone();
        return clone;
    }

    @Override
    public String toString() {
        return getName() + ":" + getValue(); //$NON-NLS-1$
    }
}
