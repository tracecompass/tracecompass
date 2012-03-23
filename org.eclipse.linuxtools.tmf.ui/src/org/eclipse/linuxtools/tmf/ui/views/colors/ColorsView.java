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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.internal.tmf.ui.viewers.timeAnalysis.widgets.TraceColorScheme;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeAnalysisProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.views.filter.FilterDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;

public class ColorsView extends TmfView {

	public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.colors"; //$NON-NLS-1$

    private static final Image ADD_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/add_button.gif"); //$NON-NLS-1$
    private static final Image DELETE_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/delete_button.gif"); //$NON-NLS-1$
    private static final Image MOVE_UP_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/moveup_button.gif"); //$NON-NLS-1$
    private static final Image MOVE_DOWN_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/movedown_button.gif"); //$NON-NLS-1$
    private static final Image IMPORT_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/import_button.gif"); //$NON-NLS-1$
    private static final Image EXPORT_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/export_button.gif"); //$NON-NLS-1$
    
    // ------------------------------------------------------------------------
    // Main data structures
    // ------------------------------------------------------------------------

    protected Shell fShell;
	protected ScrolledComposite fScrolledComposite;
	protected Composite fListComposite;
	protected Composite fFillerComposite;
	
	private ColorSettingRow fSelectedRow = null;

	private TraceColorScheme traceColorScheme = new TraceColorScheme();
	private TmfTimeAnalysisProvider timeAnalysisProvider = new TmfTimeAnalysisProvider() {
		@Override
        public StateColor getEventColor(ITimeEvent event) {
	        return null;
        }
		@Override
        public String getTraceClassName(ITmfTimeAnalysisEntry trace) {
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
	
	Action fAddAction;
	Action fDeleteAction;
	Action fMoveUpAction;
	Action fMoveDownAction;
	Action fImportAction;
	Action fExportAction;

	protected List<ColorSetting> fColorSettings;
	
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     *  Default Constructor
     */
	public ColorsView() {
		super("Colors"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		fShell = parent.getShell();

        fScrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        fScrolledComposite.setExpandHorizontal(true);
        fScrolledComposite.setExpandVertical(true);
        fListComposite = new Composite(fScrolledComposite, SWT.NONE);
        fScrolledComposite.setContent(fListComposite);
        
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 1;
        fListComposite.setLayout(gl);

		fColorSettings = new ArrayList<ColorSetting>(Arrays.asList(ColorSettingsManager.getColorSettings())); 
        for (ColorSetting colorSetting : fColorSettings) {
        	new ColorSettingRow(fListComposite, colorSetting);
        }
        
        fFillerComposite = new Composite(fListComposite, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 0;
        fFillerComposite.setLayoutData(gd);
        gl = new GridLayout();
        gl.marginHeight = 1;
        gl.marginWidth = 1;
        fFillerComposite.setLayout(gl);
        Label fillerLabel = new Label(fFillerComposite, SWT.NONE);
        fillerLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fillerLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        
        fFillerComposite.addPaintListener(new PaintListener() {
			@Override
            public void paintControl(PaintEvent e) {
				if (fSelectedRow == null) {
					Color lineColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
					Point p = fFillerComposite.getSize();
					GC gc = e.gc;
					gc.setForeground(lineColor);
					gc.drawLine(0, 0, p.x - 1, 0);
				}
			}
		});

        MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				fSelectedRow = null;
				refresh();
			}
        }; 
        fillerLabel.addMouseListener(mouseListener);
        
        fScrolledComposite.setMinSize(fListComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
        fillToolBar();
	}
	
	public void refresh() {
    	fListComposite.layout();
        fScrolledComposite.setMinSize(fListComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		fListComposite.redraw(0, 0, fListComposite.getBounds().width, fListComposite.getBounds().height, true);
		if (fSelectedRow == null) {
			fDeleteAction.setEnabled(false);
			fMoveUpAction.setEnabled(false);
			fMoveDownAction.setEnabled(false);
		} else {
			fDeleteAction.setEnabled(true);
			fMoveUpAction.setEnabled(true);
			fMoveDownAction.setEnabled(true);
		}
	}
	
	private void fillToolBar() {
		
		fAddAction = new AddAction();
		fAddAction.setImageDescriptor(ImageDescriptor.createFromImage(ADD_IMAGE));
		fAddAction.setToolTipText(Messages.ColorsView_AddActionToolTipText);	

		fDeleteAction = new DeleteAction();
		fDeleteAction.setImageDescriptor(ImageDescriptor.createFromImage(DELETE_IMAGE));
		fDeleteAction.setToolTipText(Messages.ColorsView_DeleteActionToolTipText);
		fDeleteAction.setEnabled(false);

		fMoveUpAction = new MoveUpAction();
		fMoveUpAction.setImageDescriptor(ImageDescriptor.createFromImage(MOVE_UP_IMAGE));
		fMoveUpAction.setToolTipText(Messages.ColorsView_MoveUpActionToolTipText);
		fMoveUpAction.setEnabled(false);

		fMoveDownAction = new MoveDownAction();
		fMoveDownAction.setImageDescriptor(ImageDescriptor.createFromImage(MOVE_DOWN_IMAGE));
		fMoveDownAction.setToolTipText(Messages.ColorsView_MoveDownActionToolTipText);
		fMoveDownAction.setEnabled(false);

		fExportAction = new ExportAction();
		fExportAction.setImageDescriptor(ImageDescriptor.createFromImage(EXPORT_IMAGE));
		fExportAction.setToolTipText(Messages.ColorsView_ExportActionToolTipText);

		fImportAction = new ImportAction();
		fImportAction.setImageDescriptor(ImageDescriptor.createFromImage(IMPORT_IMAGE));
		fImportAction.setToolTipText(Messages.ColorsView_ImportActionToolTipText);

		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(fAddAction);
		manager.add(fDeleteAction);
		manager.add(fMoveUpAction);
		manager.add(fMoveDownAction);
		manager.add(new Separator());
		manager.add(fExportAction);
		manager.add(fImportAction);
	}

	private class AddAction extends Action {
		@Override
		public void run() {
			ColorSetting colorSetting = new ColorSetting(
					Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB(),
					Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB(),
					TraceColorScheme.BLACK_STATE,
					null);
        	ColorSettingRow row = new ColorSettingRow(fListComposite, colorSetting);
        	if (fSelectedRow == null) {
        		fColorSettings.add(colorSetting);
        		row.moveAbove(fFillerComposite);
        	} else {
        		fColorSettings.add(fColorSettings.indexOf(fSelectedRow.getColorSetting()), colorSetting);
        		row.moveAbove(fSelectedRow);
        	}
        	fSelectedRow = row;
        	refresh();
			ColorSettingsManager.setColorSettings(fColorSettings.toArray(new ColorSetting[0]));
		}
	}
	
	private class DeleteAction extends Action {
		@Override
		public void run() {
			if (fSelectedRow != null) {
				int index = fColorSettings.indexOf(fSelectedRow.getColorSetting());
				fColorSettings.remove(index);
				fSelectedRow.fColorSetting.dispose();
				fSelectedRow.dispose();
				if (index < fColorSettings.size()) {
					fSelectedRow = (ColorSettingRow) fListComposite.getChildren()[index];
				} else {
					fSelectedRow = null;
				}
	        	refresh();
				ColorSettingsManager.setColorSettings(fColorSettings.toArray(new ColorSetting[0]));
			}
		}
	}
	
	private class MoveUpAction extends Action {
		@Override
		public void run() {
			if (fSelectedRow != null) {
				int index = fColorSettings.indexOf(fSelectedRow.getColorSetting());
				if (index > 0) {
					fColorSettings.add(index - 1, fColorSettings.remove(index));
					fSelectedRow.moveAbove(fListComposite.getChildren()[index - 1]);
					refresh();
					ColorSettingsManager.setColorSettings(fColorSettings.toArray(new ColorSetting[0]));
				}
			}
		}
	}
	
	private class MoveDownAction extends Action {
		@Override
		public void run() {
			if (fSelectedRow != null) {
				int index = fColorSettings.indexOf(fSelectedRow.getColorSetting());
				if (index < fColorSettings.size() - 1) {
					fColorSettings.add(index + 1, fColorSettings.remove(index));
					fSelectedRow.moveBelow(fListComposite.getChildren()[index + 1]);
					refresh();
					ColorSettingsManager.setColorSettings(fColorSettings.toArray(new ColorSetting[0]));
				}
			}
		}
	}
	
	private class ExportAction extends Action {
		@Override
		public void run() {
			FileDialog fileDialog = new FileDialog(fShell, SWT.SAVE);
			fileDialog.setFilterExtensions(new String[] {"*.xml"}); //$NON-NLS-1$
			fileDialog.setOverwrite(true);
			String pathName = fileDialog.open();
	        if (pathName != null) {
	        	ColorSettingsXML.save(pathName, fColorSettings.toArray(new ColorSetting[0]));
	        }
		}
	}
	
	private class ImportAction extends Action {
		@Override
		public void run() {
			FileDialog fileDialog = new FileDialog(fShell, SWT.OPEN);
			fileDialog.setFilterExtensions(new String[] {"*.xml"}); //$NON-NLS-1$
			String pathName = fileDialog.open();
	        if (pathName != null) {
	        	ColorSetting[] colorSettings = ColorSettingsXML.load(pathName);
	        	if (colorSettings.length > 0) {
	        		if (fColorSettings.size() > 0) {
	        			boolean overwrite = MessageDialog.openQuestion(fShell,
	        					Messages.ColorsView_ImportOverwriteDialogTitle,
	        					Messages.ColorsView_ImportOverwriteDialogMessage1 +
	        					Messages.ColorsView_ImportOverwriteDialogMessage2);
	        			if (overwrite) {
		        			for (Control control : fListComposite.getChildren()) {
		        				if (control instanceof ColorSettingRow) {
		        					((ColorSettingRow) control).fColorSetting.dispose();
		        					control.dispose();
		        				}
		        			}
		        			fColorSettings = new ArrayList<ColorSetting>();
		        			fSelectedRow = null;
	        			}
	        		}
	                for (ColorSetting colorSetting : colorSettings) {
		            	ColorSettingRow row = new ColorSettingRow(fListComposite, colorSetting);
		            	if (fSelectedRow == null) {
		            		fColorSettings.add(colorSetting);
		            		row.moveAbove(fFillerComposite);
		            	} else {
		            		fColorSettings.add(fColorSettings.indexOf(fSelectedRow.getColorSetting()), colorSetting);
		            		row.moveAbove(fSelectedRow);
		            	}
	                }
	                refresh();
	                ColorSettingsManager.setColorSettings(fColorSettings.toArray(new ColorSetting[0]));
	        	}
	        }
		}
	}
	
	private class ColorSettingRow extends Composite {

		ColorSetting fColorSetting;
		
		public ColorSettingRow(final Composite parent, final ColorSetting colorSetting) {
			super(parent, SWT.NONE);
			fColorSetting = colorSetting;
			
			setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			
	        setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	        GridLayout gl = new GridLayout(7, false);
	        gl.marginHeight = 1;
	        gl.marginWidth = 1;
	        gl.horizontalSpacing = 1;
	        gl.verticalSpacing = 0;
	        setLayout(gl);

	        final Button fgButton = new Button(this, SWT.PUSH);
	        fgButton.setText(Messages.ColorsView_ForegroundButtonText);
	        fgButton.setSize(fgButton.computeSize(SWT.DEFAULT, 19));
	        fgButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	
	        final Button bgButton = new Button(this, SWT.PUSH);
	        bgButton.setText(Messages.ColorsView_BackgroundButtonText);
	        bgButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

	        final Composite labelComposite = new Composite(this, SWT.NONE);
	        labelComposite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
	        gl = new GridLayout();
	        gl.marginHeight = 0;
	        gl.marginWidth = 0;;
	        labelComposite.setLayout(gl);
	        labelComposite.setBackground(colorSetting.getBackgroundColor());
	        
	        final Label label = new Label(labelComposite, SWT.NONE);
	        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true));
	        label.setText(" Text "); //$NON-NLS-1$
	        label.setForeground(colorSetting.getForegroundColor());
	        label.setBackground(colorSetting.getBackgroundColor());
	        
	        fgButton.addSelectionListener(new SelectionAdapter() {
	        	@Override
	        	public void widgetSelected(SelectionEvent e) {
	        		fSelectedRow = ColorSettingRow.this;
	        		refresh();
	        		ColorDialog dialog = new ColorDialog(fShell);
	        		dialog.setRGB(colorSetting.getForegroundRGB());
	        		dialog.setText(Messages.ColorsView_ForegroundDialogText);
	        		dialog.open();
	        		colorSetting.setForegroundRGB(dialog.getRGB());
	        		ColorSettingsManager.setColorSettings(fColorSettings.toArray(new ColorSetting[0]));
	        		label.setForeground(colorSetting.getForegroundColor());
	        	}});
	        
	        bgButton.addSelectionListener(new SelectionAdapter() {
	        	@Override
	        	public void widgetSelected(SelectionEvent e) {
	        		fSelectedRow = ColorSettingRow.this;
	        		refresh();
	        		ColorDialog dialog = new ColorDialog(fShell);
	        		dialog.setRGB(colorSetting.getBackgroundRGB());
	        		dialog.setText(Messages.ColorsView_BackgroundDialogText);
	        		dialog.open();
	        		colorSetting.setBackgroundRGB(dialog.getRGB());
	        		ColorSettingsManager.setColorSettings(fColorSettings.toArray(new ColorSetting[0]));
	        		labelComposite.setBackground(colorSetting.getBackgroundColor());
	        		label.setBackground(colorSetting.getBackgroundColor());
	        	}});
	        
	        final Button tickButton = new Button(this, SWT.PUSH);
	        tickButton.setText(Messages.ColorsView_TickButtonText);
	        tickButton.setSize(tickButton.computeSize(SWT.DEFAULT, 19));
	        tickButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	        
	        final Canvas tickCanvas = new Canvas(this, SWT.NONE);
	        GridData gd = new GridData(SWT.CENTER, SWT.FILL, false, false);
	        gd.widthHint = 12;
	        gd.heightHint = bgButton.getSize().y;
	        tickCanvas.setLayoutData(gd);
	        tickCanvas.setBackground(traceColorScheme.getBkColor(false, false, false));
	        tickCanvas.addPaintListener(new PaintListener() {
				@Override
                public void paintControl(PaintEvent e) {
					Rectangle bounds = tickCanvas.getBounds();
					e.gc.setForeground(traceColorScheme.getColor(TraceColorScheme.MID_LINE));
					int midy = bounds.y + bounds.height / 2 - 1;
					//int midy = e.y + e.height / 2;
					e.gc.drawLine(e.x, midy, e.x + e.width, midy);
					Rectangle rect = new Rectangle(e.x + 1, bounds.y + 2, 0, bounds.height - 6);
					for (int i = 1; i <= 3; i++) {
						rect.x += i;
						rect.width = i;
						timeAnalysisProvider.drawState(traceColorScheme, fColorSetting.getTickColorIndex(), rect, e.gc, false, false, false);
					}
                }});

	        tickButton.addSelectionListener(new SelectionAdapter() {
	        	@Override
	        	public void widgetSelected(SelectionEvent e) {
	        		fSelectedRow = ColorSettingRow.this;
	        		refresh();
	        		TickColorDialog dialog = new TickColorDialog(fShell);
	        		dialog.setColorIndex(colorSetting.getTickColorIndex());
	        		dialog.open();
	        		if (dialog.getReturnCode() == Dialog.OK) {
	        			if (dialog.getColorIndex() != colorSetting.getTickColorIndex()) {
	        				colorSetting.setTickColorIndex(dialog.getColorIndex());
	        				ColorSettingsManager.setColorSettings(fColorSettings.toArray(new ColorSetting[0]));
	        				refresh();
	        			}
	        		}
	        	}});
	        
	        final Button filterButton = new Button(this, SWT.PUSH);
	        filterButton.setText(Messages.ColorsView_FilterButtonText);
	        filterButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	        
	        final Label filterText = new Label(this, SWT.NONE);
	        if (colorSetting.getFilter() != null) {
	        	filterText.setText(colorSetting.getFilter().toString());
	        	filterText.setToolTipText(colorSetting.getFilter().toString());
	        }
	        filterText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	        filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	        
	        filterButton.addSelectionListener(new SelectionAdapter() {
	        	@Override
	        	public void widgetSelected(SelectionEvent e) {
	        		fSelectedRow = ColorSettingRow.this;
	        		refresh();
	        		FilterDialog dialog = new FilterDialog(fShell);
	        		dialog.setFilter(colorSetting.getFilter());
	        		dialog.open();
	        		if (dialog.getReturnCode() == Dialog.OK) {
		        		if (dialog.getFilter() != null) {
			        		colorSetting.setFilter(dialog.getFilter());
			        		filterText.setText(dialog.getFilter().toString());
			        		filterText.setToolTipText(dialog.getFilter().toString());
		        		} else {
			        		colorSetting.setFilter(null);
			        		filterText.setText(""); //$NON-NLS-1$
			        		filterText.setToolTipText(""); //$NON-NLS-1$
		        		}
		        		ColorSettingsManager.setColorSettings(fColorSettings.toArray(new ColorSetting[0]));
		        		refresh();
	        		}
	        	}});
	        
	        addPaintListener(new PaintListener() {
				@Override
                public void paintControl(PaintEvent e) {
					if (fSelectedRow == ColorSettingRow.this) {
						Color borderColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
						Point p = ColorSettingRow.this.getSize();
						Rectangle rect = new Rectangle(0, 0, p.x - 1, p.y - 1);
						GC gc = e.gc;
						gc.setForeground(borderColor);
						gc.drawRectangle(rect);
					}
				}
			});

	        MouseListener mouseListener = new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					fSelectedRow = ColorSettingRow.this;
					refresh();
				}
	        }; 
	        addMouseListener(mouseListener);
	        label.addMouseListener(mouseListener);
	        tickCanvas.addMouseListener(mouseListener);
	        filterText.addMouseListener(mouseListener);
		}

		/**
		 * @return the ColorSetting
		 */
		public ColorSetting getColorSetting() {
			return fColorSetting;
		}

	}
}
