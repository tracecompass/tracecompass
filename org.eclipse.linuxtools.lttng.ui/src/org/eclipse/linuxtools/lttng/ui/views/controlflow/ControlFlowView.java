/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial implementation
 *   Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *   Bernd Hufmann - Bug fixes
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.controlflow;

import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.internal.lttng.core.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.internal.lttng.core.request.ILttngSyntEventRequest;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ITransEventProcessor;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.ItemContainer;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventProcess;
import org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView;
import org.eclipse.linuxtools.lttng.ui.views.common.ParamsUpdater;
import org.eclipse.linuxtools.lttng.ui.views.controlflow.evProcessor.FlowEventToHandlerFactory;
import org.eclipse.linuxtools.lttng.ui.views.controlflow.model.FlowModelFactory;
import org.eclipse.linuxtools.lttng.ui.views.controlflow.model.FlowTimeRangeViewerProvider;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewerFactory;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITmfTimeFilterSelectionListener;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITmfTimeScaleSelectionListener;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITmfTimeSelectionListener;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeFilterSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeScaleSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * <b><u>ControlFlowView</u></b>
 */
/**
 * @author alvaro
 * 
 */
public class ControlFlowView extends AbsTimeUpdateView implements
		ITmfTimeSelectionListener, ITmfTimeScaleSelectionListener,
		ITmfTimeFilterSelectionListener {

    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.controlflow"; //$NON-NLS-1$
    
	// ========================================================================
	// Table data
	// ========================================================================

    private final String PROCESS_COLUMN    = Messages.getString("ControlFlowView.processColumn"); //$NON-NLS-1$
	private final String BRAND_COLUMN      = Messages.getString("ControlFlowView.brandColumn"); //$NON-NLS-1$
	private final String PID_COLUMN        = Messages.getString("ControlFlowView.pidColumn"); //$NON-NLS-1$
	private final String TGID_COLUMN       = Messages.getString("ControlFlowView.tgidColumn"); //$NON-NLS-1$
	private final String PPID_COLUMN       = Messages.getString("ControlFlowView.ppidColumn"); //$NON-NLS-1$
	private final String CPU_COLUMN        = Messages.getString("ControlFlowView.cpuColumn"); //$NON-NLS-1$
	private final String BIRTH_SEC_COLUMN  = Messages.getString("ControlFlowView.birthSecColumn"); //$NON-NLS-1$
	private final String BIRTH_NSEC_COLUMN = Messages.getString("ControlFlowView.birthNSecColumn"); //$NON-NLS-1$
	private final String TRACE             = Messages.getString("ControlFlowView.TraceNameColumn"); //$NON-NLS-1$

	private final String[] columnNames = new String[] { PROCESS_COLUMN, /* */
	BRAND_COLUMN,/* */
	PID_COLUMN,/* */
	TGID_COLUMN,/* */
	PPID_COLUMN,/* */
	CPU_COLUMN, /* */
	BIRTH_SEC_COLUMN,/* */
	BIRTH_NSEC_COLUMN,/* */
	TRACE /* */
	};

	// ========================================================================
	// Data
	// ========================================================================
	private TableViewer tableViewer;
	// private int totalNumItems = 0;
	// Actions
	private Action doubleClickAction;
	private Action resetScale;
	private Action nextEvent;
	private Action prevEvent;
	private Action nextTrace;
	private Action prevTrace;
	private Action showLegend;
	private Action filterTraces;
	private Action zoomIn;
	private Action zoomOut;
	private Action zoomFilter;

	private ViewProcessFilter tableFilter = null;
	private ScrolledComposite scrollFrame = null;
	
	private TmfTimeRange initTimeRange = TmfTimeRange.NULL_RANGE;

	// private static SimpleDateFormat stimeformat = new SimpleDateFormat(
	// "yy/MM/dd HH:mm:ss");

	// private TraceModelImplFactory fact;

	// ========================================================================
	// Methods
	// ========================================================================
	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	static class ViewContentProvider implements
	/* ILazyContentProvider, */IStructuredContentProvider {
		private TableViewer cviewer = null;
		private ITmfTimeAnalysisEntry[] elements = null;

		public ViewContentProvider(TableViewer v) {
			cviewer = v;
		}

		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			this.elements = (ITmfTimeAnalysisEntry[]) newInput;
			if (elements != null) {
				TraceDebug.debug("Total number of processes provided to Control Flow view: " + elements.length); //$NON-NLS-1$
			} else {
				TraceDebug.debug("New input = null"); //$NON-NLS-1$
			}
		}

		@Override
		public void dispose() {

		}

		// Needed with the use of virtual tables in order to initialize items
		// which were not initially visible.
		public void updateElement(int index) {
			cviewer.replace(elements[index], index);
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return elements;
		}
	}

	static class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			String strRes = ""; //$NON-NLS-1$
			LttngTimestamp time;
			if (obj instanceof TimeRangeEventProcess) {
				TimeRangeEventProcess process = (TimeRangeEventProcess) obj;
				switch (index) {
				case 0:
					strRes = process.getName();
					break;
				case 1:
					strRes = process.getBrand();
					break;
				case 2:
					strRes = process.getPid().toString();
					break;
				case 3:
					strRes = process.getTgid().toString();
					break;
				case 4:
					strRes = process.getPpid().toString();
					break;
				case 5:
					strRes = process.getCpu().toString();
					break;
				case 6:
					time = new LttngTimestamp(process.getCreationTime()
							.longValue());
					strRes = time.getSeconds();
					break;
				case 7:
					time = new LttngTimestamp(process.getCreationTime()
							.longValue());
					strRes = time.getNanoSeconds();
					break;
				case 8:
					strRes = process.getTraceID();
					break;
				default:
					break;
				}
			} else {
				return getText(obj);
			}

			return strRes;
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj) {
			// No image needed for the time being
			// return PlatformUI.getWorkbench().getSharedImages().getImage(
			// ISharedImages.IMG_OBJ_ELEMENT);
			return null;
		}
	}

	static class ViewProcessFilter extends ViewerFilter {

		private Vector<ITmfTimeAnalysisEntry> filteredSet = new Vector<ITmfTimeAnalysisEntry>();
		StructuredViewer viewer;

		public ViewProcessFilter(StructuredViewer rviewer) {
			this.viewer = rviewer;
		}

		public void setFilter(Vector<ITmfTimeAnalysisEntry> filtered) {
			if (filtered != null) {
				this.filteredSet = filtered;
				viewer.refresh();
			}
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			boolean filteredIn = true;
			if (element instanceof ITmfTimeAnalysisEntry) {
				ITmfTimeAnalysisEntry process = (ITmfTimeAnalysisEntry) element;
				if (filteredSet.contains(process)) {
					// The element is marked to be filtered out
					return false;
				}
			} else {
				TraceDebug.debug("Unexpected type of filter element received: " //$NON-NLS-1$
						+ element.toString());
			}
			// Compare element versus a list of filtered out
			return filteredIn;
		}
	}

	/**
	 * The constructor.
	 */
	public ControlFlowView() {
		super(ID);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.tmf.ui.views.TmfView#createPartControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {

		scrollFrame = new ScrolledComposite(parent, SWT.V_SCROLL);
		
		scrollFrame.setExpandVertical(true);
		scrollFrame.setExpandHorizontal(true);
		scrollFrame.setAlwaysShowScrollBars(true);
		
		SashForm sash = new SashForm(scrollFrame, SWT.NONE);
		scrollFrame.setContent(sash);

		tableViewer = new TableViewer(sash, SWT.FULL_SELECTION | SWT.H_SCROLL);
		tableViewer.setContentProvider(new ViewContentProvider(tableViewer));
		tableViewer.setLabelProvider(new ViewLabelProvider());
		Table table = tableViewer.getTable();
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				if (!sel.isEmpty()) {
					Object firstSel = null;
					if (sel instanceof IStructuredSelection) {
						firstSel = ((IStructuredSelection) sel).getFirstElement();

						// Make sure the selection is visible
						updateScrollOrigin();

						if (firstSel instanceof ITmfTimeAnalysisEntry) {
							ITmfTimeAnalysisEntry trace = (ITmfTimeAnalysisEntry) firstSel;
							tsfviewer.setSelectedTrace(trace);
						}
					}
				}
			}

			/**
			 * Make sure the selected item is visible
			 */
			private void updateScrollOrigin() {
				Table table = tableViewer.getTable();
				if (table != null && table.getItemCount() > 0) {
					TableItem item = table.getSelection()[0];
					if (item == null) {
						// no selected reference to go up or down
						return;
					}

					Rectangle itemRect = item.getBounds();
					int step = itemRect.height;

					// calculate height of horizontal bar
					int hscrolly = 0;
					ScrollBar hbar = scrollFrame.getHorizontalBar();
					if (hbar != null) {
						hscrolly = hbar.getSize().y;
					}

					int visibleHeight = scrollFrame.getSize().y - hscrolly;

					// the current scrollbar offset to adjust i.e. start
					// of
					// the visible window
					Point origin = scrollFrame.getOrigin();
					// end of visible window
					int endy = origin.y + visibleHeight;

					int itemStartPos = itemRect.y + table.getHeaderHeight() + table.getBorderWidth()
							+ table.getParent().getBorderWidth();

					// Item End Position
					int itemEndPos = itemStartPos + step;

					// check if need to go up
					if (origin.y >= step && itemStartPos < origin.y) {
						// one step up
						scrollFrame.setOrigin(origin.x, origin.y - step);

					}

							// check if it needs to go down
					if (itemEndPos > endy) {
						// one step down
						scrollFrame.setOrigin(origin.x, origin.y + step);

							}
				}
			}
		});
		
		// Listen to page up /down and Home / Enc keys
		tableViewer.getTable().addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				Table table = tableViewer.getTable();
				Point origin = scrollFrame.getOrigin();
				if (table == null || table.getItemCount() < 1) {
					// nothing to page
					return;
				}

				switch (e.keyCode) {
				case SWT.PAGE_DOWN:
					updateScrollPageDown();
					break;
				case SWT.PAGE_UP:
					updateScrollUp();
					break;
				case SWT.HOME:
					// Go to the top
					scrollFrame.setOrigin(origin.x, 0);
					break;
				case SWT.END:
					// End Selected
					int count = table.getItemCount();
					TableItem item = table.getItem(count - 1);
					int itemStartPos = item.getBounds().y;
					// Get to the bottom
					scrollFrame.setOrigin(origin.x, itemStartPos);
					break;
				default:
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// Nothing to do

			}

			/**
			 * Scroll one page down
			 */
			private void updateScrollPageDown() {
				// null protection before calling private method
				Table table = tableViewer.getTable();
				int step = table.getItemHeight();

				int hscrolly = 0;
				ScrollBar hbar = scrollFrame.getHorizontalBar();
				if (hbar != null) {
					hscrolly = hbar.getSize().y;
				}

				Point origin = scrollFrame.getOrigin();
				int visibleHeight = scrollFrame.getSize().y - hscrolly;
				int endy = origin.y + visibleHeight;

				scrollFrame.setOrigin(origin.x, endy - step);
			}

			/**
			 * Scroll one page up
			 */
			private void updateScrollUp() {
				// null protection before calling private method
				Table table = tableViewer.getTable();
				int step = table.getItemHeight();

				int hscrolly = 0;
				ScrollBar hbar = scrollFrame.getHorizontalBar();
				if (hbar != null) {
					hscrolly = hbar.getSize().y;
				}

				Point origin = scrollFrame.getOrigin();
				int visibleHeight = scrollFrame.getSize().y - hscrolly;
				int pageUpPos = origin.y - visibleHeight + step;
				pageUpPos = pageUpPos > 0 ? pageUpPos : 0;
				scrollFrame.setOrigin(origin.x, pageUpPos);
			}

		});
		// Describe table
		applyTableLayout(table);

		int borderWidth = table.getBorderWidth();

		int itemHeight = table.getItemHeight() + getTableItemHeightAdjustement();
		int headerHeight = table.getHeaderHeight();
		table.getVerticalBar().setVisible(false);

		tsfviewer = TmfViewerFactory.createViewer(sash, new FlowTimeRangeViewerProvider(getParamsUpdater()));

		// Traces shall not be grouped to allow synchronisation
		tsfviewer.groupTraces(false);
		tsfviewer.setItemHeight(itemHeight);
		tsfviewer.setBorderWidth(borderWidth);
		tsfviewer.setHeaderHeight(headerHeight);
		tsfviewer.setVisibleVerticalScroll(false);
		// Names provided by the table
		tsfviewer.setNameWidthPref(0);
		tsfviewer.setAcceptSelectionAPIcalls(true);

		// Viewer to notify selection to this class
		// This class will synchronise selections with table.
		tsfviewer.addWidgetSelectionListner(this);
		tsfviewer.addFilterSelectionListner(this);
		tsfviewer.addWidgetTimeScaleSelectionListner(this);

		sash.setWeights(new int[] { 1, 1 });
		// Create the help context id for the viewer's control
		// TODO: Associate with help system
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				tableViewer.getControl(),
				"org.eclipse.linuxtools.lttnng.ui.views.flow.viewer"); //$NON-NLS-1$

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		// scrollFrame.addControlListener(new ControlAdapter() {
		//
		// @Override
		// public void controlResized(ControlEvent e) {
		// tsfviewer.resizeControls();
		// updateScrolls(scrollFrame);
		// }
		// });

		// set the initial view parameter values
		// Experiment start and end time
		// as well as time space width in pixels, used by the time analysis
		// widget
		// Read relevant values
//		int timeSpaceWidth = tsfviewer.getTimeSpace();
//		if (timeSpaceWidth < 0) {
//			timeSpaceWidth = -timeSpaceWidth;
//		}

		TmfExperiment<?> experiment = TmfExperiment.getCurrentExperiment();
		if (experiment != null) {
			TmfTimeRange experimentTRange = experiment.getTimeRange();

			if (experimentTRange != TmfTimeRange.NULL_RANGE) {
				// send request and received the adjusted time used
				TmfTimeRange adjustedTimeRange = initialExperimentDataRequest(this,
						experimentTRange);
	
				// initialize widget time boundaries and filtering parameters
				modelUpdateInit(experimentTRange, adjustedTimeRange, this);
			}
		} else {
			TraceDebug.debug("No selected experiment information available"); //$NON-NLS-1$
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ControlFlowView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
		tableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(showLegend);
		manager.add(new Separator());
		manager.add(resetScale);
		manager.add(nextEvent);
		manager.add(prevEvent);
		manager.add(nextTrace);
		manager.add(prevTrace);
		// manager.add(filterTraces);
		manager.add(zoomIn);
		manager.add(zoomOut);
		manager.add(zoomFilter);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(showLegend);
		manager.add(new Separator());
		manager.add(resetScale);
		manager.add(nextEvent);
		manager.add(prevEvent);
		manager.add(nextTrace);
		manager.add(prevTrace);
		manager.add(showLegend);
		// manager.add(filterTraces);
		manager.add(zoomIn);
		manager.add(zoomOut);
		manager.add(zoomFilter);
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(showLegend);
		manager.add(new Separator());
		manager.add(resetScale);
		manager.add(nextEvent);
		manager.add(prevEvent);
		manager.add(nextTrace);
		manager.add(prevTrace);
		// manager.add(filterTraces);
		manager.add(zoomIn);
		manager.add(zoomOut);
		manager.add(zoomFilter);
		manager.add(new Separator());
	}

	private void makeActions() {
		// resetScale
		resetScale = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.resetStartFinishTime();
				}

			}
		};
		resetScale.setText(Messages.getString("ControlFlowView.Action.Reset")); //$NON-NLS-1$
		resetScale.setToolTipText(Messages
				.getString("ControlFlowView.Action.Reset.ToolTip")); //$NON-NLS-1$
		resetScale.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ControlFlowView.tmf.UI"), //$NON-NLS-1$
						"icons/elcl16/home_nav.gif")); //$NON-NLS-1$

		// nextEvent
		nextEvent = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectNextEvent();
				}
			}
		};
		nextEvent.setText(Messages
				.getString("ControlFlowView.Action.NextEvent")); //$NON-NLS-1$
		nextEvent.setToolTipText(Messages
				.getString("ControlFlowView.Action.NextEvent.Tooltip")); //$NON-NLS-1$
		nextEvent.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ControlFlowView.tmf.UI"), //$NON-NLS-1$
						"icons/elcl16/next_event.gif")); //$NON-NLS-1$

		// prevEvent
		prevEvent = new Action() {
		@Override
		public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectPrevEvent();
				}
			}
		};
		prevEvent.setText(Messages
				.getString("ControlFlowView.Action.PrevEvent")); //$NON-NLS-1$
		prevEvent.setToolTipText(Messages
				.getString("ControlFlowView.Action.PrevEvent.Tooltip")); //$NON-NLS-1$
		prevEvent.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ControlFlowView.tmf.UI"), //$NON-NLS-1$
						"icons/elcl16/prev_event.gif")); //$NON-NLS-1$

		// nextTrace
		nextTrace = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectNextTrace();
				}
			}
		};
		nextTrace.setText(Messages
				.getString("ControlFlowView.Action.NextProcess")); //$NON-NLS-1$
		nextTrace.setToolTipText(Messages
				.getString("ControlFlowView.Action.NextProcess.ToolTip")); //$NON-NLS-1$
		nextTrace.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ControlFlowView.tmf.UI"), //$NON-NLS-1$
						"icons/elcl16/next_item.gif")); //$NON-NLS-1$

		// prevTrace
		prevTrace = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectPrevTrace();
				}
			}
		};
		prevTrace.setText(Messages
				.getString("ControlFlowView.Action.PreviousProcess")); //$NON-NLS-1$
		prevTrace.setToolTipText(Messages
				.getString("ControlFlowView.Action.PreviousProcess.Tooltip")); //$NON-NLS-1$
		prevTrace.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ControlFlowView.tmf.UI"), //$NON-NLS-1$
						"icons/elcl16/prev_item.gif")); //$NON-NLS-1$

		// showLegend
		showLegend = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.showLegend();
				}
			}
		};
		showLegend.setText(Messages.getString("ControlFlowView.Action.Legend")); //$NON-NLS-1$
		showLegend.setToolTipText(Messages
				.getString("ControlFlowView.Action.Legend.ToolTip")); //$NON-NLS-1$

		// filterTraces
		filterTraces = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.filterTraces();
				}
			}
		};
		filterTraces.setText(Messages
				.getString("ControlFlowView.Action.Filter")); //$NON-NLS-1$
		filterTraces.setToolTipText(Messages
				.getString("ControlFlowView.Action.Filter.ToolTip")); //$NON-NLS-1$
		filterTraces.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ControlFlowView.tmf.UI"), //$NON-NLS-1$
						"icons/elcl16/filter_items.gif")); //$NON-NLS-1$

		// zoomIn
		zoomIn = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.zoomIn();
				}
			}
		};
		zoomIn.setText(Messages.getString("ControlFlowView.Action.ZoomIn")); //$NON-NLS-1$
		zoomIn.setToolTipText(Messages
				.getString("ControlFlowView.Action.ZoomIn.Tooltip")); //$NON-NLS-1$
		zoomIn.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ControlFlowView.tmf.UI"), //$NON-NLS-1$
				"icons/elcl16/zoomin_nav.gif")); //$NON-NLS-1$

		// zoomOut
		zoomOut = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.zoomOut();
				}
			}
		};
		zoomOut.setText(Messages.getString("ControlFlowView.Action.ZoomOut")); //$NON-NLS-1$
		zoomOut.setToolTipText(Messages
				.getString("ControlFlowView.Action.ZoomOut.tooltip")); //$NON-NLS-1$
		zoomOut.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ControlFlowView.tmf.UI"), //$NON-NLS-1$
				"icons/elcl16/zoomout_nav.gif")); //$NON-NLS-1$

		// zoomFilter
		zoomFilter = new Action() {
			@Override
			public void run() {
				// Nothing to do, however the selection status is needed by the
				// application
			}
		};
		zoomFilter.setText(Messages
				.getString("ControlFlowView.Action.ZoomFilter")); //$NON-NLS-1$
		zoomFilter.setToolTipText(Messages
				.getString("ControlFlowView.Action.ZoomFilter.tooltip")); //$NON-NLS-1$
		zoomFilter.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ControlFlowView.tmf.UI"), //$NON-NLS-1$
						"icons/elcl16/filter_items.gif")); //$NON-NLS-1$
		zoomFilter.setChecked(false);

		// PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_SYNCED);

		doubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = tableViewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage("Double-click detected on " + obj.toString()); //$NON-NLS-1$
			}
		};
	}

	private void hookDoubleClickAction() {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(tableViewer.getControl().getShell(),
				Messages.getString("ControlFlowView.msgSlogan"), message); //$NON-NLS-1$
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#
	 * tsfTmProcessSelEvent
	 * (org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeSelectionEvent
	 * )
	 */
	@Override
	public void tsfTmProcessSelEvent(TmfTimeSelectionEvent event) {
		// common implementation
		super.tsfTmProcessSelEvent(event);

		// Reselect the table viewer to widget selection
		ISelection sel = tsfviewer.getSelectionTrace();
		if (sel != null && !sel.isEmpty()) {
			tableViewer.setSelection(sel);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.
	 * ITmfTimeScaleSelectionListener
	 * #tsfTmProcessTimeScaleEvent(org.eclipse.linuxtools
	 * .tmf.ui.viewers.timeAnalysis.TmfTimeScaleSelectionEvent)
	 */
	@Override
	public void tsfTmProcessTimeScaleEvent(TmfTimeScaleSelectionEvent event) {
		super.tsfTmProcessTimeScaleEvent(event);
	}

	private void applyTableLayout(Table table) {
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
			tableColumn.setText(columnNames[i]);
			tableColumn.pack();
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
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
	public void displayModel(final ITmfTimeAnalysisEntry[] items,
			final long startBoundTime, final long endBoundTime,
			final boolean updateTimeBounds, final long startVisibleWindow,
			final long endVisibleWindow, final Object source) {
		
		if(tableViewer != null) {
			final Table table = tableViewer.getTable();
			
			// Ignore update if widget is disposed
			if (table.isDisposed()) return;
			
			Display display = table.getDisplay();

			// Perform the updates on the UI thread)
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!table.isDisposed()) {
						tableViewer.setInput(items); // This shall be the minimal
						// initial
						tableFilter = new ViewProcessFilter(tableViewer);
						tableViewer.setFilters(new ViewerFilter[] { tableFilter });

						resizeTableColumns(table);
						table.update();
						tableViewer.refresh();

						tsfviewer.display(items, startBoundTime, endBoundTime,
								updateTimeBounds);

						// validate visible boundaries
						if (startVisibleWindow > -1 && endVisibleWindow > -1) {
							tsfviewer.setSelectVisTimeWindow(startVisibleWindow,
									endVisibleWindow, source);
						}

						tsfviewer.resizeControls();

						// Adjust asynchronously the size of the vertical scroll bar to fit the
						// contents 
                        tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                if ((scrollFrame != null) && (!scrollFrame.isDisposed())) {
                                    updateScrolls(scrollFrame);
                                }
                            }
                        });
					}
				}
			});
		}
	}

	@Override
	public void dispose() {
		// dispose parent resources
		super.dispose();

		tsfviewer.removeFilterSelectionListner(this);
		tsfviewer.removeWidgetSelectionListner(this);
		tsfviewer.removeWidgetTimeScaleSelectionListner(this);
		tableViewer = null;
		tsfviewer = null;
	}

	/**
	 * @param tableComposite
	 * @param table
	 */
	private synchronized void resizeTableColumns(Table table) {
		if (table != null) {
			TableColumn[] columns = table.getColumns();
			for (TableColumn column : columns) {
				column.pack();
			}
		}
	}

	@Override
	public void tmfTaProcessFilterSelection(TmfTimeFilterSelectionEvent event) {
		if (tableFilter != null) {
			Vector<ITmfTimeAnalysisEntry> filteredout = event.getFilteredOut();
			if (filteredout != null) {
				tableFilter.setFilter(filteredout);
			} else {
				tableFilter.setFilter(new Vector<ITmfTimeAnalysisEntry>());
			}
			tableViewer.refresh();
		}
	}

	/**
	 * @param scrollFrame
	 * @param wrapper
	 */
	private void updateScrolls(final ScrolledComposite scrollFrame) {
		scrollFrame.setMinSize(tableViewer.getTable().computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * Registers as listener of time selection from other views
	 * 
	 * @param signal
	 */
	@Override
	@TmfSignalHandler
	public void synchToTime(TmfTimeSynchSignal signal) {
		super.synchToTime(signal);
	}

	/**
	 * Annotation Registers as listener of time range selection from other views
	 * The implementation handles the entry of the signal.
	 * 
	 * @param signal
	 */
	@TmfSignalHandler
	public void synchToTimeRange(TmfRangeSynchSignal signal) {
		if (zoomFilter != null) {
			synchToTimeRange(signal, zoomFilter.isChecked());
		}
	}

	@Override
	public void modelIncomplete(ILttngSyntEventRequest request) {
		// Nothing to do
		// The data will be refreshed on the next request
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#
	 * getEventProcessor()
	 */
	@Override
	public ITransEventProcessor getEventProcessor() {
		return FlowEventToHandlerFactory.getInstance();
	}

	/**
	 * @param signal
	 */
	@TmfSignalHandler
	public void experimentSelected(
			TmfExperimentSelectedSignal<? extends TmfEvent> signal) {
		if (signal != null) {
			TmfTimeRange experimentTRange = signal.getExperiment()
					.getTimeRange();

			initTimeRange = TmfTimeRange.NULL_RANGE;
			if (experimentTRange != TmfTimeRange.NULL_RANGE) {
				// prepare time intervals in widget
				modelUpdateInit(experimentTRange, experimentTRange, signal
						.getSource());
	
				// request initial data
				initialExperimentDataRequest(signal
						.getSource(), experimentTRange);
			}
		}
	}

	@TmfSignalHandler
	public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal) {
		if (initTimeRange == TmfTimeRange.NULL_RANGE && signal.getExperiment().equals(TmfExperiment.getCurrentExperiment())) {
			TmfTimeRange experimentTRange = signal.getRange();

			if (experimentTRange != TmfTimeRange.NULL_RANGE) {
				// prepare time intervals in widget
				modelUpdateInit(experimentTRange, experimentTRange, signal.getSource());

				// request initial data
				initialExperimentDataRequest(signal.getSource(), experimentTRange);
			}
		}
	}

    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
        if (signal.getExperiment().equals(TmfExperiment.getCurrentExperiment())) {
            final TmfTimeRange range = signal.getExperiment().getTimeRange();
            if (range != TmfTimeRange.NULL_RANGE) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        tsfviewer.setTimeBounds(range.getStartTime().getValue(), range.getEndTime().getValue());
                    }});
            }
        }
    }

	/**
	 * @param source
	 * @param experimentTRange
	 * @return Adjusted time window used for the request (smaller window to
	 *         initialize view)
	 */
	private TmfTimeRange initialExperimentDataRequest(Object source,
			TmfTimeRange experimentTRange) {
		// Adjust the initial time window to a shorter interval to allow
		// user to select the interesting area based on the perspective
		TmfTimeRange initTimeWindow = getInitTRange(experimentTRange);

		eventRequest(initTimeWindow, experimentTRange, true, ExecutionType.FOREGROUND);
		if (TraceDebug.isDEBUG()) {
			TraceDebug.debug("Initialization request time range is: " //$NON-NLS-1$
					+ initTimeWindow.getStartTime().toString() + "-" //$NON-NLS-1$
					+ initTimeWindow.getEndTime().toString());
		}

		initTimeRange = initTimeWindow;
		return initTimeWindow;
	}
	
	/*
	 * SWT doesn't seem to report correctly the table item height, at least in
	 * the case of KDE.
	 * 
	 * This method provides an adjustment term according to the desktop session.
	 * 
	 * @return Height adjustment 
	 */
	private int getTableItemHeightAdjustement() {
		int ajustement = 0;
		String desktopSession = System.getenv("DESKTOP_SESSION"); //$NON-NLS-1$

		if (desktopSession != null) {
	        if (desktopSession.equals("kde")) { //$NON-NLS-1$
	            ajustement = 2;
	        }
		}

		return ajustement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#
	 * getParamsUpdater()
	 */
	@Override
	protected ParamsUpdater getParamsUpdater() {
		return FlowModelFactory.getParamsUpdater();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#
	 * getItemContainer()
	 */
	@Override
	protected ItemContainer<?> getItemContainer() {
		return FlowModelFactory.getProcContainer();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#getProviderId()
	 */
	@Override
	protected int getProviderId() { 
	    return LttngCoreProviderFactory.CONTROL_FLOW_LTTNG_SYTH_EVENT_PROVIDER; 
	}
}
