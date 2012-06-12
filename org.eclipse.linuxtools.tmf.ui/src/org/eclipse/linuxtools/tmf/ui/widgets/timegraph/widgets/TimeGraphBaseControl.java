/*****************************************************************************
 * Copyright (c) 2007 Intel Corporation, 2009, 2012 Ericsson.
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
 *
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * Base control abstract class for the time graph widget
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public abstract class TimeGraphBaseControl extends Canvas implements PaintListener {

    static public final int MARGIN = 4;
    static public final int EXPAND_SIZE = 9; // the [+] or [-] control size
    static public final int RIGHT_MARGIN = 1; // 1 pixels less to make sure end time is visible
    static public final int SMALL_ICON_SIZE = 16;

    protected TimeGraphColorScheme _colors;
    protected int _fontHeight = 0;

    public TimeGraphBaseControl(Composite parent, TimeGraphColorScheme colors) {
        this(parent, colors, SWT.NO_BACKGROUND | SWT.NO_FOCUS);
    }

    public TimeGraphBaseControl(Composite parent, TimeGraphColorScheme colors, int style) {
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
        if (e.widget != this) {
            return;
        }
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
