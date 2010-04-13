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

/**
 * <b><u>TimeTextGroup</u></b>
 * <p>
 * Special control for HistogramView
 * <p>
 * This control will give you a group, a text box and a label at once.
 */
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
    
    /**
     * Default Constructor.<p>
     * 
     * @param newParentView		Parent HistogramView
     * @param parent			Parent Composite, used to position the inner controls.
     * @param textStyle			Style of the textbox. Usually SWT.BORDER or SWT.NONE (or anything that suit a Text)
     * @param groupStyle		Style of the group.   Anything that suite a Text
     */
    public TimeTextGroup(HistogramView newParentView, Composite parent, int textStyle, int groupStyle) {
    	this(newParentView, parent, textStyle, groupStyle, "", HistogramConstant.formatNanoSecondsTime(0L));
    }
    
    /**
     * Default Constructor, allow you to give the groupname and the textbox value.<p>
     * 
     * @param newParentView		Parent HistogramView
     * @param parent			Parent Composite, used to position the inner controls.
     * @param textStyle			Style of the textbox. Usually SWT.BORDER or SWT.NONE (or anything that suit a Text)
     * @param groupStyle		Style of the group.   Anything that suite a Text
     * @param groupValue		Value (label) of the group. 
     * @param textValue         Value of the textbox.
     */
    public TimeTextGroup(HistogramView newParentView, Composite parent, int textStyle, int groupStyle, String groupValue, String textValue) {
    	Font font = parent.getFont();
		FontData tmpFontData = font.getFontData()[0];
		// We will use a slightly smaller font than the default one to show, we calculate it here
		Font smallFont = new Font(font.getDevice(), tmpFontData.getName(), tmpFontData.getHeight()-SMALL_FONT_MODIFIER, tmpFontData.getStyle());
    	
		parentView = newParentView;
		
		// ** Creation of the group
		GridLayout gridLayoutgroup = new GridLayout(2, false);
		gridLayoutgroup.horizontalSpacing = 0;
		gridLayoutgroup.verticalSpacing = 0;
        grpName = new Group(parent, groupStyle);
        grpName.setText(groupValue);
        grpName.setFont(smallFont);
        grpName.setLayout(gridLayoutgroup);
        
        // ** Creation of the textbox
        // Calculate the optimal size of the textbox already
        // This will avoid the control to move around and resize when bigger value are given 
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
        
        // ** Creation of the label
        lblNanosec = new Label(grpName, SWT.LEFT);
        lblNanosec.setText(NANOSEC_LABEL);
        lblNanosec.setFont(smallFont);
        GridData gridDataLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gridDataLabel.horizontalIndent = 10;
        gridDataLabel.verticalIndent = 0;
        lblNanosec.setLayoutData(gridDataLabel);
        
        // Add all listener
        addNeededListeners();
    }
    
    /*
     * Create and add all listeners needed by our control.<p>
     */
    private void addNeededListeners() {
    	
    	// AsyncCanvasRedrawer is an internal class
		// This is used to redraw the canvas without danger from a different thread
		asyncRedrawer = new AsyncTimeTextGroupRedrawer(this);
    	
    	txtNanosec.addFocusListener(this);
    	txtNanosec.addKeyListener(this);
    }
    
    /**
     * Getter for the layout data currently in use.<p>
     * 
     * @return the layout
     */
    public Object getLayoutData() {
    	return grpName.getLayoutData();
    }
    
    /**
     * Set a new layoutData for our control.<p>
     * 
     * @param layoutData	the new layout data
     */
    public void setLayoutData(Object layoutData) {
    	grpName.setLayoutData(layoutData);
    } 
    
    /**
     * Get the control's parent.<p>
     * 
     * @return	Currently used parent
     */
    public Composite getParent() {
    	return grpName.getParent();
    }
    
    /**
     * Set a new parent for the control.<p>
     * 
     * @return	Currently used parent
     */
    public void setParent(Composite newParent) {
    	grpName.setParent(newParent);
    	txtNanosec.setParent(newParent);
    	lblNanosec.setParent(newParent);
    }
    
    /**
     * Getter for the time value of the control.<p>
     * 
     * @return	The nanoseconds time value
     */
    public Long getValue() {
    	return timeValue;
    }
    
    /**
     * Set a new String value to the control.<p>
     * Note : The String value will be converted in Long before being applied;
     * 			if any conversion error occur, 0 will be used. <p>
     * 
     * @param newTimeAsString	The value to convert and set.
     */
    public void setValue(String newTimeAsString) {
    	Long timeAsLong = HistogramConstant.convertStringToNanoseconds(newTimeAsString);
    	setValue( timeAsLong );
    }
    
    /**
     * Set a new value to the control.<p>
     * Note : The value will be formatted as nanosecond value, 
     * 			missing zero will be added if needed.<p>
     * 
     * @param newTime	The value to set.
     */
    public void setValue(Long newTime) {
    	timeValue = newTime;
    	txtNanosec.setText( HistogramConstant.formatNanoSecondsTime(newTime) );
    }
    
    /**
     * Set a new String value, asynchronously.<p>
     * This will call setValue(String) in async.Exec to avoid Thread Access problem to UI.<p>
     * 
     * @param newTimeAsString	The value to convert and set.
     */
    public void setValueAsynchronously(String newTimeAsString) {
    	Long timeAsLong = HistogramConstant.convertStringToNanoseconds(newTimeAsString);
    	setValueAsynchronously( timeAsLong );
    }
    
    /**
     * Set a new String value, asynchronously.<p>
     * This will call setValue(Long) in async.Exec to avoid Thread Access problem to UI.<p>
     * 
     * @param newTimeAsString	The value to set.
     */
    public void setValueAsynchronously(Long newTime) {
    	// Set the correct value ASAP
    	timeValue = newTime;
    	
    	// Create a new redrawer in case it doesn't exist yet (we never know with thread!)
		if ( asyncRedrawer == null ) {
			asyncRedrawer = new AsyncTimeTextGroupRedrawer(this);
		}
		
		asyncRedrawer.asynchronousSetValue(newTime);
    }
    
    /**
     * Set a new group name (label) for this control.<p>
     * 
     * @param newName	The new name to set.
     */
    public void setGroupName(String newName) {
    	grpName.setText(newName);
    }
    
    /**
     * Set a new group name (label) for this control, asynchronously.<p>
     * This will call setValue(Long) in async.Exec to avoid Thread Access problem to UI.<p>
     * 
     * @param newName	The new name to set.
     */
    public void setGroupNameAsynchronously(String newGroupName) {
    	// Create a new redrawer in case it doesn't exist yet (we never know with thread!)
		if ( asyncRedrawer == null ) {
			asyncRedrawer = new AsyncTimeTextGroupRedrawer(this);
		}
		
		asyncRedrawer.asynchronousSetGroupName(newGroupName);
    }
    
	
	/**
	 * Method to call the "Asynchronous redrawer" for this time text group<p>
	 * This allow safe redraw from different threads.
	 */
	public void redrawAsynchronously() {
		// Create a new redrawer in case it doesn't exist yet (we never know with thread!)
		if ( asyncRedrawer == null ) {
			asyncRedrawer = new AsyncTimeTextGroupRedrawer(this);
		}
		
		asyncRedrawer.asynchronousRedraw();
	}
	
	/**
	 * Redraw the control
	 */
	public void redraw () {
		grpName.redraw();
    	txtNanosec.redraw();
    	lblNanosec.redraw();
	}
    
    /*
     * This function is called when an user enter a new string in the control by hand.<p>
     * It will ensure the format of the String is valid.
     */
    private void handleNewStringValue() {
    	String valueInText = txtNanosec.getText();
		Long valueAsLong = HistogramConstant.convertStringToNanoseconds(valueInText);
		
		if ( getValue() != valueAsLong ) {
			setValue(valueAsLong);
			// Notify our parent that the control was updated
			notifyParentUpdatedTextGroupValue();
		}
    }
    
    /**
     * This function notify our parent HistogramView that our value changed.
     */
    public void notifyParentUpdatedTextGroupValue() {
    	parentView.timeTextGroupChangeNotification();
    }
    
    /**
	 * Function that is called when the canvas get focus.<p>
	 * 
	 * Doesn't do anything yet... 
	 * 
	 * @param event  The focus event generated.
	 */
	public void focusGained(FocusEvent event) {
		// Nothing to do yet
	}
	
	/**
	 * Function that is called when the canvas loose focus.<p>
	 * It will validate that the String entered by the user (if any) is valid.<p>
	 * 
	 * @param event  The focus event generated.
	 */
	public void focusLost(FocusEvent event) {
		handleNewStringValue();
	}
	
	/**
	 * Function that is called when a key is pressed.<p>
	 * Possible actions : 
	 * - Enter (CR) : Validate the entered String.<p>
	 * 
	 * @param event  The KeyEvent generated when the key was pressed.
	 */
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
	
	/**
	 * Function that is called when a key is released.<p>
	 * Possible actions : 
	 * 		Nothing yet
	 * 
	 * @param event  The KeyEvent generated when the key was pressed.
	 */
	public void keyReleased(KeyEvent e) {
		
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
	 * Asynchronous SetValue for time text group.
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
	 * Asynchronous SetGroupName for time text group.
	 * 
	 * Basically, it just run "getParent().setGroupName(name)" in asyncExec.
	 * 
	 * @param newGroupName 	The new group name to set
	 */
	public void asynchronousSetGroupName(String newGroupName) {
		final String tmpName = newGroupName;
		
		Display display =  parentTimeTextGroup.getParent().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				parentTimeTextGroup.setGroupName(tmpName);
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