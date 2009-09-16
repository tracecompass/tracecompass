/*****************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
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

package org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.Messages;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.Utils.Resolution;
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

	private ITimeDataProvider _timeProvider;
	private int _dragState = 0;
	private int _dragX0 = 0;
	private int _dragX = 0;
	private long _time0bak;
	private long _time1bak;
	private boolean _isInUpdate;
	private Rectangle _rect0 = new Rectangle(0, 0, 0, 0);

	public void setTimeProvider(ITimeDataProvider timeProvider) {
		_timeProvider = timeProvider;
	}

	private double _timeDeltaD;
	private long _timeDelta;

	private void calcTimeDelta(int width, double K) {
		long D[] = { 1, 2, 5, };
		long pow = 1;
		double powD = 1;
		long td = pow;
		double tdD = powD;
		double dx = tdD * K;
		int i = 0;
		while (dx < width) {
			td = D[i] * pow;
			tdD = D[i] * powD;
			dx = tdD * K;
			i++;
			if (i == 3) {
				i = 0;
				pow *= 10;
				powD *= 10;
			}
		}
		_timeDeltaD = tdD;
		_timeDelta = td;
//		Trace.debug("Width: " + width + " K: " + K + " Time Delta: "
//				+ _timeDelta);
	}

	static private TimeDraw _tds[] = new TimeDraw[] { new TimeDrawSec(),
			new TimeDrawMillisec(), new TimeDrawMicrosec(),
			new TimeDrawNanosec(), new TimeDrawAbsSec(),
			new TimeDrawAbsMillisec(), new TimeDrawAbsMicroSec(),
			new TimeDrawAbsNanoSec() };

	TimeDraw getTimeDraw(long timeDelta) {
		TimeDraw timeDraw;
		if (_timeProvider != null) {
			if (_timeProvider.isCalendarFormat()) {
				if (timeDelta >= 1000000000)
					timeDraw = _tds[4];
				else if (timeDelta >= 1000000)
					timeDraw = _tds[5];
				else if (timeDelta >= 1000)
					timeDraw = _tds[6];
				else
					timeDraw = _tds[7];
				return timeDraw;
			}
		}
		if (timeDelta >= 1000000000)
			timeDraw = _tds[0];
		else if (timeDelta >= 1000000)
			timeDraw = _tds[1];
		else if (timeDelta >= 1000)
			timeDraw = _tds[2];
		else
			timeDraw = _tds[3];
		return timeDraw;
	}

	void paint(Rectangle rect, PaintEvent e) {

		if (_isInUpdate || null == _timeProvider)
			return;

		GC gc = e.gc;
		if (null == _timeProvider) {
			gc.fillRectangle(rect);
			return;
		}

		gc.setBackground(_colors.getColor(TraceColorScheme.TOOL_BACKGROUND));
		gc.setForeground(_colors.getColor(TraceColorScheme.TOOL_FOREGROUND));
		long time0 = _timeProvider.getTime0();
		long time1 = _timeProvider.getTime1();
		long selectedTime = _timeProvider.getSelectedTime();
		int leftSpace = _timeProvider.getNameSpace();
		int timeSpace = _timeProvider.getTimeSpace();

		if (time1 <= time0 || timeSpace < 2) {
			gc.fillRectangle(rect);
			return;
		}

		int numDigits = calculateDigits(time0, time1);

		Utils.init(_rect0, rect);
		int labelWidth = gc.getCharWidth('0') * numDigits;
		double K = 1;
		if (rect.width - leftSpace > 0) {
			K = (double) timeSpace / (time1 - time0);
			calcTimeDelta(labelWidth, K);
		}
		
		TimeDraw timeDraw = getTimeDraw(_timeDelta);

		// draw top left area
		_rect0.width = leftSpace;
		gc.fillRectangle(_rect0);
		_rect0.x += 4;
		_rect0.width -= 4;
		if (_rect0.width > 0) {
// TODO: Check if we really need that piece of code...			
//			if (false && rect.width - leftSpace > 0)
//				Utils.drawText(gc, Messages._Timescale + ": "
//						+ timeDraw.hint(), _rect0, true);
//			else
				Utils.drawText(gc, Messages._Timescale + ":", _rect0, true);
		}
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

		if (_rect0.isEmpty())
			return;

		// draw selected time
		int x = _rect0.x + (int) ((selectedTime - time0) * K);
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
		long time = (long) (Math.floor(time0 / _timeDeltaD) * _timeDeltaD);
		// long t = (long) (time * 1000000000);
		long t = time;
		int y = _rect0.y + _rect0.height;
		while (true) {
			x = rect.x + leftSpace + (int) ((time - time0) * K);
			if (x >= rect.x + leftSpace + rect.width - _rect0.width) {
				break;
			}
			if (x >= rect.x + leftSpace) {
				gc.drawLine(x, y, x, y + 4);
				_rect0.x = x;
				if (x + _rect0.width <= rect.x + rect.width)
					timeDraw.draw(gc, t, _rect0);
			}
			time += _timeDeltaD;
			t += _timeDelta;
		}
	}

	private int calculateDigits(long time0, long time1) {
		int numDigits = 5;
		long timeRange = time1 - time0;

		if (_timeProvider.isCalendarFormat()) {
			// Calculate the number of digits to represent the minutes provided
			// 11:222
			// HH:mm:ss
			numDigits += 5;
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

	public void mouseDown(MouseEvent e) {
		if (1 == e.button && null != _timeProvider) {
			setCapture(true);
			_dragState = 1;
		}
		// Window adjustment allowed using mouse button three.
		_dragX = _dragX0 = e.x - _timeProvider.getNameSpace();
		_time0bak = _timeProvider.getTime0();
		_time1bak = _timeProvider.getTime1();
	}

	public void mouseUp(MouseEvent e) {
		if (1 == _dragState) {
			setCapture(false);
			_dragState = 0;
		}

		if (3 == e.button && null != _timeProvider) {
			if (_dragX0 < 0) {
				return;
			}
			Point size = getSize();

			int leftSpace = _timeProvider.getNameSpace();
			int x = e.x - leftSpace;
			if (x > 0 && size.x > leftSpace && _dragX != x) {
				_dragX = x;
				long time0 = _time0bak + ((_time1bak - _time0bak) * _dragX0)
						/ (size.x - leftSpace);
				long time1 = _time0bak + ((_time1bak - _time0bak) * _dragX)
						/ (size.x - leftSpace);

				_timeProvider.setStartFinishTime(time0, time1);
			}
		}
	}

	public void mouseMove(MouseEvent e) {
		if (_dragX0 < 0) {
			return;
		}
		Point size = getSize();
		if (1 == _dragState && null != _timeProvider) {
			int leftSpace = _timeProvider.getNameSpace();
			int x = e.x - leftSpace;
			if (x > 0 && size.x > leftSpace && _dragX != x) {
				_dragX = x;
				long time1 = _time0bak + ((_time1bak - _time0bak) * _dragX0)
						/ _dragX;
				_timeProvider.setStartFinishTime(_time0bak, time1);
			}
		}
	}

	public void mouseDoubleClick(MouseEvent e) {
		if (null != _timeProvider) {
			_timeProvider.resetStartFinishTime();
		}
	}
}

abstract class TimeDraw {
	static String S = ":";
	static String S0 = ":0";
	static String S00 = ":00";
	protected SimpleDateFormat stimeformat = new SimpleDateFormat("HH:mm:ss");
	
	static String pad(long n) {
		String s = S;
		if (n < 10)
			s = S00;
		else if (n < 100)
			s = S0;
		return s + n;
	}

	public abstract void draw(GC gc, long time, Rectangle rect);

	public abstract String hint();
}

class TimeDrawSec extends TimeDraw {
	static String _hint = "sec";

	public void draw(GC gc, long time, Rectangle rect) {
		time /= 1000000000;
		Utils.drawText(gc, time + "", rect, true);
	}

	public String hint() {
		return _hint;
	}
}

class TimeDrawMillisec extends TimeDraw {
	static String _hint = "s:ms";

	public void draw(GC gc, long time, Rectangle rect) {
		time /= 1000000;
		long ms = time % 1000;
		time /= 1000;
		Utils.drawText(gc, time + pad(ms), rect, true);
	}

	public String hint() {
		return _hint;
	}
}

class TimeDrawMicrosec extends TimeDraw {
	static String _hint = "s:ms:mcs";

	public void draw(GC gc, long time, Rectangle rect) {
		time /= 1000;
		long mcs = time % 1000;
		time /= 1000;
		long ms = time % 1000;
		time /= 1000;
		Utils.drawText(gc, time + pad(ms) + pad(mcs), rect, true);
	}

	public String hint() {
		return _hint;
	}
}

class TimeDrawNanosec extends TimeDraw {
	static String _hint = "s:ms:mcs:ns";

	public void draw(GC gc, long time, Rectangle rect) {
		long ns = time % 1000;
		time /= 1000;
		long mcs = time % 1000;
		time /= 1000;
		long ms = time % 1000;
		time /= 1000;
		Utils.drawText(gc, time + pad(ms) + pad(mcs) + pad(ns), rect, true);
	}

	public String hint() {
		return _hint;
	}
}

class TimeDrawAbsSec extends TimeDraw {
	static String _hint = "HH:mm:ss";

	public void draw(GC gc, long time, Rectangle rect) {
		String stime = stimeformat.format(new Date((long) (time * 1E-6)));
		Utils.drawText(gc, stime, rect, true);
	}

	public String hint() {
		return _hint;
	}
}

class TimeDrawAbsMillisec extends TimeDraw {
	static String _hint = "HH:ss:ms";

	public void draw(GC gc, long time, Rectangle rect) {
		String stime = stimeformat.format(new Date((long) (time * 1E-6)));
		String ns = Utils.formatNs(time, Resolution.MILLISEC);

		Utils.drawText(gc, stime + " " + ns, rect, true);
	}

	public String hint() {
		return _hint;
	}
}

class TimeDrawAbsMicroSec extends TimeDraw {
	static String _hint = "HH:ss:ms:mcs";

	public void draw(GC gc, long time, Rectangle rect) {
		String stime = stimeformat.format(new Date((long) (time * 1E-6)));
		String micr = Utils.formatNs(time, Resolution.MICROSEC);
		Utils.drawText(gc, stime + " " + micr, rect, true);
	}

	public String hint() {
		return _hint;
	}
}

class TimeDrawAbsNanoSec extends TimeDraw {
	static String _hint = "HH:ss:ms:mcs:ns";

	public void draw(GC gc, long time, Rectangle rect) {
		String stime = stimeformat.format(new Date((long) (time * 1E-6)));
		String ns = Utils.formatNs(time, Resolution.NANOSEC);
		Utils.drawText(gc, stime + " " + ns, rect, true);
	}

	public String hint() {
		return _hint;
	}
}
