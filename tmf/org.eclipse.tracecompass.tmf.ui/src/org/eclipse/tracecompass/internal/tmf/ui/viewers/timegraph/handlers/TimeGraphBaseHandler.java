/*******************************************************************************
* Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.timegraph.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Base handler, makes sure we have a timegraph control selected
 *
 * @author Matthew Khouzam
 *
 */
class TimeGraphBaseHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        IWorkbenchPart part = HandlerUtil.getActivePart(event);
        if (part instanceof AbstractTimeGraphView) {
            AbstractTimeGraphView view = (AbstractTimeGraphView) part;
            execute(view);

        }
        return null;
    }

    /**
     * Handle a view
     *
     * @param view
     *            the view
     */
    public void execute(AbstractTimeGraphView view) {
        TimeGraphViewer viewer = view.getTimeGraphViewer();
        if (viewer != null) {
            execute(viewer);
        }
    }

    /**
     * Handle a viewer
     *
     * @param viewer
     *            the viewer
     */
    public void execute(TimeGraphViewer viewer) {
        TimeGraphControl control = viewer.getTimeGraphControl();
        if (control != null) {
            execute(control);
        }
    }

    /**
     * Handle the control
     *
     * @param timegraph
     *            the control
     */
    public void execute(TimeGraphControl timegraph) {
        // Do nothing
    }
}
