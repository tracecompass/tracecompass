/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson, others
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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
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
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphFilterDialog;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Time graph "combo" view (with the list/tree on the left and the gantt chart
 * on the right)
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeGraphCombo extends Composite {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** Constant indicating that all levels of the time graph should be expanded
     * @since 3.1 */
    public static final int ALL_LEVELS = AbstractTreeViewer.ALL_LEVELS;

    private static final Object FILLER = new Object();

    private static final String ITEM_HEIGHT = "$height$"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** The tree viewer */
    private TreeViewer fTreeViewer;

    /** The time viewer */
    private TimeGraphViewer fTimeGraphViewer;

    /** The selection listener map */
    private final Map<ITimeGraphSelectionListener, SelectionListenerWrapper> fSelectionListenerMap = new HashMap<>();

    /** The map of viewer filters */
    private final Map<ViewerFilter, ViewerFilter> fViewerFilterMap = new HashMap<>();

    /**
     * Flag to block the tree selection changed listener when triggered by the
     * time graph combo
     */
    private boolean fInhibitTreeSelection = false;

    /** Number of filler rows used by the tree content provider */
    private int fNumFillerRows;

    /** Calculated item height for Linux workaround */
    private int fLinuxItemHeight = 0;

    /** The button that opens the filter dialog */
    private Action showFilterAction;

    /** The filter dialog */
    private TimeGraphFilterDialog fFilterDialog;

    /** The filter generated from the filter dialog */
    private RawViewerFilter fFilter;

    /** Default weight of each part of the sash */
    private static final int[] DEFAULT_WEIGHTS = { 1, 1 };

    /** List of all expanded items whose parents are also expanded */
    private List<TreeItem> fVisibleExpandedItems = null;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    /**
     * The TreeContentProviderWrapper is used to insert filler items after
     * the elements of the tree's real content provider.
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
     * The TreeLabelProviderWrapper is used to intercept the filler items
     * from the calls to the tree's real label provider.
     */
    private class TreeLabelProviderWrapper implements ITableLabelProvider {
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
     * The ViewerFilterWrapper is used to intercept the filler items from
     * the time graph combo's real ViewerFilters. These filler items should
     * always be visible.
     */
    private class ViewerFilterWrapper extends ViewerFilter {

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

    /**
     * This filter simply keeps a list of elements that should be filtered out.
     * All the other elements will be shown.
     * By default and when the list is set to null, all elements are shown.
     */
    private class RawViewerFilter extends ViewerFilter {

        private List<Object> fFiltered = null;

        public void setFiltered(List<Object> objects) {
            fFiltered = objects;
        }

        public List<Object> getFiltered() {
            return fFiltered;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (fFiltered == null) {
                return true;
            }
            return !fFiltered.contains(element);
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new instance of this class given its parent
     * and a style value describing its behavior and appearance.
     *
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
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
     *            The relative weights of each side of the sash form
     * @since 2.1
     */
    public TimeGraphCombo(Composite parent, int style, int[] weights) {
        super(parent, style);
        setLayout(new FillLayout());

        final SashForm sash = new SashForm(this, SWT.NONE);

        fTreeViewer = new TreeViewer(sash, SWT.FULL_SELECTION | SWT.H_SCROLL);
        fTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        final Tree tree = fTreeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        fTimeGraphViewer = new TimeGraphViewer(sash, SWT.NONE);
        fTimeGraphViewer.setItemHeight(getItemHeight(tree));
        fTimeGraphViewer.setHeaderHeight(tree.getHeaderHeight());
        fTimeGraphViewer.setBorderWidth(tree.getBorderWidth());
        fTimeGraphViewer.setNameWidthPref(0);

        fFilter = new RawViewerFilter();
        addFilter(fFilter);

        fFilterDialog = new TimeGraphFilterDialog(getShell());

        // Feature in Windows. The tree vertical bar reappears when
        // the control is resized so we need to hide it again.
        // Bug in Linux. The tree header height is 0 in constructor,
        // so we need to reset it later when the control is resized.
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
                fTimeGraphViewer.setHeaderHeight(tree.getHeaderHeight());
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
        tree.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                TreeItem treeItem = tree.getItem(new Point(event.x, event.y));
                if (treeItem == null || treeItem.getData() == FILLER) {
                    event.doit = false;
                    List<TreeItem> treeItems = getVisibleExpandedItems(tree, false);
                    if (treeItems.size() == 0) {
                        fTreeViewer.setSelection(new StructuredSelection());
                        fTimeGraphViewer.setSelection(null);
                        return;
                    }
                    // this prevents from scrolling up when selecting
                    // the partially visible tree item at the bottom
                    tree.select(treeItems.get(treeItems.size() - 1));
                    fTreeViewer.setSelection(new StructuredSelection());
                    fTimeGraphViewer.setSelection(null);
                }
            }
        });

        // prevent mouse wheel from scrolling down into filler tree items
        tree.addListener(SWT.MouseWheel, new Listener() {
            @Override
            public void handleEvent(Event event) {
                event.doit = false;
                Slider scrollBar = fTimeGraphViewer.getVerticalBar();
                fTimeGraphViewer.setTopIndex(scrollBar.getSelection() - event.count);
                alignTreeItems(false);
            }
        });

        // prevent key stroke from selecting a filler tree item
        tree.addListener(SWT.KeyDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                List<TreeItem> treeItems = getVisibleExpandedItems(tree, false);
                if (treeItems.size() == 0) {
                    fTreeViewer.setSelection(new StructuredSelection());
                    event.doit = false;
                    return;
                }
                if (event.keyCode == SWT.ARROW_DOWN) {
                    int index = Math.min(fTimeGraphViewer.getSelectionIndex() + 1, treeItems.size() - 1);
                    fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(index).getData());
                    event.doit = false;
                } else if (event.keyCode == SWT.PAGE_DOWN) {
                    int height = tree.getSize().y - tree.getHeaderHeight() - tree.getHorizontalBar().getSize().y;
                    int countPerPage = height / getItemHeight(tree);
                    int index = Math.min(fTimeGraphViewer.getSelectionIndex() + countPerPage - 1, treeItems.size() - 1);
                    fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(index).getData());
                    event.doit = false;
                } else if (event.keyCode == SWT.END) {
                    fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(treeItems.size() - 1).getData());
                    event.doit = false;
                }
                if (fTimeGraphViewer.getSelectionIndex() >= 0) {
                    fTreeViewer.setSelection(new StructuredSelection(fTimeGraphViewer.getSelection()));
                } else {
                    fTreeViewer.setSelection(new StructuredSelection());
                }
                alignTreeItems(false);
            }
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getTimeGraphControl().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                alignTreeItems(false);
            }
        });

        // ensure synchronization of selected item between tree and time graph
        fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (fInhibitTreeSelection) {
                    return;
                }
                if (event.getSelection() instanceof IStructuredSelection) {
                    Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
                    if (selection instanceof ITimeGraphEntry) {
                        fTimeGraphViewer.setSelection((ITimeGraphEntry) selection);
                    }
                    alignTreeItems(false);
                }
            }
        });

        // ensure synchronization of selected item between tree and time graph
        fTimeGraphViewer.addSelectionListener(new ITimeGraphSelectionListener() {
            @Override
            public void selectionChanged(TimeGraphSelectionEvent event) {
                ITimeGraphEntry entry = fTimeGraphViewer.getSelection();
                fInhibitTreeSelection = true; // block the tree selection changed listener
                if (entry != null) {
                    StructuredSelection selection = new StructuredSelection(entry);
                    fTreeViewer.setSelection(selection);
                } else {
                    fTreeViewer.setSelection(new StructuredSelection());
                }
                fInhibitTreeSelection = false;
                alignTreeItems(false);
            }
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getVerticalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                alignTreeItems(false);
            }
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getTimeGraphControl().addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                alignTreeItems(false);
            }
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
        fNumFillerRows = Display.getDefault().getBounds().height / getItemHeight(tree);

        sash.setWeights(weights);
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
    public TimeGraphViewer getTimeGraphViewer() {
        return fTimeGraphViewer;
    }

    /**
     * Callback for the show filter action
     *
     * @since 2.0
     */
    public void showFilterDialog() {
        ITimeGraphEntry[] topInput = fTimeGraphViewer.getTimeGraphContentProvider().getElements(fTimeGraphViewer.getInput());
        if (topInput != null) {
            List<? extends ITimeGraphEntry> allElements = listAllInputs(Arrays.asList(topInput));
            fFilterDialog.setInput(fTimeGraphViewer.getInput());
            fFilterDialog.setTitle(Messages.TmfTimeFilterDialog_WINDOW_TITLE);
            fFilterDialog.setMessage(Messages.TmfTimeFilterDialog_MESSAGE);
            fFilterDialog.setExpandedElements(allElements.toArray());
            if (fFilter.getFiltered() != null) {
                ArrayList<? extends ITimeGraphEntry> nonFilteredElements = new ArrayList<>(allElements);
                nonFilteredElements.removeAll(fFilter.getFiltered());
                fFilterDialog.setInitialElementSelections(nonFilteredElements);
            } else {
                fFilterDialog.setInitialElementSelections(allElements);
            }
            fFilterDialog.create();
            fFilterDialog.open();
            // Process selected elements
            if (fFilterDialog.getResult() != null) {
                fInhibitTreeSelection = true;
                if (fFilterDialog.getResult().length != allElements.size()) {
                    ArrayList<Object> filteredElements = new ArrayList<Object>(allElements);
                    filteredElements.removeAll(Arrays.asList(fFilterDialog.getResult()));
                    fFilter.setFiltered(filteredElements);
                } else {
                    fFilter.setFiltered(null);
                }
                fTreeViewer.refresh();
                fTreeViewer.expandAll();
                fTimeGraphViewer.refresh();
                fInhibitTreeSelection = false;
                alignTreeItems(true);
                // Reset selection
                if (fFilterDialog.getResult().length > 0) {
                    setSelection(null);
                }
            }
        }
    }

    /**
     * Get the show filter action.
     *
     * @return The Action object
     * @since 2.0
     */
    public Action getShowFilterAction() {
        if (showFilterAction == null) {
            // showFilter
            showFilterAction = new Action() {
                @Override
                public void run() {
                    showFilterDialog();
                }
            };
            showFilterAction.setText(Messages.TmfTimeGraphCombo_FilterActionNameText);
            showFilterAction.setToolTipText(Messages.TmfTimeGraphCombo_FilterActionToolTipText);
            // TODO find a nice, distinctive icon
            showFilterAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FILTERS));
        }

        return showFilterAction;
    }

    // ------------------------------------------------------------------------
    // Control
    // ------------------------------------------------------------------------

    @Override
    public void redraw() {
        fTimeGraphViewer.getControl().redraw();
        super.redraw();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Sets the tree content provider used by this time graph combo.
     *
     * @param contentProvider the tree content provider
     */
    public void setTreeContentProvider(ITreeContentProvider contentProvider) {
        fTreeViewer.setContentProvider(new TreeContentProviderWrapper(contentProvider));
    }

    /**
     * Sets the tree label provider used by this time graph combo.
     *
     * @param labelProvider the tree label provider
     */
    public void setTreeLabelProvider(ITableLabelProvider labelProvider) {
        fTreeViewer.setLabelProvider(new TreeLabelProviderWrapper(labelProvider));
    }

    /**
     * Sets the tree content provider used by the filter dialog
     *
     * @param contentProvider the tree content provider
     * @since 2.0
     */
    public void setFilterContentProvider(ITreeContentProvider contentProvider) {
        fFilterDialog.setContentProvider(contentProvider);
    }

    /**
     * Sets the tree label provider used by the filter dialog
     *
     * @param labelProvider the tree label provider
     * @since 2.0
     */
    public void setFilterLabelProvider(ITableLabelProvider labelProvider) {
        fFilterDialog.setLabelProvider(labelProvider);
    }

    /**
     * Sets the tree columns for this time graph combo.
     *
     * @param columnNames the tree column names
     */
    public void setTreeColumns(String[] columnNames) {
        final Tree tree = fTreeViewer.getTree();
        for (String columnName : columnNames) {
            TreeColumn column = new TreeColumn(tree, SWT.LEFT);
            column.setText(columnName);
            column.pack();
        }
    }

    /**
     * Sets the tree columns for this time graph combo's filter dialog.
     *
     * @param columnNames the tree column names
     * @since 2.0
     */
    public void setFilterColumns(String[] columnNames) {
        fFilterDialog.setColumnNames(columnNames);
    }

    /**
     * Sets the time graph content provider used by this time graph combo.
     *
     * @param timeGraphContentProvider
     *            the time graph content provider
     *
     * @since 3.0
     */
    public void setTimeGraphContentProvider(ITimeGraphContentProvider timeGraphContentProvider) {
        fTimeGraphViewer.setTimeGraphContentProvider(timeGraphContentProvider);
    }

    /**
     * Sets the time graph presentation provider used by this time graph combo.
     *
     * @param timeGraphProvider the time graph provider
     */
    public void setTimeGraphProvider(ITimeGraphPresentationProvider timeGraphProvider) {
        fTimeGraphViewer.setTimeGraphProvider(timeGraphProvider);
    }

    /**
     * Sets or clears the input for this time graph combo.
     *
     * @param input the input of this time graph combo, or <code>null</code> if none
     *
     * @since 3.0
     */
    public void setInput(Object input) {
        fFilter.setFiltered(null);
        fInhibitTreeSelection = true;
        fTreeViewer.setInput(input);
        for (SelectionListenerWrapper listenerWrapper : fSelectionListenerMap.values()) {
            listenerWrapper.selection = null;
        }
        fInhibitTreeSelection = false;
        fTreeViewer.getTree().getVerticalBar().setEnabled(false);
        fTreeViewer.getTree().getVerticalBar().setVisible(false);
        fTimeGraphViewer.setItemHeight(getItemHeight(fTreeViewer.getTree()));
        fTimeGraphViewer.setInput(input);
        // queue the alignment update because in Linux the item bounds are not
        // set properly until the tree has been painted at least once
        fVisibleExpandedItems = null; // invalidate the cache
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                alignTreeItems(true);
            }});
    }

    /**
     * Gets the input for this time graph combo.
     *
     * @return The input of this time graph combo, or <code>null</code> if none
     *
     * @since 3.0
     */
    public Object getInput() {
        return fTreeViewer.getInput();
    }

    /**
     * Sets or clears the list of links to display on this combo
     *
     * @param links the links to display in this time graph combo
     * @since 2.1
     */
    public void setLinks(List<ILinkEvent> links) {
        fTimeGraphViewer.setLinks(links);
    }

    /**
     * @param filter The filter object to be attached to the view
     * @since 2.0
     */
    public void addFilter(ViewerFilter filter) {
        ViewerFilter wrapper = new ViewerFilterWrapper(filter);
        fTreeViewer.addFilter(wrapper);
        fTimeGraphViewer.addFilter(wrapper);
        fViewerFilterMap.put(filter, wrapper);
        alignTreeItems(true);
    }

    /**
     * @param filter The filter object to be removed from the view
     * @since 2.0
     */
    public void removeFilter(ViewerFilter filter) {
        ViewerFilter wrapper = fViewerFilterMap.get(filter);
        fTreeViewer.removeFilter(wrapper);
        fTimeGraphViewer.removeFilter(wrapper);
        fViewerFilterMap.remove(filter);
        alignTreeItems(true);
    }

    /**
     * Refreshes this time graph completely with information freshly obtained from its model.
     */
    public void refresh() {
        fInhibitTreeSelection = true;
        Tree tree = fTreeViewer.getTree();
        tree.setRedraw(false);
        fTreeViewer.refresh();
        fTreeViewer.expandAll();
        tree.setRedraw(true);
        fTimeGraphViewer.refresh();
        alignTreeItems(true);
        fInhibitTreeSelection = false;
    }

    /**
     * Adds a listener for selection changes in this time graph combo.
     *
     * @param listener a selection listener
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
     * @param listener a selection changed listener
     */
    public void removeSelectionListener(ITimeGraphSelectionListener listener) {
        SelectionListenerWrapper listenerWrapper = fSelectionListenerMap.remove(listener);
        fTreeViewer.removeSelectionChangedListener(listenerWrapper);
        fTimeGraphViewer.removeSelectionListener(listenerWrapper);
    }

    /**
     * Sets the current selection for this time graph combo.
     *
     * @param selection the new selection
     */
    public void setSelection(ITimeGraphEntry selection) {
        fTimeGraphViewer.setSelection(selection);
        fInhibitTreeSelection = true; // block the tree selection changed listener
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
     * Sets the auto-expand level to be used when the input of the viewer is set
     * using {@link #setInput(Object)}. The value 0 means that there is no
     * auto-expand; 1 means that top-level elements are expanded, but not their
     * children; 2 means that top-level elements are expanded, and their
     * children, but not grand-children; and so on.
     * <p>
     * The value {@link #ALL_LEVELS} means that all subtrees should be expanded.
     * </p>
     * @param level
     *            non-negative level, or <code>ALL_LEVELS</code> to expand all
     *            levels of the tree
     * @since 3.1
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
     * @since 3.1
     */
    public int getAutoExpandLevel() {
        return fTimeGraphViewer.getAutoExpandLevel();
    }

    /**
     * Set the expanded state of an entry
     *
     * @param entry
     *            The entry to expand/collapse
     * @param expanded
     *            True for expanded, false for collapsed
     *
     * @since 2.0
     */
    public void setExpandedState(ITimeGraphEntry entry, boolean expanded) {
        fTimeGraphViewer.setExpandedState(entry, expanded);
        fTreeViewer.setExpandedState(entry, expanded);
        alignTreeItems(true);
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void collapseAll() {
        fTimeGraphViewer.collapseAll();
        fTreeViewer.collapseAll();
        alignTreeItems(true);
    }

    /**
     * Expands all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
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
            ArrayList<TreeItem> items = new ArrayList<>();
            for (TreeItem item : tree.getItems()) {
                if (item.getData() == FILLER) {
                    break;
                }
                items.add(item);
                if (item.getExpanded()) {
                    addVisibleExpandedItems(items, item);
                }
            }
            fVisibleExpandedItems = items;
        }
        return fVisibleExpandedItems;
    }

    private void addVisibleExpandedItems(List<TreeItem> items, TreeItem treeItem) {
        for (TreeItem item : treeItem.getItems()) {
            items.add(item);
            if (item.getExpanded()) {
                addVisibleExpandedItems(items, item);
            }
        }
    }

    /**
     * Explores the list of top-level inputs and returns all the inputs
     *
     * @param inputs The top-level inputs
     * @return All the inputs
     */
    private List<? extends ITimeGraphEntry> listAllInputs(List<? extends ITimeGraphEntry> inputs) {
        ArrayList<ITimeGraphEntry> items = new ArrayList<>();
        for (ITimeGraphEntry entry : inputs) {
            items.add(entry);
            if (entry.hasChildren()) {
                items.addAll(listAllInputs(entry.getChildren()));
            }
        }
        return items;
    }

    private int getItemHeight(final Tree tree) {
        /*
         * Bug in Linux.  The method getItemHeight doesn't always return the correct value.
         */
        if (fLinuxItemHeight >= 0 && System.getProperty("os.name").contains("Linux")) { //$NON-NLS-1$ //$NON-NLS-2$
            if (fLinuxItemHeight != 0) {
                return fLinuxItemHeight;
            }
            List<TreeItem> treeItems = getVisibleExpandedItems(tree, true);
            if (treeItems.size() > 1) {
                final TreeItem treeItem0 = treeItems.get(0);
                final TreeItem treeItem1 = treeItems.get(1);
                PaintListener paintListener = new PaintListener() {
                    @Override
                    public void paintControl(PaintEvent e) {
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
            fLinuxItemHeight = -1; // Not Linux, don't perform os.name check anymore
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

        // ensure the time graph item heights are equal to the tree item heights
        int treeHeight = fTreeViewer.getTree().getBounds().height;
        int index = topIndex;
        Rectangle bounds = item.getBounds();
        while (index < treeItems.size() - 1) {
            if (bounds.y > treeHeight) {
                break;
            }
            /*
             * Bug in Linux. The method getBounds doesn't always return the correct height.
             * Use the difference of y position between items to calculate the height.
             */
            TreeItem nextItem = treeItems.get(index + 1);
            Rectangle nextBounds = nextItem.getBounds();
            Integer itemHeight = nextBounds.y - bounds.y;
            if (itemHeight > 0 && !itemHeight.equals(item.getData(ITEM_HEIGHT))) {
                ITimeGraphEntry entry = (ITimeGraphEntry) item.getData();
                if (fTimeGraphViewer.getTimeGraphControl().setItemHeight(entry, itemHeight)) {
                    item.setData(ITEM_HEIGHT, itemHeight);
                }
            }
            index++;
            item = nextItem;
            bounds = nextBounds;
        }
    }

}
