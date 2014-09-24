/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yuriy Vashchuk - Initial API and implementation
 *   Xavier Raynaud - add cut/copy/paste/dnd support
 *   based on Francois Chouinard ProjectView code.
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.filter;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.linuxtools.tmf.core.filter.xml.TmfFilterXMLParser;
import org.eclipse.linuxtools.tmf.core.filter.xml.TmfFilterXMLWriter;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.xml.sax.SAXException;

/**
 * View that contain UI to the TMF filter.
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 */
public class FilterView extends TmfView {

    /** ID for the Filter view */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.ui.views.filter"; //$NON-NLS-1$

    private static final Image SAVE_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/save_button.gif"); //$NON-NLS-1$
    private static final Image ADD_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/add_button.gif"); //$NON-NLS-1$
    private static final Image IMPORT_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/import_button.gif"); //$NON-NLS-1$
    private static final Image EXPORT_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/export_button.gif"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Main data structures
    // ------------------------------------------------------------------------

    private FilterViewer fViewer;
    private final ITmfFilterTreeNode fRoot;

    private final IWorkspace fWorkspace;

    private SaveAction fSaveAction;
    private AddAction fAddAction;
    private ExportAction fExportAction;
    private ImportAction fImportAction;

    /**
     * Getter for the Filter Tree Root
     *
     * @return The root of builded tree
     */
    public ITmfFilterTreeNode getFilterRoot() {
        return fRoot;
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default Constructor
     */
    public FilterView() {
        super("Filter"); //$NON-NLS-1$

        fWorkspace = ResourcesPlugin.getWorkspace();
        try {
            fWorkspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            Activator.getDefault().logError("Error refreshing workspace", e); //$NON-NLS-1$
        }

        fRoot = new TmfFilterRootNode();
        for (ITmfFilterTreeNode node : FilterManager.getSavedFilters()) {
            fRoot.addChild(node);
        }
    }

    /**
     * Add a filter to the FilterView. This does not modify the XML, which must
     * be done manually. If the filter is already in the FilterView, this is a
     * no-op.
     *
     * @param filter
     *            The filter to add.
     * @since 3.1
     */
    public void addFilter(ITmfFilterTreeNode filter) {
        ITmfFilterTreeNode root = fViewer.getInput();
        for (ITmfFilterTreeNode node : root.getChildren()) {
            if (node.equals(filter)) {
                return;
            }
        }
        root.addChild(filter);
        fViewer.setInput(root);
    }

    /**
     * Refresh the tree widget
     */
    public void refresh() {
        fViewer.refresh();
    }

    /**
     * Setter for selection
     *
     * @param node
     *            The node to select
     */
    public void setSelection(ITmfFilterTreeNode node) {
        fViewer.setSelection(node, true);
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {

        fViewer = new FilterViewer(parent, SWT.NONE);
        fViewer.setInput(fRoot);

        contributeToActionBars();

        fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (!(event.getSelection().isEmpty()) && event.getSelection() instanceof IStructuredSelection) {
                    fExportAction.setEnabled(true);
                } else {
                    fExportAction.setEnabled(false);
                }
            }
        });
        this.getSite().setSelectionProvider(fViewer.getTreeViewer());

        MenuManager menuManager = fViewer.getMenuManager();
        this.getSite().registerContextMenu(menuManager, fViewer.getTreeViewer());
    }

    /**
     * @return the ITmfFilterTreeNode currently selected
     */
    ITmfFilterTreeNode getSelection() {
        return fViewer.getSelection();
    }

    @Override
    public void setFocus() {
        fViewer.setFocus();
    }

    @Override
    public String toString() {
        return "[FilterView]"; //$NON-NLS-1$
    }

    /**
     * Builds the menu toolbar
     */
    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        // fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    /**
     * Build the popup menu
     *
     * @param manager
     *            The manager to build
     */
    private void fillLocalToolBar(IToolBarManager manager) {

        fSaveAction = new SaveAction();
        fSaveAction.setImageDescriptor(ImageDescriptor.createFromImage(SAVE_IMAGE));
        fSaveAction.setToolTipText(Messages.FilterView_SaveActionToolTipText);

        fAddAction = new AddAction();
        fAddAction.setImageDescriptor(ImageDescriptor.createFromImage(ADD_IMAGE));
        fAddAction.setToolTipText(Messages.FilterView_AddActionToolTipText);

        fExportAction = new ExportAction();
        fExportAction.setImageDescriptor(ImageDescriptor.createFromImage(EXPORT_IMAGE));
        fExportAction.setToolTipText(Messages.FilterView_ExportActionToolTipText);

        fImportAction = new ImportAction();
        fImportAction.setImageDescriptor(ImageDescriptor.createFromImage(IMPORT_IMAGE));
        fImportAction.setToolTipText(Messages.FilterView_ImportActionToolTipText);

        manager.add(fSaveAction);
        manager.add(new Separator("add_delete")); //$NON-NLS-1$
        manager.add(fAddAction);
        manager.add(new Separator("edit")); //$NON-NLS-1$
        manager.add(new Separator());
        manager.add(fExportAction);
        manager.add(fImportAction);
    }

    private class SaveAction extends Action {
        @Override
        public void run() {
            FilterManager.setSavedFilters(fRoot.getChildren());
        }
    }

    private class AddAction extends Action {
        @Override
        public void run() {

            TmfFilterNode newNode = new TmfFilterNode(fRoot, ""); //$NON-NLS-1$
            refresh();
            setSelection(newNode);
        }
    }

    private class ExportAction extends Action {
        @Override
        public void run() {
            try {
                FileDialog dlg = new FileDialog(new Shell(), SWT.SAVE);
                dlg.setFilterNames(new String[] { Messages.FilterView_FileDialogFilterName + " (*.xml)" }); //$NON-NLS-1$
                dlg.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$

                String fn = dlg.open();
                if (fn != null) {
                    TmfFilterXMLWriter writerXML = new TmfFilterXMLWriter(fRoot);
                    writerXML.saveTree(fn);
                }

            } catch (ParserConfigurationException e) {
                Activator.getDefault().logError("Error parsing filter xml file", e); //$NON-NLS-1$
            }
        }
    }

    private class ImportAction extends Action {
        @Override
        public void run() {
            if (fViewer != null) {
                ITmfFilterTreeNode root = null;
                try {
                    FileDialog dlg = new FileDialog(new Shell(), SWT.OPEN);
                    dlg.setFilterNames(new String[] { Messages.FilterView_FileDialogFilterName + " (*.xml)" }); //$NON-NLS-1$
                    dlg.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$

                    TmfFilterXMLParser parserXML = null;
                    String fn = dlg.open();
                    if (fn != null) {
                        parserXML = new TmfFilterXMLParser(fn);
                        root = parserXML.getTree();
                    }

                } catch (SAXException e) {
                    Activator.getDefault().logError("Error importing filter xml file", e); //$NON-NLS-1$
                } catch (IOException e) {
                    Activator.getDefault().logError("Error importing filter xml file", e); //$NON-NLS-1$
                }

                if (root != null) {
                    for (ITmfFilterTreeNode node : root.getChildren()) {
                        if (node instanceof TmfFilterNode) {
                            fRoot.addChild(node);
                            refresh();
                            fViewer.setSelection(node);
                        }
                    }
                }
            }
        }
    }

}