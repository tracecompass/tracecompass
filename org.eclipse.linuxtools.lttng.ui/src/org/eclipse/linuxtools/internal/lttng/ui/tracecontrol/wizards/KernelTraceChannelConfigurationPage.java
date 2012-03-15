/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource.TraceState;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannel;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannels;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.model.config.TraceChannelCellModifier;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.model.config.TraceChannelTableContentProvider;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.model.config.TraceChannelTableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>KernelTraceChannelConfigurationPage</u></b>
 * <p>
 *  Wizard page implementation to configure the kernel trace channels.
 * </p>
 */
public class KernelTraceChannelConfigurationPage extends WizardPage implements ITraceChannelConfigurationPage {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    private TraceChannels fChannels;
    private TraceState fTraceState;
    private Composite container;
    private TableViewer tableViewer;
    List<String> fColumnNames;
    
    Action enableAllAction;
    Action disableAllAction;
    Action setOverrideAllAction;
    Action resetOverideAllAction;
    Action setNumSubbufAction;
    Action setSubbufSizeAction;
    Action setChanTimerAction;

    private ColumnData[] columnDataList = new ColumnData[] {
            new ColumnData(Messages.ChannelConfigPage_ChannelName, 150, SWT.LEFT, Messages.ChannelConfigPage_ChannelNameTooltip, new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        TraceChannel chan1 = (TraceChannel) e1;
                        TraceChannel chan2 = (TraceChannel) e2;
                        return chan1.getName().compareTo(chan2.getName());
                    }
            }),
            new ColumnData(Messages.ChannelConfigPage_ChannelEnabled, 150, SWT.LEFT, Messages.ChannelConfigPage_ChannelEnabledTooltip, null),
            new ColumnData(Messages.ChannelConfigPage_ChannelOverride, 150, SWT.LEFT, Messages.ChannelConfigPage_BufferOverrideTooltip, null),
            new ColumnData(Messages.ChannelConfigPage_NumSubBuf, 175, SWT.LEFT, Messages.ChannelConfigPage_NumSubBufTooltip, null),
            new ColumnData(Messages.ChannelConfigPage_SubBufSize, 150, SWT.LEFT, Messages.ChannelConfigPage_SubBufSizeTooltip, null),
            new ColumnData(Messages.ChannelConfigPage_ChannelTimer, 150, SWT.LEFT, Messages.ChannelConfigPage_ChannelTimerTooltip, null)};
    

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * 
     * @param channels The current channels information
     * @param state The trace state
     */
    protected KernelTraceChannelConfigurationPage(TraceChannels channels, TraceState state) {
        super("TraceChannelConfigurationPage"); //$NON-NLS-1$
        fChannels = channels;
        fTraceState = state;
        setTitle(Messages.ChannelConfigPage_PageTitle);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout());
        setControl(container);

        Composite headerComposite = new Composite(container, SWT.FILL);
        GridLayout headerLayout = new GridLayout(1, true);
        headerLayout.marginHeight = 0;
        headerLayout.marginWidth = 0;
        headerComposite.setLayout(headerLayout);
        headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        tableViewer = new TableViewer(headerComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION );
        tableViewer.setUseHashlookup(true);
        
        Table table = tableViewer.getTable(); 
        table.setHeaderVisible(true);

        fColumnNames = new ArrayList<String>();
        for (int i = 0; i < columnDataList.length; i++) {

            final ColumnData columnData = columnDataList[i];
            fColumnNames.add(columnData.header);
            final TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
            tableColumn.setText(columnData.header);
            tableColumn.setWidth(columnData.width);
            tableColumn.setAlignment(columnData.alignment);
            tableColumn.setToolTipText(columnData.tooltip);
            tableColumn.setMoveable(false);
            if (columnData.comparator != null) {
                tableColumn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (tableViewer.getTable().getSortDirection() == SWT.UP || (tableViewer.getTable().getSortColumn() != tableColumn)) {
                            tableViewer.setComparator(columnData.comparator);
                            tableViewer.getTable().setSortDirection(SWT.DOWN);
                        } else {
                            tableViewer.setComparator(new ViewerComparator() {
                                @Override
                                public int compare(Viewer viewer,Object e1, Object e2) {
                                    return -1 * columnData.comparator.compare(viewer, e1,e2);
                                }
                            });
                            tableViewer.getTable().setSortDirection(SWT.UP);
                        }
                        tableViewer.getTable().setSortColumn(tableColumn);
                    }
                });
            }
        }

        tableViewer.setColumnProperties(fColumnNames.toArray(new String[0]));
        
        // Create the cell editors
        CellEditor[] editors = new CellEditor[columnDataList.length];

        // Column 1 : Completed (Checkbox)
        TextCellEditor textEditor = new TextCellEditor(table);
        editors[0] = textEditor;

        // Column 2 : Description (Free text)
        editors[1] = new CheckboxCellEditor(table);

        // Column 3 : Owner (Combo Box) 
        editors[2] = new CheckboxCellEditor(table);

        // Column 4 : Percent complete (Text with digits only)
         textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).addVerifyListener(
        
            new VerifyListener() {
                
                @Override
                public void verifyText(VerifyEvent e) {
                    e.doit = e.text.matches("[0-9]*"); //$NON-NLS-1$
                }
            });
        editors[3] = textEditor;

        textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).addVerifyListener(
        
            new VerifyListener() {
                
                @Override
                public void verifyText(VerifyEvent e) {
                    e.doit = e.text.matches("[0-9]*"); //$NON-NLS-1$
                }
            });
        editors[4] = textEditor;
        
        textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).addVerifyListener(
        
            new VerifyListener() {
                
                @Override
                public void verifyText(VerifyEvent e) {
                    e.doit = e.text.matches("[0-9]*"); //$NON-NLS-1$
                }
            });
        editors[5] = textEditor;

        // Assign the cell editors to the viewer 
        tableViewer.setCellEditors(editors);
        tableViewer.setCellModifier(new TraceChannelCellModifier(this));

        tableViewer.setContentProvider(new TraceChannelTableContentProvider());
        tableViewer.setLabelProvider(new TraceChannelTableLabelProvider());
        
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        makeActions();
        addContextMenu();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible && (fChannels != null)) {
            // Get new channels configuration
            tableViewer.setInput(fChannels);
            
            Table table = tableViewer.getTable();
            TableItem[] items = table.getItems();
            for (int i = 0; i < items.length; i++) {
                if ((i % 2) != 0) {
                    items[i].setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
                }
                
                if ((fTraceState != TraceState.CREATED)  && (fTraceState != TraceState.CONFIGURED)) {
                    items[i].setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
                }    
            }

            container.layout();
        }
        super.setVisible(visible);
    }

    /**
     * Gets the list of column names.
     * 
     * @return list of column names
     */
    public List<String> getColumnProperties() {
        return fColumnNames;
    }
    
    /*
     * Refreshes the table.
     */
    public void refresh() {
        tableViewer.refresh();
    }
    
    /**
     * Gets the trace state.
     * 
     * @return trace state
     */
    public TraceState getTraceState() {
        return fTraceState;
    }
    
    /**
     * Gets if trace is a local trace (i.e. if trace output is stored on host 
     * where client is running)
     * 
     * @return isLocalTrace
     */
    public boolean isLocalTrace() {
        TraceConfigurationPage configPage = (TraceConfigurationPage) getPreviousPage();
        return configPage.isLocalTrace();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.ui.wizards.ITraceChannelConfigurationPage#getTraceChannels()
     */
    @Override
    public TraceChannels getTraceChannels() {
        return fChannels;
    }
    
    /*
     * Local class to configure table columns
     */
    private static final class ColumnData {
        // Name of the column.
        public final String header;
        // Width of the column.
        public final int width;
        // Alignment of the column.
        public final int alignment;
        // Tooltip of the column.
        public final String tooltip;
        // Used to sort elements of this column. Can be null.
        public final ViewerComparator comparator;

        public ColumnData(String h, int w, int a, String t, ViewerComparator c) {
            header = h;
            width = w;
            alignment = a;
            tooltip = t;
            comparator = c;
        }
    };
    
    /*
     * Creates actions for context sensitive menu. 
     */
    private void makeActions() {

        // Create the context menu actions
        enableAllAction = new Action(Messages.ChannelConfigPage_EnableAll, Activator.getDefault().getImageDescriptor(Activator.ICON_ID_CHECKED)) {
            @Override
            public void run() {
                TableItem[] items = tableViewer.getTable().getItems();
                for (int i = 0; i < items.length; i++) {
                    TraceChannel chan = (TraceChannel)items[i].getData();
                    chan.setIsEnabled(true);
                }
                tableViewer.refresh();
            }
        };

        disableAllAction = new Action(Messages.ChannelConfigPage_DisableAll, Activator.getDefault().getImageDescriptor(Activator.ICON_ID_UNCHECKED)) {
            @Override
            public void run() {
                TableItem[] items = tableViewer.getTable().getItems();
                for (int i = 0; i < items.length; i++) {
                    TraceChannel chan = (TraceChannel)items[i].getData();
                    chan.setIsEnabled(false);
                }
                tableViewer.refresh();
            }
        };
        setOverrideAllAction = new Action(Messages.ChannelConfigPage_EnableAllBufferOverride, Activator.getDefault().getImageDescriptor(Activator.ICON_ID_CHECKED)) {
            @Override
            public void run() {
                TableItem[] items = tableViewer.getTable().getItems();
                for (int i = 0; i < items.length; i++) {
                    TraceChannel chan = (TraceChannel)items[i].getData();
                    chan.setIsChannelOverride(true);
                }
                tableViewer.refresh();
            }
        };
        resetOverideAllAction= new Action(Messages.ChannelConfigage_DisableAllBufferOverride, Activator.getDefault().getImageDescriptor(Activator.ICON_ID_UNCHECKED)) {
            @Override
            public void run() {
                TableItem[] items = tableViewer.getTable().getItems();
                for (int i = 0; i < items.length; i++) {
                    TraceChannel chan = (TraceChannel)items[i].getData();
                    chan.setIsChannelOverride(false);
                }
                tableViewer.refresh();
            }
        };
        setNumSubbufAction = new Action(Messages.ChannelConfigPage_SetAllNumSubBuf, Activator.getDefault().getImageDescriptor(Activator.ICON_ID_EDIT)) {
            @Override
            public void run() {
                TableItem[] items = tableViewer.getTable().getItems();
                SetDialog setDialog = new SetDialog(getShell(), Messages.ChannelConfigPage_NumSubBuf);
                if (setDialog.open() == Window.OK) {

                    for (int i = 0; i < items.length; i++) {
                        TraceChannel chan = (TraceChannel)items[i].getData();
                        chan.setSubbufNum(setDialog.getValue());
                    }

                    tableViewer.refresh();
                }
            }
        };
        setSubbufSizeAction = new Action(Messages.channelConfigPage_SetAllSubBufSize, Activator.getDefault().getImageDescriptor(Activator.ICON_ID_EDIT)) {
            @Override
            public void run() {
                TableItem[] items = tableViewer.getTable().getItems();
                SetDialog setDialog = new SetDialog(getShell(), Messages.ChannelConfigPage_SubBufSize);
                if (setDialog.open() == Window.OK) {

                    for (int i = 0; i < items.length; i++) {
                        TraceChannel chan = (TraceChannel)items[i].getData();
                        chan.setSubbufSize(setDialog.getValue());
                    }

                    tableViewer.refresh();
                }
            }
        };
        setChanTimerAction = new Action(Messages.ChannelConfigPage_SetAllChannelTimer, Activator.getDefault().getImageDescriptor("ICON_ID_EDIT")) { //$NON-NLS-1$
            @Override
            public void run() {
                TableItem[] items = tableViewer.getTable().getItems();
                SetDialog setDialog = new SetDialog(getShell(), Messages.ChannelConfigPage_ChannelTimer);
                if (setDialog.open() == Window.OK) {

                    for (int i = 0; i < items.length; i++) {
                        TraceChannel chan = (TraceChannel)items[i].getData();
                        chan.setTimer(setDialog.getValue());
                    }

                    tableViewer.refresh();
                }
            }
        };
    }

    /*
     * Adds context sensitive menu to table.
     */
    private void addContextMenu() {
        MenuManager manager = new MenuManager("configChanPopupMenu"); //$NON-NLS-1$
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new IMenuListener() {
            
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                
                if ((fTraceState != TraceState.CREATED)  && (fTraceState != TraceState.CONFIGURED)) {
                    return;
                }
                manager.add(enableAllAction);
                manager.add(disableAllAction);
                manager.add(setOverrideAllAction);
                manager.add(resetOverideAllAction);
                manager.add(setNumSubbufAction);
                manager.add(setSubbufSizeAction);
                manager.add(setChanTimerAction);
            }
        });
        
        Menu menu = manager.createContextMenu(tableViewer.getControl());
        tableViewer.getControl().setMenu(menu);
    }
    
    /**
     * Local class dialog box implementation for setting values for all
     * rows for a given column  
     */
    private static final class SetDialog extends Dialog {

        // ------------------------------------------------------------------------
        // Attributes
        // ------------------------------------------------------------------------

        private String fWhat = null;
        private Text fValueText = null;
        private Long fValue = null;

        // ------------------------------------------------------------------------
        // Constructors
        // ------------------------------------------------------------------------

        public SetDialog(Shell parentShell, String what) {
            super(parentShell);
            fWhat = what;
        }

        // ------------------------------------------------------------------------
        // Operations
        // ------------------------------------------------------------------------

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
         */
        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.ChannelConfigPage_SetAll);    
            newShell.setImage(Activator.getDefault().getImage(Activator.ICON_ID_EDIT));
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createDialogArea(Composite parent) {
            //Main dialog panel
            Composite composite = new Composite(parent, SWT.RESIZE);
            GridLayout mainLayout = new GridLayout(3, true);
            composite.setLayout(mainLayout);
            Label what = new Label(composite, SWT.LEFT);
            what.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
            what.setText(fWhat);
            
            fValueText = new Text(composite, SWT.LEFT);
            fValueText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
            
            fValueText.addVerifyListener(
                    new VerifyListener() {
                        
                        @Override
                        public void verifyText(VerifyEvent e) {
                            e.doit = e.text.matches("[0-9]*"); //$NON-NLS-1$
                        }
                    });
            
            fValueText.addListener(SWT.Modify, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    Button ok = getButton(IDialogConstants.OK_ID);
                    ok.setEnabled(validateInput(fValueText.getText()));
                }
            });
            
            return composite;
        }
        
        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            Button ok = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
            
            ok.setEnabled(false);
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.dialogs.Dialog#okPressed()
         */
        @Override
        protected void okPressed() {
            fValue = Long.valueOf(fValueText.getText());
            super.okPressed();
        }

        /*
         * Method to validate input.
         */
        private boolean validateInput(String path) {
            return (path.trim().length() > 0);
        }

        /**
         * Gets value that the user input. 
         * 
         * @return value
         */
        public Long getValue() {
            return fValue;
        }
    }
}
