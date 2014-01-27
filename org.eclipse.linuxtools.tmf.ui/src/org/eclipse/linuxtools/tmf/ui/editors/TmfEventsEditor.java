/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
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
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomEventsTable;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.Messages;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.osgi.framework.Bundle;

/**
 * Editor for TMF events
 *
 * @version 1.0
 * @author Patrick Tasse
 * @since 2.0
 */
public class TmfEventsEditor extends TmfEditor implements ITmfTraceEditor, IReusableEditor, IPropertyListener, IResourceChangeListener, ISelectionProvider, ISelectionChangedListener, IPartListener, IGotoMarker {

    /** ID for this class */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.editors.events"; //$NON-NLS-1$

    private TmfEventsTable fEventsTable;
    private IFile fFile;
    private ITmfTrace fTrace;
    private Composite fParent;
    private ListenerList fSelectionChangedListeners = new ListenerList();
    private boolean fTraceSelected;
    private IMarker fPendingGotoMarker;

    @Override
    public void doSave(final IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void init(final IEditorSite site, IEditorInput input) throws PartInitException {
        IFileEditorInput fileEditorInput;
        if (input instanceof TmfEditorInput) {
            fFile = ((TmfEditorInput) input).getFile();
            fTrace = ((TmfEditorInput) input).getTrace();
            /* change the input to a FileEditorInput to allow open handlers to find this editor */
            fileEditorInput = new FileEditorInput(fFile);
        } else if (input instanceof IFileEditorInput) {
            fileEditorInput = (IFileEditorInput) input;
            fFile = fileEditorInput.getFile();
            if (fFile == null) {
                throw new PartInitException("Invalid IFileEditorInput: " + fileEditorInput); //$NON-NLS-1$
            }
            try {
                final String traceTypeId = fFile.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                if (traceTypeId == null) {
                    throw new PartInitException(Messages.TmfOpenTraceHelper_NoTraceType);
                }
                if (traceTypeId.equals(TmfExperiment.class.getCanonicalName())) {
                    // Special case: experiment bookmark resource
                    final TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject(), true);
                    if (project == null) {
                        throw new PartInitException(Messages.TmfOpenTraceHelper_NoTraceType);
                    }
                    for (final ITmfProjectModelElement projectElement : project.getExperimentsFolder().getChildren()) {
                        final String traceName = fFile.getParent().getName();
                        if (projectElement.getName().equals(traceName)) {
                            final TmfExperimentElement experimentElement = (TmfExperimentElement) projectElement;
                            setPartName(experimentElement.getName());
                            super.setSite(site);
                            super.setInput(fileEditorInput);
                            TmfOpenTraceHelper.reopenExperimentFromElement(experimentElement, this);
                            return;
                        }
                    }
                } else if (traceTypeId.equals(TmfTrace.class.getCanonicalName())) {
                    // Special case: trace bookmark resource
                    final TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject(), true);
                    for (final ITmfProjectModelElement projectElement : project.getTracesFolder().getChildren()) {
                        final String traceName = fFile.getParent().getName();
                        if (projectElement.getName().equals(traceName)) {
                            final TmfTraceElement traceElement = (TmfTraceElement) projectElement;
                            setPartName(traceElement.getName());
                            super.setSite(site);
                            super.setInput(fileEditorInput);
                            TmfOpenTraceHelper.reopenTraceFromElement(traceElement, this);
                            return;
                        }
                    }
                } else {
                    final TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject(), true);
                    for (final ITmfProjectModelElement projectElement : project.getTracesFolder().getChildren()) {
                        if (projectElement.getResource().equals(fFile)) {
                            final TmfTraceElement traceElement = (TmfTraceElement) projectElement;
                            setPartName(traceElement.getName());
                            super.setSite(site);
                            super.setInput(fileEditorInput);
                            TmfOpenTraceHelper.reopenTraceFromElement(traceElement, this);
                            return;
                        }
                    }
                }
            } catch (final PartInitException e) {
                throw e;
            } catch (final InvalidRegistryObjectException e) {
                Activator.getDefault().logError("Error initializing TmfEventsEditor", e); //$NON-NLS-1$
            } catch (final CoreException e) {
                Activator.getDefault().logError("Error initializing TmfEventsEditor", e); //$NON-NLS-1$
            }
        } else {
            throw new PartInitException("Invalid IEditorInput: " + input.getClass()); //$NON-NLS-1$
        }
        if (fTrace == null) {
            throw new PartInitException("Invalid IEditorInput: " + fFile.getName()); //$NON-NLS-1$
        }
        super.setSite(site);
        super.setInput(fileEditorInput);
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
        if (propId == IEditorPart.PROP_INPUT && getEditorInput() instanceof TmfEditorInput) {
            if (fTrace != null) {
                broadcast(new TmfTraceClosedSignal(this, fTrace));
            }
            fTraceSelected = false;
            fFile = ((TmfEditorInput) getEditorInput()).getFile();
            fTrace = ((TmfEditorInput) getEditorInput()).getTrace();
            /* change the input to a FileEditorInput to allow open handlers to find this editor */
            super.setInput(new FileEditorInput(fFile));
            fEventsTable.dispose();
            if (fTrace != null) {
                setPartName(fTrace.getName());
                fEventsTable = createEventsTable(fParent, fTrace.getCacheSize());
                fEventsTable.addSelectionChangedListener(this);
                fEventsTable.setTrace(fTrace, true);
                fEventsTable.refreshBookmarks(fFile);
                if (fPendingGotoMarker != null) {
                    fEventsTable.gotoMarker(fPendingGotoMarker);
                    fPendingGotoMarker = null;
                }

                /* ensure start time is set */
                final ITmfContext context = fTrace.seekEvent(0);
                fTrace.getNext(context);
                context.dispose();

                broadcast(new TmfTraceOpenedSignal(this, fTrace, fFile));
            } else {
                setPartName(getEditorInput().getName());
                fEventsTable = new TmfEventsTable(fParent, 0);
                fEventsTable.addSelectionChangedListener(this);
            }
            fParent.layout();
        }
    }

    @Override
    public void createPartControl(final Composite parent) {
        fParent = parent;
        if (fTrace != null) {
            setPartName(fTrace.getName());
            fEventsTable = createEventsTable(parent, fTrace.getCacheSize());
            fEventsTable.addSelectionChangedListener(this);
            fEventsTable.setTrace(fTrace, true);
            fEventsTable.refreshBookmarks(fFile);

            /* ensure start time is set */
            final ITmfContext context = fTrace.seekEvent(0);
            fTrace.getNext(context);
            context.dispose();

            broadcast(new TmfTraceOpenedSignal(this, fTrace, fFile));
        } else {
            fEventsTable = new TmfEventsTable(parent, 0);
            fEventsTable.addSelectionChangedListener(this);
        }
        IStatusLineManager statusLineManager = getEditorSite().getActionBars().getStatusLineManager();
        fEventsTable.setStatusLineManager(statusLineManager);
        addPropertyListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        // we need to wrap the ISelectionProvider interface in the editor because
        // the events table can be replaced later while the selection changed listener
        // is only added once by the platform to the selection provider set here
        getSite().setSelectionProvider(this);
        getSite().getPage().addPartListener(this);
    }

    @Override
    public void dispose() {
        if (getSite() != null) {
            getSite().getPage().removePartListener(this);
        }
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

    /**
     * Create the events table
     *
     * @param parent the parent composite
     * @param cacheSize the cache size
     * @return an events table instance
     */
    protected TmfEventsTable createEventsTable(final Composite parent, final int cacheSize) {
        TmfEventsTable eventsTable = getEventsTable(parent, cacheSize);
        if (eventsTable == null) {
            eventsTable = new TmfEventsTable(parent, cacheSize);
        }
        return eventsTable;
    }

    private TmfEventsTable getEventsTable(final Composite parent, final int cacheSize) {
        if (fTrace instanceof TmfExperiment) {
            return getExperimentEventsTable((TmfExperiment) fTrace, parent, cacheSize);
        }
        TmfEventsTable eventsTable = null;
        try {
            if (fTrace.getResource() == null) {
                return null;
            }
            final String traceType = fTrace.getResource().getPersistentProperty(TmfCommonConstants.TRACETYPE);
            if (traceType == null) {
                return null;
            }
            if (traceType.startsWith(CustomTxtTrace.class.getCanonicalName())) {
                return new CustomEventsTable(((CustomTxtTrace) fTrace).getDefinition(), parent, cacheSize);
            }
            if (traceType.startsWith(CustomXmlTrace.class.getCanonicalName())) {
                return new CustomEventsTable(((CustomXmlTrace) fTrace).getDefinition(), parent, cacheSize);
            }
            for (final IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(traceType)) {
                    final IConfigurationElement[] eventsTableTypeCE = ce.getChildren(TmfTraceType.EVENTS_TABLE_TYPE_ELEM);
                    if (eventsTableTypeCE.length != 1) {
                        break;
                    }
                    final String eventsTableType = eventsTableTypeCE[0].getAttribute(TmfTraceType.CLASS_ATTR);
                    if ((eventsTableType == null) || (eventsTableType.length() == 0)) {
                        break;
                    }
                    final Bundle bundle = Platform.getBundle(ce.getContributor().getName());
                    final Class<?> c = bundle.loadClass(eventsTableType);
                    final Class<?>[] constructorArgs = new Class[] { Composite.class, int.class };
                    final Constructor<?> constructor = c.getConstructor(constructorArgs);
                    final Object[] args = new Object[] { parent, cacheSize };
                    eventsTable = (TmfEventsTable) constructor.newInstance(args);
                    break;
                }
            }
        } catch (final InvalidRegistryObjectException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable", e); //$NON-NLS-1$
        } catch (final CoreException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable", e); //$NON-NLS-1$
        } catch (final ClassNotFoundException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable", e); //$NON-NLS-1$
        } catch (final SecurityException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable", e); //$NON-NLS-1$
        } catch (final NoSuchMethodException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable", e); //$NON-NLS-1$
        } catch (final IllegalArgumentException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable", e); //$NON-NLS-1$
        } catch (final InstantiationException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable", e); //$NON-NLS-1$
        } catch (final IllegalAccessException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable", e); //$NON-NLS-1$
        } catch (final InvocationTargetException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable", e); //$NON-NLS-1$
        }
        return eventsTable;
    }

    /**
     * Get the events table for an experiment. If all traces in the experiment
     * are of the same type, use the extension point specified event table
     *
     * @param experiment
     *            the experiment
     * @param parent
     *            the parent Composite
     * @param cacheSize
     *            the event table cache size
     * @return an events table of the appropriate type
     */
    private static TmfEventsTable getExperimentEventsTable(
            final TmfExperiment experiment, final Composite parent,
            final int cacheSize) {
        TmfEventsTable eventsTable = null;
        String commonTraceType = null;
        try {
            for (final ITmfTrace trace : experiment.getTraces()) {
                final IResource resource = trace.getResource();
                if (resource == null) {
                    return null;
                }
                final String traceType = resource.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                if ((commonTraceType != null) && !commonTraceType.equals(traceType)) {
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
            for (final IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(commonTraceType)) {
                    final IConfigurationElement[] eventsTableTypeCE = ce.getChildren(TmfTraceType.EVENTS_TABLE_TYPE_ELEM);
                    if (eventsTableTypeCE.length != 1) {
                        break;
                    }
                    final String eventsTableType = eventsTableTypeCE[0].getAttribute(TmfTraceType.CLASS_ATTR);
                    if ((eventsTableType == null) || (eventsTableType.length() == 0)) {
                        break;
                    }
                    final Bundle bundle = Platform.getBundle(ce.getContributor().getName());
                    final Class<?> c = bundle.loadClass(eventsTableType);
                    final Class<?>[] constructorArgs = new Class[] { Composite.class, int.class };
                    final Constructor<?> constructor = c.getConstructor(constructorArgs);
                    final Object[] args = new Object[] { parent, cacheSize };
                    eventsTable = (TmfEventsTable) constructor.newInstance(args);
                    break;
                }
            }
        } catch (final CoreException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable for experiment", e); //$NON-NLS-1$
        } catch (final InvalidRegistryObjectException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable for experiment", e); //$NON-NLS-1$
        } catch (final SecurityException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable for experiment", e); //$NON-NLS-1$
        } catch (final IllegalArgumentException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable for experiment", e); //$NON-NLS-1$
        } catch (final ClassNotFoundException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable for experiment", e); //$NON-NLS-1$
        } catch (final NoSuchMethodException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable for experiment", e); //$NON-NLS-1$
        } catch (final InstantiationException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable for experiment", e); //$NON-NLS-1$
        } catch (final IllegalAccessException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable for experiment", e); //$NON-NLS-1$
        } catch (final InvocationTargetException e) {
            Activator.getDefault().logError("Error getting TmfEventsTable for experiment", e); //$NON-NLS-1$
        }
        return eventsTable;
    }

    @Override
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public IFile getBookmarksFile() {
        return fFile;
    }

    @Override
    public void setFocus() {
        fEventsTable.setFocus();
    }

    @Override
    public Object getAdapter(final Class adapter) {
        if (IGotoMarker.class.equals(adapter)) {
            if (fTrace == null || fEventsTable == null) {
                return this;
            }
            return fEventsTable;
        } else if (IPropertySheetPage.class.equals(adapter)) {
            return new UnsortedPropertySheetPage();
        }
        return super.getAdapter(adapter);
    }

    /**
     * @since 2.1
     */
    @Override
    public void gotoMarker(IMarker marker) {
        if (fTrace == null || fEventsTable == null) {
            fPendingGotoMarker = marker;
        } else {
            fEventsTable.gotoMarker(marker);
        }
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        for (final IMarkerDelta delta : event.findMarkerDeltas(IMarker.BOOKMARK, false)) {
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
    // ISelectionProvider
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        fSelectionChangedListeners.add(listener);
    }

    /**
     * @since 2.0
     */
    @Override
    public ISelection getSelection() {
        if (fEventsTable == null) {
            return StructuredSelection.EMPTY;
        }
        return fEventsTable.getSelection();
    }

    /**
     * @since 2.0
     */
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        fSelectionChangedListeners.remove(listener);
    }

    /**
     * @since 2.0
     */
    @Override
    public void setSelection(ISelection selection) {
        // not implemented
    }

    /**
     * Notifies any selection changed listeners that the viewer's selection has changed.
     * Only listeners registered at the time this method is called are notified.
     *
     * @param event a selection changed event
     *
     * @see ISelectionChangedListener#selectionChanged
     * @since 2.0
     */
    protected void fireSelectionChanged(final SelectionChangedEvent event) {
        Object[] listeners = fSelectionChangedListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }

    // ------------------------------------------------------------------------
    // ISelectionChangedListener
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        fireSelectionChanged(event);
    }

    // ------------------------------------------------------------------------
    // IPartListener
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public void partActivated(IWorkbenchPart part) {
        if (part == this && fTrace != null) {
            if (fTraceSelected) {
                return;
            }
            fTraceSelected = true;
            broadcast(new TmfTraceSelectedSignal(this, fTrace));
        }
    }

    /**
     * @since 2.0
     */
    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
        if (part == this && fTrace != null) {
            if (fTraceSelected) {
                return;
            }
            fTraceSelected = true;
            broadcast(new TmfTraceSelectedSignal(this, fTrace));
        }
    }

    /**
     * @since 2.0
     */
    @Override
    public void partClosed(IWorkbenchPart part) {
    }

    /**
     * @since 2.0
     */
    @Override
    public void partDeactivated(IWorkbenchPart part) {
    }

    /**
     * @since 2.0
     */
    @Override
    public void partOpened(IWorkbenchPart part) {
    }

    // ------------------------------------------------------------------------
    // Global commands
    // ------------------------------------------------------------------------

    /**
     * Add a bookmark
     */
    public void addBookmark() {
        fEventsTable.addBookmark(fFile);
    }


    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the Trace Selected signal
     *
     * @param signal The incoming signal
     */
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        if ((signal.getSource() != this)) {
            if (signal.getTrace().equals(fTrace)) {
                getSite().getPage().bringToTop(this);
            } else {
                fTraceSelected = false;
            }
        }
    }

    /**
     * Update the display to use the updated timestamp format
     *
     * @param signal the incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        if (fEventsTable != null) {
            fEventsTable.refresh();
        }
    }

}
