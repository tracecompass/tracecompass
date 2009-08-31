/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.project;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.lttng.stubs.LTTngEventParserStub;
import org.eclipse.linuxtools.lttng.stubs.LTTngEventStreamStub;
import org.eclipse.linuxtools.lttng.ui.views.Labels;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.stream.ITmfEventParser;
import org.eclipse.linuxtools.tmf.stream.ITmfEventStream;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceSelectedSignal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;

/**
 * <b><u>ProjectView</u></b>
 * <p>
 * The ProjectView keeps track of the LTTng projects in the workspace.
 *
 * TODO: Implement me. Please.
 * TODO: Display only LTTng projects (nature)
 * TODO: Add context menu
 * TODO: Identify LTTng traces and hook doubleClick properly
 * TODO: Handle multiple traces
 */
@SuppressWarnings("restriction")
public class ProjectView extends ViewPart {

    public static final String ID = Labels.ProjectView_ID;

    private final IWorkspace fWorkspace;
    private final IResourceChangeListener fResourceChangeListener;
    private TreeViewer fViewer;

    // To perform updates on the UI thread
    private Runnable fViewRefresher = new Runnable() {
    	public void run() {
    		if (fViewer != null)
    			fViewer.refresh();
    	}
    };

    // ========================================================================
    // Constructor/Destructor
    // ========================================================================

    /**
	 * This view needs to react to workspace resource changes
	 */
	public ProjectView() {
        fWorkspace = ResourcesPlugin.getWorkspace();
        fResourceChangeListener = new IResourceChangeListener() {
            public void resourceChanged(IResourceChangeEvent event) {
                if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
                	Tree tree = fViewer.getTree();
                	if (tree != null && !tree.isDisposed())
                		tree.getDisplay().asyncExec(fViewRefresher);
                }
            }            
        };
        fWorkspace.addResourceChangeListener(fResourceChangeListener);
	}

    /**
     * 
     */
    @Override
	public void dispose() {
        fWorkspace.removeResourceChangeListener(fResourceChangeListener);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        fViewer = new TreeViewer(parent, SWT.SINGLE);
        fViewer.setContentProvider(new ProjectContentProvider());
        fViewer.setLabelProvider(new ProjectLabelProvider());
        fViewer.setInput(root);

        hookMouse();
        createContextMenu();
	}

    /**
     * TODO: Hook to LTTng trace instead of File (when available)
     */
    private void hookMouse() {
        fViewer.getTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent event) {
                TreeSelection selection = (TreeSelection) fViewer.getSelection();
                Object element = selection.getFirstElement();
//                if (element instanceof Folder) {
//                	openTraceFile((Folder) element);                
//                }
                if (element instanceof File) {
                    openTraceFile((File) element);                
                }
            }
        });
    }

    /**
     * @param trace
     * 
     * TODO: Handle LTTng Trace files (when available)
     * TODO: Handle multiple traces simultaneously
     */
    private void openTraceFile(File file) {
        String fname = Platform.getLocation() + file.getFullPath().toOSString();
        try {
            ITmfEventParser parser = new LTTngEventParserStub();
            ITmfEventStream stream = new LTTngEventStreamStub(fname, parser);
            TmfTrace trace = new TmfTrace(file.getName(), stream);
            TmfSignalManager.dispatchSignal(new TmfTraceSelectedSignal(this, trace));
            stream.indexStream(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     * @param trace
//     * 
//     * TODO: Handle LTTng Trace files (when available)
//     * TODO: Handle multiple traces simultaneously
//     */
//    private void openTraceFile(Folder trace) {
//        String fname = Platform.getLocation() + trace.getFullPath().toOSString();
//        try {
//            ITmfEventStream stream = new LttngEventStream(fname);
//            TmfTrace eventLog = new TmfTrace(trace.getName(), stream);
//            broadcastEvent(new TmfTraceSelectedEvent(eventLog));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public void handleEvent(ITmfEventLogEvent event) {
////    	System.out.println("ProjectView.handleEvent()");
//    }

    /**
     * 
     */
    private void createContextMenu() {
        MenuManager menuManager = new MenuManager("#PopupMenu");
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                ProjectView.this.fillContextMenu(manager);               
            }
        });

        Menu menu = menuManager.createContextMenu(fViewer.getControl());
        fViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuManager, fViewer);
    }

    /**
     * @param manager
     */
    private void fillContextMenu(IMenuManager manager) {
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
