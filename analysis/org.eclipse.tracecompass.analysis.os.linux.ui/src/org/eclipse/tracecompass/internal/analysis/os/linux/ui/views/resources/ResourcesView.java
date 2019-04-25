/*******************************************************************************
 * Copyright (c) 2012, 2018 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph views
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfCpuSelectedSignal;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfThreadSelectedSignal;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel.Type;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesStatusDataProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.FollowCpuAction;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.FollowThreadAction;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.UnfollowCpuAction;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.UnfollowThreadAction;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NamedTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.ui.IWorkbenchActionConstants;

import com.google.common.collect.Multimap;

/**
 * Main implementation for the LTTng 2.0 kernel Resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesView extends BaseDataProviderTimeGraphView {

    /** View ID. */
    public static final @NonNull String ID = "org.eclipse.tracecompass.analysis.os.linux.views.resources"; //$NON-NLS-1$

    /** ID of the followed CPU in the map data in {@link TmfTraceContext} */
    public static final @NonNull String RESOURCES_FOLLOW_CPU = ID + ".FOLLOW_CPU"; //$NON-NLS-1$

    /**
     * ID of the followed Current Thread in the map data in
     * {@link TmfTraceContext}
     *
     * @deprecated Selected thread should be matched with its host. Use
     *             {@link HostThread#SELECTED_HOST_THREAD_KEY} instead, with a
     *             value of type {@link HostThread}
     */
    @Deprecated
    public static final @NonNull String RESOURCES_FOLLOW_CURRENT_THREAD = ID + ".FOLLOW_CURRENT_THREAD"; //$NON-NLS-1$

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private String fFollowedThread = EMPTY_STRING;

    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            Messages.ResourcesView_stateTypeName
    };

    /**
     * CPU group entries (Current Thread, CPU, Frequency, separator) first, then IRQ, then SoftIRQ.
     * Numerical order second, then CPU group entries in their correct order.
     */
    private static final @NonNull List<Type> CPU_GROUP_ORDER = Arrays.asList(Type.CURRENT_THREAD, Type.CPU, Type.FREQUENCY, Type.GROUP);

    private static final Comparator<ResourcesEntryModel> COMPARATOR = Comparator
            .comparing((Function<ResourcesEntryModel, Type>) entry -> CPU_GROUP_ORDER.contains(entry.getType()) ? Type.GROUP : entry.getType())
            .thenComparing(ResourcesEntryModel::getResourceId)
            .thenComparing(entry -> CPU_GROUP_ORDER.indexOf(entry.getType()));

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public ResourcesView() {
        super(ID, new ResourcesPresentationProvider(), ResourcesStatusDataProvider.ID);
        setFilterColumns(FILTER_COLUMN_NAMES);
        setFilterLabelProvider(new ResourcesFilterLabelProvider());
        setEntryComparator(new ResourcesEntryComparator());
        setAutoExpandLevel(1);
    }

    private static class ResourcesEntryComparator implements Comparator<ITimeGraphEntry> {
        @Override
        public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
            if (o1 instanceof TraceEntry && o2 instanceof TraceEntry) {
                /* sort trace entries alphabetically */
                return o1.getName().compareTo(o2.getName());
            } else if (o1 instanceof TimeGraphEntry && o2 instanceof TimeGraphEntry) {
                ITmfTreeDataModel model1 = ((TimeGraphEntry) o1).getEntryModel();
                ITmfTreeDataModel model2 = ((TimeGraphEntry) o2).getEntryModel();
                /* sort resource entries by their defined order */
                if (model1 instanceof ResourcesEntryModel && model2 instanceof ResourcesEntryModel) {
                    return COMPARATOR.compare((ResourcesEntryModel) model1, (ResourcesEntryModel) model2);
                }
            }
            return 0;
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        createTimeEventContextMenu();
    }

    private void createTimeEventContextMenu() {
        MenuManager eventMenuManager = new MenuManager();
        eventMenuManager.setRemoveAllWhenShown(true);
        TimeGraphControl timeGraphControl = getTimeGraphViewer().getTimeGraphControl();
        final Menu timeEventMenu = eventMenuManager.createContextMenu(timeGraphControl);

        timeGraphControl.addTimeEventMenuListener(event -> {
            Menu menu = timeEventMenu;
            if (event.data instanceof TimeEvent) {
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
        getSite().registerContextMenu(eventMenuManager, getTimeGraphViewer().getSelectionProvider());
    }

    /**
     * @since 2.0
     */
    @Override
    protected void fillTimeGraphEntryContextMenu(@NonNull IMenuManager menuManager) {
        ISelection selection = getSite().getSelectionProvider().getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection sSel = (IStructuredSelection) selection;
            if (sSel.getFirstElement() instanceof TimeGraphEntry) {
                TimeGraphEntry resourcesEntry = (TimeGraphEntry) sSel.getFirstElement();
                ITmfTreeDataModel model = resourcesEntry.getEntryModel();
                if (model instanceof ResourcesEntryModel) {
                    ResourcesEntryModel resourcesModel = (ResourcesEntryModel) model;
                    Type type = resourcesModel.getType();
                    if (type == Type.CPU || type == Type.CURRENT_THREAD) {
                        ITmfTrace trace = getTrace(resourcesEntry);
                        TmfTraceContext ctx = TmfTraceManager.getInstance().getTraceContext(trace);
                        Integer data = (Integer) ctx.getData(RESOURCES_FOLLOW_CPU);
                        int cpu = data != null ? data.intValue() : -1;
                        if (cpu >= 0) {
                            menuManager.add(new UnfollowCpuAction(ResourcesView.this, resourcesModel.getResourceId(), trace));
                        } else {
                            menuManager.add(new FollowCpuAction(ResourcesView.this, resourcesModel.getResourceId(), trace));
                        }
                    }
                }
            }
        }
    }

    /**
     * Fill context menu
     *
     * @param menuManager
     *                        a menuManager to fill
     */
    protected void fillTimeEventContextMenu(@NonNull IMenuManager menuManager) {
        ISelection selection = getSite().getSelectionProvider().getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection sSel = (IStructuredSelection) selection;
            if (sSel.getFirstElement() instanceof TimeGraphEntry) {
                TimeGraphEntry resourcesEntry = (TimeGraphEntry) sSel.getFirstElement();
                ITmfTreeDataModel model = resourcesEntry.getEntryModel();
                if (sSel.toArray()[1] instanceof NamedTimeEvent && ((ResourcesEntryModel) model).getType() == Type.CURRENT_THREAD) {
                    ITmfTrace trace = getTrace(resourcesEntry);
                    NamedTimeEvent event = (NamedTimeEvent) sSel.toArray()[1];
                    TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
                    HostThread data = (HostThread) ctx.getData(HostThread.SELECTED_HOST_THREAD_KEY);
                    if (data != null) {
                        menuManager.add(new UnfollowThreadAction(ResourcesView.this));
                    } else {
                        menuManager.add(new FollowThreadAction(ResourcesView.this, null, event.getValue(), trace));
                    }
                }
            }
        }
    }

    private static class ResourcesFilterLabelProvider extends TreeLabelProvider {
        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0 && element instanceof TimeGraphEntry) {
                return ((TimeGraphEntry) element).getName();
            }
            return ""; //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected String getNextText() {
        return Messages.ResourcesView_nextResourceActionNameText;
    }

    @Override
    protected String getNextTooltip() {
        return Messages.ResourcesView_nextResourceActionToolTipText;
    }

    @Override
    protected String getPrevText() {
        return Messages.ResourcesView_previousResourceActionNameText;
    }

    @Override
    protected String getPrevTooltip() {
        return Messages.ResourcesView_previousResourceActionToolTipText;
    }

    private void setFollowedThread(String regex) {
        fFollowedThread = regex;
    }

    private void removeFollowedThread() {
        fFollowedThread = EMPTY_STRING;
    }

    @Override
    protected @NonNull Multimap<@NonNull Integer, @NonNull String> getRegexes() {
        Multimap<@NonNull Integer, @NonNull String> regexes = super.getRegexes();
        if (!fFollowedThread.isEmpty()) {
            regexes.put(IFilterProperty.BOUND, fFollowedThread);
        } else {
            regexes.removeAll(IFilterProperty.BOUND);
        }
        return regexes;
    }

    /**
     * Signal handler for a cpu selected signal.
     *
     * @param signal
     *            the cpu selected signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void listenToCpu(TmfCpuSelectedSignal signal) {
        int data = signal.getCore() >= 0 ? signal.getCore() : -1;
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return;
        }
        TmfTraceManager.getInstance().updateTraceContext(trace,
                builder -> builder.setData(RESOURCES_FOLLOW_CPU, data));
    }

    /**
     * Signal handler for a thread selected signal.
     *
     * @param signal
     *            the thread selected signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void listenToCurrentThread(TmfThreadSelectedSignal signal) {
        HostThread data = signal.getThreadId() >= 0 ? signal.getHostThread() : null;
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return;
        }

        TmfTraceManager.getInstance().updateTraceContext(trace,
                builder -> builder.setData(HostThread.SELECTED_HOST_THREAD_KEY, data));
        if (data != null) {
            setFollowedThread("Current_thread==" + data.getTid() + " || TID==" + data.getTid()); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            removeFollowedThread();
        }
        restartZoomThread();
    }
}
