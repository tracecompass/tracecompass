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
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
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
    public void doSave(final IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void init(final IEditorSite site, IEditorInput input) throws PartInitException {
        if (input instanceof TmfEditorInput) {
            fFile = ((TmfEditorInput) input).getFile();
            fTrace = ((TmfEditorInput) input).getTrace();
            input = new FileEditorInput(fFile);
        } else if (input instanceof IFileEditorInput) {
            fFile = ((IFileEditorInput) input).getFile();
            if (fFile == null)
                throw new PartInitException("Invalid IFileEditorInput: " + input); //$NON-NLS-1$
            try {
                final String traceTypeId = fFile.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                if (traceTypeId == null)
                    throw new PartInitException(Messages.OpenTraceHandler_NoTraceType);
                if (traceTypeId.equals(TmfExperiment.class.getCanonicalName())) {
                    // Special case: experiment bookmark resource
                    final TmfNavigatorContentProvider ncp = new TmfNavigatorContentProvider();
                    ncp.getChildren(fFile.getProject()); // force the model to be populated
                    final TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject());
                    if (project == null)
                        throw new PartInitException(Messages.OpenExperimentHandler_NoTraceType);
                    for (final ITmfProjectModelElement projectElement : project.getExperimentsFolder().getChildren()) {
                        final String traceName = fFile.getParent().getName();
                        if (projectElement.getName().equals(traceName)) {
                            final TmfExperimentElement experimentElement = (TmfExperimentElement) projectElement;
                            // Instantiate the experiment's traces
                            final List<TmfTraceElement> traceEntries = experimentElement.getTraces();
                            final int nbTraces = traceEntries.size();
                            int cacheSize = Integer.MAX_VALUE;
                            final ITmfTrace<?>[] traces = new ITmfTrace[nbTraces];
                            for (int i = 0; i < nbTraces; i++) {
                                final TmfTraceElement traceElement = traceEntries.get(i);
                                final ITmfTrace trace = traceElement.instantiateTrace();
                                final ITmfEvent traceEvent = traceElement.instantiateEvent();
                                if ((trace == null) || (traceEvent == null)) {
                                    for (int j = 0; j < i; j++)
                                        traces[j].dispose();
                                    throw new PartInitException(Messages.OpenExperimentHandler_NoTraceType);
                                }
                                try {
                                    trace.initTrace(traceElement.getResource(), traceElement.getLocation().getPath(), traceEvent.getClass());
                                } catch (final TmfTraceException e) {
                                }
                                cacheSize = Math.min(cacheSize, trace.getCacheSize());
                                traces[i] = trace;
                            }
                            final TmfExperiment experiment = new TmfExperiment(ITmfEvent.class, experimentElement.getName(), traces, cacheSize);
                            experiment.setBookmarksFile(fFile);
                            fTrace = experiment;
                            experiment.initTrace(null, null, null);
                            break;
                        }
                    }
                } else if (traceTypeId.equals(TmfTrace.class.getCanonicalName())) {
                    // Special case: trace bookmark resource
                    final TmfNavigatorContentProvider ncp = new TmfNavigatorContentProvider();
                    ncp.getChildren(fFile.getProject()); // force the model to be populated
                    final TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject());
                    for (final ITmfProjectModelElement projectElement : project.getTracesFolder().getChildren()) {
                        final String traceName = fFile.getParent().getName();
                        if (projectElement.getName().equals(traceName)) {
                            final TmfTraceElement traceElement = (TmfTraceElement) projectElement;
                            // Instantiate the trace
                            final ITmfTrace trace = traceElement.instantiateTrace();
                            final ITmfEvent traceEvent = traceElement.instantiateEvent();
                            if ((trace == null) || (traceEvent == null))
                                throw new PartInitException(Messages.OpenTraceHandler_NoTraceType);
                            try {
                                trace.initTrace(traceElement.getResource(), traceElement.getLocation().getPath(), traceEvent.getClass());
                            } catch (final TmfTraceException e) {
                            }
                            fTrace = trace;
                            break;
                        }
                    }
                } else {
                    final TmfNavigatorContentProvider ncp = new TmfNavigatorContentProvider();
                    ncp.getChildren(fFile.getProject()); // force the model to be populated
                    final TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject());
                    for (final ITmfProjectModelElement projectElement : project.getTracesFolder().getChildren())
                        if (projectElement.getResource().equals(fFile)) {
                            final TmfTraceElement traceElement = (TmfTraceElement) projectElement;
                            // Instantiate the trace
                            final ITmfTrace trace = traceElement.instantiateTrace();
                            final ITmfEvent traceEvent = traceElement.instantiateEvent();
                            if ((trace == null) || (traceEvent == null))
                                throw new PartInitException(Messages.OpenTraceHandler_NoTraceType);
                            try {
                                trace.initTrace(traceElement.getResource(), traceElement.getLocation().getPath(), traceEvent.getClass());
                            } catch (final TmfTraceException e) {
                            }
                            fTrace = trace;
                            break;
                        }
                }
            } catch (final InvalidRegistryObjectException e) {
                e.printStackTrace();
            } catch (final PartInitException e) {
                throw e;
            } catch (final CoreException e) {
                e.printStackTrace();
            }
        } else
            throw new PartInitException("Invalid IEditorInput: " + input.getClass()); //$NON-NLS-1$
        if (fTrace == null)
            throw new PartInitException("Invalid IEditorInput: " + fFile.getName()); //$NON-NLS-1$
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
    public void setInput(final IEditorInput input) {
        super.setInput(input);
        firePropertyChange(IEditorPart.PROP_INPUT);
    }

    @Override
    public void propertyChanged(final Object source, final int propId) {
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
            } else
                fEventsTable = new TmfEventsTable(fParent, 0);
            fParent.layout();
        }
    }

    @Override
    public void createPartControl(final Composite parent) {
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
        if (fTrace != null)
            broadcast(new TmfTraceClosedSignal(this, fTrace));
        if (fEventsTable != null)
            fEventsTable.dispose();
        super.dispose();
    }

    protected TmfEventsTable createEventsTable(final Composite parent, final int cacheSize) {
        TmfEventsTable eventsTable = getEventsTable(parent, cacheSize);
        if (eventsTable == null)
            eventsTable = new TmfEventsTable(parent, cacheSize);
        return eventsTable;
    }

    private TmfEventsTable getEventsTable(final Composite parent, final int cacheSize) {
        if (fTrace instanceof TmfExperiment)
            return getExperimentEventsTable((TmfExperiment<?>) fTrace, parent, cacheSize);
        TmfEventsTable eventsTable = null;
        try {
            if (fTrace.getResource() == null)
                return null;
            final String traceType = fTrace.getResource().getPersistentProperty(TmfCommonConstants.TRACETYPE);
            if (traceType == null)
                return null;
            if (traceType.startsWith(CustomTxtTrace.class.getCanonicalName()))
                return new CustomEventsTable(((CustomTxtTrace) fTrace).getDefinition(), parent, cacheSize);
            if (traceType.startsWith(CustomXmlTrace.class.getCanonicalName()))
                return new CustomEventsTable(((CustomXmlTrace) fTrace).getDefinition(), parent, cacheSize);
            for (final IConfigurationElement ce : TmfTraceType.getTypeElements())
                if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(traceType)) {
                    final IConfigurationElement[] eventsTableTypeCE = ce.getChildren(TmfTraceType.EVENTS_TABLE_TYPE_ELEM);
                    if (eventsTableTypeCE.length != 1)
                        break;
                    final String eventsTableType = eventsTableTypeCE[0].getAttribute(TmfTraceType.CLASS_ATTR);
                    if ((eventsTableType == null) || (eventsTableType.length() == 0))
                        break;
                    final Bundle bundle = Platform.getBundle(ce.getContributor().getName());
                    final Class<?> c = bundle.loadClass(eventsTableType);
                    final Class<?>[] constructorArgs = new Class[] { Composite.class, int.class };
                    final Constructor<?> constructor = c.getConstructor(constructorArgs);
                    final Object[] args = new Object[] { parent, cacheSize };
                    eventsTable = (TmfEventsTable) constructor.newInstance(args);
                    break;
                }
        } catch (final InvalidRegistryObjectException e) {
            e.printStackTrace();
        } catch (final CoreException e) {
            e.printStackTrace();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
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
    private TmfEventsTable getExperimentEventsTable(final TmfExperiment<?> experiment, final Composite parent, final int cacheSize) {
        TmfEventsTable eventsTable = null;
        String commonTraceType = null;
        try {
            for (final ITmfTrace<?> trace : experiment.getTraces()) {
                final IResource resource = trace.getResource();
                if (resource == null)
                    return null;
                final String traceType = resource.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                if ((commonTraceType != null) && !commonTraceType.equals(traceType))
                    return null;
                commonTraceType = traceType;
            }
            if (commonTraceType == null)
                return null;
            if (commonTraceType.startsWith(CustomTxtTrace.class.getCanonicalName()))
                return new CustomEventsTable(((CustomTxtTrace) experiment.getTraces()[0]).getDefinition(), parent, cacheSize);
            if (commonTraceType.startsWith(CustomXmlTrace.class.getCanonicalName()))
                return new CustomEventsTable(((CustomXmlTrace) experiment.getTraces()[0]).getDefinition(), parent, cacheSize);
            for (final IConfigurationElement ce : TmfTraceType.getTypeElements())
                if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(commonTraceType)) {
                    final IConfigurationElement[] eventsTableTypeCE = ce.getChildren(TmfTraceType.EVENTS_TABLE_TYPE_ELEM);
                    if (eventsTableTypeCE.length != 1)
                        break;
                    final String eventsTableType = eventsTableTypeCE[0].getAttribute(TmfTraceType.CLASS_ATTR);
                    if ((eventsTableType == null) || (eventsTableType.length() == 0))
                        break;
                    final Bundle bundle = Platform.getBundle(ce.getContributor().getName());
                    final Class<?> c = bundle.loadClass(eventsTableType);
                    final Class<?>[] constructorArgs = new Class[] { Composite.class, int.class };
                    final Constructor<?> constructor = c.getConstructor(constructorArgs);
                    final Object[] args = new Object[] { parent, cacheSize };
                    eventsTable = (TmfEventsTable) constructor.newInstance(args);
                    break;
                }
        } catch (final CoreException e) {
            e.printStackTrace();
        } catch (final InvalidRegistryObjectException e) {
            e.printStackTrace();
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
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
        if (fTrace != null)
            broadcast(new TmfTraceSelectedSignal(this, fTrace));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(final Class adapter) {
        if (IGotoMarker.class.equals(adapter))
            return fEventsTable;
        return super.getAdapter(adapter);
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        for (final IMarkerDelta delta : event.findMarkerDeltas(IMarker.BOOKMARK, false))
            if (delta.getResource().equals(fFile))
                if (delta.getKind() == IResourceDelta.REMOVED) {
                    final IMarker bookmark = delta.getMarker();
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            fEventsTable.removeBookmark(bookmark);
                        }
                    });
                } else if (delta.getKind() == IResourceDelta.CHANGED)
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            fEventsTable.getTable().refresh();
                        }
                    });
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
    public void traceParserUpdated(final TmfTraceParserUpdatedSignal signal) {
        if (signal.getTraceResource().equals(fFile)) {
            broadcast(new TmfTraceClosedSignal(this, fTrace));
            try {
                fTrace.getName();
                fTrace = null;
                final String traceTypeId = fFile.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                if (traceTypeId != null)
                    for (final IConfigurationElement ce : TmfTraceType.getTypeElements())
                        if (traceTypeId.equals(ce.getAttribute(TmfTraceType.ID_ATTR))) {
                            fTrace = (ITmfTrace<?>) ce.createExecutableExtension(TmfTraceType.TRACE_TYPE_ATTR);
                            final ITmfEvent event = (TmfEvent) ce.createExecutableExtension(TmfTraceType.EVENT_TYPE_ATTR);
                            final String path = fFile.getLocationURI().getPath();
                            fTrace.initTrace(null, path, event.getClass());
                            break;
                        }
            } catch (final InvalidRegistryObjectException e) {
                e.printStackTrace();
            } catch (final TmfTraceException e) {
                e.printStackTrace();
            } catch (final CoreException e) {
                e.printStackTrace();
            }
            fEventsTable.dispose();
            if (fTrace != null) {
                fEventsTable = createEventsTable(fParent, fTrace.getCacheSize());
                fEventsTable.setTrace(fTrace, true);
                broadcast(new TmfTraceOpenedSignal(this, fTrace, fFile, fEventsTable));
            } else
                fEventsTable = new TmfEventsTable(fParent, 0);
            fParent.layout();
        }
    }

    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        if ((signal.getSource() != this) && signal.getTrace().equals(fTrace))
            getSite().getPage().bringToTop(this);
    }

}
