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

package org.eclipse.linuxtools.tmf.ui.views.project;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.TmfUiPreferenceInitializer;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEditorInput;
import org.eclipse.linuxtools.tmf.ui.parsers.ParserProviderManager;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.views.project.model.ITmfProjectTreeNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfExperimentNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectContentProvider;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectLabelProvider;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectRoot;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectTreeNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfTraceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ResourceTransfer;
import org.osgi.service.prefs.BackingStoreException;

/**
 * <b><u>ProjectView</u></b>
 * <p>
 * The ProjectView keeps track of the Tmf projects in the workspace.
 */
public class ProjectView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.project"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Main data structures
    // ------------------------------------------------------------------------

    private TreeViewer fViewer;
    private TmfProjectRoot fProjectRoot;

    private IWorkspace fWorkspace;
    private IResourceChangeListener fResourceChangeListener;
    private IPreferenceChangeListener fPreferenceChangeListener;

    // ------------------------------------------------------------------------
    // Static methods
    // ------------------------------------------------------------------------

	static public IFolder getActiveProjectTracesFolder() {
		IEclipsePreferences node = new InstanceScope()
				.getNode(TmfUiPlugin.PLUGIN_ID);
		String activeProjectName = node.get(
				TmfUiPreferenceInitializer.ACTIVE_PROJECT_PREFERENCE,
				TmfUiPreferenceInitializer.ACTIVE_PROJECT_DEFAULT);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (IProject project : projects) {
			if (project.isAccessible()
					&& project.getName().equals(activeProjectName)) {
				return project.getFolder(TmfProjectNode.TRACE_FOLDER_NAME);
			}
		}
		return null;
	}

	static public IFile createLink(URI uri) throws CoreException {
		IFolder folder = getActiveProjectTracesFolder();
		if (folder == null || !folder.exists()) {
			throw new CoreException(new Status(Status.ERROR, TmfUiPlugin.PLUGIN_ID, "No active project set"));
		}
		String path = uri.getPath();
		// TODO: support duplicate file names
		IFile file = folder.getFile(path.substring(path
				.lastIndexOf(Path.SEPARATOR)));
		if (!file.exists()) {
			file.createLink(uri, IResource.NONE, null);
		}
		return file;
	}

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

    public TmfProjectRoot getRoot() {
    	return fProjectRoot;
    }
   
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

	public ProjectView() {
		super("Projects"); //$NON-NLS-1$
		
		fWorkspace = ResourcesPlugin.getWorkspace();
		try {
            fWorkspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }

		fProjectRoot = new TmfProjectRoot(this);
	}

	public void refresh() {
       	Tree tree = fViewer.getTree();
       	if (tree != null && !tree.isDisposed())
       		tree.getDisplay().asyncExec(fViewRefresher);
	}

	public void setSelection(ITmfProjectTreeNode node) {
		fViewer.setSelection(new StructuredSelection(node), true);
	}

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
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
        fPreferenceChangeListener = new IPreferenceChangeListener() {
            @Override
			public void preferenceChange(PreferenceChangeEvent event) {
                refresh();
            }
        };
        IEclipsePreferences node = new InstanceScope().getNode(TmfUiPlugin.PLUGIN_ID);
        node.addPreferenceChangeListener(fPreferenceChangeListener);
    }

    @Override
	public void dispose() {
    	fWorkspace.removeResourceChangeListener(fResourceChangeListener);
    }

	@Override
	public void createPartControl(Composite parent) {

		fViewer = new TreeViewer(parent, SWT.SINGLE);
        fViewer.setContentProvider(new TmfProjectContentProvider());
        fViewer.setSorter(new ViewerSorter());
        fViewer.setLabelProvider(new TmfProjectLabelProvider());
        fViewer.setInput(fProjectRoot);

        int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
        Transfer[] transfers = new Transfer[] { ResourceTransfer.getInstance(), FileTransfer.getInstance() };
        fViewer.addDropSupport(ops, transfers, new ViewerDropAdapter(fViewer) {
            @Override
            public boolean validateDrop(Object target, int operation, TransferData transferType) {
                if (! (target instanceof ITmfProjectTreeNode)) {
                    return false;
                }
                overrideOperation(DND.DROP_LINK);
                return true;
            }
            @Override
            public boolean performDrop(Object data) {
                Object target = getCurrentTarget();
                ITmfProjectTreeNode node = (TmfProjectTreeNode) target;
                while (node != null && !(node instanceof TmfProjectNode)) {
                    node = node.getParent();
                }
                IFolder targetFolder = ((TmfProjectNode) node).getTracesFolder().getFolder();
                if (data instanceof String[]) {
                    // FileTransfer
                    System.out.println("Drop:" + ((String[])data)[0]);
                    System.out.println("Folder:" + targetFolder);
                    for (String path : (String[]) data) {
                        File sourceFile = new File(path);
                        if (sourceFile.isFile()) {
                            IFile file = targetFolder.getFile(path.substring(path.lastIndexOf(File.separator)));
                            try {
                                file.createLink(sourceFile.toURI(), IResource.NONE, null);
                                ITmfTrace trace = ParserProviderManager.getTrace(file);
                                if (trace != null) {
                                    trace.dispose();
                                }
                            } catch (CoreException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (data instanceof IResource[]) {
                    // ResourceTransfer
                    System.out.println("Drop:" + ((IResource[])data)[0].getName());
                    System.out.println("Folder:" + targetFolder);
                    for (IResource resource : (IResource[]) data) {
                        if (resource instanceof IFile) {
                            IFile file = targetFolder.getFile(resource.getName());
                            try {
                                file.createLink(resource.getLocation(), IResource.NONE, null);
                                String parser = resource.getPersistentProperty(ParserProviderManager.PARSER_PROPERTY);
                                file.setPersistentProperty(ParserProviderManager.PARSER_PROPERTY, parser);
                            } catch (CoreException e) {
                                e.printStackTrace();
                            }
                        }
                        
                    }
                }
                return true;
            }
        });

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
                TreeItem treeItem = fViewer.getTree().getItem(new Point(event.x, event.y));
                if (treeItem == null) return;
                Object element = treeItem.getData();
                if (element instanceof TmfExperimentNode) {
                	TmfExperimentNode experiment = (TmfExperimentNode) element;
                	selectExperiment(experiment);                
                } else if (element instanceof TmfProjectNode) {
                    TmfProjectNode project = (TmfProjectNode) element;
                    if (project.isOpen() && project.isTmfProject()) {
                        IEclipsePreferences node = new InstanceScope().getNode(TmfUiPlugin.PLUGIN_ID);
                        node.put(TmfUiPreferenceInitializer.ACTIVE_PROJECT_PREFERENCE, ((TmfProjectNode) element).getName());
                        try {
                            node.flush();
                        } catch (BackingStoreException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (element instanceof TmfTraceNode) {
                    IWorkbench wb = PlatformUI.getWorkbench();
                    IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                    IHandlerService handlerService = (IHandlerService) win.getService(IHandlerService.class);
                    try {
                        handlerService.executeCommand("org.eclipse.linuxtools.tmf.ui.command.project.trace.open", null); //$NON-NLS-1$
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (NotDefinedException e) {
                        e.printStackTrace();
                    } catch (NotEnabledException e) {
                    } catch (NotHandledException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

	public void selectExperiment(TmfExperimentNode experiment) {
    	String expId = experiment.getName();
//        if (fSelectedExperiment != null)
//        	fSelectedExperiment.deregister();
        try {
        	TmfTraceNode[] traceEntries = experiment.getTraces();
        	int nbTraces = traceEntries.length;
        	List<ITmfTrace> traces = new ArrayList<ITmfTrace>();
        	int cacheSize = Integer.MAX_VALUE;
        	for (int i = 0; i < nbTraces; i++) {
        		IResource resource = traceEntries[i].getResource();
                resource = experiment.getProject().getTracesFolder().getTraceForLocation(resource.getLocation()).getResource();
        		ITmfTrace expTrace = ParserProviderManager.getTrace(resource);
        		if (expTrace != null) {
        		    traces.add(expTrace);
    		        cacheSize = Math.min(cacheSize, expTrace.getCacheSize());
    		        try {
    		            // create a new independent copy of the trace for the editor
		                ITmfTrace trace = ParserProviderManager.getTrace(resource);
    		            IEditorInput editorInput = new TmfEditorInput(resource, trace);
    		            IWorkbench wb = PlatformUI.getWorkbench();
    		            IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
    		  
    		            String editorId = ParserProviderManager.getEditorId(resource);
    		            IEditorPart editor = activePage.findEditor(editorInput);
    		            if (editor != null && editor instanceof IReusableEditor) {
    		                activePage.reuseEditor((IReusableEditor)editor, editorInput);
    		                activePage.activate(editor);
    		            } else {
    		                editor = activePage.openEditor(editorInput, editorId);
    		            }
    		            
    		        } catch (PartInitException e) {
    		            e.printStackTrace();
    		        }
        		}
        	}
        	TmfExperiment<TmfEvent> selectedExperiment = new TmfExperiment<TmfEvent>(TmfEvent.class, expId, traces.toArray(new ITmfTrace[0]), cacheSize);
            broadcast(new TmfExperimentSelectedSignal<TmfEvent>(this, selectedExperiment));
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