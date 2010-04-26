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
 * $Id: Utils.java,v 1.11 2008/06/16 21:04:49 jkubasta Exp $ 
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITimeAnalysisViewer.TimeFormat;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.TimeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class Utils {

	static public final int IMG_THREAD_RUNNING = 0;
	static public final int IMG_THREAD_SUSPENDED = 1;
	static public final int IMG_THREAD_STOPPED = 2;
	static public final int IMG_METHOD_RUNNING = 3;
	static public final int IMG_METHOD = 4;
	static public final int IMG_NUM = 5;

	static public final Object[] _empty = new Object[0];

	static enum Resolution {
		SECONDS, MILLISEC, MICROSEC, NANOSEC
	};

	static private final SimpleDateFormat stimeformat = new SimpleDateFormat("HH:mm:ss");
	static private final SimpleDateFormat sdateformat = new SimpleDateFormat("yyyy-MM-dd");
	static {
        stimeformat.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdateformat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

//	static private String _externalPlugin[] = { "org.eclipse.debug.ui",
//			"org.eclipse.debug.ui", "org.eclipse.debug.ui",
//			"org.eclipse.debug.ui", "org.eclipse.debug.ui", };
//
//	static private String _externalPath[] = {
//			"icons/full/obj16/thread_obj.gif", // running thread
//			"icons/full/obj16/threads_obj.gif", // suspended
//			"icons/full/obj16/threadt_obj.gif", // stopped
//			"icons/full/obj16/stckframe_running_obj.gif", // running stack frame
//			"icons/full/obj16/stckframe_obj.gif", // stack frame
//	};

//	static public Image getImage(int idx) {
//		if (idx < 0 || idx >= IMG_NUM)
//			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
//		String key = "trace.img." + idx;
//		Image img = TimeAnalysisPlugin.getDefault().getImageRegistry().get(key);
//		if (null == img) {
//			ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(
//					_externalPlugin[idx], _externalPath[idx]);
//			TimeAnalysisPlugin.getDefault().getImageRegistry().put(key, desc);
//			img = TimeAnalysisPlugin.getDefault().getImageRegistry().get(key);
//		}
//		return img;
//	}

	static public void init(Rectangle rect) {
		rect.x = 0;
		rect.y = 0;
		rect.width = 0;
		rect.height = 0;
	}

	static public void init(Rectangle rect, int x, int y, int width, int height) {
		rect.x = x;
		rect.y = y;
		rect.width = width;
		rect.height = height;
	}

	static public void init(Rectangle rect, Rectangle source) {
		rect.x = source.x;
		rect.y = source.y;
		rect.width = source.width;
		rect.height = source.height;
	}

	static public void deflate(Rectangle rect, int x, int y) {
		rect.x += x;
		rect.y += y;
		rect.width -= x + x;
		rect.height -= y + y;
	}

	static public void inflate(Rectangle rect, int x, int y) {
		rect.x -= x;
		rect.y -= y;
		rect.width += x + x;
		rect.height += y + y;
	}

	static void dispose(Color col) {
		if (null != col)
			col.dispose();
	}

	static public Color mixColors(Device display, Color c1, Color c2, int w1,
			int w2) {
		return new Color(display, (w1 * c1.getRed() + w2 * c2.getRed())
				/ (w1 + w2), (w1 * c1.getGreen() + w2 * c2.getGreen())
				/ (w1 + w2), (w1 * c1.getBlue() + w2 * c2.getBlue())
				/ (w1 + w2));
	}

	static public Color getSysColor(int id) {
		Color col = Display.getCurrent().getSystemColor(id);
		return new Color(col.getDevice(), col.getRGB());
	}

	static public Color mixColors(Color col1, Color col2, int w1, int w2) {
		return mixColors(Display.getCurrent(), col1, col2, w1, w2);
	}

	static public int drawText(GC gc, String text, Rectangle rect,
			boolean transp) {
		Point size = gc.stringExtent(text);
		gc.drawText(text, rect.x, rect.y, transp);
		return size.x;
	}

	static public int drawText(GC gc, String text, int x, int y, boolean transp) {
		Point size = gc.stringExtent(text);
		gc.drawText(text, x, y, transp);
		return size.x;
	}

	/**
	 * Formats time in format: MM:SS:NNN
	 * 
	 * @param v
	 * @param option
	 *            0: MMMM:ss:nnnnnnnnn, 1: HH:MM:ss MMM.mmmm.nnn
	 * @return
	 */
	static public String formatTime(long v, TimeFormat format, Resolution res) {
		// if format is absolute (Calendar)
		if (format == TimeFormat.ABSOLUTE) {
			return formatTimeAbs(v, res);
		}

		StringBuffer str = new StringBuffer();
		boolean neg = v < 0;
		if (neg) {
			v = -v;
			str.append('-');
		}

		long sec = (long) (v * 1E-9);
		// TODO: Expand to make it possible to select the minute, second, nanosecond format
		//printing minutes is suppressed just sec and ns
		// if (sec / 60 < 10)
		// str.append('0');
		// str.append(sec / 60);
		// str.append(':');
		// sec %= 60;
		// if (sec < 10)
		// str.append('0');
		str.append(sec);
		String ns = formatNs(v, res);
		if (!ns.equals("")) {
			str.append(':');
			str.append(ns);
		}

		return str.toString();
	}

	/**
	 * From input time in nanoseconds, convert to Date format YYYY-MM-dd
	 * 
	 * @param absTime
	 * @return
	 */
	public static String formatDate(long absTime) {
		String sdate = sdateformat.format(new Date((long) (absTime * 1E-6)));
		return sdate;
	}

	/**
	 * Formats time in ns to Calendar format: HH:MM:SS MMM.mmm.nnn
	 * 
	 * @param time
	 * @return
	 */
	static public String formatTimeAbs(long time, Resolution res) {
		StringBuffer str = new StringBuffer();

		// format time from nanoseconds to calendar time HH:MM:SS
		String stime = stimeformat.format(new Date((long) (time * 1E-6)));
		str.append(stime + " ");
		// append the Milliseconds, MicroSeconds and NanoSeconds as specified in
		// the Resolution
		str.append(formatNs(time, res));
		return str.toString();
	}

	/**
	 * Obtains the remainder fraction on unit Seconds of the entered value in
	 * nanoseconds. e.g. input: 1241207054171080214 ns The number of fraction
	 * seconds can be obtained by removing the last 9 digits: 1241207054 the
	 * fractional portion of seconds, expressed in ns is: 171080214
	 * 
	 * @param time
	 * @param res
	 * @return
	 */
	public static String formatNs(long time, Resolution res) {
		StringBuffer temp = new StringBuffer();
		boolean neg = time < 0;
		if (neg) {
			time = -time;
		}

		// The following approach could be used although performance
		// decreases in half.
		// String strVal = String.format("%09d", time);
		// String tmp = strVal.substring(strVal.length() - 9);

		// number of segments to be included
		int segments = 0;
		switch (res) {
		case MILLISEC:
			segments = 1;
			break;
		case MICROSEC:
			segments = 2;
			break;
		case NANOSEC:
			segments = 3;
			break;
		default:
			break;
		}

		long ns = time;
		ns %= 1000000000;
		if (ns < 10) {
			temp.append("00000000");
		} else if (ns < 100) {
			temp.append("0000000");
		} else if (ns < 1000) {
			temp.append("000000");
		} else if (ns < 10000) {
			temp.append("00000");
		} else if (ns < 100000) {
			temp.append("0000");
		} else if (ns < 1000000) {
			temp.append("000");
		} else if (ns < 10000000) {
			temp.append("00");
		} else if (ns < 100000000) {
			temp.append("0");
		}
		temp.append(ns);

		StringBuffer str = new StringBuffer();
		if (segments > 0) {
			// append ms
			str.append(temp.substring(0, 3));
		}
		if (segments > 1) {
			// append Micro secs
			str.append(".");
			str.append(temp.substring(3, 6));
		}
		if (segments > 2) {
			// append Nano seconds
			str.append(".");
			str.append(temp.substring(6));
		}

		return str.toString();
	}

	static public int loadIntOption(String opt, int def, int min, int max) {
		// int val =
		// TraceUIPlugin.getDefault().getPreferenceStore().getInt(opt);
		// if (0 == val)
		// val = def;
		// if (val < min)
		// val = min;
		// if (val > max)
		// val = max;
		return def;
	}

	// static public int loadIntOption(String opt) {
	// int val = TraceUIPlugin.getDefault().getPreferenceStore().getInt(opt);
	// return val;
	// }

	static public void saveIntOption(String opt, int val) {
		// TraceUIPlugin.getDefault().getPreferenceStore().setValue(opt, val);
	}

	static ITimeEvent getFirstEvent(ITmfTimeAnalysisEntry thread) {
		if (null == thread)
			return null;
		Vector<TimeEvent> list =   thread.getTraceEvents();
		ITimeEvent event = null;
		if (!list.isEmpty())
			event = (ITimeEvent) list.get(0);
		return event;
	}

	/**
	 * N means: <list> <li>-1: Previous Event</li> <li>0: Current Event</li> <li>
	 * 1: Next Event</li> <li>2: Previous Event when located in a non Event Area
	 * </list>
	 * 
	 * @param thread
	 * @param time
	 * @param n
	 * @return
	 */
    static ITimeEvent findEvent(ITmfTimeAnalysisEntry thread, long time, int n) {
        if (null == thread)
            return null;
        List<TimeEvent> list = thread.getTraceEvents();
        Iterator<TimeEvent> it = list.iterator();
        ITimeEvent nextEvent = null;
        ITimeEvent currEvent = null;
        ITimeEvent prevEvent = null;

        while (it.hasNext()) {
            nextEvent = (ITimeEvent) it.next();
            long nextStartTime = nextEvent.getTime();
            
            if (nextStartTime > time) {
                break;
            }
            
            if (currEvent == null || currEvent.getTime() != nextStartTime) {
                prevEvent = currEvent;
                currEvent = nextEvent;
            }
        }
        
        if (n == -1) { //previous
            if (currEvent != null && currEvent.getTime() + currEvent.getDuration() >= time) {
                return prevEvent;
            } else {
                return currEvent;
            }
        } else if (n == 0) { //current
            if (currEvent != null && currEvent.getTime() + currEvent.getDuration() >= time) {
                return currEvent;
            } else {
                return null;
            }
        } else if (n == 1) { //next
            return nextEvent;
        } else if (n == 2) { //current or previous when in empty space
            return currEvent;
        }
        
        return null;
    }

	// static public TRCPackage getPackage(Object element) {
	// if (element instanceof TRCPackage)
	// return (TRCPackage) element;
	// if (element instanceof TRCClass)
	// return ((TRCClass) element).getPackage();
	// return null;
	// }

	// static public TRCObjectAllocationAnnotation getAllocationAnnotation(
	// TRCClass cls) {
	// TRCObjectAllocationAnnotation aa = null;
	// EList list = cls.getAnnotations();
	// int len = list.size();
	// for (int i = 0; i < len; i++) {
	// TRCAnnotation annotation = (TRCAnnotation) list.get(i);
	// if (annotation instanceof TRCObjectAllocationAnnotation)
	// aa = (TRCObjectAllocationAnnotation) annotation;
	// }
	// return aa;
	// }

	static public String fixMethodSignature(String sig) {
		int pos = sig.indexOf('(');
		if (pos >= 0) {
			String ret = sig.substring(0, pos);
			sig = sig.substring(pos);
			sig = sig + " " + ret;
		}
		return sig;
	}

	static public String restoreMethodSignature(String sig) {
		String ret = "";
		int pos = sig.indexOf('(');
		if (pos >= 0) {
			ret = sig.substring(0, pos);
			sig = sig.substring(pos + 1);
		}
		pos = sig.indexOf(')');
		if (pos >= 0) {
			sig = sig.substring(0, pos);
		}
		String args[] = sig.split(",");
		sig = "(";
		for (int i = 0; i < args.length; i++) {
			String arg = args[i].trim();
			if (arg.length() == 0 && args.length == 1)
				break;
			sig += getTypeSignature(arg);
		}
		sig += ")" + getTypeSignature(ret);
		return sig;
	}

	static public String getTypeSignature(String type) {
		int dim = 0;
		for (int j = 0; j < type.length(); j++) {
			if (type.charAt(j) == '[')
				dim++;
		}
		int pos = type.indexOf('[');
		if (pos >= 0)
			type = type.substring(0, pos);
		String sig = "";
		for (int j = 0; j < dim; j++)
			sig += "[";
		if (type.equals("boolean"))
			sig += "Z";
		else if (type.equals("byte"))
			sig += "B";
		else if (type.equals("char"))
			sig += "C";
		else if (type.equals("short"))
			sig += "S";
		else if (type.equals("int"))
			sig += "I";
		else if (type.equals("long"))
			sig += "J";
		else if (type.equals("float"))
			sig += "F";
		else if (type.equals("double"))
			sig += "D";
		else if (type.equals("void"))
			sig += "V";
		else
			sig += "L" + type.replace('.', '/') + ";";
		return sig;
	}

	// static public boolean openSource(Object element) {
	// if (element instanceof String) {
	// final String pattern = (String) element;
	// final int javaType = IJavaSearchConstants.METHOD;
	// BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
	// public void run() {
	// if (!OpenJavaSource.openSource(pattern, javaType,
	// SearchEngine.createWorkspaceScope(), true)) {
	// MessageDialog.openInformation(UIPlugin.getDefault()
	// .getWorkbench().getActiveWorkbenchWindow()
	// .getShell(), TraceMessages.TRC_MSGT, NLS.bind(
	// TraceUIMessages._68, pattern));
	// }
	// }
	// });
	// }
	// OpenSource.openSource(element);
	// return true;
	// }

	// static public int getObjAge(TRCFullTraceObject obj, EList listGC) {
	// int age = 0;
	// double t0 = obj.getCreateTime();
	// double t1 = obj.getCollectTime();
	// int len = listGC.size();
	// for (int j = 0; j < len; j++) {
	// TRCGCEvent gcEvent = (TRCGCEvent) listGC.get(j);
	// if (gcEvent.getType().equals("finish")) {
	// double time = gcEvent.getTime();
	// if (time <= t0)
	// continue;
	// if (t1 > 0 && time >= t1)
	// break;
	// age++;
	// }
	// }
	// return age;
	// }

	static public int compare(double d1, double d2) {
		if (d1 > d2)
			return 1;
		if (d1 < d2)
			return 1;
		return 0;
	}

	static public int compare(String s1, String s2) {
		if (s1 != null && s2 != null)
			return s1.compareToIgnoreCase(s2);
		if (s1 != null)
			return 1;
		if (s2 != null)
			return -1;
		return 0;
	}

	// static public String formatPercent(int val, int max) {
	// String s = max > 0 && max >= val ? TString
	// .formatAsPercentage((double) val / (double) max) : "";
	// return s;
	// }
}
