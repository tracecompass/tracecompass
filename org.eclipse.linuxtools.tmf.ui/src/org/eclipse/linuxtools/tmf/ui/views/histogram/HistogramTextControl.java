/*******************************************************************************
 * Copyright (c) 2009, 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Wiliam Bourque - Adapted from SpinnerGroup (in TimeFrameView)
 *   Francois Chouinard - Cleanup and refactoring
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>HistogramTextControl</u></b>
 * <p>
 * This control provides a group containing a text control.
 */
public abstract class HistogramTextControl implements FocusListener, KeyListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    protected final HistogramView fParentView;
    private final Composite fParent;

    // Controls
    private final Group fGroup;
    protected final Text fTextValue;
    private long fValue;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public HistogramTextControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle) {
        this(parentView, parent, textStyle, groupStyle, "", HistogramUtils.nanosecondsToString(0L)); //$NON-NLS-1$
    }

    public HistogramTextControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle, String groupValue, String textValue) {

        fParentView = parentView;
        fParent = parent;

        // --------------------------------------------------------------------
        // Reduce font size for a more pleasing rendering
        // --------------------------------------------------------------------

        final int fontSizeAdjustment = -1;
        final Font font = parent.getFont();
        final FontData fontData = font.getFontData()[0];
        final Font adjustedFont = new Font(font.getDevice(), fontData.getName(), fontData.getHeight() + fontSizeAdjustment, fontData.getStyle());

        // --------------------------------------------------------------------
        // Pre-compute the size of the control
        // --------------------------------------------------------------------

        final String longestStringValue = "." + Long.MAX_VALUE; //$NON-NLS-1$
        final int maxChars = longestStringValue.length();
        final int textBoxSize = HistogramUtils.getTextSizeInControl(parent, longestStringValue);

        // --------------------------------------------------------------------
        // Create the group
        // --------------------------------------------------------------------

        // Re-used layout variables
        GridLayout gridLayout;
        GridData gridData;

        // Group control
        gridLayout = new GridLayout(1, false);
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        fGroup = new Group(fParent, groupStyle);
        fGroup.setText(groupValue);
        fGroup.setFont(adjustedFont);
        fGroup.setLayout(gridLayout);

        // Group control
        gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gridData.horizontalIndent = 0;
        gridData.verticalIndent = 0;
        gridData.minimumWidth = textBoxSize;
        fTextValue = new Text(fGroup, textStyle);
        fTextValue.setTextLimit(maxChars);
        fTextValue.setText(textValue);
        fTextValue.setFont(adjustedFont);
        fTextValue.setLayoutData(gridData);

        // --------------------------------------------------------------------
        // Add listeners
        // --------------------------------------------------------------------

        fTextValue.addFocusListener(this);
        fTextValue.addKeyListener(this);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    // State
    public boolean isDisposed() {
        return fGroup.isDisposed();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    protected abstract void updateValue();

    // LayoutData
    public void setLayoutData(GridData layoutData) {
        fGroup.setLayoutData(layoutData);
    }

    // Time value
    public void setValue(String timeString) {
        long timeValue = HistogramUtils.stringToNanoseconds(timeString);
        setValue(timeValue);
    }

    public void setValue(final long time) {
        // If this is the UI thread, process now
        Display display = Display.getCurrent();
        if (display != null) {
            fValue = time;
            fTextValue.setText(HistogramUtils.nanosecondsToString(time));
            return;
        }

        // Call "recursively" from the UI thread
        if (!isDisposed()) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!isDisposed()) {
                        setValue(time);
                    }
                }
            });
        }
    }

    public long getValue() {
        return fValue;
    }

    // ------------------------------------------------------------------------
    // FocusListener
    // ------------------------------------------------------------------------

    @Override
    public void focusGained(FocusEvent event) {
    }

    @Override
    public void focusLost(FocusEvent event) {
        updateValue();
    }

    // ------------------------------------------------------------------------
    // KeyListener
    // ------------------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.keyCode) {
            case SWT.CR:
                updateValue();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
