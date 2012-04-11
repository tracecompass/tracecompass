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

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeAnalysisViewer.TimeFormat;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITmfTimeAnalysisEntry;
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

	static private final SimpleDateFormat stimeformat = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
	static private final SimpleDateFormat sdateformat = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	static {
        stimeformat.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
        sdateformat.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
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
	 * @param time time
	 * @param format  0: MMMM:ss:nnnnnnnnn, 1: HH:MM:ss MMM.mmmm.nnn
	 * @param resolution the resolution
	 * @return the formatted time
	 */
	static public String formatTime(long time, TimeFormat format, Resolution resolution) {
		// if format is absolute (Calendar)
		if (format == TimeFormat.ABSOLUTE) {
			return formatTimeAbs(time, resolution);
		}

		StringBuffer str = new StringBuffer();
		boolean neg = time < 0;
		if (neg) {
			time = -time;
			str.append('-');
		}

		long sec = (long) (time * 1E-9);
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
		String ns = formatNs(time, resolution);
		if (!ns.equals("")) { //$NON-NLS-1$
			str.append(':');
			str.append(ns);
		}

		return str.toString();
	}

	/**
	 * From input time in nanoseconds, convert to Date format YYYY-MM-dd
	 * 
	 * @param absTime
	 * @return the formatted date
	 */
	public static String formatDate(long absTime) {
		String sdate = sdateformat.format(new Date((long) (absTime * 1E-6)));
		return sdate;
	}

	/**
	 * Formats time in ns to Calendar format: HH:MM:SS MMM.mmm.nnn
	 * 
	 * @param time
	 * @return the formatted time
	 */
	static public String formatTimeAbs(long time, Resolution res) {
		StringBuffer str = new StringBuffer();

		// format time from nanoseconds to calendar time HH:MM:SS
		String stime = stimeformat.format(new Date((long) (time * 1E-6)));
		str.append(stime + " "); //$NON-NLS-1$
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
	 * @return the formatted nanosec
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
			temp.append("00000000"); //$NON-NLS-1$
		} else if (ns < 100) {
			temp.append("0000000"); //$NON-NLS-1$
		} else if (ns < 1000) {
			temp.append("000000"); //$NON-NLS-1$
		} else if (ns < 10000) {
			temp.append("00000"); //$NON-NLS-1$
		} else if (ns < 100000) {
			temp.append("0000"); //$NON-NLS-1$
		} else if (ns < 1000000) {
			temp.append("000"); //$NON-NLS-1$
		} else if (ns < 10000000) {
			temp.append("00"); //$NON-NLS-1$
		} else if (ns < 100000000) {
			temp.append("0"); //$NON-NLS-1$
		}
		temp.append(ns);

		StringBuffer str = new StringBuffer();
		if (segments > 0) {
			// append ms
			str.append(temp.substring(0, 3));
		}
		if (segments > 1) {
			// append Micro secs
			str.append("."); //$NON-NLS-1$
			str.append(temp.substring(3, 6));
		}
		if (segments > 2) {
			// append Nano seconds
			str.append("."); //$NON-NLS-1$
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
		Iterator<ITimeEvent> iterator = thread.getTraceEventsIterator();
		if (iterator.hasNext()) {
		    return iterator.next();
		} else {
		    return null;
		}
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
        Iterator<ITimeEvent> iterator = thread.getTraceEventsIterator();
        ITimeEvent nextEvent = null;
        ITimeEvent currEvent = null;
        ITimeEvent prevEvent = null;

        while (iterator.hasNext()) {
            nextEvent = (ITimeEvent) iterator.next();
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
			sig = sig + " " + ret; //$NON-NLS-1$
		}
		return sig;
	}

	static public String restoreMethodSignature(String sig) {
		String ret = ""; //$NON-NLS-1$
		int pos = sig.indexOf('(');
		if (pos >= 0) {
			ret = sig.substring(0, pos);
			sig = sig.substring(pos + 1);
		}
		pos = sig.indexOf(')');
		if (pos >= 0) {
			sig = sig.substring(0, pos);
		}
		String args[] = sig.split(","); //$NON-NLS-1$
        StringBuffer result = new StringBuffer("("); //$NON-NLS-1$
		for (int i = 0; i < args.length; i++) {
			String arg = args[i].trim();
			if (arg.length() == 0 && args.length == 1)
				break;
			result.append(getTypeSignature(arg));
		}
		result.append(")").append(getTypeSignature(ret)); //$NON-NLS-1$
		return result.toString();
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
		StringBuffer sig = new StringBuffer(""); //$NON-NLS-1$
		for (int j = 0; j < dim; j++)
			sig.append("[");                 //$NON-NLS-1$
		if (type.equals("boolean"))     //$NON-NLS-1$
			sig.append("Z");                 //$NON-NLS-1$
		else if (type.equals("byte"))   //$NON-NLS-1$
			sig.append("B");                 //$NON-NLS-1$
		else if (type.equals("char"))   //$NON-NLS-1$
			sig.append("C");                 //$NON-NLS-1$
		else if (type.equals("short"))  //$NON-NLS-1$
			sig.append("S");                 //$NON-NLS-1$
		else if (type.equals("int"))    //$NON-NLS-1$
			sig.append("I");                 //$NON-NLS-1$
		else if (type.equals("long"))   //$NON-NLS-1$
			sig.append("J");                 //$NON-NLS-1$
		else if (type.equals("float"))  //$NON-NLS-1$
			sig.append("F");                 //$NON-NLS-1$
		else if (type.equals("double")) //$NON-NLS-1$
			sig.append("D");                 //$NON-NLS-1$
		else if (type.equals("void"))   //$NON-NLS-1$
			sig.append("V");                 //$NON-NLS-1$
		else
			sig.append("L").append(type.replace('.', '/')).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
		return sig.toString();
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
