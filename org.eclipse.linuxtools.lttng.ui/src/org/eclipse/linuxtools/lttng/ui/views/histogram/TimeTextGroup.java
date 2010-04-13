/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.histogram;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class TimeTextGroup implements FocusListener, KeyListener {
	
    private static final String 	NANOSEC_LABEL = "sec";
    private static final String		LONGEST_STRING_VALUE = "." + Long.MAX_VALUE;
    private static final Integer	MAX_CHAR_IN_TEXTBOX = LONGEST_STRING_VALUE.length();
    
    // The "small font" height used to display time will be "default font" minus this constant
    private static final Integer SMALL_FONT_MODIFIER = 1;
    
    private HistogramView parentView = null;
    private AsyncTimeTextGroupRedrawer asyncRedrawer = null;
    
    private Group 	grpName 	= null;
    private Text 	txtNanosec 	= null;
    private Label 	lblNanosec 	= null;
    
    private Long 	timeValue 	= 0L; 
    
    public TimeTextGroup(HistogramView newParentView, Composite parent, int textStyle, int groupStyle) {
    	this(newParentView, parent, textStyle, groupStyle, "", HistogramConstant.formatNanoSecondsTime(0L));
    }
    
    public TimeTextGroup(HistogramView newParentView, Composite parent, int textStyle, int groupStyle, String groupValue, String textValue) {
    	Font font = parent.getFont();
		FontData tmpFontData = font.getFontData()[0];
		Font smallFont = new Font(font.getDevice(), tmpFontData.getName(), tmpFontData.getHeight()-SMALL_FONT_MODIFIER, tmpFontData.getStyle());
    	
		parentView = newParentView;
		
		GridLayout gridLayoutgroup = new GridLayout(2, false);
		gridLayoutgroup.horizontalSpacing = 0;
		gridLayoutgroup.verticalSpacing = 0;
        grpName = new Group(parent, groupStyle);
        grpName.setText(groupValue);
        grpName.setFont(smallFont);
        grpName.setLayout(gridLayoutgroup);
        
        int textBoxSize = HistogramConstant.getTextSizeInControl(parent, LONGEST_STRING_VALUE);
        txtNanosec = new Text(grpName, textStyle);
        txtNanosec.setTextLimit( MAX_CHAR_IN_TEXTBOX );
        txtNanosec.setText( textValue );
        txtNanosec.setFont(smallFont);
        GridData gridDataTextBox = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gridDataTextBox.horizontalIndent = 10;
        gridDataTextBox.verticalIndent = 0;
        gridDataTextBox.widthHint = textBoxSize;
        gridDataTextBox.minimumWidth = textBoxSize;
        txtNanosec.setLayoutData(gridDataTextBox);
        
        int labelSize = HistogramConstant.getTextSizeInControl(parent, NANOSEC_LABEL);
        lblNanosec = new Label(grpName, SWT.LEFT);
        lblNanosec.setText(NANOSEC_LABEL);
        lblNanosec.setFont(smallFont);
        GridData gridDataLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gridDataLabel.widthHint = labelSize;
        gridDataLabel.minimumWidth = labelSize;
        gridDataLabel.horizontalIndent = 10;
        gridDataLabel.verticalIndent = 0;
        lblNanosec.setLayoutData(gridDataLabel);
        
        addNeededListeners();
    }
    
    public void addNeededListeners() {
    	
    	// AsyncCanvasRedrawer is an internal class
		// This is used to redraw the canvas without danger from a different thread
		asyncRedrawer = new AsyncTimeTextGroupRedrawer(this);
    	
    	txtNanosec.addFocusListener(this);
    	txtNanosec.addKeyListener(this);
    }
    
    
    public void setLayoutData(Object layoutData) {
    	grpName.setLayoutData(layoutData);
    } 
    
    public Composite getParent() {
    	return grpName.getParent();
    }
    
    public void setParent(Composite newParent) {
    	grpName.setParent(newParent);
    	txtNanosec.setParent(newParent);
    	lblNanosec.setParent(newParent);
    }
    
    public Long getValue() {
    	return timeValue;
    }
    
    
    public void setValueAsynchronously(String newTimeAsString) {
    	Long timeAsLong = HistogramConstant.convertStringToNanoseconds(newTimeAsString);
    	setValueAsynchronously( timeAsLong );
    }
    
    public void setValueAsynchronously(Long newTime) {
    	// Set the correct value ASAP
    	timeValue = newTime;
    	
    	// Create a new redrawer in case it doesn't exist yet (we never know with thread!)
		if ( asyncRedrawer == null ) {
			asyncRedrawer = new AsyncTimeTextGroupRedrawer(this);
		}
		
		asyncRedrawer.asynchronousSetValue(newTime);
    }
    
    
    public void setValue(String newTimeAsString) {
    	Long timeAsLong = HistogramConstant.convertStringToNanoseconds(newTimeAsString);
    	setValue( timeAsLong );
    }
    
    public void setValue(Long newTime) {
    	timeValue = newTime;
    	txtNanosec.setText( HistogramConstant.formatNanoSecondsTime(newTime) );
    }
    
    public void handleNewStringValue() {
    	String valueInText = txtNanosec.getText();
		Long valueAsLong = HistogramConstant.convertStringToNanoseconds(valueInText);
		
		if ( getValue() != valueAsLong ) {
			setValue(valueAsLong);
			notifyParentUpdatedTextGroupValue();
		}
    }
    
    public void notifyParentUpdatedTextGroupValue() {
    	parentView.timeTextGroupChangeNotification();
    }
    
	public void focusGained(FocusEvent event) {
		// Nothing to do yet
	}
	
	public void focusLost(FocusEvent event) {
		handleNewStringValue();
	}
	
	public void keyPressed(KeyEvent event) {
		switch (event.keyCode) {
			// SWT.CR is "ENTER" Key
			case SWT.CR:
				handleNewStringValue();
				break;
			default:
				break;
		}
	}

	public void keyReleased(KeyEvent e) {
		
	}
	
	/**
	 * Method to call the "Asynchronous redrawer" for this time text group<p>
	 * This allow safe redraw from different threads.
	 * 
	 */
	public void redrawAsynchronously() {
		// Create a new redrawer in case it doesn't exist yet (we never know with thread!)
		if ( asyncRedrawer == null ) {
			asyncRedrawer = new AsyncTimeTextGroupRedrawer(this);
		}
		
		asyncRedrawer.asynchronousRedraw();
	}
	
	public void redraw () {
		grpName.redraw();
    	txtNanosec.redraw();
    	lblNanosec.redraw();
	}
}

/**
 * <b><u>AsyncTimeTextGroupRedrawer Inner Class</u></b>
 * <p>
 * Asynchronous redrawer for the TimeTextGroup
 * <p>
 * This class role is to call method that update the UI on asynchronously. 
 * This should prevent any "invalid thread access" exception when trying to update UI from a different thread.
 */
class AsyncTimeTextGroupRedrawer {
	
	private TimeTextGroup parentTimeTextGroup = null; 
	
	/**
	 * AsyncTimeTextGroupRedrawer constructor.
	 * 
	 * @param newParent 	Related time text group.
	 */
	public AsyncTimeTextGroupRedrawer(TimeTextGroup newParent) {
		parentTimeTextGroup = newParent;
	}
	
	/**
	 * Ssynchronous SetValue for time text group.
	 * 
	 * Basically, it just run "getParent().setValue(time)" in asyncExec.
	 * 
	 * @param newTime 	The new time to set
	 */
	public void asynchronousSetValue(Long newTime) {
		final long tmpTime = newTime;
		
		Display display =  parentTimeTextGroup.getParent().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				parentTimeTextGroup.setValue(tmpTime);
			}
		});
	}
	
	/**
	 * Function to redraw the related time text group asynchonously.<p>
	 * 
	 * Basically, it just run "getParent().redraw()" in asyncExec.
	 * 
	 */
	public void asynchronousRedraw() {
		Display display =  parentTimeTextGroup.getParent().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				parentTimeTextGroup.getParent().redraw();
			}
		});
	}
}