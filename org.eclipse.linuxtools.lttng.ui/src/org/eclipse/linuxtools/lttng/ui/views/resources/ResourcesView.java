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
 * 	 Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.resources;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.linuxtools.lttng.core.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.lttng.core.request.ILttngSyntEventRequest;
import org.eclipse.linuxtools.lttng.core.state.evProcessor.ITransEventProcessor;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.ItemContainer;
import org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView;
import org.eclipse.linuxtools.lttng.ui.views.common.ParamsUpdater;
import org.eclipse.linuxtools.lttng.ui.views.resources.evProcessor.ResourcesEventToHandlerFactory;
import org.eclipse.linuxtools.lttng.ui.views.resources.model.ResourceModelFactory;
import org.eclipse.linuxtools.lttng.ui.views.resources.model.ResourcesTimeRangeViewerProvider;
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
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITmfTimeScaleSelectionListener;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITmfTimeSelectionListener;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeScaleSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author alvaro
 * 
 */
public class ResourcesView extends AbsTimeUpdateView implements
		ITmfTimeSelectionListener, ITmfTimeScaleSelectionListener {

	// ========================================================================
	// Data
	// ========================================================================
	public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.resources"; //$NON-NLS-1$

	// private int totalNumItems = 0;
	// Actions
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
	private Composite top;

	private TmfTimeRange initTimeRange = TmfTimeRange.Null;

	// private static SimpleDateFormat stimeformat = new SimpleDateFormat(
	// "yy/MM/dd HH:mm:ss");

	// private TraceModelImplFactory fact;

	// ========================================================================
	// Constructor
	// ========================================================================

	/**
	 * The constructor.
	 */
	public ResourcesView() {
		super(ID);
	}

	// ========================================================================
	// Methods
	// ========================================================================

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		top = new Composite(parent, SWT.BORDER);

		top.setLayout(new FillLayout());
		tsfviewer = TmfViewerFactory.createViewer(top, new ResourcesTimeRangeViewerProvider(getParamsUpdater()));

		tsfviewer.addWidgetSelectionListner(this);
		tsfviewer.addWidgetTimeScaleSelectionListner(this);

		// Traces shall not be grouped to allow synchronisation
		tsfviewer.groupTraces(true);
		tsfviewer.setAcceptSelectionAPIcalls(true);

		// Viewer to notify selection to this class
		// This class will synchronise selections with table.
//		tsfviewer.addWidgetSelectionListner(this);
//		tsfviewer.addWidgetTimeScaleSelectionListner(this);

		// Create the help context id for the viewer's control
		// TODO: Associate with help system
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				tsfviewer.getControl(),
				"org.eclipse.linuxtools.lttng.ui.views.resource.view"); //$NON-NLS-1$

		makeActions();
		hookContextMenu();
		contributeToActionBars();

//		// Read relevant values
//		int timeSpaceWidth = tsfviewer.getTimeSpace();
//		if (timeSpaceWidth < 0) {
//			timeSpaceWidth = -timeSpaceWidth;
//		}

		TmfExperiment<?> experiment = TmfExperiment.getCurrentExperiment();
		if (experiment != null) {
			TmfTimeRange experimentTRange = experiment.getTimeRange();
			if (experimentTRange != TmfTimeRange.Null) {
				long time0 = experimentTRange.getStartTime().getValue();
				long time1 = experimentTRange.getEndTime().getValue();
				ParamsUpdater paramUpdater = getParamsUpdater();
				paramUpdater.update(time0, time1); // , timeSpaceWidth);

				// send the initial request and obtained the adjusted time used
				TmfTimeRange adjustedTimeRange = initialExperimentDataRequest(this, experimentTRange);

				// initialize widget time boundaries and filtering parameters
				ModelUpdateInit(experimentTRange, adjustedTimeRange, this);
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
				ResourcesView.this.fillContextMenu(manager);
			}
		});

		Menu menu = menuMgr.createContextMenu(tsfviewer.getControl());
		tsfviewer.getControl().setMenu(menu);
		getSite()
				.registerContextMenu(menuMgr, tsfviewer.getSelectionProvider());
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
		// manager.add(showLegend);
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
		// action4
		resetScale = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.resetStartFinishTime();
				}

			}
		};
		resetScale.setText(Messages.getString("ResourcesView.Action.Reset")); //$NON-NLS-1$
		resetScale.setToolTipText(Messages.getString("ResourcesView.Action.Reset.ToolTip")); //$NON-NLS-1$
		resetScale.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"),	"icons/elcl16/home_nav.gif")); //$NON-NLS-1$ //$NON-NLS-2$

		// action5
		nextEvent = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectNextEvent();
				}
			}
		};
		nextEvent.setText(Messages.getString("ResourcesView.Action.NextEvent")); //$NON-NLS-1$
		nextEvent.setToolTipText(Messages.getString("ResourcesView.Action.NextEvent.Tooltip")); //$NON-NLS-1$
		nextEvent.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"),	"icons/elcl16/next_event.gif"));  //$NON-NLS-1$//$NON-NLS-2$

		// action6
		prevEvent = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectPrevEvent();
				}
			}
		};
		prevEvent.setText(Messages.getString("ResourcesView.Action.PrevEvent")); //$NON-NLS-1$
		prevEvent.setToolTipText(Messages.getString("ResourcesView.Action.PrevEvent.Tooltip")); //$NON-NLS-1$
		prevEvent.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"),	"icons/elcl16/prev_event.gif")); //$NON-NLS-1$//$NON-NLS-2$

		// action7
		nextTrace = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectNextTrace();
				}
			}
		};
		nextTrace.setText(Messages.getString("ResourcesView.Action.NextResource")); //$NON-NLS-1$
		nextTrace.setToolTipText(Messages.getString("ResourcesView.Action.NextResource.ToolTip")); //$NON-NLS-1$
		nextTrace.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"),	"icons/elcl16/next_item.gif")); //$NON-NLS-1$//$NON-NLS-2$

		// action8
		prevTrace = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectPrevTrace();
				}
			}
		};
		prevTrace.setText(Messages.getString("ResourcesView.Action.PreviousResource")); //$NON-NLS-1$
		prevTrace.setToolTipText(Messages.getString("ResourcesView.Action.PreviousResource.Tooltip")); //$NON-NLS-1$
		prevTrace.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"),	"icons/elcl16/prev_item.gif"));  //$NON-NLS-1$//$NON-NLS-2$

		// action9
		showLegend = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.showLegend();
				}
			}
		};
		showLegend.setText(Messages.getString("ResourcesView.Action.Legend")); //$NON-NLS-1$
		showLegend.setToolTipText(Messages.getString("ResourcesView.Action.Legend.ToolTip")); //$NON-NLS-1$

		// action10
		filterTraces = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.filterTraces();
				}
			}
		};
		filterTraces.setText(Messages.getString("ResourcesView.Action.Filter")); //$NON-NLS-1$
		filterTraces.setToolTipText(Messages.getString("ResourcesView.Action.Filter.ToolTip")); //$NON-NLS-1$
		filterTraces.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"),	"icons/elcl16/filter_items.gif"));  //$NON-NLS-1$//$NON-NLS-2$

		// action10
		zoomIn = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.zoomIn();
				}
			}
		};
		zoomIn.setText(Messages.getString("ResourcesView.Action.ZoomIn")); //$NON-NLS-1$
		zoomIn.setToolTipText(Messages.getString("ResourcesView.Action.ZoomIn.Tooltip")); //$NON-NLS-1$
		zoomIn.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"), "icons/elcl16/zoomin_nav.gif"));  //$NON-NLS-1$//$NON-NLS-2$

		// action10
		zoomOut = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.zoomOut();
				}
			}
		};
		zoomOut.setText(Messages.getString("ResourcesView.Action.ZoomOut")); //$NON-NLS-1$
		zoomOut.setToolTipText(Messages.getString("ResourcesView.Action.ZoomOut.tooltip")); //$NON-NLS-1$
		zoomOut.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"),	"icons/elcl16/zoomout_nav.gif"));  //$NON-NLS-1$//$NON-NLS-2$

		// zoomFilter
		zoomFilter = new Action() {
			@Override
			public void run() {
				// Nothing to do, however the selection status is needed by the
				// application
			}
		};

		zoomFilter.setText(Messages.getString("ResourcesView.Action.ZoomFilter")); //$NON-NLS-1$
		zoomFilter.setToolTipText(Messages.getString("ResourcesView.Action.ZoomFilter.tooltip")); //$NON-NLS-1$
		zoomFilter.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"), "icons/elcl16/filter_items.gif"));  //$NON-NLS-1$//$NON-NLS-2$
		zoomFilter.setChecked(false);

		// PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_SYNCED);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		tsfviewer.getControl().setFocus();
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.
	 * ITmfTimeScaleSelectionListener
	 * #tsfTmProcessTimeScaleEvent(org.eclipse.linuxtools
	 * .tmf.ui.viewers.timeAnalysis.TmfTimeScaleSelectionEvent)
	 */
	@Override
	public void tsfTmProcessTimeScaleEvent(TmfTimeScaleSelectionEvent event) {
		super.tsfTmProcessTimeScaleEvent(event);
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
	public void displayModel(final ITmfTimeAnalysisEntry[] items, final long startBoundTime,
			final long endBoundTime, final boolean updateTimeBounds, final long startVisibleWindow,
			final long endVisibleWindow, final Object source) {
		
		// Return if disposed
		if ((tsfviewer == null) || (tsfviewer.getControl().isDisposed())) return;
		
		Display display = tsfviewer.getControl().getDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if ((tsfviewer != null) && (!tsfviewer.getControl().isDisposed())) {
					tsfviewer.display(items, startBoundTime, endBoundTime, updateTimeBounds);
					// validate visible boundaries
					if (startVisibleWindow > -1 && endVisibleWindow > -1) {
						tsfviewer.setSelectVisTimeWindow(startVisibleWindow, endVisibleWindow, source);
					}
					tsfviewer.resizeControls();
				}
			}
		});
	}

	@Override
	public void dispose() {
		// dispose parent resources
		super.dispose();

		tsfviewer.removeWidgetSelectionListner(this);
		tsfviewer.removeWidgetTimeScaleSelectionListner(this);
		tsfviewer = null;
	}

	/**
	 * Registers as listener of time selection from other tmf views
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
		return ResourcesEventToHandlerFactory.getInstance();
	}

	/**
	 * @param signal
	 */
	@TmfSignalHandler
	public void experimentSelected(TmfExperimentSelectedSignal<? extends TmfEvent> signal) {
		if (signal != null) {
			TmfTimeRange experimentTRange = signal.getExperiment().getTimeRange();

			initTimeRange = TmfTimeRange.Null;
			if (experimentTRange != TmfTimeRange.Null) {
				// prepare time intervals in widget
				ModelUpdateInit(experimentTRange, experimentTRange, signal.getSource());

				// request initial data
				initialExperimentDataRequest(signal.getSource(), experimentTRange);
			}
		}
	}

	@TmfSignalHandler
	public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal) {
		if (initTimeRange == TmfTimeRange.Null && signal.getExperiment().equals(TmfExperiment.getCurrentExperiment())) {
			TmfTimeRange experimentTRange = signal.getRange();

			if (experimentTRange != TmfTimeRange.Null) {
				// prepare time intervals in widget
				ModelUpdateInit(experimentTRange, experimentTRange, signal.getSource());

				// request initial data
				initialExperimentDataRequest(signal.getSource(), experimentTRange);
			}
		}
	}

    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
        if (signal.getExperiment().equals(TmfExperiment.getCurrentExperiment())) {
            final TmfTimeRange range = signal.getExperiment().getTimeRange();
            if (range != TmfTimeRange.Null) {
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
	private TmfTimeRange initialExperimentDataRequest(Object source, TmfTimeRange experimentTRange) {
		// Adjust the initial time window to a shorter interval to allow
		// user to select the interesting area based on the perspective
		TmfTimeRange initTimeWindow = getInitTRange(experimentTRange);

		eventRequest(initTimeWindow, experimentTRange, true, ExecutionType.FOREGROUND);
		if (TraceDebug.isDEBUG()) {
			TraceDebug.debug("Initialization request time range is: " + initTimeWindow.getStartTime().toString() + "-" //$NON-NLS-1$ //$NON-NLS-2$
					+ initTimeWindow.getEndTime().toString());
		}

		initTimeRange = initTimeWindow;
		return initTimeWindow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#
	 * getParamsUpdater()
	 */
	@Override
	protected ParamsUpdater getParamsUpdater() {
		return ResourceModelFactory.getParamsUpdater();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#
	 * getItemContainer()
	 */
	@Override
	protected ItemContainer<?> getItemContainer() {
		return ResourceModelFactory.getResourceContainer();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.common.AbsTimeUpdateView#getProviderId()
	 */
	@Override
    protected int getProviderId() { 
        return LttngCoreProviderFactory.RESOURCE_LTTNG_SYTH_EVENT_PROVIDER; 
    }
}