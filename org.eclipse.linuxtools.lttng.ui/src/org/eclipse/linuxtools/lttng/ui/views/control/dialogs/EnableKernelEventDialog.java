/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.TraceControlContentProvider;
import org.eclipse.linuxtools.lttng.ui.views.control.TraceControlLabelProvider;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.KernelProviderComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceProviderGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>EnableKernelEventDialog</u></b>
 * <p>
 * Dialog box for collecting information kernel events to be enabled.
 * </p>
 */
public class EnableKernelEventDialog extends Dialog implements IEnableKernelEventsDialog  {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String ENABLE_EVENT_ICON_FILE = "icons/elcl16/edit.gif"; //$NON-NLS-1$ 

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The dialog composite.
     */
    private Composite fDialogComposite;
    /**
     * The goup for the list of available tracepoints.
     */
    private Group fTracepointsGroup;
    /**
     * A tree viewer for diplaying and selection of available tracepoints.
     */
    private CheckboxTreeViewer fTracepointsViewer;
    /**
     * The Group for Syscalls selection.
     */
    private Group fSyscallGroup;
    /**
     * The button to enable or disable all syscalls
     */
    private Button fSyscallButton;
    /**
     * The group for defining a dynamic probe. 
     */
    private Group fProbeGroup;
    /**
     * The text field for the event name for the dynamic probe. 
     */
    private Text fProbeEventNameText;
    /**
     * The text field for the dynamic probe.
     */
    private Text fProbeText;
    /**
     * The group for defining a dynamic function probe. 
     */
    private Group fFunctionGroup;
    /**
     * The text field for the event name for the dynamic probe. 
     */
    private Text fFunctionEventNameText;
    /**
     * The text field for the dynamic function entry/return probe.
     */
    private Text fFunctionText;
    /**
     * The referenced kernel provider component which contains a list of available tracepoints.
     */
    private KernelProviderComponent fKernelProvider;
    /**
     * The flag indicating that all tracepoints are selected.
     */
    private boolean fIsAllTracepoints;
    /**
     * The flag indicating that syscalls are selected.
     */
    private boolean fIsAllSysCalls;
    /**
     * The list of tracepoints to be enabled.
     */
    private List<String> fSelectedEvents;
    /**
     *  The event name of the dynamic probe. 
     */
    private String fProbeEventName;
    /**
     * The dynamic probe.
     */
    private String fProbeString;
    /**
     * The event name of the dynamic function entry/return probe.
     */
    private String fFunctionEventName;
    /**
     * The dynamic function entry/return probe.
     */
    private String fFunctionString;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     * @param kernelProvider - the kernel provider component
     */
    public EnableKernelEventDialog(Shell shell, KernelProviderComponent kernelProvider) {
        super(shell);
        fKernelProvider = kernelProvider;
        setShellStyle(SWT.RESIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.dialogs.IEnableKernelEventsDialog#isAllTracePoints()
     */
    @Override
    public boolean isAllTracePoints() {
        return fIsAllTracepoints;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.dialogs.IEnableKernelEventsDialog#isAllSysCalls()
     */
    @Override
    public boolean isAllSysCalls() {
        return fIsAllSysCalls;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.dialogs.IEnableKernelEventsDialog#getEventNames()
     */
    @Override
    public List<String> getEventNames() {
        return new ArrayList<String>(fSelectedEvents);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.dialogs.IEnableKernelEventsDialog#getProbeName()
     */
    @Override
    public String getProbeName() {
        return fProbeString;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.dialogs.IEnableKernelEventsDialog#getProbeEventName()
     */
    @Override
    public String getProbeEventName() {
        return fProbeEventName;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.dialogs.IEnableKernelEventsDialog#getFunctionEventName()
     */
    @Override
    public String getFunctionEventName() {
        return fFunctionEventName;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.dialogs.IEnableKernelEventsDialog#getFunction()
     */
    @Override
    public String getFunction() {
        return fFunctionString;
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
        newShell.setText(Messages.TraceControl_EnableKernelEventsDialogTitle);
        newShell.setImage(LTTngUiPlugin.getDefault().loadIcon(ENABLE_EVENT_ICON_FILE));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        
        // Main dialog panel
        fDialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        fDialogComposite.setLayout(layout);
        fDialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // ------------------------------------------------------------------------
        // Tracepoints Group 
        // ------------------------------------------------------------------------
        fTracepointsGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        fTracepointsGroup.setText(Messages.TraceControl_EnableEventsTracepointGroupName);
        layout = new GridLayout(1, true);
        fTracepointsGroup.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        fTracepointsGroup.setLayoutData(data);
        
        fTracepointsViewer = new CheckboxTreeViewer(fTracepointsGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        fTracepointsViewer.getTree().setToolTipText(Messages.TraceControl_EnableEventsTracepointTreeTooltip);
        fTracepointsViewer.setContentProvider(new TraceControlContentProvider() {
            @Override
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof TraceProviderGroup) {
                    List<ITraceControlComponent> children = ((ITraceControlComponent)parentElement).getChildren(KernelProviderComponent.class);
                    return (ITraceControlComponent[]) children.toArray(new ITraceControlComponent[children.size()]);
                }
                if (parentElement instanceof ITraceControlComponent) {
                    return ((ITraceControlComponent)parentElement).getChildren();
                }
                return new Object[0];
            }
        });

        fTracepointsViewer.setLabelProvider(new TraceControlLabelProvider() {
            @Override
            public Image getImage(Object element) {
                return null;
            }
            @Override
            public String getText(Object element) {
                if ((element != null) && (element instanceof KernelProviderComponent)) {
                    return Messages.TraceControl_EnableEventsTracepointTreeAllLabel;
                }
                return super.getText(element);
            }
        });
        
        fTracepointsViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
              if (event.getChecked()) {
                  if (event.getElement() instanceof KernelProviderComponent) {
                      fTracepointsViewer.setSubtreeChecked(event.getElement(), true);
                  } 
              } else { 
                  if (event.getElement() instanceof KernelProviderComponent) {
                      fTracepointsViewer.setSubtreeChecked(event.getElement(), false);
                  } else {
                      ITraceControlComponent component = (ITraceControlComponent) event.getElement();
                      fTracepointsViewer.setChecked(component.getParent(), false);
                  }
              }
            }
          });

        fTracepointsViewer.setInput(fKernelProvider.getParent());
        fTracepointsViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

        // ------------------------------------------------------------------------
        // Syscalls Group 
        // ------------------------------------------------------------------------
        fSyscallGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        layout = new GridLayout(4, true);
        fSyscallGroup.setLayout(layout);
        fSyscallGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fSyscallButton = new Button(fSyscallGroup, SWT.CHECK);
        fSyscallButton.setText(Messages.TraceControl_EnableEventsSyscallName);
        fSyscallButton.setToolTipText(Messages.TraceControl_EnableEventsSyscallTooltip);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 4;
        fSyscallButton.setLayoutData(data);

        // ------------------------------------------------------------------------
        // Dynamic Probe Group 
        // ------------------------------------------------------------------------
        fProbeGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        fProbeGroup.setText(Messages.TraceControl_EnableEventsProbeGroupName);
        layout = new GridLayout(4, true);
        fProbeGroup.setLayout(layout);
        fProbeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label probeNameLabel = new Label(fProbeGroup, SWT.LEFT);
        probeNameLabel.setText(Messages.TraceControl_EnableEventsProbeEventNameLabel);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 1;
        probeNameLabel.setLayoutData(data);
        
        fProbeEventNameText = new Text(fProbeGroup, SWT.LEFT);
        fProbeEventNameText.setToolTipText(Messages.TraceControl_EnableEventsProbeEventNameTooltip);
        
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        fProbeEventNameText.setLayoutData(data);
        
        Label probeLabel = new Label(fProbeGroup, SWT.LEFT);
        probeLabel.setText(Messages.TraceControl_EnableEventsProbeNameLabel);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 1;
        probeLabel.setLayoutData(data);
        
        fProbeText = new Text(fProbeGroup, SWT.LEFT);
        fProbeText.setToolTipText(Messages.TraceControl_EnableEventsProbeNameTooltip);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        fProbeText.setLayoutData(data);

        // ------------------------------------------------------------------------
        // Dynamic Function Probe Group 
        // ------------------------------------------------------------------------
        fFunctionGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        fFunctionGroup.setText(Messages.TraceControl_EnableEventsFucntionGroupName);
        layout = new GridLayout(4, true);
        fFunctionGroup.setLayout(layout);
        fFunctionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label functionNameLabel = new Label(fFunctionGroup, SWT.LEFT);
        functionNameLabel.setText(Messages.TraceControl_EnableEventsProbeEventNameLabel);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 1;
        functionNameLabel.setLayoutData(data);

        fFunctionEventNameText = new Text(fFunctionGroup, SWT.LEFT);
        fFunctionEventNameText.setToolTipText(Messages.TraceControl_EnableEventsFunctionEventNameTooltip);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        fFunctionEventNameText.setLayoutData(data);
        
        Label functionLabel = new Label(fFunctionGroup, SWT.LEFT);
        functionLabel.setText(Messages.TraceControl_EnableEventsFunctionNameLabel);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 1;
        functionLabel.setLayoutData(data);

        fFunctionText = new Text(fFunctionGroup, SWT.LEFT);
        fFunctionText.setToolTipText(Messages.TraceControl_EnableEventsProbeNameTooltip);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        fFunctionText.setLayoutData(data);
        
        fDialogComposite.layout();
        
        return fDialogComposite;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        fIsAllTracepoints = fTracepointsViewer.getChecked(fKernelProvider);
        fIsAllSysCalls = fSyscallButton.getSelection();
        
        ITraceControlComponent[] events = fKernelProvider.getChildren();
        fSelectedEvents = new ArrayList<String>();
        for (int i = 0; i < events.length; i++) {
            if (fTracepointsViewer.getChecked(events[i])) {
                fSelectedEvents.add(events[i].getName());
            }
        }
        
        // initialize probe string
        fProbeEventName = null;
        fProbeString = null;
        String temp = fProbeEventNameText.getText();
        if (!temp.matches("^[\\s]{0,}$") && !temp.matches("^[a-zA-Z0-9\\-\\_]{1,}$")) { //$NON-NLS-1$ //$NON-NLS-2$
            MessageDialog.openError(getShell(),
                  Messages.TraceControl_EnableKernelEventsDialogTitle,
                  Messages.TraceControl_InvalidProbeNameError + " (" + temp + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            
            return;
        }
        
        if(!fProbeText.getText().matches("\\s*")) { //$NON-NLS-1$
            fProbeEventName = temp;
            // fProbeString will be validated by lttng-tools
            fProbeString = fProbeText.getText();
        } 

        // initialize function string
        fFunctionEventName = null;
        fFunctionString = null;

        temp = fFunctionEventNameText.getText();
        if (!temp.matches("^[\\s]{0,}$") && !temp.matches("^[a-zA-Z0-9\\-\\_]{1,}$")) { //$NON-NLS-1$ //$NON-NLS-2$
            MessageDialog.openError(getShell(),
                  Messages.TraceControl_EnableKernelEventsDialogTitle,
                  Messages.TraceControl_InvalidProbeNameError + " (" + temp + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            
            return;
        }

        if(!fFunctionText.getText().matches("\\s*")) { //$NON-NLS-1$
            fFunctionEventName = temp;
            // fFunctionString will be validated by lttng-tools
            fFunctionString = fFunctionText.getText();
        }

        // validation successful -> call super.okPressed()
        super.okPressed();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    
    
}
