/*****************************************************************************
 * Copyright (c) 2007 Intel Corporation, 2009, 2012 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Intel Corporation - Initial API and implementation
 *   Vitaly A. Provodin, Intel - Initial API and implementation
 *   Alvaro Sanchez-Leon - Updated for TMF
 *   Patrick Tasse - Refactoring
 *
 *****************************************************************************/
package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.Resolution;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
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


public class TimeGraphTooltipHandler {

    private Shell _tipShell;
    private Table _tipTable;
    private Point _tipPosition;
    private ITimeDataProvider _timeDataProvider;
    ITimeGraphPresentationProvider _utilImp = null;

    public TimeGraphTooltipHandler(Shell parent, ITimeGraphPresentationProvider rUtilImpl,
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
        new TableColumn(_tipTable, SWT.NONE);
        new TableColumn(_tipTable, SWT.NONE);
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
        //FIXME: remove old listeners
        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (_tipShell.isVisible()) {
                    _tipShell.setVisible(false);
                }
            }
        });

        control.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if (_tipShell.isVisible()) {
                    _tipShell.setVisible(false);
                }
            }
        });

        control.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseExit(MouseEvent e) {
                if (_tipShell.isVisible()) {
                    _tipShell.setVisible(false);
                }
            }

            private void addItem(String name, String value) {
                TableItem line = new TableItem(_tipTable, SWT.NONE);
                line.setText(0, name);
                line.setText(1, value);
            }

            private void fillValues(Point pt, TimeGraphControl threadStates, ITimeGraphEntry entry) {
                if (entry == null) {
                    return;
                }
                if (entry.hasTimeEvents()) {
                    ITimeEvent threadEvent = Utils.findEvent(entry, threadStates.getTimeAtX(pt.x), 2);
                    ITimeEvent nextEvent = Utils.findEvent(entry, threadStates.getTimeAtX(pt.x), 1);
                    // state name
                    addItem(_utilImp.getStateTypeName(), entry.getName());
                    if (threadEvent == null) {
                        return;
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
                }
            }

            @Override
            public void mouseHover(MouseEvent event) {
                Point pt = new Point(event.x, event.y);
                TimeGraphControl threadStates = (TimeGraphControl) event.widget;
                ITimeGraphEntry entry = threadStates.getEntry(pt);
                _tipTable.remove(0, _tipTable.getItemCount() - 1);
                fillValues(pt, threadStates, entry);
                if (_tipTable.getItemCount() == 0) {
                    return;
                }
                _tipTable.getColumn(0).pack();
                _tipTable.getColumn(1).pack();
                _tipShell.pack();
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
