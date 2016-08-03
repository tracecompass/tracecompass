/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceProviderGroup;

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
     * The composite with widgets for collecting information about JUL events.
     */
    private EnableLoggersComposite fJulComposite;
    /**
     * The composite with widgets for collecting information about LOG4J events.
     */
    private EnableLoggersComposite fLog4jComposite;
    /**
     * The composite with widgets for collecting information about Python events.
     */
    private EnableLoggersComposite fPythonComposite;
    /**
     * Radio button for selecting kernel domain.
     */
    private Button fKernelButton;
    /**
     * Radio button for selecting UST domain.
     */
    private Button fUstButton;
    /**
     * Radio button for selecting JUL domain.
     */
    private Button fJulButton;
    /**
     * Radio button for selecting LOG4J domain.
     */
    private Button fLog4jButton;
    /**
     * Radio button for selecting Python domain.
     */
    private Button fPythonButton;
    /**
     * The referenced trace provider group containing the kernel provider and UST
     * provider component which contains a list of available tracepoints.
     */
    private TraceProviderGroup fProviderGroup;
    /**
     * The parent domain component where the channel node should be added.
     * Null in case the domain is not known (i.e. on session level).
     */
    private TraceDomainComponent fDomainComponent;
    /**
     * The domain type ({@link TraceDomainType})
     */
    private TraceDomainType fDomain;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public EnableEventsDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    @Override
    public boolean isAllEvents() {
        switch (fDomain) {
        case KERNEL:
            return fKernelComposite.isAllEvents();
        case JUL:
            return fJulComposite.isAllTracePoints();
        case UST:
            return fUstComposite.isAllTracePoints();
        case LOG4J:
            return fLog4jComposite.isAllTracePoints();
        case PYTHON:
            return fPythonComposite.isAllTracePoints();
        case UNKNOWN:
        default:
            return false;
        }
    }

    @Override
    public boolean isTracepoints() {
        switch (fDomain) {
        case JUL:
        case LOG4J:
        case PYTHON:
            // Loggers are always TRACEPOINT
            return true;
        case KERNEL:
            return fKernelComposite.isTracepoints();
        case UST:
            return fUstComposite.isTracepoints();
        case UNKNOWN:
        default:
            return false;
        }
    }

    @Override
    public boolean isAllTracePoints() {
        switch (fDomain) {
        case KERNEL:
            return fKernelComposite.isAllTracePoints();
        case UST:
            return fUstComposite.isAllTracePoints();
        case JUL:
            return fJulComposite.isAllTracePoints();
        case LOG4J:
            return fLog4jComposite.isAllTracePoints();
        case PYTHON:
            return fPythonComposite.isAllTracePoints();
        case UNKNOWN:
        default:
            return false;
        }
    }

    @Override
    public boolean isSyscalls() {
        if (fDomain.equals(TraceDomainType.KERNEL)) {
            return fKernelComposite.isSyscalls();
        }
        return false;
    }

    @Override
    public boolean isAllSyscalls() {
        if (fDomain.equals(TraceDomainType.KERNEL)) {
            return fKernelComposite.isAllSyscalls();
        }
        return false;
    }

    @Override
    public List<String> getEventNames() {
        switch (fDomain) {
        case JUL:
            return fJulComposite.getEventNames();
        case KERNEL:
            return fKernelComposite.getEventNames();
        case UST:
            return fUstComposite.getEventNames();
        case LOG4J:
            return fLog4jComposite.getEventNames();
        case PYTHON:
            return fPythonComposite.getEventNames();
        case UNKNOWN:
        default:
            return null;
        }
    }

    @Override
    public boolean isDynamicProbe() {
        if (fDomain.equals(TraceDomainType.KERNEL)) {
            return fKernelComposite.isDynamicProbe();
        }
        return false;
    }

    @Override
    public String getProbeName() {
        if (fDomain.equals(TraceDomainType.KERNEL)) {
            return fKernelComposite.getProbeName();
        }
        return null;
    }

    @Override
    public String getProbeEventName() {
        if (fDomain.equals(TraceDomainType.KERNEL)) {
            return fKernelComposite.getProbeEventName();
        }
        return null;
    }

    @Override
    public boolean isDynamicFunctionProbe() {
        if (fDomain.equals(TraceDomainType.KERNEL)) {
            return fKernelComposite.isDynamicFunctionProbe();
        }
        return false;
    }

    @Override
    public String getFunctionEventName() {
        if (fDomain.equals(TraceDomainType.KERNEL)) {
            return fKernelComposite.getFunctionEventName();
        }
        return null;
    }

    @Override
    public String getFunction() {
        if (fDomain.equals(TraceDomainType.KERNEL)) {
            return fKernelComposite.getFunction();
        }
        return null;
    }

    @Override
    public boolean isWildcard() {
        if (fDomain.equals(TraceDomainType.UST)) {
            return fUstComposite.isWildcard();
        }
        return false;
    }

    @Override
    public String getWildcard() {
        if (fDomain.equals(TraceDomainType.UST)) {
            return fUstComposite.getWildcard();
        }
        return null;
    }

    @Override
    public boolean isLogLevel() {
        switch (fDomain) {
        case JUL:
            return fJulComposite.isLogLevel();
        case KERNEL:
            return false;
        case UST:
            return fUstComposite.isLogLevel();
        case LOG4J:
            return fLog4jComposite.isLogLevel();
        case PYTHON:
            return fPythonComposite.isLogLevel();
        case UNKNOWN:
        default:
            return false;
        }
    }

    @Override
    public LogLevelType getLogLevelType() {
        switch (fDomain) {
        case JUL:
            return fJulComposite.getLogLevelType();
        case KERNEL:
            return null;
        case UST:
            return fUstComposite.getLogLevelType();
        case LOG4J:
            return fLog4jComposite.getLogLevelType();
        case PYTHON:
            return fPythonComposite.getLogLevelType();
        case UNKNOWN:
        default:
            return null;
        }
    }

    @Override
    public ITraceLogLevel getLogLevel() {
        switch (fDomain) {
        case JUL:
            return fJulComposite.getLogLevel();
        case KERNEL:
            return null;
        case UST:
            return fUstComposite.getLogLevel();
        case LOG4J:
            return fLog4jComposite.getLogLevel();
        case PYTHON:
            return fPythonComposite.getLogLevel();
        case UNKNOWN:
        default:
            return null;
        }
    }

    @Override
    public TraceDomainType getDomain() {
        return fDomain;
    }

    @Override
    public void setTraceProviderGroup(TraceProviderGroup providerGroup) {
        fProviderGroup = providerGroup;
    }

    @Override
    public void setTraceDomainComponent(TraceDomainComponent domain) {
        fDomainComponent = domain;
        if (fDomainComponent != null) {
            fDomain = fDomainComponent.getDomain();
        } else {
            if (fProviderGroup != null) {
                fDomain = fProviderGroup.hasKernelProvider() ? TraceDomainType.KERNEL : TraceDomainType.UST;
            }
        }
    }

    @Override
    public String getFilterExpression() {

        switch (fDomain) {
        case KERNEL:
            return fKernelComposite.getFilterExpression();
        case UST:
            return fUstComposite.getFilterExpression();
        case JUL:
        case LOG4J:
        case PYTHON:
        case UNKNOWN:
        default:
            return null;
        }
    }

    @Override
    public List<String> getExcludedEvents() {
        if (fDomain.equals(TraceDomainType.UST)) {
            return fUstComposite.getExcludedEvents();
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_EnableEventsDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(ENABLE_EVENT_ICON_FILE));
    }

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
        layout = new GridLayout(5, true);
        domainGroup.setLayout(layout);

        fKernelButton = new Button(domainGroup, SWT.RADIO);
        fKernelButton.setText(Messages.TraceControl_KernelDomainDisplayName);
        fUstButton = new Button(domainGroup, SWT.RADIO);
        fUstButton.setText(Messages.TraceControl_UstDisplayName);
        fJulButton = new Button(domainGroup, SWT.RADIO);
        fJulButton.setText(Messages.TraceControl_JULDomainDisplayName);
        fLog4jButton = new Button(domainGroup, SWT.RADIO);
        fLog4jButton.setText(Messages.TraceControl_LOG4JDomainDisplayName);
        fPythonButton = new Button(domainGroup, SWT.RADIO);
        fPythonButton.setText(Messages.TraceControl_PythonDomainDisplayName);

        switch (fDomain) {
        case KERNEL:
            fKernelButton.setSelection(true);
            break;
        case UST:
            fUstButton.setSelection(true);
            break;
        case JUL:
            fJulButton.setSelection(true);
            break;
        case LOG4J:
            fLog4jButton.setSelection(true);
            break;
        case PYTHON:
            fPythonButton.setSelection(true);
            break;
        case UNKNOWN:
        default:
            break;
        }

        if (fDomainComponent != null) {
            fKernelButton.setEnabled(false);
            fUstButton.setEnabled(false);
            fJulButton.setEnabled(false);
            fLog4jButton.setEnabled(false);
            fPythonButton.setEnabled(false);
        } else if ((fProviderGroup != null) && (!fProviderGroup.hasKernelProvider())) {
            fKernelButton.setEnabled(false);
            fUstButton.setEnabled(true);
            fJulButton.setEnabled(true);
            fLog4jButton.setEnabled(true);
            fPythonButton.setEnabled(true);
        }

        // layout widgets
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        domainGroup.setLayoutData(data);

        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fKernelButton.setLayoutData(data);
        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fUstButton.setLayoutData(data);

        // ------------------------------------------------------------------------
        // Domain data group
        // ------------------------------------------------------------------------
        fUstComposite = null;
        fKernelComposite = null;
        fJulComposite = null;
        fLog4jComposite = null;
        fPythonComposite= null;

        switch (fDomain) {
        case KERNEL:
            createKernelComposite();
            break;
        case UST:
            createUstComposite();
            break;
        case JUL:
            createJulComposite();
            break;
        case LOG4J:
            createLog4jComposite();
            break;
        case PYTHON:
            createPythonComposite();
            break;
        case UNKNOWN:
        default:
            break;
        }

        fKernelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fKernelButton.getSelection()) {
                    disposeAllComposite();
                    createKernelComposite();
                    fDialogComposite.layout();
                }
            }
        });

        fUstButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fUstButton.getSelection()) {
                    disposeAllComposite();
                    createUstComposite();
                    fDialogComposite.layout();
                }
            }
        });

        fJulButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fJulButton.getSelection()) {
                    disposeAllComposite();
                    createJulComposite();
                    fDialogComposite.layout();
                }
            }
        });

        fLog4jButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fLog4jButton.getSelection()) {
                    disposeAllComposite();
                    createLog4jComposite();
                    fDialogComposite.layout();
                }
            }
        });

        fPythonButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fPythonButton.getSelection()) {
                    disposeAllComposite();
                    createPythonComposite();
                    fDialogComposite.layout();
                }
            }
        });

    getShell().setMinimumSize(new Point(550, 850));

        return fDialogComposite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {

        if (fKernelButton.getSelection()) {
            fDomain = TraceDomainType.KERNEL;
        } else if (fUstButton.getSelection()){
            fDomain = TraceDomainType.UST;
        } else if (fJulButton.getSelection()) {
            fDomain = TraceDomainType.JUL;
        } else if (fLog4jButton.getSelection()) {
            fDomain= TraceDomainType.LOG4J;
        } else if (fPythonButton.getSelection()) {
            fDomain = TraceDomainType.PYTHON;
        }

        // Validate kernel composite in case of kernel domain
        if (fKernelComposite != null && !fKernelComposite.isValid()) {
            return;
        }

        // Validate UST composite in case of UST domain
        if (fUstComposite != null && !fUstComposite.isValid()) {
            return;
        }

        // Validate JUL composite in case of JUL domain
        if (fJulComposite != null && !fJulComposite.isValid()) {
            return;
        }

        // Validate LOG4J composite in case of LOG4J domain
        if (fLog4jComposite != null && !fLog4jComposite.isValid()) {
            return;
        }

        // Validate Python composite in case of Python domain
        if (fPythonComposite != null && !fPythonComposite.isValid()) {
            return;
        }

        // validation successful -> call super.okPressed()
        super.okPressed();
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Disposes all composites (if existing)
     */
    private void disposeAllComposite() {
        disposeKernelComposite();
        disposeUstComposite();
        disposeJulComposite();
        disposeLog4jComposite();
        disposePythonComposite();
    }

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

    /**
     * Creates the JUL composite (if not existing)
     */
    private void createJulComposite() {
        if (fJulComposite == null) {
            fJulComposite = new EnableLoggersComposite(fDialogComposite, SWT.NONE, fProviderGroup, TraceDomainType.JUL);
            GridLayout layout = new GridLayout(1, true);
            fJulComposite.setLayout(layout);
            fJulComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

            fJulComposite.createContent();
        }
    }

    /**
     * Disposes the JUL composite (if existing)
     */
    private void disposeJulComposite() {
        if (fJulComposite != null) {
            fJulComposite.dispose();
            fJulComposite = null;
        }
    }

    /**
     * Creates the LOG4J composite (if not existing)
     */
    private void createLog4jComposite() {
        if (fLog4jComposite == null) {
            fLog4jComposite = new EnableLoggersComposite(fDialogComposite, SWT.NONE, fProviderGroup, TraceDomainType.LOG4J);
            GridLayout layout = new GridLayout(1, true);
            fLog4jComposite.setLayout(layout);
            fLog4jComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

            fLog4jComposite.createContent();
        }
    }

    /**
     * Disposes the LOG4J composite (if existing)
     */
    private void disposeLog4jComposite() {
        if (fLog4jComposite != null) {
            fLog4jComposite.dispose();
            fLog4jComposite = null;
        }
    }

    /**
     * Creates the Python composite (if not existing)
     */
    private void createPythonComposite() {
        if (fPythonComposite == null) {
            fPythonComposite = new EnableLoggersComposite(fDialogComposite, SWT.NONE, fProviderGroup, TraceDomainType.PYTHON);
            GridLayout layout = new GridLayout(1, true);
            fPythonComposite.setLayout(layout);
            fPythonComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

            fPythonComposite.createContent();
        }
    }

    /**
     * Disposes the Python composite (if existing)
     */
    private void disposePythonComposite() {
        if (fPythonComposite != null) {
            fPythonComposite.dispose();
            fPythonComposite = null;
        }
    }
}
