/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
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
 *   Francois Chouinard - Better handling of control display, support for signals
 *   Patrick Tasse - Update for mouse wheel zoom
  *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This control provides a group containing a text control.
 *
 * @version 1.1
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

    // Controls data
    private final Composite fParent;
    private Font fFont;
    private final Composite fComposite;
    private final Label fLabel;

    /**
     * The text value field.
     */
    protected final Text fTextValue;

    private long fValue;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

   /**
    * Constructor with given group and text values.
    *
    * @param parentView The parent histogram view.
    * @param parent The parent composite
    * @param label The text label
    * @param value The initial value
    * @since 2.0
    */
    public HistogramTextControl(HistogramView parentView, Composite parent, String label, long value)
    {
        fParentView = parentView;
        fParent = parent;

        // --------------------------------------------------------------------
        // Reduce font size for a more pleasing rendering
        // --------------------------------------------------------------------

        final int fontSizeAdjustment = -1;
        final Font font = parent.getFont();
        final FontData fontData = font.getFontData()[0];
        fFont = new Font(font.getDevice(), fontData.getName(), fontData.getHeight() + fontSizeAdjustment, fontData.getStyle());

        // --------------------------------------------------------------------
        // Create the group
        // --------------------------------------------------------------------

        // Re-used layout variables
        GridLayout gridLayout;
        GridData gridData;

        // Composite
        gridLayout = new GridLayout(3, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        fComposite = new Composite(fParent, SWT.NONE);
        fComposite.setLayout(gridLayout);

        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        Label filler = new Label(fComposite, SWT.NONE);
        filler.setLayoutData(gridData);

        // Label
        gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        fLabel = new Label(fComposite, SWT.NONE);
        fLabel.setText(label);
        fLabel.setFont(fFont);

        // Text control
        gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        fTextValue = new Text(fComposite, SWT.BORDER);
        fTextValue.setFont(fFont);
        fTextValue.setLayoutData(gridData);

        // --------------------------------------------------------------------
        // Add listeners
        // --------------------------------------------------------------------

        fTextValue.addFocusListener(this);
        fTextValue.addKeyListener(this);

        TmfSignalManager.register(this);
    }

    /**
     * Dispose of the widget
     * @since 2.0
     */
    public void dispose() {
        fFont.dispose();
        TmfSignalManager.deregister(this);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns if widget isDisposed or not
     * @return <code>true</code> if widget is disposed else <code>false</code>
     */
    public boolean isDisposed() {
        return fComposite.isDisposed();
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
        fComposite.setLayoutData(layoutData);
    }

    /**
     * Enables the receiver if the argument is <code>true</code>,
     * and disables it otherwise. A disabled control is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     *
     * @param enabled the new enabled state
     * @since 2.2
     */
    public void setEnabled(boolean enabled) {
        fTextValue.setEnabled(enabled);
    }

    /**
     * The time value in to set in the text field.
     *
     * @param time the time to set
     * @param displayTime the display value
     * @since 2.0
     */
    protected void setValue(final long time, final String displayTime) {
        // If this is the UI thread, process now
        Display display = Display.getCurrent();
        if (display != null) {
            if (!isDisposed()) {
                fValue = time;
                fTextValue.setText(displayTime);
                fComposite.layout();
                fParent.getParent().layout();
            }
            return;
        }

        // Call self from the UI thread
        if (!isDisposed()) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!isDisposed()) {
                        setValue(time, displayTime);
                    }
                }
            });
        }
    }

    /**
     * @param time the time value to display
     */
    public abstract void setValue(long time);

    /**
     * Returns the time value.
     * @return time value.
     */
    public long getValue() {
        return fValue;
    }

    /**
     * Add a mouse wheel listener to the text control
     * @param listener the mouse wheel listener
     * @since 2.0
     */
    public void addMouseWheelListener(MouseWheelListener listener) {
        fTextValue.addMouseWheelListener(listener);
    }

    /**
     * Remove a mouse wheel listener from the text control
     * @param listener the mouse wheel listener
     * @since 2.0
     */
    public void removeMouseWheelListener(MouseWheelListener listener) {
        fTextValue.removeMouseWheelListener(listener);
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
        switch (event.character) {
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
