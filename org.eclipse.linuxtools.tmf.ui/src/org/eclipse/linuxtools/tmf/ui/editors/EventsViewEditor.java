/*******************************************************************************
* Copyright (c) 2012 Ericsson
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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.linuxtools.internal.tmf.ui.project.handlers.Messages;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.views.events.TmfEventsView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.EditorPart;

/**
 *
 * This editor is used to open a trace in the Events view
 * and set the trace as the current experiment.
 * It intercepts the IGotoMarker adapter and dispatches
 * the handling to the Events view' events table.
 * The editor then closes itself and hides the
 * editor area if no other editor is open.
 *
 */
public class EventsViewEditor extends TmfEditor {

    /**
     * The editor ID
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.editors.eventsView"; //$NON-NLS-1$

    private IFile fFile;
    @SuppressWarnings("rawtypes")
    private ITmfTrace fTrace;
    private IMarker fGotoMarker;
    private boolean fEditorAreaVisible;

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        fEditorAreaVisible = site.getPage().isEditorAreaVisible();
        if (input instanceof TmfEditorInput) {
            fFile = ((TmfEditorInput) input).getFile();
            fTrace = ((TmfEditorInput) input).getTrace();
        } else if (input instanceof IFileEditorInput) {
            fFile = ((IFileEditorInput) input).getFile();
            if (fFile == null) {
                throw new PartInitException("Invalid IFileEditorInput: " + input); //$NON-NLS-1$
            }
            TmfExperiment currentExperiment = TmfExperiment.getCurrentExperiment();
            if ((currentExperiment != null) && fFile.equals(currentExperiment.getBookmarksFile())) {
                fTrace = currentExperiment;
                super.setSite(site);
                super.setInput(input);
                return;
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
                                ITmfEvent traceEvent = traceElement.instantiateEvent();
                                if ((trace == null) || (traceEvent == null)) {
                                    for (int j = 0; j < i; j++) {
                                        traces[j].dispose();
                                    }
                                    throw new PartInitException(Messages.OpenExperimentHandler_NoTraceType);
                                }
                                try {
                                    trace.initTrace(traceElement.getName(), traceElement.getLocation().getPath(), traceEvent.getClass());
                                } catch (FileNotFoundException e) {
                                }
                                trace.setResource(traceElement.getResource());
                                cacheSize = Math.min(cacheSize, trace.getIndexPageSize());
                                traces[i] = trace;
                            }
                            TmfExperiment experiment = new TmfExperiment(TmfEvent.class, experimentElement.getName(), traces, cacheSize);
                            experiment.setBookmarksFile(fFile);
                            fTrace = experiment;
                            TmfExperiment.setCurrentExperiment(experiment);
                            TmfSignalManager.dispatchSignal(new TmfExperimentSelectedSignal(this, experiment));
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
                            // Instantiate the experiment trace
                            ITmfTrace trace = traceElement.instantiateTrace();
                            ITmfEvent traceEvent = traceElement.instantiateEvent();
                            if ((trace == null) || (traceEvent == null)) {
                                throw new PartInitException(Messages.OpenTraceHandler_NoTraceType);
                            }
                            try {
                                trace.initTrace(traceElement.getName(), traceElement.getLocation().getPath(), traceEvent.getClass());
                            } catch (FileNotFoundException e) {
                            }
                            trace.setResource(traceElement.getResource());
                            ITmfTrace[] traces = new ITmfTrace[] { trace };
                            TmfExperiment experiment = new TmfExperiment(traceEvent.getClass(), traceElement.getName(), traces, trace.getIndexPageSize());
                            experiment.setBookmarksFile(fFile);
                            fTrace = experiment;
                            TmfExperiment.setCurrentExperiment(experiment);
                            TmfSignalManager.dispatchSignal(new TmfExperimentSelectedSignal(this, experiment));
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
                            // Instantiate the experiment trace
                            ITmfTrace trace = traceElement.instantiateTrace();
                            ITmfEvent traceEvent = traceElement.instantiateEvent();
                            if ((trace == null) || (traceEvent == null)) {
                                throw new PartInitException(Messages.OpenTraceHandler_NoTraceType);
                            }
                            try {
                                trace.initTrace(traceElement.getName(), traceElement.getLocation().getPath(), traceEvent.getClass());
                                trace.indexTrace(false);
                            } catch (FileNotFoundException e) {
                            }
                            if (trace instanceof TmfTrace) {
                                ((TmfTrace) trace).setResource(traceElement.getResource());
                            }
                            ITmfTrace[] traces = new ITmfTrace[] { trace };
                            TmfExperiment experiment = new TmfExperiment(traceEvent.getClass(), traceElement.getName(), traces, trace.getIndexPageSize());
                            experiment.setBookmarksFile(fFile);
                            fTrace = experiment;
                            TmfExperiment.setCurrentExperiment(experiment);
                            TmfSignalManager.dispatchSignal(new TmfExperimentSelectedSignal(this, experiment));
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
            input = new TmfEditorInput(fFile, fTrace);
        } else {
            throw new PartInitException("Invalid IEditorInput: " + input.getClass()); //$NON-NLS-1$
        }
        if (fTrace == null) {
            throw new PartInitException("Invalid IEditorInput: " + fFile.getName()); //$NON-NLS-1$
        }
        super.setSite(site);
        super.setInput(input);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
    public boolean isDirty() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        setPartName(getEditorInput().getName());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                EditorPart editorPart = EventsViewEditor.this;
                IWorkbenchPage page = editorPart.getEditorSite().getPage();
                page.closeEditor(editorPart, false);
                if (page.getEditorReferences().length == 0) {
                    page.setEditorAreaVisible(fEditorAreaVisible);
                }
                try {
                    IViewPart viewPart = page.showView(TmfEventsView.ID);
                    if (fGotoMarker != null) {
                        IGotoMarker adapter = (IGotoMarker) viewPart.getAdapter(IGotoMarker.class);
                        if (adapter != null) {
                            adapter.gotoMarker(fGotoMarker);
                        }
                    }
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (IGotoMarker.class.equals(adapter)) {
            return new IGotoMarker() {
                @Override
                public void gotoMarker(IMarker marker) {
                    fGotoMarker = marker;
                }
            };
        }
        return super.getAdapter(adapter);
    }

}
