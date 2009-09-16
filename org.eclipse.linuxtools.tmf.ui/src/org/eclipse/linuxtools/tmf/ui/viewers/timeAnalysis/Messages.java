/********************************************************************** 
 * Copyright (c) 2005, 2008, 2009  IBM Corporation, Intel Corporation, Ericsson
 * 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: UIMessages.java,v 1.18 2008/06/03 16:53:41 aalexeev Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 * Alvaro Sanchez-Leon - Stripped down for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis;

import org.eclipse.osgi.util.NLS;

public class Messages {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.messages"; //$NON-NLS-1$
	public static String TRACE_STATES_TITLE;

	public static String _TRACE_ID;
	public static String _TRACE_NAME;
	public static String _TRACE_CLASS_NAME;
	public static String _TRACE_GROUP_NAME;
	public static String _TRACE_START_TIME;
	public static String _TRACE_DATE;
	public static String _TRACE_STOP_TIME;
	public static String _TRACE_STATE;
	public static String _NUMBER_OF_TRACES;
	public static String _TRACE_FILTER;
	public static String _TRACE_FILTER_DESC;
	// misc
	public static String _Timescale;
	public static String _DURATION;
	public static String _UNDEFINED_GROUP;
	public static String _TRACE_GROUP_LABEL;
	public static String _EDIT_PROFILING_OPTIONS;

	public static String _LEGEND;
	public static String _TRACE_STATES;
	public static String _WINDOW_TITLE;

	private Messages() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
