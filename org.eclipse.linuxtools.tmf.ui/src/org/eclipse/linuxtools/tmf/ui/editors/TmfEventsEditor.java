/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.parsers.ParserProviderManager;
import org.eclipse.linuxtools.tmf.ui.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.ui.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.ui.signal.TmfTraceParserUpdatedSignal;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.views.project.ProjectView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IGotoMarker;

/**
 * <b><u>TmfEventsEditor</u></b>
 */
public class TmfEventsEditor extends TmfEditor implements ITmfTraceEditor, IReusableEditor, IPropertyListener, IResourceChangeListener {

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.editors.events"; //$NON-NLS-1$
    
    private TmfEventsTable fEventsTable;
    private IResource fResource;
    private ITmfTrace fTrace;
    private Composite fParent;

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (input instanceof TmfEditorInput) {
            fResource = ((TmfEditorInput) input).getResource();
            fTrace = ((TmfEditorInput) input).getTrace();
        } else if (input instanceof IFileEditorInput) {
            fResource = ((IFileEditorInput) input).getFile();
            fTrace = ParserProviderManager.getTrace(fResource);
            input = new TmfEditorInput(fResource, fTrace);
        } else if (input instanceof FileStoreEditorInput) {
            try {
                FileStoreEditorInput fileStoreEditorInput = (FileStoreEditorInput) input;
                fResource = ProjectView.createLink(fileStoreEditorInput.getURI());
                fTrace = ParserProviderManager.getTrace(fResource);
                input = new TmfEditorInput(fResource, fTrace);
            } catch (CoreException e) {
                throw new PartInitException(e.getMessage());
            }
        } else {
            throw new PartInitException("Invalid IEditorInput: " + input.getClass()); //$NON-NLS-1$
        }
        if (fTrace == null) {
            throw new PartInitException("Invalid IEditorInput: " + fResource.getName()); //$NON-NLS-1$
        }
        super.setSite(site);
        super.setInput(input);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void setInput(IEditorInput input) {
        super.setInput(input);
        firePropertyChange(IEditorPart.PROP_INPUT);
    }

    @Override
	public void propertyChanged(Object source, int propId) {
        if (propId == IEditorPart.PROP_INPUT) {
            broadcast(new TmfTraceClosedSignal(this, fTrace));
            fResource = ((TmfEditorInput) getEditorInput()).getResource();
            fTrace = ((TmfEditorInput) getEditorInput()).getTrace();
            fEventsTable.dispose();
            if (fTrace != null) {
                fEventsTable = createEventsTable(fParent, fTrace.getCacheSize());
                fEventsTable.setTrace(fTrace, true);
                fEventsTable.refreshBookmarks(fResource);
                broadcast(new TmfTraceOpenedSignal(this, fTrace, fResource, fEventsTable));
            } else {
                fEventsTable = new TmfEventsTable(fParent, 0);
            }
            fParent.layout();
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        fParent = parent;
        setPartName(getEditorInput().getName());
        if (fTrace != null) {
            fEventsTable = createEventsTable(parent, fTrace.getCacheSize());
            fEventsTable.setTrace(fTrace, true);
            fEventsTable.refreshBookmarks(fResource);
            broadcast(new TmfTraceOpenedSignal(this, fTrace, fResource, fEventsTable));
        } else {
            fEventsTable = new TmfEventsTable(parent, 0);
        }
        addPropertyListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public void dispose() {
    	ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    	removePropertyListener(this);
        if (fTrace != null) {
            broadcast(new TmfTraceClosedSignal(this, fTrace));
        }
        if (fEventsTable != null) {
            fEventsTable.dispose();
        }
        super.dispose();
    }

    protected TmfEventsTable createEventsTable(Composite parent, int cacheSize) {
        TmfEventsTable eventsTable = ParserProviderManager.getEventsTable(fTrace, parent, cacheSize);
        if (eventsTable == null) {
            eventsTable = new TmfEventsTable(parent, cacheSize);
        }
        return eventsTable;
    }
    
    @Override
	public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public IResource getResource() {
    	return fResource;
    }

    @Override
    public void setFocus() {
        fEventsTable.setFocus();
        if (fTrace != null) {
            broadcast(new TmfTraceSelectedSignal(this, fTrace));
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
    	if (IGotoMarker.class.equals(adapter)) {
    		return fEventsTable;
    	}
    	return super.getAdapter(adapter);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
    	for (IMarkerDelta delta : event.findMarkerDeltas(IMarker.BOOKMARK, false)) {
    		if (delta.getResource().equals(fResource) && delta.getKind() == IResourceDelta.REMOVED) {
    			final IMarker bookmark = delta.getMarker();
    			Display.getDefault().asyncExec(new Runnable() {
    				@Override
    				public void run() {
    					fEventsTable.removeBookmark(bookmark);
    				}
    			});
    		}
    	}
    }
     
    // ------------------------------------------------------------------------
    // Global commands
    // ------------------------------------------------------------------------

    public void addBookmark() {
    	fEventsTable.addBookmark(fResource);
    }
    

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------
    
    @TmfSignalHandler
    public void traceParserUpdated(TmfTraceParserUpdatedSignal signal) {
        if (signal.getTraceResource().equals(fResource)) {
            broadcast(new TmfTraceClosedSignal(this, fTrace));
            fTrace = ParserProviderManager.getTrace(fResource);
            fEventsTable.dispose();
            if (fTrace != null) {
                fEventsTable = createEventsTable(fParent, fTrace.getCacheSize());
                fEventsTable.setTrace(fTrace, true);
                broadcast(new TmfTraceOpenedSignal(this, fTrace, fResource, fEventsTable));
            } else {
                fEventsTable = new TmfEventsTable(fParent, 0);
            }
            fParent.layout();
        }
    }

    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        if (signal.getSource() != this && signal.getTrace().equals(fTrace)) {
            getSite().getPage().bringToTop(this);
        }
    }

}
