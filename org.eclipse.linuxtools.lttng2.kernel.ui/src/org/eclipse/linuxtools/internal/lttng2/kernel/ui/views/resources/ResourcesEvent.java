/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesEntry.Type;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

public class ResourcesEvent extends TimeEvent {

    private Type fType;
    private int fValue;

    public ResourcesEvent(ResourcesEntry entry, long time, long duration, int value) {
        super(entry, time, duration);
        fType = entry.getType();
        fValue = value;
    }

    public ResourcesEvent(ResourcesEntry entry, long time, long duration) {
        super(entry, time, duration);
        fType = Type.NULL;
    }

    public int getValue() {
        return fValue;
    }

    public Type getType() {
        return fType;
    }
}
