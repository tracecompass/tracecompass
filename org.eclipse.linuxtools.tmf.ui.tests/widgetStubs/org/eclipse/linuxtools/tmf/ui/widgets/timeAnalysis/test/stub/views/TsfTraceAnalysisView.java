/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewerFactory;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITimeAnalysisViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITmfTimeScaleSelectionListener;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITmfTimeSelectionListener;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeScaleSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.adaption.TsfImplProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.model.EventImpl;
import org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.model.TraceImpl;
import org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.model.TraceModelImplFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

@SuppressWarnings("nls")
public class TsfTraceAnalysisView extends ViewPart implements
		ITmfTimeSelectionListener, ITmfTimeScaleSelectionListener {

	// ========================================================================
	// Data
	// ========================================================================
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action actGroup;
	private Action resetScale;
	private Action nextEvent;
	private Action prevEvent;
	private Action nextTrace;
	private Action prevTrace;
	private Action showLegent;
	private Action filterTraces;
	private Action zoomIn;
	private Action zoomOut;
	private Action synch;
	private Action events300K;

	private Action doubleClickAction;
	private ITimeAnalysisViewer tsfviewer;
	private ITimeAnalysisViewer tsfviewer2;

	private static SimpleDateFormat stimeformat = new SimpleDateFormat(
			"yy/MM/dd HH:mm:ss");
	private TraceModelImplFactory fact;

	// ========================================================================
	// Inner Classes
	// ========================================================================
	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class TreeObject implements IAdaptable {
		private String name;
		private TreeParent parent;

		public TreeObject(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setParent(TreeParent parent) {
			this.parent = parent;
		}

		public TreeParent getParent() {
			return parent;
		}

		@Override
		public String toString() {
			return getName();
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class key) {
			return null;
		}
	}

	class TreeParent extends TreeObject {
		private ArrayList<TreeObject> children;

		public TreeParent(String name) {
			super(name);
			children = new ArrayList<TreeObject>();
		}

		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}

		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}

		public TreeObject[] getChildren() {
			return children.toArray(new TreeObject[children
					.size()]);
		}

		public boolean hasChildren() {
			return children.size() > 0;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {
		private TreeParent invisibleRoot;

		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot == null)
					initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		@Override
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject) child).getParent();
			}
			return null;
		}

		@Override
		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent) parent).getChildren();
			}
			return new Object[0];
		}

		@Override
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent) parent).hasChildren();
			return false;
		}

		/*
		 * We will set up a dummy model to initialize tree heararchy. In a real
		 * code, you will connect to a real model and expose its hierarchy.
		 */
		private void initialize() {
			TreeObject to1 = new TreeObject("Leaf 1");
			TreeObject to2 = new TreeObject("Leaf 2");
			TreeObject to3 = new TreeObject("Leaf 3");
			TreeParent p1 = new TreeParent("Parent 1");
			p1.addChild(to1);
			p1.addChild(to2);
			p1.addChild(to3);

			TreeObject to4 = new TreeObject("Leaf 4");
			TreeParent p2 = new TreeParent("Parent 2");
			p2.addChild(to4);

			TreeParent root = new TreeParent("Root");
			root.addChild(p1);
			root.addChild(p2);

			invisibleRoot = new TreeParent("");
			invisibleRoot.addChild(root);
		}
	}

	class ViewLabelProvider extends LabelProvider {

		@Override
		public String getText(Object obj) {
			return obj.toString();
		}

		@Override
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof TreeParent)
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					imageKey);
		}
	}

	class NameSorter extends ViewerSorter {
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
		final SashForm sashForm = new SashForm(parent, SWT.NONE);
		final SashForm sashForm2 = new SashForm(sashForm, SWT.NONE);

		tsfviewer = TmfViewerFactory.createViewer(sashForm2,
				new TsfImplProvider());
		tsfviewer2 = TmfViewerFactory.createViewer(sashForm2,
				new TsfImplProvider());

		viewer = new TreeViewer(sashForm, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		sashForm.setWeights(new int[] { 5, 1 });
		sashForm2.setWeights(new int[] { 1, 1 });

		fact = new TraceModelImplFactory();
		ITmfTimeAnalysisEntry[] traceArr = fact.createTraces();
		tsfviewer.display(traceArr);
		tsfviewer.addWidgetSelectionListner(this);
		tsfviewer.addWidgetTimeScaleSelectionListner(this);
		tsfviewer.setTimeCalendarFormat(true);

		tsfviewer2.display(traceArr);
		tsfviewer2.addWidgetSelectionListner(this);
		tsfviewer2.addWidgetTimeScaleSelectionListner(this);
		// tsfviewer2.setTimeFormat(ITimeAnalysisViewer.timeFormat.epoch);

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				TsfTraceAnalysisView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(new Separator());
		manager.add(actGroup);
		manager.add(resetScale);
		manager.add(nextEvent);
		manager.add(prevEvent);
		manager.add(nextTrace);
		manager.add(prevTrace);
		manager.add(showLegent);
		manager.add(filterTraces);
		manager.add(zoomIn);
		manager.add(zoomOut);
		manager.add(synch);
		manager.add(events300K);
		manager.add(new Separator());

		drillDownAdapter.addNavigationActions(manager);
	}

	private ITimeAnalysisViewer getActiveTsfCtrl() {
		ITimeAnalysisViewer inFocusViewer = null;
		if (tsfviewer.isInFocus())
			inFocusViewer = tsfviewer;
		else if (tsfviewer2.isInFocus())
			inFocusViewer = tsfviewer2;
		return inFocusViewer;
	}

	private void makeActions() {
		// action1
		action1 = new Action() {
			@Override
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		// action2
		action2 = new Action() {
			@Override
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		// action3
		actGroup = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					if (actGroup.isChecked()) {
						inFocusViewer.groupTraces(false);
					} else {
						inFocusViewer.groupTraces(true);
					}
				}
			}
		};
		actGroup.setText("Group");
		actGroup.setToolTipText("Groupped or flat list");
		actGroup.setChecked(true);

		// action4
		resetScale = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					inFocusViewer.resetStartFinishTime();
				}

			}
		};
		resetScale.setText("Reset");
		resetScale.setToolTipText("Reset the Time Scale to Default");

		// action5
		nextEvent = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					inFocusViewer.selectNextEvent();
				}
			}
		};
		nextEvent.setText("NextEv");
		nextEvent.setToolTipText("Next Event");

		// action6
		prevEvent = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					inFocusViewer.selectPrevEvent();
				}
			}
		};
		prevEvent.setText("PrevEv");
		prevEvent.setToolTipText("Previous Event");

		// action7
		nextTrace = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					inFocusViewer.selectNextTrace();
				}
			}
		};
		nextTrace.setText("NextTrace");
		nextTrace.setToolTipText("Select Next Event");

		// action8
		prevTrace = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					inFocusViewer.selectPrevTrace();
				}
			}
		};
		prevTrace.setText("PrevTrace");
		prevTrace.setToolTipText("Select Previous Trace");

		// action9
		showLegent = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					inFocusViewer.showLegend();
				}
			}
		};
		showLegent.setText("Legend");
		showLegent.setToolTipText("Show Legend");

		// action10
		filterTraces = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					inFocusViewer.filterTraces();
				}
			}
		};
		filterTraces.setText("Filter");
		filterTraces.setToolTipText("Trace Filter options");

		// action10
		zoomIn = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					inFocusViewer.zoomIn();
				}
			}
		};
		zoomIn.setText("Zoom In");
		zoomIn.setToolTipText("Zoom In");

		// action10
		zoomOut = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					inFocusViewer.zoomOut();
				}
				// ISelection selection = inFocusViewer.getSelection();
				// Object sel = null;
				// if (selection != null && !selection.isEmpty()) {
				// sel = ((IStructuredSelection) selection)
				// .getFirstElement();
				// if (sel instanceof EventImpl) {
				// EventImpl event = (EventImpl) sel;
				// inFocusViewer.selectNextEvent();
				// }
				// }
			}
		};
		zoomOut.setText("Zoom Out");
		zoomOut.setToolTipText("Zoom Out");

		// action11
		synch = new Action() {
			@Override
			public void run() {
				if (synch.isChecked()) {
					tsfviewer.setAcceptSelectionAPIcalls(true);
					tsfviewer2.setAcceptSelectionAPIcalls(true);
				} else {
					tsfviewer.setAcceptSelectionAPIcalls(false);
					tsfviewer2.setAcceptSelectionAPIcalls(false);
				}
			}
		};
		synch.setText("Synchronize");
		synch
				.setToolTipText("Synchronize by listening to external API selection calls");
		synch.setChecked(false);

		// action12
		events300K = new Action() {
			@Override
			public void run() {
				ITimeAnalysisViewer inFocusViewer = getActiveTsfCtrl();
				if (inFocusViewer != null) {
					ITmfTimeAnalysisEntry[] traceArr = fact
							.createLargeTraces(60);
					inFocusViewer.display(traceArr);
				}
			}
		};
		events300K.setText("300K Events");
		events300K.setToolTipText("Add 300K Events");

		doubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"TsfTrace Analysis View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@SuppressWarnings("deprecation")
    @Override
	public void tsfTmProcessSelEvent(TmfTimeSelectionEvent event) {
		Object source = event.getSource();
		if (source == null || !(source instanceof ITimeAnalysisViewer)) {
			return;
		}

		ITimeAnalysisViewer rViewer = (ITimeAnalysisViewer) event.getSource();
		ITimeAnalysisViewer synchViewer = null;
		// Synchronize viewer selections if Enabled,
		// make sure the selection does not go in loops
		if (tsfviewer == rViewer) {
			synchViewer = tsfviewer2;
		} else {
			synchViewer = tsfviewer;
		}
		Object selection = event.getSelection();

		long selTimens = event.getSelectedTime();
		long tms = (long) (selTimens * 1E-6);
		Date date = new Date(tms);
		String fDate = stimeformat.format(date);
		String ns = formatNs(selTimens);

		if (selection instanceof EventImpl) {
			EventImpl selEvent = (EventImpl) selection;
			System.out
					.println("TsfTmIncubatorListener.tsfTmProcessEvent() Selected Event: \nType: "
							+ selEvent.getType().toString()
							+ "\nTime: "
							+ selEvent.getTime()
							+ "\nTrace Name: "
							+ selEvent.getEntry().getName()
							+ "\nSelection Type: "
							+ event.getDType().toString()
							+ "\nSelected Time: "
							+ selTimens + " " + fDate + " " + ns);

			synchViewer.setSelectedEvent(selEvent, source);

		} else if (selection instanceof TraceImpl) {
			TraceImpl selTrace = (TraceImpl) selection;
			System.out
					.println("TsfTmIncubatorListener.tsfTmProcessEvent() Selected Trace: \nName: "
							+ selTrace.getName().toString()
							+ "\nClass Name: "
							+ selTrace.getClassName()
							+ "\nNumber of Events: "
							+ selTrace.getTraceEvents().size()
							+ "\nSelection Type: "
							+ event.getDType().toString()
							+ "\nSelected Time: "
							+ selTimens + " " + fDate + " " + ns);

			synchViewer.setSelectedTraceTime(selTrace, event.getSelectedTime(),
					source);
		} else {
			System.out
					.println("TsfTmIncubatorListener.tsfTmProcessEvent() Unexpected event source received: "
							+ selection.getClass().getName());
		}

	}

	@Override
	public void tsfTmProcessTimeScaleEvent(TmfTimeScaleSelectionEvent event) {
		Object source = event.getSource();
		if (source == null || !(source instanceof ITimeAnalysisViewer)) {
			return;
		}

		if (event != null && event instanceof TmfTimeScaleSelectionEvent) {
			TmfTimeScaleSelectionEvent rEvent = (TmfTimeScaleSelectionEvent) event;
			ITimeAnalysisViewer rViewer = (ITimeAnalysisViewer) event
					.getSource();
			ITimeAnalysisViewer synchViewer = null;
			// Synchronize viewer selections if Enabled,
			// make sure the selection does not go in loops
			if (tsfviewer == rViewer) {
				synchViewer = tsfviewer2;
			} else {
				synchViewer = tsfviewer;
			}


			synchViewer.setSelectVisTimeWindow(rEvent.getTime0(), rEvent
					.getTime1(), source);
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
}