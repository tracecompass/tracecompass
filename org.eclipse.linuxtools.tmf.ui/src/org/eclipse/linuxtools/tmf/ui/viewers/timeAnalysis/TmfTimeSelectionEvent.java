/*******************************************************************************
 * Copyright (c) 2009 Ericsson
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

import java.util.EventObject;

public class TmfTimeSelectionEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum Type {WIDGET_DEF_SEL, WIDGET_SEL}
	Type dType;
	Object selection = null;
	long selTime = 0;
	int index = 0;
	
	public TmfTimeSelectionEvent(Object arg0, Type rType, Object sel, long selectedTime) {
		super(arg0);
		dType = rType;
		selection = sel;
		selTime = selectedTime;
	}
	
	public Type getDType() {
		return dType;
	}
	
	public Object getSelection() {
		return selection;
	}
	
	public long getSelectedTime() {
		return selTime;
	}

}
