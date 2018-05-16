/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Author:
 *     Sonia Farrah
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.flamegraph;

import java.util.Collection;
import java.util.concurrent.Semaphore;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tracecompass.analysis.profiling.ui.symbols.TmfSymbolProviderUpdatedSignal;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.CallGraphAnalysis;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.ThreadNode;
import org.eclipse.tracecompass.internal.analysis.timing.ui.Activator;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.views.SaveImageUtil;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;

import com.google.common.annotations.VisibleForTesting;

/**
 * View to display the flame graph .This uses the flameGraphNode tree generated
 * by CallGraphAnalysisUI.
 *
 * @author Sonia Farrah
 */
public class FlameGraphView extends TmfView {

    /**
     *
     */
    public static final String ID = FlameGraphView.class.getPackage().getName() + ".flamegraphView"; //$NON-NLS-1$

    private static final String SORT_OPTION_KEY = "sort.option"; //$NON-NLS-1$
    private static final String CONTENT_PRESENTATION_OPTION_KEY = "presentation.option"; //$NON-NLS-1$
    private static final ImageDescriptor SORT_BY_NAME_ICON = Activator.getDefault().getImageDescripterFromPath("icons/etool16/sort_alpha.gif"); //$NON-NLS-1$
    private static final ImageDescriptor SORT_BY_NAME_REV_ICON = Activator.getDefault().getImageDescripterFromPath("icons/etool16/sort_alpha_rev.gif"); //$NON-NLS-1$
    private static final ImageDescriptor SORT_BY_ID_ICON = Activator.getDefault().getImageDescripterFromPath("icons/etool16/sort_num.gif"); //$NON-NLS-1$
    private static final ImageDescriptor SORT_BY_ID_REV_ICON = Activator.getDefault().getImageDescripterFromPath("icons/etool16/sort_num_rev.gif"); //$NON-NLS-1$

    private final Action VIEW_BY_THREAD = new Action(Messages.FlameGraph_ShowPerThreads, IAction.AS_RADIO_BUTTON) {
        @Override
        public void run() {
            if (fContentPresentation != ContentPresentation.BY_THREAD) {
                buildFlameGraph(fFlamegraphModule);
                fContentPresentation = ContentPresentation.BY_THREAD;
                saveContentPresentationOption(fContentPresentation);
            }
        }
    };

    private final Action VIEW_AGGREGATE = new Action(Messages.FlameGraph_AggregateByThread, IAction.AS_RADIO_BUTTON) {
        @Override
        public void run() {
            if (fContentPresentation != ContentPresentation.AGGREGATE_THREADS) {
                buildFlameGraph(fFlamegraphModule);
                fContentPresentation = ContentPresentation.AGGREGATE_THREADS;
                saveContentPresentationOption(fContentPresentation);
            }
        }
    };

    private volatile ContentPresentation fContentPresentation = ContentPresentation.AGGREGATE_THREADS;

    private TimeGraphViewer fTimeGraphViewer;

    private FlameGraphContentProvider fTimeGraphContentProvider;

    private ITmfTrace fTrace;

    /**
     * A plain old semaphore is used since different threads will be competing
     * for the same resource.
     */
    private final Semaphore fLock = new Semaphore(1);

    private Action fSortByNameAction;
    private Action fSortByIdAction;
    private Job fJob;

    private CallGraphAnalysis fFlamegraphModule = null;

    /**
     * Constructor
     */
    public FlameGraphView() {
        super(ID);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        fTimeGraphViewer = new TimeGraphViewer(parent, SWT.NONE);
        FlameGraphContentProvider timeGraphContentProvider = new FlameGraphContentProvider();
        fTimeGraphContentProvider = timeGraphContentProvider;
        fTimeGraphViewer.setTimeGraphContentProvider(timeGraphContentProvider);
        fTimeGraphViewer.setTimeGraphProvider(new FlameGraphPresentationProvider());
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
        contributeToActionBars();
        loadSortOption();
        loadContentPresentationOption();
        TmfSignalManager.register(this);
        getSite().setSelectionProvider(fTimeGraphViewer.getSelectionProvider());
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        MenuManager item = new MenuManager(Messages.FlameGraphView_ContentPresentation);
        item.setRemoveAllWhenShown(true);
        item.addMenuListener(manager -> {
            item.add(VIEW_BY_THREAD);
            item.add(VIEW_AGGREGATE);
        });
        menuManager.add(item);
        createTimeEventContextMenu();
        fTimeGraphViewer.getTimeGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                TimeGraphControl timeGraphControl = getTimeGraphViewer().getTimeGraphControl();
                ISelection selection = timeGraphControl.getSelection();
                if (selection instanceof IStructuredSelection) {
                    for (Object object : ((IStructuredSelection) selection).toList()) {
                        if (object instanceof FlamegraphEvent) {
                            FlamegraphEvent event = (FlamegraphEvent) object;
                            long startTime = event.getTime();
                            long endTime = startTime + event.getDuration();
                            getTimeGraphViewer().setStartFinishTimeNotify(startTime, endTime);
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * Get the time graph viewer
     *
     * @return the time graph viewer
     */
    @VisibleForTesting
    public TimeGraphViewer getTimeGraphViewer() {
        return fTimeGraphViewer;
    }

    /**
     * Handler for the trace selected signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        fTrace = signal.getTrace();
        if (fTrace != null) {
            fFlamegraphModule = TmfTraceUtils.getAnalysisModuleOfClass(fTrace, CallGraphAnalysis.class, CallGraphAnalysis.ID);
            buildFlameGraph(fFlamegraphModule);
        }
    }

    /**
     * Get the necessary data for the flame graph and display it
     *
     * @param callGraphAnalysis
     *            the callGraphAnalysis
     */
    @VisibleForTesting
    public void buildFlameGraph(CallGraphAnalysis callGraphAnalysis) {
        /*
         * Note for synchronization:
         *
         * Acquire the lock at entry. then we have 4 places to release it
         *
         * 1- if the lock failed
         *
         * 2- if the data is null and we have no UI to update
         *
         * 3- if the request is cancelled before it gets to the display
         *
         * 4- on a clean execution
         */
        Job job = fJob;
        if (job != null) {
            job.cancel();
        }
        try {
            fLock.acquire();
        } catch (InterruptedException e) {
            Activator.getDefault().logError(e.getMessage(), e);
            fLock.release();
        }
        if (callGraphAnalysis == null) {
            fTimeGraphViewer.setInput(null);
            fLock.release();
            return;
        }
        callGraphAnalysis.schedule();
        job = new Job(Messages.CallGraphAnalysis_Execution) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    callGraphAnalysis.waitForCompletion(monitor);
                    // compute input outside of display thread.
                    Collection<ThreadNode> input = fContentPresentation == ContentPresentation.BY_THREAD ? callGraphAnalysis.getThreadNodes() : callGraphAnalysis.getFlameGraph();
                    Display.getDefault().asyncExec(() -> {
                        fTimeGraphViewer.setInput(input);
                        fTimeGraphViewer.resetStartFinishTime();
                    });
                    return Status.OK_STATUS;
                } finally {
                    fJob = null;
                    fLock.release();
                }
            }
        };
        fJob = job;
        job.schedule();
    }

    /**
     * Await the next refresh
     *
     * @throws InterruptedException
     *             something took too long
     */
    @VisibleForTesting
    public void waitForUpdate() throws InterruptedException {
        /*
         * wait for the semaphore to be available, then release it immediately
         */
        fLock.acquire();
        fLock.release();
    }

    /**
     * Trace is closed: clear the data structures and the view
     *
     * @param signal
     *            the signal received
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        if (signal.getTrace() == fTrace) {
            fTimeGraphViewer.setInput(null);
        }
    }

    @Override
    public void setFocus() {
        fTimeGraphViewer.setFocus();
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private void createTimeEventContextMenu() {
        MenuManager eventMenuManager = new MenuManager();
        eventMenuManager.setRemoveAllWhenShown(true);
        TimeGraphControl timeGraphControl = fTimeGraphViewer.getTimeGraphControl();
        final Menu timeEventMenu = eventMenuManager.createContextMenu(timeGraphControl);

        timeGraphControl.addTimeGraphEntryMenuListener(event -> {
            /*
             * The TimeGraphControl will call the TimeGraphEntryMenuListener
             * before the TimeEventMenuListener. We need to clear the menu
             * for the case the selection was done on the namespace where
             * the time event listener below won't be called afterwards.
             */
            timeGraphControl.setMenu(null);
            event.doit = false;
        });
        timeGraphControl.addTimeEventMenuListener(event -> {
            Menu menu = timeEventMenu;
            if (event.data instanceof FlamegraphEvent) {
                timeGraphControl.setMenu(menu);
                return;
            }
            timeGraphControl.setMenu(null);
            event.doit = false;
        });

        eventMenuManager.addMenuListener(manager -> {
            fillTimeEventContextMenu(eventMenuManager);
            eventMenuManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        });
        getSite().registerContextMenu(eventMenuManager, fTimeGraphViewer.getSelectionProvider());
    }

    /**
     * Fill context menu
     *
     * @param menuManager
     *            a menuManager to fill
     */
    protected void fillTimeEventContextMenu(@NonNull IMenuManager menuManager) {
        ISelection selection = getSite().getSelectionProvider().getSelection();
        if (selection instanceof IStructuredSelection) {
            for (Object object : ((IStructuredSelection) selection).toList()) {
                if (object instanceof FlamegraphEvent) {
                    final FlamegraphEvent flamegraphEvent = (FlamegraphEvent) object;
                    menuManager.add(new Action(Messages.FlameGraphView_GotoMaxDuration) {
                        @Override
                        public void run() {
                            ISegment maxSeg = flamegraphEvent.getStatistics().getDurationStatistics().getMaxObject();
                            if (maxSeg == null) {
                                return;
                            }
                            TmfSelectionRangeUpdatedSignal sig = new TmfSelectionRangeUpdatedSignal(this, TmfTimestamp.fromNanos(maxSeg.getStart()), TmfTimestamp.fromNanos(maxSeg.getEnd()), fTrace);
                            broadcast(sig);
                        }
                    });

                    menuManager.add(new Action(Messages.FlameGraphView_GotoMinDuration) {
                        @Override
                        public void run() {
                            ISegment minSeg = flamegraphEvent.getStatistics().getDurationStatistics().getMinObject();
                            if (minSeg == null) {
                                return;
                            }
                            TmfSelectionRangeUpdatedSignal sig = new TmfSelectionRangeUpdatedSignal(this, TmfTimestamp.fromNanos(minSeg.getStart()), TmfTimestamp.fromNanos(minSeg.getEnd()), fTrace);
                            broadcast(sig);
                        }
                    });
                }
            }
        }
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(getSortByNameAction());
        manager.add(getSortByIdAction());
        manager.add(new Separator());
    }

    private Action getSortByNameAction() {
        if (fSortByNameAction == null) {
            fSortByNameAction = new Action(Messages.FlameGraph_SortByThreadName, IAction.AS_CHECK_BOX) {
                @Override
                public void run() {
                    SortOption sortOption = fTimeGraphContentProvider.getSortOption();
                    if (sortOption == SortOption.BY_NAME) {
                        setSortOption(SortOption.BY_NAME_REV);
                    } else {
                        setSortOption(SortOption.BY_NAME);
                    }
                }
            };
            fSortByNameAction.setToolTipText(Messages.FlameGraph_SortByThreadName);
            fSortByNameAction.setImageDescriptor(SORT_BY_NAME_ICON);
        }
        return fSortByNameAction;
    }

    private Action getSortByIdAction() {
        if (fSortByIdAction == null) {
            fSortByIdAction = new Action(Messages.FlameGraph_SortByThreadId, IAction.AS_CHECK_BOX) {
                @Override
                public void run() {
                    SortOption sortOption = fTimeGraphContentProvider.getSortOption();
                    if (sortOption == SortOption.BY_ID) {
                        setSortOption(SortOption.BY_ID_REV);
                    } else {
                        setSortOption(SortOption.BY_ID);
                    }
                }
            };
            fSortByIdAction.setToolTipText(Messages.FlameGraph_SortByThreadId);
            fSortByIdAction.setImageDescriptor(SORT_BY_ID_ICON);
        }
        return fSortByIdAction;
    }

    private void setSortOption(SortOption sortOption) {
        // reset defaults
        getSortByNameAction().setChecked(false);
        getSortByNameAction().setImageDescriptor(SORT_BY_NAME_ICON);
        getSortByIdAction().setChecked(false);
        getSortByIdAction().setImageDescriptor(SORT_BY_ID_ICON);

        if (sortOption.equals(SortOption.BY_NAME)) {
            getSortByNameAction().setChecked(true);
        } else if (sortOption.equals(SortOption.BY_NAME_REV)) {
            getSortByNameAction().setChecked(true);
            getSortByNameAction().setImageDescriptor(SORT_BY_NAME_REV_ICON);
        } else if (sortOption.equals(SortOption.BY_ID)) {
            getSortByIdAction().setChecked(true);
        } else if (sortOption.equals(SortOption.BY_ID_REV)) {
            getSortByIdAction().setChecked(true);
            getSortByIdAction().setImageDescriptor(SORT_BY_ID_REV_ICON);
        }
        fTimeGraphContentProvider.setSortOption(sortOption);
        saveSortOption();
        fTimeGraphViewer.refresh();
    }

    private void saveSortOption() {
        SortOption sortOption = fTimeGraphContentProvider.getSortOption();
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(getClass().getName());
        if (section == null) {
            section = settings.addNewSection(getClass().getName());
        }
        section.put(SORT_OPTION_KEY, sortOption.name());
    }

    private void loadSortOption() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(getClass().getName());
        if (section == null) {
            return;
        }
        String sortOption = section.get(SORT_OPTION_KEY);
        if (sortOption == null) {
            return;
        }
        setSortOption(SortOption.fromName(sortOption));
    }

    private void saveContentPresentationOption(ContentPresentation contentPresentation) {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(getClass().getName());
        if (section == null) {
            section = settings.addNewSection(getClass().getName());
        }
        section.put(CONTENT_PRESENTATION_OPTION_KEY, contentPresentation.name());
    }

    private void loadContentPresentationOption() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(getClass().getName());
        ContentPresentation contentPresentation = fContentPresentation;
        if (section != null) {
            String contentPresentationOption = section.get(CONTENT_PRESENTATION_OPTION_KEY);
            if (contentPresentationOption != null) {
                contentPresentation = ContentPresentation.fromName(contentPresentationOption);
            }
        }
        VIEW_BY_THREAD.setChecked(contentPresentation == ContentPresentation.BY_THREAD);
        VIEW_AGGREGATE.setChecked(contentPresentation == ContentPresentation.AGGREGATE_THREADS);
        fContentPresentation = contentPresentation;
    }

    /**
     * Symbol map provider updated
     *
     * @param signal
     *            the signal
     */
    @TmfSignalHandler
    public void symbolMapUpdated(TmfSymbolProviderUpdatedSignal signal) {
        if (signal.getSource() != this) {
            fTimeGraphViewer.refresh();
        }
    }

    @Override
    protected @Nullable IAction createSaveAction() {
        return SaveImageUtil.createSaveAction(getName(), this::getTimeGraphViewer);
    }

}
