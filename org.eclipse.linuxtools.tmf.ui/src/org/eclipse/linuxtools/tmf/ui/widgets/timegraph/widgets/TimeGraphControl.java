/*****************************************************************************
 * Copyright (c) 2007, 2014 Intel Corporation and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Intel Corporation - Initial API and implementation
 *   Ruslan A. Scherbakov, Intel - Initial API and implementation
 *   Alvaro Sanchez-Leon, Ericsson - Updated for TMF
 *   Patrick Tasse, Ericsson - Refactoring
 *   Geneviève Bastien, École Polytechnique de Montréal - Move code to
 *                            provide base classes for time graph view
 *                            Add display of links between items
 *   Xavier Raynaud, Kalray - Code optimization
 *   Generoso Pagano, Inria - Support for drag selection listeners
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampDelta;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphColorListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider2;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTreeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTreeExpansionEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Time graph control implementation
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public class TimeGraphControl extends TimeGraphBaseControl
        implements FocusListener, KeyListener, MouseMoveListener, MouseListener, MouseWheelListener,
        ControlListener, SelectionListener, MouseTrackListener, TraverseListener, ISelectionProvider,
        MenuDetectListener, ITmfTimeGraphDrawingHelper, ITimeGraphColorListener {

    /** Max scrollbar size */
    public static final int H_SCROLLBAR_MAX = Integer.MAX_VALUE - 1;

    /** Constant indicating that all levels of the time graph should be expanded
     * @since 3.1 */
    public static final int ALL_LEVELS = AbstractTreeViewer.ALL_LEVELS;

    private static final int DRAG_NONE = 0;
    private static final int DRAG_TRACE_ITEM = 1;
    private static final int DRAG_SPLIT_LINE = 2;
    private static final int DRAG_ZOOM = 3;
    private static final int DRAG_SELECTION = 4;

    private static final int CUSTOM_ITEM_HEIGHT = -1; // get item height from provider

    private static final double ZOOM_FACTOR = 1.5;
    private static final double ZOOM_IN_FACTOR = 0.8;
    private static final double ZOOM_OUT_FACTOR = 1.25;

    private static final int SNAP_WIDTH = 2;
    private static final int ARROW_HOVER_MAX_DIST = 5;

    private static final int NO_STATUS = -1;

    /** Resource manager */
    private LocalResourceManager fResourceManager = new LocalResourceManager(JFaceResources.getResources());

    /** Color map for event types */
    private Color[] fEventColorMap = null;

    private ITimeDataProvider fTimeProvider;
    private IStatusLineManager fStatusLineManager = null;
    private TimeGraphScale fTimeGraphScale = null;

    private boolean fIsInFocus = false;
    private boolean fMouseOverSplitLine = false;
    private int fGlobalItemHeight = CUSTOM_ITEM_HEIGHT;
    private int fMinimumItemWidth = 0;
    private int fTopIndex = 0;
    private int fDragState = DRAG_NONE;
    private int fDragButton;
    private int fDragX0 = 0;
    private int fDragX = 0;
    private long fDragTime0 = 0; // used to preserve accuracy of modified selection
    private int fIdealNameSpace = 0;
    private long fTime0bak;
    private long fTime1bak;
    private ITimeGraphPresentationProvider fTimeGraphProvider = null;
    private ItemData fItemData = null;
    private List<SelectionListener> fSelectionListeners;
    private List<ITimeGraphTimeListener> fDragSelectionListeners;
    private final List<ISelectionChangedListener> fSelectionChangedListeners = new ArrayList<>();
    private final List<ITimeGraphTreeListener> fTreeListeners = new ArrayList<>();
    private final List<MenuDetectListener> fTimeGraphEntryMenuListeners = new ArrayList<>();
    private final List<MenuDetectListener> fTimeEventMenuListeners = new ArrayList<>();
    private final Cursor fDragCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_HAND);
    private final Cursor fResizeCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_IBEAM);
    private final Cursor fWaitCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT);
    private final Cursor fZoomCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_SIZEWE);
    private final List<ViewerFilter> fFilters = new ArrayList<>();
    private MenuDetectEvent fPendingMenuDetectEvent = null;
    private boolean fHideArrows = false;
    private int fAutoExpandLevel = ALL_LEVELS;

    private int fBorderWidth = 0;
    private int fHeaderHeight = 0;

    private Listener fMouseScrollFilterListener;

    private MouseScrollNotifier fMouseScrollNotifier;
    private final Object fMouseScrollNotifierLock = new Object();

    private class MouseScrollNotifier extends Thread {
        private static final long DELAY = 400L;
        private static final long POLLING_INTERVAL = 10L;
        private long fLastScrollTime = Long.MAX_VALUE;

        @Override
        public void run() {
            while ((System.currentTimeMillis() - fLastScrollTime) < DELAY) {
                try {
                    Thread.sleep(POLLING_INTERVAL);
                } catch (Exception e) {
                    return;
                }
            }
            if (!isInterrupted()) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (isDisposed()) {
                            return;
                        }
                        fTimeProvider.notifyStartFinishTime();
                    }
                });
            }
            synchronized (fMouseScrollNotifierLock) {
                fMouseScrollNotifier = null;
            }
        }

        public void mouseScrolled() {
            fLastScrollTime = System.currentTimeMillis();
        }
    }

    /**
     * Standard constructor
     *
     * @param parent
     *            The parent composite object
     * @param colors
     *            The color scheme to use
     */
    public TimeGraphControl(Composite parent, TimeGraphColorScheme colors) {

        super(parent, colors, SWT.NO_BACKGROUND | SWT.H_SCROLL | SWT.DOUBLE_BUFFERED);

        fItemData = new ItemData();

        addFocusListener(this);
        addMouseListener(this);
        addMouseMoveListener(this);
        addMouseTrackListener(this);
        addMouseWheelListener(this);
        addTraverseListener(this);
        addKeyListener(this);
        addControlListener(this);
        addMenuDetectListener(this);
        ScrollBar scrollHor = getHorizontalBar();

        if (scrollHor != null) {
            scrollHor.addSelectionListener(this);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        fResourceManager.dispose();
    }

    /**
     * Sets the timegraph provider used by this timegraph viewer.
     *
     * @param timeGraphProvider the timegraph provider
     */
    public void setTimeGraphProvider(ITimeGraphPresentationProvider timeGraphProvider) {
        fTimeGraphProvider = timeGraphProvider;

        if (timeGraphProvider instanceof ITimeGraphPresentationProvider2) {
            ((ITimeGraphPresentationProvider2) timeGraphProvider).setDrawingHelper(this);
            ((ITimeGraphPresentationProvider2) timeGraphProvider).addColorListener(this);
        }

        StateItem[] stateItems = fTimeGraphProvider.getStateTable();
        colorSettingsChanged(stateItems);
    }

    /**
     * Gets the timegraph provider used by this timegraph viewer.
     *
     * @return the timegraph provider, or <code>null</code> if not set.
     * @since 3.0
     */
    public ITimeGraphPresentationProvider getTimeGraphProvider() {
        return fTimeGraphProvider;
    }

    /**
     * Gets the color map used by this timegraph viewer.
     *
     * @return a color map, or <code>null</code> if not set.
     * @since 3.0
     */
    public Color[] getEventColorMap() {
        return fEventColorMap;
    }

    /**
     * Assign the given time provider
     *
     * @param timeProvider
     *            The time provider
     */
    public void setTimeProvider(ITimeDataProvider timeProvider) {
        fTimeProvider = timeProvider;
        adjustScrolls();
        redraw();
    }

    /**
     * Assign the status line manager
     *
     * @param statusLineManager
     *            The status line manager, or null to disable status line messages
     * @since 2.1
     */
    public void setStatusLineManager(IStatusLineManager statusLineManager) {
        if (fStatusLineManager != null && statusLineManager == null) {
            fStatusLineManager.setMessage(""); //$NON-NLS-1$
        }
        fStatusLineManager = statusLineManager;
    }

    /**
     * Assign the time graph scale
     *
     * @param timeGraphScale
     *            The time graph scale
     * @since 2.1
     */
    public void setTimeGraphScale(TimeGraphScale timeGraphScale) {
        fTimeGraphScale = timeGraphScale;
    }

    /**
     * Add a selection listener
     *
     * @param listener
     *            The listener to add
     */
    public void addSelectionListener(SelectionListener listener) {
        if (listener == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        if (null == fSelectionListeners) {
            fSelectionListeners = new ArrayList<>();
        }
        fSelectionListeners.add(listener);
    }

    /**
     * Remove a selection listener
     *
     * @param listener
     *            The listener to remove
     */
    public void removeSelectionListener(SelectionListener listener) {
        if (null != fSelectionListeners) {
            fSelectionListeners.remove(listener);
        }
    }

    /**
     * Selection changed callback
     */
    public void fireSelectionChanged() {
        if (null != fSelectionListeners) {
            Iterator<SelectionListener> it = fSelectionListeners.iterator();
            while (it.hasNext()) {
                SelectionListener listener = it.next();
                listener.widgetSelected(null);
            }
        }
    }

    /**
     * Default selection callback
     */
    public void fireDefaultSelection() {
        if (null != fSelectionListeners) {
            Iterator<SelectionListener> it = fSelectionListeners.iterator();
            while (it.hasNext()) {
                SelectionListener listener = it.next();
                listener.widgetDefaultSelected(null);
            }
        }
    }

    /**
     * Add a drag selection listener
     *
     * @param listener
     *            The listener to add
     * @since 3.1
     */
    public void addDragSelectionListener(ITimeGraphTimeListener listener) {
        if (listener == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        if (null == fDragSelectionListeners) {
            fDragSelectionListeners = new ArrayList<>();
        }
        fDragSelectionListeners.add(listener);
    }

    /**
     * Remove a drag selection listener
     *
     * @param listener
     *            The listener to remove
     * @since 3.1
     */
    public void removeDragSelectionListener(ITimeGraphTimeListener listener) {
        if (null != fDragSelectionListeners) {
            fDragSelectionListeners.remove(listener);
        }
    }

    /**
     * Drag Selection changed callback
     *
     * @param start
     *            Time interval start
     * @param end
     *            Time interval end
     * @since 3.1
     */
    public void fireDragSelectionChanged(long start, long end) {
        // check for backward intervals
        long beginTime, endTime;
        if (start > end) {
            beginTime = end;
            endTime = start;
        } else {
            beginTime = start;
            endTime = end;
        }
        // call the listeners
        if (null != fDragSelectionListeners) {
            Iterator<ITimeGraphTimeListener> it = fDragSelectionListeners.iterator();
            while (it.hasNext()) {
                ITimeGraphTimeListener listener = it.next();
                listener.timeSelected(new TimeGraphTimeEvent(this, beginTime, endTime));
            }
        }
    }

    /**
     * Get the traces in the model
     *
     * @return The array of traces
     */
    public ITimeGraphEntry[] getTraces() {
        return fItemData.getEntries();
    }

    /**
     * Get the on/off trace filters
     *
     * @return The array of filters
     */
    public boolean[] getTraceFilter() {
        return fItemData.getEntryFilter();
    }

    /**
     * Refresh the data for the thing
     */
    public void refreshData() {
        fItemData.refreshData();
        adjustScrolls();
        redraw();
    }

    /**
     * Refresh data for the given traces
     *
     * @param traces
     *            The traces to refresh
     */
    public void refreshData(ITimeGraphEntry[] traces) {
        fItemData.refreshData(traces);
        adjustScrolls();
        redraw();
    }

    /**
     * Refresh the links (arrows) of this widget
     *
     * @param events The link events to refresh
     * @since 2.1
     */
    public void refreshArrows(List<ILinkEvent> events) {
        fItemData.refreshArrows(events);
    }

    /**
     * Adjust the scoll bars
     */
    public void adjustScrolls() {
        if (null == fTimeProvider) {
            getHorizontalBar().setValues(0, 1, 1, 1, 1, 1);
            return;
        }

        // HORIZONTAL BAR
        // Visible window
        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        // Time boundaries
        long timeMin = fTimeProvider.getMinTime();
        long timeMax = fTimeProvider.getMaxTime();

        long delta = timeMax - timeMin;

        int timePos = 0;
        int thumb = H_SCROLLBAR_MAX;

        if (delta != 0) {
            // Thumb size (page size)
            thumb = Math.max(1, (int) (H_SCROLLBAR_MAX * ((double) (time1 - time0) / delta)));
            // At the beginning of visible window
            timePos = (int) (H_SCROLLBAR_MAX * ((double) (time0 - timeMin) / delta));
        }

        // position, minimum, maximum, thumb size, increment (half page)t, page
        // increment size (full page)
        getHorizontalBar().setValues(timePos, 0, H_SCROLLBAR_MAX, thumb, Math.max(1, thumb / 2), Math.max(2, thumb));
    }

    boolean ensureVisibleItem(int idx, boolean redraw) {
        boolean changed = false;
        int index = idx;
        if (index < 0) {
            for (index = 0; index < fItemData.fExpandedItems.length; index++) {
                if (fItemData.fExpandedItems[index].fSelected) {
                    break;
                }
            }
        }
        if (index >= fItemData.fExpandedItems.length) {
            return changed;
        }
        if (index < fTopIndex) {
            setTopIndex(index);
            if (redraw) {
                redraw();
            }
            changed = true;
        } else {
            int page = countPerPage();
            if (index >= fTopIndex + page) {
                setTopIndex(index - page + 1);
                if (redraw) {
                    redraw();
                }
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Assign the given index as the top one
     *
     * @param idx
     *            The index
     */
    public void setTopIndex(int idx) {
        int index = Math.min(idx, fItemData.fExpandedItems.length - countPerPage());
        index = Math.max(0,  index);
        fTopIndex = index;
        redraw();
    }

    /**
     * Sets the auto-expand level to be used when the entries are refreshed
     * using {@link #refreshData()} or {@link #refreshData(ITimeGraphEntry[])}.
     * The value 0 means that there is no auto-expand; 1 means that top-level
     * entries are expanded, but not their children; 2 means that top-level
     * entries are expanded, and their children, but not grand-children; and so
     * on.
     * <p>
     * The value {@link #ALL_LEVELS} means that all subtrees should be expanded.
     * </p>
     * @param level
     *            non-negative level, or <code>ALL_LEVELS</code> to expand all
     *            levels of the tree
     * @since 3.1
     */
    public void setAutoExpandLevel(int level) {
        fAutoExpandLevel = level;
    }

    /**
     * Returns the auto-expand level.
     *
     * @return non-negative level, or <code>ALL_LEVELS</code> if all levels of
     *         the tree are expanded automatically
     * @see #setAutoExpandLevel
     * @since 3.1
     */
    public int getAutoExpandLevel() {
        return fAutoExpandLevel;
    }

    /**
     * Set the expanded state of a given entry
     *
     * @param entry
     *            The entry
     * @param expanded
     *            True if expanded, false if collapsed
     */
    public void setExpandedState(ITimeGraphEntry entry, boolean expanded) {
        Item item = fItemData.findItem(entry);
        if (item != null && item.fExpanded != expanded) {
            item.fExpanded = expanded;
            fItemData.updateExpandedItems();
            redraw();
        }
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void collapseAll() {
        for (Item item : fItemData.fItems) {
            item.fExpanded = false;
        }
        fItemData.updateExpandedItems();
        redraw();
    }

    /**
     * Expands all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void expandAll() {
        for (Item item : fItemData.fItems) {
            item.fExpanded = true;
        }
        fItemData.updateExpandedItems();
        redraw();
    }

    /**
     * Add a tree listener
     *
     * @param listener
     *            The listener to add
     */
    public void addTreeListener(ITimeGraphTreeListener listener) {
        if (!fTreeListeners.contains(listener)) {
            fTreeListeners.add(listener);
        }
    }

    /**
     * Remove a tree listener
     *
     * @param listener
     *            The listener to remove
     */
    public void removeTreeListener(ITimeGraphTreeListener listener) {
        if (fTreeListeners.contains(listener)) {
            fTreeListeners.remove(listener);
        }
    }

    /**
     * Tree event callback
     *
     * @param entry
     *            The affected entry
     * @param expanded
     *            The expanded state (true for expanded, false for collapsed)
     */
    public void fireTreeEvent(ITimeGraphEntry entry, boolean expanded) {
        TimeGraphTreeExpansionEvent event = new TimeGraphTreeExpansionEvent(this, entry);
        for (ITimeGraphTreeListener listener : fTreeListeners) {
            if (expanded) {
                listener.treeExpanded(event);
            } else {
                listener.treeCollapsed(event);
            }
        }
    }

    /**
     * Add a menu listener on {@link ITimeGraphEntry}s
     * @param listener
     *            The listener to add
     * @since 1.2
     */
    public void addTimeGraphEntryMenuListener(MenuDetectListener listener) {
        if (!fTimeGraphEntryMenuListeners.contains(listener)) {
            fTimeGraphEntryMenuListeners.add(listener);
        }
    }

    /**
     * Remove a menu listener on {@link ITimeGraphEntry}s
     *
     * @param listener
     *            The listener to remove
     * @since 1.2
     */
    public void removeTimeGraphEntryMenuListener(MenuDetectListener listener) {
        if (fTimeGraphEntryMenuListeners.contains(listener)) {
            fTimeGraphEntryMenuListeners.remove(listener);
        }
    }

    /**
     * Menu event callback on {@link ITimeGraphEntry}s
     *
     * @param event
     *            The MenuDetectEvent, with field {@link TypedEvent#data} set to the selected {@link ITimeGraphEntry}
     */
    private void fireMenuEventOnTimeGraphEntry(MenuDetectEvent event) {
        for (MenuDetectListener listener : fTimeGraphEntryMenuListeners) {
            listener.menuDetected(event);
        }
    }

    /**
     * Add a menu listener on {@link ITimeEvent}s
     *
     * @param listener
     *            The listener to add
     * @since 1.2
     */
    public void addTimeEventMenuListener(MenuDetectListener listener) {
        if (!fTimeEventMenuListeners.contains(listener)) {
            fTimeEventMenuListeners.add(listener);
        }
    }

    /**
     * Remove a menu listener on {@link ITimeEvent}s
     *
     * @param listener
     *            The listener to remove
     * @since 1.2
     */
    public void removeTimeEventMenuListener(MenuDetectListener listener) {
        if (fTimeEventMenuListeners.contains(listener)) {
            fTimeEventMenuListeners.remove(listener);
        }
    }

    /**
     * Menu event callback on {@link ITimeEvent}s
     *
     * @param event
     *            The MenuDetectEvent, with field {@link TypedEvent#data} set to the selected {@link ITimeEvent}
     */
    private void fireMenuEventOnTimeEvent(MenuDetectEvent event) {
        for (MenuDetectListener listener : fTimeEventMenuListeners) {
            listener.menuDetected(event);
        }
    }

    @Override
    public ISelection getSelection() {
        TimeGraphSelection sel = new TimeGraphSelection();
        ITimeGraphEntry trace = getSelectedTrace();
        if (null != trace && null != fTimeProvider) {
            long selectedTime = fTimeProvider.getSelectionBegin();
            ITimeEvent event = Utils.findEvent(trace, selectedTime, 0);
            if (event != null) {
                sel.add(event);
            } else {
                sel.add(trace);
            }
        }
        return sel;
    }

    /**
     * Get the selection object
     *
     * @return The selection
     */
    public ISelection getSelectionTrace() {
        TimeGraphSelection sel = new TimeGraphSelection();
        ITimeGraphEntry trace = getSelectedTrace();
        if (null != trace) {
            sel.add(trace);
        }
        return sel;
    }

    /**
     * Enable/disable one of the traces in the model
     *
     * @param n
     *            1 to enable it, -1 to disable. The method returns immediately
     *            if another value is used.
     */
    public void selectTrace(int n) {
        if ((n != 1) && (n != -1)) {
            return;
        }

        boolean changed = false;
        int lastSelection = -1;
        for (int i = 0; i < fItemData.fExpandedItems.length; i++) {
            Item item = fItemData.fExpandedItems[i];
            if (item.fSelected) {
                lastSelection = i;
                if ((1 == n) && (i < fItemData.fExpandedItems.length - 1)) {
                    item.fSelected = false;
                    item = fItemData.fExpandedItems[i + 1];
                    item.fSelected = true;
                    changed = true;
                } else if ((-1 == n) && (i > 0)) {
                    item.fSelected = false;
                    item = fItemData.fExpandedItems[i - 1];
                    item.fSelected = true;
                    changed = true;
                }
                break;
            }
        }

        if (lastSelection < 0 && fItemData.fExpandedItems.length > 0) {
            Item item = fItemData.fExpandedItems[0];
            item.fSelected = true;
            changed = true;
        }

        if (changed) {
            ensureVisibleItem(-1, false);
            redraw();
            fireSelectionChanged();
        }
    }

    /**
     * Select an event
     *
     * @param n
     *            1 for next event, -1 for previous event
     */
    public void selectEvent(int n) {
        if (null == fTimeProvider) {
            return;
        }
        ITimeGraphEntry trace = getSelectedTrace();
        if (trace == null) {
            return;
        }
        long selectedTime = fTimeProvider.getSelectionBegin();
        long endTime = fTimeProvider.getEndTime();
        ITimeEvent nextEvent;
        if (-1 == n && selectedTime > endTime) {
            nextEvent = Utils.findEvent(trace, selectedTime, 0);
        } else {
            nextEvent = Utils.findEvent(trace, selectedTime, n);
        }
        if (null == nextEvent && -1 == n) {
            nextEvent = Utils.getFirstEvent(trace);
        }
        if (null != nextEvent) {
            long nextTime = nextEvent.getTime();
            // If last event detected e.g. going back or not moving to a next
            // event
            if (nextTime <= selectedTime && n == 1) {
                // Select to the end of this last event
                nextTime = nextEvent.getTime() + nextEvent.getDuration();
                // but not beyond the end of the trace
                if (nextTime > endTime) {
                    nextTime = endTime;
                }
            } else if (n == -1 && nextEvent.getTime() + nextEvent.getDuration() < selectedTime) {
                // for previous event go to its end time unless we were already there
                nextTime = nextEvent.getTime() + nextEvent.getDuration();
            }
            fTimeProvider.setSelectedTimeNotify(nextTime, true);
            fireSelectionChanged();
        } else if (1 == n) {
            fTimeProvider.setSelectedTimeNotify(endTime, true);
            fireSelectionChanged();
        }
    }

    /**
     * Select the next event
     */
    public void selectNextEvent() {
        selectEvent(1);
        // Notify if visible time window has been adjusted
        fTimeProvider.setStartFinishTimeNotify(fTimeProvider.getTime0(), fTimeProvider.getTime1());
    }

    /**
     * Select the previous event
     */
    public void selectPrevEvent() {
        selectEvent(-1);
        // Notify if visible time window has been adjusted
        fTimeProvider.setStartFinishTimeNotify(fTimeProvider.getTime0(), fTimeProvider.getTime1());
    }

    /**
     * Select the next trace
     */
    public void selectNextTrace() {
        selectTrace(1);
    }

    /**
     * Select the previous trace
     */
    public void selectPrevTrace() {
        selectTrace(-1);
    }

    /**
     * Zoom based on mouse cursor location with mouse scrolling
     *
     * @param zoomIn true to zoom in, false to zoom out
     */
    public void zoom(boolean zoomIn) {
        int globalX = getDisplay().getCursorLocation().x;
        Point p = toControl(globalX, 0);
        int nameSpace = fTimeProvider.getNameSpace();
        int timeSpace = fTimeProvider.getTimeSpace();
        int xPos = Math.max(nameSpace, Math.min(nameSpace + timeSpace, p.x));
        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        long interval = time1 - time0;
        if (interval == 0) {
            interval = 1;
        } // to allow getting out of single point interval
        long newInterval;
        if (zoomIn) {
            newInterval = Math.max(Math.round(interval * ZOOM_IN_FACTOR), fTimeProvider.getMinTimeInterval());
        } else {
            newInterval = (long) Math.ceil(interval * ZOOM_OUT_FACTOR);
        }
        long center = time0 + Math.round(((double) (xPos - nameSpace) / timeSpace * interval));
        long newTime0 = center - Math.round((double) newInterval * (center - time0) / interval);
        long newTime1 = newTime0 + newInterval;
        fTimeProvider.setStartFinishTime(newTime0, newTime1);
        synchronized (fMouseScrollNotifierLock) {
            if (fMouseScrollNotifier == null) {
                fMouseScrollNotifier = new MouseScrollNotifier();
                fMouseScrollNotifier.start();
            }
            fMouseScrollNotifier.mouseScrolled();
        }
    }

    /**
     * zoom in using single click
     */
    public void zoomIn() {
        long prevTime0 = fTimeProvider.getTime0();
        long prevTime1 = fTimeProvider.getTime1();
        long prevRange = prevTime1 - prevTime0;
        if (prevRange == 0) {
            return;
        }
        ITimeDataProvider provider = fTimeProvider;
        long selTime = (provider.getSelectionEnd() + provider.getSelectionBegin()) / 2;
        if (selTime <= prevTime0 || selTime >= prevTime1) {
            selTime = (prevTime0 + prevTime1) / 2;
        }
        long time0 = selTime - (long) ((selTime - prevTime0) / ZOOM_FACTOR);
        long time1 = selTime + (long) ((prevTime1 - selTime) / ZOOM_FACTOR);

        long inaccuracy = (fTimeProvider.getMaxTime() - fTimeProvider.getMinTime()) - (time1 - time0);

        if (inaccuracy > 0 && inaccuracy < 100) {
            fTimeProvider.setStartFinishTimeNotify(fTimeProvider.getMinTime(), fTimeProvider.getMaxTime());
            return;
        }

        long min = fTimeProvider.getMinTimeInterval();
        if ((time1 - time0) < min) {
            time0 = selTime - (selTime - prevTime0) * min / prevRange;
            time1 = time0 + min;
        }

        fTimeProvider.setStartFinishTimeNotify(time0, time1);
    }

    /**
     * zoom out using single click
     */
    public void zoomOut() {
        long prevTime0 = fTimeProvider.getTime0();
        long prevTime1 = fTimeProvider.getTime1();
        ITimeDataProvider provider = fTimeProvider;
        long selTime = (provider.getSelectionEnd() + provider.getSelectionBegin()) / 2;
        if (selTime <= prevTime0 || selTime >= prevTime1) {
            selTime = (prevTime0 + prevTime1) / 2;
        }
        long time0 = (long) (selTime - (selTime - prevTime0) * ZOOM_FACTOR);
        long time1 = (long) (selTime + (prevTime1 - selTime) * ZOOM_FACTOR);

        long inaccuracy = (fTimeProvider.getMaxTime() - fTimeProvider.getMinTime()) - (time1 - time0);
        if (inaccuracy > 0 && inaccuracy < 100) {
            fTimeProvider.setStartFinishTimeNotify(fTimeProvider.getMinTime(), fTimeProvider.getMaxTime());
            return;
        }

        fTimeProvider.setStartFinishTimeNotify(time0, time1);
    }

    /**
     * Hide arrows
     *
     * @param hideArrows true to hide arrows
     *
     * @since 2.1
     */
    public void hideArrows(boolean hideArrows) {
        fHideArrows = hideArrows;
    }

    /**
     * Follow the arrow forward
     *
     * @since 2.1
     */
    public void followArrowFwd() {
        ITimeGraphEntry trace = getSelectedTrace();
        if (trace == null) {
            return;
        }
        long selectedTime = fTimeProvider.getSelectionBegin();
        for (ILinkEvent link : fItemData.fLinks) {
            if (link.getEntry() == trace && link.getTime() == selectedTime) {
                selectItem(link.getDestinationEntry(), false);
                if (link.getDuration() != 0) {
                    fTimeProvider.setSelectedTimeNotify(link.getTime() + link.getDuration(), true);
                    // Notify if visible time window has been adjusted
                    fTimeProvider.setStartFinishTimeNotify(fTimeProvider.getTime0(), fTimeProvider.getTime1());
                }
                fireSelectionChanged();
                return;
            }
        }
        selectNextEvent();
    }

    /**
     * Follow the arrow backward
     *
     * @since 2.1
     */
    public void followArrowBwd() {
        ITimeGraphEntry trace = getSelectedTrace();
        if (trace == null) {
            return;
        }
        long selectedTime = fTimeProvider.getSelectionBegin();
        for (ILinkEvent link : fItemData.fLinks) {
            if (link.getDestinationEntry() == trace && link.getTime() + link.getDuration() == selectedTime) {
                selectItem(link.getEntry(), false);
                if (link.getDuration() != 0) {
                    fTimeProvider.setSelectedTimeNotify(link.getTime(), true);
                    // Notify if visible time window has been adjusted
                    fTimeProvider.setStartFinishTimeNotify(fTimeProvider.getTime0(), fTimeProvider.getTime1());
                }
                fireSelectionChanged();
                return;
            }
        }
        selectPrevEvent();
    }

    /**
     * Return the currently selected trace
     *
     * @return The entry matching the trace
     */
    public ITimeGraphEntry getSelectedTrace() {
        ITimeGraphEntry trace = null;
        int idx = getSelectedIndex();
        if (idx >= 0) {
            trace = fItemData.fExpandedItems[idx].fEntry;
        }
        return trace;
    }

    /**
     * Retrieve the index of the currently selected item
     *
     * @return The index
     */
    public int getSelectedIndex() {
        int idx = -1;
        for (int i = 0; i < fItemData.fExpandedItems.length; i++) {
            Item item = fItemData.fExpandedItems[i];
            if (item.fSelected) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    boolean toggle(int idx) {
        boolean toggled = false;
        if (idx >= 0 && idx < fItemData.fExpandedItems.length) {
            Item item = fItemData.fExpandedItems[idx];
            if (item.fHasChildren) {
                item.fExpanded = !item.fExpanded;
                fItemData.updateExpandedItems();
                adjustScrolls();
                redraw();
                toggled = true;
                fireTreeEvent(item.fEntry, item.fExpanded);
            }
        }
        return toggled;
    }

    /**
     * Gets the index of the item at the given location.
     *
     * @param y
     *            the y coordinate
     * @return the index of the item at the given location, of -1 if none.
     * @since 3.0
     */
    protected int getItemIndexAtY(int y) {
        if (y < 0) {
            return -1;
        }
        int ySum = 0;
        for (int idx = fTopIndex; idx < fItemData.fExpandedItems.length; idx++) {
            ySum += fItemData.fExpandedItems[idx].fItemHeight;
            if (y < ySum) {
                return idx;
            }
        }
        return -1;
    }

    boolean isOverSplitLine(int x) {
        if (x < 0 || null == fTimeProvider) {
            return false;
        }
        int nameWidth = fTimeProvider.getNameSpace();
        return Math.abs(x - nameWidth) < SNAP_WIDTH;
    }

    /**
     * Gets the {@link ITimeGraphEntry} at the given location.
     *
     * @param pt
     *            a point in the widget
     * @return the {@link ITimeGraphEntry} at this point, or <code>null</code>
     *         if none.
     * @since 3.0
     */
    protected ITimeGraphEntry getEntry(Point pt) {
        int idx = getItemIndexAtY(pt.y);
        return idx >= 0 ? fItemData.fExpandedItems[idx].fEntry : null;
    }

    /**
     * Return the arrow event closest to the given point that is no further than
     * a maximum distance.
     *
     * @param pt
     *            a point in the widget
     * @return The closest arrow event, or null if there is none close enough.
     * @since 3.1
     */
    protected ILinkEvent getArrow(Point pt) {
        if (fHideArrows) {
            return null;
        }
        ILinkEvent linkEvent = null;
        double minDistance = Double.MAX_VALUE;
        for (ILinkEvent event : fItemData.fLinks) {
            Rectangle rect = getArrowRectangle(new Rectangle(0, 0, 0, 0), event);
            if (rect != null) {
                int x1 = rect.x;
                int y1 = rect.y;
                int x2 = x1 + rect.width;
                int y2 = y1 + rect.height;
                double d = Utils.distance(pt.x, pt.y, x1, y1, x2, y2);
                if (minDistance > d) {
                    minDistance = d;
                    linkEvent = event;
                }
            }
        }
        if (minDistance <= ARROW_HOVER_MAX_DIST) {
            return linkEvent;
        }
        return null;
    }

    /**
     * @since 2.0
     */
    @Override
    public int getXForTime(long time) {
        if (null == fTimeProvider) {
            return -1;
        }
        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        int width = getCtrlSize().x;
        int nameSpace = fTimeProvider.getNameSpace();
        double pixelsPerNanoSec = (width - nameSpace <= RIGHT_MARGIN) ? 0 : (double) (width - nameSpace - RIGHT_MARGIN) / (time1 - time0);
        int x = getBounds().x + nameSpace + (int) ((time - time0) * pixelsPerNanoSec);
        return x;
    }

    /**
     * @since 2.0
     */
    @Override
    public long getTimeAtX(int coord) {
        if (null == fTimeProvider) {
            return -1;
        }
        long hitTime = -1;
        Point size = getCtrlSize();
        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        int nameWidth = fTimeProvider.getNameSpace();
        final int x = coord - nameWidth;
        int timeWidth = size.x - nameWidth - RIGHT_MARGIN;
        if (x >= 0 && size.x >= nameWidth) {
            if (time1 - time0 > timeWidth) {
                // nanosecond smaller than one pixel: use the first integer nanosecond of this pixel's time range
                hitTime = time0 + (long) Math.ceil((time1 - time0) * ((double) x / timeWidth));
            } else {
                // nanosecond greater than one pixel: use the nanosecond that covers this pixel start position
                hitTime = time0 + (long) Math.floor((time1 - time0) * ((double) x / timeWidth));
            }
        }
        return hitTime;
    }

    void selectItem(int idx, boolean addSelection) {
        boolean changed = false;
        if (addSelection) {
            if (idx >= 0 && idx < fItemData.fExpandedItems.length) {
                Item item = fItemData.fExpandedItems[idx];
                changed = !item.fSelected;
                item.fSelected = true;
            }
        } else {
            for (int i = 0; i < fItemData.fExpandedItems.length; i++) {
                Item item = fItemData.fExpandedItems[i];
                if ((i == idx && !item.fSelected) || (idx == -1 && item.fSelected)) {
                    changed = true;
                }
                item.fSelected = i == idx;
            }
        }
        changed |= ensureVisibleItem(idx, true);
        if (changed) {
            redraw();
        }
    }

    /**
     * Callback for item selection
     *
     * @param trace
     *            The entry matching the trace
     * @param addSelection
     *            If the selection is added or removed
     */
    public void selectItem(ITimeGraphEntry trace, boolean addSelection) {
        int idx = fItemData.findItemIndex(trace);
        selectItem(idx, addSelection);
    }

    /**
     * Retrieve the number of entries shown per page.
     *
     * @return The count
     */
    public int countPerPage() {
        int height = getCtrlSize().y;
        int count = 0;
        int ySum = 0;
        for (int idx = fTopIndex; idx < fItemData.fExpandedItems.length; idx++) {
            ySum += fItemData.fExpandedItems[idx].fItemHeight;
            if (ySum >= height) {
                return count;
            }
            count++;
        }
        for (int idx = fTopIndex - 1; idx >= 0; idx--) {
            ySum += fItemData.fExpandedItems[idx].fItemHeight;
            if (ySum >= height) {
                return count;
            }
            count++;
        }
        return count;
    }

    /**
     * Get the index of the top element
     *
     * @return The index
     */
    public int getTopIndex() {
        return fTopIndex;
    }

    /**
     * Get the number of expanded items
     *
     * @return The count of expanded items
     */
    public int getExpandedElementCount() {
        return fItemData.fExpandedItems.length;
    }

    /**
     * Get an array of all expanded elements
     *
     * @return The expanded elements
     */
    public ITimeGraphEntry[] getExpandedElements() {
        ArrayList<ITimeGraphEntry> elements = new ArrayList<>();
        for (Item item : fItemData.fExpandedItems) {
            elements.add(item.fEntry);
        }
        return elements.toArray(new ITimeGraphEntry[0]);
    }

    Point getCtrlSize() {
        Point size = getSize();
        if (getHorizontalBar().isVisible()) {
            size.y -= getHorizontalBar().getSize().y;
        }
        return size;
    }

    Rectangle getNameRect(Rectangle bound, int idx, int nameWidth) {
        Rectangle rect = getStatesRect(bound, idx, nameWidth);
        rect.x = bound.x;
        rect.width = nameWidth;
        return rect;
    }

    Rectangle getStatesRect(Rectangle bound, int idx, int nameWidth) {
        int x = bound.x + nameWidth;
        int width = bound.width - x;
        int ySum = 0;
        if (idx >= fTopIndex) {
            for (int i = fTopIndex; i < idx; i++) {
                ySum += fItemData.fExpandedItems[i].fItemHeight;
            }
        } else {
            for (int i = fTopIndex - 1; i >= idx; i--) {
                ySum -= fItemData.fExpandedItems[i].fItemHeight;
            }
        }
        int y = bound.y + ySum;
        int height = fItemData.fExpandedItems[idx].fItemHeight;
        return new Rectangle(x, y, width, height);
    }

    @Override
    void paint(Rectangle bounds, PaintEvent e) {
        GC gc = e.gc;
        gc.setBackground(getColorScheme().getColor(TimeGraphColorScheme.BACKGROUND));
        drawBackground(gc, bounds.x, bounds.y, bounds.width, bounds.height);

        if (bounds.width < 2 || bounds.height < 2 || null == fTimeProvider) {
            return;
        }

        fIdealNameSpace = 0;
        int nameSpace = fTimeProvider.getNameSpace();

        // draw empty name space background
        gc.setBackground(getColorScheme().getBkColor(false, false, true));
        drawBackground(gc, bounds.x, bounds.y, nameSpace, bounds.height);

        // draw items
        drawItems(bounds, fTimeProvider, fItemData.fExpandedItems, fTopIndex, nameSpace, gc);
        drawLinks(bounds, fTimeProvider, fItemData.fLinks, nameSpace, gc);
        fTimeGraphProvider.postDrawControl(bounds, gc);

        int alpha = gc.getAlpha();
        gc.setAlpha(100);

        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        long selectionBegin = fTimeProvider.getSelectionBegin();
        long selectionEnd = fTimeProvider.getSelectionEnd();
        double pixelsPerNanoSec = (bounds.width - nameSpace <= RIGHT_MARGIN) ? 0 : (double) (bounds.width - nameSpace - RIGHT_MARGIN) / (time1 - time0);
        int x0 = bounds.x + nameSpace + (int) ((selectionBegin - time0) * pixelsPerNanoSec);
        int x1 = bounds.x + nameSpace + (int) ((selectionEnd - time0) * pixelsPerNanoSec);

        // draw selection lines
        if (fDragState != DRAG_SELECTION) {
            gc.setForeground(getColorScheme().getColor(TimeGraphColorScheme.SELECTED_TIME));
            if (x0 >= nameSpace && x0 < bounds.x + bounds.width) {
                gc.drawLine(x0, bounds.y, x0, bounds.y + bounds.height);
            }
            if (x1 != x0) {
                if (x1 >= nameSpace && x1 < bounds.x + bounds.width) {
                    gc.drawLine(x1, bounds.y, x1, bounds.y + bounds.height);
                }
            }
        }

        // draw selection background
        if (selectionBegin != 0 && selectionEnd != 0 && fDragState != DRAG_SELECTION) {
            x0 = Math.max(nameSpace, Math.min(bounds.x + bounds.width, x0));
            x1 = Math.max(nameSpace, Math.min(bounds.x + bounds.width, x1));
            gc.setBackground(getColorScheme().getBkColor(false, false, true));
            if (x1 - x0 > 1) {
                gc.fillRectangle(new Rectangle(x0 + 1, bounds.y, x1 - x0 - 1, bounds.height));
            } else if (x0 - x1 > 1) {
                gc.fillRectangle(new Rectangle(x1 + 1, bounds.y, x0 - x1 - 1, bounds.height));
            }
        }

        // draw drag selection background
        if (fDragState == DRAG_ZOOM || fDragState == DRAG_SELECTION) {
            gc.setBackground(getColorScheme().getBkColor(false, false, true));
            if (fDragX0 < fDragX) {
                gc.fillRectangle(new Rectangle(fDragX0, bounds.y, fDragX - fDragX0, bounds.height));
            } else if (fDragX0 > fDragX) {
                gc.fillRectangle(new Rectangle(fDragX, bounds.y, fDragX0 - fDragX, bounds.height));
            }
        }

        // draw drag line
        if (DRAG_SPLIT_LINE == fDragState) {
            gc.setForeground(getColorScheme().getColor(TimeGraphColorScheme.BLACK));
            gc.drawLine(bounds.x + nameSpace, bounds.y, bounds.x + nameSpace, bounds.y + bounds.height - 1);
        } else if (DRAG_ZOOM == fDragState && Math.max(fDragX, fDragX0) > nameSpace) {
            gc.setForeground(getColorScheme().getColor(TimeGraphColorScheme.TOOL_FOREGROUND));
            gc.drawLine(fDragX0, bounds.y, fDragX0, bounds.y + bounds.height - 1);
            if (fDragX != fDragX0) {
                gc.drawLine(fDragX, bounds.y, fDragX, bounds.y + bounds.height - 1);
            }
        } else if (DRAG_SELECTION == fDragState && Math.max(fDragX, fDragX0) > nameSpace) {
            gc.setForeground(getColorScheme().getColor(TimeGraphColorScheme.SELECTED_TIME));
            gc.drawLine(fDragX0, bounds.y, fDragX0, bounds.y + bounds.height - 1);
            if (fDragX != fDragX0) {
                gc.drawLine(fDragX, bounds.y, fDragX, bounds.y + bounds.height - 1);
            }
        } else if (DRAG_NONE == fDragState && fMouseOverSplitLine && fTimeProvider.getNameSpace() > 0) {
            gc.setForeground(getColorScheme().getColor(TimeGraphColorScheme.RED));
            gc.drawLine(bounds.x + nameSpace, bounds.y, bounds.x + nameSpace, bounds.y + bounds.height - 1);
        }

        gc.setAlpha(alpha);
    }

    /**
     * Draw many items at once
     *
     * @param bounds
     *            The rectangle of the area
     * @param timeProvider
     *            The time provider
     * @param items
     *            The array items to draw
     * @param topIndex
     *            The index of the first element to draw
     * @param nameSpace
     *            The width reserved for the names
     * @param gc
     *            Reference to the SWT GC object
     */
    public void drawItems(Rectangle bounds, ITimeDataProvider timeProvider,
            Item[] items, int topIndex, int nameSpace, GC gc) {
        for (int i = topIndex; i < items.length; i++) {
            Item item = items[i];
            drawItem(item, bounds, timeProvider, i, nameSpace, gc);
        }
    }

    /**
     * Draws the item
     *
     * @param item the item to draw
     * @param bounds the container rectangle
     * @param timeProvider Time provider
     * @param i the item index
     * @param nameSpace the name space
     * @param gc Graphics context
     */
    protected void drawItem(Item item, Rectangle bounds, ITimeDataProvider timeProvider, int i, int nameSpace, GC gc) {
        ITimeGraphEntry entry = item.fEntry;
        long time0 = timeProvider.getTime0();
        long time1 = timeProvider.getTime1();
        long selectedTime = fTimeProvider.getSelectionBegin();

        Rectangle nameRect = getNameRect(bounds, i, nameSpace);
        if (nameRect.y >= bounds.y + bounds.height) {
            return;
        }

        if (! item.fEntry.hasTimeEvents()) {
            Rectangle statesRect = getStatesRect(bounds, i, nameSpace);
            nameRect.width += statesRect.width;
            drawName(item, nameRect, gc);
        } else {
            drawName(item, nameRect, gc);
        }
        Rectangle rect = getStatesRect(bounds, i, nameSpace);
        if (rect.isEmpty()) {
            fTimeGraphProvider.postDrawEntry(entry, rect, gc);
            return;
        }
        if (time1 <= time0) {
            gc.setBackground(getColorScheme().getBkColor(false, false, false));
            gc.fillRectangle(rect);
            fTimeGraphProvider.postDrawEntry(entry, rect, gc);
            return;
        }

        // Initialize _rect1 to same values as enclosing rectangle rect
        Rectangle stateRect = Utils.clone(rect);
        boolean selected = item.fSelected;
        // K pixels per second
        double pixelsPerNanoSec = (rect.width <= RIGHT_MARGIN) ? 0 : (double) (rect.width - RIGHT_MARGIN) / (time1 - time0);

        if (item.fEntry.hasTimeEvents()) {
            gc.setClipping(new Rectangle(nameSpace, 0, bounds.width - nameSpace, bounds.height));
            fillSpace(rect, gc, selected);
            // Drawing rectangle is smaller than reserved space
            stateRect.y += 3;
            stateRect.height -= 6;

            long maxDuration = (timeProvider.getTimeSpace() == 0) ? Long.MAX_VALUE : 1 * (time1 - time0) / timeProvider.getTimeSpace();
            Iterator<ITimeEvent> iterator = entry.getTimeEventsIterator(time0, time1, maxDuration);

            int lastX = -1;
            while (iterator.hasNext()) {
                ITimeEvent event = iterator.next();
                int x = rect.x + (int) ((event.getTime() - time0) * pixelsPerNanoSec);
                int xEnd = rect.x + (int) ((event.getTime() + event.getDuration() - time0) * pixelsPerNanoSec);
                if (x >= rect.x + rect.width || xEnd < rect.x) {
                    // event is out of bounds
                    continue;
                }
                xEnd = Math.min(rect.x + rect.width, xEnd);
                stateRect.x = Math.max(rect.x, x);
                stateRect.width = Math.max(0, xEnd - stateRect.x + 1);
                if (stateRect.x == lastX) {
                    stateRect.width -= 1;
                    if (stateRect.width > 0) {
                        gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                        gc.drawPoint(stateRect.x, stateRect.y - 2);
                        stateRect.x += 1;
                    }
                }
                boolean timeSelected = selectedTime >= event.getTime() && selectedTime < event.getTime() + event.getDuration();
                if (drawState(getColorScheme(), event, stateRect, gc, selected, timeSelected)) {
                    lastX = x;
                }
            }
            gc.setClipping((Rectangle) null);
        }
        fTimeGraphProvider.postDrawEntry(entry, rect, gc);
    }

    /**
     * Draw the links
     *
     * @param bounds
     *            The rectangle of the area
     * @param timeProvider
     *            The time provider
     * @param links
     *            The array items to draw
     * @param nameSpace
     *            The width reserved for the names
     * @param gc
     *            Reference to the SWT GC object
     * @since 2.1
     */
    public void drawLinks(Rectangle bounds, ITimeDataProvider timeProvider,
            List<ILinkEvent> links, int nameSpace, GC gc) {
        if (fHideArrows) {
            return;
        }
        gc.setClipping(new Rectangle(nameSpace, 0, bounds.width - nameSpace, bounds.height));
        for (ILinkEvent event : links) {
            drawLink(event, bounds, timeProvider, nameSpace, gc);
        }
        gc.setClipping((Rectangle) null);
    }

    /**
     * Draws the link type events of this item
     *
     * @param event
     *            the item to draw
     * @param bounds
     *            the container rectangle
     * @param timeProvider
     *            Time provider
     * @param nameSpace
     *            the name space
     * @param gc
     *            Graphics context
     * @since 2.1
     */
    protected void drawLink(ILinkEvent event, Rectangle bounds, ITimeDataProvider timeProvider, int nameSpace, GC gc) {
        drawArrow(getColorScheme(), event, getArrowRectangle(bounds, event), gc);
    }

    private Rectangle getArrowRectangle(Rectangle bounds, ILinkEvent event) {
        int srcIndex = fItemData.findItemIndex(event.getEntry());
        int destIndex = fItemData.findItemIndex(event.getDestinationEntry());

        if ((srcIndex == -1) || (destIndex == -1)) {
            return null;
        }

        Rectangle src = getStatesRect(bounds, srcIndex, fTimeProvider.getNameSpace());
        Rectangle dst = getStatesRect(bounds, destIndex, fTimeProvider.getNameSpace());

        int x0 = getXForTime(event.getTime());
        int x1 = getXForTime(event.getTime() + event.getDuration());

        // limit the x-coordinates to prevent integer overflow in calculations
        // and also GC.drawLine doesn't draw properly with large coordinates
        final int limit = Integer.MAX_VALUE / 1024;
        x0 = Math.max(-limit, Math.min(x0, limit));
        x1 = Math.max(-limit, Math.min(x1, limit));

        int y0 = src.y + src.height / 2;
        int y1 = dst.y + dst.height / 2;
        return new Rectangle(x0, y0, x1 - x0, y1 - y0);
    }

    /**
     * Draw the state (color fill)
     *
     * @param colors
     *            Color scheme
     * @param event
     *            Time event for which we're drawing the state
     * @param rect
     *            Where to draw
     * @param gc
     *            Graphics context
     * @return true if the state was drawn
     * @since 2.1
     */
    protected boolean drawArrow(TimeGraphColorScheme colors, ITimeEvent event,
            Rectangle rect, GC gc) {

        int colorIdx = fTimeGraphProvider.getStateTableIndex(event);
        if (colorIdx < 0) {
            return false;
        }
        boolean visible = ((rect.height == 0) && (rect.width == 0)) ? false : true;

        if (visible) {
            Color stateColor = null;
            if (colorIdx < fEventColorMap.length) {
                stateColor = fEventColorMap[colorIdx];
            } else {
                stateColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
            }

            gc.setForeground(stateColor);
            gc.setBackground(stateColor);

            /* Draw the arrow */
            gc.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
            drawArrowHead(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, gc);

        }
        fTimeGraphProvider.postDrawEvent(event, rect, gc);
        return visible;
    }

    /*
     * @author Francis Giraldeau
     *
     * Inspiration:
     * http://stackoverflow.com/questions/3010803/draw-arrow-on-line-algorithm
     *
     * The algorithm was taken from this site, not the code itself
     */
    private static void drawArrowHead(int x0, int y0, int x1, int y1, GC gc)
    {
        int factor = 10;
        double cos = 0.9510;
        double sin = 0.3090;
        long lenx = x1 - x0;
        long leny = y1 - y0;
        double len = Math.sqrt(lenx * lenx + leny * leny);

        double dx = factor * lenx / len;
        double dy = factor * leny / len;
        int end1X = (int) Math.round((x1 - (dx * cos + dy * -sin)));
        int end1Y = (int) Math.round((y1 - (dx * sin + dy * cos)));
        int end2X = (int) Math.round((x1 - (dx * cos + dy * sin)));
        int end2Y = (int) Math.round((y1 - (dx * -sin + dy * cos)));
        int[] arrow = new int[] { x1, y1, end1X, end1Y, end2X, end2Y, x1, y1 };
        gc.fillPolygon(arrow);
    }

    /**
     * Draw the name of an item.
     *
     * @param item
     *            Item object
     * @param bounds
     *            Where to draw the name
     * @param gc
     *            Graphics context
     */
    protected void drawName(Item item, Rectangle bounds, GC gc) {
        boolean hasTimeEvents = item.fEntry.hasTimeEvents();
        if (! hasTimeEvents) {
            gc.setBackground(getColorScheme().getBkColorGroup(item.fSelected, fIsInFocus));
            gc.fillRectangle(bounds);
            if (item.fSelected && fIsInFocus) {
                gc.setForeground(getColorScheme().getBkColor(item.fSelected, fIsInFocus, false));
                gc.drawRectangle(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
            }
        } else {
            gc.setBackground(getColorScheme().getBkColor(item.fSelected, fIsInFocus, true));
            gc.setForeground(getColorScheme().getFgColor(item.fSelected, fIsInFocus));
            gc.fillRectangle(bounds);
        }

        // No name to be drawn
        if (fTimeProvider.getNameSpace() == 0) {
            return;
        }

        int leftMargin = MARGIN + item.fLevel * EXPAND_SIZE;
        if (item.fHasChildren) {
            gc.setForeground(getColorScheme().getFgColorGroup(false, false));
            gc.setBackground(getColorScheme().getBkColor(false, false, false));
            Rectangle rect = Utils.clone(bounds);
            rect.x += leftMargin;
            rect.y += (bounds.height - EXPAND_SIZE) / 2;
            rect.width = EXPAND_SIZE;
            rect.height = EXPAND_SIZE;
            gc.fillRectangle(rect);
            gc.drawRectangle(rect.x, rect.y, rect.width - 1, rect.height - 1);
            int midy = rect.y + rect.height / 2;
            gc.drawLine(rect.x + 2, midy, rect.x + rect.width - 3, midy);
            if (!item.fExpanded) {
                int midx = rect.x + rect.width / 2;
                gc.drawLine(midx, rect.y + 2, midx, rect.y + rect.height - 3);
            }
        }
        leftMargin += EXPAND_SIZE + MARGIN;

        Image img = fTimeGraphProvider.getItemImage(item.fEntry);
        if (img != null) {
            // draw icon
            int imgHeight = img.getImageData().height;
            int imgWidth = img.getImageData().width;
            int x = leftMargin;
            int y = bounds.y + (bounds.height - imgHeight) / 2;
            gc.drawImage(img, x, y);
            leftMargin += imgWidth + MARGIN;
        }
        String name = item.fName;
        Point size = gc.stringExtent(name);
        if (fIdealNameSpace < leftMargin + size.x + MARGIN) {
            fIdealNameSpace = leftMargin + size.x + MARGIN;
        }
        if (hasTimeEvents) {
            // cut long string with "..."
            int width = bounds.width - leftMargin;
            int cuts = 0;
            while (size.x > width && name.length() > 1) {
                cuts++;
                name = name.substring(0, name.length() - 1);
                size = gc.stringExtent(name + "..."); //$NON-NLS-1$
            }
            if (cuts > 0) {
                name += "..."; //$NON-NLS-1$
            }
        }
        Rectangle rect = Utils.clone(bounds);
        rect.x += leftMargin;
        rect.width -= leftMargin;
        // draw text
        if (rect.width > 0) {
            rect.y += (bounds.height - gc.stringExtent(name).y) / 2;
            gc.setForeground(getColorScheme().getFgColor(item.fSelected, fIsInFocus));
            int textWidth = Utils.drawText(gc, name, rect, true);
            leftMargin += textWidth + MARGIN;
            rect.y -= 2;

            if (hasTimeEvents) {
                // draw middle line
                int x = bounds.x + leftMargin;
                int width = bounds.width - x;
                int midy = bounds.y + bounds.height / 2;
                gc.setForeground(getColorScheme().getColor(TimeGraphColorScheme.MID_LINE));
                gc.drawLine(x, midy, x + width, midy);
            }
        }
    }

    /**
     * Draw the state (color fill)
     *
     * @param colors
     *            Color scheme
     * @param event
     *            Time event for which we're drawing the state
     * @param rect
     *            Where to draw
     * @param gc
     *            Graphics context
     * @param selected
     *            Is this time event currently selected (so it appears
     *            highlighted)
     * @param timeSelected
     *            Is the timestamp currently selected
     * @return true if the state was drawn
     * @since 2.0
     */
    protected boolean drawState(TimeGraphColorScheme colors, ITimeEvent event,
            Rectangle rect, GC gc, boolean selected, boolean timeSelected) {

        int colorIdx = fTimeGraphProvider.getStateTableIndex(event);
        if (colorIdx < 0 && colorIdx != ITimeGraphPresentationProvider.TRANSPARENT) {
            return false;
        }
        boolean visible = rect.width == 0 ? false : true;
        Color black =  Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
        gc.setForeground(black);

        if (visible) {
            if (colorIdx == ITimeGraphPresentationProvider.TRANSPARENT) {
                // Only draw the top and bottom borders
                gc.drawLine(rect.x, rect.y, rect.x + rect.width - 1, rect.y);
                gc.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width - 1, rect.y + rect.height - 1);
                if (rect.width == 1) {
                    gc.drawPoint(rect.x, rect.y - 2);
                }
                return false;
            }
            Color stateColor = null;
            if (colorIdx < fEventColorMap.length) {
                stateColor = fEventColorMap[colorIdx];
            } else {
                stateColor = black;
            }

            boolean reallySelected = timeSelected && selected;
            // fill all rect area
            gc.setBackground(stateColor);
            gc.fillRectangle(rect);

            if (reallySelected) {
                gc.drawLine(rect.x, rect.y - 1, rect.x + rect.width - 1, rect.y - 1);
                gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width - 1, rect.y + rect.height);
            }
        } else {
            gc.drawPoint(rect.x, rect.y - 2);
        }
        fTimeGraphProvider.postDrawEvent(event, rect, gc);
        return visible;
    }

    /**
     * Fill the space between two contiguous time events
     *
     * @param rect
     *            Rectangle to fill
     * @param gc
     *            Graphics context
     * @param selected
     *            Is this time event selected or not
     */
    protected void fillSpace(Rectangle rect, GC gc, boolean selected) {
        gc.setBackground(getColorScheme().getBkColor(selected, fIsInFocus, false));
        gc.fillRectangle(rect);
        if (fDragState == DRAG_ZOOM) {
            gc.setBackground(getColorScheme().getBkColor(selected, fIsInFocus, true));
            if (fDragX0 < fDragX) {
                gc.fillRectangle(new Rectangle(fDragX0, rect.y, fDragX - fDragX0, rect.height));
            } else if (fDragX0 > fDragX) {
                gc.fillRectangle(new Rectangle(fDragX, rect.y, fDragX0 - fDragX, rect.height));
            }
        }
        // draw middle line
        gc.setForeground(getColorScheme().getColor(TimeGraphColorScheme.MID_LINE));
        int midy = rect.y + rect.height / 2;
        gc.drawLine(rect.x, midy, rect.x + rect.width, midy);
    }

    @Override
    public void keyTraversed(TraverseEvent e) {
        if ((e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)) {
            e.doit = true;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int idx = -1;
        if (fItemData.fExpandedItems.length == 0) {
            return;
        }
        if (SWT.HOME == e.keyCode) {
            idx = 0;
        } else if (SWT.END == e.keyCode) {
            idx = fItemData.fExpandedItems.length - 1;
        } else if (SWT.ARROW_DOWN == e.keyCode) {
            idx = getSelectedIndex();
            if (idx < 0) {
                idx = 0;
            } else if (idx < fItemData.fExpandedItems.length - 1) {
                idx++;
            }
        } else if (SWT.ARROW_UP == e.keyCode) {
            idx = getSelectedIndex();
            if (idx < 0) {
                idx = 0;
            } else if (idx > 0) {
                idx--;
            }
        } else if (SWT.ARROW_LEFT == e.keyCode) {
            selectPrevEvent();
        } else if (SWT.ARROW_RIGHT == e.keyCode) {
            selectNextEvent();
        } else if (SWT.PAGE_DOWN == e.keyCode) {
            int page = countPerPage();
            idx = getSelectedIndex();
            if (idx < 0) {
                idx = 0;
            }
            idx += page;
            if (idx >= fItemData.fExpandedItems.length) {
                idx = fItemData.fExpandedItems.length - 1;
            }
        } else if (SWT.PAGE_UP == e.keyCode) {
            int page = countPerPage();
            idx = getSelectedIndex();
            if (idx < 0) {
                idx = 0;
            }
            idx -= page;
            if (idx < 0) {
                idx = 0;
            }
        } else if (SWT.CR == e.keyCode) {
            idx = getSelectedIndex();
            if (idx >= 0) {
                if (fItemData.fExpandedItems[idx].fHasChildren) {
                    toggle(idx);
                } else {
                    fireDefaultSelection();
                }
            }
            idx = -1;
        }
        if (idx >= 0) {
            selectItem(idx, false);
            fireSelectionChanged();
        }
        int x = toControl(e.display.getCursorLocation()).x;
        updateCursor(x, e.stateMask | e.keyCode);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int x = toControl(e.display.getCursorLocation()).x;
        updateCursor(x, e.stateMask & ~e.keyCode);
    }

    @Override
    public void focusGained(FocusEvent e) {
        fIsInFocus = true;
        if (fMouseScrollFilterListener == null) {
            fMouseScrollFilterListener = new Listener() {
                // This filter is used to prevent horizontal scrolling of the view
                // when the mouse wheel is used to zoom
                @Override
                public void handleEvent(Event event) {
                    event.doit = false;
                }
            };
            getDisplay().addFilter(SWT.MouseWheel, fMouseScrollFilterListener);
        }
        redraw();
        updateStatusLine(NO_STATUS);
    }

    @Override
    public void focusLost(FocusEvent e) {
        fIsInFocus = false;
        if (fMouseScrollFilterListener != null) {
            getDisplay().removeFilter(SWT.MouseWheel, fMouseScrollFilterListener);
            fMouseScrollFilterListener = null;
        }
        if (DRAG_NONE != fDragState) {
            setCapture(false);
            fDragState = DRAG_NONE;
        }
        redraw();
        updateStatusLine(NO_STATUS);
    }

    /**
     * @return If the current view is focused
     */
    public boolean isInFocus() {
        return fIsInFocus;
    }

    /**
     * Provide the possibility to control the wait cursor externally e.g. data
     * requests in progress
     *
     * @param waitInd Should we wait indefinitely?
     */
    public void waitCursor(boolean waitInd) {
        // Update cursor as indicated
        if (waitInd) {
            setCursor(fWaitCursor);
        } else {
            setCursor(null);
        }
    }

    private void updateCursor(int x, int stateMask) {
        // if Wait cursor not active, check for the need to change the cursor
        if (getCursor() == fWaitCursor) {
            return;
        }
        Cursor cursor = null;
        if (fDragState == DRAG_SPLIT_LINE) {
        } else if (fDragState == DRAG_SELECTION) {
            cursor = fResizeCursor;
        } else if (fDragState == DRAG_TRACE_ITEM) {
            cursor = fDragCursor;
        } else if (fDragState == DRAG_ZOOM) {
            cursor = fZoomCursor;
        } else if ((stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
            cursor = fDragCursor;
        } else if ((stateMask & SWT.MODIFIER_MASK) == SWT.SHIFT) {
            cursor = fResizeCursor;
        } else if (!isOverSplitLine(x)) {
            long selectionBegin = fTimeProvider.getSelectionBegin();
            long selectionEnd = fTimeProvider.getSelectionEnd();
            int xBegin = getXForTime(selectionBegin);
            int xEnd = getXForTime(selectionEnd);
            if (Math.abs(x - xBegin) < SNAP_WIDTH || Math.abs(x - xEnd) < SNAP_WIDTH) {
                cursor = fResizeCursor;
            }
        }
        if (getCursor() != cursor) {
            setCursor(cursor);
        }
    }

    private void updateStatusLine(int x) {
        if (fStatusLineManager == null || null == fTimeProvider ||
                fTimeProvider.getTime0() == fTimeProvider.getTime1()) {
            return;
        }
        StringBuilder message = new StringBuilder();
        if (x >= 0 && fDragState == DRAG_NONE) {
            long time = getTimeAtX(x);
            if (time >= 0) {
                message.append("T: "); //$NON-NLS-1$
                message.append(new TmfNanoTimestamp(time).toString());
                message.append("     T1: "); //$NON-NLS-1$
                long selectionBegin = fTimeProvider.getSelectionBegin();
                long selectionEnd = fTimeProvider.getSelectionEnd();
                message.append(new TmfNanoTimestamp(Math.min(selectionBegin, selectionEnd)).toString());
                if (selectionBegin != selectionEnd) {
                    message.append("     T2: "); //$NON-NLS-1$
                    message.append(new TmfNanoTimestamp(Math.max(selectionBegin, selectionEnd)).toString());
                    message.append("     \u0394: "); //$NON-NLS-1$
                    message.append(new TmfTimestampDelta(Math.abs(selectionBegin - selectionEnd), ITmfTimestamp.NANOSECOND_SCALE));
                }
            }
        } else if (fDragState == DRAG_SELECTION || fDragState == DRAG_ZOOM) {
            long time0 = fDragTime0;
            long time = getTimeAtX(fDragX);
            message.append("T1: "); //$NON-NLS-1$
            message.append(new TmfNanoTimestamp(Math.min(time, time0)).toString());
            if (time != time0) {
                message.append("     T2: "); //$NON-NLS-1$
                message.append(new TmfNanoTimestamp(Math.max(time, time0)).toString());
                message.append("     \u0394: "); //$NON-NLS-1$
                message.append(new TmfTimestampDelta(Math.abs(time - time0), ITmfTimestamp.NANOSECOND_SCALE));
            }
        }
        fStatusLineManager.setMessage(message.toString());
    }

    @Override
    public void mouseMove(MouseEvent e) {
        if (null == fTimeProvider) {
            return;
        }
        Point size = getCtrlSize();
        if (DRAG_TRACE_ITEM == fDragState) {
            int nameWidth = fTimeProvider.getNameSpace();
            if (e.x > nameWidth && size.x > nameWidth && fDragX != e.x) {
                fDragX = e.x;
                double pixelsPerNanoSec = (size.x - nameWidth <= RIGHT_MARGIN) ? 0 : (double) (size.x - nameWidth - RIGHT_MARGIN) / (fTime1bak - fTime0bak);
                long timeDelta = (long) ((pixelsPerNanoSec == 0) ? 0 : ((fDragX - fDragX0) / pixelsPerNanoSec));
                long time1 = fTime1bak - timeDelta;
                long maxTime = fTimeProvider.getMaxTime();
                if (time1 > maxTime) {
                    time1 = maxTime;
                }
                long time0 = time1 - (fTime1bak - fTime0bak);
                if (time0 < fTimeProvider.getMinTime()) {
                    time0 = fTimeProvider.getMinTime();
                    time1 = time0 + (fTime1bak - fTime0bak);
                }
                fTimeProvider.setStartFinishTime(time0, time1);
            }
        } else if (DRAG_SPLIT_LINE == fDragState) {
            fDragX = e.x;
            fTimeProvider.setNameSpace(e.x);
        } else if (DRAG_SELECTION == fDragState) {
            fDragX = Math.min(Math.max(e.x, fTimeProvider.getNameSpace()), size.x - RIGHT_MARGIN);
            redraw();
            fTimeGraphScale.setDragRange(fDragX0, fDragX);
            fireDragSelectionChanged(getTimeAtX(fDragX0), getTimeAtX(fDragX));
        } else if (DRAG_ZOOM == fDragState) {
            fDragX = Math.min(Math.max(e.x, fTimeProvider.getNameSpace()), size.x - RIGHT_MARGIN);
            redraw();
            fTimeGraphScale.setDragRange(fDragX0, fDragX);
        } else if (DRAG_NONE == fDragState) {
            boolean mouseOverSplitLine = isOverSplitLine(e.x);
            if (fMouseOverSplitLine != mouseOverSplitLine) {
                redraw();
            }
            fMouseOverSplitLine = mouseOverSplitLine;
        }
        updateCursor(e.x, e.stateMask);
        updateStatusLine(e.x);
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
        if (null == fTimeProvider) {
            return;
        }
        if (1 == e.button && (e.stateMask & SWT.BUTTON_MASK) == 0) {
            if (isOverSplitLine(e.x) && fTimeProvider.getNameSpace() != 0) {
                fTimeProvider.setNameSpace(fIdealNameSpace);
                boolean mouseOverSplitLine = isOverSplitLine(e.x);
                if (fMouseOverSplitLine != mouseOverSplitLine) {
                    redraw();
                }
                fMouseOverSplitLine = mouseOverSplitLine;
                return;
            }
            int idx = getItemIndexAtY(e.y);
            if (idx >= 0) {
                selectItem(idx, false);
                fireDefaultSelection();
            }
        }
    }

    @Override
    public void mouseDown(MouseEvent e) {
        if (fDragState != DRAG_NONE || null == fTimeProvider ||
                fTimeProvider.getTime0() == fTimeProvider.getTime1() ||
                getCtrlSize().x - fTimeProvider.getNameSpace() <= 0) {
            return;
        }
        int idx;
        if (1 == e.button && (e.stateMask & SWT.MODIFIER_MASK) == 0) {
            int nameSpace = fTimeProvider.getNameSpace();
            if (nameSpace != 0 && isOverSplitLine(e.x)) {
                fDragState = DRAG_SPLIT_LINE;
                fDragButton = e.button;
                fDragX = e.x;
                fDragX0 = fDragX;
                fTime0bak = fTimeProvider.getTime0();
                fTime1bak = fTimeProvider.getTime1();
                redraw();
                updateCursor(e.x, e.stateMask);
                return;
            }
        }
        if (1 == e.button && ((e.stateMask & SWT.MODIFIER_MASK) == 0 || (e.stateMask & SWT.MODIFIER_MASK) == SWT.SHIFT)) {
            int nameSpace = fTimeProvider.getNameSpace();
            idx = getItemIndexAtY(e.y);
            if (idx >= 0) {
                Item item = fItemData.fExpandedItems[idx];
                if (item.fHasChildren && e.x < nameSpace && e.x < MARGIN + (item.fLevel + 1) * EXPAND_SIZE) {
                    toggle(idx);
                    return;
                }
                selectItem(idx, false);
                fireSelectionChanged();
            } else {
                selectItem(idx, false); // clear selection
                fireSelectionChanged();
            }
            long hitTime = getTimeAtX(e.x);
            if (hitTime >= 0) {
                setCapture(true);

                fDragState = DRAG_SELECTION;
                fDragButton = e.button;
                fDragX = e.x;
                fDragX0 = fDragX;
                fDragTime0 = getTimeAtX(fDragX0);
                long selectionBegin = fTimeProvider.getSelectionBegin();
                long selectionEnd = fTimeProvider.getSelectionEnd();
                int xBegin = getXForTime(selectionBegin);
                int xEnd = getXForTime(selectionEnd);
                if ((e.stateMask & SWT.MODIFIER_MASK) == SWT.SHIFT) {
                    long time = getTimeAtX(e.x);
                    if (Math.abs(time - selectionBegin) < Math.abs(time - selectionEnd)) {
                        fDragX0 = xEnd;
                        fDragTime0 = selectionEnd;
                    } else {
                        fDragX0 = xBegin;
                        fDragTime0 = selectionBegin;
                    }
                } else {
                    long time = getTimeAtX(e.x);
                    if (Math.abs(e.x - xBegin) < SNAP_WIDTH && Math.abs(time - selectionBegin) <= Math.abs(time - selectionEnd)) {
                        fDragX0 = xEnd;
                        fDragTime0 = selectionEnd;
                    } else if (Math.abs(e.x - xEnd) < SNAP_WIDTH && Math.abs(time - selectionEnd) <= Math.abs(time - selectionBegin)) {
                        fDragX0 = xBegin;
                        fDragTime0 = selectionBegin;
                    }
                }
                fTime0bak = fTimeProvider.getTime0();
                fTime1bak = fTimeProvider.getTime1();
                redraw();
                updateCursor(e.x, e.stateMask);
                fTimeGraphScale.setDragRange(fDragX0, fDragX);
            }
        } else if (2 == e.button || (1 == e.button && (e.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL)) {
            long hitTime = getTimeAtX(e.x);
            if (hitTime > 0) {
                setCapture(true);
                fDragState = DRAG_TRACE_ITEM;
                fDragButton = e.button;
                fDragX = e.x;
                fDragX0 = fDragX;
                fTime0bak = fTimeProvider.getTime0();
                fTime1bak = fTimeProvider.getTime1();
                updateCursor(e.x, e.stateMask);
            }
        } else if (3 == e.button) {
            setCapture(true);
            fDragX = Math.min(Math.max(e.x, fTimeProvider.getNameSpace()), getCtrlSize().x - RIGHT_MARGIN);
            fDragX0 = fDragX;
            fDragState = DRAG_ZOOM;
            fDragButton = e.button;
            redraw();
            updateCursor(e.x, e.stateMask);
            fTimeGraphScale.setDragRange(fDragX0, fDragX);
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if (fPendingMenuDetectEvent != null && e.button == 3) {
            menuDetected(fPendingMenuDetectEvent);
        }
        if (DRAG_NONE != fDragState) {
            setCapture(false);
            if (e.button == fDragButton && DRAG_TRACE_ITEM == fDragState) {
                if (fDragX != fDragX0) {
                    fTimeProvider.notifyStartFinishTime();
                }
                fDragState = DRAG_NONE;
            } else if (e.button == fDragButton && DRAG_SPLIT_LINE == fDragState) {
                fDragState = DRAG_NONE;
                redraw();
            }  else if (e.button == fDragButton && DRAG_SELECTION == fDragState) {
                if (fDragX == fDragX0) { // click without selecting anything
                    long time = getTimeAtX(e.x);
                    fTimeProvider.setSelectedTimeNotify(time, false);
                } else {
                    long time0 = fDragTime0;
                    long time1 = getTimeAtX(fDragX);
                    if (time0 <= time1) {
                        fTimeProvider.setSelectionRangeNotify(time0, time1);
                    } else {
                        fTimeProvider.setSelectionRangeNotify(time1, time0);
                    }
                }
                fDragState = DRAG_NONE;
                redraw();
                fTimeGraphScale.setDragRange(-1, -1);
            } else if (e.button == fDragButton && DRAG_ZOOM == fDragState) {
                int nameWidth = fTimeProvider.getNameSpace();
                if (Math.max(fDragX, fDragX0) > nameWidth && fDragX != fDragX0) {
                    long time0 = getTimeAtX(fDragX0);
                    long time1 = getTimeAtX(fDragX);
                    if (time0 < time1) {
                        fTimeProvider.setStartFinishTimeNotify(time0, time1);
                    } else {
                        fTimeProvider.setStartFinishTimeNotify(time1, time0);
                    }
                } else {
                    redraw();
                }
                fDragState = DRAG_NONE;
                fTimeGraphScale.setDragRange(-1, -1);
            }
        }
        updateCursor(e.x, e.stateMask);
        updateStatusLine(e.x);
    }

    @Override
    public void mouseEnter(MouseEvent e) {
    }

    @Override
    public void mouseExit(MouseEvent e) {
        if (fMouseOverSplitLine) {
            fMouseOverSplitLine = false;
            redraw();
        }
        updateStatusLine(NO_STATUS);
    }

    @Override
    public void mouseHover(MouseEvent e) {
    }

    @Override
    public void mouseScrolled(MouseEvent e) {
        if ((fMouseScrollFilterListener == null) || fDragState != DRAG_NONE) {
            return;
        }
        boolean zoomScroll = false;
        Point p = getParent().toControl(getDisplay().getCursorLocation());
        Point parentSize = getParent().getSize();
        if (p.x >= 0 && p.x < parentSize.x && p.y >= 0 && p.y < parentSize.y) {
            // over the parent control
            if (e.x > getCtrlSize().x) {
                // over the horizontal scroll bar
                zoomScroll = false;
            } else if (e.y >= 0 && e.y < getCtrlSize().y && e.x < fTimeProvider.getNameSpace()) {
                // over the name space
                zoomScroll = false;
            } else {
                zoomScroll = true;
            }
        }
        if (zoomScroll && fTimeProvider.getTime0() != fTimeProvider.getTime1()) {
            if (e.count > 0) {
                zoom(true);
            } else if (e.count < 0) {
                zoom(false);
            }
        } else {
            setTopIndex(getTopIndex() - e.count);
        }
    }

    @Override
    public void controlMoved(ControlEvent e) {
    }

    @Override
    public void controlResized(ControlEvent e) {
        adjustScrolls();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.widget == getVerticalBar()) {
            setTopIndex(getVerticalBar().getSelection());
        } else if (e.widget == getHorizontalBar() && null != fTimeProvider) {
            int start = getHorizontalBar().getSelection();
            long time0 = fTimeProvider.getTime0();
            long time1 = fTimeProvider.getTime1();
            long timeMin = fTimeProvider.getMinTime();
            long timeMax = fTimeProvider.getMaxTime();
            long delta = timeMax - timeMin;

            long range = time1 - time0;
            time0 = timeMin + Math.round(delta * ((double) start / H_SCROLLBAR_MAX));
            time1 = time0 + range;

            // TODO: Follow-up with Bug 310310
            // In Linux SWT.DRAG is the only value received
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=310310
            if (e.detail == SWT.DRAG) {
                fTimeProvider.setStartFinishTime(time0, time1);
            } else {
                fTimeProvider.setStartFinishTimeNotify(time0, time1);
            }
        }
    }

    @Override
    public int getBorderWidth() {
        return fBorderWidth;
    }

    /**
     * Set the border width
     *
     * @param borderWidth
     *            The width
     */
    public void setBorderWidth(int borderWidth) {
        this.fBorderWidth = borderWidth;
    }

    /**
     * @return The current height of the header row
     */
    public int getHeaderHeight() {
        return fHeaderHeight;
    }

    /**
     * Set the height of the header row
     *
     * @param headerHeight
     *            The height
     */
    public void setHeaderHeight(int headerHeight) {
        this.fHeaderHeight = headerHeight;
    }

    /**
     * @return The default height of regular item rows
     */
    public int getItemHeight() {
        return fGlobalItemHeight;
    }

    /**
     * Set the default height of regular item rows.
     *
     * @param rowHeight
     *            The height
     */
    public void setItemHeight(int rowHeight) {
        this.fGlobalItemHeight = rowHeight;
    }

    /**
     * Set the height of a specific item. Overrides the default item height.
     *
     * @param entry
     *            A time graph entry
     * @param rowHeight
     *            The height
     * @return true if the height is successfully stored, false otherwise
     *
     * @since 2.1
     */
    public boolean setItemHeight(ITimeGraphEntry entry, int rowHeight) {
        Item item = fItemData.findItem(entry);
        if (item != null) {
            item.fItemHeight = rowHeight;
            return true;
        }
        return false;
    }

    /**
     * Set the minimum item width
     *
     * @param width The minimum width
     */
    public void setMinimumItemWidth(int width) {
        this.fMinimumItemWidth = width;
    }

    /**
     * @return The minimum item width
     */
    public int getMinimumItemWidth() {
        return fMinimumItemWidth;
    }

    /**
     * @return The entries that are currently filtered out
     *
     * @since 2.0
     */
    public List<ITimeGraphEntry> getFilteredOut() {
        return fItemData.getFilteredOut();
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (listener != null && !fSelectionChangedListeners.contains(listener)) {
            fSelectionChangedListeners.add(listener);
        }
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        if (listener != null) {
            fSelectionChangedListeners.remove(listener);
        }
    }

    @Override
    public void setSelection(ISelection selection) {
        if (selection instanceof TimeGraphSelection) {
            TimeGraphSelection sel = (TimeGraphSelection) selection;
            Object ob = sel.getFirstElement();
            if (ob instanceof ITimeGraphEntry) {
                ITimeGraphEntry trace = (ITimeGraphEntry) ob;
                selectItem(trace, false);
            }
        }

    }

    /**
     * @param filter The filter object to be attached to the view
     * @since 2.0
     */
    public void addFilter(ViewerFilter filter) {
        if (!fFilters.contains(filter)) {
            fFilters.add(filter);
        }
    }

    /**
     * @param filter The filter object to be attached to the view
     * @since 2.0
     */
    public void removeFilter(ViewerFilter filter) {
        fFilters.remove(filter);
    }

    /**
     * @since 3.0
     */
    @Override
    public void colorSettingsChanged(StateItem[] stateItems) {
        /* Destroy previous colors from the resource manager */
        if (fEventColorMap != null) {
            for (Color color : fEventColorMap) {
                fResourceManager.destroyColor(color.getRGB());
            }
        }
        if (stateItems != null) {
            fEventColorMap = new Color[stateItems.length];
            for (int i = 0; i < stateItems.length; i++) {
                fEventColorMap[i] = fResourceManager.createColor(stateItems[i].getStateColor());
            }
        } else {
            fEventColorMap = new Color[] { };
        }
        redraw();
    }

    private class ItemData {
        private final Map<ITimeGraphEntry, Item> fItemMap = new LinkedHashMap<>();
        private Item[] fExpandedItems = new Item[0];
        private Item[] fItems = new Item[0];
        private ITimeGraphEntry fRootEntries[] = new ITimeGraphEntry[0];
        private List<ILinkEvent> fLinks = new ArrayList<>();
        private boolean fEntryFilter[] = new boolean[0];
        private final ArrayList<ITimeGraphEntry> fFilteredOut = new ArrayList<>();

        public ItemData() {
        }

        public Item findItem(ITimeGraphEntry entry) {
            return fItemMap.get(entry);
        }

        public int findItemIndex(ITimeGraphEntry entry) {
            Item item = fItemMap.get(entry);
            if (item == null) {
                return -1;
            }
            return item.fExpandedIndex;
        }

        public void refreshData() {
            fItemMap.clear();
            fFilteredOut.clear();
            ITimeGraphEntry selection = getSelectedTrace();
            for (int i = 0; i < fRootEntries.length; i++) {
                ITimeGraphEntry entry = fRootEntries[i];
                refreshData(fItemMap, null, 0, entry);
            }
            fItems = fItemMap.values().toArray(new Item[0]);
            updateExpandedItems();
            if (selection != null) {
                for (Item item : fExpandedItems) {
                    if (item.fEntry == selection) {
                        item.fSelected = true;
                        break;
                    }
                }
            }
        }

        private void refreshData(Map<ITimeGraphEntry, Item> itemMap, Item parent, int level, ITimeGraphEntry entry) {
            Item item = new Item(entry, entry.getName(), level);
            if (parent != null) {
                parent.fChildren.add(item);
            }
            if (fGlobalItemHeight == CUSTOM_ITEM_HEIGHT) {
                item.fItemHeight = fTimeGraphProvider.getItemHeight(entry);
            } else {
                item.fItemHeight = fGlobalItemHeight;
            }
            itemMap.put(entry, item);
            if (entry.hasChildren()) {
                item.fExpanded = fAutoExpandLevel == ALL_LEVELS || level < fAutoExpandLevel;
                item.fHasChildren = true;
                for (ITimeGraphEntry child : entry.getChildren()) {
                    refreshData(itemMap, item, level + 1, child);
                }
            }
        }

        public void updateExpandedItems() {
            for (Item item : fItems) {
                item.fExpandedIndex = -1;
            }
            List<Item> expandedItemList = new ArrayList<>();
            for (int i = 0; i < fRootEntries.length; i++) {
                ITimeGraphEntry entry = fRootEntries[i];
                Item item = findItem(entry);
                refreshExpanded(expandedItemList, item);
            }
            fExpandedItems = expandedItemList.toArray(new Item[0]);
            fTopIndex = Math.min(fTopIndex, Math.max(0, fExpandedItems.length - 1));
        }

        private void refreshExpanded(List<Item> expandedItemList, Item item) {
            // Check for filters
            boolean display = true;
            for (ViewerFilter filter : fFilters) {
                if (!filter.select(null, item.fEntry.getParent(), item.fEntry)) {
                    display = false;
                    break;
                }
            }
            if (display) {
                item.fExpandedIndex = expandedItemList.size();
                expandedItemList.add(item);
                if (item.fHasChildren && item.fExpanded) {
                    for (Item child : item.fChildren) {
                        refreshExpanded(expandedItemList, child);
                    }
                }
            }
        }

        public void refreshData(ITimeGraphEntry[] entries) {
            if (entries == null) {
                fEntryFilter = null;
                fRootEntries = null;
            } else {
                if (entries.length == 0) {
                    fEntryFilter = null;
                } else if (fEntryFilter == null || entries.length != fEntryFilter.length) {
                    fEntryFilter = new boolean[entries.length];
                    java.util.Arrays.fill(fEntryFilter, true);
                }
                fRootEntries = Arrays.copyOf(entries, entries.length);
            }

            refreshData();
        }

        public void refreshArrows(List<ILinkEvent> events) {
            /* If links are null, reset the list */
            if (events != null) {
                fLinks = events;
            } else {
                fLinks = new ArrayList<>();
            }
        }

        public ITimeGraphEntry[] getEntries() {
            return fRootEntries;
        }

        public boolean[] getEntryFilter() {
            return fEntryFilter;
        }

        public List<ITimeGraphEntry> getFilteredOut() {
            return fFilteredOut;
        }
    }

    private class Item {
        private boolean fExpanded;
        private int fExpandedIndex;
        private boolean fSelected;
        private boolean fHasChildren;
        private int fItemHeight;
        private final int fLevel;
        private final List<Item> fChildren;
        private final String fName;
        private final ITimeGraphEntry fEntry;

        public Item(ITimeGraphEntry entry, String name, int level) {
            this.fEntry = entry;
            this.fName = name;
            this.fLevel = level;
            this.fChildren = new ArrayList<>();
        }

        @Override
        public String toString() {
            return fName;
        }
    }

    /**
     * @since 1.2
     */
    @Override
    public void menuDetected(MenuDetectEvent e) {
        if (null == fTimeProvider) {
            return;
        }
        if (e.detail == SWT.MENU_MOUSE) {
            if (fPendingMenuDetectEvent == null) {
                /* Feature in Linux. The MenuDetectEvent is received before mouseDown.
                 * Store the event and trigger it later just before handling mouseUp.
                 * This allows for the method to detect if mouse is used to drag zoom.
                 */
                fPendingMenuDetectEvent = e;
                return;
            }
            fPendingMenuDetectEvent = null;
            if (fDragState != DRAG_ZOOM || fDragX != fDragX0) {
                return;
            }
        } else {
            if (fDragState != DRAG_NONE) {
                return;
            }
        }
        Point p = toControl(e.x, e.y);
        int idx = getItemIndexAtY(p.y);
        if (idx >= 0 && idx < fItemData.fExpandedItems.length) {
            Item item = fItemData.fExpandedItems[idx];
            ITimeGraphEntry entry = item.fEntry;
            if (entry.hasTimeEvents()) {
                ITimeEvent event = Utils.findEvent(entry, getTimeAtX(p.x), 2);
                if (event != null) {
                    e.data = event;
                    fireMenuEventOnTimeEvent(e);
                    return;
                }
            }
            e.data = entry;
            fireMenuEventOnTimeGraphEntry(e);
        }
    }

}


