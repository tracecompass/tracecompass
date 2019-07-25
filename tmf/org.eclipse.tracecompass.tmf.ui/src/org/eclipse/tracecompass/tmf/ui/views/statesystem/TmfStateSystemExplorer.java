/*******************************************************************************
 * Copyright (c) 2013, 2018 École Polytechnique de Montréal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *   Alexandre Montplaisir - Refactoring, performance tweaks
 *   Bernd Hufmann - Updated signal handling
 *   Marc-Andre Laperle - Add time zone preference
 *   Geneviève Bastien - Use a tree viewer instead of a tree
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statesystem;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.provider.StateSystemDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.provider.StateSystemDataProvider.AttributeEntryModel;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.provider.StateSystemDataProvider.ModuleEntryModel;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.provider.StateSystemDataProvider.StateSystemEntryModel;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.provider.StateSystemDataProvider.TraceEntryModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartAnalysisSignal;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Displays the State System at a current time.
 *
 * @author Benjamin Saint-Cyr
 * @author Florian Wininger
 * @author Alexandre Montplaisir
 * @author Loic Prieur-Drevon - make extend {@link AbstractTimeGraphView}
 */
public class TmfStateSystemExplorer extends BaseDataProviderTimeGraphView {

    /** The Environment View's ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.ssvisualizer"; //$NON-NLS-1$

    private static final String[] COLUMN_NAMES = new String[] {
            Messages.TreeNodeColumnLabel,
            Messages.QuarkColumnLabel
    };

    private final Set<ITmfAnalysisModuleWithStateSystems> fStartedAnalysis = ConcurrentHashMap.newKeySet();

    private static final Comparator<ITimeGraphEntry> NAME_COMPARATOR = (a, b) -> {
        if (a instanceof TimeGraphEntry && b instanceof TimeGraphEntry) {
            ITmfTreeDataModel aModel = ((TimeGraphEntry) a).getEntryModel();
            ITmfTreeDataModel bModel = ((TimeGraphEntry) b).getEntryModel();
            if (aModel instanceof TraceEntryModel && bModel instanceof TraceEntryModel) {
                ITmfTrace ta = ((TraceEntryModel) aModel).getTrace();
                ITmfTrace tb = ((TraceEntryModel) bModel).getTrace();
                // Puts the experiment entries at the top of the list
                if (ta instanceof TmfExperiment && !(tb instanceof TmfExperiment)) {
                    return -1;
                } else if (!(ta instanceof TmfExperiment) && (tb instanceof TmfExperiment)) {
                    return 1;
                }
            }
        }
        try {
            return Long.compare(Long.parseLong(a.getName()), Long.parseLong(b.getName()));
        } catch (NumberFormatException e) {
            // fall through to string compare
        }
        return a.getName().compareTo(b.getName());
    };

    private static final Comparator<ITimeGraphEntry> QUARK_COMPARATOR = (a, b) -> {
        if (a instanceof TimeGraphEntry && b instanceof TimeGraphEntry) {
            ITmfTreeDataModel aModel = ((TimeGraphEntry) a).getEntryModel();
            ITmfTreeDataModel bModel = ((TimeGraphEntry) b).getEntryModel();
            if (aModel instanceof AttributeEntryModel && bModel instanceof AttributeEntryModel) {
                return Integer.compare(((AttributeEntryModel) aModel).getQuark(), ((AttributeEntryModel) bModel).getQuark());
            }
        }
        return 0;
    };

    private static final Comparator<ITimeGraphEntry>[] COLUMN_COMPARATORS;
    static {
        ImmutableList.Builder<Comparator<ITimeGraphEntry>> builder = ImmutableList.builder();
        builder.add(NAME_COMPARATOR)
                .add(QUARK_COMPARATOR);
        List<Comparator<ITimeGraphEntry>> l = builder.build();
        COLUMN_COMPARATORS = l.toArray(new Comparator[l.size()]);
    }

    private static final int NAME_COLUMN_INDEX = 0;
    /**
     * Setting the auto expand level to 2 shows all entries down to the state
     * systems.
     */
    private static final int DEFAULT_AUTOEXPAND = 2;

    private static class StateSystemTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof TimeGraphEntry) {
                TimeGraphEntry entry = (TimeGraphEntry) element;
                entry.getParent();
                if (columnIndex == 0) {
                    return entry.getName();
                } else if (entry.getEntryModel() instanceof AttributeEntryModel) {
                    AttributeEntryModel model = (AttributeEntryModel) entry.getEntryModel();
                    if (columnIndex == 1) {
                        return Integer.toString(model.getQuark());
                    }
                }
            }
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Default constructor
     */
    public TmfStateSystemExplorer() {
        super(ID, new StateSystemPresentationProvider(), StateSystemDataProvider.ID);
        setTreeColumns(COLUMN_NAMES, COLUMN_COMPARATORS, NAME_COLUMN_INDEX);
        setTreeLabelProvider(new StateSystemTreeLabelProvider());
        setAutoExpandLevel(DEFAULT_AUTOEXPAND);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        getTimeGraphViewer().addTimeListener(event -> synchingToTime(event.getBeginTime()));

        getTimeGraphViewer().getTimeGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent event) {
                ITimeGraphEntry selection = getTimeGraphViewer().getSelection();
                if (selection instanceof TimeGraphEntry) {
                    ITmfTreeDataModel model = ((TimeGraphEntry) selection).getEntryModel();
                    if (model instanceof ModuleEntryModel && selection.getChildren().isEmpty()) {
                        /**
                         * Schedule the analysis if it has not run yet.
                         */
                        ITmfAnalysisModuleWithStateSystems module = ((ModuleEntryModel) model).getModule();
                        module.schedule();
                    }
                    TimeGraphControl control = getTimeGraphViewer().getTimeGraphControl();
                    boolean expandedState = control.getExpandedState(selection);
                    control.setExpandedState(selection, !expandedState);
                }
            }
        });
    }

    /**
     * Get the {@link ITmfStateSystem} and path for an entry.
     *
     * @param entry
     *            any {@link TimeGraphEntry} from the tree
     * @return a {@link Pair} encapsulating both, else null if the entry was for
     *         a trace / module / state system.
     */
    static ITmfStateSystem getStateSystem(TimeGraphEntry entry) {
        TimeGraphEntry parent = entry;
        while (parent != null) {
            parent = parent.getParent();
            if (parent.getEntryModel() instanceof StateSystemEntryModel) {
                return ((StateSystemEntryModel) parent.getEntryModel()).getStateSystem();
            }
        }
        return null;
    }

    /**
     * Rebuild the view's entry tree to ensure that entries from a newly started
     * trace are added.
     *
     * @param signal
     *            analysis started signal.
     * @since 3.3
     */
    @TmfSignalHandler
    public void handleAnalysisStarted(TmfStartAnalysisSignal signal) {
        IAnalysisModule module = signal.getAnalysisModule();
        if (module instanceof ITmfAnalysisModuleWithStateSystems && !module.isAutomatic()) {
            /*
             * use set to wait for initialization in build entry list to avoid
             * deadlocks.
             */
            if (Iterables.contains(allModules(getTrace()), module)) {
                /*
                 * Rebuild only if the started analysis module is from the
                 * active trace/experiment.
                 */
                synchronized (fStartedAnalysis) {
                    fStartedAnalysis.add((ITmfAnalysisModuleWithStateSystems) module);
                    //Every children of ITmfAnalysisModuleWithStateSystems extends TmfAbstractAnalysisModule
                    ITmfTrace moduleTrace = module instanceof TmfAbstractAnalysisModule ? ((TmfAbstractAnalysisModule) module).getTrace() : getTrace();
                    getDataProvider(moduleTrace).startedAnalysisSignalHandler((ITmfAnalysisModuleWithStateSystems) module);
                    rebuild();
                }
            } else {
                /*
                 * Reset the View for the relevant trace, ensuring that the
                 * entry list will be rebuilt when the view switches back.
                 */
                for (ITmfTrace trace : TmfTraceManager.getInstance().getOpenedTraces()) {
                    if (Iterables.contains(allModules(trace), module)) {
                        synchronized (fStartedAnalysis) {
                            fStartedAnalysis.add((ITmfAnalysisModuleWithStateSystems) module);
                            resetView(trace);
                        }
                        break;
                    }
                }
            }
        }
    }

    private StateSystemDataProvider getDataProvider(ITmfTrace moduleTrace) {
        ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> dataProvider = DataProviderManager
                .getInstance().getDataProvider(Objects.requireNonNull(moduleTrace), getProviderId(), ITimeGraphDataProvider.class);
        if (dataProvider instanceof StateSystemDataProvider) {
            return (StateSystemDataProvider) dataProvider;
        }
        throw new NullPointerException();
    }

    private static Iterable<ITmfAnalysisModuleWithStateSystems> allModules(ITmfTrace trace) {
        Collection<@NonNull ITmfTrace> traces = TmfTraceManager.getTraceSetWithExperiment(trace);
        Iterable<IAnalysisModule> allModules = Iterables.concat(Iterables.transform(traces, ITmfTrace::getAnalysisModules));
        return Iterables.filter(allModules, ITmfAnalysisModuleWithStateSystems.class);
    }

    @Override
    protected @NonNull Iterable<ITmfTrace> getTracesToBuild(@Nullable ITmfTrace trace) {
        return TmfTraceManager.getTraceSetWithExperiment(trace);
    }

}
