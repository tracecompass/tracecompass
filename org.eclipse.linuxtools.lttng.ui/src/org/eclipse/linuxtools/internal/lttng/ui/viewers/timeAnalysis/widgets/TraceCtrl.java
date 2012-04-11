/*****************************************************************************
 * Copyright (c) 2007, Intel Corporation.
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
 * $Id: TraceCtrl.java,v 1.2 2007/02/27 18:37:36 ewchan Exp $ 
 *****************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public abstract class TraceCtrl extends Canvas implements PaintListener {

	static public final int MARGIN = 4;
    static public final int RIGHT_MARGIN = 2; // 2 pixels less to make sure end time is visible
	static public final int SMALL_ICON_SIZE = 16;

	protected TraceColorScheme _colors;
	protected int _fontHeight = 0;

	public TraceCtrl(Composite parent, TraceColorScheme colors) {
		this(parent, colors, SWT.NO_BACKGROUND | SWT.NO_FOCUS);
	}

	public TraceCtrl(Composite parent, TraceColorScheme colors, int style) {
		super(parent, style);
		_colors = colors;
		addPaintListener(this);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void paintControl(PaintEvent e) {
		if (e.widget != this)
			return;
		_fontHeight = e.gc.getFontMetrics().getHeight();
		Rectangle bound = getClientArea();
		if (!bound.isEmpty()) {
			Color colBackup = e.gc.getBackground();
			paint(bound, e);
			e.gc.setBackground(colBackup);
		}
	}

	public int getFontHeight() {
		return _fontHeight;
	}

	abstract void paint(Rectangle bound, PaintEvent e);
}
