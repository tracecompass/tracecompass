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

package org.eclipse.linuxtools.lttng.ui.views.statistics;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.lttng.state.IStateDataRequestListener;
import org.eclipse.linuxtools.lttng.state.RequestCompletedSignal;
import org.eclipse.linuxtools.lttng.state.RequestStartedSignal;
import org.eclipse.linuxtools.lttng.state.evProcessor.EventProcessorProxy;
import org.eclipse.linuxtools.lttng.state.experiment.StateExperimentManager;
import org.eclipse.linuxtools.lttng.state.experiment.StateManagerFactory;
import org.eclipse.linuxtools.lttng.ui.views.statistics.evProcessor.StatsEventCountHandlerFactory;
import org.eclipse.linuxtools.lttng.ui.views.statistics.evProcessor.StatsTimeCountHandlerFactory;
import org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsTreeFactory;
import org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsTreeNode;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
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
public class StatisticsView extends TmfView implements IStateDataRequestListener {

	public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.statistics";

	private TreeViewer treeViewer;

	// Table column names
	private final String LEVEL_COLUMN = "Level";
	private final String EVENTS_COUNT_COLUMN = "Number of Events";
	private final String CPU_TIME_COLUMN = "CPU Time";
	private final String CUMULATIVE_CPU_TIME_COLUMN = "Cumulative CPU Time";
	private final String ELAPSED_TIME_COLUMN = "Elapsed Time";

	// Table column tooltips
	private final String LEVEL_COLUMN_TIP = "Level at which statistics apply.";
	private final String EVENTS_COUNT_COLUMN_TIP = "Total amount of events that are tied to given resource.";
	private final String CPU_TIME_COLUMN_TIP = "Total amount of time the CPU was used excluding wait times(I/O, etc.) at that level.";
	private final String CUMULATIVE_CPU_TIME_COLUMN_TIP = "Total amount of time between the first and last event excluding wait times in a level.";
	private final String ELAPSED_TIME_COLUMN_TIP = "Total amount of time the CPU was used including wait times(I/O, etc.) at that level.";

	// Level for which statistics should not be displayed.
	private Set<String> folderLevels = new HashSet<String>(Arrays
			.asList(new String[] { "Event Types", "Modes", "Submodes", "CPUs",
					"Processes", "Functions" }));

	// Levels for which sub-levels should not contain time-related statistics.
	private Set<String> levelsWithEmptyTime = new HashSet<String>(Arrays
			.asList(new String[] { "Event Types" }));

	private DecimalFormat decimalFormat = new DecimalFormat("0.#########");

	// Used to draw bar charts in columns.
	private interface ColumnPercentageProvider {
		public double getPercentage(StatisticsTreeNode node);
	}

	/**
	 * Contains all the information necessary to build a column of the table.
	 */
	private class ColumnData {
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
	private ColumnData[] columnDataList = new ColumnData[] {
			new ColumnData(LEVEL_COLUMN, 200, SWT.LEFT, LEVEL_COLUMN_TIP,
					new ColumnLabelProvider() {
						@Override
						public String getText(Object element) {
							return ((StatisticsTreeNode) element).getKey();
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

							return n1.getKey().compareTo(n2.getKey());
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
								return "";
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
								return "";
							} else if (node.getParent() != null
									&& levelsWithEmptyTime.contains(node
											.getParent().getKey())) {
								return "";
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
								return "";
							} else if (node.getParent() != null
									&& levelsWithEmptyTime.contains(node
											.getParent().getKey())) {
								return "";
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
								return "";
							} else if (node.getParent() != null
									&& levelsWithEmptyTime.contains(node
											.getParent().getKey())) {
								return "";
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
	class TreeContentProvider implements ITreeContentProvider {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang
		 * .Object)
		 */
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
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
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
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public StatisticsView(String viewName) {
		super("StatisticsView");
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
		EventProcessorProxy.getInstance().addEventProcessorFactory(
				StatsTimeCountHandlerFactory.getInstance());
		EventProcessorProxy.getInstance().addEventProcessorFactory(
				StatsEventCountHandlerFactory.getInstance());

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

		treeViewer.setInput(StatisticsTreeFactory
				.getStatisticsTree("Experiment"));

		// Read current data if any available
		StateExperimentManager experimentManger = StateManagerFactory
				.getExperimentManager();
		experimentManger.readExperiment("statisticsView", this);

	}
	@Override
	public void dispose() {
		super.dispose();
		EventProcessorProxy.getInstance().removeEventProcessorFactory(
				StatsTimeCountHandlerFactory.getInstance());
		EventProcessorProxy.getInstance().removeEventProcessorFactory(
				StatsEventCountHandlerFactory.getInstance());

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

	@TmfSignalHandler
	public void processingStarted(RequestStartedSignal request) {
		// Nothing to do for the time being

	}

	@TmfSignalHandler
	public void processingCompleted(RequestCompletedSignal signal) {
		treeViewer.getTree().getDisplay().asyncExec(new Runnable() {
			// @Override
			public void run() {
				treeViewer.refresh();
			}
		});
	}
}
