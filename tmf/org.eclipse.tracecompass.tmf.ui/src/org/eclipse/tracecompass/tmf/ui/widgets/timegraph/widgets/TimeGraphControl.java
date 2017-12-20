/*****************************************************************************
 * Copyright (c) 2007, 2018 Intel Corporation and others
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

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.math.SaturatedArithmetic;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.Resolution;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat;
import org.eclipse.tracecompass.tmf.ui.views.ITmfTimeAligned;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphColorListener;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider2;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphTreeListener;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphViewerFilterListener;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphTreeExpansionEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

import com.google.common.collect.Iterables;

/**
 * Time graph control implementation
 *
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public class TimeGraphControl extends TimeGraphBaseControl
        implements FocusListener, KeyListener, MouseMoveListener, MouseListener,
        MouseWheelListener, MouseTrackListener, TraverseListener, ISelectionProvider,
        MenuDetectListener, ITmfTimeGraphDrawingHelper, ITimeGraphColorListener, Listener {

    /**
     * Constant indicating that all levels of the time graph should be expanded
     */
    public static final int ALL_LEVELS = AbstractTreeViewer.ALL_LEVELS;

    private static final int DRAG_MARGIN = 5;

    private static final int DRAG_NONE = 0;
    private static final int DRAG_TRACE_ITEM = 1;
    private static final int DRAG_SPLIT_LINE = 2;
    private static final int DRAG_ZOOM = 3;
    private static final int DRAG_SELECTION = 4;

    /**
     * Get item height from provider
     */
    private static final int CUSTOM_ITEM_HEIGHT = -1;

    private static final double ZOOM_FACTOR = 1.5;
    private static final double ZOOM_IN_FACTOR = 0.8;
    private static final double ZOOM_OUT_FACTOR = 1.25;

    private static final int SNAP_WIDTH = 3;
    private static final int ARROW_HOVER_MAX_DIST = 5;

    /**
     * base to height ratio
     */
    private static final double ARROW_RATIO = Math.sqrt(3) / 2;
    private static final int NO_STATUS = -1;
    private static final int STATUS_WITHOUT_CURSOR_TIME = -2;

    private static final int MAX_LABEL_LENGTH = 256;

    /** points per inch */
    private static final int PPI = 72;
    private static final int DPI = Display.getDefault().getDPI().y;

    private static final int VERTICAL_ZOOM_DELAY = 400;

    private static final String PREFERRED_WIDTH = "width"; //$NON-NLS-1$

    /** Resource manager */
    private LocalResourceManager fResourceManager = new LocalResourceManager(JFaceResources.getResources());

    /** Color map for event types */
    private Color[] fEventColorMap = null;

    private ITimeDataProvider fTimeProvider;
    private ITableLabelProvider fLabelProvider;
    private IStatusLineManager fStatusLineManager = null;
    private Tree fTree = null;
    private TimeGraphScale fTimeGraphScale = null;

    private boolean fIsInFocus = false;
    private boolean fMouseOverSplitLine = false;
    private int fGlobalItemHeight = CUSTOM_ITEM_HEIGHT;
    private int fHeightAdjustment = 0;
    private int fMaxItemHeight = 0;
    private Map<Integer, Font> fFonts = new HashMap<>();
    private boolean fBlendSubPixelEvents = false;
    private int fMinimumItemWidth = 0;
    private int fTopIndex = 0;
    private int fDragState = DRAG_NONE;
    private boolean fDragBeginMarker = false;
    private int fDragButton;
    private int fDragX0 = 0;
    private int fDragX = 0;
    private boolean fHasNamespaceFocus = false;
    /**
     * Used to preserve accuracy of modified selection
     */
    private long fDragTime0 = 0;
    private int fIdealNameSpace = 0;
    private boolean fAutoResizeColumns = true;
    private long fTime0bak;
    private long fTime1bak;
    private ITimeGraphPresentationProvider fTimeGraphProvider = null;
    private ItemData fItemData = null;
    private List<IMarkerEvent> fMarkers = null;
    private boolean fMarkersVisible = true;
    private List<SelectionListener> fSelectionListeners;
    private List<ITimeGraphTimeListener> fDragSelectionListeners;
    private final List<ISelectionChangedListener> fSelectionChangedListeners = new ArrayList<>();
    private final List<ITimeGraphTreeListener> fTreeListeners = new ArrayList<>();
    private final List<ITimeGraphViewerFilterListener> fViewerFilterListeners = new ArrayList<>();
    private final List<MenuDetectListener> fTimeGraphEntryMenuListeners = new ArrayList<>();
    private final List<MenuDetectListener> fTimeEventMenuListeners = new ArrayList<>();
    private final Cursor fDragCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_HAND);
    private final Cursor fResizeCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_IBEAM);
    private final Cursor fWaitCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT);
    private final Cursor fZoomCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_SIZEWE);
    private final Set<@NonNull ViewerFilter> fFilters = new LinkedHashSet<>();
    private MenuDetectEvent fPendingMenuDetectEvent = null;
    private boolean fGridLinesVisible = true;
    private Color fGridLineColor = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
    private boolean fHideArrows = false;
    private int fAutoExpandLevel = ALL_LEVELS;
    private Entry<ITimeGraphEntry, Integer> fVerticalZoomAlignEntry = null;
    private long fVerticalZoomAlignTime = 0;
    private int fBorderWidth = 0;
    private int fHeaderHeight = 0;

    private boolean fFirstHeightWarning = true;

    /**
     * Standard constructor
     *
     * @param parent
     *            The parent composite object
     * @param colors
     *            The color scheme to use
     */
    public TimeGraphControl(Composite parent, TimeGraphColorScheme colors) {

        super(parent, colors, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);

        fItemData = new ItemData();

        addFocusListener(this);
        addMouseListener(this);
        addMouseMoveListener(this);
        addMouseTrackListener(this);
        addMouseWheelListener(this);
        addTraverseListener(this);
        addKeyListener(this);
        addMenuDetectListener(this);
        addListener(SWT.MouseWheel, this);
        addDisposeListener((e) -> {
            fResourceManager.dispose();
            for (Font font : fFonts.values()) {
                font.dispose();
            }
        });
    }

    /**
     * Sets the timegraph provider used by this timegraph viewer.
     *
     * @param timeGraphProvider
     *            the timegraph provider
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
     */
    public ITimeGraphPresentationProvider getTimeGraphProvider() {
        return fTimeGraphProvider;
    }

    /**
     * Gets the time data provider used by this viewer.
     *
     * @return The time data provider, or <code>null</code> if not set
     * @since 2.1
     */
    public ITimeDataProvider getTimeDataProvider() {
        return fTimeProvider;
    }

    /**
     * Gets the color map used by this timegraph viewer.
     *
     * @return a color map, or <code>null</code> if not set.
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
        redraw();
    }

    /**
     * Set the label provider for the name space
     *
     * @param labelProvider
     *            The label provider
     * @since 2.3
     */
    public void setLabelProvider(ITableLabelProvider labelProvider) {
        fLabelProvider = labelProvider;
        redraw();
    }

    /**
     * Get the label provider for the name space
     *
     * @return The label provider
     * @since 2.3
     */
    public ITableLabelProvider getLabelProvider() {
        return fLabelProvider;
    }

    /**
     * Assign the status line manager
     *
     * @param statusLineManager
     *            The status line manager, or null to disable status line
     *            messages
     */
    public void setStatusLineManager(IStatusLineManager statusLineManager) {
        if (fStatusLineManager != null && statusLineManager == null) {
            fStatusLineManager.setMessage(""); //$NON-NLS-1$
        }
        fStatusLineManager = statusLineManager;
    }

    /**
     * Assign the tree that represents the name space header
     *
     * @param tree
     *            The tree
     * @since 2.3
     */
    public void setTree(Tree tree) {
        fTree = tree;
    }

    /**
     * Returns the tree control associated with this time graph control. The
     * tree is only used for column handling of the name space and contains no
     * tree items.
     *
     * @return the tree control
     * @since 2.3
     */
    public Tree getTree() {
        return fTree;
    }

    /**
     * Sets the columns for this time graph control's name space.
     *
     * @param columnNames
     *            the column names
     * @since 2.3
     */
    public void setColumns(String[] columnNames) {
        for (TreeColumn column : fTree.getColumns()) {
            column.dispose();
        }
        ControlListener controlListener = new ControlListener() {
            @Override
            public void controlResized(ControlEvent e) {
                if (fAutoResizeColumns && ((TreeColumn) e.widget).getWidth() < (Integer) e.widget.getData(PREFERRED_WIDTH)) {
                    fAutoResizeColumns = false;
                }
                redraw();
            }

            @Override
            public void controlMoved(ControlEvent e) {
                redraw();
            }
        };
        for (String columnName : columnNames) {
            TreeColumn column = new TreeColumn(fTree, SWT.LEFT);
            column.setMoveable(true);
            column.setText(columnName);
            column.pack();
            column.setData(PREFERRED_WIDTH, column.getWidth());
            column.addControlListener(controlListener);
        }
    }

    /**
     * Assign the time graph scale
     *
     * @param timeGraphScale
     *            The time graph scale
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

        if (null != fSelectionChangedListeners) {
            for (ISelectionChangedListener listener : fSelectionChangedListeners) {
                listener.selectionChanged(new SelectionChangedEvent(this, getSelection()));
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
     * Refresh the data for the thing
     */
    public void refreshData() {
        fItemData.refreshData();
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
        redraw();
    }

    /**
     * Refresh the links (arrows) of this widget
     *
     * @param events
     *            The link event list
     */
    public void refreshArrows(List<ILinkEvent> events) {
        fItemData.refreshArrows(events);
    }

    /**
     * Get the links (arrows) of this widget
     *
     * @return The unmodifiable link event list
     *
     * @since 1.1
     */
    public List<ILinkEvent> getArrows() {
        return Collections.unmodifiableList(fItemData.fLinks);
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
        index = Math.max(0, index);
        fTopIndex = index;
        redraw();
    }

    /**
     * Set the top index so that the requested element is at the specified
     * position.
     *
     * @param entry
     *            the time graph entry to be positioned
     * @param y
     *            the requested y-coordinate
     * @since 2.0
     */
    public void setElementPosition(ITimeGraphEntry entry, int y) {
        Item item = fItemData.fItemMap.get(entry);
        if (item == null || item.fExpandedIndex == -1) {
            return;
        }
        int index = item.fExpandedIndex;
        Rectangle itemRect = getItemRect(getClientArea(), index);
        int delta = itemRect.y + itemRect.height - y;
        int topIndex = getItemIndexAtY(delta);
        if (topIndex != -1) {
            setTopIndex(topIndex);
        } else {
            if (delta < 0) {
                setTopIndex(0);
            } else {
                setTopIndex(getExpandedElementCount());
            }
        }
    }

    /**
     * Sets the auto-expand level to be used for new entries discovered when
     * calling {@link #refreshData()} or {@link #refreshData(ITimeGraphEntry[])}
     * . The value 0 means that there is no auto-expand; 1 means that top-level
     * entries are expanded, but not their children; 2 means that top-level
     * entries are expanded, and their children, but not grand-children; and so
     * on.
     * <p>
     * The value {@link #ALL_LEVELS} means that all subtrees should be expanded.
     * </p>
     *
     * @param level
     *            non-negative level, or <code>ALL_LEVELS</code> to expand all
     *            levels of the tree
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
     */
    public int getAutoExpandLevel() {
        return fAutoExpandLevel;
    }

    /**
     * Get the expanded state of a given entry.
     *
     * @param entry
     *            The entry
     * @return true if the entry is expanded, false if collapsed
     * @since 1.1
     */
    public boolean getExpandedState(ITimeGraphEntry entry) {
        Item item = fItemData.fItemMap.get(entry);
        return (item != null ? item.fExpanded : false);
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
     * Set the expanded state of a given list of entries
     *
     * @param entries
     *            The list of entries
     * @param expanded
     *            True if expanded, false if collapsed
     * @since 3.1
     */
    public void setExpandedState(Iterable<ITimeGraphEntry> entries, boolean expanded) {
        for (ITimeGraphEntry entry : entries) {
            Item item = fItemData.findItem(entry);
            if (item != null) {
                item.fExpanded = expanded;
            }
        }
        fItemData.updateExpandedItems();
        redraw();
    }

    /**
     * Set the expanded state of a given entry to certain relative level. It
     * will call fireTreeEvent() for each changed entry. At the end it will call
     * redraw().
     *
     * @param entry
     *            The entry
     * @param level
     *            level to expand to or negative for all levels
     * @param expanded
     *            True if expanded, false if collapsed
     */
    private void setExpandedState(ITimeGraphEntry entry, int level, boolean expanded) {
        setExpandedStateInt(entry, level, expanded);
        redraw();
    }

    /**
     * Set the expanded state of a given entry and its children to the first
     * level that has one collapsed entry.
     *
     * @param entry
     *            The entry
     */
    private void setExpandedStateLevel(ITimeGraphEntry entry) {
        int level = findExpandedLevel(entry);
        if (level >= 0) {
            setExpandedStateInt(entry, level, true);
            redraw();
        }
    }

    /*
     * Inner class for finding relative level with at least one collapsed entry.
     */
    private class SearchNode {
        SearchNode(ITimeGraphEntry e, int l) {
            entry = e;
            level = l;
        }

        ITimeGraphEntry entry;
        int level;
    }

    /**
     * Finds the relative level with at least one collapsed entry.
     *
     * @param entry
     *            the start entry
     * @return the found level or -1 if all levels are already expanded.
     */
    private int findExpandedLevel(ITimeGraphEntry entry) {
        Queue<SearchNode> queue = new LinkedList<>();
        SearchNode root = new SearchNode(entry, 0);
        SearchNode node = root;
        queue.add(root);

        while (!queue.isEmpty()) {
            node = queue.remove();
            if (node.entry.hasChildren() && !getExpandedState(node.entry)) {
                return node.level;
            }
            for (ITimeGraphEntry e : node.entry.getChildren()) {
                if (e.hasChildren()) {
                    SearchNode n = new SearchNode(e, node.level + 1);
                    queue.add(n);
                }
            }
        }
        return -1;
    }

    /**
     * Set the expanded state of a given entry to certain relative level. It
     * will call fireTreeEvent() for each changed entry. No redraw is done.
     *
     * @param entry
     *            The entry
     * @param level
     *            level to expand to or negative for all levels
     * @param expanded
     *            True if expanded, false if collapsed
     */
    private void setExpandedStateInt(ITimeGraphEntry entry, int aLevel, boolean expanded) {
        int level = aLevel;
        if ((level > 0) || (level < 0)) {
            level--;
            if (entry.hasChildren()) {
                for (ITimeGraphEntry e : entry.getChildren()) {
                    setExpandedStateInt(e, level, expanded);
                }
            }
        }
        Item item = fItemData.findItem(entry);
        if (item != null && item.fExpanded != expanded) {
            item.fExpanded = expanded;
            fItemData.updateExpandedItems();
            fireTreeEvent(item.fEntry, item.fExpanded);
        }
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root.
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
     * Add a viewer filter listener
     *
     * @param listener
     *            The listener to add
     * @since 3.2
     */
    public void addViewerFilterListener(ITimeGraphViewerFilterListener listener) {
        if (!fViewerFilterListeners.contains(listener)) {
            fViewerFilterListeners.add(listener);
        }
    }

    /**
     * Remove a viewer filter listener
     *
     * @param listener
     *            The listener to remove
     * @since 3.2
     */
    public void removeViewerFilterListener(ITimeGraphViewerFilterListener listener) {
        if (fViewerFilterListeners.contains(listener)) {
            fViewerFilterListeners.remove(listener);
        }
    }

    /**
     * Viewer filter added callback
     *
     * @param filters
     *            The added filters
     *
     * @since 3.1
     */
    private void fireFiltersAdded(@NonNull Iterable<ViewerFilter> filters) {
        for (ITimeGraphViewerFilterListener listener : fViewerFilterListeners) {
            listener.filtersAdded(filters);
        }
    }

    /**
     * Viewer filter changed callback
     *
     * @param filters
     *            The changed filters
     *
     * @since 3.1
     */
    private void fireFiltersChanged(@NonNull Iterable<ViewerFilter> filters) {
        for (ITimeGraphViewerFilterListener listener : fViewerFilterListeners) {
            listener.filtersChanged(filters);
        }
    }

    /**
     * Viewer filter removed callback
     *
     * @param filters
     *            The removed filters
     *
     * @since 3.1
     */
    private void fireFiltersRemoved(@NonNull Iterable<ViewerFilter> filters) {
        for (ITimeGraphViewerFilterListener listener : fViewerFilterListeners) {
            listener.filtersRemoved(filters);
        }
    }

    /**
     * Add a menu listener on {@link ITimeGraphEntry}s
     *
     * @param listener
     *            The listener to add
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
     *            The MenuDetectEvent, with field {@link TypedEvent#data} set to
     *            the selected {@link ITimeGraphEntry}
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
     *            The MenuDetectEvent, with field {@link TypedEvent#data} set to
     *            the selected {@link ITimeEvent}
     */
    private void fireMenuEventOnTimeEvent(MenuDetectEvent event) {
        for (MenuDetectListener listener : fTimeEventMenuListeners) {
            listener.menuDetected(event);
        }
    }

    @Override
    public boolean setFocus() {
        if ((fTimeProvider != null) && fTimeProvider.getNameSpace() > 0) {
            fHasNamespaceFocus = true;
        }
        return super.setFocus();
    }

    /**
     * Returns the current selection for this time graph. If a time graph entry
     * is selected, it will be the first element in the selection. If a time
     * event is selected, it will be the second element in the selection.
     *
     * @return the current selection
     */
    @Override
    public ISelection getSelection() {
        ITimeGraphEntry entry = getSelectedTrace();
        if (null != entry && null != fTimeProvider) {
            long selectedTime = fTimeProvider.getSelectionBegin();
            ITimeEvent event = Utils.findEvent(entry, selectedTime, 0);
            if (event == null) {
                return new StructuredSelection(entry);
            }
            return new StructuredSelection(new Object[] { entry, event });
        }
        return StructuredSelection.EMPTY;
    }

    /**
     * Get the selection object
     *
     * @return The selection
     */
    public ISelection getSelectionTrace() {
        ITimeGraphEntry entry = getSelectedTrace();
        if (null != entry) {
            return new StructuredSelection(entry);
        }
        return StructuredSelection.EMPTY;
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
     * @param extend
     *            true to extend selection range, false for single selection
     * @since 1.0
     */
    public void selectEvent(int n, boolean extend) {
        if (null == fTimeProvider) {
            return;
        }
        ITimeGraphEntry entry = getSelectedTrace();
        if (entry == null) {
            return;
        }
        long time = fTimeProvider.getSelectionEnd();
        if (n > 0) {
            time = Math.min(Utils.nextChange(entry, time), fTimeProvider.getMaxTime());
        } else if (n < 0) {
            time = Math.max(Utils.prevChange(entry, time), fTimeProvider.getMinTime());
        }
        if (extend) {
            fTimeProvider.setSelectionRangeNotify(fTimeProvider.getSelectionBegin(), time, true);
        } else {
            fTimeProvider.setSelectedTimeNotify(time, true);
        }
        fireSelectionChanged();
        updateStatusLine(STATUS_WITHOUT_CURSOR_TIME);
    }

    /**
     * Select the next event
     *
     * @param extend
     *            true to extend selection range, false for single selection
     * @since 1.0
     */
    public void selectNextEvent(boolean extend) {
        selectEvent(1, extend);
        // Notify if visible time window has been adjusted
        fTimeProvider.setStartFinishTimeNotify(fTimeProvider.getTime0(), fTimeProvider.getTime1());
    }

    /**
     * Select the previous event
     *
     * @param extend
     *            true to extend selection range, false for single selection
     * @since 1.0
     */
    public void selectPrevEvent(boolean extend) {
        selectEvent(-1, extend);
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
     * Scroll left or right by one quarter window size
     *
     * @param left
     *            true to scroll left, false to scroll right
     */
    public void horizontalScroll(boolean left) {
        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        long timeMin = fTimeProvider.getMinTime();
        long timeMax = fTimeProvider.getMaxTime();
        long range = time1 - time0;
        if (range <= 0) {
            return;
        }
        long increment = Math.max(1, range / 4);
        if (left) {
            time0 = Math.max(time0 - increment, timeMin);
            time1 = time0 + range;
        } else {
            time1 = Math.min(time1 + increment, timeMax);
            time0 = time1 - range;
        }
        fTimeProvider.setStartFinishTimeNotify(time0, time1);
    }

    /**
     * Zoom based on mouse cursor location with mouse scrolling
     *
     * @param zoomIn
     *            true to zoom in, false to zoom out
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
        /* snap to bounds if zooming out of range */
        newTime0 = Math.max(fTimeProvider.getMinTime(), Math.min(newTime0, fTimeProvider.getMaxTime() - newInterval));
        long newTime1 = newTime0 + newInterval;
        fTimeProvider.setStartFinishTimeNotify(newTime0, newTime1);
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
        if (selTime < prevTime0 || selTime > prevTime1) {
            selTime = (prevTime0 + prevTime1) / 2;
        }
        long time0 = selTime - (long) ((selTime - prevTime0) / ZOOM_FACTOR);
        long time1 = selTime + (long) ((prevTime1 - selTime) / ZOOM_FACTOR);

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
        if (selTime < prevTime0 || selTime > prevTime1) {
            selTime = (prevTime0 + prevTime1) / 2;
        }
        long newInterval;
        long time0;
        if (prevTime1 - prevTime0 <= 1) {
            newInterval = 2;
            time0 = selTime - 1;
        } else {
            newInterval = (long) Math.ceil((prevTime1 - prevTime0) * ZOOM_FACTOR);
            time0 = selTime - (long) Math.ceil((selTime - prevTime0) * ZOOM_FACTOR);
        }
        /* snap to bounds if zooming out of range */
        time0 = Math.max(fTimeProvider.getMinTime(), Math.min(time0, fTimeProvider.getMaxTime() - newInterval));
        long time1 = time0 + newInterval;

        fTimeProvider.setStartFinishTimeNotify(time0, time1);
    }

    /**
     * Zoom vertically.
     *
     * @param zoomIn
     *            true to zoom in, false to zoom out
     * @since 2.0
     */
    public void verticalZoom(boolean zoomIn) {
        if (zoomIn) {
            fHeightAdjustment++;
        } else {
            fHeightAdjustment--;
            fHeightAdjustment = Math.max(fHeightAdjustment, 1 - fMaxItemHeight);
        }
        fItemData.refreshData();
        redraw();
    }

    /**
     * Reset the vertical zoom to default.
     *
     * @since 2.0
     */
    public void resetVerticalZoom() {
        fHeightAdjustment = 0;
        fItemData.refreshData();
        redraw();
    }

    /**
     * Set the grid lines visibility. The default is true.
     *
     * @param visible
     *            true to show the grid lines, false otherwise
     * @since 2.0
     */
    public void setGridLinesVisible(boolean visible) {
        fGridLinesVisible = visible;
    }

    /**
     * Get the grid lines visibility.
     *
     * @return true if the grid lines are visible, false otherwise
     * @since 2.0
     */
    public boolean getGridLinesVisible() {
        return fGridLinesVisible;
    }

    /**
     * Set the grid line color. The default is SWT.COLOR_GRAY.
     *
     * @param color
     *            the grid line color
     * @since 2.0
     */
    public void setGridLineColor(Color color) {
        fGridLineColor = color;
    }

    /**
     * Get the grid line color.
     *
     * @return the grid line color
     * @since 2.0
     */
    public Color getGridLineColor() {
        return fGridLineColor;
    }

    /**
     * Set the markers list.
     *
     * @param markers
     *            The markers list, or null
     * @since 2.0
     */
    public void setMarkers(List<IMarkerEvent> markers) {
        fMarkers = markers;
    }

    /**
     * Get the markers list.
     *
     * @return The markers list, or null
     * @since 2.0
     */
    public List<IMarkerEvent> getMarkers() {
        return fMarkers;
    }

    /**
     * Set the markers visibility. The default is true.
     *
     * @param visible
     *            true to show the markers, false otherwise
     * @since 2.0
     */
    public void setMarkersVisible(boolean visible) {
        fMarkersVisible = visible;
    }

    /**
     * Get the markers visibility.
     *
     * @return true if the markers are visible, false otherwise
     * @since 2.0
     */
    public boolean getMarkersVisible() {
        return fMarkersVisible;
    }

    /**
     * Hide arrows
     *
     * @param hideArrows
     *            true to hide arrows
     */
    public void hideArrows(boolean hideArrows) {
        fHideArrows = hideArrows;
    }

    /**
     * Follow the arrow forward
     *
     * @param extend
     *            true to extend selection range, false for single selection
     * @since 1.0
     */
    public void followArrowFwd(boolean extend) {
        ITimeGraphEntry trace = getSelectedTrace();
        if (trace == null) {
            return;
        }
        long selectedTime = fTimeProvider.getSelectionEnd();
        for (ILinkEvent link : fItemData.fLinks) {
            if (link.getEntry() == trace && link.getTime() == selectedTime) {
                selectItem(link.getDestinationEntry(), false);
                if (link.getDuration() != 0) {
                    if (extend) {
                        fTimeProvider.setSelectionRangeNotify(fTimeProvider.getSelectionBegin(), link.getTime() + link.getDuration(), true);
                    } else {
                        fTimeProvider.setSelectedTimeNotify(link.getTime() + link.getDuration(), true);
                    }
                    // Notify if visible time window has been adjusted
                    fTimeProvider.setStartFinishTimeNotify(fTimeProvider.getTime0(), fTimeProvider.getTime1());
                }
                fireSelectionChanged();
                updateStatusLine(STATUS_WITHOUT_CURSOR_TIME);
                return;
            }
        }
        selectNextEvent(extend);
    }

    /**
     * Follow the arrow backward
     *
     * @param extend
     *            true to extend selection range, false for single selection
     * @since 1.0
     */
    public void followArrowBwd(boolean extend) {
        ITimeGraphEntry trace = getSelectedTrace();
        if (trace == null) {
            return;
        }
        long selectedTime = fTimeProvider.getSelectionEnd();
        for (ILinkEvent link : fItemData.fLinks) {
            if (link.getDestinationEntry() == trace && link.getTime() + link.getDuration() == selectedTime) {
                selectItem(link.getEntry(), false);
                if (link.getDuration() != 0) {
                    if (extend) {
                        fTimeProvider.setSelectionRangeNotify(fTimeProvider.getSelectionBegin(), link.getTime(), true);
                    } else {
                        fTimeProvider.setSelectedTimeNotify(link.getTime(), true);
                    }
                    // Notify if visible time window has been adjusted
                    fTimeProvider.setStartFinishTimeNotify(fTimeProvider.getTime0(), fTimeProvider.getTime1());
                }
                fireSelectionChanged();
                updateStatusLine(STATUS_WITHOUT_CURSOR_TIME);
                return;
            }
        }
        selectPrevEvent(extend);
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
     */
    protected int getItemIndexAtY(int y) {
        int ySum = 0;
        if (y < 0) {
            for (int idx = fTopIndex - 1; idx >= 0; idx--) {
                ySum -= fItemData.fExpandedItems[idx].fItemHeight;
                if (y >= ySum) {
                    return idx;
                }
            }
        } else {
            for (int idx = fTopIndex; idx < fItemData.fExpandedItems.length; idx++) {
                ySum += fItemData.fExpandedItems[idx].fItemHeight;
                if (y < ySum) {
                    return idx;
                }
            }
        }
        return -1;
    }

    boolean isOverSplitLine(int x) {
        if (x < 0 || null == fTimeProvider) {
            return false;
        }
        int nameWidth = fTimeProvider.getNameSpace();
        return Math.abs(x - nameWidth) <= SNAP_WIDTH;
    }

    boolean isOverTimeSpace(int x, int y) {
        Point size = getSize();
        return x >= fTimeProvider.getNameSpace() && x < size.x && y >= 0 && y < size.y;
    }

    /**
     * Gets the {@link ITimeGraphEntry} at the given location.
     *
     * @param pt
     *            a point in the widget
     * @return the {@link ITimeGraphEntry} at this point, or <code>null</code>
     *         if none.
     * @since 2.0
     */
    public ITimeGraphEntry getEntry(Point pt) {
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

    @Override
    public int getXForTime(long time) {
        if (null == fTimeProvider) {
            return -1;
        }
        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        int width = getSize().x;
        int nameSpace = fTimeProvider.getNameSpace();
        double pixelsPerNanoSec = (width - nameSpace <= RIGHT_MARGIN) ? 0 : (double) (width - nameSpace - RIGHT_MARGIN) / (time1 - time0);
        int x = SaturatedArithmetic.add(getBounds().x + nameSpace, (int) ((time - time0) * pixelsPerNanoSec));
        return x;
    }

    @Override
    public long getTimeAtX(int coord) {
        if (null == fTimeProvider) {
            return -1;
        }
        long hitTime = -1;
        Point size = getSize();
        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        int nameWidth = fTimeProvider.getNameSpace();
        final int x = coord - nameWidth;
        int timeWidth = size.x - nameWidth - RIGHT_MARGIN;
        if (x >= 0 && size.x >= nameWidth) {
            if (time1 - time0 > timeWidth) {
                // nanosecond smaller than one pixel: use the first integer
                // nanosecond of this pixel's time range
                hitTime = time0 + (long) Math.ceil((time1 - time0) * ((double) x / timeWidth));
            } else {
                // nanosecond greater than one pixel: use the nanosecond that
                // covers this pixel start position
                hitTime = time0 + (long) Math.floor((time1 - time0) * ((double) x / timeWidth));
            }
        }
        return hitTime;
    }

    void selectItem(int idx, boolean addSelection) {
        selectItem(idx, addSelection, true);
    }

    void selectItem(int idx, boolean addSelection, boolean reveal) {
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
        if (reveal) {
            changed |= ensureVisibleItem(idx, true);
        }
        if (changed) {
            redraw();
        }
    }

    /**
     * Select an entry and make it visible
     *
     * @param entry
     *            The entry to select
     * @param addSelection
     *            <code>true</code> to add the entry to the current selection,
     *            or <code>false</code> to set a new selection
     */
    public void selectItem(ITimeGraphEntry entry, boolean addSelection) {
        int idx = fItemData.findItemIndex(entry);
        selectItem(idx, addSelection);
    }

    /**
     * Select an entry
     *
     * @param entry
     *            The entry to select
     * @param addSelection
     *            <code>true</code> to add the entry to the current selection,
     *            or <code>false</code> to set a new selection
     * @param reveal
     *            <code>true</code> if the selection is to be made visible, and
     *            <code>false</code> otherwise
     * @since 2.3
     */
    public void selectItem(ITimeGraphEntry entry, boolean addSelection, boolean reveal) {
        int idx = fItemData.findItemIndex(entry);
        selectItem(idx, addSelection, reveal);
    }

    /**
     * Retrieve the number of entries shown per page.
     *
     * @return The count
     */
    public int countPerPage() {
        int height = getSize().y;
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
     * Get the number of expanded (visible) items
     *
     * @return The count of expanded (visible) items
     */
    public int getExpandedElementCount() {
        return fItemData.fExpandedItems.length;
    }

    /**
     * Get an array of all expanded (visible) elements
     *
     * @return The expanded (visible) elements
     */
    public ITimeGraphEntry[] getExpandedElements() {
        ArrayList<ITimeGraphEntry> elements = new ArrayList<>();
        for (Item item : fItemData.fExpandedItems) {
            elements.add(item.fEntry);
        }
        return elements.toArray(new ITimeGraphEntry[0]);
    }

    /**
     * Get the expanded (visible) element at the specified index.
     *
     * @param index
     *            the element index
     * @return The expanded (visible) element or null if out of range
     * @since 2.0
     */
    public ITimeGraphEntry getExpandedElement(int index) {
        if (index < 0 || index >= fItemData.fExpandedItems.length) {
            return null;
        }
        return fItemData.fExpandedItems[index].fEntry;
    }

    /**
     * Get the bounds of the specified entry relative to its parent time graph.
     *
     * @param entry
     *            the time graph entry
     * @return the bounds of the entry, or null if the entry is not visible
     * @since 2.3
     */
    public Rectangle getItemBounds(ITimeGraphEntry entry) {
        int idx = fItemData.findItemIndex(entry);
        if (idx >= 0) {
            return getItemRect(getClientArea(), idx);
        }
        return null;
    }

    Rectangle getNameRect(Rectangle bounds, int idx, int nameWidth) {
        Rectangle rect = getItemRect(bounds, idx);
        rect.width = nameWidth;
        return rect;
    }

    Rectangle getStatesRect(Rectangle bounds, int idx, int nameWidth) {
        Rectangle rect = getItemRect(bounds, idx);
        rect.x += nameWidth;
        rect.width -= nameWidth;
        return rect;
    }

    Rectangle getItemRect(Rectangle bounds, int idx) {
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
        int y = bounds.y + ySum;
        int height = fItemData.fExpandedItems[idx].fItemHeight;
        return new Rectangle(bounds.x, y, bounds.width, height);
    }

    @Override
    void paint(Rectangle bounds, PaintEvent e) {
        GC gc = e.gc;

        if (bounds.width < 2 || bounds.height < 2 || null == fTimeProvider) {
            return;
        }

        fIdealNameSpace = 0;
        int nameSpace = fTimeProvider.getNameSpace();

        // draw the background layer
        drawBackground(bounds, nameSpace, gc);

        // draw the grid lines
        drawGridLines(bounds, gc);

        // draw the background markers
        drawMarkers(bounds, fTimeProvider, fMarkers, false, nameSpace, gc);

        // draw the items
        drawItems(bounds, fTimeProvider, fItemData.fExpandedItems, fTopIndex, nameSpace, gc);

        // draw the foreground markers
        drawMarkers(bounds, fTimeProvider, fMarkers, true, nameSpace, gc);

        // draw the links (arrows)
        drawLinks(bounds, fTimeProvider, fItemData.fLinks, nameSpace, gc);

        fTimeGraphProvider.postDrawControl(bounds, gc);

        int alpha = gc.getAlpha();
        gc.setAlpha(100);

        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        long selectionBegin = fTimeProvider.getSelectionBegin();
        long selectionEnd = fTimeProvider.getSelectionEnd();
        double pixelsPerNanoSec = (bounds.width - nameSpace <= RIGHT_MARGIN) ? 0 : (double) (bounds.width - nameSpace - RIGHT_MARGIN) / (time1 - time0);
        int x0 = SaturatedArithmetic.add(bounds.x + nameSpace, (int) ((selectionBegin - time0) * pixelsPerNanoSec));
        int x1 = SaturatedArithmetic.add(bounds.x + nameSpace, (int) ((selectionEnd - time0) * pixelsPerNanoSec));

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

        // draw split line
        if (DRAG_SPLIT_LINE == fDragState ||
                (DRAG_NONE == fDragState && fMouseOverSplitLine && fTimeProvider.getNameSpace() > 0)) {
            gc.setBackground(getColorScheme().getColor(TimeGraphColorScheme.DARK_GRAY));
        } else {
            gc.setBackground(getColorScheme().getColor(TimeGraphColorScheme.GRAY));
        }
        gc.fillRectangle(bounds.x + nameSpace - SNAP_WIDTH, bounds.y, SNAP_WIDTH, bounds.height);

        if (DRAG_ZOOM == fDragState && Math.max(fDragX, fDragX0) > nameSpace) {
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
        }

        gc.setAlpha(alpha);
    }

    /**
     * Draw the background layer. Fills the background of the control's name
     * space and states space, updates the background of items if necessary, and
     * draws the item's name text and middle line.
     *
     * @param bounds
     *            The bounds of the control
     * @param nameSpace
     *            The name space width
     * @param gc
     *            Graphics context
     * @since 2.0
     */
    protected void drawBackground(Rectangle bounds, int nameSpace, GC gc) {
        // draw empty name space background
        gc.setBackground(getColorScheme().getBkColor(false, false, true));
        drawBackground(gc, bounds.x, bounds.y, nameSpace, bounds.height);

        // draw empty states space background
        gc.setBackground(getColorScheme().getColor(TimeGraphColorScheme.BACKGROUND));
        drawBackground(gc, bounds.x + nameSpace, bounds.y, bounds.width - nameSpace, bounds.height);

        for (int i = fTopIndex; i < fItemData.fExpandedItems.length; i++) {
            Rectangle itemRect = getItemRect(bounds, i);
            if (itemRect.y >= bounds.y + bounds.height) {
                break;
            }
            Item item = fItemData.fExpandedItems[i];
            // draw the background of selected item and items with no time
            // events
            if (!item.fEntry.hasTimeEvents()) {
                gc.setBackground(getColorScheme().getBkColorGroup(item.fSelected, fIsInFocus));
                gc.fillRectangle(itemRect);
            } else if (item.fSelected) {
                gc.setBackground(getColorScheme().getBkColor(true, fIsInFocus, true));
                gc.fillRectangle(itemRect.x, itemRect.y, nameSpace, itemRect.height);
                gc.setBackground(getColorScheme().getBkColor(true, fIsInFocus, false));
                gc.fillRectangle(nameSpace, itemRect.y, itemRect.width - nameSpace, itemRect.height);
            }
            // draw the name space
            Rectangle nameRect = new Rectangle(itemRect.x, itemRect.y, nameSpace, itemRect.height);
            drawName(item, nameRect, gc);
            if (item.fEntry.hasTimeEvents()) {
                Rectangle rect = new Rectangle(nameSpace, itemRect.y, itemRect.width - nameSpace, itemRect.height);
                drawMidLine(rect, gc);
            }
        }
    }

    /**
     * Draw the grid lines
     *
     * @param bounds
     *            The bounds of the control
     * @param gc
     *            Graphics context
     * @since 2.0
     */
    public void drawGridLines(Rectangle bounds, GC gc) {
        if (!fGridLinesVisible) {
            return;
        }
        gc.setForeground(fGridLineColor);
        gc.setAlpha(fGridLineColor.getAlpha());
        for (int x : fTimeGraphScale.getTickList()) {
            gc.drawLine(x, bounds.y, x, bounds.y + bounds.height);
        }
        gc.setAlpha(255);
    }

    /**
     * Draw the markers
     *
     * @param bounds
     *            The rectangle of the area
     * @param timeProvider
     *            The time provider
     * @param markers
     *            The list of markers
     * @param foreground
     *            true to draw the foreground markers, false otherwise
     * @param nameSpace
     *            The width reserved for the names
     * @param gc
     *            Reference to the SWT GC object
     * @since 2.0
     */
    protected void drawMarkers(Rectangle bounds, ITimeDataProvider timeProvider, List<IMarkerEvent> markers, boolean foreground, int nameSpace, GC gc) {
        if (!fMarkersVisible || markers == null || markers.isEmpty()) {
            return;
        }
        gc.setClipping(new Rectangle(nameSpace, 0, bounds.width - nameSpace, bounds.height));
        /* the list can grow concurrently but cannot shrink */
        for (int i = 0; i < markers.size(); i++) {
            IMarkerEvent marker = markers.get(i);
            if (marker.isForeground() == foreground) {
                drawMarker(marker, bounds, timeProvider, nameSpace, gc);
            }
        }
        gc.setClipping((Rectangle) null);
    }

    /**
     * Draw a single marker
     *
     * @param marker
     *            The marker event
     * @param bounds
     *            The bounds of the control
     * @param timeProvider
     *            The time provider
     * @param nameSpace
     *            The width reserved for the name
     * @param gc
     *            Reference to the SWT GC object
     * @since 2.0
     */
    protected void drawMarker(IMarkerEvent marker, Rectangle bounds, ITimeDataProvider timeProvider, int nameSpace, GC gc) {
        Rectangle rect = Utils.clone(bounds);
        if (marker.getEntry() != null) {
            int index = fItemData.findItemIndex(marker.getEntry());
            if (index == -1) {
                return;
            }
            rect = getStatesRect(bounds, index, nameSpace);
            if (rect.y < 0 || rect.y > bounds.height) {
                return;
            }
        }
        int x0 = getXForTime(marker.getTime());
        int x1 = getXForTime(marker.getTime() + marker.getDuration());
        if (x0 > bounds.width || x1 < nameSpace) {
            return;
        }
        rect.x = Math.max(nameSpace, Math.min(bounds.width, x0));
        rect.width = Math.max(1, Math.min(bounds.width, x1) - rect.x);

        Color color = getColorScheme().getColor(marker.getColor());
        gc.setBackground(color);
        gc.setAlpha(color.getAlpha());
        gc.fillRectangle(rect);
        gc.setAlpha(255);
        String label = marker.getLabel();
        if (label != null && marker.getEntry() != null) {
            label = label.substring(0, Math.min(label.indexOf('\n') != -1 ? label.indexOf('\n') : label.length(), MAX_LABEL_LENGTH));
            gc.setForeground(color);
            Utils.drawText(gc, label, rect.x - gc.textExtent(label).x, rect.y, true);
        }
    }

    /**
     * Draw many items at once
     *
     * @param bounds
     *            The bounds of the control
     * @param timeProvider
     *            The time provider
     * @param items
     *            The array items to draw
     * @param topIndex
     *            The index of the first element to draw
     * @param nameSpace
     *            The name space width
     * @param gc
     *            Graphics context
     */
    public void drawItems(Rectangle bounds, ITimeDataProvider timeProvider,
            Item[] items, int topIndex, int nameSpace, GC gc) {
        int bottomIndex = Integer.min(topIndex + countPerPage() + 1, items.length);
        for (int i = topIndex; i < bottomIndex; i++) {
            Item item = items[i];
            drawItem(item, bounds, timeProvider, i, nameSpace, gc);
        }
    }

    /**
     * Draws the item
     *
     * @param item
     *            The item to draw
     * @param bounds
     *            The bounds of the control
     * @param timeProvider
     *            The time provider
     * @param i
     *            The expanded item index
     * @param nameSpace
     *            The name space width
     * @param gc
     *            Graphics context
     */
    protected void drawItem(Item item, Rectangle bounds, ITimeDataProvider timeProvider, int i, int nameSpace, GC gc) {
        Rectangle itemRect = getItemRect(bounds, i);
        if (itemRect.y >= bounds.y + bounds.height) {
            return;
        }

        ITimeGraphEntry entry = item.fEntry;
        long time0 = timeProvider.getTime0();
        long time1 = timeProvider.getTime1();
        long selectedTime = fTimeProvider.getSelectionEnd();

        Rectangle rect = new Rectangle(nameSpace, itemRect.y, itemRect.width - nameSpace, itemRect.height);
        if (rect.isEmpty() || (time1 <= time0)) {
            fTimeGraphProvider.postDrawEntry(entry, rect, gc);
            return;
        }

        boolean selected = item.fSelected;
        // K pixels per second
        double pixelsPerNanoSec = (rect.width <= RIGHT_MARGIN) ? 0 : (double) (rect.width - RIGHT_MARGIN) / (time1 - time0);

        if (item.fEntry.hasTimeEvents()) {
            gc.setClipping(new Rectangle(nameSpace, 0, bounds.width - nameSpace, bounds.height));
            fillSpace(rect, gc, selected);

            int margins = getMarginForHeight(rect.height);
            int height = rect.height - margins;
            int topMargin = (margins + 1) / 2;
            Rectangle stateRect = new Rectangle(rect.x, rect.y + topMargin, rect.width, height);

            /* Set the font for this item */
            setFontForHeight(height, gc);

            long maxDuration = (timeProvider.getTimeSpace() == 0) ? Long.MAX_VALUE : 1 * (time1 - time0) / timeProvider.getTimeSpace();
            Iterator<ITimeEvent> iterator = entry.getTimeEventsIterator(time0, time1, maxDuration);

            int lastX = -1;
            while (iterator.hasNext()) {
                ITimeEvent event = iterator.next();
                int x = SaturatedArithmetic.add(rect.x, (int) ((event.getTime() - time0) * pixelsPerNanoSec));
                int xEnd = SaturatedArithmetic.add(rect.x, (int) ((event.getTime() + event.getDuration() - time0) * pixelsPerNanoSec));
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
                    lastX = stateRect.x;
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
     *            The bounds of the control
     * @param timeProvider
     *            The time provider
     * @param links
     *            The list of link events
     * @param nameSpace
     *            The name space width
     * @param gc
     *            Graphics context
     */
    public void drawLinks(Rectangle bounds, ITimeDataProvider timeProvider,
            List<ILinkEvent> links, int nameSpace, GC gc) {
        if (fHideArrows) {
            return;
        }
        gc.setClipping(new Rectangle(nameSpace, 0, bounds.width - nameSpace, bounds.height));
        /* the list can grow concurrently but cannot shrink */
        for (int i = 0; i < links.size(); i++) {
            drawLink(links.get(i), bounds, timeProvider, nameSpace, gc);
        }
        gc.setClipping((Rectangle) null);
    }

    /**
     * Draws a link type event
     *
     * @param event
     *            The link event to draw
     * @param bounds
     *            The bounds of the control
     * @param timeProvider
     *            The time provider
     * @param nameSpace
     *            The name space width
     * @param gc
     *            Graphics context
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
     * Draw an arrow
     *
     * @param colors
     *            Color scheme
     * @param event
     *            Time event for which we're drawing the arrow
     * @param rect
     *            The arrow rectangle
     * @param gc
     *            Graphics context
     * @return true if the arrow was drawn
     */
    protected boolean drawArrow(TimeGraphColorScheme colors, ITimeEvent event,
            Rectangle rect, GC gc) {

        if (rect == null) {
            return false;
        }
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
    private static void drawArrowHead(int x0, int y0, int x1, int y1, GC gc) {
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
     * Draw the name space of an item.
     *
     * @param item
     *            Item object
     * @param bounds
     *            The bounds of the item's name space
     * @param gc
     *            Graphics context
     */
    protected void drawName(Item item, Rectangle bounds, GC gc) {
        // No name space to be drawn
        if (fTimeProvider.getNameSpace() == 0) {
            return;
        }

        boolean hasTimeEvents = item.fEntry.hasTimeEvents();
        if (hasTimeEvents) {
            gc.setClipping(bounds);
        }

        int height = bounds.height - getMarginForHeight(bounds.height);
        setFontForHeight(height, gc);

        String name = fLabelProvider == null ? item.fName : fLabelProvider.getColumnText(item.fEntry, 0);
        Rectangle rect = Utils.clone(bounds);
        rect.y += (bounds.height - gc.stringExtent(name).y) / 2;
        TreeColumn[] columns = fTree.getColumns();
        int idealNameSpace = 0;
        for (int i = 0; i < columns.length; i++) {
            int columnIndex = fTree.getColumnOrder()[i];
            TreeColumn column = columns[columnIndex];
            rect.width = column.getWidth();
            gc.setClipping(rect.x, bounds.y, Math.min(rect.width, bounds.x + bounds.width - rect.x - SNAP_WIDTH), bounds.height);
            int width = MARGIN;
            if (i == 0) {
                // first visible column
                width += item.fLevel * EXPAND_SIZE;
                if (item.fHasChildren) {
                    // draw expand/collapse arrow
                    gc.setBackground(getColorScheme().getColor(TimeGraphColorScheme.DARK_GRAY));
                    int arrowHeightHint = (height < 4) ? height : (height < 6) ? height - 1 : height - 2;
                    int arrowHalfHeight = Math.max(1, Math.min(arrowHeightHint, (int) Math.round((EXPAND_SIZE - 2) / ARROW_RATIO))) / 2;
                    int arrowHalfWidth = (Math.max(1, Math.min(EXPAND_SIZE - 2, (int) Math.round(arrowHeightHint * ARROW_RATIO))) + 1) / 2;
                    int x1 = bounds.x + width + 1;
                    int x2 = x1 + 2 * arrowHalfWidth;
                    int midy = bounds.y + bounds.height / 2;
                    int y1 = midy - arrowHalfHeight;
                    int y2 = midy + arrowHalfHeight;
                    if (!item.fExpanded) { // >
                        gc.fillPolygon(new int[] { x1, y1, x2, midy, x1, y2 });
                    } else { // v
                        int midx = x1 + arrowHalfWidth;
                        gc.fillPolygon(new int[] { x1, y1, x2, y1, midx, y2 });
                    }
                }
                width += EXPAND_SIZE + MARGIN;

                Image img = fLabelProvider != null ? fLabelProvider.getColumnImage(item.fEntry, columnIndex)
                        : columnIndex == 0 ? fTimeGraphProvider.getItemImage(item.fEntry) : null;
                if (img != null) {
                    // draw icon
                    int imgHeight = img.getImageData().height;
                    int imgWidth = img.getImageData().width;
                    int dstHeight = Math.min(bounds.height, imgHeight);
                    int dstWidth = dstHeight * imgWidth / imgHeight;
                    int x = width;
                    int y = bounds.y + (bounds.height - dstHeight) / 2;
                    gc.drawImage(img, 0, 0, imgWidth, imgHeight, x, y, dstWidth, dstHeight);
                    width += imgWidth + MARGIN;
                }
            } else {
                if (fLabelProvider == null) {
                    break;
                }
            }
            String label = fLabelProvider != null ? fLabelProvider.getColumnText(item.fEntry, columnIndex)
                    : columnIndex == 0 ? item.fName : ""; //$NON-NLS-1$
            gc.setForeground(getColorScheme().getFgColor(item.fSelected, fIsInFocus));
            Rectangle textRect = new Rectangle(rect.x + width, rect.y, rect.width - width, rect.height);
            int textWidth = Utils.drawText(gc, label, textRect, true);
            width += textWidth + MARGIN;
            if (textWidth > 0) {
                idealNameSpace = rect.x + width;
            }
            if (columns.length == 1) {
                drawMidLine(new Rectangle(bounds.x + width, bounds.y, bounds.x + bounds.width, bounds.height), gc);
            }
            if (fAutoResizeColumns && width > column.getWidth()) {
                column.setData(PREFERRED_WIDTH, width);
                column.setWidth(width);
            }
            gc.setForeground(getColorScheme().getColor(TimeGraphColorScheme.MID_LINE));
            if (i < columns.length - 1) {
                // not the last visible column: draw the vertical cell border
                int x = rect.x + rect.width - 1;
                gc.drawLine(x, bounds.y, x, bounds.y + bounds.height);
            }
            rect.x += rect.width;
        }
        fIdealNameSpace = Math.max(fIdealNameSpace, idealNameSpace);

        gc.setClipping((Rectangle) null);
    }

    /**
     * Draw the state (color fill)
     *
     * @param colors
     *            Color scheme
     * @param event
     *            Time event for which we're drawing the state
     * @param rect
     *            The state rectangle
     * @param gc
     *            Graphics context
     * @param selected
     *            Is this time event currently selected (so it appears
     *            highlighted)
     * @param timeSelected
     *            Is the timestamp currently selected
     * @return true if the state was drawn
     */
    protected boolean drawState(TimeGraphColorScheme colors, ITimeEvent event,
            Rectangle rect, GC gc, boolean selected, boolean timeSelected) {

        int colorIdx = fTimeGraphProvider.getStateTableIndex(event);
        if (colorIdx < 0 && colorIdx != ITimeGraphPresentationProvider.TRANSPARENT) {
            return false;
        }
        boolean visible = rect.width == 0 ? false : true;
        rect.width = Math.max(1, rect.width);
        Color black = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
        gc.setForeground(black);
        Map<String, Object> styleMap = fTimeGraphProvider.getEventStyle(event);
        float heightFactor = (float) styleMap.getOrDefault(ITimeEventStyleStrings.heightFactor(), 1.0f);
        if (heightFactor > 1.0 || heightFactor < 0) {
            if (fFirstHeightWarning) {
                TraceCompassLog.getLogger(this.getClass()).warning("Heightfactor out of range : " + heightFactor + " for event " + event.toString() + " - clamping results"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                fFirstHeightWarning = false;
            }
            heightFactor = Math.max(0.0f, Math.min(1.0f, heightFactor));
        }

        int height = (int) (rect.height * heightFactor);
        Rectangle drawRect = new Rectangle(rect.x, rect.y + ((rect.height - height) / 2), rect.width, height);

        if (colorIdx == ITimeGraphPresentationProvider.TRANSPARENT) {
            if (visible) {
                // Only draw the top and bottom borders
                gc.drawLine(drawRect.x, drawRect.y, drawRect.x + drawRect.width - 1, drawRect.y);
                gc.drawLine(drawRect.x, drawRect.y + drawRect.height - 1, drawRect.x + drawRect.width - 1, drawRect.y + drawRect.height - 1);
                if (drawRect.width == 1) {
                    gc.drawPoint(drawRect.x, drawRect.y - 2);
                }
            }
            fTimeGraphProvider.postDrawEvent(event, drawRect, gc);
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
        if (visible) {
            int prevAlpha = gc.getAlpha();
            int alpha = ((int) styleMap.getOrDefault(ITimeEventStyleStrings.fillColor(), 0xff)) & 0xff;
            gc.setAlpha(alpha);
            gc.fillRectangle(drawRect);
            gc.setAlpha(prevAlpha);
        } else if (fBlendSubPixelEvents) {
            gc.setAlpha(128);
            gc.fillRectangle(drawRect);
            gc.setAlpha(255);
        }

        if (reallySelected) {
            gc.drawLine(drawRect.x, drawRect.y - 1, drawRect.x + drawRect.width - 1, drawRect.y - 1);
            gc.drawLine(drawRect.x, drawRect.y + drawRect.height, drawRect.x + drawRect.width - 1, drawRect.y + drawRect.height);
        }
        if (!visible) {
            gc.drawPoint(drawRect.x, drawRect.y - 2);
        }
        fTimeGraphProvider.postDrawEvent(event, drawRect, gc);
        return visible;
    }

    /**
     * Fill an item's states rectangle
     *
     * @param rect
     *            The states rectangle
     * @param gc
     *            Graphics context
     * @param selected
     *            true if the item is selected
     */
    protected void fillSpace(Rectangle rect, GC gc, boolean selected) {
        /* Nothing to draw */
    }

    /**
     * Draw a line at the middle height of a rectangle
     *
     * @param rect
     *            The rectangle
     * @param gc
     *            Graphics context
     */
    private void drawMidLine(Rectangle rect, GC gc) {
        gc.setForeground(getColorScheme().getColor(TimeGraphColorScheme.MID_LINE));
        int midy = rect.y + rect.height / 2;
        gc.drawLine(rect.x, midy, rect.x + rect.width, midy);
    }

    private static int getMarginForHeight(int height) {
        /*
         * State rectangle is smaller than the item bounds when height is > 4.
         * Don't use any margin if the height is below or equal that threshold.
         * Use a maximum of 6 pixels for both margins, otherwise try to use 13
         * pixels for the state height, but with a minimum margin of 1.
         */
        final int MARGIN_THRESHOLD = 4;
        final int PREFERRED_HEIGHT = 13;
        final int MIN_MARGIN = 1;
        final int MAX_MARGIN = 6;
        return height <= MARGIN_THRESHOLD ? 0 : Math.max(Math.min(height - PREFERRED_HEIGHT, MAX_MARGIN), MIN_MARGIN);
    }

    private void setFontForHeight(int pixels, GC gc) {
        /* convert font height from pixels to points */
        int height = Math.max(pixels * PPI / DPI, 1);
        Font font = fFonts.get(height);
        if (font == null) {
            FontData fontData = gc.getFont().getFontData()[0];
            fontData.setHeight(height);
            font = new Font(gc.getDevice(), fontData);
            fFonts.put(height, font);
        }
        gc.setFont(font);
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
        } else if (SWT.ARROW_LEFT == e.keyCode && fDragState == DRAG_NONE) {
            boolean extend = (e.stateMask & SWT.SHIFT) != 0;
            selectPrevEvent(extend);
        } else if (SWT.ARROW_RIGHT == e.keyCode && fDragState == DRAG_NONE) {
            boolean extend = (e.stateMask & SWT.SHIFT) != 0;
            selectNextEvent(extend);
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
        } else if ((e.character == '+' || e.character == '=') && ((e.stateMask & SWT.CTRL) != 0)) {
            fVerticalZoomAlignEntry = getVerticalZoomAlignSelection();
            verticalZoom(true);
            if (fVerticalZoomAlignEntry != null) {
                setElementPosition(fVerticalZoomAlignEntry.getKey(), fVerticalZoomAlignEntry.getValue());
            }
        } else if (e.character == '-' && ((e.stateMask & SWT.CTRL) != 0)) {
            fVerticalZoomAlignEntry = getVerticalZoomAlignSelection();
            verticalZoom(false);
            if (fVerticalZoomAlignEntry != null) {
                setElementPosition(fVerticalZoomAlignEntry.getKey(), fVerticalZoomAlignEntry.getValue());
            }
        } else if (e.character == '0' && ((e.stateMask & SWT.CTRL) != 0)) {
            fVerticalZoomAlignEntry = getVerticalZoomAlignSelection();
            resetVerticalZoom();
            if (fVerticalZoomAlignEntry != null) {
                setElementPosition(fVerticalZoomAlignEntry.getKey(), fVerticalZoomAlignEntry.getValue());
            }
        } else if ((e.character == '+' || e.character == '=') && ((e.stateMask & SWT.CTRL) == 0)) {
            if (fHasNamespaceFocus) {
                ITimeGraphEntry entry = getSelectedTrace();
                setExpandedState(entry, 0, true);
            } else {
                zoomIn();
            }
        } else if (e.character == '-' && ((e.stateMask & SWT.CTRL) == 0)) {
            if (fHasNamespaceFocus) {
                ITimeGraphEntry entry = getSelectedTrace();
                if ((entry != null) && entry.hasChildren()) {
                    setExpandedState(entry, -1, false);
                }
            } else {
                zoomOut();
            }
        } else if ((e.character == '*') && ((e.stateMask & SWT.CTRL) == 0)) {
            if (fHasNamespaceFocus) {
                ITimeGraphEntry entry = getSelectedTrace();
                if ((entry != null) && entry.hasChildren()) {
                    setExpandedStateLevel(entry);
                }
            }
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
        redraw();
        updateStatusLine(STATUS_WITHOUT_CURSOR_TIME);
    }

    @Override
    public void focusLost(FocusEvent e) {
        fIsInFocus = false;
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
     * @param waitInd
     *            Should we wait indefinitely?
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

    /**
     * Update the status line following a change of selection.
     *
     * @since 2.0
     */
    public void updateStatusLine() {
        updateStatusLine(STATUS_WITHOUT_CURSOR_TIME);
    }

    private void updateStatusLine(int x) {
        // use the time provider of the time graph scale for the status line
        ITimeDataProvider tdp = fTimeGraphScale.getTimeProvider();
        if (fStatusLineManager == null || null == tdp ||
                tdp.getTime0() == tdp.getTime1()) {
            return;
        }

        long cursorTime = -1;
        long selectionBeginTime = 0;
        long selectionEndTime = 0;
        if ((x >= 0 || x == STATUS_WITHOUT_CURSOR_TIME) && fDragState == DRAG_NONE) {
            if (x != STATUS_WITHOUT_CURSOR_TIME) {
                long time = getTimeAtX(x);
                if (time >= 0) {
                    if (tdp instanceof ITimeDataProviderConverter) {
                        time = ((ITimeDataProviderConverter) tdp).convertTime(time);
                    }
                    cursorTime = time;
                }
            }
            selectionBeginTime = tdp.getSelectionBegin();
            selectionEndTime = tdp.getSelectionEnd();
        } else if (fDragState == DRAG_SELECTION || fDragState == DRAG_ZOOM) {
            long time0 = fDragBeginMarker ? getTimeAtX(fDragX0) : fDragTime0;
            long time = fDragBeginMarker ? fDragTime0 : getTimeAtX(fDragX);
            if (tdp instanceof ITimeDataProviderConverter) {
                time0 = ((ITimeDataProviderConverter) tdp).convertTime(time0);
                time = ((ITimeDataProviderConverter) tdp).convertTime(time);
            }
            // Use the time of T2 to update the cursor time
            cursorTime = time;
            selectionBeginTime = time0;
            selectionEndTime = time;
        }
        String message = buildStatusMessage(cursorTime, selectionBeginTime, selectionEndTime, tdp.getTimeFormat().convert(), Resolution.NANOSEC);
        fStatusLineManager.setMessage(message);
    }

    private static String buildStatusMessage(long cursorTime, long selectionBeginTime, long selectionEndTime, TimeFormat tf, Resolution res) {
        StringBuilder message = new StringBuilder();

        if (cursorTime >= 0) {
            message.append(NLS.bind("T: {0}{1}     ", //$NON-NLS-1$
                    new Object[] {
                            tf == TimeFormat.CALENDAR ? FormatTimeUtils.formatDate(cursorTime) + ' ' : "", //$NON-NLS-1$
                            FormatTimeUtils.formatTime(cursorTime, tf, res)
                    }));
        }

        message.append(NLS.bind("T1: {0}{1}", //$NON-NLS-1$
                new Object[] {
                        tf == TimeFormat.CALENDAR ? FormatTimeUtils.formatDate(selectionBeginTime) + ' ' : "", //$NON-NLS-1$
                        FormatTimeUtils.formatTime(selectionBeginTime, tf, res)
                }));

        if (selectionBeginTime != selectionEndTime) {
            message.append(NLS.bind("     T2: {0}{1}     \u0394: {2}", //$NON-NLS-1$
                    new Object[] {
                            tf == TimeFormat.CALENDAR ? FormatTimeUtils.formatDate(selectionEndTime) + ' ' : "", //$NON-NLS-1$
                            FormatTimeUtils.formatTime(selectionEndTime, tf, res),
                            FormatTimeUtils.formatDelta(selectionEndTime - selectionBeginTime, tf, res)
                    }));
        }
        return message.toString();
    }

    @Override
    public void mouseMove(MouseEvent e) {
        if (null == fTimeProvider) {
            return;
        }
        Point size = getSize();
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
                fTimeProvider.setStartFinishTimeNotify(time0, time1);
            }
        } else if (DRAG_SPLIT_LINE == fDragState) {
            fDragX = e.x;
            fTimeProvider.setNameSpace(e.x);
            TmfSignalManager.dispatchSignal(new TmfTimeViewAlignmentSignal(this, getTimeViewAlignmentInfo()));
        } else if (DRAG_SELECTION == fDragState) {
            if (fDragBeginMarker) {
                fDragX0 = Math.min(Math.max(e.x, fTimeProvider.getNameSpace()), size.x - RIGHT_MARGIN);
            } else {
                fDragX = Math.min(Math.max(e.x, fTimeProvider.getNameSpace()), size.x - RIGHT_MARGIN);
            }
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

        if (e.x >= fTimeProvider.getNameSpace()) {
            fHasNamespaceFocus = false;
        } else {
            fHasNamespaceFocus = true;
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
                TmfSignalManager.dispatchSignal(new TmfTimeViewAlignmentSignal(this, getTimeViewAlignmentInfo()));
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
        if (fDragState != DRAG_NONE) {
            return;
        }
        if (1 == e.button && (e.stateMask & SWT.MODIFIER_MASK) == 0) {
            int nameSpace = fTimeProvider.getNameSpace();
            if (nameSpace != 0 && isOverSplitLine(e.x)) {
                fDragState = DRAG_SPLIT_LINE;
                fDragButton = e.button;
                fDragX = e.x;
                fDragX0 = fDragX;
                redraw();
                updateCursor(e.x, e.stateMask);
                return;
            }
        }
        if (fTimeProvider == null ||
                fTimeProvider.getTime0() == fTimeProvider.getTime1() ||
                getSize().x - fTimeProvider.getNameSpace() <= 0) {
            return;
        }
        int idx;
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
                fDragBeginMarker = false;
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
                        fDragBeginMarker = true;
                        fDragX = xEnd;
                        fDragX0 = e.x;
                        fDragTime0 = selectionEnd;
                    } else {
                        fDragX0 = xBegin;
                        fDragTime0 = selectionBegin;
                    }
                } else {
                    long time = getTimeAtX(e.x);
                    if (Math.abs(e.x - xBegin) < SNAP_WIDTH && Math.abs(time - selectionBegin) <= Math.abs(time - selectionEnd)) {
                        fDragBeginMarker = true;
                        fDragX = xEnd;
                        fDragX0 = e.x;
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
            if (e.x >= fTimeProvider.getNameSpace()) {
                setCapture(true);
                fDragX = Math.min(Math.max(e.x, fTimeProvider.getNameSpace()), getSize().x - RIGHT_MARGIN);
                fDragX0 = fDragX;
                fDragTime0 = getTimeAtX(fDragX0);
                fDragState = DRAG_ZOOM;
                fDragButton = e.button;
                redraw();
                updateCursor(e.x, e.stateMask);
                fTimeGraphScale.setDragRange(fDragX0, fDragX);
            } else {
                idx = getItemIndexAtY(e.y);
                selectItem(idx, false);
                fireSelectionChanged();
            }
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if (fPendingMenuDetectEvent != null && e.button == 3) {
            if ((fDragState == DRAG_ZOOM) && isInDragZoomMargin()) {
                // Select entry and time event for single click
                long time = getTimeAtX(fDragX0);
                fTimeProvider.setSelectionRangeNotify(time, time, false);
                int idx = getItemIndexAtY(e.y);
                selectItem(idx, false);
                fireSelectionChanged();
            }
            menuDetected(fPendingMenuDetectEvent);
        }
        if (DRAG_NONE != fDragState) {
            setCapture(false);
            if (e.button == fDragButton && DRAG_TRACE_ITEM == fDragState) {
                fDragState = DRAG_NONE;
                if (fDragX != fDragX0) {
                    fTimeProvider.notifyStartFinishTime();
                }
            } else if (e.button == fDragButton && DRAG_SPLIT_LINE == fDragState) {
                fDragState = DRAG_NONE;
                redraw();
            } else if (e.button == fDragButton && DRAG_SELECTION == fDragState) {
                fDragState = DRAG_NONE;
                if (fDragX == fDragX0) { // click without selecting anything
                    long time = getTimeAtX(e.x);
                    fTimeProvider.setSelectedTimeNotify(time, false);
                } else {
                    long time0 = fDragBeginMarker ? getTimeAtX(fDragX0) : fDragTime0;
                    long time1 = fDragBeginMarker ? fDragTime0 : getTimeAtX(fDragX);
                    fTimeProvider.setSelectionRangeNotify(time0, time1, false);
                }
                redraw();
                fTimeGraphScale.setDragRange(-1, -1);
            } else if (e.button == fDragButton && DRAG_ZOOM == fDragState) {
                fDragState = DRAG_NONE;
                int nameWidth = fTimeProvider.getNameSpace();
                if ((Math.max(fDragX, fDragX0) > nameWidth) && !isInDragZoomMargin()) {
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
        updateStatusLine(STATUS_WITHOUT_CURSOR_TIME);
    }

    @Override
    public void mouseHover(MouseEvent e) {
    }

    @Override
    public void mouseScrolled(MouseEvent e) {
        if (fDragState != DRAG_NONE || e.count == 0) {
            return;
        }

        /*
         * On some platforms the mouse scroll event is sent to the control that
         * has focus even if it is not under the cursor. Handle the event only
         * if over the time graph control.
         */
        Point size = getSize();
        Rectangle bounds = new Rectangle(0, 0, size.x, size.y);
        if (!bounds.contains(e.x, e.y)) {
            return;
        }

        boolean horizontalZoom = false;
        boolean horizontalScroll = false;
        boolean verticalZoom = false;
        boolean verticalScroll = false;

        // over the time graph control
        if ((e.stateMask & SWT.MODIFIER_MASK) == (SWT.SHIFT | SWT.CTRL)) {
            verticalZoom = true;
        } else if (e.x < fTimeProvider.getNameSpace()) {
            // over the name space
            verticalScroll = true;
        } else {
            // over the state area
            if ((e.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
                // over the state area, CTRL pressed
                horizontalZoom = true;
            } else if ((e.stateMask & SWT.MODIFIER_MASK) == SWT.SHIFT) {
                // over the state area, SHIFT pressed
                horizontalScroll = true;
            } else {
                // over the state area, no modifier pressed
                verticalScroll = true;
            }
        }
        if (verticalZoom) {
            fVerticalZoomAlignEntry = getVerticalZoomAlignCursor(e.y);
            verticalZoom(e.count > 0);
            if (fVerticalZoomAlignEntry != null) {
                setElementPosition(fVerticalZoomAlignEntry.getKey(), fVerticalZoomAlignEntry.getValue());
            }
        } else if (horizontalZoom && fTimeProvider.getTime0() != fTimeProvider.getTime1()) {
            zoom(e.count > 0);
        } else if (horizontalScroll) {
            horizontalScroll(e.count > 0);
        } else if (verticalScroll) {
            setTopIndex(getTopIndex() - e.count);
        }
    }

    /**
     * Get the vertical zoom alignment entry and position based on the current
     * selection. If there is no selection or if the selection is not visible,
     * return an alignment entry with a null time graph entry.
     *
     * @return a map entry where the key is the selection's time graph entry and
     *         the value is the center y-coordinate of that entry, or null
     */
    private Entry<ITimeGraphEntry, Integer> getVerticalZoomAlignSelection() {
        Entry<ITimeGraphEntry, Integer> alignEntry = getVerticalZoomAlignOngoing();
        if (alignEntry != null) {
            return alignEntry;
        }
        int index = getSelectedIndex();
        if (index == -1 || index >= getExpandedElementCount()) {
            return new SimpleEntry<>(null, 0);
        }
        Rectangle bounds = getClientArea();
        Rectangle itemRect = getItemRect(bounds, index);
        if (itemRect.y < bounds.y || itemRect.y > bounds.y + bounds.height) {
            /* selection is not visible */
            return new SimpleEntry<>(null, 0);
        }
        ITimeGraphEntry entry = getExpandedElement(index);
        int y = itemRect.y + itemRect.height / 2;
        return new SimpleEntry<>(entry, y);
    }

    /**
     * Get the vertical zoom alignment entry and position at the specified
     * cursor position.
     *
     * @param y
     *            the cursor y-coordinate
     * @return a map entry where the key is the time graph entry under the
     *         cursor and the value is the cursor y-coordinate
     */
    private Entry<ITimeGraphEntry, Integer> getVerticalZoomAlignCursor(int y) {
        Entry<ITimeGraphEntry, Integer> alignEntry = getVerticalZoomAlignOngoing();
        if (alignEntry != null) {
            return alignEntry;
        }
        int index = getItemIndexAtY(y);
        if (index == -1) {
            index = getExpandedElementCount() - 1;
        }
        ITimeGraphEntry entry = getExpandedElement(index);
        return new SimpleEntry<>(entry, y);
    }

    /**
     * Get the vertical zoom alignment entry and position if there is an ongoing
     * one and we are within the vertical zoom delay, or otherwise return null.
     *
     * @return a map entry where the key is a time graph entry and the value is
     *         a y-coordinate, or null
     */
    private Entry<ITimeGraphEntry, Integer> getVerticalZoomAlignOngoing() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis < fVerticalZoomAlignTime + VERTICAL_ZOOM_DELAY) {
            /*
             * If the vertical zoom is triggered repeatedly in a short amount of
             * time, use the initial event's entry and position.
             */
            fVerticalZoomAlignTime = currentTimeMillis;
            return fVerticalZoomAlignEntry;
        }
        fVerticalZoomAlignTime = currentTimeMillis;
        return null;
    }

    @Override
    public void handleEvent(Event event) {
        if (event.type == SWT.MouseWheel) {
            // prevent horizontal scrolling when the mouse wheel is used to
            // scroll vertically or zoom
            event.doit = false;
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
        for (Item item : fItemData.fItems) {
            item.fItemHeight = rowHeight;
        }
    }

    /**
     * Set the height of a specific item. Overrides the default item height.
     *
     * @param entry
     *            A time graph entry
     * @param rowHeight
     *            The height
     * @return true if the height is successfully stored, false otherwise
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
     * @param width
     *            The minimum width
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
     * Set whether all time events with a duration shorter than one pixel should
     * be blended in. If false, only the first such time event will be drawn and
     * the subsequent time events in the same pixel will be discarded. The
     * default value is false.
     *
     * @param blend
     *            true if sub-pixel events should be blended, false otherwise.
     * @since 1.1
     */
    public void setBlendSubPixelEvents(boolean blend) {
        fBlendSubPixelEvents = blend;
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
        if (selection instanceof IStructuredSelection) {
            Object ob = ((IStructuredSelection) selection).getFirstElement();
            if (ob instanceof ITimeGraphEntry) {
                selectItem((ITimeGraphEntry) ob, false);
            }
        }

    }

    /**
     * Add a new viewer filter object
     *
     * @param filter
     *            The filter object to be attached to the view
     * @since 3.1
     */
    public void addFilter(@NonNull ViewerFilter filter) {
        fFilters.add(filter);
        fireFiltersAdded(Collections.singleton(filter));
    }

    /**
     * Change the viewer filter object
     *
     * The filter elements are already updated, we only let the listener know that a
     * change happened at this point
     *
     * @param filter
     *            The filter object to be attached to the view
     * @since 3.2
     */
    public void changeFilter(@NonNull ViewerFilter filter) {
        fireFiltersChanged(Collections.singleton(filter));
    }

    /**
     * Remove a viewer filter object
     *
     * @param filter
     *            The filter object to be attached to the view
     */
    public void removeFilter(@NonNull ViewerFilter filter) {
        fFilters.remove(filter);
        fireFiltersRemoved(Collections.singleton(filter));
    }

    /**
     * Returns this control's filters.
     *
     * @return an array of viewer filters
     * @since 1.2
     */
    public @NonNull ViewerFilter[] getFilters() {
        return Iterables.toArray(fFilters, ViewerFilter.class);
    }

    /**
     * Sets the filters, replacing any previous filters.
     *
     * @param filters
     *            an array of viewer filters, or null
     * @since 1.2
     */
    public void setFilters(@NonNull ViewerFilter[] filters) {
        fFilters.clear();
        if (filters != null) {
            List<@NonNull ViewerFilter> filtersList = Arrays.asList(filters);
            fFilters.addAll(filtersList);
            fireFiltersChanged(filtersList);
        }
    }

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
            fEventColorMap = new Color[] {};
        }
        redraw();
    }

    private class ItemData {
        private Map<ITimeGraphEntry, Item> fItemMap = new LinkedHashMap<>();
        private Item[] fExpandedItems = new Item[0];
        private Item[] fItems = new Item[0];
        private ITimeGraphEntry fRootEntries[] = new ITimeGraphEntry[0];
        private List<ILinkEvent> fLinks = new ArrayList<>();

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
            ITimeGraphEntry selection = getSelectedTrace();
            Map<ITimeGraphEntry, Item> itemMap = new LinkedHashMap<>();
            fMaxItemHeight = 0;
            for (int i = 0; i < fRootEntries.length; i++) {
                ITimeGraphEntry entry = fRootEntries[i];
                refreshData(itemMap, null, 0, entry);
            }
            fItemMap = itemMap;
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
            fMaxItemHeight = Math.max(fMaxItemHeight, item.fItemHeight);
            item.fItemHeight = Math.max(1, item.fItemHeight + fHeightAdjustment);
            itemMap.put(entry, item);
            if (entry.hasChildren()) {
                Item oldItem = fItemMap.get(entry);
                if (oldItem != null && oldItem.fHasChildren && level == oldItem.fLevel && entry.getParent() == oldItem.fEntry.getParent()) {
                    /* existing items keep their old expanded state */
                    item.fExpanded = oldItem.fExpanded;
                } else {
                    /*
                     * new items set the expanded state according to auto-expand
                     * level
                     */
                    item.fExpanded = fAutoExpandLevel == ALL_LEVELS || level < fAutoExpandLevel;
                }
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
                fRootEntries = null;
            } else {
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

    @Override
    public void menuDetected(MenuDetectEvent e) {
        if (null == fTimeProvider) {
            return;
        }
        /*
         * This flag indicates if menu was prevented from being shown below and
         * therefore must be made visible on callback from mouseUp().
         */
        boolean pendingEventCallback = fPendingMenuDetectEvent != null;
        Point p = toControl(e.x, e.y);
        if (e.detail == SWT.MENU_MOUSE && isOverTimeSpace(p.x, p.y)) {
            if (fPendingMenuDetectEvent == null) {
                /*
                 * Feature in Linux. The MenuDetectEvent is received before
                 * mouseDown. Store the event and trigger it later just before
                 * handling mouseUp. This allows for the method to detect if
                 * mouse is used to drag zoom.
                 */
                fPendingMenuDetectEvent = e;
                /*
                 * Prevent the platform to show the menu when returning. The
                 * menu will be shown (see below) when this method is called
                 * again during mouseUp().
                 */
                e.doit = false;
                return;
            }
            fPendingMenuDetectEvent = null;
            if (fDragState != DRAG_ZOOM || !isInDragZoomMargin()) {
                /*
                 * Don't show the menu on mouseUp() if a drag zoom is in
                 * progress with a drag range outside of the drag zoom margin,
                 * or if any other drag operation, or none, is in progress.
                 */
                e.doit = false;
                return;
            }
        } else {
            if (fDragState != DRAG_NONE) {
                /*
                 * Don't show the menu on keyboard menu or mouse menu outside of
                 * the time space if any drag operation is in progress.
                 */
                e.doit = false;
                return;
            }
        }
        int idx = getItemIndexAtY(p.y);
        if (idx >= 0 && idx < fItemData.fExpandedItems.length) {
            Item item = fItemData.fExpandedItems[idx];
            ITimeGraphEntry entry = item.fEntry;

            /* Send menu event for the time graph entry */
            e.doit = true;
            e.data = entry;
            fireMenuEventOnTimeGraphEntry(e);
            Menu menu = getMenu();
            if (pendingEventCallback && e.doit && (menu != null)) {
                menu.setVisible(true);
            }

            /* Send menu event for time event */
            if (entry.hasTimeEvents()) {
                ITimeEvent event = Utils.findEvent(entry, getTimeAtX(p.x), 2);
                if (event != null) {
                    e.doit = true;
                    e.data = event;
                    fireMenuEventOnTimeEvent(e);
                    menu = getMenu();
                    if (pendingEventCallback && e.doit && (menu != null)) {
                        menu.setVisible(true);
                    }
                }
            }
        }
    }

    /**
     * Perform the alignment operation.
     *
     * @param offset
     *            the alignment offset
     *
     * @see ITmfTimeAligned
     *
     * @since 1.0
     */
    public void performAlign(int offset) {
        fTimeProvider.setNameSpace(offset);
    }

    /**
     * Return the time alignment information
     *
     * @return the time alignment information
     *
     * @see ITmfTimeAligned
     *
     * @since 1.0
     */
    public TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo() {
        return new TmfTimeViewAlignmentInfo(getShell(), toDisplay(0, 0), fTimeProvider.getNameSpace());
    }

    private boolean isInDragZoomMargin() {
        return (Math.abs(fDragX - fDragX0) < DRAG_MARGIN);
    }
}
