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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class NanosecTextGroup {
	
    private static final String 	NANOSEC_LABEL = "sec";
    private static final String		LONGEST_STRING_VALUE = "." + Long.MAX_VALUE;
    private static final Integer	MAX_CHAR_IN_TEXTBOX = LONGEST_STRING_VALUE.length();
    
    private Group 	grpName 	= null;
    private Text 	txtNanosec 	= null;
    private Label 	lblNanosec 	= null;
    
    private Long timeValue = 0L; 
    
    public NanosecTextGroup(Composite parent, int textStyle, int groupStyle) {
    	this(parent, textStyle, groupStyle, "", HistogramConstant.formatNanoSecondsTime(0L));
    }
    
    public NanosecTextGroup(Composite parent, int textStyle, int groupStyle, String groupValue, String textValue) {
    	Font font = parent.getFont();
		FontData tmpFontData = font.getFontData()[0];
		//Font smallFont = new Font(font.getDevice(), tmpFontData.getName(), tmpFontData.getHeight()-1, tmpFontData.getStyle());
		Font smallFont = new Font(font.getDevice(), tmpFontData.getName(), tmpFontData.getHeight(), tmpFontData.getStyle());
    	
		
        grpName = new Group(parent, groupStyle);
        grpName.setText(groupValue);
        grpName.setFont(smallFont);
        grpName.setLayout(new GridLayout(2, false));
        
        txtNanosec = new Text(grpName, textStyle);
        txtNanosec.setTextLimit( MAX_CHAR_IN_TEXTBOX );
        
        int textBoxSize = HistogramConstant.getTextSizeInControl(parent, LONGEST_STRING_VALUE);
        
        txtNanosec.setText( textValue );
        txtNanosec.setOrientation(SWT.RIGHT_TO_LEFT);
        GridData gridDataTextBox = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridDataTextBox.widthHint = textBoxSize;
        gridDataTextBox.minimumWidth = textBoxSize;
        txtNanosec.setFont(smallFont);
        txtNanosec.setLayoutData(gridDataTextBox);
        
        lblNanosec = new Label(grpName, SWT.LEFT);
        lblNanosec.setText(NANOSEC_LABEL);
        lblNanosec.setFont(smallFont);
        lblNanosec.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
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
    
    
    public void setValue(String newTimeAsString) {
    	
    	Long timeAsLong = convertStringToLong(newTimeAsString);
    	setValue( timeAsLong );
    }
    
    public void setValue(Long newTime) {
    	timeValue = newTime;
    	txtNanosec.setText( HistogramConstant.formatNanoSecondsTime(newTime) );
    }
	
    public Long convertStringToLong( String timeString ) {
		
    	Long returnedNumber = 0L;
		
	    try {
	        // Avoid simple commat/dot mistake
	        timeString = timeString.replace(",", ".");
	
	        // If we have a dot, we have a decimal number to convert
	        int dotPosition = timeString.indexOf(".");
	        System.out.println("Dot pos : " + dotPosition);
	
	        // If the user begun the line with a dot, we add a zero
	        if ( dotPosition == 0 ) {
                timeString = "0" + timeString;
                dotPosition = 1;
	        }
	
	        if ( dotPosition != -1 ) {
                int decimalNumber = (timeString.length() - dotPosition -1);

                System.out.println("Decimal nb : " + decimalNumber);

                if ( decimalNumber < 9 ) {
                    for ( int nbDec=decimalNumber; nbDec<9; nbDec++) {
                        timeString += "0";
                    }
                }
	        }
	
	        System.out.println("Final string number : " + timeString);
	
	        // Conversion into decimal seconds
	        Double dblMaxTimerange = Double.parseDouble(timeString);
	        // Conversion into nanoseconds
	        returnedNumber = (long)(dblMaxTimerange * 1000000000.0);
	
	        System.out.println("Final long number : " + returnedNumber);
	    }
	    catch (NumberFormatException e) {
	        System.out.println("Warning : Could not convert string into nanoseconds (convertStringToLong)");
	    }
	    
	    return returnedNumber;
    }
    
}