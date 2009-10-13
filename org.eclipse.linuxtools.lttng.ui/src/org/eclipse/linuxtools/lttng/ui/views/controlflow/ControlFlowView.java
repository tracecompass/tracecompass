/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Alvaro Sanchez-Leon - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.controlflow;

import java.util.Arrays;
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
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.state.StateDataRequest;
import org.eclipse.linuxtools.lttng.state.StateManager;
import org.eclipse.linuxtools.lttng.state.evProcessor.EventProcessorProxy;
import org.eclipse.linuxtools.lttng.state.experiment.StateExperimentManager;
import org.eclipse.linuxtools.lttng.state.experiment.StateManagerFactory;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventProcess;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeViewerProvider;
import org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView;
import org.eclipse.linuxtools.lttng.ui.views.common.ParamsUpdater;
import org.eclipse.linuxtools.lttng.ui.views.controlflow.evProcessor.FlowTRangeUpdateFactory;
import org.eclipse.linuxtools.lttng.ui.views.controlflow.model.FlowModelFactory;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewerFactory;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITimeAnalysisViewer;
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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
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
 * <p>
 * TODO: Implement me. Please.
 */
/**
 * @author alvaro
 * 
 */
public class ControlFlowView extends AbsTimeUpdateView implements
		ITmfTimeSelectionListener, ITmfTimeScaleSelectionListener,
		ITmfTimeFilterSelectionListener {

    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.controlflow";
    
	// ========================================================================
	// Table data
	// ========================================================================
	private final String PROCESS_COLUMN = "Process";
	private final String BRAND_COLUMN = "Brand";
	private final String PID_COLUMN = "PID";
	private final String TGID_COLUMN = "TGID";
	private final String PPID_COLUMN = "PPID";
	private final String CPU_COLUMN = "CPU";
	private final String BIRTH_SEC_COLUMN = "Birth sec";
	private final String BIRTH_NSEC_COLUMN = "Birth nsec";
	private final String TRACE = "TRACE";

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
	private Action synch;

	private ITimeAnalysisViewer tsfviewer;
	private ViewProcessFilter tableFilter = null;
	private ScrolledComposite scrollFrame = null;
	private Composite wrapper = null;
	private Composite top;

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

	class ViewContentProvider implements
	/* ILazyContentProvider, */IStructuredContentProvider {
		private TableViewer cviewer = null;
		private ITmfTimeAnalysisEntry[] elements = null;

		public ViewContentProvider(TableViewer v) {
			cviewer = v;
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			this.elements = (ITmfTimeAnalysisEntry[]) newInput;
			if (elements != null) {
				TraceDebug
						.debug("Total number of processes provided to Control Flow view: "
								+ elements.length);
			} else {
				TraceDebug.debug("New input = null");
			}
		}

		public void dispose() {

		}

		// Needed with the use of virtual tables in order to initialize items
		// which were not initially visible.
		public void updateElement(int index) {
			cviewer.replace(elements[index], index);
		}

		// @Override
		public Object[] getElements(Object inputElement) {
			// TODO Auto-generated method stub
			return elements;
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
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

	class ViewProcessFilter extends ViewerFilter {

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
				TraceDebug.debug("Unexpected type of filter element received: "
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
	@Override
	public void createPartControl(Composite parent) {
		top = new Composite(parent, SWT.BORDER);

		top.setLayout(new FillLayout());

		scrollFrame = new ScrolledComposite(top, SWT.V_SCROLL | SWT.H_SCROLL);
		scrollFrame.setBounds(top.getClientArea());

		wrapper = new Composite(scrollFrame, SWT.NONE);
		scrollFrame.setEnabled(true);
		scrollFrame.setRedraw(true);
		scrollFrame.setExpandVertical(true);
		scrollFrame.setExpandHorizontal(true);
		scrollFrame.setContent(wrapper);
		scrollFrame.setAlwaysShowScrollBars(true);
		wrapper.setLayout(new FillLayout());

		SashForm sash = new SashForm(wrapper, SWT.NONE);
		final Composite tableComposite = new Composite(sash, SWT.NO_SCROLL);
		FillLayout layout = new FillLayout();
		tableComposite.setLayout(layout);
		tableViewer = new TableViewer(tableComposite, SWT.FULL_SELECTION
				| SWT.H_SCROLL);
		tableViewer.setContentProvider(new ViewContentProvider(tableViewer));
		tableViewer.setLabelProvider(new ViewLabelProvider());
		Table table = tableViewer.getTable();
		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						ISelection sel = event.getSelection();
						if (!sel.isEmpty()) {
							Object firstSel = null;
							if (sel instanceof IStructuredSelection) {
								firstSel = ((IStructuredSelection) sel)
										.getFirstElement();

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

							int visibleHeight = scrollFrame.getSize().y
									- hscrolly;

							// the current scrollbar offset to adjust i.e. start
							// of
							// the visible window
							Point origin = scrollFrame.getOrigin();
							// end of visible window
							int endy = origin.y + visibleHeight;

							int itemStartPos = itemRect.y
									+ table.getHeaderHeight()
									+ table.getBorderWidth()
									+ table.getParent().getBorderWidth();

							// Item End Position
							int itemEndPos = itemStartPos + step;

							// check if need to go up
							if (origin.y >= step && itemStartPos < origin.y) {
								// one step up
								scrollFrame
										.setOrigin(origin.x, origin.y - step);

							}

							// check if it needs to go down
							if (itemEndPos > endy) {
								// one step down
								scrollFrame
										.setOrigin(origin.x, origin.y + step);

							}
						}
					}
				});

		// Listen to page up /down and Home / Enc keys
		tableViewer.getTable().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				Table table = tableViewer.getTable();
				Point origin = scrollFrame.getOrigin();
				if (table == null || table.getItemCount() < 1) {
					// nothing to page
					return;
				}

				TableItem item;
				int count;

				switch (e.keyCode) {
				case SWT.PAGE_DOWN:
					updateScrollPageDown();
					break;
				case SWT.PAGE_UP:
					updateScrollUp();
					break;
				case SWT.HOME:
					// Home
					count = table.getItemCount();
					item = table.getItem(0);
					// Go to the top
					scrollFrame.setOrigin(origin.x, 0);
					break;
				case SWT.END:
					// End Selected
					count = table.getItemCount();
					item = table.getItem(count - 1);
					int itemStartPos = item.getBounds().y;
					// Get to the bottom
					scrollFrame.setOrigin(origin.x, itemStartPos);
					break;
				default:
					break;
				}
			}

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

		int itemHeight = table.getItemHeight();
		int headerHeight = table.getHeaderHeight();
		table.getVerticalBar().setVisible(false);

		tsfviewer = TmfViewerFactory.createViewer(sash,
				new TimeRangeViewerProvider());

		tsfviewer.addWidgetSelectionListner(this);
		tsfviewer.addWidgetTimeScaleSelectionListner(this);

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

		scrollFrame.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				tsfviewer.resizeControls();
				updateScrolls(scrollFrame, wrapper);
			}
		});

		tableComposite.addControlListener(new ControlListener() {
			public void controlResized(ControlEvent e) {
				scrollFrame.getParent().update();
			}

			public void controlMoved(ControlEvent e) {

			}
		});

		// Register the updater in charge to refresh elements as we update the
		// time ranges
		// FlowParamsUpdater listener = FlowModelFactory.getParamsUpdater();
		// tsfviewer.addWidgetTimeScaleSelectionListner(listener);

		// Register this view to receive updates when the model is updated with
		// fresh info
		// ModelListenFactory.getRegister().addFlowModelUpdatesListener(this);

		// Register the event processor factory in charge of event handling
		EventProcessorProxy.getInstance().addEventProcessorFactory(
				FlowTRangeUpdateFactory.getInstance());

		// set the initial view parameter values
		// Experiment start and end time
		// as well as time space width in pixels, used by the time analysis
		// widget
		ParamsUpdater paramUpdater = FlowModelFactory.getParamsUpdater();
		StateExperimentManager experimentManger = StateManagerFactory
				.getExperimentManager();
		// Read relevant values
		int timeSpaceWidth = tsfviewer.getTimeSpace();
		TmfTimeRange timeRange = experimentManger.getExperimentTimeRange();
		if (timeRange != null) {
			long time0 = timeRange.getStartTime().getValue();
			long time1 = timeRange.getEndTime().getValue();
			paramUpdater.update(time0, time1, timeSpaceWidth);
		}

		experimentManger.readExperiment("flowView", this);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
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
		// manager.add(showLegend);
		manager.add(new Separator());
		manager.add(resetScale);
		manager.add(nextEvent);
		manager.add(prevEvent);
		manager.add(nextTrace);
		manager.add(prevTrace);
		// manager.add(filterTraces);
		manager.add(zoomIn);
		manager.add(zoomOut);
		manager.add(synch);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		// manager.add(showLegend);
		manager.add(new Separator());
		manager.add(resetScale);
		manager.add(nextEvent);
		manager.add(prevEvent);
		manager.add(nextTrace);
		manager.add(prevTrace);
		// manager.add(showLegend);
		// manager.add(filterTraces);
		manager.add(zoomIn);
		manager.add(zoomOut);
		manager.add(synch);
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		// manager.add(showLegend);
		manager.add(new Separator());
		manager.add(resetScale);
		manager.add(nextEvent);
		manager.add(prevEvent);
		manager.add(nextTrace);
		manager.add(prevTrace);
		// manager.add(filterTraces);
		manager.add(zoomIn);
		manager.add(zoomOut);
		manager.add(synch);
		manager.add(new Separator());
	}

	private void makeActions() {
		// action4
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
						.getString("ControlFlowView.tmf.UI"),
						"icons/home_nav.gif"));

		// action5
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
						.getString("ControlFlowView.tmf.UI"),
						"icons/next_event.gif"));

		// action6
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
						.getString("ControlFlowView.tmf.UI"),
						"icons/prev_event.gif"));

		// action7
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
						.getString("ControlFlowView.tmf.UI"),
						"icons/next_item.gif"));

		// action8
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
						.getString("ControlFlowView.tmf.UI"),
						"icons/prev_item.gif"));

		// action9
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

		// action10
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
						.getString("ControlFlowView.tmf.UI"),
						"icons/filter_items.gif"));

		// action10
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
				Messages.getString("ControlFlowView.tmf.UI"),
				"icons/zoomin_nav.gif"));

		// action10
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
				Messages.getString("ControlFlowView.tmf.UI"),
				"icons/zoomout_nav.gif"));

		// action11
		synch = new Action() {
			@Override
			public void run() {
				// Note: No action since the synch flag is used by Control flow
				// view
				// the actual viewer is set to accept api selections in
				// createpartcontrol.

				// if (synch.isChecked()) {
				// tsfviewer.setAcceptSelectionAPIcalls(true);
				// } else {
				// tsfviewer.setAcceptSelectionAPIcalls(false);
				// }
			}
		};
		synch.setText(Messages.getString("ControlFlowView.Action.Synchronize")); //$NON-NLS-1$
		synch.setToolTipText(Messages
				.getString("ControlFlowView.Action.Synchronize.ToolTip")); //$NON-NLS-1$
		synch.setChecked(false);
		synch.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ControlFlowView.tmf.UI"),
				"icons/synced.gif"));
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

	public void tsfTmProcessSelEvent(TmfTimeSelectionEvent event) {
		Object source = event.getSource();
		if (source == null) {
			return;
		}

		// Reselect the table viewer to widget selection
		ISelection sel = tsfviewer.getSelectionTrace();
		if (sel != null && !sel.isEmpty()) {
			tableViewer.setSelection(sel);
		}

		ParamsUpdater paramUpdater = FlowModelFactory.getParamsUpdater();
		Long savedSelTime = paramUpdater.getSelectedTime();

		long selTimens = event.getSelectedTime();

		// make sure the new selected time is different than saved before
		// executing update
		if (savedSelTime == null || savedSelTime != selTimens) {
			// Notify listener views.
			synchTimeNotification(selTimens);

			// Update the parameter updater to save the selected time
			paramUpdater.setSelectedTime(selTimens);

			if (TraceDebug.isDEBUG()) {
				// Object selection = event.getSelection();
				TraceDebug.debug("Selected Time in control Flow View: "
						+ new LttngTimestamp(selTimens));
			}
		}
	}

	public synchronized void tsfTmProcessTimeScaleEvent(
			TmfTimeScaleSelectionEvent event) {
		// source needed to keep track of source values
		Object source = event.getSource();

		if (source != null) {
			// Update the parameter updater before carrying out a read request
			ParamsUpdater paramUpdater = FlowModelFactory.getParamsUpdater();
			boolean newParams = paramUpdater.processTimeScaleEvent(event);

			if (newParams) {
				// Read the updated time window
				TmfTimeRange trange = paramUpdater.getTrange();
				if (trange != null) {
					// Request new data for specified time range
					dataRequest(trange);
				}
			}
		}
	}

	/**
	 * Obtains the remainder fraction on unit Seconds of the entered value in
	 * nanoseconds. e.g. input: 1241207054171080214 ns The number of seconds can
	 * be obtain by removing the last 9 digits: 1241207054 the fractional
	 * portion of seconds, expressed in ns is: 171080214
	 * 
	 * @param v
	 * @return
	 */
	public String formatNs(long v) {
		StringBuffer str = new StringBuffer();
		boolean neg = v < 0;
		if (neg) {
			v = -v;
			str.append('-');
		}

		String strVal = String.valueOf(v);
		if (v < 1000000000) {
			return strVal;
		}

		// Extract the last nine digits (e.g. fraction of a S expressed in ns
		return strVal.substring(strVal.length() - 9);
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

	/**
	 * @param items
	 * @param startTime
	 * @param endTime
	 * @param updateTimeBounds - Update needed e.g. a new Experiment or trace selected
	 */
	public void flowModelUpdates(final ITmfTimeAnalysisEntry[] items,
			final long startTime, final long endTime,
			final boolean updateTimeBounds) {
		final Table table = tableViewer.getTable();
		Display display = table.getDisplay();

		// Perform the updates on the UI thread)
		display.asyncExec(new Runnable() {
			public void run() {

				tableViewer.setInput(items); // This shall be the minimal
				// initial
				tableFilter = new ViewProcessFilter(tableViewer);
				tableViewer.setFilters(new ViewerFilter[] { tableFilter });

				resizeTableColumns(table);
				table.update();
				tableViewer.refresh();

				tsfviewer.display(items, startTime, endTime, updateTimeBounds);
				tsfviewer.resizeControls();

				// Adjust the size of the vertical scroll bar to fit the
				// contents
				if (scrollFrame != null && wrapper != null) {
					updateScrolls(scrollFrame, wrapper);
					// scrollFrame.update();
				}
			}
		});
	}

	@Override
	public void dispose() {
		// dispose parent resources
		super.dispose();
		// Remove the event processor factory
		EventProcessorProxy.getInstance().removeEventProcessorFactory(
				FlowTRangeUpdateFactory.getInstance());

		// Remove listener to model updates
		// ModelListenFactory.getRegister().removeFlowModelUpdatesListener(this);
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
			Composite parent = table.getParent();
			int tableWidthSum = parent.getBorderWidth();

			TableColumn[] columns = table.getColumns();
			for (TableColumn column : columns) {
				column.pack();
				tableWidthSum += column.getWidth();
			}
		}
	}

	// @Override
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
	private void updateScrolls(final ScrolledComposite scrollFrame,
			final Composite wrapper) {

		Point ptSize = wrapper.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		wrapper.setSize(ptSize);
		scrollFrame.setMinSize(ptSize);

		// calculate the increment area considering the table header height and
		// borders
		Rectangle area = top.getBounds();
		int marginsHeight = tableViewer.getTable().getHeaderHeight();
		marginsHeight -= top.getBorderWidth() + wrapper.getBorderWidth();
		area.height -= marginsHeight;

		// set page vertical increment area
		ScrollBar verBar = scrollFrame.getVerticalBar();
		ScrollBar horBar = scrollFrame.getHorizontalBar();
		if (verBar != null) {
			verBar.setPageIncrement(area.height);
		}
		if (horBar != null) {
			horBar.setPageIncrement(area.width);
		}

	}

	/**
	 * Trigger time synchronisation to other views this method shall be called
	 * when a check has been performed to note that an actual change of time has
	 * been performed vs a pure re-selection of the same time
	 * 
	 * @param time
	 */
	private void synchTimeNotification(long time) {
		// if synchronisation selected
		if (synch.isChecked()) {
			// Notify other views
			TmfSignalManager.dispatchSignal(new TmfTimeSynchSignal(this,
					new LttngTimestamp(time)));
		}
	}

	/**
	 * Registers as listener of time selection from other tmf views
	 * 
	 * @param signal
	 */
	@TmfSignalHandler
	public void synchToTime(TmfTimeSynchSignal signal) {
		if (synch.isChecked()) {
			Object source = signal.getSource();
			if (signal != null && source != null && source != this) {
				// Internal value is expected in nano seconds.
				long selectedTime = signal.getCurrentTime().getValue();
				if (tsfviewer != null) {
					tsfviewer.setSelectedTime(selectedTime, true, source);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.ui.views.common.LttngTimeUpdateView#waitCursor
	 * (boolean)
	 */
	@Override
	protected synchronized void waitCursor(final boolean waitInd) {
		if (tsfviewer != null) {
			Display display = tsfviewer.getControl().getDisplay();

			// Perform the updates on the UI thread
			display.asyncExec(new Runnable() {
				public void run() {
					tsfviewer.waitCursor(waitInd);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#
	 * ModelUpdatePrep(java.lang.String, boolean)
	 */
	@Override
	public void ModelUpdatePrep(String traceId, boolean clearAllData,
			TmfTimeRange trange) {
		if (clearAllData) {
			FlowModelFactory.getProcContainer().clearProcesses();
			// Obtain the current process list
			Vector<TimeRangeEventProcess> processList = FlowModelFactory
					.getProcContainer().readProcesses();
			// convert it to an Array as expected by the widget
			TimeRangeEventProcess[] processArr = processList
					.toArray(new TimeRangeEventProcess[processList.size()]);

			// initialise to an empty model
			flowModelUpdates(processArr, -1, -1, false);
		} else {
			FlowModelFactory.getProcContainer().clearChildren(traceId);
		}

		ParamsUpdater updater = FlowModelFactory.getParamsUpdater();
		// Start over
		updater.setEventsDiscarded(0);

		// Update new visible time range if available
		if (trange != null) {
			updater.update(trange.getStartTime().getValue(), trange
					.getEndTime().getValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.ui.views.common.LttngTimeUpdateView#
	 * ModelUpdateComplete(org.eclipse.linuxtools.lttng.state.StateDataRequest)
	 */
	@Override
	public void ModelUpdateComplete(StateDataRequest request) {
		long experimentStartTime = -1;
		long experimentEndTime = -1;
		StateManager smanager = request.getStateManager();
		TmfTimeRange experimentTimeRange = smanager.getExperimentTimeWindow();
		if (experimentTimeRange != null) {
			experimentStartTime = experimentTimeRange.getStartTime().getValue();
			experimentEndTime = experimentTimeRange.getEndTime().getValue();
		}
		// Obtain the current process list
		Vector<TimeRangeEventProcess> processList = FlowModelFactory
				.getProcContainer().readProcesses();
		// convert it to an Array as expected by the widget
		TimeRangeEventProcess[] processArr = processList
				.toArray(new TimeRangeEventProcess[processList.size()]);
		// Sort the array by pid
		Arrays.sort(processArr);

		// Update the view part
		flowModelUpdates(processArr, experimentStartTime, experimentEndTime,
				request.isclearDataInd());

		// get back to user selected time if still within range
		ParamsUpdater paramUpdater = FlowModelFactory.getParamsUpdater();
		final Long selTime = paramUpdater.getSelectedTime();
		if (selTime != null) {
			Display display = tsfviewer.getControl().getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					tsfviewer.setSelectedTime(selTime, false, this);
				}
			});
		}

		if (TraceDebug.isDEBUG()) {
			int eventCount = 0;
			Long count = smanager.getEventCount();
			for (TimeRangeEventProcess process : processList) {
				eventCount += process.getTraceEvents().size();
			}

			int discarded = FlowModelFactory.getParamsUpdater()
					.getEventsDiscarded();
			int discardedOutofOrder = FlowModelFactory.getParamsUpdater()
					.getEventsDiscardedWrongOrder();
			TmfTimeRange range = request.getRange();
			StringBuilder sb = new StringBuilder(
					"Events handled: "
							+ count
							+ " Events loaded in Control Flow view: "
							+ eventCount
							+ " Number of events discarded: "
							+ discarded
							+ "\n\tNumber of events discarded with start time earlier than next good time: "
							+ discardedOutofOrder);

			sb.append("\n\t\tRequested Time Range: " + range.getStartTime()
					+ "-" + range.getEndTime());
			sb.append("\n\t\tExperiment Time Range: " + experimentStartTime
					+ "-" + experimentEndTime);
			TraceDebug.debug(sb.toString());
		}
	}
}