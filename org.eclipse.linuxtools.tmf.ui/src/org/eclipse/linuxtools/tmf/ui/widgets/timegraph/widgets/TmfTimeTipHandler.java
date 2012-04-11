/*****************************************************************************
 * Copyright (c) 2007, Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    Vitaly A. Provodin, Intel - Initial API and implementation
 *    Alvaro Sanchex-Leon - Udpated for TMF
 *
 * $Id: ThreadsTipHandler.java,v 1.5 2007/06/06 19:16:16 gnagarajan Exp $
 *****************************************************************************/
package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeAnalysisViewer.TimeFormat;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TmfTimeAnalysisProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.Resolution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;


public class TmfTimeTipHandler {

	private Shell _tipShell;
	private Table _tipTable;
	private Item _tipItem;
	private Point _tipPosition;
	private ITimeDataProvider _timeDataProvider;
	TmfTimeAnalysisProvider _utilImp = null;

	public TmfTimeTipHandler(Shell parent, TmfTimeAnalysisProvider rUtilImpl,
			ITimeDataProvider timeProv) {
		final Display display = parent.getDisplay();

		this._utilImp = rUtilImpl;
		this._timeDataProvider = timeProv;
		_tipShell = new Shell(parent, SWT.ON_TOP | SWT.TOOL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 2;
		gridLayout.marginHeight = 2;
		_tipShell.setLayout(gridLayout);
		GridData data = new GridData(GridData.BEGINNING, GridData.BEGINNING,
				true, true);
		_tipShell.setLayoutData(data);
		_tipShell.setBackground(display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

		_tipTable = new Table(_tipShell, SWT.NONE);
		_tipTable.setForeground(display
				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		_tipTable.setBackground(display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		_tipTable.setHeaderVisible(false);
		_tipTable.setLinesVisible(false);

		// tipTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
		// | GridData.VERTICAL_ALIGN_CENTER));
	}

	public void activateHoverHelp(final Control control) {
		control.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (_tipShell.isVisible())
					_tipShell.setVisible(false);
			}
		});

		control.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseExit(MouseEvent e) {
				if (_tipShell.isVisible())
					_tipShell.setVisible(false);
				_tipItem = null;

			}

			private void addItem(String name, String value) {
				TableItem line = new TableItem(_tipTable, SWT.NONE);
				line.setText(0, name);
				line.setText(1, value);
			}

			private void fillValues(Point pt, TmfTimeStatesCtrl threadStates,
					Item item) {
				if (item instanceof TraceItem) {
					ITmfTimeAnalysisEntry thrd = ((TraceItem) item)._trace;
					ITimeEvent threadEvent = Utils.findEvent(thrd, threadStates.hitTimeTest(pt.x), 2);
					ITimeEvent nextEvent = Utils.findEvent(thrd, threadStates.hitTimeTest(pt.x), 1);
					// thread name
					addItem(Messages.TmfTimeTipHandler_TRACE_NAME, thrd.getName());
					// class name
					String traceClass = _utilImp.getTraceClassName(thrd);
					if (traceClass != null) {
						addItem(Messages.TmfTimeTipHandler_TRACE_CLASS_NAME, traceClass);
					}
					// thread state
					String state = _utilImp.getEventName(threadEvent);
					if (state != null) {
						addItem(Messages.TmfTimeTipHandler_TRACE_STATE, state);
					}

					// This block receives a
					// list of <String, String> values to be added to the tip
					// table
					Map<String, String> eventAddOns = _utilImp.getEventHoverToolTipInfo(threadEvent);
					if (eventAddOns != null) {
						for (Iterator<String> iter = eventAddOns.keySet().iterator(); iter.hasNext();) {
							String message = (String) iter.next();
							addItem(message, eventAddOns.get(message));
						}
					}

					long eventStartTime = -1;
					long eventDuration = -1;
					long eventEndTime = -1;
					
					if (threadEvent != null) {
					    eventStartTime = threadEvent.getTime();
					    eventDuration = threadEvent.getDuration();
					    if (eventDuration < 0 && nextEvent != null) {
					        eventEndTime = nextEvent.getTime();
					        eventDuration = eventEndTime - eventStartTime;
					    } else {
					        eventEndTime = eventStartTime + eventDuration;
					    }
					}

// TODO: Check if we need "format"					
//					TimeFormat format = TimeFormat.RELATIVE;
					Resolution res = Resolution.NANOSEC;
					if (_timeDataProvider.isCalendarFormat()) {
//						format = TimeFormat.ABSOLUTE; // Absolute format
//														// (calendar)
						// Add Date
						addItem(Messages.TmfTimeTipHandler_TRACE_DATE, eventStartTime > -1 ?
						        Utils.formatDate(eventStartTime)
						        : "?"); //$NON-NLS-1$
						if (eventDuration > 0) {
                            addItem(Messages.TmfTimeTipHandler_TRACE_START_TIME, eventStartTime > -1 ?
                                    Utils.formatTime(eventStartTime, TimeFormat.ABSOLUTE, res)
                                    : "?"); //$NON-NLS-1$
                            
                            addItem(Messages.TmfTimeTipHandler_TRACE_STOP_TIME, eventEndTime > -1 ?
                                    Utils.formatTime(eventEndTime, TimeFormat.ABSOLUTE, res)
                                    : "?"); //$NON-NLS-1$
						} else {
                            addItem(Messages.TmfTimeTipHandler_TRACE_EVENT_TIME, eventStartTime > -1 ?
                                    Utils.formatTime(eventStartTime, TimeFormat.ABSOLUTE, res)
                                    : "?"); //$NON-NLS-1$
						}
					} else {
						if (eventDuration > 0) {
					        addItem(Messages.TmfTimeTipHandler_TRACE_START_TIME, eventStartTime > -1 ?
					                Utils.formatTime(eventStartTime, TimeFormat.RELATIVE, res)
					                : "?"); //$NON-NLS-1$
					    
					        addItem(Messages.TmfTimeTipHandler_TRACE_STOP_TIME, eventEndTime > -1 ?
					                Utils.formatTime(eventEndTime, TimeFormat.RELATIVE, res)
					                : "?"); //$NON-NLS-1$
						} else {
                            addItem(Messages.TmfTimeTipHandler_TRACE_EVENT_TIME, eventStartTime > -1 ?
                                    Utils.formatTime(eventStartTime, TimeFormat.RELATIVE, res)
                                    : "?"); //$NON-NLS-1$
						}
					}

					if (eventDuration > 0) {
					    // Duration in relative format in any case
					    addItem(Messages.TmfTimeTipHandler_DURATION, eventDuration > -1 ?
					            Utils.formatTime(eventDuration, TimeFormat.RELATIVE, res)
					            : "?"); //$NON-NLS-1$
					}

				} else if (item instanceof GroupItem) {
					addItem(Messages.TmfTimeTipHandler_TRACE_GROUP_NAME, item.toString());
					addItem(Messages.TmfTimeTipHandler_NUMBER_OF_TRACES, "" + ((GroupItem) item)._traces.size()); //$NON-NLS-1$
				}
			}

			@Override
			public void mouseHover(MouseEvent event) {
				Point pt = new Point(event.x, event.y);
				Widget widget = event.widget;
				Item item = null;
				if (widget instanceof TmfTimeStatesCtrl) {
					TmfTimeStatesCtrl threadStates = (TmfTimeStatesCtrl) widget;
					item = (Item) threadStates.getItem(pt);
					_tipTable.remove(0, _tipTable.getItemCount() - 1);
					new TableColumn(_tipTable, SWT.NONE);
					new TableColumn(_tipTable, SWT.NONE);
					fillValues(pt, threadStates, item);
					_tipTable.getColumn(0).setWidth(200);
					_tipTable.getColumn(1).pack();
					_tipTable.setSize(_tipTable.computeSize(SWT.DEFAULT, 200));
					_tipShell.pack();
				} else if (widget == null) {
					_tipShell.setVisible(false);
					_tipItem = null;
					return;
				}
				if (item == _tipItem)
					return;
				_tipItem = item;
				_tipPosition = control.toDisplay(pt);
				_tipShell.pack();
				setHoverLocation(_tipShell, _tipPosition);
				_tipShell.setVisible(true);
			}
		});
	}

	private void setHoverLocation(Shell shell, Point position) {
		Rectangle displayBounds = shell.getDisplay().getBounds();
		Rectangle shellBounds = shell.getBounds();
		shellBounds.x = Math.max(Math.min(position.x, displayBounds.width
				- shellBounds.width), 0);
		shellBounds.y = Math.max(Math.min(position.y + 16, displayBounds.height
				- shellBounds.height), 0);
		shell.setBounds(shellBounds);
	}

}