/*******************************************************************************
 * Copyright (c) 2016, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.ArrayResult;
import org.eclipse.swtbot.swt.finder.results.ListResult;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.WaitForObjectCondition;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;

/**
 * SWTBot class representing a time graph entry
 */
public class SWTBotTimeGraphEntry extends AbstractSWTBotControl<TimeGraphControl> {

    private final ITimeGraphEntry fEntry;

    /**
     * Constructor
     *
     * @param w
     *            the widget
     * @param entry
     *            the time graph entry
     *
     * @throws WidgetNotFoundException
     *             if the widget is <code>null</code> or widget has been
     *             disposed.
     */
    public SWTBotTimeGraphEntry(TimeGraphControl w, ITimeGraphEntry entry) throws WidgetNotFoundException {
        super(w);
        fEntry = entry;
    }

    @Override
    protected SWTBotRootMenu contextMenu(final Control control) throws WidgetNotFoundException {
        Rectangle bounds = absoluteLocation();
        if (bounds == null) {
            return null;
        }
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                final Event event = new Event();
                event.time = (int) System.currentTimeMillis();
                event.display = control.getDisplay();
                event.widget = control;
                event.x = bounds.x + widget.getTimeDataProvider().getNameSpace() / 2;
                event.y = bounds.y + bounds.height / 2;
                control.notifyListeners(SWT.MenuDetect, event);
            }
        });
        select();

        WaitForObjectCondition<Menu> waitForMenu = Conditions.waitForPopupMenu(control);
        new SWTBot().waitUntilWidgetAppears(waitForMenu);
        return new SWTBotRootMenu(waitForMenu.get(0));
    }

    /*
     * Note this is public
     */
    @Override
    public Rectangle absoluteLocation() {
        return UIThreadRunnable.syncExec(() -> {
            Rectangle bounds = widget.getItemBounds(fEntry);
            if (bounds == null) {
                return null;
            }
            Point location = widget.toDisplay(bounds.x, bounds.y);
            bounds.x = location.x;
            bounds.y = location.y;
            return bounds;
        });
    }

    /**
     * Get the child entries of this entry
     *
     * @return the array of child entries
     */
    public SWTBotTimeGraphEntry[] getEntries() {
        return syncExec(new ArrayResult<SWTBotTimeGraphEntry>() {
            @Override
            public SWTBotTimeGraphEntry[] run() {
                List<SWTBotTimeGraphEntry> entries = new ArrayList<>();
                for (ITimeGraphEntry entry : widget.getExpandedElements()) {
                    if (fEntry.equals(entry.getParent())) {
                        entries.add(new SWTBotTimeGraphEntry(widget, entry));
                    }
                }
                return entries.toArray(new SWTBotTimeGraphEntry[0]);
            }
        });
    }

    /**
     * Click on the entry at a timestamp
     *
     * @param time
     *            the timestamp to click at.
     */
    public void click(long time) {
        final Point p = getPointForTime(time);
        if (p == null) {
            return;
        }
        clickXY(p.x, p.y);
    }

    /**
     * Double-click on the entry at a timestamp
     *
     * @param time
     *            the timestamp to double-click at.
     */
    public void doubleClick(long time) {
        final Point p = getPointForTime(time);
        if (p == null) {
            return;
        }
        doubleClickXY(p.x, p.y);
    }

    /**
     * Get the coordinates of a point for a specified time
     *
     * @param time
     *            the time to get the equivalent Point of.
     * @return the point at the time
     */
    public Point getPointForTime(long time) {
        return UIThreadRunnable.syncExec((Result<Point>) () -> {
            ITimeDataProvider timeDataProvider = widget.getTimeDataProvider();
            if (timeDataProvider.getTime0() > time || timeDataProvider.getTime1() < time) {
                return null;
            }
            int x = widget.getXForTime(time);
            Rectangle bounds = widget.getItemBounds(fEntry);
            int y = bounds.y + bounds.height / 2;
            return new Point(x, y);
        });
    }

    /**
     * Get the time in the entry for a given X point
     *
     * @param x
     *            the x point
     * @return the time or {@code null} or {@code -1} if it is out of bounds
     */
    public Long getTimeForPoint(int x) {
        return UIThreadRunnable.syncExec((Result<@Nullable Long>) () -> {
            Rectangle bounds = widget.getBounds();
            Point p = new Point(x, bounds.y + bounds.height / 2);
            if (!bounds.contains(p)) {
                return null;
            }
            return widget.getTimeAtX(x);
        });
    }

    /**
     * Get the child entry of this entry with the given name
     *
     * @param name
     *            the name of the entry
     *
     * @return the child entry
     */
    public SWTBotTimeGraphEntry getEntry(String name) {
        AtomicReference<ITimeGraphEntry> found = new AtomicReference<>();
        SWTBotUtils.waitUntil(timegraph -> {
            List<ITimeGraphEntry> entries = syncExec(new ListResult<ITimeGraphEntry>() {
                @Override
                public List<ITimeGraphEntry> run() {
                    return Arrays.asList(timegraph.getExpandedElements());
                }
            });
            ITableLabelProvider labelProvider = timegraph.getLabelProvider();
            for (ITimeGraphEntry entry : entries) {
                if (fEntry.equals(entry.getParent())) {
                    String label = labelProvider == null ? entry.getName() : labelProvider.getColumnText(entry, 0);
                    if (name.equals(label)) {
                        found.set(entry);
                        return true;
                    }
                }
            }
            return false;
        }, widget, () -> "Timed out waiting for time graph entry " + name);
        return new SWTBotTimeGraphEntry(widget, found.get());
    }

    /**
     * Get the text of this entry
     *
     * @return the text
     */
    @Override
    public String getText() {
        return getText(0);
    }

    /**
     * Get the text of this entry for the given column index
     *
     * @param column
     *            the column index
     * @return the column text
     */
    public String getText(int column) {
        ITableLabelProvider labelProvider = widget.getLabelProvider();
        return labelProvider != null ? labelProvider.getColumnText(fEntry, column) : column == 0 ? fEntry.getName() : "";
    }

    /**
     * Select this time graph entry
     *
     * @return itself
     */
    public SWTBotTimeGraphEntry select() {
        syncExec(new VoidResult() {
            @Override
            public void run() {
                widget.setFocus();
                widget.selectItem(fEntry, false);
                widget.fireSelectionChanged();
            }
        });
        return this;
    }

    /**
     * Expand the time graph entry
     */
    public void expand() {
        syncExec(() -> widget.setExpandedState(fEntry, true));
    }

    /**
     * Expand the time graph entry
     */
    public void collapse() {
        syncExec(() -> widget.setExpandedState(fEntry, false));
    }
}
