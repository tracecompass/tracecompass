/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class TimeGraphCombo extends Composite {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final Object FILLER = new Object();

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The tree viewer
    private TreeViewer fTreeViewer;

    // The time viewer
    private TimeGraphViewer fTimeGraphViewer;

    // The selection listener map
    private HashMap<ITimeGraphSelectionListener, SelectionListenerWrapper> fSelectionListenerMap = new HashMap<ITimeGraphSelectionListener, SelectionListenerWrapper>();

    // Flag to block the tree selection changed listener when triggered by the time graph combo
    private boolean fInhibitTreeSelection = false;

    // Number of filler rows used by the tree content provider
    private static int fNumFillerRows;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class TreeContentProviderWrapper implements ITreeContentProvider {
        private ITreeContentProvider contentProvider;

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
            Object[] oElements = Arrays.copyOf(elements, elements.length + fNumFillerRows, new Object[0].getClass());
            for (int i = 0; i < fNumFillerRows; i++) {
                oElements[elements.length + i] = FILLER;
            }
            return oElements;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ITimeGraphEntry) {
                return contentProvider.getChildren(parentElement);
            } else {
                return new Object[0];
            }
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof ITimeGraphEntry) {
                return contentProvider.getParent(element);
            } else {
                return null;
            }
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof ITimeGraphEntry) {
                return contentProvider.hasChildren(element);
            } else {
                return false;
            }
        }
    }

    private class TreeLabelProviderWrapper implements ITableLabelProvider {
        private ITableLabelProvider labelProvider;

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
            } else {
                return false;
            }
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            labelProvider.removeListener(listener);
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.getColumnImage(element, columnIndex);
            } else {
                return null;
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.getColumnText(element, columnIndex);
            } else {
                return null;
            }
        }

    }

    private class SelectionListenerWrapper implements ISelectionChangedListener, ITimeGraphSelectionListener {
        private ITimeGraphSelectionListener listener;
        private ITimeGraphEntry selection = null;

        public SelectionListenerWrapper(ITimeGraphSelectionListener listener) {
            this.listener = listener;
        }

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (fInhibitTreeSelection) {
                return;
            }
            ITimeGraphEntry entry = (ITimeGraphEntry) ((IStructuredSelection) event.getSelection()).getFirstElement();
            if (entry != selection) {
                selection = entry;
                listener.selectionChanged(new TimeGraphSelectionEvent(event.getSource(), selection));
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

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TimeGraphCombo(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());

        final SashForm sash = new SashForm(this, SWT.NONE);

        fTreeViewer = new TreeViewer(sash, SWT.FULL_SELECTION | SWT.H_SCROLL);
        final Tree tree = fTreeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        fTimeGraphViewer = new TimeGraphViewer(sash, SWT.NONE);
        fTimeGraphViewer.setItemHeight(tree.getItemHeight() + getTreeItemHeightAdjustement());
        fTimeGraphViewer.setHeaderHeight(tree.getHeaderHeight());
        fTimeGraphViewer.setBorderWidth(tree.getBorderWidth());
        fTimeGraphViewer.setNameWidthPref(0);

        fTreeViewer.addTreeListener(new ITreeViewerListener() {
            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                fTimeGraphViewer.setExpandedState((ITimeGraphEntry) event.getElement(), false);
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                fTimeGraphViewer.setExpandedState((ITimeGraphEntry) event.getElement(), true);
            }
        });

        fTimeGraphViewer.addTreeListener(new ITimeGraphTreeListener() {
            @Override
            public void treeCollapsed(TimeGraphTreeExpansionEvent event) {
                fTreeViewer.setExpandedState(event.getEntry(), false);
            }

            @Override
            public void treeExpanded(TimeGraphTreeExpansionEvent event) {
                fTreeViewer.setExpandedState(event.getEntry(), true);
            }
        });

        // prevent mouse button from selecting a filler tree item
        tree.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                TreeItem treeItem = tree.getItem(new Point(event.x, event.y));
                if (treeItem == null || treeItem.getData() == FILLER) {
                    event.doit = false;
                    ArrayList<TreeItem> treeItems = getVisibleExpandedItems(tree);
                    if (treeItems.size() == 0) {
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

        tree.addListener(SWT.MouseWheel, new Listener() {
            @Override
            public void handleEvent(Event event) {
                event.doit = false;
                Slider scrollBar = fTimeGraphViewer.getVerticalBar();
                fTimeGraphViewer.setTopIndex(scrollBar.getSelection() - event.count);
                ArrayList<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
            }
        });

        // prevent key stroke from selecting a filler tree item
        tree.addListener(SWT.KeyDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ArrayList<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                if (event.keyCode == SWT.ARROW_DOWN) {
                    int index = Math.min(fTimeGraphViewer.getSelectionIndex() + 1, treeItems.size() - 1);
                    fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(index).getData());
                    event.doit = false;
                } else if (event.keyCode == SWT.PAGE_DOWN) {
                    int height = tree.getSize().y - tree.getHeaderHeight() - tree.getHorizontalBar().getSize().y;
                    int countPerPage = height / (tree.getItemHeight() + getTreeItemHeightAdjustement());
                    int index = Math.min(fTimeGraphViewer.getSelectionIndex() + countPerPage - 1, treeItems.size() - 1);
                    fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(index).getData());
                    event.doit = false;
                } else if (event.keyCode == SWT.END) {
                    fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(treeItems.size() - 1).getData());
                    event.doit = false;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
                if (fTimeGraphViewer.getSelectionIndex() >= 0) {
                    fTreeViewer.setSelection(new StructuredSelection(fTimeGraphViewer.getSelection()));
                } else {
                    fTreeViewer.setSelection(new StructuredSelection());
                }
            }
        });

        fTimeGraphViewer.getTimeGraphControl().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                ArrayList<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
            }
        });

        fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (fInhibitTreeSelection) {
                    return;
                }
                if (event.getSelection() instanceof IStructuredSelection) {
                    Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
                    ArrayList<TreeItem> treeItems = getVisibleExpandedItems(tree);
                    if (selection instanceof ITimeGraphEntry) {
                        fTimeGraphViewer.setSelection((ITimeGraphEntry) selection);
                    }
                    TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                    tree.setTopItem(treeItem);
                }
            }
        });

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
                ArrayList<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
            }
        });

        fTimeGraphViewer.getVerticalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ArrayList<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
            }
        });

        fNumFillerRows = Display.getDefault().getBounds().height / (tree.getItemHeight() + getTreeItemHeightAdjustement());

        sash.setWeights(new int[] { 1, 1 });
    }

    private ArrayList<TreeItem> getVisibleExpandedItems(Tree tree) {
        ArrayList<TreeItem> items = new ArrayList<TreeItem>();
        for (TreeItem item : tree.getItems()) {
            if (item.getData() == FILLER) {
                break;
            }
            items.add(item);
            if (item.getExpanded()) {
                items.addAll(getVisibleExpandedItems(item));
            }
        }
        return items;
    }

    private ArrayList<TreeItem> getVisibleExpandedItems(TreeItem treeItem) {
        ArrayList<TreeItem> items = new ArrayList<TreeItem>();
        for (TreeItem item : treeItem.getItems()) {
            items.add(item);
            if (item.getExpanded()) {
                items.addAll(getVisibleExpandedItems(item));
            }
        }
        return items;
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

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    /*
     * SWT doesn't seem to report correctly the tree item height, at least in
     * the case of KDE.
     * 
     * This method provides an adjustment term according to the desktop session.
     * 
     * @return Height adjustment 
     */
    private int getTreeItemHeightAdjustement() {
        int ajustement = 0;
        String desktopSession = System.getenv("DESKTOP_SESSION"); //$NON-NLS-1$

        if (desktopSession != null) {
            if (desktopSession.equals("kde")) { //$NON-NLS-1$
                ajustement = 2;
            }
        }

        return ajustement;
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
     * @param treeLabelProvider the tree label provider
     */
    public void setTreeLabelProvider(ITableLabelProvider labelProvider) {
        fTreeViewer.setLabelProvider(new TreeLabelProviderWrapper(labelProvider));
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
     * Sets the time graph provider used by this time graph combo.
     * 
     * @param timeGraphProvider the time graph provider
     */
    public void setTimeGraphProvider(ITimeGraphProvider timeGraphProvider) {
        fTimeGraphViewer.setTimeGraphProvider(timeGraphProvider);
    }

    /**
     * Sets or clears the input for this time graph combo.
     *
     * @param input the input of this time graph combo, or <code>null</code> if none
     */
    public void setInput(ITimeGraphEntry[] input) {
        fTreeViewer.setInput(input);
        fTreeViewer.expandAll();
        fTreeViewer.getTree().getVerticalBar().setEnabled(false);
        fTimeGraphViewer.setInput(input);
    }

    /**
     * Refreshes this time graph completely with information freshly obtained from its model.
     */
    public void refresh() {
        fTreeViewer.refresh();
        fTimeGraphViewer.refresh();
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
}
