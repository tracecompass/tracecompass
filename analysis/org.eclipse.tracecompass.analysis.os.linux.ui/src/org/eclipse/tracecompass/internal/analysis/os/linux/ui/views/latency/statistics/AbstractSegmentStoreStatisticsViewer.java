/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.statistics;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.statistics.LatencyStatistics;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

/**
 * An abstract tree viewer implementation for displaying latency statistics
 *
 * @author Bernd Hufmann
 *
 */
public abstract class AbstractSegmentStoreStatisticsViewer extends AbstractTmfTreeViewer {

    private static final NumberFormat FORMATTER = checkNotNull(NumberFormat.getNumberInstance(Locale.getDefault()));

    @Nullable private TmfAbstractAnalysisModule fModule;

    private static final String[] COLUMN_NAMES = new String[] {
            checkNotNull(Messages.LatencyStatistics_LevelLabel),
            checkNotNull(Messages.LatencyStatistics_MinLabel),
            checkNotNull(Messages.LatencyStatistics_MaxLabel),
            checkNotNull(Messages.LatencyStatistics_AverageLabel)
    };

    /**
     * Constructor
     *
     * @param parent
     *            the parent composite
     */
    public AbstractSegmentStoreStatisticsViewer(Composite parent) {
        super(parent, false);
        setLabelProvider(new LatencyLabelProvider());
    }

    /** Provides label for the Latency tree viewer cells */
    protected static class LatencyLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(@Nullable Object element, int columnIndex) {
            String value = "";  //$NON-NLS-1$
            if (element instanceof HiddenTreeViewerEntry) {
                if (columnIndex == 0) {
                    value = ((HiddenTreeViewerEntry) element).getName();
                }
            } else {
                LatencyTreeViewerEntry obj = (LatencyTreeViewerEntry) element;
                if (obj != null) {
                    if (columnIndex == 0) {
                        value = String.valueOf(obj.getName());
                    } else if (columnIndex == 1) {
                        value = toFormattedString(obj.getEntry().getMin());
                    } else if (columnIndex == 2) {
                        value = String.valueOf(toFormattedString(obj.getEntry().getMax()));
                    } else if (columnIndex == 3) {
                        value = String.valueOf(toFormattedString(obj.getEntry().getAverage()));
                    }
                }
            }
            return checkNotNull(value);
        }
    }

    /**
     * Creates the statistics analysis module
     *
     * @return the statistics analysis module
     */
    @Nullable protected abstract TmfAbstractAnalysisModule createStatisticsAnalysiModule();

    /**
     * Gets the statistics analysis module
     * @return the statistics analysis module
     */
    @Nullable public TmfAbstractAnalysisModule getStatisticsAnalysisModule() {
        return fModule;
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return new ITmfTreeColumnDataProvider() {

            @Override
            public List<@Nullable TmfTreeColumnData> getColumnData() {
                /* All columns are sortable */
                List<@Nullable TmfTreeColumnData> columns = new ArrayList<>();
                TmfTreeColumnData column = new TmfTreeColumnData(COLUMN_NAMES[0]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(@Nullable Viewer viewer, @Nullable Object e1, @Nullable Object e2) {
                        if ((e1 == null) || (e2 == null)) {
                            return 0;
                        }

                        LatencyTreeViewerEntry n1 = (LatencyTreeViewerEntry) e1;
                        LatencyTreeViewerEntry n2 = (LatencyTreeViewerEntry) e2;

                        return n1.getName().compareTo(n2.getName());

                    }
                });
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[1]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(@Nullable Viewer viewer, @Nullable Object e1, @Nullable Object e2) {
                        if ((e1 == null) || (e2 == null)) {
                            return 0;
                        }

                        LatencyTreeViewerEntry n1 = (LatencyTreeViewerEntry) e1;
                        LatencyTreeViewerEntry n2 = (LatencyTreeViewerEntry) e2;

                        return Long.compare(n1.getEntry().getMin(), n2.getEntry().getMin());

                    }
                });
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[2]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(@Nullable Viewer viewer, @Nullable Object e1, @Nullable Object e2) {
                        if ((e1 == null) || (e2 == null)) {
                            return 0;
                        }

                        LatencyTreeViewerEntry n1 = (LatencyTreeViewerEntry) e1;
                        LatencyTreeViewerEntry n2 = (LatencyTreeViewerEntry) e2;

                        return Long.compare(n1.getEntry().getMax(), n2.getEntry().getMax());

                    }
                });
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[3]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(@Nullable Viewer viewer, @Nullable Object e1, @Nullable Object e2) {
                        if ((e1 == null) || (e2 == null)) {
                            return 0;
                        }

                        LatencyTreeViewerEntry n1 = (LatencyTreeViewerEntry) e1;
                        LatencyTreeViewerEntry n2 = (LatencyTreeViewerEntry) e2;

                        return Double.compare(n1.getEntry().getAverage(), n2.getEntry().getAverage());

                    }
                });
                columns.add(column);

                return columns;
            }

        };
    }


    @Override
    public void initializeDataSource() {
        /* Should not be called while trace is still null */
        ITmfTrace trace = checkNotNull(getTrace());
        TmfAbstractAnalysisModule module = createStatisticsAnalysiModule();
        if (module == null) {
            return;
        }
        try {
            module.setTrace(trace);
            module.schedule();
            fModule = module;
        } catch (TmfAnalysisException e) {
            Activator.getDefault().logError("Error initializing statistics analysis module", e); //$NON-NLS-1$
        }
    }

    /**
     * Formats a double value string
     *
     * @param value
     *            a value to format
     * @return formatted value
     */
    protected static String toFormattedString(double value) {
        // The cast to long is needed because the formatter cannot truncate the number.
        String percentageString = checkNotNull(String.format("%s", FORMATTER.format(value))); //$NON-NLS-1$
        return percentageString;
    }

    /**
     * Class for defining an entry in the statistics tree.
     */
    protected class LatencyTreeViewerEntry extends TmfTreeViewerEntry {

        private LatencyStatistics fEntry;

        /**
         * Constructor
         *
         * @param name
         *            name of entry
         *
         * @param entry
         *            latency statistics object
         */
        public LatencyTreeViewerEntry(String name, LatencyStatistics entry) {
            super(name);
            fEntry = entry;
        }

        /**
         * Gets the statistics object
         *
         * @return statistics object
         */
        public LatencyStatistics getEntry() {
            return checkNotNull(fEntry);
        }

    }

    /**
     * Class to define a level in the tree that doesn't have any values.
     */
    protected class HiddenTreeViewerEntry extends LatencyTreeViewerEntry {
        /**
         * Constructor
         *
         * @param name
         *            the name of the level
         */
        public HiddenTreeViewerEntry(String name) {
            super(name, new LatencyStatistics());
        }
    }

}
