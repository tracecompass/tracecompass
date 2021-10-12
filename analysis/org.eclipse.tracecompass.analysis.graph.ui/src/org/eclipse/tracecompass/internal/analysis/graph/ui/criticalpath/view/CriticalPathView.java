/*******************************************************************************
 * Copyright (c) 2015, 2018 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.ui.criticalpath.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathModule;
import org.eclipse.tracecompass.internal.analysis.graph.core.dataprovider.CriticalPathDataProvider;
import org.eclipse.tracecompass.internal.analysis.graph.core.dataprovider.CriticalPathEntry;
import org.eclipse.tracecompass.internal.analysis.graph.ui.Activator;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph.BaseDataProviderTimeGraphPresentationProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartAnalysisSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeLinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.ui.IWorkbenchActionConstants;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

/**
 * The Critical Path view
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public class CriticalPathView extends BaseDataProviderTimeGraphView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** View ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.analysis.graph.ui.criticalpath.view.criticalpathview"; //$NON-NLS-1$

    private static final double NANOINV = 10E-9;

    private static final String COLUMN_PROCESS = Messages.getMessage(Messages.CriticalFlowView_columnProcess);
    private static final String COLUMN_ELAPSED = Messages.getMessage(Messages.CriticalFlowView_columnElapsed);
    private static final String COLUMN_PERCENT = Messages.getMessage(Messages.CriticalFlowView_columnPercent);

    private static final String[] COLUMN_NAMES = new String[] {
            COLUMN_PROCESS,
            COLUMN_ELAPSED,
            COLUMN_PERCENT
    };

    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            COLUMN_PROCESS
    };

    private Phaser fPhaser = new Phaser();

    private class CriticalPathTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(@Nullable Object element, int columnIndex) {
            if (!(element instanceof TimeGraphEntry)) {
                return StringUtils.EMPTY;
            }
            TimeGraphEntry entry = (TimeGraphEntry) element;
            ITmfTreeDataModel model = entry.getEntryModel();
            if (columnIndex == 0) {
                return entry.getName();
            } else if (columnIndex == 1 && model instanceof CriticalPathEntry) {
                return String.format("%.9f", ((CriticalPathEntry) model).getSum() * NANOINV); //$NON-NLS-1$
            } else if (columnIndex == 2 && model instanceof CriticalPathEntry) {
                double percent = ((CriticalPathEntry) model).getPercent();
                return String.format("%.2f", percent * 100); //$NON-NLS-1$
            }
            return StringUtils.EMPTY;
        }
    }

    private class CriticalPathEntryComparator implements Comparator<ITimeGraphEntry> {

        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            if ((o1 instanceof TimeGraphEntry) && (o2 instanceof TimeGraphEntry)) {
                ITmfTreeDataModel model1 = ((TimeGraphEntry) o1).getEntryModel();
                ITmfTreeDataModel model2 = ((TimeGraphEntry) o2).getEntryModel();
                if (model1 instanceof CriticalPathEntry && model2 instanceof CriticalPathEntry) {
                    return Long.compare(((CriticalPathEntry) model2).getSum(), ((CriticalPathEntry) model1).getSum());
                }
            }
            return 0;
        }
    }

    /**
     * Constructor
     */
    public CriticalPathView() {
        super(ID, new BaseDataProviderTimeGraphPresentationProvider(), CriticalPathDataProvider.ID);
        setTreeColumns(COLUMN_NAMES);
        setFilterColumns(FILTER_COLUMN_NAMES);
        setTreeLabelProvider(new CriticalPathTreeLabelProvider());
        setEntryComparator(new CriticalPathEntryComparator());
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected List<ITimeEvent> createTimeEvents(TimeGraphEntry entry, List<ITimeGraphState> values) {
        // need to override to not have transparent events in the gaps.
        return Lists.transform(values, state -> createTimeEvent(entry, state));
    }

    @Override
    protected List<ILinkEvent> getLinkList(long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        List<@NonNull TimeGraphEntry> traceEntries = getEntryList(getTrace());
        if (traceEntries == null) {
            return Collections.emptyList();
        }
        List<@NonNull ILinkEvent> linkList = new ArrayList<>();
        TimeQueryFilter queryFilter = new TimeQueryFilter(startTime, endTime, 2);

        /*
         * group entries by critical path data provider as several hosts may refer to
         * the same data provider
         */
        Table<ITimeGraphDataProvider<?>, Long, TimeGraphEntry> table = HashBasedTable.create();
        for (TraceEntry traceEntry : Iterables.filter(traceEntries, TraceEntry.class)) {
            for (TimeGraphEntry entry : Utils.flatten(traceEntry)) {
                table.put(traceEntry.getProvider(), entry.getEntryModel().getId(), entry);
            }
        }

        for (Map.Entry<ITimeGraphDataProvider<?>, Map<Long, TimeGraphEntry>> entry : table.rowMap().entrySet()) {
            ITimeGraphDataProvider<?> provider = entry.getKey();
            Map<Long, TimeGraphEntry> map = entry.getValue();
            TmfModelResponse<List<ITimeGraphArrow>> response = provider.fetchArrows(FetchParametersUtils.timeQueryToMap(queryFilter), monitor);
            List<ITimeGraphArrow> model = response.getModel();

            if (monitor.isCanceled()) {
                return null;
            }
            if (model != null) {
                for (ITimeGraphArrow arrow : model) {
                    ITimeGraphEntry src = map.get(arrow.getSourceId());
                    ITimeGraphEntry dst = map.get(arrow.getDestinationId());
                    if (src != null && dst != null) {
                        linkList.add(new TimeLinkEvent(src, dst, arrow.getStartTime(), arrow.getDuration(), arrow.getValue()));
                    }
                }
            }
        }
        return linkList;
    }

    /**
     * Signal handler for analysis started, we need to rebuilt the entry list with
     * updated statistics values for the current graph worker of the critical path
     * module.
     *
     * @param signal
     *            The signal
     */
    @TmfSignalHandler
    public void analysisStarted(TmfStartAnalysisSignal signal) {
        IAnalysisModule analysis = signal.getAnalysisModule();
        if (analysis instanceof CriticalPathModule) {
            CriticalPathModule criticalPath = (CriticalPathModule) analysis;
            /*
             * We need to wait for CriticalPathDataProviderFactory to have
             * received this signal. Create a new phaser and register, and wait
             * for the end synch signal to arrive and advance in a new thread.
             */
            fPhaser = new Phaser();
            fPhaser.register();
            new Thread() {
                @Override
                public void run() {
                    fPhaser.awaitAdvance(0);
                    Collection<ITmfTrace> traces = TmfTraceManager.getTraceSetWithExperiment(getTrace());
                    if (traces.contains(criticalPath.getTrace())) {
                        rebuild();
                    }
                }
            }.start();
        }
    }

    /**
     * Handler for the start synch signal
     *
     * @param signal
     *            Incoming signal
     */
    @TmfSignalHandler
    public void startSynch(TmfStartSynchSignal signal) {
        fPhaser.register();
    }

    /**
     * Handler for the end synch signal
     *
     * @param signal
     *            Incoming signal
     */
    @TmfSignalHandler
    public void endSynch(TmfEndSynchSignal signal) {
        fPhaser.arriveAndDeregister();
    }

    @Override
    protected void fillLocalToolBar(@Nullable IToolBarManager manager) {
        super.fillLocalToolBar(manager);
        if (manager == null) {
            return;
        }

        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(getClass().getName());
        if (section == null) {
            section = settings.addNewSection(getClass().getName());
        }

        IAction hideArrowsAction = getTimeGraphViewer().getHideArrowsAction(section);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, hideArrowsAction);

        IAction followArrowBwdAction = getTimeGraphViewer().getFollowArrowBwdAction();
        followArrowBwdAction.setText(Messages.CriticalPathView_followArrowBwdText);
        followArrowBwdAction.setToolTipText(Messages.CriticalPathView_followArrowBwdText);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, followArrowBwdAction);

        IAction followArrowFwdAction = getTimeGraphViewer().getFollowArrowFwdAction();
        followArrowFwdAction.setText(Messages.CriticalPathView_followArrowFwdText);
        followArrowFwdAction.setToolTipText(Messages.CriticalPathView_followArrowFwdText);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, followArrowFwdAction);
    }

    @Override
    protected Iterable<ITmfTrace> getTracesToBuild(@Nullable ITmfTrace trace) {
        // we need the critical path module to run on the experiment, not the traces.
        return trace != null ? Collections.singleton(trace) : Collections.emptyList();
    }

}
