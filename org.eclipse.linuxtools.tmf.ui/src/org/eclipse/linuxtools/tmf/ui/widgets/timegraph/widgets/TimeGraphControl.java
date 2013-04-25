/*****************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation, Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Intel Corporation - Initial API and implementation
 *   Ruslan A. Scherbakov, Intel - Initial API and implementation
 *   Alvaro Sanchez-Leon - Updated for TMF
 *   Patrick Tasse - Refactoring
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTreeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTreeExpansionEvent;
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
public class TimeGraphControl extends TimeGraphBaseControl implements FocusListener, KeyListener, MouseMoveListener, MouseListener, MouseWheelListener, ControlListener, SelectionListener, MouseTrackListener, TraverseListener, ISelectionProvider, MenuDetectListener {

    /** Max scrollbar size */
    public static final int H_SCROLLBAR_MAX = Integer.MAX_VALUE - 1;

    /** Resource manager */
    protected LocalResourceManager fResourceManager = new LocalResourceManager(JFaceResources.getResources());

    /** Color map for event types */
    protected Color[] fEventColorMap = null;

    private static final int DRAG_NONE = 0;
    private static final int DRAG_TRACE_ITEM = 1;
    private static final int DRAG_SPLIT_LINE = 2;
    private static final int DRAG_ZOOM = 3;

    private static final int CUSTOM_ITEM_HEIGHT = -1; // get item height from provider

    private static final double zoomCoeff = 1.5;

    private ITimeDataProvider _timeProvider;
    private boolean _isInFocus = false;
    private boolean _isDragCursor3 = false;
    private boolean _isWaitCursor = true;
    private boolean _mouseOverSplitLine = false;
    private int _itemHeight = CUSTOM_ITEM_HEIGHT;
    private int _minimumItemWidth = 0;
    private int _topIndex = 0;
    private int _dragState = DRAG_NONE;
    private int _dragX0 = 0;
    private int _dragX = 0;
    private int _idealNameSpace = 0;
    // private double _timeStep = 10000000;
    private long _time0bak;
    private long _time1bak;
    private ITimeGraphPresentationProvider fTimeGraphProvider = null;
    private ItemData _data = null;
    private List<SelectionListener> _selectionListeners;
    private final List<ISelectionChangedListener> _selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
    private final List<ITimeGraphTreeListener> _treeListeners = new ArrayList<ITimeGraphTreeListener>();
    private final List<MenuDetectListener> _timeGraphEntryMenuListeners = new ArrayList<MenuDetectListener>();
    private final List<MenuDetectListener> _timeEventMenuListeners = new ArrayList<MenuDetectListener>();
    private final Cursor _dragCursor3;
    private final Cursor _WaitCursor;
    private final List<ViewerFilter> _filters = new ArrayList<ViewerFilter>();
    private MenuDetectEvent fPendingMenuDetectEvent = null;

    // Vertical formatting formatting for the state control view
    private final boolean _visibleVerticalScroll = true;
    private int _borderWidth = 0;
    private int _headerHeight = 0;

    private Listener mouseScrollFilterListener;

    private MouseScrollNotifier fMouseScrollNotifier;
    private final Object fMouseScrollNotifierLock = new Object();
    private class MouseScrollNotifier extends Thread {
        private final static long DELAY = 400L;
        private final static long POLLING_INTERVAL = 10L;
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
                        _timeProvider.notifyStartFinishTime();
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

        _data = new ItemData();

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

        _dragCursor3 = new Cursor(super.getDisplay(), SWT.CURSOR_SIZEWE);
        _WaitCursor = new Cursor(super.getDisplay(), SWT.CURSOR_WAIT);
    }

    @Override
    public void dispose() {
        super.dispose();
        _dragCursor3.dispose();
        _WaitCursor.dispose();
        fResourceManager.dispose();
    }

    /**
     * Sets the timegraph provider used by this timegraph viewer.
     *
     * @param timeGraphProvider the timegraph provider
     */
    public void setTimeGraphProvider(ITimeGraphPresentationProvider timeGraphProvider) {
        fTimeGraphProvider = timeGraphProvider;
        _data.provider = timeGraphProvider;

        if (fEventColorMap != null) {
            for (Color color : fEventColorMap) {
                fResourceManager.destroyColor(color.getRGB());
            }
        }
        StateItem[] stateItems = fTimeGraphProvider.getStateTable();
        if (stateItems != null) {
            fEventColorMap = new Color[stateItems.length];
            for (int i = 0; i < stateItems.length; i++) {
                fEventColorMap[i] = fResourceManager.createColor(stateItems[i].getStateColor());
            }
        } else {
            fEventColorMap = new Color[] { };
        }
    }

    /**
     * Assign the given time provider
     *
     * @param timeProvider
     *            The time provider
     */
    public void setTimeProvider(ITimeDataProvider timeProvider) {
        _timeProvider = timeProvider;
        adjustScrolls();
        redraw();
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
        if (null == _selectionListeners) {
            _selectionListeners = new ArrayList<SelectionListener>();
        }
        _selectionListeners.add(listener);
    }

    /**
     * Remove a selection listener
     *
     * @param listener
     *            The listener to remove
     */
    public void removeSelectionListener(SelectionListener listener) {
        if (null != _selectionListeners) {
            _selectionListeners.remove(listener);
        }
    }

    /**
     * Selection changed callback
     */
    public void fireSelectionChanged() {
        if (null != _selectionListeners) {
            Iterator<SelectionListener> it = _selectionListeners.iterator();
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
        if (null != _selectionListeners) {
            Iterator<SelectionListener> it = _selectionListeners.iterator();
            while (it.hasNext()) {
                SelectionListener listener = it.next();
                listener.widgetDefaultSelected(null);
            }
        }
    }

    /**
     * Get the traces in the model
     *
     * @return The array of traces
     */
    public ITimeGraphEntry[] getTraces() {
        return _data.getTraces();
    }

    /**
     * Get the on/off trace filters
     *
     * @return The array of filters
     */
    public boolean[] getTraceFilter() {
        return _data.getTraceFilter();
    }

    /**
     * Refresh the data for the thing
     */
    public void refreshData() {
        _data.refreshData();
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
        _data.refreshData(traces);
        adjustScrolls();
        redraw();
    }

    /**
     * Adjust the scoll bars
     */
    public void adjustScrolls() {
        if (null == _timeProvider) {
            getHorizontalBar().setValues(0, 1, 1, 1, 1, 1);
            return;
        }

        // HORIZONTAL BAR
        // Visible window
        long time0 = _timeProvider.getTime0();
        long time1 = _timeProvider.getTime1();
        // Time boundaries
        long timeMin = _timeProvider.getMinTime();
        long timeMax = _timeProvider.getMaxTime();

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
            for (index = 0; index < _data._expandedItems.length; index++) {
                if (_data._expandedItems[index]._selected) {
                    break;
                }
            }
        }
        if (index >= _data._expandedItems.length) {
            return changed;
        }
        if (index < _topIndex) {
            setTopIndex(index);
            //FIXME:getVerticalBar().setSelection(_topItem);
            if (redraw) {
                redraw();
            }
            changed = true;
        } else {
            int page = countPerPage();
            if (index >= _topIndex + page) {
                setTopIndex(index - page + 1);
                //FIXME:getVerticalBar().setSelection(_topItem);
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
        int index = Math.min(idx, _data._expandedItems.length - countPerPage());
        index = Math.max(0,  index);
        _topIndex = index;
        redraw();
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
        Item item = _data.findItem(entry);
        if (item != null && item._expanded != expanded) {
            item._expanded = expanded;
            _data.updateExpandedItems();
            redraw();
        }
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void collapseAll() {
        for (Item item : _data._items) {
            item._expanded = false;
        }
        _data.updateExpandedItems();
        redraw();
    }

    /**
     * Expands all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void expandAll() {
        for (Item item : _data._items) {
            item._expanded = true;
        }
        _data.updateExpandedItems();
        redraw();
    }

    /**
     * Add a tree listener
     *
     * @param listener
     *            The listener to add
     */
    public void addTreeListener(ITimeGraphTreeListener listener) {
        if (!_treeListeners.contains(listener)) {
            _treeListeners.add(listener);
        }
    }

    /**
     * Remove a tree listener
     *
     * @param listener
     *            The listener to remove
     */
    public void removeTreeListener(ITimeGraphTreeListener listener) {
        if (_treeListeners.contains(listener)) {
            _treeListeners.remove(listener);
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
        for (ITimeGraphTreeListener listener : _treeListeners) {
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
        if (!_timeGraphEntryMenuListeners.contains(listener)) {
            _timeGraphEntryMenuListeners.add(listener);
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
        if (_timeGraphEntryMenuListeners.contains(listener)) {
            _timeGraphEntryMenuListeners.remove(listener);
        }
    }

    /**
     * Menu event callback on {@link ITimeGraphEntry}s
     *
     * @param event
     *            The MenuDetectEvent, with field {@link TypedEvent#data} set to the selected {@link ITimeGraphEntry}
     */
    private void fireMenuEventOnTimeGraphEntry(MenuDetectEvent event) {
        for (MenuDetectListener listener : _timeGraphEntryMenuListeners) {
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
        if (!_timeEventMenuListeners.contains(listener)) {
            _timeEventMenuListeners.add(listener);
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
        if (_timeEventMenuListeners.contains(listener)) {
            _timeEventMenuListeners.remove(listener);
        }
    }

    /**
     * Menu event callback on {@link ITimeEvent}s
     *
     * @param event
     *            The MenuDetectEvent, with field {@link TypedEvent#data} set to the selected {@link ITimeEvent}
     */
    private void fireMenuEventOnTimeEvent(MenuDetectEvent event) {
        for (MenuDetectListener listener : _timeEventMenuListeners) {
            listener.menuDetected(event);
        }
    }

    @Override
    public ISelection getSelection() {
        TimeGraphSelection sel = new TimeGraphSelection();
        ITimeGraphEntry trace = getSelectedTrace();
        if (null != trace && null != _timeProvider) {
            long selectedTime = _timeProvider.getSelectedTime();
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
        for (int i = 0; i < _data._expandedItems.length; i++) {
            Item item = _data._expandedItems[i];
            if (item._selected) {
                lastSelection = i;
                if ((1 == n) && (i < _data._expandedItems.length - 1)) {
                    item._selected = false;
                    item = _data._expandedItems[i + 1];
                    item._selected = true;
                    changed = true;
                } else if ((-1 == n) && (i > 0)) {
                    item._selected = false;
                    item = _data._expandedItems[i - 1];
                    item._selected = true;
                    changed = true;
                }
                break;
            }
        }

        if (lastSelection < 0 && _data._expandedItems.length > 0) {
            Item item = _data._expandedItems[0];
            item._selected = true;
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
        if (null == _timeProvider) {
            return;
        }
        ITimeGraphEntry trace = getSelectedTrace();
        if (trace == null) {
            return;
        }
        long selectedTime = _timeProvider.getSelectedTime();
        long endTime = _timeProvider.getEndTime();
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
            } else if (n == -1) {
                // for previous event go to its end time unless we were already there
                if (nextEvent.getTime() + nextEvent.getDuration() < selectedTime) {
                    nextTime = nextEvent.getTime() + nextEvent.getDuration();
                }
            }
            _timeProvider.setSelectedTimeNotify(nextTime, true);
            fireSelectionChanged();
        } else if (1 == n) {
            _timeProvider.setSelectedTimeNotify(endTime, true);
            fireSelectionChanged();
        }
    }

    /**
     * Select the next event
     */
    public void selectNextEvent() {
        selectEvent(1);
        // Notify if visible time window has been adjusted
        _timeProvider.setStartFinishTimeNotify(_timeProvider.getTime0(), _timeProvider.getTime1());
    }

    /**
     * Select the previous event
     */
    public void selectPrevEvent() {
        selectEvent(-1);
        // Notify if visible time window has been adjusted
        _timeProvider.setStartFinishTimeNotify(_timeProvider.getTime0(), _timeProvider.getTime1());
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
        int nameSpace = _timeProvider.getNameSpace();
        int timeSpace = _timeProvider.getTimeSpace();
        int xPos = Math.max(nameSpace, Math.min(nameSpace + timeSpace, p.x));
        long time0 = _timeProvider.getTime0();
        long time1 = _timeProvider.getTime1();
        long interval = time1 - time0;
        if (interval == 0) {
            interval = 1;
        } // to allow getting out of single point interval
        long newInterval;
        if (zoomIn) {
            newInterval = Math.max(Math.round(interval * 0.8), _timeProvider.getMinTimeInterval());
        } else {
            newInterval = (long) Math.ceil(interval * 1.25);
        }
        long center = time0 + Math.round(((double) (xPos - nameSpace) / timeSpace * interval));
        long newTime0 = center - Math.round((double) newInterval * (center - time0) / interval);
        long newTime1 = newTime0 + newInterval;
        _timeProvider.setStartFinishTime(newTime0, newTime1);
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
        long _time0 = _timeProvider.getTime0();
        long _time1 = _timeProvider.getTime1();
        long _range = _time1 - _time0;
        if (_range == 0) {
            return;
        }
        long selTime = _timeProvider.getSelectedTime();
        if (selTime <= _time0 || selTime >= _time1) {
            selTime = (_time0 + _time1) / 2;
        }
        long time0 = selTime - (long) ((selTime - _time0) / zoomCoeff);
        long time1 = selTime + (long) ((_time1 - selTime) / zoomCoeff);

        long inaccuracy = (_timeProvider.getMaxTime() - _timeProvider.getMinTime()) - (time1 - time0);

        // Trace.debug("selTime:" + selTime + " time0:" + time0 + " time1:"
        // + time1 + " inaccuracy:" + inaccuracy);

        if (inaccuracy > 0 && inaccuracy < 100) {
            _timeProvider.setStartFinishTimeNotify(_timeProvider.getMinTime(), _timeProvider.getMaxTime());
            return;
        }

        long m = _timeProvider.getMinTimeInterval();
        if ((time1 - time0) < m) {
            time0 = selTime - (selTime - _time0) * m / _range;
            time1 = time0 + m;
        }

        _timeProvider.setStartFinishTimeNotify(time0, time1);
    }

    /**
     * zoom out using single click
     */
    public void zoomOut() {
        long _time0 = _timeProvider.getTime0();
        long _time1 = _timeProvider.getTime1();
        long selTime = _timeProvider.getSelectedTime();
        if (selTime <= _time0 || selTime >= _time1) {
            selTime = (_time0 + _time1) / 2;
        }
        long time0 = (long) (selTime - (selTime - _time0) * zoomCoeff);
        long time1 = (long) (selTime + (_time1 - selTime) * zoomCoeff);

        long inaccuracy = (_timeProvider.getMaxTime() - _timeProvider.getMinTime()) - (time1 - time0);
        if (inaccuracy > 0 && inaccuracy < 100) {
            _timeProvider.setStartFinishTimeNotify(_timeProvider.getMinTime(), _timeProvider.getMaxTime());
            return;
        }

        _timeProvider.setStartFinishTimeNotify(time0, time1);
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
            trace = _data._expandedItems[idx]._trace;
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
        for (int i = 0; i < _data._expandedItems.length; i++) {
            Item item = _data._expandedItems[i];
            if (item._selected) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    boolean toggle(int idx) {
        boolean toggled = false;
        if (idx >= 0 && idx < _data._expandedItems.length) {
            Item item = _data._expandedItems[idx];
            if (item._hasChildren) {
                item._expanded = !item._expanded;
                _data.updateExpandedItems();
                adjustScrolls();
                redraw();
                toggled = true;
                fireTreeEvent(item._trace, item._expanded);
            }
        }
        return toggled;
    }

    int getItemIndexAtY(int y) {
        if (y < 0) {
            return -1;
        }
        if (_itemHeight == CUSTOM_ITEM_HEIGHT) {
            int ySum = 0;
            for (int idx = _topIndex; idx < _data._expandedItems.length; idx++) {
                ySum += _data._expandedItems[idx].itemHeight;
                if (y < ySum) {
                    return idx;
                }
            }
            return -1;
        }
        int idx = y / _itemHeight;
        idx += _topIndex;
        if (idx < _data._expandedItems.length) {
            return idx;
        }
        return -1;
    }

    boolean isOverSplitLine(int x) {
        if (x < 0 || null == _timeProvider) {
            return false;
        }
        int w = 4;
        int nameWidth = _timeProvider.getNameSpace();
        if (x > nameWidth - w && x < nameWidth + w) {
            return true;
        }
        return false;
    }

    ITimeGraphEntry getEntry(Point pt) {
        int idx = getItemIndexAtY(pt.y);
        return idx >= 0 ? _data._expandedItems[idx]._trace : null;
    }

    /**
     * Return the x coordinate corresponding to a time
     *
     * @param time the time
     * @return the x coordinate corresponding to the time
     *
     * @since 2.0
     */
    public int getXForTime(long time) {
        if (null == _timeProvider) {
            return -1;
        }
        long time0 = _timeProvider.getTime0();
        long time1 = _timeProvider.getTime1();
        int width = getCtrlSize().x;
        int nameSpace = _timeProvider.getNameSpace();
        double pixelsPerNanoSec = (width - nameSpace <= RIGHT_MARGIN) ? 0 : (double) (width - nameSpace - RIGHT_MARGIN) / (time1 - time0);
        int x = getBounds().x + nameSpace + (int) ((time - time0) * pixelsPerNanoSec);
        return x;
    }

    /**
     * Return the time corresponding to an x coordinate
     *
     * @param coord The X coordinate
     * @return The time corresponding to the x coordinate
     *
     * @since 2.0
     */
    public long getTimeAtX(int coord) {
        if (null == _timeProvider) {
            return -1;
        }
        long hitTime = -1;
        Point size = getCtrlSize();
        long time0 = _timeProvider.getTime0();
        long time1 = _timeProvider.getTime1();
        int nameWidth = _timeProvider.getNameSpace();
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
            if (idx >= 0 && idx < _data._expandedItems.length) {
                Item item = _data._expandedItems[idx];
                changed = (item._selected == false);
                item._selected = true;
            }
        } else {
            for (int i = 0; i < _data._expandedItems.length; i++) {
                Item item = _data._expandedItems[i];
                if ((i == idx && !item._selected) || (idx == -1 && item._selected)) {
                    changed = true;
                }
                item._selected = i == idx;
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
        int idx = _data.findItemIndex(trace);
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
        if (_itemHeight == CUSTOM_ITEM_HEIGHT) {
            int ySum = 0;
            for (int idx = _topIndex; idx < _data._expandedItems.length; idx++) {
                ySum += _data._expandedItems[idx].itemHeight;
                if (ySum >= height) {
                    return count;
                }
                count++;
            }
            for (int idx = _topIndex - 1; idx >= 0; idx--) {
                ySum += _data._expandedItems[idx].itemHeight;
                if (ySum >= height) {
                    return count;
                }
                count++;
            }
            return count;
        }
        if (height > 0) {
            count = height / _itemHeight;
        }
        return count;
    }

    /**
     * Get the index of the top element
     *
     * @return The index
     */
    public int getTopIndex() {
        return _topIndex;
    }

    /**
     * Get the number of expanded items
     *
     * @return The count of expanded items
     */
    public int getExpandedElementCount() {
        return _data._expandedItems.length;
    }

    /**
     * Get an array of all expanded elements
     *
     * @return The expanded elements
     */
    public ITimeGraphEntry[] getExpandedElements() {
        ArrayList<ITimeGraphEntry> elements = new ArrayList<ITimeGraphEntry>();
        for (Item item : _data._expandedItems) {
            elements.add(item._trace);
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
        int x = bound.x;
        int y = bound.y + (idx - _topIndex) * _itemHeight;
        int width = nameWidth;
        int height = _itemHeight;
        if (_itemHeight == CUSTOM_ITEM_HEIGHT) {
            int ySum = 0;
            for (int i = _topIndex; i < idx; i++) {
                ySum += _data._expandedItems[i].itemHeight;
            }
            y = bound.y + ySum;
            height = _data._expandedItems[idx].itemHeight;
        }
        return new Rectangle(x, y, width, height);
    }

    Rectangle getStatesRect(Rectangle bound, int idx, int nameWidth) {
        int x = bound.x + nameWidth;
        int y = bound.y + (idx - _topIndex) * _itemHeight;
        int width = bound.width - x;
        int height = _itemHeight;
        if (_itemHeight == CUSTOM_ITEM_HEIGHT) {
            int ySum = 0;
            for (int i = _topIndex; i < idx; i++) {
                ySum += _data._expandedItems[i].itemHeight;
            }
            y = bound.y + ySum;
            height = _data._expandedItems[idx].itemHeight;
        }
        return new Rectangle(x, y, width, height);
    }

    @Override
    void paint(Rectangle bounds, PaintEvent e) {
        GC gc = e.gc;
        gc.setBackground(_colors.getColor(TimeGraphColorScheme.BACKGROUND));
        drawBackground(gc, bounds.x, bounds.y, bounds.width, bounds.height);

        if (bounds.width < 2 || bounds.height < 2 || null == _timeProvider) {
            return;
        }

        _idealNameSpace = 0;
        int nameSpace = _timeProvider.getNameSpace();

        // draw empty name space background
        gc.setBackground(_colors.getBkColor(false, false, true));
        drawBackground(gc, bounds.x, bounds.y, nameSpace, bounds.height);

        if (_dragState == DRAG_ZOOM) {
            // draw selected zoom region background
            gc.setBackground(_colors.getBkColor(false, false, true));
            if (_dragX0 < _dragX) {
                gc.fillRectangle(new Rectangle(_dragX0, bounds.y, _dragX - _dragX0, bounds.height));
            } else if (_dragX0 > _dragX) {
                gc.fillRectangle(new Rectangle(_dragX, bounds.y, _dragX0 - _dragX, bounds.height));
            }
        }

        drawItems(bounds, _timeProvider, _data._expandedItems, _topIndex, nameSpace, gc);

        // draw selected time
        long time0 = _timeProvider.getTime0();
        long time1 = _timeProvider.getTime1();
        long selectedTime = _timeProvider.getSelectedTime();
        double pixelsPerNanoSec = (bounds.width - nameSpace <= RIGHT_MARGIN) ? 0 : (double) (bounds.width - nameSpace - RIGHT_MARGIN) / (time1 - time0);
        int x = bounds.x + nameSpace + (int) ((selectedTime - time0) * pixelsPerNanoSec);
        if (x >= nameSpace && x < bounds.x + bounds.width) {
            gc.setForeground(_colors.getColor(TimeGraphColorScheme.SELECTED_TIME));
            gc.drawLine(x, bounds.y, x, bounds.y + bounds.height);
        }

        // draw drag line, no line if name space is 0.
        if (DRAG_SPLIT_LINE == _dragState) {
            gc.setForeground(_colors.getColor(TimeGraphColorScheme.BLACK));
            gc.drawLine(bounds.x + nameSpace, bounds.y, bounds.x + nameSpace, bounds.y + bounds.height - 1);
        } else if (DRAG_ZOOM == _dragState && Math.max(_dragX, _dragX0) > nameSpace && _dragX != _dragX0) {
            gc.setForeground(_colors.getColor(TimeGraphColorScheme.TOOL_FOREGROUND));
            gc.drawLine(_dragX0, bounds.y, _dragX0, bounds.y + bounds.height - 1);
            gc.drawLine(_dragX, bounds.y, _dragX, bounds.y + bounds.height - 1);
        } else if (DRAG_NONE == _dragState && _mouseOverSplitLine && _timeProvider.getNameSpace() > 0) {
            gc.setForeground(_colors.getColor(TimeGraphColorScheme.RED));
            gc.drawLine(bounds.x + nameSpace, bounds.y, bounds.x + nameSpace, bounds.y + bounds.height - 1);
        }
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
        fTimeGraphProvider.postDrawControl(bounds, gc);
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
        ITimeGraphEntry entry = item._trace;
        long time0 = timeProvider.getTime0();
        long time1 = timeProvider.getTime1();
        long selectedTime = timeProvider.getSelectedTime();

        Rectangle nameRect = getNameRect(bounds, i, nameSpace);
        if (nameRect.y >= bounds.y + bounds.height) {
            return;
        }

        if (! item._trace.hasTimeEvents()) {
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
            gc.setBackground(_colors.getBkColor(false, false, false));
            gc.fillRectangle(rect);
            fTimeGraphProvider.postDrawEntry(entry, rect, gc);
            return;
        }

        // Initialize _rect1 to same values as enclosing rectangle rect
        Rectangle stateRect = Utils.clone(rect);
        boolean selected = item._selected;
        // K pixels per second
        double pixelsPerNanoSec = (rect.width <= RIGHT_MARGIN) ? 0 : (double) (rect.width - RIGHT_MARGIN) / (time1 - time0);

        if (item._trace.hasTimeEvents()) {
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
                if (drawState(_colors, event, stateRect, gc, selected, timeSelected)) {
                    lastX = x;
                }
            }
        }
        fTimeGraphProvider.postDrawEntry(entry, rect, gc);
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
        boolean hasTimeEvents = item._trace.hasTimeEvents();
        if (! hasTimeEvents) {
            gc.setBackground(_colors.getBkColorGroup(item._selected, _isInFocus));
            gc.fillRectangle(bounds);
            if (item._selected && _isInFocus) {
                gc.setForeground(_colors.getBkColor(item._selected, _isInFocus, false));
                gc.drawRectangle(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
            }
        } else {
            gc.setBackground(_colors.getBkColor(item._selected, _isInFocus, true));
            gc.setForeground(_colors.getFgColor(item._selected, _isInFocus));
            gc.fillRectangle(bounds);
        }

        // No name to be drawn
        if (_timeProvider.getNameSpace() == 0) {
            return;
        }

        int leftMargin = MARGIN + item.level * EXPAND_SIZE;
        if (item._hasChildren) {
            gc.setForeground(_colors.getFgColorGroup(false, false));
            gc.setBackground(_colors.getBkColor(false, false, false));
            Rectangle rect = Utils.clone(bounds);
            rect.x += leftMargin;
            rect.y += (bounds.height - EXPAND_SIZE) / 2;
            rect.width = EXPAND_SIZE;
            rect.height = EXPAND_SIZE;
            gc.fillRectangle(rect);
            gc.drawRectangle(rect.x, rect.y, rect.width - 1, rect.height - 1);
            int midy = rect.y + rect.height / 2;
            gc.drawLine(rect.x + 2, midy, rect.x + rect.width - 3, midy);
            if (!item._expanded) {
                int midx = rect.x + rect.width / 2;
                gc.drawLine(midx, rect.y + 2, midx, rect.y + rect.height - 3);
            }
        }
        leftMargin += EXPAND_SIZE + MARGIN;

        Image img = fTimeGraphProvider.getItemImage(item._trace);
        if (img != null) {
            // draw icon
            int imgHeight = img.getImageData().height;
            int imgWidth = img.getImageData().width;
            int x = leftMargin;
            int y = bounds.y + (bounds.height - imgHeight) / 2;
            gc.drawImage(img, x, y);
            leftMargin += imgWidth + MARGIN;
        }
        String name = item._name;
        Point size = gc.stringExtent(name);
        if (_idealNameSpace < leftMargin + size.x + MARGIN) {
            _idealNameSpace = leftMargin + size.x + MARGIN;
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
            gc.setForeground(_colors.getFgColor(item._selected, _isInFocus));
            int textWidth = Utils.drawText(gc, name, rect, true);
            leftMargin += textWidth + MARGIN;
            rect.y -= 2;

            if (hasTimeEvents) {
                // draw middle line
                int x = bounds.x + leftMargin;
                int width = bounds.width - x;
                int midy = bounds.y + bounds.height / 2;
                gc.setForeground(_colors.getColor(TimeGraphColorScheme.MID_LINE));
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

        if (visible) {
            if (colorIdx == ITimeGraphPresentationProvider.TRANSPARENT) {
                // Only draw the top and bottom borders
                gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                gc.drawLine(rect.x, rect.y, rect.x + rect.width - 1, rect.y);
                gc.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width - 1, rect.y + rect.height - 1);
                if (rect.width == 1) {
                    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                    gc.drawPoint(rect.x, rect.y - 2);
                }
                return false;
            }
            Color stateColor = null;
            if (colorIdx < fEventColorMap.length) {
                stateColor = fEventColorMap[colorIdx];
            } else {
                stateColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
            }

            boolean reallySelected = timeSelected && selected;
            if (reallySelected) {
                // modify the color?
            }
            // fill all rect area
            gc.setBackground(stateColor);
            gc.fillRectangle(rect);
            // get the border color?
            gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

            // draw bounds
            if (!reallySelected) {
                // Draw the top and bottom borders i.e. no side borders
                gc.drawLine(rect.x, rect.y, rect.x + rect.width - 1, rect.y);
                gc.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width - 1, rect.y + rect.height - 1);
            }
        } else {
            gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
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
        gc.setBackground(_colors.getBkColor(selected, _isInFocus, false));
        gc.fillRectangle(rect);
        if (_dragState == DRAG_ZOOM) {
            gc.setBackground(_colors.getBkColor(selected, _isInFocus, true));
            if (_dragX0 < _dragX) {
                gc.fillRectangle(new Rectangle(_dragX0, rect.y, _dragX - _dragX0, rect.height));
            } else if (_dragX0 > _dragX) {
                gc.fillRectangle(new Rectangle(_dragX, rect.y, _dragX0 - _dragX, rect.height));
            }
        }
        // draw middle line
        gc.setForeground(_colors.getColor(TimeGraphColorScheme.MID_LINE));
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
        if (_data._expandedItems.length == 0) {
            return;
        }
        if (SWT.HOME == e.keyCode) {
            idx = 0;
        } else if (SWT.END == e.keyCode) {
            idx = _data._expandedItems.length - 1;
        } else if (SWT.ARROW_DOWN == e.keyCode) {
            idx = getSelectedIndex();
            if (idx < 0) {
                idx = 0;
            } else if (idx < _data._expandedItems.length - 1) {
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
            if (idx >= _data._expandedItems.length) {
                idx = _data._expandedItems.length - 1;
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
                if (_data._expandedItems[idx]._hasChildren) {
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
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void focusGained(FocusEvent e) {
        _isInFocus = true;
        if (mouseScrollFilterListener == null) {
            mouseScrollFilterListener = new Listener() {
                // This filter is used to prevent horizontal scrolling of the view
                // when the mouse wheel is used to zoom
                @Override
                public void handleEvent(Event event) {
                    event.doit = false;
                }
            };
            getDisplay().addFilter(SWT.MouseWheel, mouseScrollFilterListener);
        }
        redraw();
    }

    @Override
    public void focusLost(FocusEvent e) {
        _isInFocus = false;
        if (mouseScrollFilterListener != null) {
            getDisplay().removeFilter(SWT.MouseWheel, mouseScrollFilterListener);
            mouseScrollFilterListener = null;
        }
        if (DRAG_NONE != _dragState) {
            setCapture(false);
            _dragState = DRAG_NONE;
        }
        redraw();
    }

    /**
     * @return If the current view is focused
     */
    public boolean isInFocus() {
        return _isInFocus;
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
            setCursor(_WaitCursor);
            _isWaitCursor = true;
        } else {
            setCursor(null);
            _isWaitCursor = false;
        }

        // Get ready for next mouse move
        _isDragCursor3 = false;
    }

    /**
     * <p>
     * If the x, y position is over the vertical split line (name to time
     * ranges), then change the cursor to a drag cursor to indicate the user the
     * possibility of resizing
     * </p>
     *
     * @param x
     * @param y
     */
    void updateCursor(int x, int y) {
        // if Wait cursor not active, check for the need to change to a drag
        // cursor
        if (_isWaitCursor == false) {
            boolean isSplitLine = isOverSplitLine(x);
            // No dragcursor is name space is fixed to zero
            if (isSplitLine && !_isDragCursor3 && _timeProvider.getNameSpace() > 0) {
                setCursor(_dragCursor3);
                _isDragCursor3 = true;
            } else if (!isSplitLine && _isDragCursor3) {
                setCursor(null);
                _isDragCursor3 = false;
            }
        }
    }

    @Override
    public void mouseMove(MouseEvent e) {
        if (null == _timeProvider) {
            return;
        }
        Point size = getCtrlSize();
        if (DRAG_TRACE_ITEM == _dragState) {
            int nameWidth = _timeProvider.getNameSpace();
            int x = e.x - nameWidth;
            if (x > 0 && size.x > nameWidth && _dragX != x) {
                _dragX = x;
                double pixelsPerNanoSec = (size.x - nameWidth <= RIGHT_MARGIN) ? 0 : (double) (size.x - nameWidth - RIGHT_MARGIN) / (_time1bak - _time0bak);
                long timeDelta = (long) ((pixelsPerNanoSec == 0) ? 0 : ((_dragX - _dragX0) / pixelsPerNanoSec));
                long time1 = _time1bak - timeDelta;
                long maxTime = _timeProvider.getMaxTime();
                if (time1 > maxTime) {
                    time1 = maxTime;
                }
                long time0 = time1 - (_time1bak - _time0bak);
                if (time0 < _timeProvider.getMinTime()) {
                    time0 = _timeProvider.getMinTime();
                    time1 = time0 + (_time1bak - _time0bak);
                }
                _timeProvider.setStartFinishTime(time0, time1);
            }
        } else if (DRAG_SPLIT_LINE == _dragState) {
            _dragX = e.x;
            _timeProvider.setNameSpace(e.x);
        } else if (DRAG_ZOOM == _dragState) {
            _dragX = Math.min(Math.max(e.x, _timeProvider.getNameSpace()), size.x - RIGHT_MARGIN);
            redraw();
        } else if (DRAG_NONE == _dragState) {
            boolean mouseOverSplitLine = isOverSplitLine(e.x);
            if (_mouseOverSplitLine != mouseOverSplitLine) {
                redraw();
            }
            _mouseOverSplitLine = mouseOverSplitLine;
        }
        updateCursor(e.x, e.y);
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
        if (null == _timeProvider) {
            return;
        }
        if (1 == e.button && (e.stateMask & SWT.BUTTON_MASK) == 0) {
            if (isOverSplitLine(e.x) && _timeProvider.getNameSpace() != 0) {
                _timeProvider.setNameSpace(_idealNameSpace);
                boolean mouseOverSplitLine = isOverSplitLine(e.x);
                if (_mouseOverSplitLine != mouseOverSplitLine) {
                    redraw();
                }
                _mouseOverSplitLine = mouseOverSplitLine;
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
        if (_dragState != DRAG_NONE || null == _timeProvider) {
            return;
        }
        int idx;
        if (1 == e.button) {
            int nameSpace = _timeProvider.getNameSpace();
            if (nameSpace != 0) {
                if (isOverSplitLine(e.x)) {
                    _dragState = DRAG_SPLIT_LINE;
                    _dragX = _dragX0 = e.x;
                    _time0bak = _timeProvider.getTime0();
                    _time1bak = _timeProvider.getTime1();
                    redraw();
                    return;
                }
            }

            idx = getItemIndexAtY(e.y);
            if (idx >= 0) {
                Item item = _data._expandedItems[idx];
                if (item._hasChildren && e.x < nameSpace && e.x < MARGIN + (item.level + 1) * EXPAND_SIZE) {
                    toggle(idx);
                } else {
                    long hitTime = getTimeAtX(e.x);
                    if (hitTime >= 0) {
                        // _timeProvider.setSelectedTimeInt(hitTime, false);
                        setCapture(true);
                        _dragState = DRAG_TRACE_ITEM;
                        _dragX = _dragX0 = e.x - nameSpace;
                        _time0bak = _timeProvider.getTime0();
                        _time1bak = _timeProvider.getTime1();
                    }
                }
                selectItem(idx, false);
                fireSelectionChanged();
            } else {
                selectItem(idx, false); // clear selection
                redraw();
                fireSelectionChanged();
            }
        } else if (3 == e.button) {
            if (_timeProvider.getTime0() == _timeProvider.getTime1() || getCtrlSize().x - _timeProvider.getNameSpace() <= 0) {
                return;
            }
            setCapture(true);
            _dragX = _dragX0 = Math.min(Math.max(e.x, _timeProvider.getNameSpace()), getCtrlSize().x - RIGHT_MARGIN);
            _dragState = DRAG_ZOOM;
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if (fPendingMenuDetectEvent != null && e.button == 3) {
            menuDetected(fPendingMenuDetectEvent);
        }
        if (DRAG_NONE != _dragState) {
            setCapture(false);
            if (e.button == 1 && DRAG_TRACE_ITEM == _dragState) {
                if (_dragX == _dragX0) { // click without drag
                    long time = getTimeAtX(e.x);
                    _timeProvider.setSelectedTimeNotify(time, false);
                } else {
                    // Notify time provider to check the need for listener
                    // notification
                    _timeProvider.notifyStartFinishTime();
                }
                _dragState = DRAG_NONE;
            } else if (e.button == 1 && DRAG_SPLIT_LINE == _dragState) {
                redraw();
                _dragState = DRAG_NONE;
            } else if (e.button == 3 && DRAG_ZOOM == _dragState) {
                int nameWidth = _timeProvider.getNameSpace();
                if (Math.max(_dragX, _dragX0) > nameWidth && _dragX != _dragX0) {
                    long time0 = getTimeAtX(_dragX0);
                    long time1 = getTimeAtX(_dragX);
                    if (time0 < time1) {
                        _timeProvider.setStartFinishTimeNotify(time0, time1);
                    } else {
                        _timeProvider.setStartFinishTimeNotify(time1, time0);
                    }
                } else {
                    redraw();
                }
                _dragState = DRAG_NONE;
            }
        }
    }

    @Override
    public void mouseEnter(MouseEvent e) {
    }

    @Override
    public void mouseExit(MouseEvent e) {
        if (_mouseOverSplitLine) {
            _mouseOverSplitLine = false;
            redraw();
        }
    }

    @Override
    public void mouseHover(MouseEvent e) {
    }

    @Override
    public void mouseScrolled(MouseEvent e) {
        if ((mouseScrollFilterListener == null) || _dragState != DRAG_NONE) {
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
            } else if (e.y >= 0 && e.y < getCtrlSize().y && e.x < _timeProvider.getNameSpace()) {
                // over the name space
                zoomScroll = false;
            } else {
                zoomScroll = true;
            }
        }
        if (zoomScroll && _timeProvider.getTime0() != _timeProvider.getTime1()) {
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
        } else if (e.widget == getHorizontalBar() && null != _timeProvider) {
            int start = getHorizontalBar().getSelection();
            long time0 = _timeProvider.getTime0();
            long time1 = _timeProvider.getTime1();
            long timeMin = _timeProvider.getMinTime();
            long timeMax = _timeProvider.getMaxTime();
            long delta = timeMax - timeMin;

            long range = time1 - time0;
            // _timeRangeFixed = true;
            time0 = timeMin + Math.round(delta * ((double) start / H_SCROLLBAR_MAX));
            time1 = time0 + range;

            // TODO: Follow-up with Bug 310310
            // In Linux SWT.DRAG is the only value received
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=310310
            if (e.detail == SWT.DRAG) {
                _timeProvider.setStartFinishTime(time0, time1);
            } else {
                _timeProvider.setStartFinishTimeNotify(time0, time1);
            }
        }
    }

    /**
     * @return The current visibility of the vertical scroll bar
     */
    public boolean isVisibleVerticalScroll() {
        return _visibleVerticalScroll;
    }

    @Override
    public int getBorderWidth() {
        return _borderWidth;
    }

    /**
     * Set the border width
     *
     * @param borderWidth
     *            The width
     */
    public void setBorderWidth(int borderWidth) {
        this._borderWidth = borderWidth;
    }

    /**
     * @return The current height of the header row
     */
    public int getHeaderHeight() {
        return _headerHeight;
    }

    /**
     * Set the height of the header row
     *
     * @param headerHeight
     *            The height
     */
    public void setHeaderHeight(int headerHeight) {
        this._headerHeight = headerHeight;
    }

    /**
     * @return The height of regular item rows
     */
    public int getItemHeight() {
        return _itemHeight;
    }

    /**
     * Set the height of regular itew rows
     *
     * @param rowHeight
     *            The height
     */
    public void setItemHeight(int rowHeight) {
        this._itemHeight = rowHeight;
    }

    /**
     * Set the minimum item width
     *
     * @param width The minimum width
     */
    public void setMinimumItemWidth(int width) {
        this._minimumItemWidth = width;
    }

    /**
     * @return The minimum item width
     */
    public int getMinimumItemWidth() {
        return _minimumItemWidth;
    }

    /**
     * @return The entries that are currently filtered out
     */
    public Vector<ITimeGraphEntry> getFilteredOut() {
        return _data.getFilteredOut();
    }

    // @Override
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (listener != null) {
            if (!_selectionChangedListeners.contains(listener)) {
                _selectionChangedListeners.add(listener);
            }
        }
    }

    // @Override
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        if (listener != null) {
            _selectionChangedListeners.remove(listener);
        }
    }

    // @Override
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
        if (!_filters.contains(filter)) {
            _filters.add(filter);
        }
    }

    /**
     * @param filter The filter object to be attached to the view
     * @since 2.0
     */
    public void removeFilter(ViewerFilter filter) {
        _filters.remove(filter);
    }

    private class ItemData {
        public Item[] _expandedItems = new Item[0];
        public Item[] _items = new Item[0];
        private ITimeGraphEntry _traces[] = new ITimeGraphEntry[0];
        private boolean traceFilter[] = new boolean[0];
        private final Vector<ITimeGraphEntry> filteredOut = new Vector<ITimeGraphEntry>();
        public ITimeGraphPresentationProvider provider;

        public ItemData() {
        }

        Item findItem(ITimeGraphEntry entry) {
            if (entry == null) {
                return null;
            }

            for (int i = 0; i < _items.length; i++) {
                Item item = _items[i];
                if (item._trace == entry) {
                    return item;
                }
            }

            return null;
        }

        int findItemIndex(ITimeGraphEntry trace) {
            if (trace == null) {
                return -1;
            }

            for (int i = 0; i < _expandedItems.length; i++) {
                Item item = _expandedItems[i];
                if (item._trace == trace) {
                    return i;
                }
            }

            return -1;
        }

        public void refreshData() {
            List<Item> itemList = new ArrayList<Item>();
            filteredOut.clear();
            ITimeGraphEntry selection = getSelectedTrace();
            for (int i = 0; i < _traces.length; i++) {
                ITimeGraphEntry entry = _traces[i];
                refreshData(itemList, null, 0, entry);
            }
            _items = itemList.toArray(new Item[0]);
            updateExpandedItems();
            if (selection != null) {
                for (Item item : _expandedItems) {
                    if (item._trace == selection) {
                        item._selected = true;
                        break;
                    }
                }
            }
        }

        private void refreshData(List<Item> itemList, Item parent, int level, ITimeGraphEntry entry) {
            Item item = new Item(entry, entry.getName(), level);
            if (parent != null) {
                parent.children.add(item);
            }
            item.itemHeight = provider.getItemHeight(entry);
            itemList.add(item);
            if (entry.hasChildren()) {
                item._expanded = true;
                item._hasChildren = true;
                for (ITimeGraphEntry child : entry.getChildren()) {
                    refreshData(itemList, item, level + 1, child);
                }
            }
        }

        public void updateExpandedItems() {
            List<Item> expandedItemList = new ArrayList<Item>();
            for (int i = 0; i < _traces.length; i++) {
                ITimeGraphEntry entry = _traces[i];
                Item item = findItem(entry);
                refreshExpanded(expandedItemList, item);
            }
            _expandedItems = expandedItemList.toArray(new Item[0]);
        }

        private void refreshExpanded(List<Item> expandedItemList, Item item) {
            // Check for filters
            boolean display = true;
            for (ViewerFilter filter : _filters) {
                if (!filter.select(null, item._trace.getParent(), item._trace)) {
                    display = false;
                    break;
                }
            }
            if (display) {
                expandedItemList.add(item);
                if (item._hasChildren && item._expanded) {
                    for (Item child : item.children) {
                        refreshExpanded(expandedItemList, child);
                    }
                }
            }
        }

        public void refreshData(ITimeGraphEntry traces[]) {
            if (traces == null || traces.length == 0) {
                traceFilter = null;
            } else if (traceFilter == null || traces.length != traceFilter.length) {
                traceFilter = new boolean[traces.length];
                java.util.Arrays.fill(traceFilter, true);
            }

            _traces = traces;
            refreshData();
        }

        public ITimeGraphEntry[] getTraces() {
            return _traces;
        }

        public boolean[] getTraceFilter() {
            return traceFilter;
        }

        public Vector<ITimeGraphEntry> getFilteredOut() {
            return filteredOut;
        }
    }

    private class Item {
        public boolean _expanded;
        public boolean _selected;
        public boolean _hasChildren;
        public int itemHeight;
        public int level;
        public List<Item> children;
        public String _name;
        public ITimeGraphEntry _trace;

        public Item(ITimeGraphEntry trace, String name, int level) {
            this._trace = trace;
            this._name = name;
            this.level = level;
            this.children = new ArrayList<Item>();
        }

        @Override
        public String toString() {
            return _name;
        }
    }

    /**
     * @since 1.2
     */
    @Override
    public void menuDetected(MenuDetectEvent e) {
        if (null == _timeProvider) {
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
            if (_dragState != DRAG_ZOOM || _dragX != _dragX0) {
                return;
            }
        } else {
            if (_dragState != DRAG_NONE) {
                return;
            }
        }
        Point p = toControl(e.x, e.y);
        int idx = getItemIndexAtY(p.y);
        if (idx >= 0 && idx < _data._expandedItems.length) {
            Item item = _data._expandedItems[idx];
            ITimeGraphEntry entry = item._trace;
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


