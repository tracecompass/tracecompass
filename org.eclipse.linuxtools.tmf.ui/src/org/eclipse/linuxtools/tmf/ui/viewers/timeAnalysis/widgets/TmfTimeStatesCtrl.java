/*****************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    Ruslan A. Scherbakov, Intel - Initial API and implementation
 *    Alvaro Sanchex-Leon - Udpated for TMF
 *
 * $Id: ThreadStatesCtrl.java,v 1.15 2008/07/11 13:49:01 aalexeev Exp $ 
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.linuxtools.tmf.ui.internal.Messages;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeAnalysisProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.TimeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * @author alvaro
 * 
 */
public class TmfTimeStatesCtrl extends TraceCtrl implements FocusListener, KeyListener, MouseMoveListener, MouseListener, MouseWheelListener, ControlListener, SelectionListener, MouseTrackListener, TraverseListener, ISelectionProvider {

    private static final int DRAG_NONE = 0;
    private static final int DRAG_TRACE_ITEM = 1;
    private static final int DRAG_GROUP_ITEM = 2;
    private static final int DRAG_SPLIT_LINE = 3;
    public static final boolean DEFAULT_DRAW_THREAD_JOIN = true;
    public static final boolean DEFAULT_DRAW_THREAD_WAIT = true;
    public static final boolean DEFAULT_DRAW_THREAD_RELEASE = true;
    public static final int H_SCROLLBAR_MAX = Integer.MAX_VALUE - 1;

    private static final double zoomCoeff = 1.5;

    private ITimeDataProvider _timeProvider;
    private boolean _isInFocus = false;
    private boolean _isDragCursor3 = false;
    private boolean _isWaitCursor = true;
    private boolean _mouseHover = false;
    private int _itemHeightDefault = 19;
    private int _itemHeight = _itemHeightDefault;
    private int _minimumItemWidth = 0;
    private int _topItem = 0;
    private int _dragState = DRAG_NONE;
    private int _hitIdx = 0;
    private int _dragX0 = 0;
    private int _dragX = 0;
    private int _idealNameWidth = 0;
    // private double _timeStep = 10000000;
    private long _time0bak;
    private long _time1bak;
    private TmfTimeAnalysisProvider utilImpl = null;
    private ItemData _data = null;
    private List<SelectionListener> _selectionListeners;
    private List<ISelectionChangedListener> _selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
    private Rectangle _rect0 = new Rectangle(0, 0, 0, 0);
    private Rectangle _rect1 = new Rectangle(0, 0, 0, 0);
    private Cursor _dragCursor3;
    private Cursor _WaitCursor;
    private boolean drawTracesInteraction = false;
    private boolean drawTraceJoins = DEFAULT_DRAW_THREAD_JOIN;
    private boolean drawTraceWaits = DEFAULT_DRAW_THREAD_WAIT;
    private boolean drawTraceReleases = DEFAULT_DRAW_THREAD_RELEASE;

    // Vertical formatting formatting for the state control view
    private boolean _visibleVerticalScroll = true;
    private int _borderWidth = 0;
    private int _headerHeight = 0;

    private Listener mouseScrollFilterListener;

    public TmfTimeStatesCtrl(Composite parent, TraceColorScheme colors, TmfTimeAnalysisProvider rutilImp) {

        super(parent, colors, SWT.NO_BACKGROUND | SWT.V_SCROLL | SWT.H_SCROLL | SWT.DOUBLE_BUFFERED);

        this.utilImpl = rutilImp;
        _data = new ItemData(utilImpl);

        addFocusListener(this);
        addMouseListener(this);
        addMouseMoveListener(this);
        addMouseTrackListener(this);
        addMouseWheelListener(this);
        addTraverseListener(this);
        addKeyListener(this);
        addControlListener(this);
        ScrollBar scrollVer = getVerticalBar();
        ScrollBar scrollHor = getHorizontalBar();
        if (scrollVer != null) {
            scrollVer.addSelectionListener(this);
            scrollVer.setVisible(_visibleVerticalScroll);
        }

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
    }

    public void setTimeProvider(ITimeDataProvider timeProvider) {
        _timeProvider = timeProvider;
        adjustScrolls();
        redraw();
    }

    public void addSelectionListener(SelectionListener listener) {
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (null == _selectionListeners)
            _selectionListeners = new ArrayList<SelectionListener>();
        _selectionListeners.add(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        if (null != _selectionListeners)
            _selectionListeners.remove(listener);
    }

    public void fireSelectionChanged() {
        if (null != _selectionListeners) {
            Iterator<SelectionListener> it = _selectionListeners.iterator();
            while (it.hasNext()) {
                SelectionListener listener = it.next();
                listener.widgetSelected(null);
            }
        }
    }

    public void fireDefaultSelection() {
        if (null != _selectionListeners) {
            Iterator<SelectionListener> it = _selectionListeners.iterator();
            while (it.hasNext()) {
                SelectionListener listener = it.next();
                listener.widgetDefaultSelected(null);
            }
        }
    }

    public Object[] getTraces() {
        return _data.getTraces();
    }

    public boolean[] getTraceFilter() {
        return _data.getTraceFilter();
    }

    public void refreshData() {
        _data.refreshData();
        adjustScrolls();
        redraw();
    }

    public void refreshData(Object traces[]) {
        _data.refreshData(traces);
        adjustScrolls();
        redraw();
    }

    public void refreshPartial(ITmfTimeAnalysisEntry parent, TimeEvent item) {
        _data.refreshPartial(parent, item);
        adjustScrolls();
        redraw();
    }

    public void adjustScrolls() {
        if (null == _timeProvider) {
            getVerticalBar().setValues(0, 1, 1, 1, 1, 1);
            getHorizontalBar().setValues(0, 1, 1, 1, 1, 1);
            return;
        }

        // Vertical scroll bar
        int page = countPerPage();
        if (_topItem + page > _data._items.length)
            _topItem = _data._items.length - page;
        if (_topItem < 0)
            _topItem = 0;
        getVerticalBar().setValues(_topItem, 0, _data._items.length, page, 1, page);

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
        if (idx < 0) {
            for (idx = 0; idx < _data._items.length; idx++) {
                if (((Item) _data._items[idx])._selected)
                    break;
            }
        }
        if (idx >= _data._items.length)
            return changed;
        if (idx < _topItem) {
            _topItem = idx;
            getVerticalBar().setSelection(_topItem);
            if (redraw)
                redraw();
            changed = true;
        } else {
            int page = countPerPage();
            if (idx >= _topItem + page) {
                _topItem = idx - page + 1;
                getVerticalBar().setSelection(_topItem);
                if (redraw)
                    redraw();
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public ISelection getSelection() {
        PlainSelection sel = new PlainSelection();
        ITmfTimeAnalysisEntry trace = getSelectedTrace();
        if (null != trace && null != _timeProvider) {
            long selectedTime = _timeProvider.getSelectedTime();
            ITimeEvent event = Utils.findEvent(trace, selectedTime, 0);
            if (event != null)
                sel.add(event);
            else
                sel.add(trace);
        }
        return sel;
    }

    public ISelection getSelectionTrace() {
        PlainSelection sel = new PlainSelection();
        ITmfTimeAnalysisEntry trace = getSelectedTrace();
        if (null != trace) {
            sel.add(trace);
        }
        return sel;
    }

    public void selectTrace(int n) {
        if (n != 1 && n != -1)
            return;
        boolean changed = false;
        int lastSelection = -1;
        for (int i = 0; i < _data._items.length; i++) {
            Item item = (Item) _data._items[i];
            if (item._selected) {
                lastSelection = i;
                if (1 == n && i < _data._items.length - 1) {
                    item._selected = false;
                    if (item._hasChildren)
                        _data.expandItem(i, true);
                    item = (Item) _data._items[i + 1];
                    if (item._hasChildren) {
                        _data.expandItem(i + 1, true);
                        item = (Item) _data._items[i + 2];
                    }
                    item._selected = true;
                    changed = true;
                } else if (-1 == n && i > 0) {
                    i--;
                    Item prevItem = (Item) _data._items[i];
                    if (prevItem._hasChildren) {
                        if (prevItem._expanded) {
                            if (i > 0) {
                                i--;
                                prevItem = (Item) _data._items[i];
                            }
                        }
                        if (!prevItem._expanded) {
                            int added = _data.expandItem(i, true);
                            prevItem = (Item) _data._items[i + added];
                            item._selected = false;
                            prevItem._selected = true;
                            changed = true;
                        }
                    } else {
                        item._selected = false;
                        prevItem._selected = true;
                        changed = true;
                    }
                }
                break;
            }
        }
        if (lastSelection < 0 && _data._items.length > 0) {
            Item item = (Item) _data._items[0];
            if (item._hasChildren) {
                _data.expandItem(0, true);
                item = (Item) _data._items[1];
                item._selected = true;
                changed = true;
            } else {
                item._selected = true;
                changed = true;
            }
        }
        if (changed) {
            ensureVisibleItem(-1, false);
            redraw();
            fireSelectionChanged();
        }
    }

    public void selectEvent(int n) {
        if (null == _timeProvider)
            return;
        ITmfTimeAnalysisEntry trace = getSelectedTrace();
        if (trace == null)
            return;
        long selectedTime = _timeProvider.getSelectedTime();
        long endTime = _timeProvider.getEndTime();
        ITimeEvent nextEvent;
        if (-1 == n && selectedTime > endTime)
            nextEvent = Utils.findEvent(trace, selectedTime, 0);
        else
            nextEvent = Utils.findEvent(trace, selectedTime, n);
        if (null == nextEvent && -1 == n)
            nextEvent = Utils.getFirstEvent(trace);
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
            }
            _timeProvider.setSelectedTimeInt(nextTime, true);
            fireSelectionChanged();
        } else if (1 == n) {
            _timeProvider.setSelectedTimeInt(endTime, true);
            fireSelectionChanged();
        }
    }

    public void selectNextEvent() {
        selectEvent(1);
        // Notify if visible time window has been adjusted
        _timeProvider.setStartFinishTimeNotify(_timeProvider.getTime0(), _timeProvider.getTime1());
    }

    public void selectPrevEvent() {
        selectEvent(-1);
        // Notify if visible time window has been adjusted
        _timeProvider.setStartFinishTimeNotify(_timeProvider.getTime0(), _timeProvider.getTime1());
    }

    public void selectNextTrace() {
        selectTrace(1);
    }

    public void selectPrevTrace() {
        selectTrace(-1);
    }

    /**
     * Zooming based on mouse cursor location with mouse scrolling
     * 
     * @param zoomIn
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
            newInterval = Math.max(Math.round((double) interval * 0.8), _timeProvider.getMinTimeInterval());
        } else {
            newInterval = (long) Math.ceil((double) interval * 1.25);
        }
        long center = time0 + Math.round(((double) (xPos - nameSpace) / timeSpace * interval));
        long newTime0 = center - Math.round((double) newInterval * (center - time0) / interval);
        long newTime1 = newTime0 + newInterval;
        _timeProvider.setStartFinishTime(newTime0, newTime1);
    }

    /**
     * zoom in using single click
     */
    public void zoomIn() {
        long _time0 = _timeProvider.getTime0();
        long _time1 = _timeProvider.getTime1();
        long _range = _time1 - _time0;
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
            time0 = selTime - (long) ((selTime - _time0) * m / _range);
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

    public void groupTraces(boolean on) {
        _data.groupTraces(on);
        adjustScrolls();
        redraw();
    }

    public void toggleTraceInteractionDrawing() {
        drawTracesInteraction = !drawTracesInteraction;
        redraw();
    }

    public void setTraceJoinDrawing(boolean on) {
        drawTraceJoins = on;
        drawTracesInteraction = true;
        redraw();
    }

    public void setTraceWaitDrawing(boolean on) {
        drawTraceWaits = on;
        drawTracesInteraction = true;
        redraw();
    }

    public void setTraceReleaseDrawing(boolean on) {
        drawTraceReleases = on;
        drawTracesInteraction = true;
        redraw();
    }

    public boolean getTracesInteractionDrawing() {
        return drawTracesInteraction;
    }

    public boolean getTraceJoinDrawing() {
        return drawTraceJoins;
    }

    public boolean getTraceWaitDrawing() {
        return drawTraceWaits;
    }

    public boolean getTraceReleaseDrawing() {
        return drawTraceReleases;
    }

    public ITmfTimeAnalysisEntry getSelectedTrace() {
        ITmfTimeAnalysisEntry trace = null;
        int idx = getSelectedIndex();
        if (idx >= 0 && _data._items[idx] instanceof TraceItem)
            trace = ((TraceItem) _data._items[idx])._trace;
        return trace;
    }

    public int getSelectedIndex() {
        int idx = -1;
        for (int i = 0; i < _data._items.length; i++) {
            Item item = (Item) _data._items[i];
            if (item._selected) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    boolean toggle(int idx) {
        boolean toggled = false;
        if (idx >= 0 && idx < _data._items.length) {
            Item item = (Item) _data._items[idx];
            if (item._hasChildren) {
                item._expanded = !item._expanded;
                _data.updateItems();
                adjustScrolls();
                redraw();
                toggled = true;
            }
        }
        return toggled;
    }

    int hitTest(int x, int y) {
        if (x < 0 || y < 0)
            return -1;
        int hit = -1;
        int idx = y / _itemHeight;
        idx += _topItem;
        if (idx < _data._items.length)
            hit = idx;
        return hit;
    }

    int hitSplitTest(int x, int y) {
        if (x < 0 || y < 0 || null == _timeProvider)
            return -1;
        int w = 4;
        int hit = -1;
        int nameWidth = _timeProvider.getNameSpace();
        if (x > nameWidth - w && x < nameWidth + w)
            hit = 1;
        return hit;
    }

    public Item getItem(Point pt) {
        int idx = hitTest(pt.x, pt.y);
        return idx >= 0 ? (Item) _data._items[idx] : null;
    }

    long hitTimeTest(int x) {
        if (null == _timeProvider)
            return -1;
        long hitTime = -1;
        Point size = getCtrlSize();
        long time0 = _timeProvider.getTime0();
        long time1 = _timeProvider.getTime1();
        int nameWidth = _timeProvider.getNameSpace();
        x -= nameWidth;
        if (x >= 0 && size.x >= nameWidth) {
            if (time1 - time0 > size.x - nameWidth - RIGHT_MARGIN) {
                // get the last possible time represented by the pixel position
                // by taking the time of the next pixel position minus 1
                // nanosecond
                hitTime = time0 + (long) ((time1 - time0) * ((double) (x + 1) / (size.x - nameWidth - RIGHT_MARGIN))) - 1;
            } else {
                hitTime = time0 + (long) ((time1 - time0) * ((double) (x) / (size.x - nameWidth - RIGHT_MARGIN)));
            }
        }
        return hitTime;
    }

    void selectItem(int idx, boolean addSelection) {
        boolean changed = false;
        if (addSelection) {
            if (idx >= 0 && idx < _data._items.length) {
                Item item = (Item) _data._items[idx];
                changed = (item._selected == false);
                item._selected = true;
            }
        } else {
            for (int i = 0; i < _data._items.length; i++) {
                Item item = (Item) _data._items[i];
                if (i == idx && item._selected == false) {
                    changed = true;
                }
                item._selected = i == idx;
            }
        }
        changed |= ensureVisibleItem(idx, true);
        if (changed)
            redraw();
    }

    public void selectItem(ITmfTimeAnalysisEntry trace, boolean addSelection) {
        Integer idx = _data.findTraceItemIndex(trace);
        if (idx != null) {
            selectItem(idx, addSelection);
        }
    }

    public int countPerPage() {
        int height = getCtrlSize().y;
        int count = 0;
        if (height > 0)
            count = height / _itemHeight;
        return count;
    }

    public int getTopIndex() {
        int idx = -1;
        if (_data._items.length > 0)
            idx = 0;
        return idx;
    }

    public int getBottomIndex() {
        int idx = _data._items.length - 1;
        return idx;
    }

    Point getCtrlSize() {
        Point size = getSize();
        if (getVerticalBar().isVisible()) {
            size.x -= getVerticalBar().getSize().x;
        }
        if (getHorizontalBar().isVisible()) {
            size.y -= getHorizontalBar().getSize().y;
        }
        return size;
    }

    void getNameRect(Rectangle rect, Rectangle bound, int idx, int nameWidth) {
        idx -= _topItem;
        rect.x = bound.x;
        rect.y = bound.y + idx * _itemHeight;
        rect.width = nameWidth;
        rect.height = _itemHeight;
    }

    void getStatesRect(Rectangle rect, Rectangle bound, int idx, int nameWidth) {
        idx -= _topItem;
        rect.x = bound.x + nameWidth;
        rect.y = bound.y + idx * _itemHeight;
        rect.width = bound.width - rect.x;
        rect.height = _itemHeight;
    }

    // private int getTraceNumber(int tid) {
    // int num = -1;
    //
    // Object[] items = _data._items;
    // for (int i = _topItem; i < items.length; i++) {
    // Item item = (Item) items[i];
    // if ((item instanceof TraceItem)) {
    // TsfTmTrace trace = ((TraceItem) item)._trace;
    // if (trace != null && trace.getId() == tid) {
    // num = i;
    // break;
    // }
    // }
    // }
    //
    // return num;
    // }

    // private void drawArrow(GC gc, int x0, int y0, int x1, int y1, Color c) {
    // gc.setForeground(c);
    // gc.drawLine(x0, y0, x1, y1);
    //
    // if (y1 > y0) {
    // gc.drawLine(x1 - 3, y1 - 3, x1, y1);
    // gc.drawLine(x1 + 3, y1 - 3, x1, y1);
    // } else {
    // gc.drawLine(x1 - 3, y1 + 3, x1, y1);
    // gc.drawLine(x1 + 3, y1 + 3, x1, y1);
    // }
    // }

    // TODO: CC: used in the removed functionality to draw thread interactions.
    // private void drawTraceThreadEvent(Rectangle bound, TsfTmEvent e,
    // TsfTmTrace trace, int nItem, int color, GC gc) {
    // if (trace == null)
    // return;
    //
    // int tid = trace.getId();
    // if (tid < 0 || getTraceNumber(tid) == -1)
    // return;
    //
    // int nameWidth = _timeProvider.getNameSpace();
    //
    // double time0 = _timeProvider.getTime0();
    // double time1 = _timeProvider.getTime1();
    // if (time0 == time1)
    // return;
    //
    // int xr = bound.x + nameWidth;
    // double K = (double) (bound.width - xr) / (time1 - time0);
    //
    // int x0 = xr + (int) ((e.getTime() - time0) * K);
    // if (x0 < xr)
    // x0 = xr;
    //
    // int x1 = xr + (int) ((trace.getStartTime() - time0) * K);
    // if (x1 < xr)
    // return;
    //
    // int y0 = bound.y + (nItem - _topItem) * _itemHeight + 3
    // + (_itemHeight - 6) / 2;
    // int y1 = bound.y + (getTraceNumber(tid) - _topItem) * _itemHeight + 3
    // + (_itemHeight - 6) / 2;
    //
    // drawArrow(gc, x0, y0, x1, y1, _colors.getColor(color));
    // }

    public void drawTraceEvent(Rectangle bound, ITimeEvent e, int nItem, int color, GC gc) {
        int nameWidth = _timeProvider.getNameSpace();

        long time0 = _timeProvider.getTime0();
        long time1 = _timeProvider.getTime1();
        if (time0 == time1)
            return;

        int xr = bound.x + nameWidth;
        double pixelsPerNanoSec = (bound.width - xr <= RIGHT_MARGIN) ? 0 : (double) (bound.width - xr - RIGHT_MARGIN) / (time1 - time0);

        int x0 = xr + (int) ((e.getTime() - time0) * pixelsPerNanoSec);
        if (x0 < xr)
            return;

        int y0 = bound.y + (nItem - _topItem) * _itemHeight + 3;

        gc.setBackground(_colors.getColor(color));
        int c[] = { x0 - 3, y0 - 3, x0, y0, x0 + 3, y0 - 3 };
        gc.fillPolygon(c);
    }

    // TODO: CC:
    // private void drawExecEvent(Rectangle bound, TsfTmTraceExecEventImpl e,
    // int nitem, int color, GC gc) {
    // List runnings = e.getRunningEvents();
    // if (runnings == null)
    // return;
    //
    // int nameWidth = _timeProvider.getNameSpace();
    //
    // double time0 = _timeProvider.getTime0();
    // double time1 = _timeProvider.getTime1();
    // if (time0 == time1)
    // return;
    //
    // int xr = bound.x + nameWidth;
    // double K = (double) (bound.width - xr) / (time1 - time0);
    //
    // int x0 = xr + (int) ((e.getTime() - time0) * K);
    // if (x0 < xr)
    // x0 = xr;
    //
    // Iterator it = runnings.iterator();
    // while (it.hasNext()) {
    // TsfTmTraceRunningEventImpl re = (TsfTmTraceRunningEventImpl) it
    // .next();
    // int tid = re.getThread().getId();
    // if (tid < 0 || getThreadNumber(tid) == -1)
    // continue;
    //
    // int x1 = xr + (int) ((re.getTime() - time0) * K);
    // if (x1 < xr)
    // continue;
    //
    // int y0 = bound.y + (nitem - _topItem) * _itemHeight + 3
    // + (_itemHeight - 6) / 2;
    // int y1 = bound.y + (getThreadNumber(tid) - _topItem) * _itemHeight
    // + 3 + (_itemHeight - 6) / 2;
    //
    // drawArrow(gc, x0, y0, x1, y1, _colors.getColor(color));
    // }
    // }

    public void drawTraceInteractions(Rectangle bound, GC gc) {
        // int nameWidth = _timeProvider.getNameSpace();
        // Object[] items = _data._items;
        //
        // double time0 = _timeProvider.getTime0();
        // double time1 = _timeProvider.getTime1();
        //
        // if (time0 == time1)
        // return;
        //
        // int xr = bound.x + nameWidth;
        // double K = (double) (bound.width - xr) / (time1 - time0);

        // for (int i = 0; i < items.length; i++) {
        // Item item = (Item) items[i];
        // if (!(item instanceof TraceItem))
        // continue;
        //
        // TsfTmTrace trace = ((TraceItem) item)._trace;
        // if (trace == null)
        // continue;
        //
        // List<TsfTmEvent> list = trace.getTraceEvents();
        // Iterator<TsfTmEvent> it = list.iterator();
        // while (it.hasNext()) {
        // TsfTmEvent te = (TsfTmEvent) it.next();
        // TODO: CC: Thread Interactions,
        // This needs to be accessed externally via a specific
        // implementation.
        // if (te instanceof TsfTmTraceStartThreadEventImpl) {
        // TsfTmTrace child = ((TsfTmTraceStartThreadEventImpl) te)
        // .getStartedThread();
        // drawThreadThreadEvent(bound, te, child, i,
        // TraceColorScheme.TI_START_THREAD, gc);
        // } else if (te instanceof TsfTmTraceHandoffLockEventImpl) {
        // if (drawThreadReleases)
        // drawExecEvent(bound, (TsfTmTraceExecEventImpl) te, i,
        // TraceColorScheme.TI_HANDOFF_LOCK, gc);
        // } else if (te instanceof TsfTmTraceNotifyAllEventImpl) {
        // if (drawThreadWaits)
        // drawExecEvent(bound, (TsfTmTraceExecEventImpl) te, i,
        // TraceColorScheme.TI_NOTIFY_ALL, gc);
        // } else if (te instanceof TsfTmTraceNotifyEventImpl) {
        // if (drawThreadWaits)
        // drawExecEvent(bound, (TsfTmTraceExecEventImpl) te, i,
        // TraceColorScheme.TI_NOTIFY, gc);
        // } else if (te instanceof
        // TsfTmTraceDeadAndNotifyJoinedEventImpl) {
        // if (drawThreadJoins)
        // drawExecEvent(bound, (TsfTmTraceExecEventImpl) te, i,
        // TraceColorScheme.TI_NOTIFY_JOINED, gc);
        // } else if (te instanceof TsfTmTraceInterruptThreadEventImpl)
        // {
        // if (drawThreadWaits)
        // drawExecEvent(bound, (TsfTmTraceExecEventImpl) te, i,
        // TraceColorScheme.TI_INTERRUPT, gc);
        // } else if (te instanceof
        // TsfTmTraceWaitTimeoutExceedEventImpl) {
        // drawThreadEvent(bound, te, i,
        // TraceColorScheme.TI_WAIT_EXCEEDED, gc);
        // }
        // }
        // }
    }

    @Override
    void paint(Rectangle bound, PaintEvent e) {
        GC gc = e.gc;
        gc.setBackground(_colors.getColor(TraceColorScheme.BACKGROUND));
        drawBackground(gc, bound.x, bound.y, bound.width, bound.height);

        if (bound.width < 2 || bound.height < 2 || null == _timeProvider)
            return;

        _idealNameWidth = 0;
        int nameWidth = _timeProvider.getNameSpace();
        long time0 = _timeProvider.getTime0();
        long time1 = _timeProvider.getTime1();
        long endTime = _timeProvider.getEndTime();
        long selectedTime = _timeProvider.getSelectedTime();
        // draw trace states
        Object[] items = _data._items;
        for (int i = _topItem; i < items.length; i++) {
            Item item = (Item) items[i];

            getNameRect(_rect0, bound, i, nameWidth);
            if (_rect0.y >= bound.y + bound.height)
                break;

            if (item instanceof GroupItem) {
                getStatesRect(_rect1, bound, i, nameWidth);
                _rect0.width += _rect1.width;
                drawName(item, _rect0, gc);
            } else {
                drawName(item, _rect0, gc);
            }
            getStatesRect(_rect0, bound, i, nameWidth);
            drawItemDataDurations(item, _rect0, time0, time1, endTime, selectedTime, gc);
        }

        if (drawTracesInteraction)
            drawTraceInteractions(bound, e.gc);

        // draw empty name space background
        if (_itemHeight * items.length < bound.height) {
            gc.setBackground(_colors.getBkColor(false, false, true));
            drawBackground(gc, bound.x, _itemHeight * items.length, nameWidth, bound.height - _itemHeight * items.length);
        }

        // draw drag line, no line if name space is 0.
        if (DRAG_SPLIT_LINE == _dragState) {
            gc.setForeground(_colors.getColor(TraceColorScheme.BLACK));
            gc.drawLine(bound.x + nameWidth, bound.y, bound.x + nameWidth, bound.y + bound.height - 1);
        } else if (DRAG_NONE == _dragState && _mouseHover && _timeProvider.getNameSpace() > 0) {
            gc.setForeground(_colors.getColor(TraceColorScheme.RED));
            gc.drawLine(bound.x + nameWidth, bound.y, bound.x + nameWidth, bound.y + bound.height - 1);
        }
    }

    void drawName(Item item, Rectangle rect, GC gc) {
        // No name to be drawn
        if (_timeProvider.getNameSpace() == 0)
            return;
        boolean group = item instanceof GroupItem;

        int elemHeight = rect.height / 2;
        int elemWidth = elemHeight;
        String name = item._name;
        if (group) {
            gc.setBackground(_colors.getBkColorGroup(item._selected, _isInFocus));
            gc.fillRectangle(rect);
            if (item._selected && _isInFocus) {
                gc.setForeground(_colors.getBkColor(item._selected, _isInFocus, false));
                gc.drawRectangle(rect.x, rect.y, rect.width - 2, rect.height - 2);
            }
            gc.setForeground(_colors.getBkColor(false, false, false));
            gc.drawLine(rect.x, rect.y + rect.height - 1, rect.width - 1, rect.y + rect.height - 1);
            gc.setForeground(_colors.getFgColorGroup(false, false));
            gc.setBackground(_colors.getBkColor(false, false, false));
            Utils.init(_rect1, rect);
            _rect1.x += MARGIN;
            _rect1.y += (rect.height - elemHeight) / 2;
            _rect1.width = elemWidth;
            _rect1.height = elemHeight;
            // Get the icon rectangle in the group items
            gc.fillRectangle(_rect1);
            gc.drawRectangle(_rect1.x, _rect1.y, _rect1.width - 1, _rect1.height - 1);
            int p = _rect1.y + _rect1.height / 2;
            gc.drawLine(_rect1.x + 2, p, _rect1.x + _rect1.width - 3, p);
            if (!item._expanded) {
                p = _rect1.x + _rect1.width / 2;
                gc.drawLine(p, _rect1.y + 2, p, _rect1.y + _rect1.height - 3);
            }
            gc.setForeground(_colors.getFgColorGroup(item._selected, _isInFocus));
            elemWidth += MARGIN;
        } else {
            gc.setBackground(_colors.getBkColor(item._selected, _isInFocus, true));
            gc.setForeground(_colors.getFgColor(item._selected, _isInFocus));
            gc.fillRectangle(rect);
            Utils.init(_rect1, rect);
            _rect1.x += MARGIN;
            // draw icon
            ITmfTimeAnalysisEntry trace = ((TraceItem) item)._trace;
            Image img = utilImpl.getItemImage(trace);
            if (null != img) {
                _rect1.y += (rect.height - img.getImageData().height) / 2;
                gc.drawImage(img, _rect1.x, _rect1.y);
            }
            elemWidth = SMALL_ICON_SIZE;
            // cut long string with "..."
            Point size = gc.stringExtent(name);
            if (_idealNameWidth < size.x)
                _idealNameWidth = size.x;
            int width = rect.width - MARGIN - MARGIN - elemWidth;
            int cuts = 0;
            while (size.x > width && name.length() > 1) {
                cuts++;
                name = name.substring(0, name.length() - 1);
                size = gc.stringExtent(name + "..."); //$NON-NLS-1$
            }
            if (cuts > 0)
                name += "..."; //$NON-NLS-1$
            elemWidth += MARGIN;
        }
        Utils.init(_rect1, rect);
        int leftMargin = MARGIN + elemWidth;
        _rect1.x += leftMargin;
        _rect1.width -= leftMargin;
        int textWidth = 0;
        // draw text
        if (_rect1.width > 0) {
            _rect1.y += 2;
            textWidth = Utils.drawText(gc, name, _rect1, true) + 8;
            _rect1.y -= 2;
        }
        // draw middle line
        if (_rect1.width > 0 && !group) {
            Utils.init(_rect1, rect);
            _rect1.x += leftMargin + textWidth;
            _rect1.width -= textWidth;
            gc.setForeground(_colors.getColor(TraceColorScheme.MID_LINE));
            int midy = _rect1.y + _rect1.height / 2;
            gc.drawLine(_rect1.x, midy, _rect1.x + _rect1.width, midy);
        }
        // gc.drawLine(_rect1.x + _rect1.width - 1, _rect1.y, _rect1.x +
        // _rect1.width - 1, _rect1.y + _rect1.height);
    }

    void drawItemData(Item item, Rectangle rect, long time0, long time1, long endTime, long selectedTime, GC gc) {
        if (rect.isEmpty())
            return;
        if (time1 <= time0) {
            gc.setBackground(_colors.getBkColor(false, false, false));
            gc.fillRectangle(rect);
            return;
        }

        Utils.init(_rect1, rect);
        boolean selected = item._selected;
        double pixelsPerNanoSec = (rect.width <= RIGHT_MARGIN) ? 0 : (double) (rect.width - RIGHT_MARGIN) / (time1 - time0);
        boolean group = item instanceof GroupItem;

        if (group) {
            // gc.setBackground(_colors.getBkColorGroup(selected, _isInFocus));
            // gc.fillRectangle(rect);
        } else if (item instanceof TraceItem) {
            ITmfTimeAnalysisEntry trace = ((TraceItem) item)._trace;

            int x0 = rect.x;
            Iterator<ITimeEvent> iterator = trace.getTraceEventsIterator();
            ITimeEvent lastEvent = null;
            if (iterator.hasNext()) {
                ITimeEvent currEvent = iterator.next();
                ITimeEvent nextEvent = null;
                long currEventTime = currEvent.getTime();
                long nextEventTime = currEventTime;
                x0 = rect.x + (int) ((currEventTime - time0) * pixelsPerNanoSec);
                int xEnd = rect.x + (int) ((time1 - time0) * pixelsPerNanoSec);
                int x1 = -1;

                // reduce rect
                _rect1.y += 3;
                _rect1.height -= 6;
                fillSpace(rect, gc, selected);

                // draw event states
                while (x0 <= xEnd && null != currEvent) {
                    boolean stopped = false;// currEvent instanceof
                    // TsfTmTraceDeadEvent;
                    if (iterator.hasNext()) {
                        nextEvent = iterator.next();
                        nextEventTime = nextEvent.getTime();
                    } else if (stopped) {
                        nextEvent = null;
                        nextEventTime = time1;
                    } else {
                        nextEvent = null;
                        nextEventTime = endTime;
                    }
                    x1 = rect.x + (int) ((nextEventTime - time0) * pixelsPerNanoSec);
                    if (x1 >= rect.x) {
                        _rect1.x = x0 >= rect.x ? x0 : rect.x;
                        _rect1.width = (x1 <= xEnd ? x1 : xEnd) - _rect1.x;
                        boolean timeSelected = currEventTime <= selectedTime && selectedTime < nextEventTime;
                        // Trace.debug("Drawing rectangle: " + _rect1.x + ","
                        // + _rect1.y + "," + _rect1.height + ", "
                        // + _rect1.width + "-->"
                        // + ((int) _rect1.x + (int) _rect1.width));
                        utilImpl.drawState(_colors, currEvent, _rect1, gc, selected, false, timeSelected);
                    }
                    lastEvent = currEvent;
                    currEvent = nextEvent;
                    currEventTime = nextEventTime;
                    x0 = x1;
                }
            }

            // fill space after last event
            int xEnd = rect.x + rect.width;
            if (x0 < xEnd) {
                _rect1.x = x0 >= rect.x ? x0 : rect.x;
                _rect1.width = xEnd - _rect1.x;
                gc.setBackground(_colors.getBkColor(selected, _isInFocus, false));
                gc.fillRectangle(_rect1);
                // draw middle line
                gc.setForeground(_colors.getColor(utilImpl.getEventColorVal(lastEvent)));
                int midy = _rect1.y + _rect1.height / 2;
                int lw = gc.getLineWidth();
                gc.setLineWidth(2);
                gc.drawLine(_rect1.x, midy, _rect1.x + _rect1.width, midy);
                gc.setLineWidth(lw);
            }
        }

        // draw selected time
        int x = rect.x + (int) ((selectedTime - time0) * pixelsPerNanoSec);
        if (x >= rect.x && x < rect.x + rect.width) {
            gc.setForeground(_colors.getColor(TraceColorScheme.SELECTED_TIME));
            if (group)
                gc.drawLine(x, rect.y + rect.height - 1, x, rect.y + rect.height);
            else
                gc.drawLine(x, rect.y, x, rect.y + rect.height);
        }
    }

    /**
     * Represent the event in series of bursts rather than sequence of states
     * 
     * @param item
     * @param rect
     *            - The container rectangle to be colored to different states
     * @param time0
     *            - Base time of all events
     * @param time1
     *            - End time of all events
     * @param endTime
     * @param selectedTime
     * @param gc
     */
    void drawItemDataBurst(Item item, Rectangle rect, long time0, long time1, long endTime, long selectedTime, GC gc) {
        if (rect.isEmpty())
            return;
        if (time1 <= time0) {
            gc.setBackground(_colors.getBkColor(false, false, false));
            gc.fillRectangle(rect);
            return;
        }

        // Initialize _rect1 to same values as enclosing rectangle rect
        Utils.init(_rect1, rect);
        boolean selected = item._selected;
        // K pixels per second
        double pixelsPerNanoSec = (rect.width <= RIGHT_MARGIN) ? 0 : (double) (rect.width - RIGHT_MARGIN) / (time1 - time0);
        // Trace.debug("Value of K: " + K + " width:" + rect.width + " time0: "
        // + time0 + " time1:" + time1 + " endTime: " + endTime);

        boolean group = item instanceof GroupItem;

        if (group) {
            // gc.setBackground(_colors.getBkColorGroup(selected, _isInFocus));
            // gc.fillRectangle(rect);
            // if (Trace.isDEBUG()) {
            // Trace.debug("Group");
            // }
        } else if (item instanceof TraceItem) {
            ITmfTimeAnalysisEntry trace = ((TraceItem) item)._trace;

            double x0 = rect.x;
            Iterator<ITimeEvent> iterator = trace.getTraceEventsIterator();
            ITimeEvent lastEvent = null;
            // Trace.debug("count is: " + count);
            if (iterator.hasNext()) {
                ITimeEvent currEvent = iterator.next();
                ITimeEvent nextEvent = null;
                long currEventTime = currEvent.getTime();
                long nextEventTime = currEventTime;
                // x0 - Points to the beginning of the event being drawn
                double step = (double) ((currEventTime - time0) * pixelsPerNanoSec);
                x0 = rect.x + step;
                // xEnd - Points to the end of the events rectangle
                double xEnd = rect.x + (double) ((time1 - time0) * pixelsPerNanoSec);
                double x1 = -1;
                //double xNext = 0;

                // Drawing rectangle is smaller than reserved space
                _rect1.y += 3;
                _rect1.height -= 6;

                // Clean up to empty line to draw on top
                fillSpace(rect, gc, selected);
                // draw event states
                while (x0 <= xEnd && null != currEvent) {
                    boolean stopped = false;// currEvent instanceof
                    // TsfTmTraceDeadEvent;
                    if (iterator.hasNext()) {
                        nextEvent = iterator.next();
                        nextEventTime = nextEvent.getTime();
                    } else if (stopped) {
                        nextEvent = null;
                        nextEventTime = time1;
                    } else {
                        nextEvent = null;
                        nextEventTime = endTime;
                        // Trace
                        // .debug("nexEventTime is endTime: "
                        // + nextEventTime);
                    }

                    // Draw it as a burst, one unit of width.
                    x1 = x0 + (int) 2;
                    if (x1 >= rect.x && x0 <= xEnd) {
                        // Fill with space until x0
                        _rect1.x = (int) (x0 >= rect.x ? x0 : rect.x);
                        _rect1.width = (int) ((x1 <= xEnd ? x1 : xEnd) - _rect1.x);
                        boolean timeSelected = currEventTime <= selectedTime && selectedTime < nextEventTime;
                        utilImpl.drawState(_colors, currEvent, _rect1, gc, selected, false, timeSelected);
                        // Trace.debug("Drawing rectangle: " + _rect1.x + ","
                        // + _rect1.y + "," + _rect1.height + ", "
                        // + _rect1.width + "-->"
                        // + ((int) _rect1.x + (int) _rect1.width));
                        // Advance rectangle to next start position and Fill
                        // with space until next event
                        _rect1.x += _rect1.width;
                        x0 = x1;
                        //xNext = rect.x + (double) ((nextEventTime - time0) * pixelsPerNanoSec);
                    }
                    // Fill space till next event
                    fillSpace(rect, gc, selected);

                    lastEvent = currEvent;
                    currEvent = nextEvent;
                    currEventTime = nextEventTime;
                    // Move x0 to the beginning of next event
                    x0 = rect.x + (double) ((nextEventTime - time0) * pixelsPerNanoSec);
                    // Trace.debug("rect.x: " + rect.x + " + " +
                    // "(nextEvenTime: "
                    // + nextEventTime + "- time0: " + time0 + ") * K: "
                    // + K + " = " + x0);
                }
            }

            // fill space after last event
            int xEnd = rect.x + rect.width;
            if (x0 < xEnd) {
                // Trace.debug("Space after last event, x0: " + x0 + ", xEnd: "
                // + xEnd);
                _rect1.x = (int) (x0 >= rect.x ? x0 : rect.x);
                _rect1.width = xEnd - _rect1.x;
                gc.setBackground(_colors.getBkColor(selected, _isInFocus, false));
                gc.fillRectangle(_rect1);
                // draw middle line
                gc.setForeground(_colors.getColor(utilImpl.getEventColorVal(lastEvent)));
                int midy = _rect1.y + _rect1.height / 2;
                int lw = gc.getLineWidth();
                gc.setLineWidth(2);
                gc.drawLine(_rect1.x, midy, _rect1.x + _rect1.width, midy);
                gc.setLineWidth(lw);
            }
        }

        // draw selected time
        int x = rect.x + (int) ((selectedTime - time0) * pixelsPerNanoSec);
        if (x >= rect.x && x < rect.x + rect.width) {
            gc.setForeground(_colors.getColor(TraceColorScheme.SELECTED_TIME));
            if (group)
                gc.drawLine(x, rect.y + rect.height - 1, x, rect.y + rect.height);
            else
                gc.drawLine(x, rect.y, x, rect.y + rect.height);
        }
    }

    /**
     * Represent the series of events with specified durations
     * 
     * @param item
     * @param rect
     *            - The container rectangle to be colored to different states
     * @param time0
     *            - Base time of all events - start of visible window
     * @param time1
     *            - End time of visible events - end time of visible window
     * @param endTime
     *            - End time of all events - may not be visible in selected
     *            visible window
     * @param selectedTime
     * @param gc
     */
    void drawItemDataDurations(Item item, Rectangle rect, long time0, long time1, long endTime, long selectedTime, GC gc) {
        if (rect.isEmpty())
            return;
        if (time1 <= time0) {
            gc.setBackground(_colors.getBkColor(false, false, false));
            gc.fillRectangle(rect);
            return;
        }

        // Initialize _rect1 to same values as enclosing rectangle rect
        Utils.init(_rect1, rect);
        boolean selected = item._selected;
        // K pixels per second
        double pixelsPerNanoSec = (rect.width <= RIGHT_MARGIN) ? 0 : (double) (rect.width - RIGHT_MARGIN) / (time1 - time0);
        // Trace.debug("Value of K: " + K + " width:" + rect.width + " time0: "
        // + time0 + " time1:" + time1 + " endTime: " + endTime);

        boolean group = item instanceof GroupItem;

        if (group) {
            // gc.setBackground(_colors.getBkColorGroup(selected, _isInFocus));
            // gc.fillRectangle(rect);
        } else if (item instanceof TraceItem) {
            ITmfTimeAnalysisEntry trace = ((TraceItem) item)._trace;

            double x0 = rect.x;
            long maxDuration = (_timeProvider.getTimeSpace() == 0) ? Long.MAX_VALUE : 1 * (_timeProvider.getTime1() - _timeProvider.getTime0()) / _timeProvider.getTimeSpace();
            Iterator<ITimeEvent> iterator = trace.getTraceEventsIterator(_timeProvider.getTime0(), _timeProvider.getTime1(), maxDuration);
            // ITimeEvent lastEvent = null;
            // if (Trace.isDEBUG()) {
            // Trace.debug("\n\t\t\tTrace: " + trace.getName()
            // + utilImpl.getTraceClassName(trace));
            // }
            // Trace.debug("count is: " + count);
            // Drawing rectangle is smaller than reserved space
            _rect1.y += 3;
            _rect1.height -= 6;

            // Clean up to empty line to draw on top
            int xEnd = rect.x + rect.width;
            fillSpace(rect, gc, selected);
            if (iterator.hasNext()) {
                ITimeEvent currEvent = iterator.next();
                ITimeEvent nextEvent = null;
                long currEventTime = currEvent.getTime();
                long currEventDuration = currEvent.getDuration();
                // initial value
                long nextEventTime = currEventTime;
                // x0 - Points to the beginning of the event being drawn
                double step = (double) ((currEventTime - time0) * pixelsPerNanoSec);
                x0 = rect.x + step;
                // xEnd - Points to the end of the events rectangle
                double x1 = -1;
                double xNext = 0;

                // draw event states
                while (/* x0 <= xEnd && */null != currEvent) {
                    boolean stopped = false;// currEvent instanceof
                    // refresh current event duration as the loop moves
                    currEventDuration = currEvent.getDuration();
                    // TsfTmTraceDeadEvent;
                    if (iterator.hasNext()) {
                        nextEvent = iterator.next();
                        nextEventTime = nextEvent.getTime();
                    } else if (stopped) {
                        nextEvent = null;
                        nextEventTime = time1;
                    } else {
                        nextEvent = null;
                        nextEventTime = endTime;
                        // Trace
                        // .debug("nexEventTime is endTime: "
                        // + nextEventTime);
                    }

                    // Calculate position to next event
                    xNext = rect.x + (double) ((nextEventTime - time0) * pixelsPerNanoSec);

                    // Calculate end position of current event
                    if (currEventDuration < 0) {
                        x1 = rect.x + (double) ((nextEventTime - time0) * pixelsPerNanoSec);
                    } else if (currEventDuration == 0) {
                        x1 = x0;
                    } else {
                        x1 = x0 + (double) ((currEventDuration) * pixelsPerNanoSec);
                    }

                    // If event end position x1 further away than start position
                    // of
                    // next event, cut width till next event
                    // Trace.debug("Next Event Pos: " + xNext
                    // + " End Of Current at: " + x1 + " Event Duration: "
                    // + currEventDuration);
                    if (currEventDuration != 0) {
                        x1 = x1 > xNext ? xNext : x1;
                    }
                    // if event end boundary is within time range
                    if (x1 >= rect.x && x0 <= xEnd) {
                        if (currEventDuration != 0) {
                            x0 = (double) (x0 >= rect.x ? x0 : rect.x);
                            _rect1.width = (int) ((x1 <= xEnd ? x1 : xEnd) - x0);
                        } else {
                            _rect1.width = 1;
                        }
                        _rect1.width = Math.max(_minimumItemWidth, _rect1.width);
                        _rect1.x = (int) x0;
                        boolean timeSelected = currEventTime <= selectedTime && selectedTime < nextEventTime;
                        utilImpl.drawState(_colors, currEvent, _rect1, gc, selected, false, timeSelected);
                        // Trace.debug("Drawing rectangle: " + _rect1.x + ","
                        // + _rect1.y + "," + _rect1.height + ", "
                        // + _rect1.width + "-->"
                        // + ((int) _rect1.x + (int) _rect1.width));
                        // Advance rectangle to next start position and Fill
                        // with space until next event
                        _rect1.x += _rect1.width;
                        x0 = _rect1.x;
                    }

                    // Fill space till next event
                    // fillSpace(rect, gc, selected, x0, xNext, xEnd);

                    // lastEvent = currEvent;
                    currEvent = nextEvent;
                    currEventTime = nextEventTime;
                    // Move x0 to the beginning of next event
                    x0 = rect.x + (double) ((nextEventTime - time0) * pixelsPerNanoSec);
                    // Trace.debug("rect.x: " + rect.x + " + " +
                    // "(nextEvenTime: "
                    // + nextEventTime + "- time0: " + time0 + ") * K: "
                    // + K + " = " + x0);
                }
            }
        }

        // draw selected time
        int x = rect.x + (int) ((double) (selectedTime - time0) * pixelsPerNanoSec);
        if (x >= rect.x && x < rect.x + rect.width) {
            gc.setForeground(_colors.getColor(TraceColorScheme.SELECTED_TIME));
            if (group)
                gc.drawLine(x, rect.y + rect.height - 1, x, rect.y + rect.height);
            else
                gc.drawLine(x, rect.y, x, rect.y + rect.height);
        }
    }

    private void fillSpace(Rectangle rect, GC gc, boolean selected) {
        gc.setBackground(_colors.getBkColor(selected, _isInFocus, false));
        gc.fillRectangle(rect);
        // draw middle line
        gc.setForeground(_colors.getColor(TraceColorScheme.MID_LINE));
        int midy = rect.y + rect.height / 2;
        gc.drawLine(rect.x, midy, rect.x + rect.width, midy);
    }

    @Override
    public void keyTraversed(TraverseEvent e) {
        if ((e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS))
            e.doit = true;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int idx = -1;
        if (SWT.HOME == e.keyCode) {
            idx = getTopIndex();
        } else if (SWT.END == e.keyCode) {
            idx = getBottomIndex();
        } else if (SWT.ARROW_DOWN == e.keyCode) {
            idx = getSelectedIndex();
            if (idx < 0)
                idx = 0;
            else if (idx < _data._items.length - 1)
                idx++;
        } else if (SWT.ARROW_UP == e.keyCode) {
            idx = getSelectedIndex();
            if (idx < 0)
                idx = 0;
            else if (idx > 0)
                idx--;
        } else if (SWT.ARROW_LEFT == e.keyCode) {
            selectPrevEvent();
        } else if (SWT.ARROW_RIGHT == e.keyCode) {
            selectNextEvent();
        } else if (SWT.PAGE_DOWN == e.keyCode) {
            int page = countPerPage();
            idx = getSelectedIndex();
            if (idx < 0)
                idx = 0;
            idx += page;
            if (idx >= _data._items.length)
                idx = _data._items.length - 1;
        } else if (SWT.PAGE_UP == e.keyCode) {
            int page = countPerPage();
            idx = getSelectedIndex();
            if (idx < 0)
                idx = 0;
            idx -= page;
            if (idx < 0)
                idx = 0;
        } else if (SWT.CR == e.keyCode) {
            idx = getSelectedIndex();
            if (idx >= 0) {
                if (_data._items[idx] instanceof TraceItem)
                    fireDefaultSelection();
                else if (_data._items[idx] instanceof GroupItem)
                    toggle(idx);
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
        redraw();
        if (mouseScrollFilterListener == null) {
            mouseScrollFilterListener = new Listener() {
                // This filter is used to prevent scrolling of the view when the
                // mouse wheel is used to zoom
                @Override
                public void handleEvent(Event event) {
                    event.doit = false;
                }
            };
            getDisplay().addFilter(SWT.MouseWheel, mouseScrollFilterListener);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        _isInFocus = false;
        if (DRAG_NONE != _dragState) {
            setCapture(false);
            _dragState = DRAG_NONE;
        }
        redraw();
        if (mouseScrollFilterListener != null) {
            getDisplay().removeFilter(SWT.MouseWheel, mouseScrollFilterListener);
            mouseScrollFilterListener = null;
        }
    }

    public boolean isInFocus() {
        return _isInFocus;
    }

    @Override
    public void mouseMove(MouseEvent e) {
        if (null == _timeProvider)
            return;
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
                if (time1 > maxTime)
                    time1 = maxTime;
                long time0 = time1 - (_time1bak - _time0bak);
                if (time0 < _timeProvider.getMinTime()) {
                    time0 = _timeProvider.getMinTime();
                    time1 = time0 + (_time1bak - _time0bak);
                }
                _timeProvider.setStartFinishTime(time0, time1);
            }
        } else if (DRAG_SPLIT_LINE == _dragState) {
            _dragX = e.x;
            _timeProvider.setNameSpace(_hitIdx + _dragX - _dragX0);
        } else if (DRAG_NONE == _dragState) {
            boolean mouseHover = hitSplitTest(e.x, e.y) > 0;
            if (_mouseHover != mouseHover)
                redraw();
            _mouseHover = mouseHover;
            // Make sure any time changes are notified to the application e.g.
            // getting back from the horizontal scroll bar or zoomed using the
            // mouse wheel
            _timeProvider.notifyStartFinishTime();
        }
        updateCursor(e.x, e.y);
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
        if (null == _timeProvider)
            return;
        if (1 == e.button) {
            int idx = hitSplitTest(e.x, e.y);
            if (idx >= 0) {
                _timeProvider.setNameSpace(_idealNameWidth + 3 * MARGIN + SMALL_ICON_SIZE);
                return;
            }
            idx = hitTest(e.x, e.y);
            if (idx >= 0) {
                selectItem(idx, false);
                if (_data._items[idx] instanceof TraceItem) {
                    fireDefaultSelection();
                }
            }
        }
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
            int idx = hitSplitTest(x, y);
            // No dragcursor is name space is fixed to zero
            if (idx > 0 && !_isDragCursor3 && _timeProvider.getNameSpace() > 0) {
                setCursor(_dragCursor3);
                _isDragCursor3 = true;
            } else if (idx <= 0 && _isDragCursor3) {
                setCursor(null);
                _isDragCursor3 = false;
            }
        }
    }

    /**
     * Provide the possibilty to control the wait cursor externally e.g. data
     * requests in progress
     * 
     * @param waitInd
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

    @Override
    public void mouseDown(MouseEvent e) {
        if (null == _timeProvider)
            return;
        int idx;
        if (1 == e.button) {
            int namewidth = _timeProvider.getNameSpace();
            if (namewidth != 0) {
                idx = hitSplitTest(e.x, e.y);
                if (idx > 0) {
                    _dragState = DRAG_SPLIT_LINE;
                    _dragX = _dragX0 = e.x;
                    _hitIdx = _timeProvider.getNameSpace();
                    ;
                    _time0bak = _timeProvider.getTime0();
                    _time1bak = _timeProvider.getTime1();
                    redraw();
                    return;
                }
            }

            idx = hitTest(e.x, e.y);
            if (idx >= 0) {
                if (_data._items[idx] instanceof TraceItem) {
                    long hitTime = hitTimeTest(e.x);
                    if (hitTime >= 0) {
                        // _timeProvider.setSelectedTimeInt(hitTime, false);
                        setCapture(true);
                        _dragState = DRAG_TRACE_ITEM;
                        _dragX = _dragX0 = e.x - _timeProvider.getNameSpace();
                        _hitIdx = idx;
                        _time0bak = _timeProvider.getTime0();
                        _time1bak = _timeProvider.getTime1();
                        return;
                    }
                } else if (_data._items[idx] instanceof GroupItem) {
                    _dragX0 = e.x;
                    _dragState = DRAG_GROUP_ITEM;
                }
                selectItem(idx, false);
                fireSelectionChanged();
            } else {
                selectItem(idx, false); // clear selection
                redraw();
            }
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if (DRAG_NONE != _dragState) {
            setCapture(false);
            if (DRAG_TRACE_ITEM == _dragState) {
                // Notify time provider to check the need for listener
                // notification
                _timeProvider.notifyStartFinishTime();
                if (_dragX == _dragX0) { // click without drag
                    long time = hitTimeTest(e.x);
                    _timeProvider.setSelectedTimeInt(time, false);
                    selectItem(_hitIdx, false);
                    fireSelectionChanged();
                }
            } else if (DRAG_GROUP_ITEM == _dragState) {
                if (e.x == _dragX0) // click without drag
                    toggle(_hitIdx);
            } else if (DRAG_SPLIT_LINE == _dragState) {
                redraw();
            }
            _dragState = DRAG_NONE;
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
            _topItem = getVerticalBar().getSelection();
            if (_topItem < 0)
                _topItem = 0;
            redraw();
        } else if (e.widget == getHorizontalBar() && null != _timeProvider) {
            int start = getHorizontalBar().getSelection();
            long time0 = _timeProvider.getTime0();
            long time1 = _timeProvider.getTime1();
            long timeMin = _timeProvider.getMinTime();
            long timeMax = _timeProvider.getMaxTime();
            long delta = timeMax - timeMin;

            long range = time1 - time0;
            // _timeRangeFixed = true;
            time0 = timeMin + (long) (delta * ((double) start / H_SCROLLBAR_MAX));
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

    @Override
    public void mouseEnter(MouseEvent e) {
    }

    @Override
    public void mouseExit(MouseEvent e) {
        if (_mouseHover) {
            _mouseHover = false;
            redraw();
        }
    }

    @Override
    public void mouseHover(MouseEvent e) {
    }

    @Override
    public void mouseScrolled(MouseEvent e) {
        if (!_isInFocus || _dragState != DRAG_NONE)
            return;
        if (e.count > 0) {
            zoom(true);
        } else if (e.count < 0) {
            zoom(false);
        }
    }

    public boolean isVisibleVerticalScroll() {
        return _visibleVerticalScroll;
    }

    public void setVisibleVerticalScroll(boolean visibleVerticalScroll) {
        ScrollBar scrollVer = getVerticalBar();
        if (scrollVer != null) {
            scrollVer.setVisible(visibleVerticalScroll);
        }
        this._visibleVerticalScroll = visibleVerticalScroll;
    }

    @Override
    public int getBorderWidth() {
        return _borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this._borderWidth = borderWidth;
    }

    public int getHeaderHeight() {
        return _headerHeight;
    }

    public void setHeaderHeight(int headerHeight) {
        this._headerHeight = headerHeight;
    }

    public int getItemHeight() {
        return _itemHeight;
    }

    public void setItemHeight(int rowHeight) {
        this._itemHeight = rowHeight;
    }

    public void setMinimumItemWidth(int width) {
        this._minimumItemWidth = width;
    }

    public Vector<ITmfTimeAnalysisEntry> getFilteredOut() {
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
        if (selection instanceof PlainSelection) {
            PlainSelection sel = (PlainSelection) selection;
            Object ob = sel.getFirstElement();
            if (ob instanceof ITmfTimeAnalysisEntry) {
                ITmfTimeAnalysisEntry trace = (ITmfTimeAnalysisEntry) ob;
                selectItem(trace, false);
            }
        }

    }
}

class Item {
    public boolean _expanded;
    public boolean _selected;
    public boolean _hasChildren;
    public String _name;

    Item(String name) {
        _name = name;
    }

    @Override
    public String toString() {
        return _name;
    }
}

class TraceItem extends Item {
    public ITmfTimeAnalysisEntry _trace;

    TraceItem(ITmfTimeAnalysisEntry trace, String name) {
        super(name);
        _trace = trace;
    }
}

class GroupItem extends Item {
    public List<ITmfTimeAnalysisEntry> _traces;

    GroupItem(String name) {
        super(name);
        _traces = new ArrayList<ITmfTimeAnalysisEntry>();
        _hasChildren = true;
    }

    void add(ITmfTimeAnalysisEntry trace) {
        _traces.add(trace);
    }
}

class ItemData {
    public Object[] _items = new Object[0];
    private Object _traces[] = new Object[0];
    private boolean traceFilter[] = new boolean[0];
    private Map<String, GroupItem> _groupTable = new HashMap<String, GroupItem>();
    private boolean _flatList = false;
    private TmfTimeAnalysisProvider utilsImp;
    private Vector<ITmfTimeAnalysisEntry> filteredOut = new Vector<ITmfTimeAnalysisEntry>();

    public ItemData(TmfTimeAnalysisProvider utils) {
        this.utilsImp = utils;
    }

    protected void groupTraces(boolean on) {
        if (_flatList == on) {
            _flatList = !on;
            updateItems();
        }
    }

    void clearGroups() {
        Iterator<GroupItem> it = _groupTable.values().iterator();
        while (it.hasNext()) {
            GroupItem group = it.next();
            group._traces.clear();
        }
    }

    void deleteEmptyGroups() {
        Iterator<GroupItem> it = _groupTable.values().iterator();
        while (it.hasNext()) {
            GroupItem group = it.next();
            if (group._traces.size() == 0)
                it.remove();
        }
    }

    TraceItem findTraceItem(ITmfTimeAnalysisEntry trace) {
        if (trace == null)
            return null;

        int traceId = trace.getId();
        TraceItem traceItem = null;

        for (int i = 0; i < _items.length; i++) {
            Object item = _items[i];
            if (item instanceof TraceItem) {
                TraceItem ti = (TraceItem) item;
                if (ti._trace.getId() == traceId) {
                    traceItem = ti;
                    break;
                }
            }
        }

        return traceItem;
    }

    Integer findTraceItemIndex(ITmfTimeAnalysisEntry trace) {
        if (trace == null)
            return null;

        for (int i = 0; i < _items.length; i++) {
            Object item = _items[i];
            if (item instanceof TraceItem) {
                TraceItem ti = (TraceItem) item;
                if (ti._trace == trace) {
                    return i;
                }
            }
        }

        return null;
    }

    public void updateItems() {
        List<Item> itemList = new ArrayList<Item>();
        String name = ""; //$NON-NLS-1$

        Iterator<GroupItem> it = _groupTable.values().iterator();
        while (it.hasNext()) {
            GroupItem group = it.next();
            if (!_flatList)
                itemList.add(group);

            if (_flatList || group._expanded) {
                Iterator<ITmfTimeAnalysisEntry> it2 = group._traces.iterator();
                while (it2.hasNext()) {
                    ITmfTimeAnalysisEntry trace = it2.next();
                    TraceItem traceItem = findTraceItem(trace);
                    name = utilsImp.composeTraceName(trace, false);
                    traceItem = new TraceItem(trace, name);
                    itemList.add(traceItem);
                }
            }
        }
        _items = itemList.toArray();
    }

    public int expandItem(int idx, boolean expand) {
        if (idx < 0 || idx >= _items.length)
            return 0;
        int ret = 0;
        Item item = (Item) _items[idx];
        if (item._hasChildren && !item._expanded) {
            item._expanded = expand;
            ret = _items.length;
            updateItems();
            ret = _items.length - ret;
        }
        return ret;
    }

    public void refreshData(Object traces[]) {
        if (traces == null || traces.length == 0) {
            traceFilter = null;
        } else if (traceFilter == null || traces.length != traceFilter.length) {
            traceFilter = new boolean[traces.length];
            java.util.Arrays.fill(traceFilter, true);
        }

        _traces = traces;
        refreshData();
    }

    /**
     * Allows to update the GUI from a stream of events handling addition one by
     * one over known TmfTaTrace parents.
     * 
     * @param parent
     * @param childItem
     */
    public void refreshPartial(ITmfTimeAnalysisEntry parent, TimeEvent childItem) {
        // Find the Trace item within the current list
//        TraceItem item = findTraceItem(parent);

        // This method is not used (yet) so this code can be commented out for
        // now
        // FIXME: Arrays.copyOf is a Java6 feature
        // if (item == null) {
        // // If the parent item is not found, make room for it in the current
        // // array
        // int length = 1;
        // Object[] traces;
        // if (_traces != null) {
        // length = _traces.length + 1;
        // traces = Arrays.copyOf(_traces, length);
        // } else {
        // traces = new Object[length];
        // }
        //
        // // Add the new parent element to the end of the array.
        // traces[length - 1] = parent;
        //
        // // update the filter array to accomodate a postion to the new
        // // element
        // traceFilter = new boolean[traces.length];
        // java.util.Arrays.fill(traceFilter, true);
        //
        // // rebuild internal data
        // _traces = traces;
        // refreshData();
        //
        // // item must be there
        // item = findTraceItem(parent);
        // }

        /*
         * Check if this is still needed!
        ITmfTimeAnalysisEntry localTraceItem = item._trace;
        // Local trace found
        Vector<TimeEvent> children = localTraceItem.getTraceEvents();
        TimeEvent lastChildIn = children.lastElement();
        long lastChildSTime = lastChildIn.getTime();
        long newChildSTime = childItem.getTime();
        if (newChildSTime < lastChildSTime) {
            // The children are expected to arrive sorted by time
            // since the new time is earlier than the last child
            // The infomation is being refreshed from start, remove all
            // children and start over
            children.clear();
        }
        // Add the new item
        children.add(childItem);
        */

    }

    public void refreshData() {
        clearGroups();
        filteredOut.clear();
        String undef = Messages.TmfTimeStatesCtrl_UNDEFINED_GROUP;
        List<GroupItem> groupList = new ArrayList<GroupItem>();
        for (int i = 0; i < _traces.length; i++) {
            ITmfTimeAnalysisEntry trace = (ITmfTimeAnalysisEntry) _traces[i];
            if (!traceFilter[i]) {
                filteredOut.add(trace);
                continue;
            }

            String groupName = trace.getGroupName();
            if (null == groupName)
                groupName = undef;

            GroupItem group = _groupTable.get(groupName);
            if (null == group) {
                group = new GroupItem(NLS.bind(Messages.TmfTimeStatesCtrl_TRACE_GROUP_LABEL, groupName));
                group._expanded = !groupName.equalsIgnoreCase("system") && !groupName.equalsIgnoreCase(undef); //$NON-NLS-1$
                _groupTable.put(groupName, group);
                groupList.add(group);
            }
            group.add(trace);
        }

        deleteEmptyGroups();
        updateItems();
    }

    public Object[] getTraces() {
        return _traces;
    }

    public boolean[] getTraceFilter() {
        return traceFilter;
    }

    public Vector<ITmfTimeAnalysisEntry> getFilteredOut() {
        return filteredOut;
    }
}
