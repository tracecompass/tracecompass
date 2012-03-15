/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.dialogs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TargetResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.service.ILttControllerService;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.util.TCFTask;

/**
 * <b><u>ConfigureMarkersDialog</u></b>
 * <p>
 * Dialog box to configure markers for a given target resource.
 * </p>
 */
public class ConfigureMarkersDialog extends Dialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    private String[] fMarkersList;
    private TargetResource fTarget;
    private Boolean fOkClicked;
    Map<String, Boolean> fMap;
    private TableItem[] fTableLines;
    private Boolean[] fInitialMarkersStates;

    private TraceSubSystem fSubSystem;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor 
     * 
     * @param parent The parent shell
     * @param subSystem The trace SubSystem
     */
    public ConfigureMarkersDialog(Shell parent, TraceSubSystem subSystem) {
        super(parent);
        fOkClicked = false;
        fSubSystem = subSystem;
    }

    /**
     * Constructor
     * 
     * @param parent The parent shell
     * @param style The dialog box style
     * @param subSystem The trace SubSystem

     */
    public ConfigureMarkersDialog(Shell parent, int style, TraceSubSystem subSystem) {
        super(parent, style);
        fOkClicked = false;
        fSubSystem = subSystem;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    public Map<String, Boolean> open(TargetResource aTarget) {
        fTarget = aTarget;
        
        Shell parent = getParent();
        final Display display = parent.getDisplay();
        final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
        shell.setText(Messages.ConfigureMarkersDialog_Title);
        shell.setImage(Activator.getDefault().getImage(Activator.ICON_ID_CONFIG_MARKERS));
        shell.setLayout(new FillLayout());
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new GridLayout());

        try {

            final ILttControllerService service = fSubSystem.getControllerService();

            // Create future task
            fMarkersList = new TCFTask<String[]>() {
                @Override
                public void run() {

                    // Get markers using Lttng controller service proxy
                    service.getMarkers(fTarget.getParent().getName(), fTarget.getName(), new ILttControllerService.DoneGetMarkers() {

                        @Override
                        public void doneGetMarkers(IToken token, Exception error, String[] str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Notify about success
                            done(str);
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            SystemMessageException sysExp;
            if (e instanceof SystemMessageException) {
                sysExp = (SystemMessageException)e;
            } else {
                sysExp = new SystemMessageException(Activator.getDefault().getMessage(e));    
            }
            SystemBasePlugin.logError(Messages.Lttng_Control_ErrorGetMarkers + " (" +  //$NON-NLS-1$
                    Messages.Lttng_Resource_Target + ": "  + fTarget.getName() + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }

        for (int i = 0; i < fMarkersList.length; i++) {
            fMarkersList[i] = fMarkersList[i].trim();
        }
        final Table table = new Table(composite, SWT.BORDER | SWT.CHECK);
        TableColumn tc0 = new TableColumn(table, SWT.LEFT | SWT.CENTER);
        TableColumn tc1 = new TableColumn(table, SWT.LEFT);
        TableColumn tc2 = new TableColumn(table, SWT.LEFT);
        TableColumn tc3 = new TableColumn(table, SWT.LEFT);
        TableColumn tc4 = new TableColumn(table, SWT.LEFT);
        TableColumn tc5 = new TableColumn(table, SWT.LEFT);
        TableColumn tc6 = new TableColumn(table, SWT.LEFT);
        tc1.setText(Messages.ConfigureMarkersDialog_NameColumn);
        tc2.setText(Messages.ConfigureMarkersDialog_Location);
        tc3.setText(Messages.ConfigureMarkersDialog_Format);
        tc4.setText(Messages.ConfigureMarkersDialog_EventId);
        tc5.setText(Messages.ConfigureMarkersDialog_Call);
        tc6.setText(Messages.ConfigureMarkersDialog_Probe_Single);
        tc0.setWidth(25);
        tc1.setWidth(100);
        tc2.setWidth(100);
        tc3.setWidth(170);
        tc4.pack();
        tc5.setWidth(100);
        tc6.setWidth(100);
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fTableLines = new TableItem[fMarkersList.length];
        fInitialMarkersStates = new Boolean[fMarkersList.length];

        for (int i = 0; i < fMarkersList.length; i++) {
            fTableLines[i] = new TableItem(table, SWT.NONE);
            String markerInfoResult = null;
            final String currentMarker = fMarkersList[i];
            
            try {

                final ILttControllerService service = fSubSystem.getControllerService();
                
                // Create future task
                markerInfoResult = new TCFTask<String>() {
                    @Override
                    public void run() {

                        // Get marker info using Lttng controller service proxy
                        service.getMarkerInfo(fTarget.getParent().getName(), fTarget.getName(), currentMarker, new ILttControllerService.DoneGetMarkerInfo() {

                            @Override
                            public void doneGetMarkerInfo(IToken token, Exception error, String str) {
                                if (error != null) {
                                    // Notify with error
                                    error(error);
                                    return;
                                }

                                // Notify about success
                                done(str);
                            }
                        });
                    }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception e) {
                SystemMessageException sysExp;
                if (e instanceof SystemMessageException) {
                    sysExp = (SystemMessageException)e;
                } else {
                    sysExp = new SystemMessageException(Activator.getDefault().getMessage(e));    
                }
                SystemBasePlugin.logError(Messages.Lttng_Control_ErrorGetMarkerInfo + " (" +  //$NON-NLS-1$
                        Messages.Lttng_Resource_Target + ": "  + fTarget.getName() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
                        Messages.Lttng_Resource_Marker + ": " + currentMarker + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }

            String markerInfos = markerInfoResult.substring(1, markerInfoResult.length() - 1);
            // System.out.println("markerInfos " + markerInfos);

            // HACK!!!
            // BAD : payload CAN contain comma!!!
            /*
             * String[] infosList = markerInfos.split(","); for(int j=0 ; j<infosList.length ; j++) { infosList[j] = infosList[j].trim(); String[] tempTable = infosList[j].split("="); infosList[j] = tempTable[1]; System.out.print(infosList[j] + " ");
             * } System.out.println("");
             */

            // QUICK FIX :
            int nbOfEqualsHack = 0;
            for (int x = 0; x < markerInfos.length(); x++) {
                if (markerInfos.charAt(x) == '=') {
                    nbOfEqualsHack++;
                }
            }

            if (nbOfEqualsHack > 0) { 
                String[] infosList = new String[nbOfEqualsHack];
                String value = ""; //$NON-NLS-1$

                int prevPos = 0;
                int curPos = 0;
                int eqPos = 0;
                int nbDone = 0;

                while ((curPos < markerInfos.length()) && (eqPos >= 0)) {
                    eqPos = markerInfos.indexOf("=", curPos); //$NON-NLS-1$

                    if (eqPos >= 0) {
                        prevPos = markerInfos.lastIndexOf(",", eqPos); //$NON-NLS-1$
                    } else {
                        prevPos = markerInfos.length() - 1;
                    }

                    if (prevPos >= 0) {
                        value = markerInfos.substring(curPos, prevPos);

                        infosList[nbDone] = value;
                        nbDone++;
                    }
                    curPos = eqPos + 1;
                }

                fTableLines[i].setText(new String[] { null, fMarkersList[i], infosList[3], infosList[4], infosList[2], infosList[0], infosList[5] });

                if (infosList[1].compareTo("1") == 0) { //$NON-NLS-1$
                    fTableLines[i].setChecked(true);
                    fInitialMarkersStates[i] = true;
                } else {
                    fTableLines[i].setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
                    fInitialMarkersStates[i] = false;
                }
            }
        }

        table.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    TableItem ti = (TableItem) event.item;
                    if (!ti.getChecked()) {
                        ti.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
                    } else {
                        ti.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
                    }
                }
            }
        });

        final Composite buttonComposite = new Composite(composite, SWT.NONE);
        buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout gl = new GridLayout(4, false);
        gl.verticalSpacing = 10;
        buttonComposite.setLayout(gl);

        Label shadow_sep_h = new Label(buttonComposite, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
        shadow_sep_h.setLayoutData(gd);

        Button selectAllButton = new Button(buttonComposite, SWT.PUSH);
        selectAllButton.setText(Messages.ConfigureMarkersDialog_Select_All);
        gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        gd.widthHint = 100;
        selectAllButton.setLayoutData(gd);
        selectAllButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                for (int i = 0; i < fTableLines.length; i++) {
                    fTableLines[i].setChecked(true);
                    fTableLines[i].setForeground(display.getSystemColor(SWT.COLOR_BLACK));
                }
            }
        });

        Button deselectAllButton = new Button(buttonComposite, SWT.PUSH);
        deselectAllButton.setText(Messages.ConfigureMarkersDialog_Deselect_All);
        gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        gd.widthHint = 100;
        deselectAllButton.setLayoutData(gd);
        deselectAllButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                for (int i = 0; i < fTableLines.length; i++) {
                    fTableLines[i].setChecked(false);
                    fTableLines[i].setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
                }
            }
        });

        Button cancelButton = new Button(buttonComposite, SWT.PUSH);
        cancelButton.setText(Messages.ConfigureMarkersDialog_Cancel);
        gd = new GridData();
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        gd.widthHint = 100;
        cancelButton.setLayoutData(gd);
        cancelButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                fOkClicked = Boolean.valueOf(false);
                shell.dispose();
            }
        });

        Button okButton = new Button(buttonComposite, SWT.PUSH);
        okButton.setText(Messages.ConfigureMarkersDialog_Ok);
        gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        gd.widthHint = 100;
        okButton.setLayoutData(gd);
        okButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                fOkClicked = Boolean.valueOf(true);
                fMap = new HashMap<String, Boolean>();
                for (int k = 0; k < fTableLines.length; k++) {
                    Boolean isChecked = fTableLines[k].getChecked();
                    if (isChecked.booleanValue() != fInitialMarkersStates[k].booleanValue()) {
                        if (isChecked) {
                            fMap.put(fMarkersList[k], Boolean.valueOf(true));
                        } else {
                            fMap.put(fMarkersList[k], Boolean.valueOf(false));
                        }
                    }
                }
                shell.dispose();
            }
        });

        shell.addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.TRAVERSE_ESCAPE) {
                    event.doit = false;
                }
            }
        });
        
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++) {
            if ((i % 2) != 0) {
                items[i].setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
            }
        }
        
        Point minSize = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        shell.setMinimumSize(shell.computeSize(minSize.x, minSize.y).x, 200);
        shell.setSize(shell.computeSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, 300));
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        if (!fOkClicked) {
            return null;
        }

        return fMap;
    }
}
