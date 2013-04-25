/*****************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation, Ericsson
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
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.NullTimeEvent;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Handler for the tool tips in the generic time graph view.
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public class TimeGraphTooltipHandler {

    private Shell _tipShell;
    private Composite _tipComposite;
    private Point _tipPosition;
    private final ITimeDataProvider _timeDataProvider;
    ITimeGraphPresentationProvider _utilImp = null;

    /**
     * Standard constructor
     *
     * @param parent
     *            The parent shell (unused, can be null)
     * @param rUtilImpl
     *            The presentation provider
     * @param timeProv
     *            The time provider
     */
    public TimeGraphTooltipHandler(Shell parent, ITimeGraphPresentationProvider rUtilImpl,
            ITimeDataProvider timeProv) {

        this._utilImp = rUtilImpl;
        this._timeDataProvider = timeProv;
    }

    private void createTooltipShell(Shell parent) {
        final Display display = parent.getDisplay();
        if (_tipShell != null && ! _tipShell.isDisposed()) {
            _tipShell.dispose();
        }
        _tipShell = new Shell(parent, SWT.ON_TOP | SWT.TOOL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 2;
        gridLayout.marginHeight = 2;
        _tipShell.setLayout(gridLayout);
        _tipShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        _tipComposite = new Composite(_tipShell, SWT.NONE);
        _tipComposite.setLayout(new GridLayout(3, false));
        setupControl(_tipComposite);

    }

    /**
     * Callback for the mouse-over tooltip
     *
     * @param control
     *            The control object to use
     */
    public void activateHoverHelp(final Control control) {
        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (_tipShell != null && ! _tipShell.isDisposed()) {
                    _tipShell.dispose();
                }
            }
        });

        control.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if (_tipShell != null && ! _tipShell.isDisposed()) {
                    _tipShell.dispose();
                }
            }
        });

        control.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseExit(MouseEvent e) {
                if (_tipShell != null && ! _tipShell.isDisposed()) {
                    Point pt = control.toDisplay(e.x, e.y);
                    if (! _tipShell.getBounds().contains(pt)) {
                        _tipShell.dispose();
                    }
                }
            }

            private void addItem(String name, String value) {
                Label nameLabel = new Label(_tipComposite, SWT.NO_FOCUS);
                nameLabel.setText(name);
                setupControl(nameLabel);
                Label separator = new Label(_tipComposite, SWT.NO_FOCUS | SWT.SEPARATOR | SWT.VERTICAL);
                GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
                gd.heightHint = nameLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                separator.setLayoutData(gd);
                setupControl(separator);
                Label valueLabel = new Label(_tipComposite, SWT.NO_FOCUS);
                valueLabel.setText(value);
                setupControl(valueLabel);
            }

            private void fillValues(Point pt, TimeGraphControl timeGraphControl, ITimeGraphEntry entry) {
                if (entry == null) {
                    return;
                }
                if (entry.hasTimeEvents()) {
                    long currPixelTime = timeGraphControl.getTimeAtX(pt.x);
                    long nextPixelTime = timeGraphControl.getTimeAtX(pt.x + 1);
                    if (nextPixelTime == currPixelTime) {
                        nextPixelTime++;
                    }
                    ITimeEvent currEvent = Utils.findEvent(entry, currPixelTime, 0);
                    ITimeEvent nextEvent = Utils.findEvent(entry, currPixelTime, 1);

                    // if there is no current event at the start of the current pixel range,
                    // or if the current event starts before the current pixel range,
                    // use the next event as long as it starts within the current pixel range
                    if (currEvent == null || currEvent.getTime() < currPixelTime) {
                        if (nextEvent != null && nextEvent.getTime() < nextPixelTime) {
                            currEvent = nextEvent;
                            currPixelTime = nextEvent.getTime();
                        }
                    }

                    // state name
                    String stateTypeName = _utilImp.getStateTypeName(entry);
                    String entryName = entry.getName();
                    if (stateTypeName == null) {
                        stateTypeName = _utilImp.getStateTypeName();
                    }

                    if (!entryName.isEmpty()) {
                        addItem(stateTypeName, entry.getName());
                    }

                    if (currEvent == null || currEvent instanceof NullTimeEvent) {
                        return;
                    }

                    // state
                    String state = _utilImp.getEventName(currEvent);
                    if (state != null) {
                        addItem(Messages.TmfTimeTipHandler_TRACE_STATE, state);
                    }

                    // This block receives a list of <String, String> values to be added to the tip table
                    Map<String, String> eventAddOns = _utilImp.getEventHoverToolTipInfo(currEvent, currPixelTime);
                    if (eventAddOns != null) {
                        for (Iterator<String> iter = eventAddOns.keySet().iterator(); iter.hasNext();) {
                            String message = iter.next();
                            addItem(message, eventAddOns.get(message));
                        }
                    }

                    long eventStartTime = -1;
                    long eventDuration = -1;
                    long eventEndTime = -1;

                    eventStartTime = currEvent.getTime();
                    eventDuration = currEvent.getDuration();
                    if (eventDuration < 0 && nextEvent != null) {
                        eventEndTime = nextEvent.getTime();
                        eventDuration = eventEndTime - eventStartTime;
                    } else {
                        eventEndTime = eventStartTime + eventDuration;
                    }

                    Resolution res = Resolution.NANOSEC;
                    TimeFormat tf = _timeDataProvider.getTimeFormat();
                    if (tf == TimeFormat.CALENDAR) {
                        addItem(Messages.TmfTimeTipHandler_TRACE_DATE, eventStartTime > -1 ?
                                Utils.formatDate(eventStartTime)
                                : "?"); //$NON-NLS-1$
                    }
                    if (eventDuration > 0) {
                        addItem(Messages.TmfTimeTipHandler_TRACE_START_TIME, eventStartTime > -1 ?
                                Utils.formatTime(eventStartTime, tf, res)
                                : "?"); //$NON-NLS-1$

                        addItem(Messages.TmfTimeTipHandler_TRACE_STOP_TIME, eventEndTime > -1 ?
                                Utils.formatTime(eventEndTime, tf, res)
                                : "?"); //$NON-NLS-1$
                    } else {
                        addItem(Messages.TmfTimeTipHandler_TRACE_EVENT_TIME, eventStartTime > -1 ?
                                Utils.formatTime(eventStartTime, tf, res)
                                : "?"); //$NON-NLS-1$
                    }

                    if (eventDuration > 0) {
                        // Duration in relative format in any case
                        if (tf == TimeFormat.CALENDAR) {
                            tf = TimeFormat.RELATIVE;
                        }
                        addItem(Messages.TmfTimeTipHandler_DURATION, eventDuration > -1 ?
                                Utils.formatTime(eventDuration, tf, res)
                                : "?"); //$NON-NLS-1$
                    }
                }
            }

            @Override
            public void mouseHover(MouseEvent event) {
                if ((event.stateMask & SWT.BUTTON_MASK) != 0) {
                    return;
                }
                Point pt = new Point(event.x, event.y);
                TimeGraphControl timeGraphControl = (TimeGraphControl) event.widget;
                createTooltipShell(timeGraphControl.getShell());
                ITimeGraphEntry entry = timeGraphControl.getEntry(pt);
                for (Control child : _tipComposite.getChildren()) {
                    child.dispose();
                }
                fillValues(pt, timeGraphControl, entry);
                if (_tipComposite.getChildren().length == 0) {
                    return;
                }
                _tipShell.pack();
                _tipPosition = control.toDisplay(pt);
                _tipShell.pack();
                setHoverLocation(_tipShell, _tipPosition);
                _tipShell.setVisible(true);
            }
        });
    }

    private static void setHoverLocation(Shell shell, Point position) {
        Rectangle displayBounds = shell.getDisplay().getBounds();
        Rectangle shellBounds = shell.getBounds();
        if (position.x + shellBounds.width + 16 > displayBounds.width && position.x - shellBounds.width - 16 >= 0) {
            shellBounds.x = position.x - shellBounds.width - 16;
        } else {
            shellBounds.x = Math.max(Math.min(position.x + 16, displayBounds.width - shellBounds.width), 0);
        }
        if (position.y + shellBounds.height + 16 > displayBounds.height && position.y - shellBounds.height - 16 >= 0) {
            shellBounds.y = position.y - shellBounds.height - 16;
        } else {
            shellBounds.y = Math.max(Math.min(position.y + 16, displayBounds.height - shellBounds.height), 0);
        }
        shell.setBounds(shellBounds);
    }

    private void setupControl(Control control) {
        control.setForeground(_tipShell.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        control.setBackground(_tipShell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                _tipShell.dispose();
            }
        });

        control.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseExit(MouseEvent e) {
                _tipShell.dispose();
            }
        });

        control.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                _tipShell.dispose();
            }
        });
    }
}
