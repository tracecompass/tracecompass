/*******************************************************************************
 * Copyright (c) 2010, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Xavier Raynaud - add cut/copy/paste/dnd support
 *   Vincent Perot - Add subfield filtering
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.filter;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfEventFieldAspect;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAspectNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode.Type;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTraceTypeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

class FilterViewer extends Composite {

    private static class TraceTypeItem {
        private final String fLabel;
        private final String fTraceTypeId;

        public TraceTypeItem(String label, String traceTypeId) {
            fLabel = label;
            fTraceTypeId = traceTypeId;
        }

        public String getLabel() {
            return fLabel;
        }

        public String getTraceTypeId() {
            return fTraceTypeId;
        }
    }

    private static class AspectItem extends TraceTypeItem {
        private ITmfEventAspect<?> fEventAspect;

        public AspectItem(String label) {
            this(label, null, null);
        }

        public AspectItem(String label, ITmfEventAspect<?> eventAspect, String traceTypeId) {
            super(label, traceTypeId);
            fEventAspect = eventAspect;
        }

        public ITmfEventAspect<?> getEventAspect() {
            return fEventAspect;
        }

        public void setEventAspect(ITmfEventAspect<?> eventAspect) {
            fEventAspect = eventAspect;
        }

    }
    private TreeViewer fViewer;

    private Composite fComposite;
    private MenuManager fMenuManager;

    private boolean fIsDialog = false;

    public FilterViewer(Composite parent, int style) {
        this(parent, style, false);
    }

    public FilterViewer(Composite parent, int style, boolean isDialog) {
        super(parent, style);

        this.fIsDialog = isDialog;

        setLayout(new FillLayout());
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayoutData(gd);

        final SashForm sash = new SashForm(this, SWT.HORIZONTAL);

        // Create the tree viewer to display the filter tree
        fViewer = new TreeViewer(sash, SWT.NONE);
        fViewer.setContentProvider(new FilterTreeContentProvider());
        fViewer.setLabelProvider(new FilterTreeLabelProvider());

        // Create the empty filter node properties panel
        fComposite = new Composite(sash, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        fComposite.setLayout(gl);

        createContextMenu();

        fViewer.addSelectionChangedListener(event -> {
            if (!(event.getSelection().isEmpty()) && event.getSelection() instanceof IStructuredSelection) {
                // Update the filter node properties panel to the selection
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                ITmfFilterTreeNode node = (ITmfFilterTreeNode) selection.getFirstElement();
                updateFilterNodeComposite(node);
                // Highlight the selection's children
                highlightTreeItems(fViewer.getTree().getSelection()[0].getItems());
            } else {
                updateFilterNodeComposite(null);
            }
        });

        fViewer.getTree().addPaintListener(e -> {
            TmfFilterTreeNode root = (TmfFilterTreeNode) fViewer.getInput();
            if (root == null || root.getChildrenCount() == 0) {
                e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                e.gc.drawText(Messages.FilterViewer_EmptyTreeHintText, 5, 0);
            }
        });

        int operations = DND.DROP_MOVE | DND.DROP_COPY;
        DragSource dragSource = new org.eclipse.swt.dnd.DragSource(fViewer.getTree(), operations);
        dragSource.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
        dragSource.addDragListener(new FilterDragSourceAdapter(this));
        DropTarget dropTarget = new DropTarget(fViewer.getTree(), operations);
        dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
        dropTarget.addDropListener(new FilterDropTargetAdapter(this));
    }

    /**
     * Create the context menu for the tree viewer
     */
    private void createContextMenu() {
        // Adds root context menu
        fMenuManager = new MenuManager();
        fMenuManager.setRemoveAllWhenShown(true);
        fMenuManager.addMenuListener(manager -> fillContextMenu(manager));

        // Context
        Menu contextMenu = fMenuManager.createContextMenu(fViewer.getTree());

        // Publish it
        fViewer.getTree().setMenu(contextMenu);
    }

    public MenuManager getMenuManager() {
        return fMenuManager;
    }

    /**
     * Fill the context menu for the tree viewer.
     *
     * @param manager
     *            The menu manager
     */
    protected void fillContextMenu(IMenuManager manager) {
        final ISelection selection = fViewer.getSelection();
        ITmfFilterTreeNode filterTreeNode = null;
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            if (element instanceof ITmfFilterTreeNode) {
                filterTreeNode = (ITmfFilterTreeNode) element;
            }
        }

        final ITmfFilterTreeNode selectedNode = filterTreeNode;
        if (selectedNode != null) {
            fillContextMenuForNode(selectedNode, manager);
        }

        manager.add(new Separator("delete")); //$NON-NLS-1$

        if (fIsDialog && (selectedNode != null)) {
            Action deleteAction = new Action(Messages.FilterViewer_DeleteActionText) {
                @Override
                public void run() {
                    selectedNode.remove();
                    fViewer.refresh();
                }
            };
            deleteAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
            manager.add(deleteAction);
        }
        manager.add(new Separator("edit")); //$NON-NLS-1$

        if (fViewer.getInput() instanceof TmfFilterRootNode || filterTreeNode == null) {
            manager.add(new Separator());
            ITmfFilterTreeNode root = (ITmfFilterTreeNode) fViewer.getInput();
            fillContextMenuForNode(root, manager);
        }
    }

    /**
     * Fill the context menu with the valid children of the provided node
     *
     * @param node
     *            The target node
     * @param manager
     *            The menu manager
     */
    protected void fillContextMenuForNode(final ITmfFilterTreeNode node, IMenuManager manager) {
        for (final String child : node.getValidChildren()) {
            final Action action = new Action() {
                @Override
                public void run() {
                    ITmfFilterTreeNode newNode = null;
                    if (TmfFilterNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterNode(node, ""); //$NON-NLS-1$
                    } else if (TmfFilterTraceTypeNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterTraceTypeNode(node);
                    } else if (TmfFilterAndNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterAndNode(node);
                    } else if (TmfFilterOrNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterOrNode(node);
                    } else if (TmfFilterContainsNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterContainsNode(node);
                    } else if (TmfFilterEqualsNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterEqualsNode(node);
                    } else if (TmfFilterMatchesNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterMatchesNode(node);
                    } else if (TmfFilterCompareNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterCompareNode(node);
                    }
                    if (newNode != null) {
                        fViewer.refresh();
                        fViewer.setSelection(new StructuredSelection(newNode), true);
                    }
                }
            };
            if (TmfFilterNode.NODE_NAME.equals(child)) {
                action.setText(Messages.FilterViewer_NewPrefix + " " + child); //$NON-NLS-1$
            } else {
                action.setText(child);
            }
            manager.add(action);
        }
    }

    /**
     * Create the appropriate filter node properties composite
     */
    private void updateFilterNodeComposite(ITmfFilterTreeNode node) {
        for (Control control : fComposite.getChildren()) {
            control.dispose();
        }

        if (node instanceof TmfFilterNode) {
            new FilterNodeComposite(fComposite, (TmfFilterNode) node);
        } else if (node instanceof TmfFilterTraceTypeNode) {
            new FilterTraceTypeNodeComposite(fComposite, (TmfFilterTraceTypeNode) node);
        } else if (node instanceof TmfFilterAndNode) {
            new FilterAndNodeComposite(fComposite, (TmfFilterAndNode) node);
        } else if (node instanceof TmfFilterOrNode) {
            new FilterOrNodeComposite(fComposite, (TmfFilterOrNode) node);
        } else if (node instanceof TmfFilterContainsNode) {
            new FilterContainsNodeComposite(fComposite, (TmfFilterContainsNode) node);
        } else if (node instanceof TmfFilterEqualsNode) {
            new FilterEqualsNodeComposite(fComposite, (TmfFilterEqualsNode) node);
        } else if (node instanceof TmfFilterMatchesNode) {
            new FilterMatchesNodeComposite(fComposite, (TmfFilterMatchesNode) node);
        } else if (node instanceof TmfFilterCompareNode) {
            new FilterCompareNodeComposite(fComposite, (TmfFilterCompareNode) node);
        } else {
            new FilterBaseNodeComposite(fComposite);
        }
        fComposite.layout();
    }

    /**
     * Highlight the provided tree items
     */
    private void highlightTreeItems(TreeItem[] items) {
        resetTreeItems(fViewer.getTree().getItems());
        for (TreeItem item : items) {
            item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
        }

    }

    /**
     * Reset the provided tree items (remove highlight)
     */
    private void resetTreeItems(TreeItem[] items) {
        for (TreeItem item : items) {
            item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
            resetTreeItems(item.getItems());
        }
    }

    public void setInput(ITmfFilterTreeNode root) {
        fViewer.setInput(root);
        fViewer.expandAll();

        updateFilterNodeComposite(null);
    }

    public ITmfFilterTreeNode getInput() {
        return (ITmfFilterTreeNode) fViewer.getInput();
    }

    public void refresh() {
        fViewer.refresh();
    }

    public void setSelection(ITmfFilterTreeNode node, boolean reveal) {
        fViewer.setSelection(new StructuredSelection(node), reveal);
    }

    public void setSelection(ITmfFilterTreeNode node) {
        fViewer.setSelection(new StructuredSelection(node));
    }

    public ITmfFilterTreeNode getSelection() {
        final ISelection selection = fViewer.getSelection();
        ITmfFilterTreeNode filterTreeNode = null;
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            if (element instanceof ITmfFilterTreeNode) {
                filterTreeNode = (ITmfFilterTreeNode) element;
            }
        }

        final ITmfFilterTreeNode selectedNode = filterTreeNode;
        return selectedNode;
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        fViewer.addSelectionChangedListener(listener);
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        fViewer.removeSelectionChangedListener(listener);
    }

    @Override
    public boolean setFocus() {
        return fViewer.getControl().setFocus();
    }

    /**
     * @return whether the tree is in focus or not
     */
    public boolean isTreeInFocus() {
        return fViewer.getControl().isFocusControl();
    }

    /**
     * Gets the TreeViewer displaying filters
     *
     * @return a {@link TreeViewer}
     */
    TreeViewer getTreeViewer() {
        return fViewer;
    }

    private class FilterBaseNodeComposite extends Composite {

        FilterBaseNodeComposite(Composite parent) {
            super(parent, SWT.NONE);
            setLayout(new GridLayout(2, false));
            setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        }

        protected Map<String, TraceTypeHelper> getTraceTypeMap(String selected) {
            Map<String, TraceTypeHelper> traceTypeMap = new TreeMap<>();
            for (TraceTypeHelper helper : TmfTraceType.getTraceTypeHelpers()) {
                if (!helper.isExperimentType() && (helper.isEnabled() || (helper.getTraceTypeId().equals(selected)))) {
                    traceTypeMap.put(helper.getLabel(), helper);
                }
            }
            return traceTypeMap;
        }
    }

    private abstract class FilterAspectNodeComposite extends FilterBaseNodeComposite {
        TmfFilterAspectNode fAspectNode;
        Combo fTraceTypeCombo;
        Combo fAspectCombo;
        Label fFieldLabel;
        Text fFieldText;
        List<AspectItem> fAspectList = null;

        FilterAspectNodeComposite(Composite parent, TmfFilterAspectNode node) {
            super(parent);
            fAspectNode = node;
        }

        protected void createAspectControls() {
            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_TypeLabel);

            final List<TraceTypeItem> traceTypeList = getTraceTypeList(fAspectNode, fAspectNode.getTraceTypeId());

            fTraceTypeCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            fTraceTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            for (TraceTypeItem traceType : traceTypeList) {
                fTraceTypeCombo.add(traceType.getLabel());
            }
            if (fAspectNode.getTraceTypeId() == null) {
                fTraceTypeCombo.select(0);
                fAspectNode.setTraceTypeId(traceTypeList.get(0).getTraceTypeId());
            } else {
                for (int i = 0; i < traceTypeList.size(); i++) {
                    TraceTypeItem traceType = traceTypeList.get(i);
                    if (fAspectNode.getTraceTypeId().equals(traceType.getTraceTypeId())) {
                        fTraceTypeCombo.select(i);
                        break;
                    }
                }
            }
            fTraceTypeCombo.addModifyListener(e -> {
                TraceTypeItem traceType = traceTypeList.get(fTraceTypeCombo.getSelectionIndex());
                fAspectNode.setTraceTypeId(traceType.getTraceTypeId());
                fAspectList = getAspectList(fAspectNode.getTraceTypeId());
                String text = fAspectCombo.getText();
                fAspectCombo.removeAll();
                for (AspectItem aspect : fAspectList) {
                    fAspectCombo.add(aspect.getLabel());
                }
                int index = Arrays.asList(fAspectCombo.getItems()).indexOf(text);
                if (index >= 0 && !text.isEmpty()) {
                    fAspectCombo.select(index);
                }
                fViewer.refresh(fAspectNode);
            });

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_AspectLabel);

            fAspectList = getAspectList(fAspectNode.getTraceTypeId());

            fAspectCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            fAspectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            for (AspectItem aspect : fAspectList) {
                fAspectCombo.add(aspect.getLabel());
            }
            if (fAspectNode.getEventAspect() != null) {
                for (int i = 0; i < fAspectList.size(); i++) {
                    AspectItem aspect = fAspectList.get(i);
                    if (aspect.getEventAspect() != null &&
                            fAspectNode.getEventAspect().getName().equals(aspect.getEventAspect().getName()) &&
                            (fAspectNode.getTraceTypeId().equals(aspect.getTraceTypeId()) ||
                                    fAspectNode.getTraceTypeId().equals(TmfFilterAspectNode.BASE_ASPECT_ID))) {
                        fAspectCombo.select(i);
                        if (fAspectNode.getEventAspect() instanceof TmfEventFieldAspect) {
                            aspect.setEventAspect(fAspectNode.getEventAspect());
                            createFieldControls((TmfEventFieldAspect) fAspectNode.getEventAspect(), aspect);
                        }
                        break;
                    }
                }
            }
            fAspectCombo.addModifyListener(e -> {
                int selection = fAspectCombo.getSelectionIndex();
                AspectItem aspect = null;
                if (selection != -1) {
                    aspect = fAspectList.get(fAspectCombo.getSelectionIndex());
                    fAspectNode.setEventAspect(aspect.getEventAspect());
                } else {
                    fAspectNode.setEventAspect(null);
                }
                if (fAspectNode.getEventAspect() instanceof TmfEventFieldAspect) {
                    TmfEventFieldAspect eventFieldAspect = (TmfEventFieldAspect) fAspectNode.getEventAspect();
                    createFieldControls(eventFieldAspect, aspect);
                    layout();
                } else if (fFieldLabel != null && fFieldText != null) {
                    fFieldLabel.dispose();
                    fFieldLabel = null;
                    fFieldText.dispose();
                    fFieldText = null;
                    layout();
                }
                fViewer.refresh(fAspectNode);
            });
        }

        private void createFieldControls(final TmfEventFieldAspect eventFieldAspect, final AspectItem aspect) {
            if (fFieldLabel != null) {
                fFieldLabel.dispose();
            }
            fFieldLabel = new Label(this, SWT.NONE);
            fFieldLabel.moveBelow(fAspectCombo);
            fFieldLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fFieldLabel.setText(Messages.FilterViewer_FieldLabel);

            if (fFieldText != null) {
                fFieldText.dispose();
            }
            fFieldText = new Text(this, SWT.BORDER);
            fFieldText.moveBelow(fFieldLabel);
            fFieldText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            fFieldText.setToolTipText(Messages.FilterViewer_Subfield_ToolTip);
            if (eventFieldAspect.getFieldPath() != null) {
                fFieldText.setText(eventFieldAspect.getFieldPath());
            }
            if (fFieldText.getText().length() == 0) {
                fFieldText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                fFieldText.setText(Messages.FilterViewer_FieldHint);
            }
            fFieldText.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (fFieldText.getText().length() == 0) {
                        fFieldText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                        fFieldText.setText(Messages.FilterViewer_FieldHint);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (fFieldText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        fFieldText.setText(""); //$NON-NLS-1$
                    }
                    fFieldText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                }
            });
            fFieldText.addModifyListener(e -> {
                if (!fFieldText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                    if (fFieldText.getText().isEmpty()) {
                        fAspectNode.setEventAspect(eventFieldAspect.forField(null));
                    } else {
                        fAspectNode.setEventAspect(eventFieldAspect.forField(fFieldText.getText()));
                    }
                    aspect.setEventAspect(fAspectNode.getEventAspect());
                    fViewer.refresh(fAspectNode);
                }
            });
        }

        private List<TraceTypeItem> getTraceTypeList(ITmfFilterTreeNode node, String selected) {
            ArrayList<TraceTypeItem> traceTypeList = new ArrayList<>();
            traceTypeList.add(new TraceTypeItem(Messages.FilterViewer_CommonCategory, TmfFilterAspectNode.BASE_ASPECT_ID));

            ITmfFilterTreeNode curNode = node;
            while (curNode != null) {
                if (curNode instanceof TmfFilterTraceTypeNode) {
                    TmfFilterTraceTypeNode traceTypeNode = (TmfFilterTraceTypeNode) curNode;
                    TraceTypeHelper helper = TmfTraceType.getTraceType(traceTypeNode.getTraceTypeId());
                    if (helper != null) {
                        traceTypeList.set(0, new TraceTypeItem(helper.getLabel(), helper.getTraceTypeId()));
                    }
                    return traceTypeList;
                }
                curNode = curNode.getParent();
            }

            for (TraceTypeHelper helper : getTraceTypeMap(selected).values()) {
                traceTypeList.add(new TraceTypeItem(helper.getLabel(), helper.getTraceTypeId()));
            }
            return traceTypeList;
        }

        private List<AspectItem> getAspectList(String traceTypeId) {
            ArrayList<AspectItem> aspectList = new ArrayList<>();

            aspectList.add(new AspectItem(Messages.FilterViewer_CommonCategory));
            for (ITmfEventAspect<?> aspect : TmfBaseAspects.getBaseAspects()) {
                aspectList.add(new AspectItem(aspect.getName(), aspect, TmfFilterAspectNode.BASE_ASPECT_ID));
            }

            TraceTypeHelper helper = TmfTraceType.getTraceType(traceTypeId);
            if (helper != null) {
                aspectList.add(new AspectItem("")); //$NON-NLS-1$
                aspectList.add(new AspectItem('[' + helper.getLabel() + ']'));
                for (ITmfEventAspect<?> aspect : helper.getTrace().getEventAspects()) {
                    for (AspectItem baseAspect : aspectList) {
                        if (aspect.equals(baseAspect.getEventAspect())) {
                            aspectList.remove(baseAspect);
                            break;
                        }
                    }
                    aspectList.add(new AspectItem(aspect.getName(), aspect, helper.getTraceTypeId()));
                }
            }
            return aspectList;
        }
    }

    private class FilterNodeComposite extends FilterBaseNodeComposite {
        TmfFilterNode fNode;
        Text fNameText;

        FilterNodeComposite(Composite parent, TmfFilterNode node) {
            super(parent);
            fNode = node;

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_NameLabel);

            fNameText = new Text(this, SWT.BORDER);
            fNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            if (node.getFilterName() != null && node.getFilterName().length() > 0) {
                fNameText.setText(node.getFilterName());
            } else {
                fNameText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                fNameText.setText(Messages.FilterViewer_FilterNameHint);
            }
            fNameText.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (fNode.getFilterName() == null || fNode.getFilterName().length() == 0) {
                        fNameText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                        fNameText.setText(Messages.FilterViewer_FilterNameHint);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (fNameText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        fNameText.setText(""); //$NON-NLS-1$
                    }
                    fNameText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                }
            });
            fNameText.addModifyListener(e -> {
                if (!fNameText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                    fNode.setFilterName(fNameText.getText());
                    fViewer.refresh(fNode);
                }
            });
        }
    }

    private class FilterTraceTypeNodeComposite extends FilterBaseNodeComposite {
        TmfFilterTraceTypeNode fNode;
        Combo fTypeCombo;
        Map<String, TraceTypeHelper> fTraceTypeMap;

        FilterTraceTypeNodeComposite(Composite parent, TmfFilterTraceTypeNode node) {
            super(parent);
            fNode = node;
            fTraceTypeMap = getTraceTypeMap(fNode.getTraceTypeId());

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_TypeLabel);

            fTypeCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            fTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            fTypeCombo.setItems(fTraceTypeMap.keySet().toArray(new String[0]));
            if (fNode.getTraceTypeId() != null) {
                fTypeCombo.setText(fNode.getName());
            }
            fTypeCombo.addModifyListener(e -> {
                TraceTypeHelper helper = checkNotNull(fTraceTypeMap.get(fTypeCombo.getText()));
                fNode.setTraceTypeId(helper.getTraceTypeId());
                fNode.setTraceClass(helper.getTraceClass());
                fNode.setName(fTypeCombo.getText());
                fViewer.refresh(fNode);
            });
        }
    }

    private class FilterAndNodeComposite extends FilterBaseNodeComposite {
        TmfFilterAndNode fNode;
        Button fNotButton;

        FilterAndNodeComposite(Composite parent, TmfFilterAndNode node) {
            super(parent);
            fNode = node;

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_NotLabel);

            fNotButton = new Button(this, SWT.CHECK);
            fNotButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fNotButton.setSelection(fNode.isNot());
            fNotButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fNode.setNot(fNotButton.getSelection());
                    fViewer.refresh(fNode);
                }
            });
        }
    }

    private class FilterOrNodeComposite extends FilterBaseNodeComposite {
        TmfFilterOrNode fNode;
        Button fNotButton;

        FilterOrNodeComposite(Composite parent, TmfFilterOrNode node) {
            super(parent);
            fNode = node;

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_NotLabel);

            fNotButton = new Button(this, SWT.CHECK);
            fNotButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fNotButton.setSelection(fNode.isNot());
            fNotButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fNode.setNot(fNotButton.getSelection());
                    fViewer.refresh(fNode);
                }
            });
        }
    }

    private class FilterContainsNodeComposite extends FilterAspectNodeComposite {
        TmfFilterContainsNode fNode;
        Button fNotButton;
        Text fValueText;
        Button fIgnoreCaseButton;

        FilterContainsNodeComposite(Composite parent, TmfFilterContainsNode node) {
            super(parent, node);
            fNode = node;

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_NotLabel);

            fNotButton = new Button(this, SWT.CHECK);
            fNotButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fNotButton.setSelection(fNode.isNot());
            fNotButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fNode.setNot(fNotButton.getSelection());
                    fViewer.refresh(fNode);
                }
            });

            createAspectControls();

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_ValueLabel);

            fValueText = new Text(this, SWT.BORDER);
            fValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            if (node.getValue() != null && node.getValue().length() > 0) {
                fValueText.setText(node.getValue());
            } else {
                fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                fValueText.setText(Messages.FilterViewer_ValueHint);
            }
            fValueText.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (fNode.getValue() == null || fNode.getValue().length() == 0) {
                        fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                        fValueText.setText(Messages.FilterViewer_ValueHint);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (fValueText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        fValueText.setText(""); //$NON-NLS-1$
                    }
                    fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                }
            });
            fValueText.addModifyListener(e -> {
                if (!fValueText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                    fNode.setValue(fValueText.getText());
                    fViewer.refresh(fNode);
                }
            });

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

            fIgnoreCaseButton = new Button(this, SWT.CHECK);
            fIgnoreCaseButton.setSelection(fNode.isIgnoreCase());
            fIgnoreCaseButton.setText(Messages.FilterViewer_IgnoreCaseButtonText);
            fIgnoreCaseButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fIgnoreCaseButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fNode.setIgnoreCase(fIgnoreCaseButton.getSelection());
                    fViewer.refresh(fNode);
                }
            });
        }
    }

    private class FilterEqualsNodeComposite extends FilterAspectNodeComposite {
        TmfFilterEqualsNode fNode;
        Button fNotButton;
        Text fValueText;
        Button fIgnoreCaseButton;

        FilterEqualsNodeComposite(Composite parent, TmfFilterEqualsNode node) {
            super(parent, node);
            fNode = node;

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_NotLabel);

            fNotButton = new Button(this, SWT.CHECK);
            fNotButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fNotButton.setSelection(fNode.isNot());
            fNotButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fNode.setNot(fNotButton.getSelection());
                    fViewer.refresh(fNode);
                }
            });

            createAspectControls();

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_ValueLabel);

            fValueText = new Text(this, SWT.BORDER);
            fValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            if (node.getValue() != null && node.getValue().length() > 0) {
                fValueText.setText(node.getValue());
            } else {
                fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                fValueText.setText(Messages.FilterViewer_ValueHint);
            }
            fValueText.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (fNode.getValue() == null || fNode.getValue().length() == 0) {
                        fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                        fValueText.setText(Messages.FilterViewer_ValueHint);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (fValueText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        fValueText.setText(""); //$NON-NLS-1$
                    }
                    fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                }
            });
            fValueText.addModifyListener(e -> {
                if (!fValueText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                    fNode.setValue(fValueText.getText());
                    fViewer.refresh(fNode);
                }
            });

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

            fIgnoreCaseButton = new Button(this, SWT.CHECK);
            fIgnoreCaseButton.setSelection(fNode.isIgnoreCase());
            fIgnoreCaseButton.setText(Messages.FilterViewer_IgnoreCaseButtonText);
            fIgnoreCaseButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fIgnoreCaseButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fNode.setIgnoreCase(fIgnoreCaseButton.getSelection());
                    fViewer.refresh(fNode);
                }
            });
        }
    }

    private class FilterMatchesNodeComposite extends FilterAspectNodeComposite {
        TmfFilterMatchesNode fNode;
        Button fNotButton;
        Text fRegexText;

        FilterMatchesNodeComposite(Composite parent, TmfFilterMatchesNode node) {
            super(parent, node);
            fNode = node;

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_NotLabel);

            fNotButton = new Button(this, SWT.CHECK);
            fNotButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fNotButton.setSelection(fNode.isNot());
            fNotButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fNode.setNot(fNotButton.getSelection());
                    fViewer.refresh(fNode);
                }
            });

            createAspectControls();

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_RegexLabel);

            fRegexText = new Text(this, SWT.BORDER);
            fRegexText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            if (node.getRegex() != null && node.getRegex().length() > 0) {
                fRegexText.setText(node.getRegex());
            } else {
                fRegexText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                fRegexText.setText(Messages.FilterViewer_RegexHint);
            }
            fRegexText.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (fNode.getRegex() == null || fNode.getRegex().length() == 0) {
                        fRegexText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                        fRegexText.setText(Messages.FilterViewer_RegexHint);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (fRegexText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        fRegexText.setText(""); //$NON-NLS-1$
                    }
                    fRegexText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                }
            });
            fRegexText.addModifyListener(e -> {
                if (!fRegexText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                    fNode.setRegex(fRegexText.getText());
                    fViewer.refresh(fNode);
                }
            });
        }
    }

    private class FilterCompareNodeComposite extends FilterAspectNodeComposite {
        TmfFilterCompareNode fNode;
        Button fNotButton;
        Text fValueText;
        Button fLTButton;
        Button fEQButton;
        Button fGTButton;
        Button fNumButton;
        Button fAlphaButton;
        Button fTimestampButton;

        FilterCompareNodeComposite(Composite parent, TmfFilterCompareNode node) {
            super(parent, node);
            fNode = node;

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_NotLabel);

            fNotButton = new Button(this, SWT.CHECK);
            fNotButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fNotButton.setSelection(fNode.isNot());
            fNotButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fNode.setNot(fNotButton.getSelection());
                    fViewer.refresh(fNode);
                }
            });

            createAspectControls();

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_ResultLabel);

            Composite resultGroup = new Composite(this, SWT.NONE);
            GridLayout rggl = new GridLayout(3, true);
            rggl.marginHeight = 0;
            rggl.marginWidth = 0;
            resultGroup.setLayout(rggl);
            resultGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

            fLTButton = new Button(resultGroup, SWT.RADIO);
            fLTButton.setSelection(fNode.getResult() < 0);
            fLTButton.setText("<"); //$NON-NLS-1$
            fLTButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fLTButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fLTButton.getSelection()) {
                        fNode.setResult(-1);
                    }
                    fViewer.refresh(fNode);
                }
            });

            fEQButton = new Button(resultGroup, SWT.RADIO);
            fEQButton.setSelection(fNode.getResult() == 0);
            fEQButton.setText("="); //$NON-NLS-1$
            fEQButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fEQButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fEQButton.getSelection()) {
                        fNode.setResult(0);
                    }
                    fViewer.refresh(fNode);
                }
            });

            fGTButton = new Button(resultGroup, SWT.RADIO);
            fGTButton.setSelection(fNode.getResult() > 0);
            fGTButton.setText(">"); //$NON-NLS-1$
            fGTButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fGTButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fGTButton.getSelection()) {
                        fNode.setResult(1);
                    }
                    fViewer.refresh(fNode);
                }
            });

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_TypeLabel);

            Composite typeGroup = new Composite(this, SWT.NONE);
            GridLayout tggl = new GridLayout(3, false);
            tggl.marginHeight = 0;
            tggl.marginWidth = 0;
            typeGroup.setLayout(tggl);
            typeGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

            fNumButton = new Button(typeGroup, SWT.RADIO);
            fNumButton.setSelection(fNode.getType() == Type.NUM);
            fNumButton.setText(Messages.FilterViewer_NumButtonText);
            fNumButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fNumButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fNumButton.getSelection()) {
                        fNode.setType(Type.NUM);
                    }
                    fViewer.refresh(fNode);
                }
            });

            fAlphaButton = new Button(typeGroup, SWT.RADIO);
            fAlphaButton.setSelection(fNode.getType() == Type.ALPHA);
            fAlphaButton.setText(Messages.FilterViewer_AlphaButtonText);
            fAlphaButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fAlphaButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fAlphaButton.getSelection()) {
                        fNode.setType(Type.ALPHA);
                    }
                    fViewer.refresh(fNode);
                }
            });

            fTimestampButton = new Button(typeGroup, SWT.RADIO);
            fTimestampButton.setSelection(fNode.getType() == Type.TIMESTAMP);
            fTimestampButton.setText(Messages.FilterViewer_TimestampButtonText);
            fTimestampButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fTimestampButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fTimestampButton.getSelection()) {
                        fNode.setType(Type.TIMESTAMP);
                    }
                    fViewer.refresh(fNode);
                }
            });

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_ValueLabel);

            fValueText = new Text(this, SWT.BORDER);
            fValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            if (node.getValue() != null && node.getValue().length() > 0) {
                fValueText.setText(node.getValue());
            } else {
                fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                fValueText.setText(Messages.FilterViewer_ValueHint);
            }
            fValueText.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (fNode.getValue() == null || fNode.getValue().length() == 0) {
                        fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                        fValueText.setText(Messages.FilterViewer_ValueHint);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (fValueText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        fValueText.setText(""); //$NON-NLS-1$
                    }
                    fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                }
            });
            fValueText.addModifyListener(e -> {
                if (!fValueText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                    fNode.setValue(fValueText.getText());
                    fViewer.refresh(fNode);
                }
            });
        }
    }

}
