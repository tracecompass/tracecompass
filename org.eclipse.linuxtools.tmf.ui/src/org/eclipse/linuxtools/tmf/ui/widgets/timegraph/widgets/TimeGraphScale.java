/*****************************************************************************
 * Copyright (c) 2007, 2014 Intel Corporation, Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Intel Corporation - Initial API and implementation
 *   Ruslan A. Scherbakov, Intel - Initial API and implementation
 *   Alvaro Sanchez-Leon - Updated for TMF
 *   Patrick Tasse - Refactoring
 *   Marc-Andre Laperle - Add time zone preference
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.Resolution;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of the scale for the time graph view.
 *
 * This goes above the "gantt chart" area.
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public class TimeGraphScale extends TimeGraphBaseControl implements
        MouseListener, MouseMoveListener {

    private static final long MICROSEC_IN_NS = 1000;
    private static final long MILLISEC_IN_NS = 1000000;
    private static final long SEC_IN_NS = 1000000000;
    private static final long MIN_IN_NS = 60 * SEC_IN_NS;
    private static final long HOUR_IN_NS = 60 * MIN_IN_NS;
    private static final long DAY_IN_NS = 24 * HOUR_IN_NS;
    private static final long MONTH_IN_NS = 31 * DAY_IN_NS; // upper limit
    private static final long YEAR_IN_NS = 366 * DAY_IN_NS; // upper limit

    private static final double LOG10_1 = Math.log10(1);
    private static final double LOG10_2 = Math.log10(2);
    private static final double LOG10_3 = Math.log10(3);
    private static final double LOG10_5 = Math.log10(5);

    private static final Calendar GREGORIAN_CALENDAR = Calendar.getInstance();

    private static final TimeDraw TIMEDRAW_NANOSEC = new TimeDrawNanosec();
    private static final TimeDraw TIMEDRAW_MICROSEC = new TimeDrawMicrosec();
    private static final TimeDraw TIMEDRAW_MILLISEC = new TimeDrawMillisec();
    private static final TimeDraw TIMEDRAW_SEC = new TimeDrawSec();
    private static final TimeDraw TIMEDRAW_ABS_NANOSEC = new TimeDrawAbsNanoSec();
    private static final TimeDraw TIMEDRAW_ABS_MICROSEC = new TimeDrawAbsMicroSec();
    private static final TimeDraw TIMEDRAW_ABS_MILLISEC = new TimeDrawAbsMillisec();
    private static final TimeDraw TIMEDRAW_ABS_SEC = new TimeDrawAbsSec();
    private static final TimeDraw TIMEDRAW_ABS_MIN = new TimeDrawAbsMin();
    private static final TimeDraw TIMEDRAW_ABS_HRS = new TimeDrawAbsHrs();
    private static final TimeDraw TIMEDRAW_ABS_DAY = new TimeDrawAbsDay();
    private static final TimeDraw TIMEDRAW_ABS_MONTH = new TimeDrawAbsMonth();
    private static final TimeDraw TIMEDRAW_ABS_YEAR = new TimeDrawAbsYear();
    private static final TimeDraw TIMEDRAW_NUMBER = new TimeDrawNumber();
    private static final TimeDraw TIMEDRAW_CYCLES = new TimeDrawCycles();

    private static final int DRAG_EXTERNAL = -1;
    private static final int NO_BUTTON = 0;
    private static final int LEFT_BUTTON = 1;

    private ITimeDataProvider fTimeProvider;
    private int fDragState = NO_BUTTON;
    private int fDragX0 = 0;
    private int fDragX = 0;
    private long fTime0bak;
    private long fTime1bak;
    private boolean fIsInUpdate;
    private int fHeight;

    /**
     * Standard constructor
     *
     * @param parent
     *            The parent composite object
     * @param colors
     *            The color scheme to use
     */
    public TimeGraphScale(Composite parent, TimeGraphColorScheme colors) {
        super(parent, colors, SWT.NO_BACKGROUND | SWT.NO_FOCUS | SWT.DOUBLE_BUFFERED);
        TmfSignalManager.register(this);
        addMouseListener(this);
        addMouseMoveListener(this);
        TimeDraw.updateTimeZone();
    }

    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
        super.dispose();
    }

    /**
     * Assign the time provider for this scale
     *
     * @param timeProvider
     *            The provider to use
     */
    public void setTimeProvider(ITimeDataProvider timeProvider) {
        fTimeProvider = timeProvider;
    }

    /**
     * Get the time provider used by this scale
     *
     * @return The time provider
     * @since 3.1
     */
    public ITimeDataProvider getTimeProvider() {
        return fTimeProvider;
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return super.computeSize(wHint, fHeight, changed);
    }

    /**
     * Set the height of the scale
     *
     * @param height
     *            The height to use
     */
    public void setHeight(int height) {
        this.fHeight = height;
    }

    /**
     * Set the drag range to paint decorators
     *
     * @param begin
     *            The begin x-coordinate
     * @param end
     *            The end x-coordinate
     * @since 2.1
     */
    public void setDragRange(int begin, int end) {
        if (NO_BUTTON == fDragState || DRAG_EXTERNAL == fDragState) {
            fDragX0 = begin - fTimeProvider.getNameSpace();
            fDragX = end - fTimeProvider.getNameSpace();
            if (begin >= 0 || end >= 0) {
                fDragState = DRAG_EXTERNAL;
            } else {
                fDragState = NO_BUTTON;
            }
        }
        redraw();
    }

    private long calcTimeDelta(int width, double pixelsPerNanoSec) {
        long timeDelta;
        double minDelta = (pixelsPerNanoSec == 0) ? YEAR_IN_NS : width / pixelsPerNanoSec;
        long unit = 1;
        if (fTimeProvider != null && fTimeProvider.getTimeFormat() == TimeFormat.CALENDAR) {
            if (minDelta > 6 * MONTH_IN_NS) {
                unit = YEAR_IN_NS;
            } else if (minDelta > 3 * MONTH_IN_NS) {
                unit = 6 * MONTH_IN_NS;
            } else if (minDelta > 10 * DAY_IN_NS) {
                unit = MONTH_IN_NS;
            } else if (minDelta > 12 * HOUR_IN_NS) {
                unit = DAY_IN_NS;
            } else if (minDelta > 3 * HOUR_IN_NS) {
                unit = 6 * HOUR_IN_NS;
            } else if (minDelta > 30 * MIN_IN_NS) {
                unit = HOUR_IN_NS;
            } else if (minDelta > 10 * MIN_IN_NS) {
                unit = 15 * MIN_IN_NS;
            } else if (minDelta > 30 * SEC_IN_NS) {
                unit = MIN_IN_NS;
            } else if (minDelta > 20 * SEC_IN_NS) {
                unit = 30 * SEC_IN_NS;
            } else if (minDelta <= 1) {
                timeDelta = 1;
                return timeDelta;
            }
        }
        double log = Math.log10(minDelta / unit);
        long pow10 = (long) log;
        double remainder = log - pow10;
        if (remainder < LOG10_1) {
            timeDelta = (long) Math.pow(10, pow10) * unit;
        } else if (remainder < LOG10_2) {
            timeDelta = 2 * (long) Math.pow(10, pow10) * unit;
        } else if (remainder < LOG10_3 && unit >= HOUR_IN_NS && unit < YEAR_IN_NS) {
            timeDelta = 3 * (long) Math.pow(10, pow10) * unit;
        } else if (remainder < LOG10_5) {
            timeDelta = 5 * (long) Math.pow(10, pow10) * unit;
        } else {
            timeDelta = 10 * (long) Math.pow(10, pow10) * unit;
        }
        if (timeDelta <= 0) {
            timeDelta = 1;
        }
        return timeDelta;
    }

    TimeDraw getTimeDraw(long timeDelta) {
        TimeDraw timeDraw;
        if (fTimeProvider != null) {
            switch (fTimeProvider.getTimeFormat()) {
            case CALENDAR:
                if (timeDelta >= YEAR_IN_NS) {
                    timeDraw = TIMEDRAW_ABS_YEAR;
                } else if (timeDelta >= MONTH_IN_NS) {
                    timeDraw = TIMEDRAW_ABS_MONTH;
                } else if (timeDelta >= DAY_IN_NS) {
                    timeDraw = TIMEDRAW_ABS_DAY;
                } else if (timeDelta >= HOUR_IN_NS) {
                    timeDraw = TIMEDRAW_ABS_HRS;
                } else if (timeDelta >= MIN_IN_NS) {
                    timeDraw = TIMEDRAW_ABS_MIN;
                } else if (timeDelta >= SEC_IN_NS) {
                    timeDraw = TIMEDRAW_ABS_SEC;
                } else if (timeDelta >= MILLISEC_IN_NS) {
                    timeDraw = TIMEDRAW_ABS_MILLISEC;
                } else if (timeDelta >= MICROSEC_IN_NS) {
                    timeDraw = TIMEDRAW_ABS_MICROSEC;
                } else {
                    timeDraw = TIMEDRAW_ABS_NANOSEC;
                }
                return timeDraw;
            case NUMBER:
                return TIMEDRAW_NUMBER;
            case CYCLES:
                return TIMEDRAW_CYCLES;
            case RELATIVE:
            default:
            }

        }
        if (timeDelta >= SEC_IN_NS) {
            timeDraw = TIMEDRAW_SEC;
        } else if (timeDelta >= MILLISEC_IN_NS) {
            timeDraw = TIMEDRAW_MILLISEC;
        } else if (timeDelta >= MICROSEC_IN_NS) {
            timeDraw = TIMEDRAW_MICROSEC;
        } else {
            timeDraw = TIMEDRAW_NANOSEC;
        }
        return timeDraw;
    }

    @Override
    void paint(Rectangle rect, PaintEvent e) {

        if (fIsInUpdate || null == fTimeProvider) {
            return;
        }

        GC gc = e.gc;
        gc.fillRectangle(rect);

        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        int leftSpace = fTimeProvider.getNameSpace();
        int timeSpace = fTimeProvider.getTimeSpace();

        gc.setBackground(getColorScheme().getColor(TimeGraphColorScheme.TOOL_BACKGROUND));
        gc.setForeground(getColorScheme().getColor(TimeGraphColorScheme.TOOL_FOREGROUND));
        Rectangle rect0 = new Rectangle(0, 0, 0, 0);
        Utils.init(rect0, rect);

        // draw top left area
        rect0.width = leftSpace;
        rect0.x += 4;
        rect0.width -= 4;
        Rectangle absHeaderRect = new Rectangle(rect0.x, rect0.y, rect0.width, rect0.height);
        rect0.x -= 4;
        rect0.width += 4;

        // prepare and draw right rect of the timescale
        rect0.x += leftSpace;
        rect0.width = rect.width - leftSpace;

        // draw bottom border and erase all other area
        gc.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width - 1,
                rect.y + rect.height - 1);
        rect0.height--;
        gc.fillRectangle(rect0);

        if (time1 <= time0 || timeSpace < 2) {
            return;
        }

        int numDigits = calculateDigits(time0, time1);

        int labelWidth = gc.getCharWidth('0') * numDigits;
        double pixelsPerNanoSec = (timeSpace <= RIGHT_MARGIN) ? 0 :
            (double) (timeSpace - RIGHT_MARGIN) / (time1 - time0);
        long timeDelta = calcTimeDelta(labelWidth, pixelsPerNanoSec);

        TimeDraw timeDraw = getTimeDraw(timeDelta);

        // draw range decorators
        if (DRAG_EXTERNAL == fDragState) {
            int x1 = leftSpace + Math.min(fDragX0, fDragX);
            int x2 = leftSpace + Math.max(fDragX0, fDragX);
            drawRangeDecorators(rect0, gc, x1, x2);
        } else {
            int x1;
            int x2;
            long selectionBegin = fTimeProvider.getSelectionBegin();
            long selectionEnd = fTimeProvider.getSelectionEnd();
            x1 = leftSpace + (int) ((selectionBegin - time0) * pixelsPerNanoSec);
            x2 = leftSpace + (int) ((selectionEnd - time0) * pixelsPerNanoSec);
            drawRangeDecorators(rect0, gc, x1, x2);
        }

        if (rect0.isEmpty()) {
            return;
        }

        // draw time scale ticks
        rect0.y = rect.y;
        rect0.height = rect.height - 4;
        rect0.width = labelWidth;

        long time;
        if (fTimeProvider != null && fTimeProvider.getTimeFormat() == TimeFormat.CALENDAR) {
            time = floorToCalendar(time0, timeDelta);
        } else {
            time = (time0 / timeDelta) * timeDelta;
            if (time != time0) {
                time += timeDelta;
            }
        }

        int y = rect0.y + rect0.height;

        if (fTimeProvider != null && fTimeProvider.getTimeFormat() == TimeFormat.CALENDAR) {
            timeDraw.drawAbsHeader(gc, time, absHeaderRect);
        }

        while (true) {
            int x = rect.x + leftSpace + (int) (Math.floor((time - time0) * pixelsPerNanoSec));
            if (x >= rect.x + leftSpace + rect.width - rect0.width) {
                break;
            }
            if (x >= rect.x + leftSpace) {
                gc.drawLine(x, y, x, y + 4);
                rect0.x = x;
                if (x + rect0.width <= rect.x + rect.width) {
                    timeDraw.draw(gc, time, rect0);
                }
            }
            if (pixelsPerNanoSec == 0 || time > Long.MAX_VALUE - timeDelta || timeDelta == 0) {
                break;
            }
            if (fTimeProvider != null && fTimeProvider.getTimeFormat() == TimeFormat.CALENDAR) {
                if (timeDelta >= YEAR_IN_NS) {
                    long millis = time / MILLISEC_IN_NS;
                    GREGORIAN_CALENDAR.setTime(new Date(millis));
                    GREGORIAN_CALENDAR.add(Calendar.YEAR, (int) (timeDelta / YEAR_IN_NS));
                    millis = GREGORIAN_CALENDAR.getTimeInMillis();
                    time = millis * MILLISEC_IN_NS;
                } else if (timeDelta >= MONTH_IN_NS) {
                    long millis = time / MILLISEC_IN_NS;
                    GREGORIAN_CALENDAR.setTime(new Date(millis));
                    GREGORIAN_CALENDAR.add(Calendar.MONTH, (int) (timeDelta / MONTH_IN_NS));
                    millis = GREGORIAN_CALENDAR.getTimeInMillis();
                    time = millis * MILLISEC_IN_NS;
                } else if (timeDelta >= DAY_IN_NS) {
                    long millis = time / MILLISEC_IN_NS;
                    GREGORIAN_CALENDAR.setTime(new Date(millis));
                    GREGORIAN_CALENDAR.add(Calendar.DAY_OF_MONTH, (int) (timeDelta / DAY_IN_NS));
                    millis = GREGORIAN_CALENDAR.getTimeInMillis();
                    time = millis * MILLISEC_IN_NS;
                } else {
                    time += timeDelta;
                }
            } else {
                time += timeDelta;
            }
        }
    }

    private static void drawRangeDecorators(Rectangle rect, GC gc, int x1, int x2) {
        int y1 = rect.y + rect.height - 9;
        int y2 = rect.y + rect.height - 5;
        int ym = (y1 + y2) / 2;
        if (x1 >= rect.x) {
            // T1
            gc.drawLine(x1 - 3, y1, x1 - 3, y2);
            gc.drawLine(x1 - 4, y1, x1 - 2, y1);
            gc.drawLine(x1, y1, x1, y2);
        }
        if (x2 >= rect.x && x2 - x1 > 3) {
            // T2
            gc.drawLine(x2 - 2, y1, x2 - 2, y2);
            gc.drawLine(x2 - 3, y1, x2 - 1, y1);
        }
        if (x2 >= rect.x && x2 - x1 > 0) {
            gc.drawLine(x2 + 1, y1, x2 + 3, y1);
            gc.drawLine(x2 + 3, y1, x2 + 3, ym);
            gc.drawLine(x2 + 1, ym, x2 + 3, ym);
            gc.drawLine(x2 + 1, ym, x2 + 1, y2);
            gc.drawLine(x2 + 1, y2, x2 + 3, y2);
        }
    }

    private static long floorToCalendar(long time, long timeDelta) {
        long ret = time;

        if (timeDelta >= YEAR_IN_NS) {
            GREGORIAN_CALENDAR.setTime(new Date(ret / MILLISEC_IN_NS));
            int year = GREGORIAN_CALENDAR.get(Calendar.YEAR);
            int yearDelta = (int) (timeDelta / YEAR_IN_NS);
            year = (year / yearDelta) * yearDelta;
            GREGORIAN_CALENDAR.set(Calendar.YEAR, year);
            GREGORIAN_CALENDAR.set(Calendar.MONTH, 0); // January 1st of year
            GREGORIAN_CALENDAR.set(Calendar.DAY_OF_MONTH, 1);
            GREGORIAN_CALENDAR.set(Calendar.HOUR_OF_DAY, 0);
            GREGORIAN_CALENDAR.set(Calendar.MINUTE, 0);
            GREGORIAN_CALENDAR.set(Calendar.SECOND, 0);
            GREGORIAN_CALENDAR.set(Calendar.MILLISECOND, 0);
            ret = GREGORIAN_CALENDAR.getTimeInMillis() * MILLISEC_IN_NS;
        } else if (timeDelta >= MONTH_IN_NS) {
            GREGORIAN_CALENDAR.setTime(new Date(ret / MILLISEC_IN_NS));
            int month = GREGORIAN_CALENDAR.get(Calendar.MONTH);
            int monthDelta = (int) (timeDelta / MONTH_IN_NS);
            month = (month / monthDelta) * monthDelta;
            GREGORIAN_CALENDAR.set(Calendar.MONTH, month);
            GREGORIAN_CALENDAR.set(Calendar.DAY_OF_MONTH, 1); // 1st of month
            GREGORIAN_CALENDAR.set(Calendar.HOUR_OF_DAY, 0);
            GREGORIAN_CALENDAR.set(Calendar.MINUTE, 0);
            GREGORIAN_CALENDAR.set(Calendar.SECOND, 0);
            GREGORIAN_CALENDAR.set(Calendar.MILLISECOND, 0);
            ret = GREGORIAN_CALENDAR.getTimeInMillis() * MILLISEC_IN_NS;
        } else {
            long offset = GREGORIAN_CALENDAR.getTimeZone().getOffset(ret / MILLISEC_IN_NS) * MILLISEC_IN_NS;
            ret += offset;
            ret = (ret / timeDelta) * timeDelta;
            ret -= offset;
        }
        return ret;
    }

    private int calculateDigits(long time0, long time1) {
        int numDigits = 5;
        long timeRange = time1 - time0;

        if (fTimeProvider.getTimeFormat() == TimeFormat.CALENDAR) {
            // Calculate the number of digits to represent the minutes provided
            // 11:222
            // HH:mm:ss
            numDigits += 8;
            if (timeRange < 10000) {
                // HH:11:222:333:444__
                numDigits += 10;
            } else if (timeRange < 10000000) {
                // HH:11:222:333__
                numDigits += 6;
            }
        } else {
            long sec = time1 / SEC_IN_NS;
            numDigits = Long.toString(sec).length();
            int thousandGroups = (numDigits - 1) / 3;
            numDigits += thousandGroups;
            numDigits += 12; // .000 000 000
            if (fTimeProvider.getTimeFormat() == TimeFormat.CYCLES) {
                numDigits += Messages.Utils_ClockCyclesUnit.length();
            }
        }

        return numDigits;
    }

    @Override
    public void mouseDown(MouseEvent e) {
        getParent().setFocus();
        if (fDragState == NO_BUTTON && null != fTimeProvider) {
            int x = e.x - fTimeProvider.getNameSpace();
            if (LEFT_BUTTON == e.button && x > 0) {
                setCapture(true);
                fDragState = LEFT_BUTTON;
            }
            if (x < 0) {
                x = 0;
            } else if (x > getSize().x - fTimeProvider.getNameSpace()) {
                x = getSize().x - fTimeProvider.getNameSpace();
            }
            fDragX = x;
            fDragX0 = x;
            fTime0bak = fTimeProvider.getTime0();
            fTime1bak = fTimeProvider.getTime1();
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if (e.button == LEFT_BUTTON && fDragState == LEFT_BUTTON) {
            setCapture(false);
            fDragState = NO_BUTTON;

            // Notify time provider to check the need for listener notification
            if (fDragX != fDragX0 && fTimeProvider.getTime0() != fTimeProvider.getTime1()) {
                fTimeProvider.setStartFinishTimeNotify(fTimeProvider.getTime0(), fTimeProvider.getTime1());
            }
        }
    }

    @Override
    public void mouseMove(MouseEvent e) {
        if (fDragX0 < 0 || fDragState == NO_BUTTON || fTimeProvider == null) {
            return;
        }
        Point size = getSize();
        int leftSpace = fTimeProvider.getNameSpace();
        int x = e.x - leftSpace;
        if (LEFT_BUTTON == fDragState) {
            if (x > 0 && size.x > leftSpace && fDragX != x) {
                fDragX = x;
                if (fTimeProvider.getTime0() == fTimeProvider.getTime1()) {
                    return;
                }
                long interval = (long) ((fTime1bak - fTime0bak) * ((double) fDragX0 / fDragX));
                if (interval == Long.MAX_VALUE) {
                    fTimeProvider.setStartFinishTime(fTime0bak, Long.MAX_VALUE);
                } else {
                    long time1 = fTime0bak + (long) ((fTime1bak - fTime0bak) * ((double) fDragX0 / fDragX));
                    fTimeProvider.setStartFinishTime(fTime0bak, time1);
                }
            }
        }
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
        if (e.button == 1 && null != fTimeProvider && fTimeProvider.getTime0() != fTimeProvider.getTime1() && (e.stateMask & SWT.BUTTON_MASK) == 0) {
            fTimeProvider.resetStartFinishTime();
            fTimeProvider.notifyStartFinishTime();
            fTime0bak = fTimeProvider.getTime0();
            fTime1bak = fTimeProvider.getTime1();
        }
    }

        /**
     * Update the display to use the updated timestamp format
     *
     * @param signal the incoming signal
     * @since 2.1
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        TimeDraw.updateTimeZone();
        Utils.updateTimeZone();
        redraw();
    }
}

abstract class TimeDraw {
    protected static final long MICROSEC_IN_NS = 1000;
    protected static final long MILLISEC_IN_NS = 1000000;
    protected static final long MILLISEC_IN_US = 1000;
    protected static final long SEC_IN_NS = 1000000000;
    protected static final long SEC_IN_MS = 1000;
    private static final String S   = ""  ; //$NON-NLS-1$
    private static final String S0  = "0" ; //$NON-NLS-1$
    private static final String S00 = "00"; //$NON-NLS-1$
    protected static final long PAD_1000 = 1000;
    protected static final SimpleDateFormat SEC_FORMAT_HEADER = new SimpleDateFormat("yyyy MMM dd"); //$NON-NLS-1$
    protected static final SimpleDateFormat SEC_FORMAT = new SimpleDateFormat("HH:mm:ss");           //$NON-NLS-1$
    protected static final SimpleDateFormat MIN_FORMAT_HEADER = new SimpleDateFormat("yyyy MMM dd"); //$NON-NLS-1$
    protected static final SimpleDateFormat MIN_FORMAT = new SimpleDateFormat("HH:mm");              //$NON-NLS-1$
    protected static final SimpleDateFormat HOURS_FORMAT_HEADER = new SimpleDateFormat("yyyy");      //$NON-NLS-1$
    protected static final SimpleDateFormat HOURS_FORMAT = new SimpleDateFormat("MMM dd HH:mm");     //$NON-NLS-1$
    protected static final SimpleDateFormat DAY_FORMAT_HEADER = new SimpleDateFormat("yyyy");        //$NON-NLS-1$
    protected static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("MMM dd");             //$NON-NLS-1$
    protected static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy MMM");         //$NON-NLS-1$
    protected static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");              //$NON-NLS-1$

    protected static final SimpleDateFormat formatArray[] = {
        SEC_FORMAT, SEC_FORMAT_HEADER, MIN_FORMAT, MIN_FORMAT_HEADER,
        HOURS_FORMAT, HOURS_FORMAT_HEADER, DAY_FORMAT, DAY_FORMAT_HEADER, MONTH_FORMAT, YEAR_FORMAT
    };

    /**
     * Updates the timezone using the preferences.
     */
    public static void updateTimeZone() {
        final TimeZone timeZone = TmfTimePreferences.getInstance().getTimeZone();
        for (SimpleDateFormat sdf : formatArray) {
            sdf.setTimeZone(timeZone);
        }
    }

    static String sep(long n) {
        StringBuilder retVal = new StringBuilder();
        String s = Long.toString(n);
        for (int i = 0; i < s.length(); i++) {
            int pos = s.length() - i - 1;
            retVal.append(s.charAt(i));
            if (pos % 3 == 0 && pos != 0) {
                retVal.append(' ');
            }
        }
        return retVal.toString();
    }

    static String pad(long n) {
        String s;
        if (n < 10) {
            s = S00;
        } else if (n < 100) {
            s = S0;
        } else {
            s = S;
        }
        return s + n;
    }

    public abstract int draw(GC gc, long time, Rectangle rect);

    /**
     * Override to draw absolute time header. This is for the time information
     * not shown in the draw of each tick
     *
     * @param gc
     *            Graphics context
     * @param nanosec
     *            time in nanosec
     * @param absHeaderRect
     *            Header rectangle
     */
    public void drawAbsHeader(GC gc, long nanosec, Rectangle absHeaderRect) {
    }
}

class TimeDrawSec extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        long sec = nanosec / SEC_IN_NS;
        return Utils.drawText(gc, sep(sec), rect, true);
    }
}

class TimeDrawMillisec extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        long millisec = nanosec / MILLISEC_IN_NS;
        long ms = millisec % PAD_1000;
        long sec = millisec / SEC_IN_MS;
        return Utils.drawText(gc, sep(sec) + "." + pad(ms), rect, true); //$NON-NLS-1$
    }
}

class TimeDrawMicrosec extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        long microsec = nanosec / MICROSEC_IN_NS;
        long us = microsec % PAD_1000;
        long millisec = microsec / MILLISEC_IN_US;
        long ms = millisec % PAD_1000;
        long sec = millisec / SEC_IN_MS;
        return Utils.drawText(gc, sep(sec) + "." + pad(ms) + " " + pad(us), rect, true); //$NON-NLS-1$ //$NON-NLS-2$
    }
}

class TimeDrawNanosec extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        long ns = nanosec % PAD_1000;
        long microsec = nanosec / MICROSEC_IN_NS;
        long us = microsec % PAD_1000;
        long millisec = microsec / MILLISEC_IN_US;
        long ms = millisec % PAD_1000;
        long sec = millisec / SEC_IN_MS;
        return Utils.drawText(gc, sep(sec) + "." + pad(ms) + " " + pad(us) + " " + pad(ns), rect, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}

class TimeDrawAbsYear extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        String stime = YEAR_FORMAT.format(new Date(nanosec / MILLISEC_IN_NS));
        return Utils.drawText(gc, stime, rect, true);
    }
}

class TimeDrawAbsMonth extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        String stime = MONTH_FORMAT.format(new Date(nanosec / MILLISEC_IN_NS));
        return Utils.drawText(gc, stime, rect, true);
    }
}

class TimeDrawAbsDay extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        String stime = DAY_FORMAT.format(new Date(nanosec / MILLISEC_IN_NS));
        return Utils.drawText(gc, stime, rect, true);
    }

    @Override
    public void drawAbsHeader(GC gc, long nanosec, Rectangle rect) {
        String header = DAY_FORMAT_HEADER.format(new Date(nanosec / MILLISEC_IN_NS));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
}

class TimeDrawAbsHrs extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        String stime = HOURS_FORMAT.format(new Date(nanosec / MILLISEC_IN_NS));
        return Utils.drawText(gc, stime, rect, true);
    }

    @Override
    public void drawAbsHeader(GC gc, long nanosec, Rectangle rect) {
        String header = HOURS_FORMAT_HEADER.format(new Date(nanosec / MILLISEC_IN_NS));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
}

class TimeDrawAbsMin extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        String stime = MIN_FORMAT.format(new Date(nanosec / MILLISEC_IN_NS));
        return Utils.drawText(gc, stime, rect, true);
    }

    @Override
    public void drawAbsHeader(GC gc, long nanosec, Rectangle rect) {
        String header = MIN_FORMAT_HEADER.format(new Date(nanosec / MILLISEC_IN_NS));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
}

class TimeDrawAbsSec extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        String stime = SEC_FORMAT.format(new Date(nanosec / MILLISEC_IN_NS));
        return Utils.drawText(gc, stime, rect, true);
    }

    @Override
    public void drawAbsHeader(GC gc, long nanosec, Rectangle rect) {
        String header = SEC_FORMAT_HEADER.format(new Date(nanosec / MILLISEC_IN_NS));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
}

class TimeDrawAbsMillisec extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        String stime = SEC_FORMAT.format(new Date(nanosec / MILLISEC_IN_NS));
        String ns = Utils.formatNs(nanosec, Resolution.MILLISEC);
        return Utils.drawText(gc, stime + "." + ns, rect, true); //$NON-NLS-1$
    }

    @Override
    public void drawAbsHeader(GC gc, long nanosec, Rectangle rect) {
        String header = SEC_FORMAT_HEADER.format(new Date(nanosec / MILLISEC_IN_NS));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
}

class TimeDrawAbsMicroSec extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        String stime = SEC_FORMAT.format(new Date(nanosec / MILLISEC_IN_NS));
        String micr = Utils.formatNs(nanosec, Resolution.MICROSEC);
        return Utils.drawText(gc, stime + "." + micr, rect, true); //$NON-NLS-1$
    }

    @Override
    public void drawAbsHeader(GC gc, long nanosec, Rectangle rect) {
        String header = SEC_FORMAT_HEADER.format(new Date(nanosec / MILLISEC_IN_NS));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
}

class TimeDrawAbsNanoSec extends TimeDraw {
    @Override
    public int draw(GC gc, long nanosec, Rectangle rect) {
        String stime = SEC_FORMAT.format(new Date(nanosec / MILLISEC_IN_NS));
        String ns = Utils.formatNs(nanosec, Resolution.NANOSEC);
        return Utils.drawText(gc, stime + "." + ns, rect, true); //$NON-NLS-1$
    }

    @Override
    public void drawAbsHeader(GC gc, long nanosec, Rectangle rect) {
        String header = SEC_FORMAT_HEADER.format(new Date(nanosec / MILLISEC_IN_NS));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
}

class TimeDrawNumber extends TimeDraw {
    @Override
    public int draw(GC gc, long time, Rectangle rect) {
        String stime = NumberFormat.getInstance().format(time);
        return Utils.drawText(gc, stime, rect, true);
    }
}

class TimeDrawCycles extends TimeDraw {
    @Override
    public int draw(GC gc, long time, Rectangle rect) {
        String stime = Utils.formatTime(time, TimeFormat.CYCLES, Resolution.SECONDS);
        return Utils.drawText(gc, stime, rect, true);
    }
}
