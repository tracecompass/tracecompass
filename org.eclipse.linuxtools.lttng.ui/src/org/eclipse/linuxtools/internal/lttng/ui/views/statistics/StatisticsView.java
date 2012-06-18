/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yann N. Dauphin    (dhaemon@gmail.com)    - Implementation
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.views.statistics;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.internal.lttng.core.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.internal.lttng.core.model.LTTngTreeNode;
import org.eclipse.linuxtools.internal.lttng.core.request.ILttngSyntEventRequest;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.AbsEventToHandlerResolver;
import org.eclipse.linuxtools.internal.lttng.core.state.experiment.StateManagerFactory;
import org.eclipse.linuxtools.internal.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.ItemContainer;
import org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.internal.lttng.ui.views.common.AbsTimeUpdateView;
import org.eclipse.linuxtools.internal.lttng.ui.views.common.ParamsUpdater;
import org.eclipse.linuxtools.internal.lttng.ui.views.statistics.evProcessor.StatsTimeCountHandlerFactory;
import org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model.KernelStatisticsData;
import org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model.StatisticsTreeNode;
import org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model.StatisticsTreeRootFactory;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>StatisticsView</u></b>
 * <p>
 * The Statistics View displays statistics for traces.
 *
 * It is implemented according to the MVC pattern. - The model is a
 * StatisticsTreeNode built by the State Manager. - The view is built with a
 * TreeViewer. - The controller that keeps model and view synchronised is an
 * observer of the model.
 */
public class StatisticsView extends AbsTimeUpdateView {
	public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.statistics"; //$NON-NLS-1$
	private TreeViewer treeViewer;

	// Table column names
	private final String LEVEL_COLUMN = Messages.StatisticsView_LevelColumn;
	private final String EVENTS_COUNT_COLUMN = Messages.StatisticsView_NbEventsColumn;
	private final String CPU_TIME_COLUMN = Messages.StatisticsView_CPUTimeColumn;
	private final String CUMULATIVE_CPU_TIME_COLUMN = Messages.StatisticsView_CumCPUTimeColumn;
	private final String ELAPSED_TIME_COLUMN = Messages.StatisticsView_ElapsedTimeColumn;

	// Table column tooltips
	private final String LEVEL_COLUMN_TIP = Messages.StatisticsView_LevelColumnTip;
	private final String EVENTS_COUNT_COLUMN_TIP = Messages.StatisticsView_NbEventsTip;
	private final String CPU_TIME_COLUMN_TIP = Messages.StatisticsView_CPUTimeTip;
	private final String CUMULATIVE_CPU_TIME_COLUMN_TIP = Messages.StatisticsView_CumCPUTimeTip;
	private final String ELAPSED_TIME_COLUMN_TIP = Messages.StatisticsView_ElapsedTimeTip;

	// Level for which statistics should not be displayed.
    private final Set<Integer> folderLevels = new HashSet<Integer>(Arrays
	            .asList(new Integer[] { KernelStatisticsData.HEADER_CPUS_INT,
	                                    KernelStatisticsData.HEADER_EVENT_TYPES_INT,
	                                    KernelStatisticsData.HEADER_FUNCTIONS_INT,
	                                    KernelStatisticsData.HEADER_MODES_INT,
	                                    KernelStatisticsData.HEADER_PROCESSES_INT,
	                                    KernelStatisticsData.HEADER_SUBMODES_INT }));

	// Levels for which sub-levels should not contain time-related statistics.
	private final Set<Integer> levelsWithEmptyTime = new HashSet<Integer>(Arrays
	            .asList(new Integer[] { KernelStatisticsData.HEADER_EVENT_TYPES_INT }));

	private final DecimalFormat decimalFormat = new DecimalFormat("0.#########"); //$NON-NLS-1$

	private Cursor fwaitCursor = null;

	private static final Long STATS_INPUT_CHANGED_REFRESH = 5000L;

	// Used to draw bar charts in columns.
	private interface ColumnPercentageProvider {
		public double getPercentage(StatisticsTreeNode node);
	}

    private boolean fStatisticsUpdateBusy = false;
    private boolean fStatisticsUpdatePending = false;
    private TmfTimeRange fStatisticsUpdateRange = null;
    private final Object fStatisticsUpdateSyncObj = new Object();
    private boolean fClearData = true;
    // Flag to force request the data from trace
    private boolean fRequestData = false;

	/**
	 * Contains all the information necessary to build a column of the table.
	 */
	private static class ColumnData {
		// Name of the column.
		public final String header;
		// Width of the column.
		public final int width;
		// Alignment of the column.
		public final int alignment;
		// Tooltip of the column.
		public final String tooltip;
		// Adapts a StatisticsTreeNode into the content of it's corresponding
		// cell for that column.
		public final ColumnLabelProvider labelProvider;
		// Used to sort elements of this column. Can be null.
		public final ViewerComparator comparator;
		// Used to draw bar charts in this column. Can be null.
		public final ColumnPercentageProvider percentageProvider;

		public ColumnData(String h, int w, int a, String t,
				ColumnLabelProvider l, ViewerComparator c,
				ColumnPercentageProvider p) {
			header = h;
			width = w;
			alignment = a;
			tooltip = t;
			labelProvider = l;
			comparator = c;
			percentageProvider = p;
		}
	};

	// List that will be used to create the table.
	private final ColumnData[] columnDataList = new ColumnData[] {
			new ColumnData(LEVEL_COLUMN, 200, SWT.LEFT, LEVEL_COLUMN_TIP,
					new ColumnLabelProvider() {
						@Override
						public String getText(Object element) {
						    StatisticsTreeNode node = (StatisticsTreeNode) element;
                            if (folderLevels.contains(node.getKey())) {
                                return (KernelStatisticsData.getCategoryFromId(node.getKey().intValue()));
                            } else {
                                return node.getName();
                            }
						}

						@Override
						public Image getImage(Object element) {
							StatisticsTreeNode node = (StatisticsTreeNode) element;
							if (folderLevels.contains(node.getKey())) {
								return PlatformUI.getWorkbench()
										.getSharedImages().getImage(
												ISharedImages.IMG_OBJ_FOLDER);
							} else {
								return PlatformUI.getWorkbench()
										.getSharedImages().getImage(
												ISharedImages.IMG_OBJ_ELEMENT);
							}
						}
					}, new ViewerComparator() {
						@Override
						public int compare(Viewer viewer, Object e1, Object e2) {
							StatisticsTreeNode n1 = (StatisticsTreeNode) e1;
							StatisticsTreeNode n2 = (StatisticsTreeNode) e2;

//							return n1.getKey().compareTo(n2.getKey());
							return n1.compareTo(n2);
						}
					}, null),
			new ColumnData(EVENTS_COUNT_COLUMN, 125, SWT.LEFT,
					EVENTS_COUNT_COLUMN_TIP, new ColumnLabelProvider() {
						@Override
						public String getText(Object element) {
							StatisticsTreeNode node = (StatisticsTreeNode) element;
							if (!folderLevels.contains(node.getKey())) {
								return Long.toString(node.getValue().nbEvents);
							} else {
								return ""; //$NON-NLS-1$
							}
						}
					}, new ViewerComparator() {
						@Override
						public int compare(Viewer viewer, Object e1, Object e2) {
							StatisticsTreeNode n1 = (StatisticsTreeNode) e1;
							StatisticsTreeNode n2 = (StatisticsTreeNode) e2;

							return (int) (n1.getValue().nbEvents - n2
									.getValue().nbEvents);
						}
					}, new ColumnPercentageProvider() {
						@Override
						public double getPercentage(StatisticsTreeNode node) {
							StatisticsTreeNode parent = node;
							do {
								parent = parent.getParent();
							} while (parent != null
									&& parent.getValue().nbEvents == 0);

							if (parent == null) {
								return 0;
							} else {
								return (double) node.getValue().nbEvents
										/ parent.getValue().nbEvents;
							}
						}
					}),
			new ColumnData(CPU_TIME_COLUMN, 125, SWT.LEFT, CPU_TIME_COLUMN_TIP,
					new ColumnLabelProvider() {
						@Override
						public String getText(Object element) {
							StatisticsTreeNode node = (StatisticsTreeNode) element;

							if (folderLevels.contains(node.getKey())) {
								return ""; //$NON-NLS-1$
							} else if (node.getParent() != null
									&& levelsWithEmptyTime.contains(node
											.getParent().getKey())) {
								return ""; //$NON-NLS-1$
							} else {
								return decimalFormat
										.format(node.getValue().cpuTime
												/ Math.pow(10, 9));
							}
						}
					}, null, null),
			new ColumnData(CUMULATIVE_CPU_TIME_COLUMN, 155, SWT.LEFT,
					CUMULATIVE_CPU_TIME_COLUMN_TIP, new ColumnLabelProvider() {
						@Override
						public String getText(Object element) {
							StatisticsTreeNode node = (StatisticsTreeNode) element;
							if (folderLevels.contains(node.getKey())) {
								return ""; //$NON-NLS-1$
							} else if (node.getParent() != null
									&& levelsWithEmptyTime.contains(node
											.getParent().getKey())) {
								return ""; //$NON-NLS-1$
							} else {
								return decimalFormat
										.format(node.getValue().cumulativeCpuTime
												/ Math.pow(10, 9));
							}
						}
					}, null, null),
			new ColumnData(ELAPSED_TIME_COLUMN, 100, SWT.LEFT,
					ELAPSED_TIME_COLUMN_TIP, new ColumnLabelProvider() {
						@Override
						public String getText(Object element) {
							StatisticsTreeNode node = (StatisticsTreeNode) element;
							if (folderLevels.contains(node.getKey())) {
								return ""; //$NON-NLS-1$
							} else if (node.getParent() != null
									&& levelsWithEmptyTime.contains(node
											.getParent().getKey())) {
								return ""; //$NON-NLS-1$
							} else {
								return decimalFormat
										.format(node.getValue().elapsedTime
												/ Math.pow(10, 9));
							}
						}
					}, null, null) };

	/**
	 * Adapter TreeViewers can use to interact with StatisticsTreeNode objects.
	 *
	 * @see org.eclipse.jface.viewers.ITreeContentProvider
	 */
	private static class TreeContentProvider implements ITreeContentProvider {
		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang
		 * .Object)
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			return ((StatisticsTreeNode) parentElement).getChildren().toArray();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang
		 * .Object)
		 */
		@Override
		public Object getParent(Object element) {
			return ((StatisticsTreeNode) element).getParent();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang
		 * .Object)
		 */
		@Override
		public boolean hasChildren(Object element) {
			return ((StatisticsTreeNode) element).hasChildren();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(
		 * java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
		 * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		// @Override
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public StatisticsView(String viewName) {
		super(viewName);
	}

	private static final String STATISTICS_VIEW = "StatisticsView"; //$NON-NLS-1$
	public StatisticsView() {
		this(STATISTICS_VIEW);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.setUseHashlookup(true);

		for (final ColumnData columnData : columnDataList) {
			final TreeViewerColumn treeColumn = new TreeViewerColumn(
					treeViewer, columnData.alignment);
			treeColumn.getColumn().setText(columnData.header);
			treeColumn.getColumn().setWidth(columnData.width);
			treeColumn.getColumn().setToolTipText(columnData.tooltip);
			if (columnData.comparator != null) {
				treeColumn.getColumn().addSelectionListener(
						new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								if (treeViewer.getTree().getSortDirection() == SWT.UP
										|| treeViewer.getTree().getSortColumn() != treeColumn
												.getColumn()) {
									treeViewer
											.setComparator(columnData.comparator);
									treeViewer.getTree().setSortDirection(
											SWT.DOWN);
								} else {
									treeViewer
											.setComparator(new ViewerComparator() {
												@Override
												public int compare(
														Viewer viewer,
														Object e1, Object e2) {
													return -1
															* columnData.comparator
																	.compare(
																			viewer,
																			e1,
																			e2);
												}
											});
									treeViewer.getTree().setSortDirection(
											SWT.UP);
								}
								treeViewer.getTree().setSortColumn(
										treeColumn.getColumn());
							}
						});
			}
			treeColumn.setLabelProvider(columnData.labelProvider);
		}

		// Handler that will draw the bar charts.
		treeViewer.getTree().addListener(SWT.EraseItem, new Listener() {
			// @Override
			@Override
			public void handleEvent(Event event) {
				if (columnDataList[event.index].percentageProvider != null) {
					StatisticsTreeNode node = (StatisticsTreeNode) event.item
							.getData();

					double percentage = columnDataList[event.index].percentageProvider
							.getPercentage(node);
					if (percentage == 0) {
						return;
					}

					if ((event.detail & SWT.SELECTED) > 0) {
						Color oldForeground = event.gc.getForeground();
						event.gc.setForeground(event.item.getDisplay()
								.getSystemColor(SWT.COLOR_LIST_SELECTION));
						event.gc.fillRectangle(event.x, event.y, event.width,
								event.height);
						event.gc.setForeground(oldForeground);
						event.detail &= ~SWT.SELECTED;
					}

					int barWidth = (int) ((treeViewer.getTree().getColumn(1)
							.getWidth() - 8) * percentage);
					int oldAlpha = event.gc.getAlpha();
					Color oldForeground = event.gc.getForeground();
					Color oldBackground = event.gc.getBackground();
					event.gc.setAlpha(64);
					event.gc.setForeground(event.item.getDisplay()
							.getSystemColor(SWT.COLOR_BLUE));
					event.gc.setBackground(event.item.getDisplay()
							.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
					event.gc.fillGradientRectangle(event.x, event.y, barWidth,
							event.height, true);
					event.gc.drawRectangle(event.x, event.y, barWidth,
							event.height);
					event.gc.setForeground(oldForeground);
					event.gc.setBackground(oldBackground);
					event.gc.setAlpha(oldAlpha);
					event.detail &= ~SWT.BACKGROUND;
				}
			}
		});

		treeViewer.setComparator(columnDataList[0].comparator);
		treeViewer.getTree().setSortColumn(treeViewer.getTree().getColumn(0));
		treeViewer.getTree().setSortDirection(SWT.DOWN);

		// Read current data if any available
		TmfExperiment experiment = TmfExperiment.getCurrentExperiment();
		if (experiment != null) {

			TmfExperimentSelectedSignal signal = new TmfExperimentSelectedSignal(this, experiment);
			fRequestData = true;
			experimentSelected(signal);

		} else {
			TraceDebug.debug("No selected experiment information available"); //$NON-NLS-1$
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (fwaitCursor != null) {
			fwaitCursor.dispose();
		}

		// clean the model
		StatisticsTreeRootFactory.removeAll();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		treeViewer.getTree().setFocus();
	}


	/*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#getInputChangedRefresh()
     */
    @Override
    protected Long getInputChangedRefresh() {
        return STATS_INPUT_CHANGED_REFRESH;
    }

	/**
	 * @return
	 */
	@Override
	public AbsEventToHandlerResolver getEventProcessor() {
		return StatsTimeCountHandlerFactory.getInstance();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#waitCursor
	 * (boolean)
	 */
	@Override
	protected void waitCursor(final boolean waitInd) {
		if ((treeViewer == null) || (treeViewer.getTree().isDisposed())) {
			return;
		}

		Display display = treeViewer.getControl().getDisplay();
		if (fwaitCursor == null) {
			fwaitCursor = new Cursor(display, SWT.CURSOR_WAIT);
		}

		// Perform the updates on the UI thread
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if ((treeViewer != null) && (!treeViewer.getTree().isDisposed())) {
					Cursor cursor = null; /* indicates default */
					if (waitInd) {
						cursor = fwaitCursor;
					}
					treeViewer.getControl().setCursor(cursor);
				}
			}
		});
	}

	@Override
	public void modelUpdatePrep(TmfTimeRange timeRange, boolean clearAllData) {
		Object input = treeViewer.getInput();
		if ((input != null) && (input instanceof StatisticsTreeNode) && (!treeViewer.getTree().isDisposed())) {
			if (clearAllData) {
				((StatisticsTreeNode) input).reset();
			}
			treeViewer.getTree().getDisplay().asyncExec(new Runnable() {
				// @Override
				@Override
				public void run() {
					if (!treeViewer.getTree().isDisposed()) {
                        treeViewer.refresh();
                    }
				}
			});
		}
	}

	@Override
	public void modelInputChanged(ILttngSyntEventRequest request, boolean complete) {
		// Ignore update if disposed
		if (treeViewer.getTree().isDisposed()) {
            return;
        }

		if(TraceDebug.isSV() && complete) {
		    // print results

		    TmfExperiment experiment = TmfExperiment.getCurrentExperiment();
		    if(experiment != null) {
		        StatisticsTreeNode node = StatisticsTreeRootFactory.getStatTreeRoot(experiment.getName());
		        printRecursively(node);

		    }
		}

		treeViewer.getTree().getDisplay().asyncExec(new Runnable() {
			// @Override
			@Override
			public void run() {
				if (!treeViewer.getTree().isDisposed()) {
                    treeViewer.refresh();
                }
			}
		});

		if (complete) {
			synchronized (fStatisticsUpdateSyncObj) {
				fStatisticsUpdateBusy = false;
				if (fStatisticsUpdatePending) {
					fStatisticsUpdatePending = false;
					requestData(TmfExperiment.getCurrentExperiment(), fStatisticsUpdateRange, false);
				}
			}
		}

	}

	private static int level = 0;
    private void printRecursively(StatisticsTreeNode node) {
        StringBuffer tab = new StringBuffer(""); //$NON-NLS-1$
        for (int i = 0; i < level; i++) {
            tab.append("\t"); //$NON-NLS-1$
        }
        level++;
        TraceDebug.traceSV(tab + node.getContent());
        if (node.hasChildren()) {
            LinkedList<StatisticsTreeNode> childreen = (LinkedList<StatisticsTreeNode>)node.getChildren();
            Collections.sort(childreen);

            for (Iterator<StatisticsTreeNode> iterator = childreen.iterator(); iterator.hasNext();) {
                StatisticsTreeNode statisticsTreeNode = iterator.next();
                printRecursively(statisticsTreeNode);
            }
        }
        level--;
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#
	 * modelIncomplete
	 * (org.eclipse.linuxtools.lttng.request.ILttngSyntEventRequest)
	 */
	@Override
	public void modelIncomplete(ILttngSyntEventRequest request) {
        // Do not remove incomplete statistics, they can be completed upon next selection
	}

	/**
	 * @param signal
	 */
	@TmfSignalHandler
	public void experimentSelected(TmfExperimentSelectedSignal signal) {
		if (signal != null) {
			TmfExperiment experiment = signal.getExperiment();
			String experimentName =  experiment.getName();

			if (StatisticsTreeRootFactory.containsTreeRoot(experimentName)) {
				// The experiment root is already present
				StatisticsTreeNode experimentTreeNode = StatisticsTreeRootFactory.getStatTreeRoot(experimentName);

				@SuppressWarnings("rawtypes")
                ITmfTrace[] traces = experiment.getTraces();

				LTTngTreeNode expNode = StateManagerFactory.getExperimentManager().getSelectedExperiment();

				// check if there is partial data loaded in the experiment
				int numTraces = experiment.getTraces().length;
				int numNodeTraces = experimentTreeNode.getNbChildren();

				if (numTraces == numNodeTraces) {
					boolean same = true;
					// Detect if the experiment contains the same traces as when
					// previously selected
					for (int i = 0; i < numTraces; i++) {
						String traceName = traces[i].getName();
						LTTngTreeNode child = expNode.getChildByName(traceName);
						if ((child == null) || (!experimentTreeNode.containsChild(child.getId().intValue()))) {
							 same = false;
							 break;
						}
					}

					if (same) {
						treeViewer.setInput(experimentTreeNode);
						synchronized (fStatisticsUpdateSyncObj) {
							fStatisticsUpdateBusy = false;
							fStatisticsUpdatePending = false;
						}
						// request in case current data is incomplete
						requestData(experiment, experiment.getTimeRange(), false);
						return;
					}
				}
			}

			StatisticsTreeNode treeModelRoot = StatisticsTreeRootFactory.getStatTreeRoot(experiment.getName());

			// if the model has contents, clear to start over
			if (treeModelRoot.hasChildren()) {
				treeModelRoot.reset();
			}

			// set input to a clean data model
			treeViewer.setInput(treeModelRoot);

			synchronized (fStatisticsUpdateSyncObj) {
				fStatisticsUpdateBusy = false;
				fStatisticsUpdatePending = false;
			}

			// if the data is not available or has changed, reload it
			fClearData = true;
			if(fRequestData) {
			    requestData(experiment, experiment.getTimeRange(), fClearData);
			    fRequestData = false;
			}
		}
	}

	/**
	 * @param signal
	 */
	@TmfSignalHandler
	public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal) {
		TmfExperiment experiment = signal.getExperiment();
		// validate
		if (! experiment.equals(TmfExperiment.getCurrentExperiment())) {
			return;
		}

		requestData(experiment, signal.getRange(), fClearData);
		fClearData = false;
	}

	/**
	 * @param experiment
	 */
	private void requestData(TmfExperiment experiment, TmfTimeRange range, boolean clearingData) {
		if (experiment != null) {
			synchronized (fStatisticsUpdateSyncObj) {
				if (fStatisticsUpdateBusy) {
					fStatisticsUpdatePending = true;
					fStatisticsUpdateRange = range;
					return;
				} else {
					fStatisticsUpdateBusy = true;
				}
			}

			int index = 0;
			for (StatisticsTreeNode node : ((StatisticsTreeNode) treeViewer.getInput()).getChildren()) {
				index += (int) node.getValue().nbEvents;
			}

			// send the initial request, to start filling up model
			eventRequest(index, range, clearingData, ExecutionType.BACKGROUND);
		} else {
			TraceDebug.debug("No selected experiment information available"); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#displayModel
	 * (org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.
	 * ITmfTimeAnalysisEntry[], long, long, boolean, long, long,
	 * java.lang.Object)
	 */
	@Override
	protected void displayModel(ITmfTimeAnalysisEntry[] items, long startBoundTime, long endBoundTime,
			boolean updateTimeBounds, long startVisibleWindow, long endVisibleWindow, Object source) {
		// No applicable to statistics view
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#
	 * getParamsUpdater()
	 */
	@Override
	protected ParamsUpdater getParamsUpdater() {
		// Not applicable to statistics view
		return null;
	}

	@Override
	protected ItemContainer<?> getItemContainer() {
		// Not applicable to statistics view
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#getProviderId()
	 */
	@Override
	protected int getProviderId() {
	    return LttngCoreProviderFactory.STATISTICS_LTTNG_SYTH_EVENT_PROVIDER;
	}
}