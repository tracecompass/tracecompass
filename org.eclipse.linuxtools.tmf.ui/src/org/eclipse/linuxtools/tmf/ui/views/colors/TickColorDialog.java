/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.colors;

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TickColorDialog extends Dialog {

	int selectedIndex = 0;
	Composite colorComposite;
	
	TimeGraphColorScheme traceColorScheme = new TimeGraphColorScheme();
	private ITimeGraphProvider timeAnalysisProvider = new TimeGraphProvider() {
		@Override
        public StateColor getEventColor(ITimeEvent event) {
	        return null;
        }
		@Override
        public String getTraceClassName(ITimeGraphEntry trace) {
	        return null;
        }
		@Override
        public String getEventName(ITimeEvent event, boolean upper, boolean extInfo) {
	        return null;
        }
		@Override
        public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
	        return null;
        }
		@Override
        public String getStateName(StateColor color) {
	        return null;
        }};
	
	protected TickColorDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.MAX);
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.TickColorDialog_TickColorDialogTitle);
		//getShell().setMinimumSize(getShell().computeSize(500, 200));
        Composite composite = (Composite) super.createDialogArea(parent);
        colorComposite = new Composite(composite, SWT.NONE);
        colorComposite.setLayout(new GridLayout(4, false));
        
        for (int i = 0; i < 16; i++) {
        	TickColorCanvas tickColorCanvas = new TickColorCanvas(colorComposite, SWT.NONE);
        	tickColorCanvas.setColorIndex(i);
        }

        return composite;
    }

    public void setColorIndex(int colorIndex) {
    	selectedIndex = colorIndex;
    }
    
    public int getColorIndex() {
    	return selectedIndex;
    }
    
    private class TickColorCanvas extends Canvas {
    	int colorIndex;
    	
    	public TickColorCanvas(Composite parent, int style) {
    		super(parent, style);
    		
    		GridData gd = new GridData(SWT.CENTER, SWT.FILL, true, false);
    		gd.widthHint = 40;
    		gd.heightHint = 25;
    		setLayoutData(gd);
    		setBackground(traceColorScheme.getBkColor(false, false, false));
    		
    		addPaintListener(new PaintListener() {
    			@Override
    			public void paintControl(PaintEvent e) {
    				e.gc.setForeground(traceColorScheme.getColor(TimeGraphColorScheme.MID_LINE));
    				int midy = e.y + e.height / 2;
    				e.gc.drawLine(e.x, midy, e.x + e.width, midy);
    				int midx = e.x + e.width / 2;
    				Rectangle rect = new Rectangle(midx - 10, e.y + 3, 0, e.height - 6);
    				for (int i = 1; i <= 3; i++) {
    					rect.x += i;
    					rect.width = i;
    					timeAnalysisProvider.drawState(traceColorScheme, colorIndex, rect, e.gc, false, false, false);
    				}
    				for (int i = 3; i > 0; i--) {
    					rect.x += i + 2;
    					rect.width = i;
    					timeAnalysisProvider.drawState(traceColorScheme, colorIndex, rect, e.gc, false, false, false);
    				}
					if (selectedIndex == colorIndex) {
						Color borderColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
						Point p = TickColorCanvas.this.getSize();
						rect = new Rectangle(0, 0, p.x - 1, p.y - 1);
						GC gc = e.gc;
						gc.setForeground(borderColor);
						gc.drawRectangle(rect);
					}
    			}});
    		
    		addMouseListener(new MouseAdapter() {
				@Override
                public void mouseUp(MouseEvent e) {
					selectedIndex = colorIndex;
					colorComposite.redraw(0, 0, colorComposite.getBounds().width, colorComposite.getBounds().height, true);
                }});
    	}

		public void setColorIndex(int index) {
	        colorIndex = index;
        }
    }
    
	
}
