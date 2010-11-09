/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.experiment.StateManagerFactory;
import org.eclipse.linuxtools.lttng.trace.LTTngExperiment;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.lttng.ui.views.project.model.ILTTngProjectTreeNode;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentNode;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProjectContentProvider;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProjectLabelProvider;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProjectRoot;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngTraceNode;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentSelectedSignal;
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
 */
public class ProjectView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.project"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Main data structures
    // ------------------------------------------------------------------------

    private TreeViewer fViewer;
    private LTTngProjectRoot fProjectRoot;
    private LTTngExperiment<LttngEvent> fSelectedExperiment = null;

    private IWorkspace fWorkspace;
    private IResourceChangeListener fResourceChangeListener;

    // ------------------------------------------------------------------------
    // View refresher
    // ------------------------------------------------------------------------

    // Perform updates on the UI thread
    private Runnable fViewRefresher = new Runnable() {
    	@Override
		public void run() {
    		if ((fViewer != null) && (!fViewer.getTree().isDisposed())) {
    			Object[] elements = fViewer.getExpandedElements();
    			fViewer.refresh();
    			fViewer.setExpandedElements(elements);
    		}
    	}
    };

    public LTTngProjectRoot getRoot() {
    	return fProjectRoot;
    }
   
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

	public ProjectView() {
		
		super("ProjectView"); //$NON-NLS-1$
        fProjectRoot = new LTTngProjectRoot(this);

		fWorkspace = ResourcesPlugin.getWorkspace();
		fResourceChangeListener = new IResourceChangeListener() {
            @Override
			public void resourceChanged(IResourceChangeEvent event) {
                if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
                	fProjectRoot.refreshChildren();
                	refresh();
                }
            }            
        };
        fWorkspace.addResourceChangeListener(fResourceChangeListener);
	}

	public void refresh() {
       	Tree tree = fViewer.getTree();
       	if (tree != null && !tree.isDisposed())
       		tree.getDisplay().asyncExec(fViewRefresher);
	}

	public void setSelection(ILTTngProjectTreeNode node) {
		fViewer.setSelection(new StructuredSelection(node), true);
	}

    @Override
	public void dispose() {
    	fWorkspace.removeResourceChangeListener(fResourceChangeListener);
    }

	@Override
	public void createPartControl(Composite parent) {

		fViewer = new TreeViewer(parent, SWT.SINGLE);
        fViewer.setContentProvider(new LTTngProjectContentProvider());
        fViewer.setSorter(new ViewerSorter());
        fViewer.setLabelProvider(new LTTngProjectLabelProvider());
        fViewer.setInput(fProjectRoot);

        getSite().setSelectionProvider(fViewer);
        hookMouse();

        createContextMenu();
	}

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

	@Override
	@SuppressWarnings("nls")
	public String toString() {
		return "[ProjectView]";
	}

    // ------------------------------------------------------------------------
    // hookMouse
    // ------------------------------------------------------------------------

    private void hookMouse() {
        fViewer.getTree().addMouseListener(new MouseAdapter() {
        	@Override
			public void mouseDoubleClick(MouseEvent event) {
                TreeSelection selection = (TreeSelection) fViewer.getSelection();
                Object element = selection.getFirstElement();
                if (element instanceof LTTngExperimentNode) {
                	LTTngExperimentNode experiment = (LTTngExperimentNode) element;
                	selectExperiment(experiment);                
                } else {
                    if (element instanceof LTTngTraceNode) {
                        LTTngTraceNode trace = (LTTngTraceNode) element;
                        selectTrace(trace);
                    }
                }
            }
        });
    }

    private void selectTrace(LTTngTraceNode traceNode) {
        if (fSelectedExperiment != null) {
            fSelectedExperiment.dispose();
        }

        try {
            ITmfTrace[] traces = new ITmfTrace[1];
            IResource res = traceNode.getFolder();
            String location = res.getLocation().toOSString();
            ITmfTrace trace = new LTTngTrace(location, waitForCompletion);
            traces[0] = trace;
            fSelectedExperiment = new LTTngExperiment<LttngEvent>(LttngEvent.class, traceNode.getName(), traces);
            TmfExperiment.setCurrentExperiment(fSelectedExperiment);
            
            // Make sure the lttng-core, experiment selection context is ready
            // for an event request from any view
            StateManagerFactory.getExperimentManager().experimentSelected_prep(
                    (TmfExperiment<LttngEvent>) fSelectedExperiment);

            broadcast(new TmfExperimentSelectedSignal<LttngEvent>(this, fSelectedExperiment));
        } catch (FileNotFoundException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private boolean waitForCompletion = true;

	/**
	 * @param experiment
	 */
	public void selectExperiment(LTTngExperimentNode experiment) {
    	String expId = experiment.getName();
        if (fSelectedExperiment != null) {
//        	System.out.println(fSelectedExperiment.getName() + ": nbEvents=" + fSelectedExperiment.getNbEvents() + 
//        			", nbReads=" + ((LTTngTrace) fSelectedExperiment.getTraces()[0]).nbEventsRead);
        	fSelectedExperiment.dispose();
        }
        try {
        	LTTngTraceNode[] traceEntries = experiment.getTraces();
        	int nbTraces = traceEntries.length;
        	ITmfTrace[] traces = new ITmfTrace[nbTraces];
        	for (int i = 0; i < nbTraces; i++) {
        		IResource res = traceEntries[i].getFolder();
        		String location = res.getLocation().toOSString();
        		ITmfTrace trace = new LTTngTrace(location, waitForCompletion);
                traces[i] = trace;
        	}
            fSelectedExperiment = new LTTngExperiment<LttngEvent>(LttngEvent.class, expId, traces);
            TmfExperiment.setCurrentExperiment(fSelectedExperiment);
            
			// Make sure the lttng-core, experiment selection context is ready
			// for an event request from any view
			StateManagerFactory.getExperimentManager().experimentSelected_prep(
					(TmfExperiment<LttngEvent>) fSelectedExperiment);

//			System.out.println(System.currentTimeMillis() + ": Experiment selected");
            broadcast(new TmfExperimentSelectedSignal<LttngEvent>(this, fSelectedExperiment));
        } catch (FileNotFoundException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // createContextMenu
    // ------------------------------------------------------------------------

	// Populated from the plug-in
    private void createContextMenu() {
        MenuManager menuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuManager.setRemoveAllWhenShown(true);
        Menu menu = menuManager.createContextMenu(fViewer.getControl());
        fViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuManager, fViewer);
    }
			
}