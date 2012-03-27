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

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomEventsTable;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.internal.tmf.ui.project.handlers.Messages;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.ui.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.ui.signal.TmfTraceParserUpdatedSignal;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;

/**
 * <b><u>TmfEventsEditor</u></b>
 */
public class TmfEventsEditor extends TmfEditor implements ITmfTraceEditor, IReusableEditor, IPropertyListener, IResourceChangeListener {

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.editors.events"; //$NON-NLS-1$
    
    private TmfEventsTable fEventsTable;
    private IFile fFile;
    @SuppressWarnings("rawtypes")
    private ITmfTrace fTrace;
    private Composite fParent;

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (input instanceof TmfEditorInput) {
            fFile = ((TmfEditorInput) input).getFile();
            fTrace = ((TmfEditorInput) input).getTrace();
            input = new FileEditorInput(fFile);
        } else if (input instanceof IFileEditorInput) {
            fFile = ((IFileEditorInput) input).getFile();
            if (fFile == null) {
                throw new PartInitException("Invalid IFileEditorInput: " + input); //$NON-NLS-1$
            }
            try {
                String traceTypeId = fFile.getPersistentProperty(TmfTraceElement.TRACETYPE);
                if (traceTypeId == null) {
                    throw new PartInitException(Messages.OpenTraceHandler_NoTraceType);
                }
                if (traceTypeId.equals(TmfExperiment.class.getCanonicalName())) {
                    // Special case: experiment bookmark resource
                    TmfNavigatorContentProvider ncp = new TmfNavigatorContentProvider();
                    ncp.getChildren(fFile.getProject()); // force the model to be populated
                    TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject());
                    if (project == null) {
                        throw new PartInitException(Messages.OpenExperimentHandler_NoTraceType);
                    }
                    for (ITmfProjectModelElement projectElement : project.getExperimentsFolder().getChildren()) {
                        String traceName = fFile.getParent().getName();
                        if (projectElement.getName().equals(traceName)) {
                            TmfExperimentElement experimentElement = (TmfExperimentElement) projectElement;
                            // Instantiate the experiment's traces
                            List<TmfTraceElement> traceEntries = experimentElement.getTraces();
                            int nbTraces = traceEntries.size();
                            int cacheSize = Integer.MAX_VALUE;
                            ITmfTrace<?>[] traces = new ITmfTrace[nbTraces];
                            for (int i = 0; i < nbTraces; i++) {
                                TmfTraceElement traceElement = traceEntries.get(i);
                                ITmfTrace trace = traceElement.instantiateTrace();
                                TmfEvent traceEvent = traceElement.instantiateEvent();
                                if (trace == null || traceEvent == null) {
                                    for (int j = 0; j < i; j++) {
                                        traces[j].dispose();
                                    }
                                    throw new PartInitException(Messages.OpenExperimentHandler_NoTraceType);
                                }
                                try {
                                    trace.initTrace(traceElement.getName(), traceElement.getLocation().getPath(), traceEvent.getClass(), 0);
                                } catch (FileNotFoundException e) {
                                }
                                trace.setResource(traceElement.getResource());
                                cacheSize = Math.min(cacheSize, trace.getCacheSize());
                                traces[i] = trace;
                            }
                            TmfExperiment experiment = new TmfExperiment(TmfEvent.class, experimentElement.getName(), traces, cacheSize);
                            experiment.setBookmarksFile(fFile);
                            fTrace = experiment;
                            experiment.initTrace(null, null, null, 0);
                            experiment.indexTrace(true);
                            break;
                        }
                    }
                } else if (traceTypeId.equals(TmfTrace.class.getCanonicalName())) {
                    // Special case: trace bookmark resource
                    TmfNavigatorContentProvider ncp = new TmfNavigatorContentProvider();
                    ncp.getChildren(fFile.getProject()); // force the model to be populated
                    TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject());
                    for (ITmfProjectModelElement projectElement : project.getTracesFolder().getChildren()) {
                        String traceName = fFile.getParent().getName();
                        if (projectElement.getName().equals(traceName)) {
                            TmfTraceElement traceElement = (TmfTraceElement) projectElement;
                            // Instantiate the trace
                            ITmfTrace trace = traceElement.instantiateTrace();
                            TmfEvent traceEvent = traceElement.instantiateEvent();
                            if (trace == null || traceEvent == null) {
                                throw new PartInitException(Messages.OpenTraceHandler_NoTraceType);
                            }
                            try {
                                trace.initTrace(traceElement.getName(), traceElement.getLocation().getPath(), traceEvent.getClass(), 0);
                                trace.indexTrace(false);
                            } catch (FileNotFoundException e) {
                            }
                            trace.setResource(traceElement.getResource());
                            fTrace = trace;
                            break;
                        }
                    }
                } else {
                    TmfNavigatorContentProvider ncp = new TmfNavigatorContentProvider();
                    ncp.getChildren(fFile.getProject()); // force the model to be populated
                    TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject());
                    for (ITmfProjectModelElement projectElement : project.getTracesFolder().getChildren()) {
                        if (projectElement.getResource().equals(fFile)) {
                            TmfTraceElement traceElement = (TmfTraceElement) projectElement;
                            // Instantiate the trace
                            ITmfTrace trace = traceElement.instantiateTrace();
                            TmfEvent traceEvent = traceElement.instantiateEvent();
                            if (trace == null || traceEvent == null) {
                                throw new PartInitException(Messages.OpenTraceHandler_NoTraceType);
                            }
                            try {
                                trace.initTrace(traceElement.getName(), traceElement.getLocation().getPath(), traceEvent.getClass(), 0);
                                trace.indexTrace(false);
                            } catch (FileNotFoundException e) {
                            }
                            if (trace instanceof TmfTrace) {
                                ((TmfTrace) trace).setResource(traceElement.getResource());
                            }
                            fTrace = trace;
                            break;
                        }
                    }
                }
            } catch (InvalidRegistryObjectException e) {
                e.printStackTrace();
            } catch (PartInitException e) {
                throw e;
            } catch (CoreException e) {
                e.printStackTrace();
            }
        } else {
            throw new PartInitException("Invalid IEditorInput: " + input.getClass()); //$NON-NLS-1$
        }
        if (fTrace == null) {
            throw new PartInitException("Invalid IEditorInput: " + fFile.getName()); //$NON-NLS-1$
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
            fFile = ((TmfEditorInput) getEditorInput()).getFile();
            fTrace = ((TmfEditorInput) getEditorInput()).getTrace();
            super.setInput(new FileEditorInput(fFile));
            fEventsTable.dispose();
            if (fTrace != null) {
                fEventsTable = createEventsTable(fParent, fTrace.getCacheSize());
                fEventsTable.setTrace(fTrace, true);
                fEventsTable.refreshBookmarks(fFile);
                broadcast(new TmfTraceOpenedSignal(this, fTrace, fFile, fEventsTable));
            } else {
                fEventsTable = new TmfEventsTable(fParent, 0);
            }
            fParent.layout();
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        fParent = parent;
        if (fTrace != null) {
            setPartName(fTrace.getName());
            fEventsTable = createEventsTable(parent, fTrace.getCacheSize());
            fEventsTable.setTrace(fTrace, true);
            fEventsTable.refreshBookmarks(fFile);
            broadcast(new TmfTraceOpenedSignal(this, fTrace, fFile, fEventsTable));
        } else {
            setPartName(getEditorInput().getName());
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
        TmfEventsTable eventsTable = getEventsTable(parent, cacheSize);
        if (eventsTable == null) {
            eventsTable = new TmfEventsTable(parent, cacheSize);
        }
        return eventsTable;
    }
    
    private TmfEventsTable getEventsTable(Composite parent, int cacheSize) {
        if (fTrace instanceof TmfExperiment) {
            return getExperimentEventsTable((TmfExperiment<?>) fTrace, parent, cacheSize);
        }
        TmfEventsTable eventsTable = null;
        try {
            if (fTrace.getResource() == null) {
                return null;
            }
            String traceType = fTrace.getResource().getPersistentProperty(TmfTraceElement.TRACETYPE);
            if (traceType == null) {
                return null;
            }
            if (traceType.startsWith(CustomTxtTrace.class.getCanonicalName())) {
                return new CustomEventsTable(((CustomTxtTrace) fTrace).getDefinition(), parent, cacheSize);
            }
            if (traceType.startsWith(CustomXmlTrace.class.getCanonicalName())) {
                return new CustomEventsTable(((CustomXmlTrace) fTrace).getDefinition(), parent, cacheSize);
            }
            for (IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(traceType)) {
                    IConfigurationElement[] eventsTableTypeCE = ce.getChildren(TmfTraceType.EVENTS_TABLE_TYPE_ELEM);
                    if (eventsTableTypeCE.length != 1) {
                        break;
                    }
                    String eventsTableType = eventsTableTypeCE[0].getAttribute(TmfTraceType.CLASS_ATTR);
                    if (eventsTableType == null || eventsTableType.length() == 0) {
                        break;
                    }
                    Bundle bundle = Platform.getBundle(ce.getContributor().getName());
                    Class<?> c = bundle.loadClass(eventsTableType);
                    Class<?>[] constructorArgs = new Class[] { Composite.class, int.class };
                    Constructor<?> constructor = c.getConstructor(constructorArgs);
                    Object[] args = new Object[] { parent, cacheSize };
                    eventsTable = (TmfEventsTable) constructor.newInstance(args);
                    break;
                }
            }
        } catch (InvalidRegistryObjectException e) {
            e.printStackTrace();
        } catch (CoreException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return eventsTable;
    }

    /**
     * Get the events table for an experiment.
     * If all traces in the experiment are of the same type,
     * use the extension point specified event table
     * @param experiment the experiment
     * @param parent the parent Composite
     * @param cacheSize the event table cache size
     * @return an events table of the appropriate type
     */
    private TmfEventsTable getExperimentEventsTable(TmfExperiment<?> experiment, Composite parent, int cacheSize) {
        TmfEventsTable eventsTable = null;
        String commonTraceType = null;
        try {
            for (ITmfTrace<?> trace : experiment.getTraces()) {
                IResource resource = trace.getResource();
                if (resource == null) {
                    return null;
                }
                String traceType = resource.getPersistentProperty(TmfTraceElement.TRACETYPE);
                if (commonTraceType != null && !commonTraceType.equals(traceType)) {
                    return null;
                }
                commonTraceType = traceType;
            }
            if (commonTraceType == null) {
                return null;
            }
            if (commonTraceType.startsWith(CustomTxtTrace.class.getCanonicalName())) {
                return new CustomEventsTable(((CustomTxtTrace) experiment.getTraces()[0]).getDefinition(), parent, cacheSize);
            }
            if (commonTraceType.startsWith(CustomXmlTrace.class.getCanonicalName())) {
                return new CustomEventsTable(((CustomXmlTrace) experiment.getTraces()[0]).getDefinition(), parent, cacheSize);
            }
            for (IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(commonTraceType)) {
                    IConfigurationElement[] eventsTableTypeCE = ce.getChildren(TmfTraceType.EVENTS_TABLE_TYPE_ELEM);
                    if (eventsTableTypeCE.length != 1) {
                        break;
                    }
                    String eventsTableType = eventsTableTypeCE[0].getAttribute(TmfTraceType.CLASS_ATTR);
                    if (eventsTableType == null || eventsTableType.length() == 0) {
                        break;
                    }
                    Bundle bundle = Platform.getBundle(ce.getContributor().getName());
                    Class<?> c = bundle.loadClass(eventsTableType);
                    Class<?>[] constructorArgs = new Class[] { Composite.class, int.class };
                    Constructor<?> constructor = c.getConstructor(constructorArgs);
                    Object[] args = new Object[] { parent, cacheSize };
                    eventsTable = (TmfEventsTable) constructor.newInstance(args);
                    break;
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        } catch (InvalidRegistryObjectException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return eventsTable;
    }

    @Override
	public ITmfTrace<?> getTrace() {
        return fTrace;
    }

    @Override
    public IFile getBookmarksFile() {
    	return fFile;
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
            if (delta.getResource().equals(fFile)) {
                if (delta.getKind() == IResourceDelta.REMOVED) {
                    final IMarker bookmark = delta.getMarker();
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            fEventsTable.removeBookmark(bookmark);
                        }
                    });
                } else if (delta.getKind() == IResourceDelta.CHANGED) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            fEventsTable.getTable().refresh();
                        }
                    });
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Global commands
    // ------------------------------------------------------------------------

    public void addBookmark() {
    	fEventsTable.addBookmark(fFile);
    }
    

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------
    
    @SuppressWarnings("unchecked")
    @TmfSignalHandler
    public void traceParserUpdated(TmfTraceParserUpdatedSignal signal) {
        if (signal.getTraceResource().equals(fFile)) {
            broadcast(new TmfTraceClosedSignal(this, fTrace));
            try {
                String name = fTrace.getName();
                fTrace = null;
                String traceTypeId = fFile.getPersistentProperty(TmfTraceElement.TRACETYPE);
                if (traceTypeId != null) {
                    for (IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                        if (traceTypeId.equals(ce.getAttribute(TmfTraceType.ID_ATTR))) {
                            fTrace = (ITmfTrace<?>) ce.createExecutableExtension(TmfTraceType.TRACE_TYPE_ATTR);
                            TmfEvent event = (TmfEvent) ce.createExecutableExtension(TmfTraceType.EVENT_TYPE_ATTR);
                            String path = fFile.getLocationURI().getPath();
                            fTrace.initTrace(name, path, event.getClass(), 0);
                            fTrace.indexTrace(false);
                            break;
                        }
                    }
                }
            } catch (InvalidRegistryObjectException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (CoreException e) {
                e.printStackTrace();
            }
            fEventsTable.dispose();
            if (fTrace != null) {
                fEventsTable = createEventsTable(fParent, fTrace.getCacheSize());
                fEventsTable.setTrace(fTrace, true);
                broadcast(new TmfTraceOpenedSignal(this, fTrace, fFile, fEventsTable));
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
