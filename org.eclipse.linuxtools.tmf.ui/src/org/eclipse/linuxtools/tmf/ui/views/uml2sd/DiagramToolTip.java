/**********************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <p>
 * This class is used to reproduce the same tooltip behavior on Windows and Linux when the mouse hovers over the
 * sequence diagram
 * </p>
 *
 * @version 1.0
 * @author sveyrier
 */
public class DiagramToolTip {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The parent control where the tooltip must be drawn.
     */
    protected Control fParent = null;
    /**
     * The tooltip shell.
     */
    protected Shell fToolTipShell = null;
    /**
     * The tooltip text.
     */
    protected String fText = null;
    /**
     * The text box.
     */
    protected Text fTextBox = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Create a new tooltip for the given parent control
     *
     * @param parent the parent control.
     */
    public DiagramToolTip(Control parent) {
        fParent = parent;
        fToolTipShell = new Shell(fParent.getShell(), SWT.MULTI);
        fToolTipShell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        fTextBox = new Text(fToolTipShell, SWT.WRAP | SWT.MULTI);
        fTextBox.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Display the tooltip using the given text The tooltip will stay on screen until it is told otherwise
     *
     * @param value the text to display
     */
    public void showToolTip(String value) {
        if ((value == null) || (value.equalsIgnoreCase(""))) { //$NON-NLS-1$
            fToolTipShell.setVisible(false);
            return;
        }

        fText = value;
        int w = fToolTipShell.getBounds().width;
        Point hr = Display.getDefault().getCursorLocation();
        int cursorH = 32;
        for (int i = 0; i < Display.getDefault().getCursorSizes().length; i++) {
            if (Display.getDefault().getCursorSizes()[i].y < cursorH) {
                cursorH = Display.getDefault().getCursorSizes()[i].y;
            }
        }
        if (hr.x + w > Display.getDefault().getBounds().width) {
            int tempX = (hr.x + w) - Display.getDefault().getBounds().width;
            if (tempX > Display.getDefault().getBounds().width) {
                hr.x = 0;
            }
            hr.x = hr.x - tempX;
        }
        fTextBox.setText(value);
        int charactersPerColumn = 100;
        GC gc = new GC(fTextBox);
        FontMetrics fm = gc.getFontMetrics();
        gc.dispose();
        int width = charactersPerColumn * fm.getAverageCharWidth();
        fTextBox.setSize(fTextBox.computeSize(width, fTextBox.getLineCount() * fTextBox.getLineHeight()));
        fToolTipShell.setLocation(hr.x, hr.y + cursorH);
        fToolTipShell.setSize(fTextBox.getSize());
        fTextBox.setVisible(true);
        fToolTipShell.setVisible(true);

    }

    /**
     * Hide the tooltip
     */
    public void hideToolTip() {
        fToolTipShell.setVisible(false);
    }

}
