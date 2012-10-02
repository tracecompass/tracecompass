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

/**
 * Item in the generic time graph view
 *
 * @version 1.0
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TimeGraphItem {
    public boolean _expanded;
    public boolean _selected;
    public boolean _hasChildren;
    public int itemHeight;
    public int level;
    public List<TimeGraphItem> children;
    public String _name;
    public ITimeGraphEntry _trace;

    /**
     * Standard constructor
     *
     * @param trace
     *            The entry matching to trace to which this item is associated
     * @param name
     *            The name of the item
     * @param level
     *            The tree level of this entry (0 = top level)
     */
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