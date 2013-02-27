/*****************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation, Ericsson
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

    /** Default left margin size */
    public static final int MARGIN = 4;

    /** Default expanded size */
    public static final int EXPAND_SIZE = 9; // the [+] or [-] control size

    /** Default size of the right margin */
    public static final int RIGHT_MARGIN = 1; // 1 pixels less to make sure end time is visible

    /** Default size for small icons */
    public static final int SMALL_ICON_SIZE = 16;

    /** Color scheme */
    protected TimeGraphColorScheme _colors;

    /** Font size */
    protected int _fontHeight = 0;

    /**
     * Basic constructor. Uses a default style value
     *
     * @param parent
     *            The parent composite object
     * @param colors
     *            The color scheme to use
     */
    public TimeGraphBaseControl(Composite parent, TimeGraphColorScheme colors) {
        this(parent, colors, SWT.NO_BACKGROUND | SWT.NO_FOCUS);
    }

    /**
     * Standard constructor
     *
     * @param parent
     *            The parent composite object
     * @param colors
     *            The color scheme to use
     * @param style
     *            The index of the style to use
     */
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

    /**
     * Retrieve the current font's height
     *
     * @return The height
     */
    public int getFontHeight() {
        return _fontHeight;
    }

    abstract void paint(Rectangle bound, PaintEvent e);
}
