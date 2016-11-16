/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;

/**
 * SWTBot class representing a Sash
 */
public class SWTBotSash extends AbstractSWTBotControl<Sash> {

    /**
     * The widget wrapper
     *
     * @param w
     *            the sash
     * @throws WidgetNotFoundException
     *             if there is no widget
     */
    public SWTBotSash(Sash w) throws WidgetNotFoundException {
        super(w);
    }

    /**
     * Get the bounds of the sash
     *
     * @return the bounds relative to the parent
     */
    public Rectangle getBounds() {
        return syncExec(new Result<Rectangle>() {
            @Override
            public Rectangle run() {
                return widget.getBounds();
            }
        });
    }

    /**
     * Drag the sash from its middle point to the destination point
     *
     * @param dst
     *            the destination point relative to the parent
     */
    public void drag(final Point dst) {
        Rectangle bounds = getBounds();
        int x = bounds.width / 2;
        int y = bounds.height / 2;
        notify(SWT.MouseEnter);
        notify(SWT.Activate);
        notify(SWT.Selection, createSelectionEvent(bounds.x + x, bounds.y + y, SWT.NONE));
        notify(SWT.MouseDown, createMouseEvent(x, y, 1, SWT.NONE, 1));
        notify(SWT.DragDetect, createMouseEvent(x, y, 0, SWT.NONE, 0));
        notify(SWT.Move);
        notify(SWT.Selection, createSelectionEvent(dst.x, dst.y, SWT.NONE));
        notify(SWT.MouseMove, createMouseEvent(x, y, 0, SWT.BUTTON1, 0));
        notify(SWT.Selection, createSelectionEvent(dst.x, dst.y, SWT.BUTTON1));
        notify(SWT.MouseUp, createMouseEvent(x, y, 1, SWT.NONE, 1));
        notify(SWT.MouseExit);
    }

    private Event createSelectionEvent(int x, int y, int stateMask) {
        return syncExec(new Result<Event>() {
            @Override
            public Event run() {
                boolean vertical = (widget.getStyle() | SWT.VERTICAL) != 0;
                Point size = widget.getSize();
                Event event = createSelectionEvent(stateMask);
                event.x = vertical ? x : 0;
                event.y = vertical ? 0 : y;
                event.width = size.x;
                event.height = size.y;
                return event;
            }
        });
    }
}
