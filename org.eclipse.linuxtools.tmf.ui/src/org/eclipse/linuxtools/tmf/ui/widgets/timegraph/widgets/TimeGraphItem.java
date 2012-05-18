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

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

public class TimeGraphItem {
    public boolean _expanded;
    public boolean _selected;
    public boolean _hasChildren;
    public int itemHeight;
    public int level;
    public List<TimeGraphItem> children;
    public String _name;
    public ITimeGraphEntry _trace;

    public TimeGraphItem(ITimeGraphEntry trace, String name, int level) {
        this._trace = trace;
        this._name = name;
        this.level = level;
        this.children = new ArrayList<TimeGraphItem>();
    }

    @Override
    public String toString() {
        return _name;
    }
}