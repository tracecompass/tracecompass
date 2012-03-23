/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis;

import java.util.Map;

import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.TraceColorScheme;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public abstract class TmfTimeAnalysisProvider {
    
	static public final int IMG_THREAD_RUNNING = 0;
	static public final int IMG_THREAD_SUSPENDED = 1;
	static public final int IMG_THREAD_STOPPED = 2;
	static public final int IMG_METHOD_RUNNING = 3;
	static public final int IMG_METHOD = 4;
	static public final int IMG_NUM = 5;

	public enum StateColor {
		GREEN, DARK_BLUE, RED, GOLD, ORANGE, GRAY, BLACK, DARK_GREEN, DARK_YELLOW, MAGENTA3, PURPLE1, PINK1, AQUAMARINE, LIGHT_BLUE, CADET_BLUE, OLIVE;

		private String stateName;

		StateColor() {
			String undef = "Undefined"; //$NON-NLS-1$
			this.stateName = undef;
		}

		public String getStateName() {
			return stateName;
		}

		public void setStateName(String stateName) {
			this.stateName = stateName;
		}
	}

	// static private String _externalPath[] = {
	// "icons/full/obj16/thread_obj.gif", // running thread
	// "icons/full/obj16/threads_obj.gif", // suspended
	// "icons/full/obj16/threadt_obj.gif", // stopped
	// "icons/full/obj16/stckframe_running_obj.gif", // running stack frame
	// "icons/full/obj16/stckframe_obj.gif", // stack frame
	// };
	//
	// static private String _externalPlugin[] = { "org.eclipse.debug.ui",
	// "org.eclipse.debug.ui", "org.eclipse.debug.ui",
	// "org.eclipse.debug.ui", "org.eclipse.debug.ui", };
	//
	// static private Image getImage(int idx) {
	// if (idx < 0 || idx >= IMG_NUM)
	// SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	// String key = "trace.img." + idx;
	// Image img = TmfUiPlugin.getDefault().getImageRegistry().get(key);
	// if (null == img) {
	// ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(
	// _externalPlugin[idx], _externalPath[idx]);
	// TmfUiPlugin.getDefault().getImageRegistry().put(key, desc);
	// img = TmfUiPlugin.getDefault().getImageRegistry().get(key);
	// }
	// return img;
	// }

	public void drawState(TraceColorScheme colors, ITimeEvent event,
			Rectangle rect, GC gc, boolean selected, boolean rectBound,
			boolean timeSelected) {
		int colorIdx = getEventColorVal(event);
		drawState(colors, colorIdx, rect, gc, selected, rectBound, timeSelected);

	}

	public void drawState(TraceColorScheme colors, int colorIdx,
			Rectangle rect, GC gc, boolean selected, boolean rectBound,
			boolean timeSelected) {

		boolean visible = rect.width == 0 ? false : true;
		int colorIdx1 = colorIdx;
		
		timeSelected = timeSelected && selected;
		if (timeSelected) {
			colorIdx1 = colorIdx + TraceColorScheme.STATES_SEL0
					- TraceColorScheme.STATES0;
		}

		if (visible) {
			// fill all rect area
			if (rect.isEmpty())
				return;

			gc.setBackground(colors.getColor(colorIdx1));
			gc.fillRectangle(rect);
			colorIdx1 = colorIdx + TraceColorScheme.STATES_BORDER0
					- TraceColorScheme.STATES0;
			gc.setForeground(colors.getColor(colorIdx1));

			// draw bounds
			if (!timeSelected) {
				if (rectBound && rect.width >= 3) {
					gc.drawRectangle(rect.x, rect.y, rect.width - 1,
							rect.height - 1);
				} else {
					// Draw the top and bottom borders i.e. no side borders
					// top
					gc
							.drawLine(rect.x, rect.y, rect.x + rect.width - 1,
									rect.y);
					// bottom
					gc.drawLine(rect.x, rect.y + rect.height - 1, rect.x
							+ rect.width - 1, rect.y + rect.height - 1);
				}
			}
			// draw decoration middle line
			// int mindy = rect.y + rect.height / 2;
			// if (TraceColorScheme.GOLD_STATE == colorIdx
			// || TraceColorScheme.ORANGE_STATE == colorIdx) {
			// int s = gc.getLineStyle();
			// int w = gc.getLineWidth();
			// gc.setLineStyle(SWT.LINE_DOT);
			// gc.setLineWidth(2);
			// gc.drawLine(rect.x, mindy, rect.x + rect.width, mindy);
			// gc.setLineStyle(s);
			// gc.setLineWidth(w);
			// } else if (TraceColorScheme.RED_STATE == colorIdx
			// || TraceColorScheme.GRAY_STATE == colorIdx) {
			// int w = gc.getLineWidth();
			// gc.setLineWidth(2);
			// gc.drawLine(rect.x, mindy, rect.x + rect.width, mindy);
			// gc.setLineWidth(w);
			// }
			// // draw selection bounds
			// if (timeSelected) {
			// gc.setForeground(colors
			// .getColor(TraceColorScheme.SELECTED_TIME));
			// if (rect.width >= 3) {
			// gc.drawRectangle(rect.x, rect.y, rect.width - 1,
			// rect.height - 1);
			// // gc.drawRectangle(rect.x + 1, rect.y + 1, rect.width - 3,
			// // rect.height - 3);
			// } else {
			// gc
			// .drawLine(rect.x, rect.y, rect.x + rect.width - 1,
			// rect.y);
			// gc.drawLine(rect.x, rect.y + rect.height - 1, rect.x
			// + rect.width - 1, rect.y + rect.height - 1);
			// }
			// gc.drawLine(rect.x, rect.y + 1, rect.x + rect.width - 1,
			// rect.y + 1);
			// gc.drawLine(rect.x, rect.y + rect.height - 2, rect.x
			// + rect.width - 1, rect.y + rect.height - 2);
			// }
		} else {
			// selected rectangle area is not visible but can be represented
			// with a broken vertical line of specified width.
			int width = 2;
			rect.width = width;
			// check if height is greater than zero.
			if (rect.isEmpty())
				return;
			// colorIdx1 = TraceColorScheme.BLACK;
			gc.setForeground(colors.getColor(colorIdx));
			int s = gc.getLineStyle();
			int w = gc.getLineWidth();
			gc.setLineStyle(SWT.LINE_DOT);
			gc.setLineWidth(width);
			// Trace.debug("Reactangle not visible, drawing vertical line with: "
			// + rect.x + "," + rect.y + "," + rect.x + "," + rect.y
			// + rect.height);
			gc.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height);
			gc.setLineStyle(s);
			gc.setLineWidth(w);
		}
	}

	/**
	 * Uses the abstract method getEventcolor to obtain an enum value and
	 * convert it to an internal color index
	 * 
	 * @param event
	 * @return the internal color index
	 */
	public int getEventColorVal(ITimeEvent event) {
		StateColor colors = getEventColor(event);
		if (colors == StateColor.GREEN) {
			return TraceColorScheme.GREEN_STATE;
		} else if (colors == StateColor.DARK_BLUE) {
			return TraceColorScheme.DARK_BLUE_STATE;
		} else if (colors == StateColor.RED) {
			return TraceColorScheme.RED_STATE;
		} else if (colors == StateColor.GOLD) {
			return TraceColorScheme.GOLD_STATE;
		} else if (colors == StateColor.ORANGE) {
			return TraceColorScheme.ORANGE_STATE;
		} else if (colors == StateColor.GRAY) {
			return TraceColorScheme.GRAY_STATE;
		} else if (colors == StateColor.DARK_GREEN) {
			return TraceColorScheme.DARK_GREEN_STATE;
		} else if (colors == StateColor.DARK_YELLOW) {
			return TraceColorScheme.DARK_YELLOW_STATE;
		} else if (colors == StateColor.MAGENTA3) {
			return TraceColorScheme.MAGENTA3_STATE;
		} else if (colors == StateColor.PURPLE1) {
			return TraceColorScheme.PURPLE1_STATE;
		} else if (colors == StateColor.PINK1) {
			return TraceColorScheme.PINK1_STATE;
		} else if (colors == StateColor.AQUAMARINE) {
			return TraceColorScheme.AQUAMARINE_STATE;
		} else if (colors == StateColor.LIGHT_BLUE) {
			return TraceColorScheme.LIGHT_BLUE_STATE;
		} else if (colors == StateColor.CADET_BLUE) {
			return TraceColorScheme.CADET_BLUE_STATE_SEL;
		} else if (colors == StateColor.OLIVE) {
			return TraceColorScheme.OLIVE_STATE;
		}

		return TraceColorScheme.BLACK_STATE;
	}

	/**
	 * Select the color for the different internal variants of events.
	 * 
	 * @param event
	 * @return the corresponding event color
	 */
	public abstract StateColor getEventColor(ITimeEvent event);

	/**
	 * This values is appended between braces to the right of Trace Name e.g.
	 * Trace And Error Log [Board 17] or for a Thread trace e.g. State Server
	 * [java.lang.Thread]
	 * 
	 * @param trace
	 * @return the trace class name
	 */
	public abstract String getTraceClassName(ITmfTimeAnalysisEntry trace);

	public String getEventName(ITimeEvent event) {
		return getEventName(event, true, false);
	}

	/**
	 * Specify a Name for the event depending on its type or state e.g. blocked,
	 * running, etc..
	 * 
	 * @param event
	 * @param upper
	 *            True return String value in Upper case
	 * @param extInfo
	 *            Verbose, add additional information if applicable
	 * @return the event name
	 */
	public abstract String getEventName(ITimeEvent event, boolean upper,
			boolean extInfo);

	public String composeTraceName(ITmfTimeAnalysisEntry trace, boolean inclState) {
		String name = trace.getName();
		String threadClass = getTraceClassName(trace);
		if (threadClass != null && threadClass.length() > 0) {
			name += " [" + threadClass + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		/*
		 * Check if this is still necessary!
		if (inclState) {
			List<TimeEvent> list = trace.getTraceEvents();
			if (null != list && list.size() > 0) {
				ITimeEvent event = (ITimeEvent) list.get(list.size() - 1);
				name += " (" + getEventName(event, false, true) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		*/
		return name;
	}

	public String composeEventName(ITimeEvent event) {
		String name = event.getEntry().getName();
		String threadClass = getTraceClassName(event.getEntry());
		if (threadClass != null && threadClass.length() > 0) {
			name += " [" + threadClass + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		name += " (" + getEventName(event, false, true) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		return name;
	}

	public abstract Map<String, String> getEventHoverToolTipInfo(
			ITimeEvent event);

	/**
	 * Provides the image icon for a given Event or Trace e.g. customize to use
	 * different icons according to specific event /state combination
	 * 
	 * @param obj
	 * @return the image icon
	 */
	public Image getItemImage(Object obj) {
	    /*
		if (obj instanceof ITmfTimeAnalysisEntry) {
			List<TimeEvent> list = ((ITmfTimeAnalysisEntry) obj).getTraceEvents();
			if (null != list && list.size() > 0)
				obj = list.get(list.size() - 1);
			else if (((ITmfTimeAnalysisEntry) obj).getStopTime() > 0)
				return getImage(IMG_THREAD_STOPPED);
			else
				return getImage(IMG_THREAD_RUNNING);
		}
		if (obj instanceof TimeEvent) {
			return getImage(IMG_THREAD_RUNNING);
		}
		*/
		return null;
	}

	public abstract String getStateName(StateColor color);

}