/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.hamcrest.SelfDescribing;

/**
 * SWT Bot sash control
 */
public class SWTBotSash extends AbstractSWTBotControl<Sash> {

    /**
     * The widget wrapper
     *
     * @param w
     *            the sash
     * @param description
     *            the description
     * @throws WidgetNotFoundException
     *             if there is no widget
     */
    public SWTBotSash(Sash w, SelfDescribing description) throws WidgetNotFoundException {
        super(w, description);
    }

    /**
     * Get the central point of the sash
     *
     * @return the center point, good for dragging
     */
    public Point getPoint() {
        return UIThreadRunnable.syncExec(new Result<Point>() {

            @Override
            public Point run() {
                return widget.toDisplay(0, widget.getSize().y / 2);
            }

        });
    }

    /**
     * Simulate a drag
     *
     * @param dst
     *            to this destination
     */
    public void drag(final Point dst) {
        final Point src = getPoint();
        /*
         * example of a move
         *
         * dn : MouseEvent{Sash {} time=262463957 data=null button=1
         * stateMask=0x0 x=5 y=59 count=1}
         *
         * move : MouseEvent{Sash {} time=262464038 data=null button=0
         * stateMask=0x80000 x=131 y=103 count=0}
         *
         * move : MouseEvent{Sash {} time=262464171 data=null button=0
         * stateMask=0x80000 x=90 y=116 count=0}
         *
         * up : MouseEvent{Sash {} time=262464796 data=null button=1
         * stateMask=0x80000 x=5 y=116 count=1}
         */
        try {
            final Robot awtRobot = new Robot();

            // move a maximum of 10 points / event
            final int magDist = (src.x - dst.x) * (src.x - dst.x) + (src.y - dst.y) * (src.y - dst.y);

            final int steps = Math.max(1, (int) Math.sqrt(magDist / 100.0));

            final int stepX = (dst.x - src.x) / steps;
            final int stepY = (dst.y - src.y) / steps;
            syncExec(new VoidResult() {
                @Override
                public void run() {
                    awtRobot.mouseMove(src.x, src.y);
                    SWTBotUtils.delay(15);
                    awtRobot.mousePress(InputEvent.BUTTON1_MASK);

                }
            });
            for (int i = 0; i < steps; i++) {
                final int index = i;
                asyncExec(new VoidResult() {
                    @Override
                    public void run() {
                        int x = src.x + index * stepX;
                        int y = src.y + index * stepY;
                        awtRobot.mouseMove(x, y);
                    }
                });
                // drag delay
                SWTBotUtils.delay(10);
            }
            // drop delay
            SWTBotUtils.delay(100);
            syncExec(new VoidResult() {
                @Override
                public void run() {
                    awtRobot.mouseRelease(InputEvent.BUTTON1_MASK);
                }
            });

        } catch (final AWTException e) {
            // log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
