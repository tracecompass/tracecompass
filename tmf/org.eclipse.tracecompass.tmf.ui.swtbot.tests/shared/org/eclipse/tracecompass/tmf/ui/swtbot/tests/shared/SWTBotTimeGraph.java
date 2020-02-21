/*******************************************************************************
 * Copyright (c) 2016, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.results.ArrayResult;
import org.eclipse.swtbot.swt.finder.results.IntResult;
import org.eclipse.swtbot.swt.finder.results.ListResult;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.TableCollection;
import org.eclipse.swtbot.swt.finder.utils.TableRow;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;

/**
 * SWTBot class representing a time graph
 */
public class SWTBotTimeGraph extends AbstractSWTBotControl<TimeGraphControl> {

    /**
     * Constructor
     *
     * @param w the widget
     * @throws WidgetNotFoundException if the widget is <code>null</code> or widget has been disposed.
     */
    public SWTBotTimeGraph(TimeGraphControl w) throws WidgetNotFoundException {
        super(w);
    }

    /**
     * Constructor
     *
     * @param bot
     *            a SWTBot instance with which to find a time graph
     * @throws WidgetNotFoundException
     *             if the widget is <code>null</code> or widget has been
     *             disposed.
     */
    public SWTBotTimeGraph(SWTBot bot) throws WidgetNotFoundException {
        super(bot.widget(WidgetOfType.widgetOfType(TimeGraphControl.class)));
    }

    /**
     * Get the root entries of this time graph
     *
     * @return the array of root entries
     */
    public SWTBotTimeGraphEntry[] getEntries() {
        return syncExec(new ArrayResult<SWTBotTimeGraphEntry>() {
            @Override
            public SWTBotTimeGraphEntry[] run() {
                List<SWTBotTimeGraphEntry> entries = new ArrayList<>();
                for (ITimeGraphEntry entry : widget.getExpandedElements()) {
                    if (entry.getParent() == null) {
                        entries.add(new SWTBotTimeGraphEntry(widget, entry));
                    }
                }
                return entries.toArray(new SWTBotTimeGraphEntry[0]);
            }
        });
    }

    /**
     * Get the time graph entry at the specified path relative to the root.
     *
     * @param names
     *            the path of names
     * @return the time graph entry
     * @throws WidgetNotFoundException
     *             if the entry was not found.
     */
    public SWTBotTimeGraphEntry getEntry(String... names) throws WidgetNotFoundException {
        AtomicReference<ITimeGraphEntry> parent = new AtomicReference<>();
        AtomicReference<String> missing = new AtomicReference<>();
        SWTBotUtils.waitUntil(timegraph -> {
            List<ITimeGraphEntry> entries = syncExec(new ListResult<ITimeGraphEntry>() {
                @Override
                public List<ITimeGraphEntry> run() {
                    return Arrays.asList(timegraph.getExpandedElements());
                }
            });
            ITableLabelProvider labelProvider = timegraph.getLabelProvider();
            parent.set(null);
            for (String name : names) {
                boolean found = false;
                for (ITimeGraphEntry entry : entries) {
                    String label = labelProvider == null ? entry.getName() : labelProvider.getColumnText(entry, 0);
                    if (Objects.equals(entry.getParent(), parent.get()) && name.equals(label)) {
                        parent.set(entry);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    missing.set(name);
                    return false;
                }
            }
            return true;
        }, widget, () -> "Timed out waiting for time graph entry " + missing.get() + " of parent " + parent.get());
        return new SWTBotTimeGraphEntry(widget, parent.get());
    }

    /**
     * Perform a drag operation on the canvas of the time graph
     *
     * @param pDown
     *            Point for the mouse to press down on
     * @param pUp
     *            Point for the mouse to release on
     * @param mouseMask
     *            the SWT button mask to press
     */
    public void drag(Point pDown, Point pUp, int mouseMask) {
        List<Integer> buttons = new ArrayList<>();
        List<Integer> masks = new ArrayList<>();
        masks.add(SWT.NONE);
        if ((mouseMask & SWT.BUTTON1) != 0) {
            buttons.add(1);
            masks.add(masks.get(masks.size() - 1) | SWT.BUTTON1);
        }
        if ((mouseMask & SWT.BUTTON2) != 0) {
            buttons.add(2);
            masks.add(masks.get(masks.size() - 1) | SWT.BUTTON2);
        }
        if ((mouseMask & SWT.BUTTON3) != 0) {
            buttons.add(3);
            masks.add(masks.get(masks.size() - 1) | SWT.BUTTON3);
        }
        if ((mouseMask & SWT.BUTTON4) != 0) {
            buttons.add(4);
            masks.add(masks.get(masks.size() - 1) | SWT.BUTTON4);
        }
        if ((mouseMask & SWT.BUTTON5) != 0) {
            buttons.add(5);
            masks.add(masks.get(masks.size() - 1) | SWT.BUTTON5);
        }
        notify(SWT.MouseEnter);
        notify(SWT.MouseMove, createMouseEvent(pDown.x, pDown.y, 0, SWT.NONE, 0));
        notify(SWT.Activate);
        notify(SWT.FocusIn);
        for (int buttonIndex = 0; buttonIndex < buttons.size(); buttonIndex++) {
            int button = buttons.get(buttonIndex);
            int buttonMask = masks.get(buttonIndex);
            notify(SWT.MouseDown, createMouseEvent(pDown.x, pDown.y, button, buttonMask, 1));
        }
        if (!Objects.equals(pUp, pDown)) {
            notify(SWT.MouseMove, createMouseEvent(pUp.x, pUp.y, 0, mouseMask, 0));
        }
        for (int buttonIndex = buttons.size() - 1; buttonIndex >= 0; buttonIndex--) {
            int button = buttons.get(buttonIndex);
            int buttonMask = masks.get(buttonIndex + 1);
            notify(SWT.MouseUp, createMouseEvent(pUp.x, pUp.y, button, buttonMask, 1));
        }
        notify(SWT.MouseMove, createMouseEvent(pUp.x, pUp.y, 0, SWT.NONE, 0));
        notify(SWT.MouseExit);
        notify(SWT.Deactivate);
        notify(SWT.FocusOut);
    }

    /**
     * Set the expand state of a time graph entry
     *
     * @param names
     *            the path of names
     * @param expanded
     *            The expand state
     * @throws WidgetNotFoundException
     *             if the entry was not found.
     *
     */
    public void expandEntry(boolean expanded, String... names) {
        SWTBotTimeGraphEntry entryBot = getEntry(names);
        if (expanded) {
            entryBot.expand();
        } else {
            entryBot.collapse();
        }
    }

    /**
     * Get the number of expanded (visible) elements in the time graph
     *
     * @return The count
     */
    public int getExpandedElementCount() {
        return syncExec(widget::getExpandedElementCount);
    }

    /**
     * Expand all time graph entries
     */
    public void expandAll() {
        syncExec(widget::expandAll);
    }

    /**
     * Collapse all time graph entries
     */
    public void collapseAll() {
        syncExec(widget::collapseAll);
    }

    /**
     * Gets the table collection representing the selection.
     *
     * @return the selection in the time graph
     */
    public TableCollection selection() {
        return syncExec(new Result<TableCollection>() {
            @Override
            public TableCollection run() {
                final TableCollection collection = new TableCollection();
                ISelection selection = widget.getSelection();
                if (!selection.isEmpty()) {
                    Object element = ((StructuredSelection) selection).getFirstElement();
                    if (element instanceof ITimeGraphEntry) {
                        TableRow tableRow = new TableRow();
                        SWTBotTimeGraphEntry entry = new SWTBotTimeGraphEntry(widget, (ITimeGraphEntry) element);
                        for (int i = 0; i < widget.getTree().getColumnCount(); i++) {
                            tableRow.add(entry.getText(i));
                        }
                        collection.add(tableRow);
                    }
                }
                return collection;
            }
        });
    }

    /**
     * Get the name space width
     *
     * @return the name space width
     */
    public int getNameSpace() {
        return syncExec(new IntResult() {
            @Override
            public Integer run() {
                return widget.getTimeDataProvider().getNameSpace();
            }
        });
    }

    /**
     * Set the name space width
     *
     * @param nameSpace the name space width
     */
    public void setNameSpace(int nameSpace) {
        int x = widget.getTimeDataProvider().getNameSpace();
        Rectangle bounds = syncExec(new Result<Rectangle>() {
            @Override
            public Rectangle run() {
                return widget.getBounds();
            }
        });
        int y = bounds.y + bounds.height / 2;
        notify(SWT.MouseEnter);
        notify(SWT.MouseMove, createMouseEvent(x, y, 0, SWT.NONE, 0));
        notify(SWT.Activate);
        notify(SWT.FocusIn);
        notify(SWT.MouseDown, createMouseEvent(x, y, 1, SWT.NONE, 1));
        notify(SWT.DragDetect, createMouseEvent(nameSpace, y, 0, SWT.NONE, 0));
        notify(SWT.MouseMove, createMouseEvent(nameSpace, y, 1, SWT.BUTTON1, 1));
        notify(SWT.MouseUp, createMouseEvent(nameSpace, y, 1, SWT.BUTTON1, 1));
        notify(SWT.MouseMove, createMouseEvent(0, y, 0, SWT.NONE, 0));
        notify(SWT.MouseExit);
        notify(SWT.Deactivate);
        notify(SWT.FocusOut);
    }

    @Override
    public AbstractSWTBotControl<TimeGraphControl> moveMouseToWidget() {
        return super.moveMouseToWidget();
    }
}
