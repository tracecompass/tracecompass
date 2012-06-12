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
 * This control provides a group containing a text control.
 * 
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class HistogramTextControl implements FocusListener, KeyListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The parent histogram view.
     */
    protected final HistogramView fParentView;
    private final Composite fParent;

    // Controls
    private final Group fGroup;
    /**
     * The text value field.
     */
    protected final Text fTextValue;
    private long fValue;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor default values
     *
     * @param parentView The parent histogram view.
     * @param parent The parent composite
     * @param textStyle The text style bits.
     * @param groupStyle The group style bits.
     * 
     */
    public HistogramTextControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle) {
        this(parentView, parent, textStyle, groupStyle, "", HistogramUtils.nanosecondsToString(0L)); //$NON-NLS-1$
    }

   /**
    * Constructor with given group and text values.
    * 
    * @param parentView The parent histogram view.
    * @param parent The parent composite
    * @param textStyle The text style bits.
    * @param groupStyle The group style bits.
    * @param groupValue A group value
    * @param textValue A text value
    */
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

    /**
     * Returns if widget isDisposed or not
     * @return <code>true</code> if widget is disposed else <code>false</code>  
     */
    public boolean isDisposed() {
        return fGroup.isDisposed();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Updates the text value.
     */
    protected abstract void updateValue();

    /**
     * Set the Grid Layout Data for the group.
     * @param layoutData A GridData to set.
     */
    public void setLayoutData(GridData layoutData) {
        fGroup.setLayoutData(layoutData);
    }

    /**
     * Sets the value converted to nano-seconds in the text field.
     * @param timeString the time string (input)
     */
    public void setValue(String timeString) {
        long timeValue = HistogramUtils.stringToNanoseconds(timeString);
        setValue(timeValue);
    }

    /**
     * The time value in nano-seconds to set in the text field. 
     * @param time the time to set
     */
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
    
    /**
     * Returns the time value.
     * @return time value.
     */
    public long getValue() {
        return fValue;
    }

    // ------------------------------------------------------------------------
    // FocusListener
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
     */
    @Override
    public void focusGained(FocusEvent event) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
     */
    @Override
    public void focusLost(FocusEvent event) {
        updateValue();
    }

    // ------------------------------------------------------------------------
    // KeyListener
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
     */
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

}
