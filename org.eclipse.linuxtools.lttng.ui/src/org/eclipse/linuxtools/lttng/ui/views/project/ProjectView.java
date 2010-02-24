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

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentEntry;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngTraceEntry;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

/**
 * <b><u>ProjectView</u></b>
 * <p>
 * The ProjectView keeps track of the LTTng projects in the workspace.
 *
 * TODO: Implement me. Please.
 * TODO: Put all actions in a context menu
 * TODO: Identify LTTng traces vs. experiments and hook doubleClick properly
 * TODO: Handle multiple traces
 */
public class ProjectView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.project";

    private final IWorkspace fWorkspace;
    private final IResourceChangeListener fResourceChangeListener;
    private TreeViewer fViewer;
    private TmfExperiment<LttngEvent> fExperiment = null;
//    private Object fSelection = null;

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

//		TmfTraceContext.init();

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

		fViewer = new TreeViewer(parent, SWT.SINGLE);
        fViewer.setLabelProvider(new LTTngProjectLabelProvider());
        fViewer.setContentProvider(new LTTngProjectContentProvider());
        fViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());

        hookMouse();
        hookDragAndDrop();
        createContextMenu();
	}

    /**
     * 
     */
    private void hookMouse() {
        fViewer.getTree().addMouseListener(new MouseAdapter() {
        	@Override
			public void mouseDoubleClick(MouseEvent event) {
                TreeSelection selection = (TreeSelection) fViewer.getSelection();
                Object element = selection.getFirstElement();
                if (element instanceof LTTngExperimentEntry) {
                	LTTngExperimentEntry experiment = (LTTngExperimentEntry) element;
                	selectExperiment(experiment);                
                }
            }
        });
    }

    private void hookDragAndDrop() {
    	new TraceDragSource(fViewer);
    	new TraceDropTarget(fViewer);
    }

    /**
     * @param experiment
     * 
     * TODO: Tie the proper parser to the trace 
     */
    // FIXME: Troubleshooting hack - start
	private boolean waitForCompletion = true;
    // FIXME: Troubleshooting hack - end

	private void selectExperiment(LTTngExperimentEntry experiment) {
    	String expId = experiment.getName();
        if (fExperiment != null)
        	fExperiment.deregister();
        try {
        	LTTngTraceEntry[] traceEntries = experiment.getTraces();
        	int nbTraces = traceEntries.length;
        	ITmfTrace[] traces = new ITmfTrace[nbTraces];
        	for (int i = 0; i < nbTraces; i++) {
        		IResource res = traceEntries[i].getResource();
        		String location = res.getLocation().toOSString();
        		ITmfTrace trace = new LTTngTrace(location, waitForCompletion);
                traces[i] = trace;
        	}
            fExperiment = new TmfExperiment<LttngEvent>(LttngEvent.class, expId, traces);
            fExperiment.indexExperiment(waitForCompletion);
            broadcast(new TmfExperimentSelectedSignal(this, fExperiment));
        } catch (FileNotFoundException e) {
        	// TODO: Why not tell the user? He would appreciate...
//            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[ProjectView]";
	}

}