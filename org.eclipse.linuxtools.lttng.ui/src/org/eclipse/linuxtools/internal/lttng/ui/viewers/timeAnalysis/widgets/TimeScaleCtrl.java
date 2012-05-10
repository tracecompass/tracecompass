/*****************************************************************************
 * Copyright (c) 2007, 2008, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    Ruslan A. Scherbakov, Intel - Initial API and implementation
 *    Alvaro Sanchex-Leon - Udpated for TMF
 *
 * $Id: TimeScaleCtrl.java,v 1.5 2008/06/16 21:04:49 jkubasta Exp $ 
 *****************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.widgets;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.widgets.Utils.Resolution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class TimeScaleCtrl extends TraceCtrl implements MouseListener,
		MouseMoveListener {

	public TimeScaleCtrl(Composite parent, TraceColorScheme colors) {
		super(parent, colors, SWT.NO_BACKGROUND | SWT.NO_FOCUS
				| SWT.DOUBLE_BUFFERED);
		addMouseListener(this);
		addMouseMoveListener(this);
	}

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
    
    private static final Calendar GREGORIAN_CALENDAR = GregorianCalendar.getInstance();
    
	private ITimeDataProvider _timeProvider;
	private int _dragState = 0;
	private int _dragX0 = 0;
	private int _dragX = 0;
	private long _time0bak;
	private long _time1bak;
	private boolean _isInUpdate;
	private Rectangle _rect0 = new Rectangle(0, 0, 0, 0);
    private int _height;

	public void setTimeProvider(ITimeDataProvider timeProvider) {
		_timeProvider = timeProvider;
	}

	private long _timeDelta;

	@Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return super.computeSize(wHint, _height, changed);
    }

    public void setHeight(int height) {
        this._height = height;
    }
    
    private void calcTimeDelta(int width, double pixelsPerNanoSec) {
        double minDelta = (double) ((pixelsPerNanoSec == 0) ? YEAR_IN_NS : width / pixelsPerNanoSec);
        long unit = 1;
        if (_timeProvider != null && _timeProvider.isCalendarFormat()) {
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
                _timeDelta = 1;
                return;
            }
        }
        double log = Math.log10((double) minDelta / unit);
        long pow10 = (long) log;
        double remainder = log - pow10;
        if (remainder < LOG10_1) {
            _timeDelta = (long) Math.pow(10, pow10) * unit;
        } else if (remainder < LOG10_2) {
            _timeDelta = 2 * (long) Math.pow(10, pow10) * unit;
        } else if (remainder < LOG10_3 && unit >= HOUR_IN_NS && unit < YEAR_IN_NS) {
            _timeDelta = 3 * (long) Math.pow(10, pow10) * unit;
        } else if (remainder < LOG10_5) {
            _timeDelta = 5 * (long) Math.pow(10, pow10) * unit;
        } else {
            _timeDelta = 10 * (long) Math.pow(10, pow10) * unit;
        }
    }

    private static TimeDraw TIMEDRAW_NANOSEC = new TimeDrawNanosec();
    private static TimeDraw TIMEDRAW_MICROSEC = new TimeDrawMicrosec();
    private static TimeDraw TIMEDRAW_MILLISEC = new TimeDrawMillisec();
    private static TimeDraw TIMEDRAW_SEC = new TimeDrawSec();
    private static TimeDraw TIMEDRAW_ABS_NANOSEC = new TimeDrawAbsNanoSec();
    private static TimeDraw TIMEDRAW_ABS_MICROSEC = new TimeDrawAbsMicroSec();
    private static TimeDraw TIMEDRAW_ABS_MILLISEC = new TimeDrawAbsMillisec();
    private static TimeDraw TIMEDRAW_ABS_SEC = new TimeDrawAbsSec();
    private static TimeDraw TIMEDRAW_ABS_MIN = new TimeDrawAbsMin();
    private static TimeDraw TIMEDRAW_ABS_HRS = new TimeDrawAbsHrs();
    private static TimeDraw TIMEDRAW_ABS_DAY = new TimeDrawAbsDay();
    private static TimeDraw TIMEDRAW_ABS_MONTH = new TimeDrawAbsMonth();
    private static TimeDraw TIMEDRAW_ABS_YEAR = new TimeDrawAbsYear();

	TimeDraw getTimeDraw(long timeDelta) {
		TimeDraw timeDraw;
		if (_timeProvider != null && _timeProvider.isCalendarFormat()) {
            if (timeDelta >= YEAR_IN_NS)
                timeDraw = TIMEDRAW_ABS_YEAR;
            else if (timeDelta >= MONTH_IN_NS)
                timeDraw = TIMEDRAW_ABS_MONTH;
            else if (timeDelta >= DAY_IN_NS)
                timeDraw = TIMEDRAW_ABS_DAY;
            else if (timeDelta >= HOUR_IN_NS)
                timeDraw = TIMEDRAW_ABS_HRS;
            else if (timeDelta >= MIN_IN_NS)
                timeDraw = TIMEDRAW_ABS_MIN;
            else if (timeDelta >= SEC_IN_NS)
                timeDraw = TIMEDRAW_ABS_SEC;
			else if (timeDelta >= 1000000)
				timeDraw = TIMEDRAW_ABS_MILLISEC;
			else if (timeDelta >= 1000)
				timeDraw = TIMEDRAW_ABS_MICROSEC;
			else
				timeDraw = TIMEDRAW_ABS_NANOSEC;
			return timeDraw;
		}
		if (timeDelta >= 1000000000)
			timeDraw = TIMEDRAW_SEC;
		else if (timeDelta >= 1000000)
			timeDraw = TIMEDRAW_MILLISEC;
		else if (timeDelta >= 1000)
			timeDraw = TIMEDRAW_MICROSEC;
		else
			timeDraw = TIMEDRAW_NANOSEC;
		return timeDraw;
	}

	@Override
	void paint(Rectangle rect, PaintEvent e) {

		if (_isInUpdate || null == _timeProvider)
			return;

		GC gc = e.gc;
		gc.fillRectangle(rect);
		
		long time0 = _timeProvider.getTime0();
		long time1 = _timeProvider.getTime1();
		long selectedTime = _timeProvider.getSelectedTime();
		int leftSpace = _timeProvider.getNameSpace();
		int timeSpace = _timeProvider.getTimeSpace();
		
		gc.setBackground(_colors.getColor(TraceColorScheme.TOOL_BACKGROUND));
		gc.setForeground(_colors.getColor(TraceColorScheme.TOOL_FOREGROUND));
		Utils.init(_rect0, rect);
		
		// draw top left area
		_rect0.width = leftSpace;
		_rect0.x += 4;
		_rect0.width -= 4;
		if (_rect0.width > 0) {
		    Utils.drawText(gc, Messages.TimeScaleCtrl_Timescale + ":", _rect0, true); //$NON-NLS-1$
		}
		int messageWidth = gc.stringExtent(Messages.TimeScaleCtrl_Timescale + ":").x + 4; //$NON-NLS-1$
		Rectangle absHeaderRect = new Rectangle(_rect0.x + messageWidth, _rect0.y, _rect0.width - messageWidth, _rect0.height);
		_rect0.x -= 4;
		_rect0.width += 4;
		
		// prepare and draw right rect of the timescale
		_rect0.x += leftSpace;
		_rect0.width = rect.width - leftSpace;
		
		// draw bottom border and erase all other area
		gc.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width - 1,
		        rect.y + rect.height - 1);
		_rect0.height--;
		gc.fillRectangle(_rect0);
		
		if (time1 <= time0 || timeSpace < 2) {
		    return;
		}
		
		int numDigits = calculateDigits(time0, time1);
		
		int labelWidth = gc.getCharWidth('0') * numDigits;
		double pixelsPerNanoSec = (timeSpace <= RIGHT_MARGIN) ? 0 :
		    (double) (timeSpace - RIGHT_MARGIN) / (time1 - time0);
		calcTimeDelta(labelWidth, pixelsPerNanoSec);
		
		TimeDraw timeDraw = getTimeDraw(_timeDelta);

		// draw zoom rectangle
        if (3 == _dragState && null != _timeProvider) {
            if (_dragX0 < _dragX) {
                gc.drawRectangle(leftSpace + _dragX0, rect.y, _dragX - _dragX0 - 1, rect.height - 8);
            } else if (_dragX0 > _dragX) {
                gc.drawRectangle(leftSpace + _dragX, rect.y, _dragX0 - _dragX - 1, rect.height - 8);
            }
        }

		if (_rect0.isEmpty())
			return;

		// draw selected time
		int x = _rect0.x + (int) ((double)(selectedTime - time0) * pixelsPerNanoSec);
		if (x >= _rect0.x && x < _rect0.x + _rect0.width) {
			gc.setForeground(_colors.getColor(TraceColorScheme.SELECTED_TIME));
			gc.drawLine(x, _rect0.y + _rect0.height - 6, x, _rect0.y
					+ _rect0.height);
			gc
					.setForeground(_colors
							.getColor(TraceColorScheme.TOOL_FOREGROUND));
		}

		// draw time scale ticks
		_rect0.y = rect.y;
		_rect0.height = rect.height - 4;
		_rect0.width = labelWidth;
		
		long time;
        if (_timeProvider != null && _timeProvider.isCalendarFormat()) {
            time = floorToCalendar(time0, _timeDelta);
        } else {
            time = (long) (Math.ceil((double) time0 / _timeDelta) * _timeDelta);
        }
		
		// long t = (long) (time * 1000000000);
		int y = _rect0.y + _rect0.height;

        if (_timeProvider != null && _timeProvider.isCalendarFormat()) {
            timeDraw.drawAbsHeader(gc, time, absHeaderRect);
        }
		
		while (true) {
			x = rect.x + leftSpace + (int) (Math.floor((time - time0) * pixelsPerNanoSec));
			if (x >= rect.x + leftSpace + rect.width - _rect0.width) {
				break;
			}
			if (x >= rect.x + leftSpace) {
				gc.drawLine(x, y, x, y + 4);
				_rect0.x = x;
				if (x + _rect0.width <= rect.x + rect.width)
					timeDraw.draw(gc, time, _rect0);
			}
			if (pixelsPerNanoSec == 0 || time > Long.MAX_VALUE - _timeDelta || _timeDelta == 0) {
			    break;
			}
			time += _timeDelta;
            if (_timeProvider != null && _timeProvider.isCalendarFormat()) {
                if (_timeDelta >= YEAR_IN_NS) {
                    GREGORIAN_CALENDAR.setTime(new Date(time / 1000000));
                    GREGORIAN_CALENDAR.set(Calendar.MONTH, 0); // January 1st of year
                    GREGORIAN_CALENDAR.set(Calendar.DAY_OF_MONTH, 1);
                    time = GREGORIAN_CALENDAR.getTimeInMillis() * 1000000;
                } else if (_timeDelta >= MONTH_IN_NS) {
                    GREGORIAN_CALENDAR.setTime(new Date(time / 1000000));
                    GREGORIAN_CALENDAR.set(Calendar.DAY_OF_MONTH, 1); // 1st of month
                    time = GREGORIAN_CALENDAR.getTimeInMillis() * 1000000;
                }
            }
		}
	}

	private long floorToCalendar(long time, long timeDelta) {
        if (_timeDelta >= YEAR_IN_NS) {
            GREGORIAN_CALENDAR.setTime(new Date(time / 1000000));
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
            time = GREGORIAN_CALENDAR.getTimeInMillis() * 1000000;
        } else if (_timeDelta >= MONTH_IN_NS) {
            GREGORIAN_CALENDAR.setTime(new Date(time / 1000000));
            int month = GREGORIAN_CALENDAR.get(Calendar.MONTH);
            int monthDelta = (int) (timeDelta / MONTH_IN_NS);
            month = (month / monthDelta) * monthDelta;
            GREGORIAN_CALENDAR.set(Calendar.MONTH, month);
            GREGORIAN_CALENDAR.set(Calendar.DAY_OF_MONTH, 1); // 1st of month
            GREGORIAN_CALENDAR.set(Calendar.HOUR_OF_DAY, 0);
            GREGORIAN_CALENDAR.set(Calendar.MINUTE, 0);
            GREGORIAN_CALENDAR.set(Calendar.SECOND, 0);
            GREGORIAN_CALENDAR.set(Calendar.MILLISECOND, 0);
            time = GREGORIAN_CALENDAR.getTimeInMillis() * 1000000;
        } else {
            time = (time / timeDelta) * timeDelta;
        }
        return time;
	}
	
	private int calculateDigits(long time0, long time1) {
		int numDigits = 5;
		long timeRange = time1 - time0;

		if (_timeProvider.isCalendarFormat()) {
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
			// Calculate the number of digits to represent the minutes provided
			long min = (long) ((time1 * 1E-9) / 60); // to sec then to minutes
			String strMinutes = String.valueOf(min);
			// 11:222
			if (strMinutes != null) {
				numDigits += strMinutes.length();
			} else {
				numDigits += 2;
			}
			if (timeRange < 10000) {
				// 11:222:333:444__
				numDigits += 8;
			} else if (timeRange < 10000000) {
				// 11:222:333__
				numDigits += 4;
			}
		}

//		Trace.debug("timeRange: " + timeRange + " numDigits: " + numDigits);
		return numDigits;
	}

	@Override
	public void mouseDown(MouseEvent e) {
	    if (_dragState == 0 && null != _timeProvider) {
	        if (1 == e.button) {
	            setCapture(true);
	            _dragState = 1;
	        } else if (3 == e.button) {
	            _dragState = 3;
	        }
	        int x = e.x - _timeProvider.getNameSpace();
	        if (x < 0) {
	            x = 0;
	        } else if (x > getSize().x - _timeProvider.getNameSpace()) {
	            x = getSize().x - _timeProvider.getNameSpace();
	        }
	        _dragX = _dragX0 = x;
	        _time0bak = _timeProvider.getTime0();
	        _time1bak = _timeProvider.getTime1();
	    }
	}

	@Override
	public void mouseUp(MouseEvent e) {
		if (e.button == 1 && _dragState == 1) {
			setCapture(false);
			_dragState = 0;
			
	        // Notify time provider to check the need for listener notification
			if (_dragX != _dragX0) {
			    _timeProvider.setStartFinishTimeNotify(_timeProvider.getTime0(), _timeProvider.getTime1());
			}
		} else if (e.button == 3 && _dragState == 3 && null != _timeProvider) {
		    _dragState = 0;
			if (_dragX0 == _dragX) {
				return;
			}
			int timeSpace = _timeProvider.getTimeSpace();
			int leftSpace = _timeProvider.getNameSpace();
			int x = e.x - leftSpace;
			if (timeSpace > 0) {
				_dragX = x;
				if (_dragX0 > _dragX) { // drag right to left
				    _dragX = _dragX0;
				    _dragX0 = x;
				}
				long time0 = _time0bak + (long) ((_time1bak - _time0bak) * ((double) _dragX0 / timeSpace));
				long time1 = _time0bak + (long) ((_time1bak - _time0bak) * ((double) _dragX / timeSpace));

				_timeProvider.setStartFinishTimeNotify(time0, time1);
	            _time0bak = _timeProvider.getTime0();
	            _time1bak = _timeProvider.getTime1();
			}
		}
	}

	@Override
	public void mouseMove(MouseEvent e) {
		if (_dragX0 < 0 || _dragState == 0 || _timeProvider == null) {
			return;
		}
		Point size = getSize();
		int leftSpace = _timeProvider.getNameSpace();
		int timeSpace = _timeProvider.getTimeSpace();
		int x = e.x - leftSpace;
		if (1 == _dragState) {
			if (x > 0 && size.x > leftSpace && _dragX != x) {
				_dragX = x;
				long time1 = _time0bak + (long) ((_time1bak - _time0bak) * ((double) _dragX0 / _dragX));
				_timeProvider.setStartFinishTime(_time0bak, time1);
			}
		} else if (3 == _dragState) {
		    if (x < 0) {
		        _dragX = 0;
		    } else if (x > timeSpace) {
		        _dragX = timeSpace;
		    } else {
                _dragX = x;
            }
		    redraw();
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (null != _timeProvider) {
			_timeProvider.resetStartFinishTime();
            _time0bak = _timeProvider.getTime0();
            _time1bak = _timeProvider.getTime1();
		}
	}
}

abstract class TimeDraw {
	static String S   = ":"  ; //$NON-NLS-1$
	static String S0  = ":0" ; //$NON-NLS-1$
	static String S00 = ":00"; //$NON-NLS-1$
    protected static final SimpleDateFormat stimeformat = new SimpleDateFormat("HH:mm:ss");          //$NON-NLS-1$
    protected static final SimpleDateFormat stimeformatheader = new SimpleDateFormat("yyyy MMM dd"); //$NON-NLS-1$
    protected static final SimpleDateFormat sminformat = new SimpleDateFormat("HH:mm");              //$NON-NLS-1$
    protected static final SimpleDateFormat sminformatheader = new SimpleDateFormat("yyyy MMM dd");  //$NON-NLS-1$
    protected static final SimpleDateFormat shrsformat = new SimpleDateFormat("MMM dd HH:mm"); 	     //$NON-NLS-1$
    protected static final SimpleDateFormat shrsformatheader = new SimpleDateFormat("yyyy");         //$NON-NLS-1$
    protected static final SimpleDateFormat sdayformat = new SimpleDateFormat("MMM dd");             //$NON-NLS-1$
    protected static final SimpleDateFormat sdayformatheader = new SimpleDateFormat("yyyy");         //$NON-NLS-1$
    protected static final SimpleDateFormat smonthformat = new SimpleDateFormat("yyyy MMM");         //$NON-NLS-1$
    protected static final SimpleDateFormat syearformat = new SimpleDateFormat("yyyy");              //$NON-NLS-1$
	
	static String pad(long n) {
		String s = S;
		if (n < 10)
			s = S00;
		else if (n < 100)
			s = S0;
		return s + n;
	}

    public abstract void draw(GC gc, long time, Rectangle rect);

    public void drawAbsHeader(GC gc, long time, Rectangle absHeaderRect) {
        // Override to draw absolute time header
        // This is for the time information not shown in the draw of each tick
    }
    
	public abstract String hint();
}

class TimeDrawSec extends TimeDraw {
	static String _hint = "sec"; //$NON-NLS-1$

	@Override
	public void draw(GC gc, long time, Rectangle rect) {
		time /= 1000000000;
		Utils.drawText(gc, time + "", rect, true); //$NON-NLS-1$
	}

	@Override
	public String hint() {
		return _hint;
	}
}

class TimeDrawMillisec extends TimeDraw {
	static String _hint = "s:ms"; //$NON-NLS-1$

	@Override
	public void draw(GC gc, long time, Rectangle rect) {
		time /= 1000000;
		long ms = time % 1000;
		time /= 1000;
		Utils.drawText(gc, time + pad(ms), rect, true);
	}

	@Override
	public String hint() {
		return _hint;
	}
}

class TimeDrawMicrosec extends TimeDraw {
	static String _hint = "s:ms:mcs"; //$NON-NLS-1$

	@Override
	public void draw(GC gc, long time, Rectangle rect) {
		time /= 1000;
		long mcs = time % 1000;
		time /= 1000;
		long ms = time % 1000;
		time /= 1000;
		Utils.drawText(gc, time + pad(ms) + pad(mcs), rect, true);
	}

	@Override
	public String hint() {
		return _hint;
	}
}

class TimeDrawNanosec extends TimeDraw {
	static String _hint = "s:ms:mcs:ns"; //$NON-NLS-1$

	@Override
	public void draw(GC gc, long time, Rectangle rect) {
		long ns = time % 1000;
		time /= 1000;
		long mcs = time % 1000;
		time /= 1000;
		long ms = time % 1000;
		time /= 1000;
		Utils.drawText(gc, time + pad(ms) + pad(mcs) + pad(ns), rect, true);
	}

	@Override
	public String hint() {
		return _hint;
	}
}

class TimeDrawAbsYear extends TimeDraw {
    static String _hint = "YYYY"; //$NON-NLS-1$

    @Override
    public void draw(GC gc, long time, Rectangle rect) {
        String stime = syearformat.format(new Date((long) (time / 1000000)));
        Utils.drawText(gc, stime, rect, true);
    }

    @Override
    public String hint() {
        return _hint;
    }
}

class TimeDrawAbsMonth extends TimeDraw {
    static String _hint = "YYYY Mmm"; //$NON-NLS-1$

    @Override
    public void draw(GC gc, long time, Rectangle rect) {
        String stime = smonthformat.format(new Date((long) (time / 1000000)));
        Utils.drawText(gc, stime, rect, true);
    }

    @Override
    public String hint() {
        return _hint;
    }
}

class TimeDrawAbsDay extends TimeDraw {
    static String _hint = "Mmm dd"; //$NON-NLS-1$

    @Override
    public void draw(GC gc, long time, Rectangle rect) {
        String stime = sdayformat.format(new Date((long) (time / 1000000)));
        Utils.drawText(gc, stime, rect, true);
    }

    @Override
    public void drawAbsHeader(GC gc, long time, Rectangle rect) {
        String header = sdayformatheader.format(new Date((long) (time / 1000000)));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
    
    @Override
    public String hint() {
        return _hint;
    }
}

class TimeDrawAbsHrs extends TimeDraw {
    static String _hint = "Mmm dd HH:mm"; //$NON-NLS-1$

    @Override
    public void draw(GC gc, long time, Rectangle rect) {
        String stime = shrsformat.format(new Date((long) (time / 1000000)));
        Utils.drawText(gc, stime, rect, true);
    }

    @Override
    public void drawAbsHeader(GC gc, long time, Rectangle rect) {
        String header = shrsformatheader.format(new Date((long) (time / 1000000)));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
    
    @Override
    public String hint() {
        return _hint;
    }
}

class TimeDrawAbsMin extends TimeDraw {
    static String _hint = "HH:mm"; //$NON-NLS-1$

    @Override
    public void draw(GC gc, long time, Rectangle rect) {
        String stime = sminformat.format(new Date((long) (time / 1000000)));
        Utils.drawText(gc, stime, rect, true);
    }

    @Override
    public void drawAbsHeader(GC gc, long time, Rectangle rect) {
        String header = sminformatheader.format(new Date((long) (time / 1000000)));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
    
    
    @Override
    public String hint() {
        return _hint;
    }
}

class TimeDrawAbsSec extends TimeDraw {
    static String _hint = "HH:mm:ss"; //$NON-NLS-1$

    @Override
    public void draw(GC gc, long time, Rectangle rect) {
        String stime = stimeformat.format(new Date((long) (time / 1000000)));
        Utils.drawText(gc, stime, rect, true);
    }

    @Override
    public void drawAbsHeader(GC gc, long time, Rectangle rect) {
        String header = stimeformatheader.format(new Date((long) (time / 1000000)));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
    
    @Override
    public String hint() {
        return _hint;
    }
}

class TimeDrawAbsMillisec extends TimeDraw {
	static String _hint = "HH:ss:ms"; //$NON-NLS-1$

	@Override
	public void draw(GC gc, long time, Rectangle rect) {
		String stime = stimeformat.format(new Date((long) (time / 1000000)));
		String ns = Utils.formatNs(time, Resolution.MILLISEC);

		Utils.drawText(gc, stime + " " + ns, rect, true); //$NON-NLS-1$
	}

    @Override
    public void drawAbsHeader(GC gc, long time, Rectangle rect) {
        String header = stimeformatheader.format(new Date((long) (time / 1000000)));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
    
	@Override
	public String hint() {
		return _hint;
	}
}

class TimeDrawAbsMicroSec extends TimeDraw {
	static String _hint = "HH:ss:ms:mcs"; //$NON-NLS-1$

	@Override
	public void draw(GC gc, long time, Rectangle rect) {
		String stime = stimeformat.format(new Date((long) (time / 1000000)));
		String micr = Utils.formatNs(time, Resolution.MICROSEC);
		Utils.drawText(gc, stime + " " + micr, rect, true); //$NON-NLS-1$
	}

    @Override
    public void drawAbsHeader(GC gc, long time, Rectangle rect) {
        String header = stimeformatheader.format(new Date((long) (time / 1000000)));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
    
	@Override
	public String hint() {
		return _hint;
	}
}

class TimeDrawAbsNanoSec extends TimeDraw {
	static String _hint = "HH:ss:ms:mcs:ns"; //$NON-NLS-1$

	@Override
	public void draw(GC gc, long time, Rectangle rect) {
		String stime = stimeformat.format(new Date((long) (time / 1000000)));
		String ns = Utils.formatNs(time, Resolution.NANOSEC);
		Utils.drawText(gc, stime + " " + ns, rect, true); //$NON-NLS-1$
	}

    @Override
    public void drawAbsHeader(GC gc, long time, Rectangle rect) {
        String header = stimeformatheader.format(new Date((long) (time / 1000000)));
        int headerwidth = gc.stringExtent(header).x + 4;
        if (headerwidth <= rect.width) {
            rect.x += (rect.width - headerwidth);
            Utils.drawText(gc, header, rect, true);
        }
    }
    
	@Override
	public String hint() {
		return _hint;
	}
}
