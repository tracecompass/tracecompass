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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceProviderGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * Dialog box for collecting information events to be enabled.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class EnableEventsDialog extends Dialog implements IEnableEventsDialog  {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The icon file for this dialog box.
     */
    public static final String ENABLE_EVENT_ICON_FILE = "icons/elcl16/enable_event.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The dialog composite.
     */
    private Composite fDialogComposite;
    /**
     * The composite with widgets for collecting information about kernel events.
     */
    private EnableKernelEventComposite fKernelComposite;
    /**
     * The composite with widgets for collecting information about UST events.
     */
    private EnableUstEventsComposite fUstComposite;
    /**
     * Radio button for selecting kernel domain.
     */
    private Button fKernelButton;
    /**
     * Radio button for selecting UST domain.
     */
    private Button fUstButton;
    /**
     * The referenced trace provider group containing the kernel provider and UST
     * provider component which contains a list of available tracepoints.
     */
    private TraceProviderGroup fProviderGroup;
    /**
     * The parent domain component where the channel node should be added.
     * Null in case the domain is not known (i.e. on session level).
     */
    private TraceDomainComponent fDomain;
    /**
     * Output domain information. True in case of Kernel domain. False for UST.
     */
    private boolean fIsKernel;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public EnableEventsDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#isTracpoints()
     */
    @Override
    public boolean isTracepoints() {
        if (fIsKernel) {
            return fKernelComposite.isTracepoints();
        }
        return fUstComposite.isTracepoints();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#isAllTracePoints()
     */
    @Override
    public boolean isAllTracePoints() {
        if (fIsKernel) {
            return fKernelComposite.isAllTracePoints();
        }
        return fUstComposite.isAllTracePoints();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#isSysCalls()
     */
    @Override
    public boolean isSysCalls() {
        if (fIsKernel) {
            return fKernelComposite.isSysCalls();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#isAllSysCalls()
     */
    @Override
    public boolean isAllSysCalls() {
        if (fIsKernel) {
            return fKernelComposite.isSysCalls();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#getEventNames()
     */
    @Override
    public List<String> getEventNames() {
        if (fIsKernel) {
            return fKernelComposite.getEventNames();
        }
        return fUstComposite.getEventNames();
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#isDynamicProbe()
     */
    @Override
    public boolean isDynamicProbe() {
        if (fIsKernel) {
            return fKernelComposite.isDynamicProbe();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#getProbeName()
     */
    @Override
    public String getProbeName() {
        if (fIsKernel) {
            return fKernelComposite.getProbeName();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#getProbeEventName()
     */
    @Override
    public String getProbeEventName() {
        if (fIsKernel) {
            return fKernelComposite.getProbeEventName();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#isDynamicFunctionProbe()
     */
    @Override
    public boolean isDynamicFunctionProbe() {
        if (fIsKernel) {
            return fKernelComposite.isDynamicFunctionProbe();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#getFunctionEventName()
     */
    @Override
    public String getFunctionEventName() {
        if (fIsKernel) {
            return fKernelComposite.getFunctionEventName();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableKernelEvents#getFunction()
     */
    @Override
    public String getFunction() {
        if (fIsKernel) {
            return fKernelComposite.getFunction();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableUstEvents#isWildcard()
     */
    @Override
    public boolean isWildcard() {
        if (!fIsKernel) {
            return fUstComposite.isWildcard();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableUstEvents#getWildcard()
     */
    @Override
    public String getWildcard() {
        if (!fIsKernel) {
            return fUstComposite.getWildcard();
        }
        return null;

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableUstEvents#isLogLevel()
     */
    @Override
    public boolean isLogLevel() {
        if (!fIsKernel) {
            return fUstComposite.isLogLevel();
        }
        return false;

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableUstEvents#getLogLevelType()
     */
    @Override
    public LogLevelType getLogLevelType() {
        if (!fIsKernel) {
            return fUstComposite.getLogLevelType();
        }
        return null;

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableUstEvents#getLogLevel()
     */
    @Override
    public TraceLogLevel getLogLevel() {
        if (!fIsKernel) {
            return fUstComposite.getLogLevel();
        }
        return null;

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableUstEvents#getLogLevelEventName()
     */
    @Override
    public String getLogLevelEventName() {
        if (!fIsKernel) {
            return fUstComposite.getLogLevelEventName();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableEventsDialog#isKernel()
     */
    @Override
    public boolean isKernel() {
        return fIsKernel;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableEventsDialog#setTraceProviderGroup(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceProviderGroup)
     */
    @Override
    public void setTraceProviderGroup(TraceProviderGroup providerGroup) {
        fProviderGroup = providerGroup;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableEventsDialog#setTraceDomainComponent(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent)
     */
    @Override
    public void setTraceDomainComponent(TraceDomainComponent domain) {
        fDomain = domain;
        if (fDomain != null) {
            fIsKernel = fDomain.isKernel();
        } else {
            fIsKernel = fProviderGroup != null ? fProviderGroup.hasKernelProvider() : true;
        }
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
        newShell.setText(Messages.TraceControl_EnableEventsDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(ENABLE_EVENT_ICON_FILE));
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
        // Domain Group
        // ------------------------------------------------------------------------
        Group domainGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        domainGroup.setText(Messages.TraceControl_DomainDisplayName);
        layout = new GridLayout(2, true);
        domainGroup.setLayout(layout);

        fKernelButton = new Button(domainGroup, SWT.RADIO);
        fKernelButton.setText(Messages.TraceControl_KernelDomainDisplayName);
        fKernelButton.setSelection(fIsKernel);
        fUstButton = new Button(domainGroup, SWT.RADIO);
        fUstButton.setText(Messages.TraceControl_UstDisplayName);
        fUstButton.setSelection(!fIsKernel);

        if ((fDomain != null) || ((fProviderGroup != null) && (!fProviderGroup.hasKernelProvider()))) {
            fKernelButton.setEnabled(false);
            fUstButton.setEnabled(false);
        }

        // layout widgets
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        domainGroup.setLayoutData(data);

        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fKernelButton.setLayoutData(data);
        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fUstButton.setLayoutData(data);

        // ------------------------------------------------------------------------
        // Kernel or UST event data group
        // ------------------------------------------------------------------------
        fUstComposite = null;
        fKernelComposite = null;
        if (fIsKernel) {
            createKernelComposite();
            fUstComposite = null;
        } else {
            createUstComposite();
        }

        fKernelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fKernelButton.getSelection()) {
                    disposeUstComposite();
                    createKernelComposite();
                    fDialogComposite.layout();
                }
            }
        });

        fUstButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fUstButton.getSelection()) {
                    disposeKernelComposite();
                    createUstComposite();
                    fDialogComposite.layout();
                }
            }
        });

        fDialogComposite.layout();

        getShell().setMinimumSize(new Point(500, 650));

        return fDialogComposite;
    }


    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        fIsKernel = fKernelButton.getSelection();

        // Validate kernel composite in case of kernel domain
        if (fKernelComposite != null && !fKernelComposite.isValid()) {
            return;
        }

     // Validate UST composite in case of UST domain
        if (fUstComposite != null && !fUstComposite.isValid()) {
            return;
        }

        // validation successful -> call super.okPressed()
        super.okPressed();
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    /**
     * Creates the kernel composite (if not existing)
     */
    private void createKernelComposite() {
        if (fKernelComposite == null) {
            fKernelComposite = new EnableKernelEventComposite(fDialogComposite, SWT.NONE, fProviderGroup);
            GridLayout layout = new GridLayout(1, true);
            fKernelComposite.setLayout(layout);
            fKernelComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

            fKernelComposite.createContent();
        }
    }

    /**
     * Disposes the kernel composite (if existing)
     */
    private void disposeKernelComposite() {
        if (fKernelComposite != null) {
            fKernelComposite.dispose();
            fKernelComposite = null;
        }
    }

    /**
     * Creates the UST composite (if not existing)
     */
    private void createUstComposite() {
        if (fUstComposite == null) {
            fUstComposite = new EnableUstEventsComposite(fDialogComposite, SWT.NONE, fProviderGroup);
            GridLayout layout = new GridLayout(1, true);
            fUstComposite.setLayout(layout);
            fUstComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

            fUstComposite.createContent();
        }
    }

    /**
     * Disposes the UST composite (if existing)
     */
    private void disposeUstComposite() {
        if (fUstComposite != null) {
            fUstComposite.dispose();
            fUstComposite = null;
        }
    }
}
