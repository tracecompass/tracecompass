/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.tree;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;

/**
 * Abstract tool tip handler for TmfTreeViewers. Register the handler to a
 * TmfTreeViewer's {@link Control} by calling
 * {@link TmfAbstractToolTipHandler#activateHoverHelp(Control)}
 *
 * @since 3.2
 * @author Loic Prieur-Drevon
 */
public abstract class TmfTreeViewerToolTipHandler extends TmfAbstractToolTipHandler {

    @Override
    protected void fill(Control control, MouseEvent event, Point pt) {
        Tree tree = (Tree) control;
        TreeItem item = tree.getItem(pt);
        if (item == null) {
            return;
        }
        Object data = item.getData();
        if (data instanceof TmfTreeViewerEntry) {
            fillValues((TmfTreeViewerEntry) data);
        }
    }

    /**
     * Abstract method for implementations to populate the tool tip by calling
     * the addItem() methods.
     *
     * @param entry
     *            the {@link TmfTreeViewerEntry} which we are hovering above.
     */
    protected abstract void fillValues(TmfTreeViewerEntry entry);

}
