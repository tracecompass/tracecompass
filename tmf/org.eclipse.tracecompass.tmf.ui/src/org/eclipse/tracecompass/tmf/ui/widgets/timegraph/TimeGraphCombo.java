/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson, others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   François Rajotte - Filter implementation
 *   Geneviève Bastien - Add event links between entries
 *   Christian Mansky - Add check active / uncheck inactive buttons
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.views.ITmfTimeAligned;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.ITimeGraphEntryActiveProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.ShowFilterDialogAction;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphMarkerAxis;

import com.google.common.collect.Iterables;

/**
 * Time graph "combo" view (with the list/tree on the left and the gantt chart
 * on the right)
 *
 * @author Patrick Tasse
 * @deprecated Use {@link #getTimeGraphViewer()} instead.
 */
@Deprecated
public class TimeGraphCombo extends Composite {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Constant indicating that all levels of the time graph should be expanded
     */
    public static final int ALL_LEVELS = AbstractTreeViewer.ALL_LEVELS;

    private static final Object FILLER = new Object();

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** The tree viewer */
    private TreeViewer fTreeViewer;

    /** The time viewer */
    private @NonNull TimeGraphViewer fTimeGraphViewer;

    /** The selection listener map */
    private final Map<ITimeGraphSelectionListener, SelectionListenerWrapper> fSelectionListenerMap = new HashMap<>();

    /** The map of viewer filters to viewer filter wrappers */
    private final Map<@NonNull ViewerFilter, @NonNull ViewerFilter> fViewerFilterMap = new HashMap<>();

    /**
     * Flag to block the tree selection changed listener when triggered by the
     * time graph combo
     */
    private boolean fInhibitTreeSelection = false;

    /** Number of filler rows used by the tree content provider */
    private int fNumFillerRows;

    /** Calculated item height for Linux workaround */
    private int fLinuxItemHeight = 0;

    /** The action that opens the filter dialog */
    private ShowFilterDialogAction fShowFilterDialogAction;

    /** Default weight of each part of the sash */
    private static final int[] DEFAULT_WEIGHTS = { 1, 1 };

    /** List of all expanded items whose parents are also expanded */
    private List<TreeItem> fVisibleExpandedItems = null;

    private Listener fSashDragListener;
    private SashForm fSashForm;

    private final boolean fScrollBarsInTreeWorkaround;

    private Font fTreeFont;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    /**
     * The TimeGraphViewerExtension is used to set appropriate values and to
     * override methods that could be called directly by the user and that must
     * be handled by the time graph combo.
     */
    private class TimeGraphViewerExtension extends TimeGraphViewer {

        private TimeGraphViewerExtension(Composite parent, int style, Tree tree) {
            super(parent, style);
            setItemHeight(TimeGraphCombo.this.getItemHeight(tree, true));
            setHeaderHeight(tree.getHeaderHeight());
            setBorderWidth(tree.getBorderWidth());
            setNameWidthPref(0);
        }

        @Override
        public ShowFilterDialogAction getShowFilterDialogAction() {
            return TimeGraphCombo.this.getShowFilterDialogAction();
        }

        @Override
        protected TimeGraphControl createTimeGraphControl(Composite composite, TimeGraphColorScheme colors) {
            return new TimeGraphControl(composite, colors) {
                @Override
                public void verticalZoom(boolean zoomIn) {
                    TimeGraphCombo.this.verticalZoom(zoomIn);
                }

                @Override
                public void resetVerticalZoom() {
                    TimeGraphCombo.this.resetVerticalZoom();
                }

                @Override
                public void setElementPosition(ITimeGraphEntry entry, int y) {
                    /*
                     * Queue the update to make sure the time graph combo has
                     * finished updating the item heights.
                     */
                    getDisplay().asyncExec(() -> {
                        if (isDisposed()) {
                            return;
                        }
                        super.setElementPosition(entry, y);
                        alignTreeItems(false);
                    });
                }
            };
        }

        private class TimeGraphMarkerAxisExtension extends TimeGraphMarkerAxis {
            private int fMargin = 0;

            public TimeGraphMarkerAxisExtension(Composite parent, @NonNull TimeGraphColorScheme colorScheme, @NonNull ITimeDataProvider timeProvider) {
                super(parent, colorScheme, timeProvider);
            }

            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                Point size = super.computeSize(wHint, hHint, changed);
                if (size.y > 0) {
                    size.y += fMargin;
                }
                return size;
            }

            @Override
            public void redraw() {
                super.redraw();
                fTreeViewer.getControl().redraw();
            }

            @Override
            protected void drawMarkerAxis(Rectangle bounds, int nameSpace, GC gc) {
                super.drawMarkerAxis(bounds, nameSpace, gc);
            }

            private Rectangle getAxisBounds() {
                Tree tree = fTreeViewer.getTree();
                Rectangle axisBounds = getBounds();
                Rectangle treeClientArea = tree.getClientArea();
                if (axisBounds.isEmpty()) {
                    treeClientArea.y += treeClientArea.height;
                    treeClientArea.height = 0;
                    return treeClientArea;
                }
                Composite axisParent = getParent();
                Point axisDisplayCoordinates = axisParent.toDisplay(axisBounds.x, axisBounds.y);
                Point axisTreeCoordinates = tree.toControl(axisDisplayCoordinates);
                int height = treeClientArea.y + treeClientArea.height - axisTreeCoordinates.y;
                int margin = Math.max(0, axisBounds.height - height);
                if (fMargin != margin) {
                    fMargin = margin;
                    getParent().layout();
                    redraw();
                    axisTreeCoordinates.y -= margin;
                    height += margin;
                }
                return new Rectangle(treeClientArea.x, axisTreeCoordinates.y, treeClientArea.width, height);
            }
        }

        @Override
        protected TimeGraphMarkerAxis createTimeGraphMarkerAxis(Composite parent, @NonNull TimeGraphColorScheme colorScheme, @NonNull ITimeDataProvider timeProvider) {
            TimeGraphMarkerAxisExtension timeGraphMarkerAxis = new TimeGraphMarkerAxisExtension(parent, colorScheme, timeProvider);
            Tree tree = fTreeViewer.getTree();
            tree.addPaintListener(e -> {
                /*
                 * Draw the marker axis over the tree. Only the name area will
                 * be drawn, since the time area has zero width.
                 */
                Rectangle bounds = timeGraphMarkerAxis.getAxisBounds();
                e.gc.setBackground(timeGraphMarkerAxis.getBackground());
                timeGraphMarkerAxis.drawMarkerAxis(bounds, bounds.width, e.gc);
            });
            tree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDown(MouseEvent e) {
                    Rectangle bounds = timeGraphMarkerAxis.getAxisBounds();
                    if (bounds.contains(e.x, e.y)) {
                        timeGraphMarkerAxis.mouseDown(e, bounds, bounds.width);
                    }
                }
            });
            tree.getHorizontalBar().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    tree.redraw();
                }
            });
            return timeGraphMarkerAxis;
        }
    }

    /**
     * The TreeContentProviderWrapper is used to insert filler items after the
     * elements of the tree's real content provider.
     */
    private class TreeContentProviderWrapper implements ITreeContentProvider {
        private final ITreeContentProvider contentProvider;

        public TreeContentProviderWrapper(ITreeContentProvider contentProvider) {
            this.contentProvider = contentProvider;
        }

        @Override
        public void dispose() {
            contentProvider.dispose();
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            contentProvider.inputChanged(viewer, oldInput, newInput);
        }

        @Override
        public Object[] getElements(Object inputElement) {
            Object[] elements = contentProvider.getElements(inputElement);
            // add filler elements to ensure alignment with time analysis viewer
            Object[] oElements = Arrays.copyOf(elements, elements.length + fNumFillerRows, Object[].class);
            for (int i = 0; i < fNumFillerRows; i++) {
                oElements[elements.length + i] = FILLER;
            }
            return oElements;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ITimeGraphEntry) {
                return contentProvider.getChildren(parentElement);
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof ITimeGraphEntry) {
                return contentProvider.getParent(element);
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof ITimeGraphEntry) {
                return contentProvider.hasChildren(element);
            }
            return false;
        }
    }

    /**
     * The TreeLabelProviderWrapper is used to intercept the filler items from
     * the calls to the tree's real label provider.
     */
    private static class TreeLabelProviderWrapper implements ITableLabelProvider {
        private final ITableLabelProvider labelProvider;

        public TreeLabelProviderWrapper(ITableLabelProvider labelProvider) {
            this.labelProvider = labelProvider;
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            labelProvider.addListener(listener);
        }

        @Override
        public void dispose() {
            labelProvider.dispose();
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.isLabelProperty(element, property);
            }
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            labelProvider.removeListener(listener);
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.getColumnImage(element, columnIndex);
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.getColumnText(element, columnIndex);
            }
            return null;
        }

    }

    /**
     * The SelectionListenerWrapper is used to intercept the filler items from
     * the time graph combo's real selection listener, and to prevent double
     * notifications from being sent when selection changes in both tree and
     * time graph at the same time.
     */
    private class SelectionListenerWrapper implements ISelectionChangedListener, ITimeGraphSelectionListener {
        private final ITimeGraphSelectionListener listener;
        private ITimeGraphEntry selection = null;

        public SelectionListenerWrapper(ITimeGraphSelectionListener listener) {
            this.listener = listener;
        }

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (fInhibitTreeSelection) {
                return;
            }
            Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
            if (element instanceof ITimeGraphEntry) {
                ITimeGraphEntry entry = (ITimeGraphEntry) element;
                if (entry != selection) {
                    selection = entry;
                    listener.selectionChanged(new TimeGraphSelectionEvent(event.getSource(), selection));
                }
            }
        }

        @Override
        public void selectionChanged(TimeGraphSelectionEvent event) {
            ITimeGraphEntry entry = event.getSelection();
            if (entry != selection) {
                selection = entry;
                listener.selectionChanged(new TimeGraphSelectionEvent(event.getSource(), selection));
            }
        }
    }

    /**
     * The ViewerFilterWrapper is used to intercept the filler items from the
     * time graph combo's real ViewerFilters. These filler items should always
     * be visible.
     */
    private static class ViewerFilterWrapper extends ViewerFilter {

        private ViewerFilter fWrappedFilter;

        ViewerFilterWrapper(ViewerFilter filter) {
            super();
            this.fWrappedFilter = filter;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof ITimeGraphEntry) {
                return fWrappedFilter.select(viewer, parentElement, element);
            }
            return true;
        }

    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new instance of this class given its parent and a style
     * value describing its behavior and appearance.
     *
     * @param parent
     *            a widget which will be the parent of the new instance (cannot
     *            be null)
     * @param style
     *            the style of widget to construct
     */
    public TimeGraphCombo(Composite parent, int style) {
        this(parent, style, DEFAULT_WEIGHTS);
    }

    /**
     * Constructs a new instance of this class given its parent and a style
     * value describing its behavior and appearance.
     *
     * @param parent
     *            a widget which will be the parent of the new instance (cannot
     *            be null)
     * @param style
     *            the style of widget to construct
     * @param weights
     *            The array (length 2) of relative weights of each side of the sash form
     */
    public TimeGraphCombo(Composite parent, int style, int[] weights) {
        super(parent, style);
        setLayout(new FillLayout());

        fSashForm = new SashForm(this, SWT.NONE);

        /*
         * In Windows, SWT.H_SCROLL | SWT.NO_SCROLL is not properly supported,
         * both scroll bars are always created. See Tree.checkStyle: "Even when
         * WS_HSCROLL or WS_VSCROLL is not specified, Windows creates trees and
         * tables with scroll bars."
         */
        fScrollBarsInTreeWorkaround = "win32".equals(SWT.getPlatform()); //$NON-NLS-1$

        int scrollBarStyle = fScrollBarsInTreeWorkaround ? SWT.H_SCROLL : SWT.H_SCROLL | SWT.NO_SCROLL;

        fTreeViewer = new TreeViewer(fSashForm, SWT.FULL_SELECTION | scrollBarStyle);
        fTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        final Tree tree = fTreeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        fTimeGraphViewer = new TimeGraphViewerExtension(fSashForm, SWT.NONE, tree);
        fTimeGraphViewer.setColumns(new String[0]);
        fTimeGraphViewer.setNameWidthPref(0);
        fTimeGraphViewer.setWeights(new int[] { 0, 1 } );

        if (fScrollBarsInTreeWorkaround) {
            // Feature in Windows. The tree vertical bar reappears when
            // the control is resized so we need to hide it again.
            tree.addControlListener(new ControlAdapter() {
                private int depth = 0;

                @Override
                public void controlResized(ControlEvent e) {
                    if (depth == 0) {
                        depth++;
                        tree.getVerticalBar().setEnabled(false);
                        // this can trigger controlResized recursively
                        tree.getVerticalBar().setVisible(false);
                        depth--;
                    }
                }
            });
        }
        // Bug in Linux. The tree header height is 0 in constructor,
        // so we need to reset it later when the control is painted.
        // This work around used to be done on control resized but the header
        // height was not initialized on the initial resize on GTK3.
        tree.addPaintListener(new PaintListener() {

    @Override
            public void paintControl(PaintEvent e) {
                int headerHeight = tree.getHeaderHeight();
                if (headerHeight > 0) {
                    fTimeGraphViewer.setHeaderHeight(headerHeight);
                    tree.removePaintListener(this);
                }
            }
        });

        tree.addDisposeListener(e -> {
            if (fTreeFont != null) {
                fTreeFont.dispose();
            }
        });

        // ensure synchronization of expanded items between tree and time graph
        fTreeViewer.addTreeListener(new ITreeViewerListener() {
            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                fTimeGraphViewer.setExpandedState((ITimeGraphEntry) event.getElement(), false);
                // queue the alignment update because the tree items may only be
                // actually collapsed after the listeners have been notified
                fVisibleExpandedItems = null; // invalidate the cache
                getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        alignTreeItems(true);
                    }});
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                ITimeGraphEntry entry = (ITimeGraphEntry) event.getElement();
                fTimeGraphViewer.setExpandedState(entry, true);
                Set<Object> expandedElements = new HashSet<>(Arrays.asList(fTreeViewer.getExpandedElements()));
                for (ITimeGraphEntry child : entry.getChildren()) {
                    if (child.hasChildren()) {
                        boolean expanded = expandedElements.contains(child);
                        fTimeGraphViewer.setExpandedState(child, expanded);
                    }
                }
                // queue the alignment update because the tree items may only be
                // actually expanded after the listeners have been notified
                fVisibleExpandedItems = null; // invalidate the cache
                getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        alignTreeItems(true);
                    }});
            }
        });

        // ensure synchronization of expanded items between tree and time graph
        fTimeGraphViewer.addTreeListener(new ITimeGraphTreeListener() {
            @Override
            public void treeCollapsed(TimeGraphTreeExpansionEvent event) {
                fTreeViewer.setExpandedState(event.getEntry(), false);
                alignTreeItems(true);
            }

            @Override
            public void treeExpanded(TimeGraphTreeExpansionEvent event) {
                ITimeGraphEntry entry = event.getEntry();
                fTreeViewer.setExpandedState(entry, true);
                Set<Object> expandedElements = new HashSet<>(Arrays.asList(fTreeViewer.getExpandedElements()));
                for (ITimeGraphEntry child : entry.getChildren()) {
                    if (child.hasChildren()) {
                        boolean expanded = expandedElements.contains(child);
                        fTimeGraphViewer.setExpandedState(child, expanded);
                    }
                }
                alignTreeItems(true);
            }
        });

        // prevent mouse button from selecting a filler tree item
        tree.addListener(SWT.MouseDown, event -> {
            List<TreeItem> treeItems = getVisibleExpandedItems(tree, false);
            if (treeItems.isEmpty()) {
                event.doit = false;
                fTreeViewer.setSelection(new StructuredSelection());
                fTimeGraphViewer.setSelection(null, false);
                return;
            }
            TreeItem lastTreeItem = treeItems.get(treeItems.size() - 1);
            if (event.y >= lastTreeItem.getBounds().y + lastTreeItem.getBounds().height) {
                event.doit = false;
                // this prevents from scrolling up when selecting
                // the partially visible tree item at the bottom
                tree.select(treeItems.get(treeItems.size() - 1));
                fTreeViewer.setSelection(new StructuredSelection());
                fTimeGraphViewer.setSelection(null);
            }
        });

        // prevent mouse wheel from scrolling down into filler tree items
        tree.addListener(SWT.MouseWheel, event -> {
            event.doit = false;
            if (event.count == 0) {
                return;
            }
            Slider scrollBar = fTimeGraphViewer.getVerticalBar();
            fTimeGraphViewer.setTopIndex(scrollBar.getSelection() - event.count);
            alignTreeItems(false);
        });

        // prevent key stroke from selecting a filler tree item
        tree.addListener(SWT.KeyDown, event -> {
            List<TreeItem> treeItems = getVisibleExpandedItems(tree, false);
            if (treeItems.size() == 0) {
                fTreeViewer.setSelection(new StructuredSelection());
                event.doit = false;
                return;
            }
            if (event.keyCode == SWT.ARROW_DOWN) {
                int index = Math.min(fTimeGraphViewer.getSelectionIndex() + 1, treeItems.size() - 1);
                fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(index).getData(), true);
                event.doit = false;
            } else if (event.keyCode == SWT.PAGE_DOWN) {
                int height = tree.getSize().y - tree.getHeaderHeight() - tree.getHorizontalBar().getSize().y;
                int countPerPage = height / getItemHeight(tree, false);
                int index = Math.min(fTimeGraphViewer.getSelectionIndex() + countPerPage - 1, treeItems.size() - 1);
                fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(index).getData());
                event.doit = false;
            } else if (event.keyCode == SWT.END) {
                fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(treeItems.size() - 1).getData());
                event.doit = false;
            } else if ((event.character == '+' || event.character == '=') && ((event.stateMask & SWT.CTRL) != 0)) {
                fTimeGraphViewer.getTimeGraphControl().keyPressed(new KeyEvent(event));
                return;
            } else if (event.character == '-' && ((event.stateMask & SWT.CTRL) != 0)) {
                fTimeGraphViewer.getTimeGraphControl().keyPressed(new KeyEvent(event));
                return;
            } else if (event.character == '0' && ((event.stateMask & SWT.CTRL) != 0)) {
                fTimeGraphViewer.getTimeGraphControl().keyPressed(new KeyEvent(event));
                return;
            } else {
                return;
            }
            if (fTimeGraphViewer.getSelectionIndex() >= 0) {
                fTreeViewer.setSelection(new StructuredSelection(fTimeGraphViewer.getSelection()));
            } else {
                fTreeViewer.setSelection(new StructuredSelection());
            }
            alignTreeItems(false);
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getTimeGraphControl().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                alignTreeItems(false);
            }
        });

        // ensure synchronization of selected item between tree and time graph
        fTreeViewer.addSelectionChangedListener(event -> {
            if (fInhibitTreeSelection) {
                return;
            }
            if (event.getSelection() instanceof IStructuredSelection) {
                Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
                if (selection instanceof ITimeGraphEntry) {
                    fTimeGraphViewer.setSelection((ITimeGraphEntry) selection, true);
                }
                alignTreeItems(false);
            }
        });

        // ensure synchronization of selected item between tree and time graph
        fTimeGraphViewer.addSelectionListener(event -> {
            ITimeGraphEntry entry = fTimeGraphViewer.getSelection();
            setSelectionInTree(entry);
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getVerticalBar().addSelectionListener(new SelectionAdapter() {

    @Override
            public void widgetSelected(SelectionEvent e) {
                alignTreeItems(false);
            }
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getTimeGraphControl().addMouseWheelListener(e -> {
            if (e.count == 0) {
                return;
            }
            alignTreeItems(false);
        });

        // ensure the tree has focus control when mouse is over it if the time graph had control
        fTreeViewer.getControl().addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                if (fTimeGraphViewer.getTimeGraphControl().isFocusControl()) {
                    fTreeViewer.getControl().setFocus();
                }
            }
        });

        // ensure the time graph has focus control when mouse is over it if the tree had control
        fTimeGraphViewer.getTimeGraphControl().addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                if (fTreeViewer.getControl().isFocusControl()) {
                    fTimeGraphViewer.getTimeGraphControl().setFocus();
                }
            }
        });
        fTimeGraphViewer.getTimeGraphScale().addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                if (fTreeViewer.getControl().isFocusControl()) {
                    fTimeGraphViewer.getTimeGraphControl().setFocus();
                }
            }
        });

        // The filler rows are required to ensure alignment when the tree does not have a
        // visible horizontal scroll bar. The tree does not allow its top item to be set
        // to a value that would cause blank space to be drawn at the bottom of the tree.
        fNumFillerRows = Display.getDefault().getBounds().height / getItemHeight(tree, false);

        fSashForm.setWeights(weights);

        fTimeGraphViewer.getTimeGraphControl().addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                // Sashes in a SashForm are being created on layout so add the
                // drag listener here
                if (fSashDragListener == null) {
                    for (Control control : fSashForm.getChildren()) {
                        if (control instanceof Sash) {
                            fSashDragListener = new Listener() {

                                @Override
                                public void handleEvent(Event event) {
                                    sendTimeViewAlignmentChanged();

                                }
                            };
                            control.removePaintListener(this);
                            control.addListener(SWT.Selection, fSashDragListener);
                            // There should be only one sash
                            break;
                        }
                    }
                }
            }
        });
    }

    private void verticalZoom(boolean zoomIn) {
        Tree tree = fTreeViewer.getTree();
        FontData fontData = tree.getFont().getFontData()[0];
        int height = fontData.getHeight() + (zoomIn ? 1 : -1);
        if (height <= 0) {
            return;
        }
        fontData.setHeight(height);
        if (fTreeFont != null) {
            fTreeFont.dispose();
        }
        fTreeFont = new Font(tree.getDisplay(), fontData);
        tree.setFont(fTreeFont);
        redraw();
        update();
        fTimeGraphViewer.setHeaderHeight(tree.getHeaderHeight());
        fTimeGraphViewer.setItemHeight(getItemHeight(tree, true));
        alignTreeItems(false);
    }

    private void resetVerticalZoom() {
        Tree tree = fTreeViewer.getTree();
        if (fTreeFont != null) {
            fTreeFont.dispose();
            fTreeFont = null;
        }
        tree.setFont(null);
        redraw();
        update();
        fTimeGraphViewer.setHeaderHeight(tree.getHeaderHeight());
        fTimeGraphViewer.setItemHeight(getItemHeight(tree, true));
        alignTreeItems(false);
    }

    private void sendTimeViewAlignmentChanged() {
        TmfSignalManager.dispatchSignal(new TmfTimeViewAlignmentSignal(fSashForm, getTimeViewAlignmentInfo()));
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns this time graph combo's tree viewer.
     *
     * @return the tree viewer
     */
    public TreeViewer getTreeViewer() {
        return fTreeViewer;
    }

    /**
     * Returns this time graph combo's time graph viewer.
     *
     * @return the time graph viewer
     */
    public @NonNull TimeGraphViewer getTimeGraphViewer() {
        return fTimeGraphViewer;
    }

    /**
     * Get the show filter dialog action.
     *
     * @return The Action object
     * @since 1.2
     */
    public ShowFilterDialogAction getShowFilterDialogAction() {
        if (fShowFilterDialogAction == null) {
            fShowFilterDialogAction = new ShowFilterDialogAction(fTimeGraphViewer) {
                @Override
                protected void addFilter(ViewerFilter filter) {
                    /* add filter to the combo instead of the viewer */
                    TimeGraphCombo.this.addFilter(filter);
                }

                @Override
                protected void removeFilter(ViewerFilter filter) {
                    /* remove filter from the combo instead of the viewer */
                    TimeGraphCombo.this.removeFilter(filter);
                }

                @Override
                protected void refresh() {
                    /* refresh the combo instead of the viewer */
                    TimeGraphCombo.this.refresh();
                }
            };
        }
        return fShowFilterDialogAction;
    }

    // ------------------------------------------------------------------------
    // Control
    // ------------------------------------------------------------------------

    @Override
    public void redraw() {
        fTimeGraphViewer.getControl().redraw();
        super.redraw();
    }

    @Override
    public void update() {
        fTimeGraphViewer.getControl().update();
        super.update();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Sets the tree content provider used by this time graph combo.
     *
     * @param contentProvider
     *            the tree content provider
     */
    public void setTreeContentProvider(ITreeContentProvider contentProvider) {
        fTreeViewer.setContentProvider(new TreeContentProviderWrapper(contentProvider));
    }

    /**
     * Sets the tree label provider used by this time graph combo.
     *
     * @param labelProvider
     *            the tree label provider
     */
    public void setTreeLabelProvider(ITableLabelProvider labelProvider) {
        fTreeViewer.setLabelProvider(new TreeLabelProviderWrapper(labelProvider));
    }

    /**
     * Sets the tree content provider used by the filter dialog
     *
     * @param contentProvider
     *            the tree content provider
     */
    public void setFilterContentProvider(ITreeContentProvider contentProvider) {
        getShowFilterDialogAction().getFilterDialog().setContentProvider(contentProvider);
    }

    /**
     * Sets the tree label provider used by the filter dialog
     *
     * @param labelProvider
     *            the tree label provider
     */
    public void setFilterLabelProvider(ITableLabelProvider labelProvider) {
        getShowFilterDialogAction().getFilterDialog().setLabelProvider(labelProvider);
    }

    /**
     * Adds a "check active" button used by the filter dialog
     *
     * @param activeProvider
     *            Additional button info specific to a certain view.
     * @since 1.0
     */
    public void addTimeGraphFilterCheckActiveButton(ITimeGraphEntryActiveProvider activeProvider) {
        getShowFilterDialogAction().getFilterDialog().addTimeGraphFilterCheckActiveButton(activeProvider);
    }

    /**
     * Adds an "uncheck inactive" button used by the filter dialog
     *
     * @param inactiveProvider
     *            Additional button info specific to a certain view.
     * @since 1.0
     */
    public void addTimeGraphFilterUncheckInactiveButton(ITimeGraphEntryActiveProvider inactiveProvider) {
        getShowFilterDialogAction().getFilterDialog().addTimeGraphFilterUncheckInactiveButton(inactiveProvider);
    }

    /**
     * Sets the tree columns for this time graph combo.
     *
     * @param columnNames
     *            the tree column names
     */
    public void setTreeColumns(String[] columnNames) {
        final Tree tree = fTreeViewer.getTree();
        for (String columnName : columnNames) {
            TreeColumn column = new TreeColumn(tree, SWT.LEFT);
            column.setMoveable(true);
            column.setText(columnName);
            column.pack();
        }
    }

    /**
     * Sets the tree columns for this time graph combo's filter dialog.
     *
     * @param columnNames
     *            the tree column names
     */
    public void setFilterColumns(String[] columnNames) {
        getShowFilterDialogAction().getFilterDialog().setColumnNames(columnNames);
    }

    /**
     * Sets the time graph content provider used by this time graph combo.
     *
     * @param timeGraphContentProvider
     *            the time graph content provider
     */
    public void setTimeGraphContentProvider(ITimeGraphContentProvider timeGraphContentProvider) {
        fTimeGraphViewer.setTimeGraphContentProvider(timeGraphContentProvider);
    }

    /**
     * Sets the time graph presentation provider used by this time graph combo.
     *
     * @param timeGraphProvider
     *            the time graph provider
     */
    public void setTimeGraphProvider(ITimeGraphPresentationProvider timeGraphProvider) {
        fTimeGraphViewer.setTimeGraphProvider(timeGraphProvider);
    }

    /**
     * Sets or clears the input for this time graph combo.
     *
     * @param input
     *            the input of this time graph combo, or <code>null</code> if
     *            none
     */
    public void setInput(Object input) {
        fInhibitTreeSelection = true;
        fTreeViewer.setInput(input);
        for (SelectionListenerWrapper listenerWrapper : fSelectionListenerMap.values()) {
            listenerWrapper.selection = null;
        }
        fInhibitTreeSelection = false;
        if (fScrollBarsInTreeWorkaround) {
            fTreeViewer.getTree().getVerticalBar().setEnabled(false);
            fTreeViewer.getTree().getVerticalBar().setVisible(false);
        }
        fTimeGraphViewer.setInput(input);
        fTimeGraphViewer.setItemHeight(getItemHeight(fTreeViewer.getTree(), false));
        // queue the alignment update because in Linux the item bounds are not
        // set properly until the tree has been painted at least once
        fVisibleExpandedItems = null; // invalidate the cache
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (isDisposed()) {
                    return;
                }
                alignTreeItems(true);
            }
        });
    }

    /**
     * Gets the input for this time graph combo.
     *
     * @return The input of this time graph combo, or <code>null</code> if none
     */
    public Object getInput() {
        return fTreeViewer.getInput();
    }

    /**
     * Sets or clears the list of links to display on this combo
     *
     * @param links
     *            the links to display in this time graph combo
     */
    public void setLinks(List<ILinkEvent> links) {
        fTimeGraphViewer.setLinks(links);
    }

    /**
     * @param filter
     *            The filter object to be attached to the view
     */
    public void addFilter(@NonNull ViewerFilter filter) {
        fInhibitTreeSelection = true;
        ViewerFilter wrapper = new ViewerFilterWrapper(filter);
        fTreeViewer.addFilter(wrapper);
        fTimeGraphViewer.addFilter(filter);
        fViewerFilterMap.put(filter, wrapper);
        alignTreeItems(true);
        fInhibitTreeSelection = false;
    }

    /**
     * @param filter
     *            The filter object to be removed from the view
     */
    public void removeFilter(@NonNull ViewerFilter filter) {
        fInhibitTreeSelection = true;
        ViewerFilter wrapper = fViewerFilterMap.get(filter);
        fTreeViewer.removeFilter(wrapper);
        fTimeGraphViewer.removeFilter(filter);
        fViewerFilterMap.remove(filter);
        alignTreeItems(true);
        fInhibitTreeSelection = false;
    }

    /**
     * Returns this viewer's filters.
     *
     * @return an array of viewer filters
     * @since 1.2
     */
    public @NonNull ViewerFilter[] getFilters() {
        return fTimeGraphViewer.getFilters();
    }

    /**
     * Sets the filters, replacing any previous filters, and triggers
     * refiltering of the elements.
     *
     * @param filters
     *            an array of viewer filters, or null
     * @since 1.2
     */
    public void setFilters(@NonNull ViewerFilter[] filters) {
        fInhibitTreeSelection = true;
        fViewerFilterMap.clear();
        if (filters == null) {
            fTreeViewer.resetFilters();
        } else {
            for (ViewerFilter filter : filters) {
                ViewerFilter wrapper = new ViewerFilterWrapper(filter);
                fViewerFilterMap.put(filter, wrapper);
            }
            ViewerFilter[] wrappers = Iterables.toArray(fViewerFilterMap.values(), ViewerFilter.class);
            fTreeViewer.setFilters(wrappers);
        }
        fTimeGraphViewer.setFilters(filters);
        alignTreeItems(true);
        fInhibitTreeSelection = false;
    }

    /**
     * Refreshes this time graph completely with information freshly obtained
     * from its model.
     */
    public void refresh() {
        fInhibitTreeSelection = true;
        Tree tree = fTreeViewer.getTree();
        try {
            tree.setRedraw(false);
            fTreeViewer.refresh();
        } finally {
            tree.setRedraw(true);
        }
        fTimeGraphViewer.refresh();
        alignTreeItems(true);
        fInhibitTreeSelection = false;
    }

    /**
     * Adds a listener for selection changes in this time graph combo.
     *
     * @param listener
     *            a selection listener
     */
    public void addSelectionListener(ITimeGraphSelectionListener listener) {
        SelectionListenerWrapper listenerWrapper = new SelectionListenerWrapper(listener);
        fTreeViewer.addSelectionChangedListener(listenerWrapper);
        fSelectionListenerMap.put(listener, listenerWrapper);
        fTimeGraphViewer.addSelectionListener(listenerWrapper);
    }

    /**
     * Removes the given selection listener from this time graph combo.
     *
     * @param listener
     *            a selection changed listener
     */
    public void removeSelectionListener(ITimeGraphSelectionListener listener) {
        SelectionListenerWrapper listenerWrapper = fSelectionListenerMap.remove(listener);
        fTreeViewer.removeSelectionChangedListener(listenerWrapper);
        fTimeGraphViewer.removeSelectionListener(listenerWrapper);
    }

    /**
     * Sets the current selection for this time graph combo.
     *
     * @param selection
     *            the new selection
     */
    public void setSelection(ITimeGraphEntry selection) {
        fTimeGraphViewer.setSelection(selection, true);
        setSelectionInTree(selection);
    }

    /**
     * Sets the current selection for this time graph combo and reveal it if
     * needed.
     *
     * @param selection
     *            The new selection
     * @since 2.0
     */
    public void selectAndReveal(@NonNull ITimeGraphEntry selection) {
        fTimeGraphViewer.selectAndReveal(selection);
        setSelectionInTree(selection);
    }

    /**
     * Select the entry in the tree structure
     *
     * @param selection
     *            The new selection
     */
    private void setSelectionInTree(ITimeGraphEntry selection) {
        fInhibitTreeSelection = true; // block the tree selection changed
                                      // listener
        if (selection != null) {
            StructuredSelection structuredSelection = new StructuredSelection(selection);
            fTreeViewer.setSelection(structuredSelection);
        } else {
            fTreeViewer.setSelection(new StructuredSelection());
        }
        fInhibitTreeSelection = false;
        alignTreeItems(false);
    }

    /**
     * Sets the auto-expand level to be used for new entries discovered when
     * calling {@link #setInput(Object)} or {@link #refresh()}. The value 0
     * means that there is no auto-expand; 1 means that top-level entries are
     * expanded, but not their children; 2 means that top-level entries are
     * expanded, and their children, but not grand-children; and so on.
     * <p>
     * The value {@link #ALL_LEVELS} means that all subtrees should be expanded.
     * </p>
     *
     * @param level
     *            non-negative level, or <code>ALL_LEVELS</code> to expand all
     *            levels of the tree
     */
    public void setAutoExpandLevel(int level) {
        fTimeGraphViewer.setAutoExpandLevel(level);
        if (level <= 0) {
            fTreeViewer.setAutoExpandLevel(level);
        } else {
            fTreeViewer.setAutoExpandLevel(level + 1);
        }
    }

    /**
     * Returns the auto-expand level.
     *
     * @return non-negative level, or <code>ALL_LEVELS</code> if all levels of
     *         the tree are expanded automatically
     * @see #setAutoExpandLevel
     */
    public int getAutoExpandLevel() {
        return fTimeGraphViewer.getAutoExpandLevel();
    }

    /**
     * Get the expanded state of an entry.
     *
     * @param entry
     *            The entry
     * @return true if the entry is expanded, false if collapsed
     * @since 2.0
     */
    public boolean getExpandedState(ITimeGraphEntry entry) {
        return fTimeGraphViewer.getExpandedState(entry);
    }

    /**
     * Set the expanded state of an entry
     *
     * @param entry
     *            The entry to expand/collapse
     * @param expanded
     *            True for expanded, false for collapsed
     */
    public void setExpandedState(ITimeGraphEntry entry, boolean expanded) {
        fTimeGraphViewer.setExpandedState(entry, expanded);
        fTreeViewer.setExpandedState(entry, expanded);
        alignTreeItems(true);
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root.
     */
    public void collapseAll() {
        fTimeGraphViewer.collapseAll();
        fTreeViewer.collapseAll();
        alignTreeItems(true);
    }

    /**
     * Expands all nodes of the viewer's tree, starting with the root.
     */
    public void expandAll() {
        fTimeGraphViewer.expandAll();
        fTreeViewer.expandAll();
        alignTreeItems(true);
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    private List<TreeItem> getVisibleExpandedItems(Tree tree, boolean refresh) {
        if (fVisibleExpandedItems == null || refresh) {
            List<TreeItem> visibleExpandedItems = new ArrayList<>();
            addVisibleExpandedItems(visibleExpandedItems, tree.getItems());
            fVisibleExpandedItems = visibleExpandedItems;
        }
        return fVisibleExpandedItems;
    }

    private void addVisibleExpandedItems(List<TreeItem> visibleExpandedItems, TreeItem[] items) {
        for (TreeItem item : items) {
            Object data = item.getData();
            if (data == FILLER) {
                break;
            }
            visibleExpandedItems.add(item);
            boolean expandedState = fTimeGraphViewer.getExpandedState((ITimeGraphEntry) data);
            if (item.getExpanded() != expandedState) {
                /* synchronize the expanded state of both viewers */
                fTreeViewer.setExpandedState(data, expandedState);
            }
            if (expandedState) {
                addVisibleExpandedItems(visibleExpandedItems, item.getItems());
            }
        }
    }

    private int getItemHeight(final Tree tree, boolean force) {
        /*
         * Bug in Linux. The method getItemHeight doesn't always return the
         * correct value.
         */
        if (fLinuxItemHeight >= 0 && System.getProperty("os.name").contains("Linux")) { //$NON-NLS-1$ //$NON-NLS-2$
            if (fLinuxItemHeight != 0 && !force) {
                return fLinuxItemHeight;
            }

            if (getVisibleExpandedItems(tree, true).size() > 1) {
                PaintListener paintListener = new PaintListener() {
                    @Override
                    public void paintControl(PaintEvent e) {
                        // get the treeItems here to have all items
                        List<TreeItem> treeItems = getVisibleExpandedItems(tree, true);
                        if (treeItems.size() < 2) {
                            return;
                        }
                        final TreeItem treeItem0 = treeItems.get(0);
                        final TreeItem treeItem1 = treeItems.get(1);
                        tree.removePaintListener(this);
                        int y0 = treeItem0.getBounds().y;
                        int y1 = treeItem1.getBounds().y;
                        int itemHeight = y1 - y0;
                        if (itemHeight > 0) {
                            fLinuxItemHeight = itemHeight;
                            fTimeGraphViewer.setItemHeight(itemHeight);
                        }
                    }
                };
                tree.addPaintListener(paintListener);
            }
        } else {
            fLinuxItemHeight = -1; // Not Linux, don't perform os.name check
                                   // anymore
        }
        return tree.getItemHeight();
    }

    private void alignTreeItems(boolean refreshExpandedItems) {

        // align the tree top item with the time graph top item
        Tree tree = fTreeViewer.getTree();
        List<TreeItem> treeItems = getVisibleExpandedItems(tree, refreshExpandedItems);
        int topIndex = fTimeGraphViewer.getTopIndex();
        if (topIndex >= treeItems.size()) {
            return;
        }
        TreeItem item = treeItems.get(topIndex);
        tree.setTopItem(item);
        /*
         * In GTK3, the bounds of the tree items are only sure to be correct
         * after the tree has been painted.
         */
        tree.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                tree.removePaintListener(this);
                doAlignTreeItems();
                redraw();
            }
        });
        /* Make sure the paint event is triggered. */
        tree.redraw();
    }

    private void doAlignTreeItems() {
        Tree tree = fTreeViewer.getTree();
        List<TreeItem> treeItems = getVisibleExpandedItems(tree, false);
        int topIndex = fTimeGraphViewer.getTopIndex();
        if (topIndex >= treeItems.size()) {
            return;
        }
        TreeItem item = treeItems.get(topIndex);

        // get the first filler item so we can calculate the last item's height
        TreeItem fillerItem = null;
        for (TreeItem treeItem : fTreeViewer.getTree().getItems()) {
            if (treeItem.getData() == FILLER) {
                fillerItem = treeItem;
                break;
            }
        }

        // ensure the time graph item heights are equal to the tree item heights
        int treeHeight = fTreeViewer.getTree().getBounds().height;
        int index = topIndex;
        Rectangle bounds = item.getBounds();
        while (index < treeItems.size()) {
            if (bounds.y > treeHeight) {
                break;
            }
            TreeItem nextItem = (index + 1 == treeItems.size()) ? fillerItem : treeItems.get(index + 1);
            Rectangle nextBounds = alignTreeItem(item, bounds, nextItem);
            index++;
            item = nextItem;
            bounds = nextBounds;
        }

        /*
         * When an item's height in the time graph changes, it is possible that
         * the time graph readjusts its top index to fill empty space at the
         * bottom of the viewer. Calling method setTopIndex() triggers this
         * adjustment, if needed. In that case, we need to make sure that the
         * newly visible items at the top of the viewer are also aligned.
         */
        fTimeGraphViewer.setTopIndex(topIndex);
        if (fTimeGraphViewer.getTopIndex() != topIndex) {
            alignTreeItems(false);
        }
    }

    private Rectangle alignTreeItem(TreeItem item, Rectangle bounds, TreeItem nextItem) {
        /*
         * Bug in Linux. The method getBounds doesn't always return the correct
         * height. Use the difference of y position between items to calculate
         * the height.
         */
        Rectangle nextBounds = nextItem.getBounds();
        Integer itemHeight = nextBounds.y - bounds.y;
        if (itemHeight > 0) {
            ITimeGraphEntry entry = (ITimeGraphEntry) item.getData();
            fTimeGraphViewer.getTimeGraphControl().setItemHeight(entry, itemHeight);
        }
        return nextBounds;
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
        Point location = fSashForm.toDisplay(0, 0);
        int timeAxisOffset = fTreeViewer.getControl().getSize().x + fSashForm.getSashWidth();
        return new TmfTimeViewAlignmentInfo(fSashForm.getShell(), location, timeAxisOffset);
    }

    /**
     * Return the available width for the time-axis.
     *
     * @see ITmfTimeAligned
     *
     * @param requestedOffset
     *            the requested offset
     * @return the available width for the time-axis
     *
     * @since 1.0
     */
    public int getAvailableWidth(int requestedOffset) {
        int vBarWidth = ((fTimeGraphViewer.getVerticalBar() != null) && (fTimeGraphViewer.getVerticalBar().isVisible())) ? fTimeGraphViewer.getVerticalBar().getSize().x : 0;
        int totalWidth = fSashForm.getBounds().width;
        return Math.min(totalWidth, Math.max(0, totalWidth - requestedOffset - vBarWidth));
    }

    /**
     * Perform the alignment operation.
     *
     * @param offset
     *            the alignment offset
     * @param width
     *            the alignment width
     *
     * @see ITmfTimeAligned
     *
     * @since 1.0
     */
    public void performAlign(int offset, int width) {
        int total = fSashForm.getBounds().width;
        int timeAxisOffset = Math.min(offset, total);
        int width1 = Math.max(0, timeAxisOffset - fSashForm.getSashWidth());
        int width2 = total - timeAxisOffset;
        if (width1 >= 0 && width2 > 0 || width1 > 0 && width2 >= 0) {
            fSashForm.setWeights(new int[] { width1, width2 });
            fSashForm.layout();
        }

        Composite composite = fTimeGraphViewer.getTimeAlignedComposite();
        GridLayout layout = (GridLayout) composite.getLayout();
        int timeBasedControlsWidth = composite.getSize().x;
        int marginSize = timeBasedControlsWidth - width;
        layout.marginRight = Math.max(0, marginSize);
        composite.layout();
    }
}
