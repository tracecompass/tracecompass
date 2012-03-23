/*****************************************************************************
 * Copyright (c) 2007 Intel Corporation.
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
 * $Id: PlainSelection.java,v 1.1 2007/04/20 13:06:49 ewchan Exp $ 
 *****************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.viewers.timeAnalysis.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

public class PlainSelection implements IStructuredSelection {

	List<Object> list = new ArrayList<Object>();

	public PlainSelection() {
	}

	public PlainSelection(Object sel) {
		add(sel);
	}

	public void add(Object sel) {
		if (null != sel && !list.contains(sel))
			list.add(sel);
	}

	@Override
	public Object getFirstElement() {
		if (!list.isEmpty())
			return list.get(0);
		return null;
	}

	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public List<Object> toList() {
		return list;
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}
}
