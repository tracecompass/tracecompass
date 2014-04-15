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
 *   Marc-Andre Laperle - Add filtering textbox
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.KernelProviderComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceControlContentProvider;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceControlLabelProvider;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceProviderGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * <p>
 * A composite for collecting information about kernel events to be enabled.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class EnableKernelEventComposite extends Composite implements IEnableKernelEvents {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private enum KernelGroupEnum { TRACEPOINTS, SYSCALLS, PROBE, FUNCTION }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * A button to enable/disable the tracepoints group
     */
    private Button fTracepointsActivateButton;
    /**
     * A tree viewer for displaying and selection of available tracepoints.
     */
    private CheckboxTreeViewer fTracepointsViewer;
    /**
     * A button to enable/disable the syscalls group
     */
    private Button fSysCallsActivateButton;
    /**
     * A button to enable or disable the dynamic probe group.
     */
    private Button fProbeActivateButton;
    /**
     * The text field for the event name for the dynamic probe.
     */
    private Text fProbeEventNameText;
    /**
     * The text field for the dynamic probe.
     */
    private Text fProbeText;
    /**
     * A button to enable or disable the dynamic function probe group.
     */
    private Button fFunctionActivateButton;
    /**
     * The text field for the event name for the dynamic probe.
     */
    private Text fFunctionEventNameText;
    /**
     * The text field for the dynamic function entry/return probe.
     */
    private Text fFunctionText;
    /**
     * The referenced trace provider group containing the kernel provider
     * component which contains a list of available tracepoints.
     */
    private final TraceProviderGroup fProviderGroup;
    /**
     * The flag indicating that tracepoints are selected.
     */
    private boolean fIsTracepoints;
    /**
     * The flag indicating that all tracepoints are selected.
     */
    private boolean fIsAllTracepoints;
    /**
     * The flag indicating that syscalls are selected.
     */
    private boolean fIsSysCalls;
    /**
     * The list of tracepoints to be enabled.
     */
    private List<String> fSelectedEvents;
    /**
     * The flag indicating that dynamic probe is selected.
     */
    private boolean fIsDynamicProbe;
    /**
     *  The event name of the dynamic probe.
     */
    private String fProbeEventName;
    /**
     * The dynamic probe.
     */
    private String fProbeString;
    /**
     * The flag indicating that the dynamic function probe is selected.
     */
    private boolean fIsDynamicFunctionProbe;
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
     *
     * @param parent
     *            The parent composite
     * @param style
     *            The index of the style for this event composite
     * @param providerGroup
     *            The trace provider group
     */
    public EnableKernelEventComposite(Composite parent, int style, TraceProviderGroup providerGroup) {
        super(parent, style);
        fProviderGroup = providerGroup;
    }

    // ------------------------------------------------------------------------
    // Acessors
    // ------------------------------------------------------------------------

    @Override
    public boolean isTracepoints() {
        return fIsTracepoints;
    }

    @Override
    public boolean isAllTracePoints() {
        return fIsAllTracepoints;
    }

    @Override
    public boolean isSysCalls() {
        return fIsSysCalls;
    }

    @Override
    public boolean isAllSysCalls() {
        return fIsSysCalls;
    }

    @Override
    public List<String> getEventNames() {
        return new ArrayList<>(fSelectedEvents);
    }

    @Override
    public boolean isDynamicProbe() {
        return fIsDynamicProbe;
    }

    @Override
    public String getProbeName() {
        return fProbeString;
    }

    @Override
    public String getProbeEventName() {
        return fProbeEventName;
    }

    @Override
    public boolean isDynamicFunctionProbe() {
        return fIsDynamicFunctionProbe;
    }

    @Override
    public String getFunctionEventName() {
        return fFunctionEventName;
    }

    @Override
    public String getFunction() {
        return fFunctionString;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Creates the composite content
     */
    public void createContent() {

        // Tracepoints Group
        createTracepointsGroup();

        // Syscalls Group
        createSysCallsGroup();

        // Dynamic Probe Group
        createDynamicProbeGroup();

        // Dynamic Function Probe Group
        createDynamicFunctionPropeGroup();

        // Set default enablements
        setKernelEnablements(KernelGroupEnum.TRACEPOINTS);
    }

    /**
     * Validates the kernel composite input data.
     * @return true if configured data is valid and can be retrieved.
     */
    public boolean isValid() {
        fIsTracepoints = fTracepointsActivateButton.getSelection();
        fIsSysCalls = fSysCallsActivateButton.getSelection();
        fIsDynamicProbe = fProbeActivateButton.getSelection();
        fIsDynamicFunctionProbe = fFunctionActivateButton.getSelection();

        // initialize tracepoint fields
        fIsAllTracepoints = false;
        fSelectedEvents = new ArrayList<>();

        if (fIsTracepoints) {
            List<ITraceControlComponent> comps = fProviderGroup.getChildren(KernelProviderComponent.class);
            fIsAllTracepoints = fTracepointsViewer.getChecked(comps.get(0));

            Object[] checkedElements = fTracepointsViewer.getCheckedElements();
            for (int i = 0; i < checkedElements.length; i++) {
                ITraceControlComponent component = (ITraceControlComponent)checkedElements[i];
                if (component instanceof BaseEventComponent) {
                    fSelectedEvents.add(component.getName());
                }
            }
        }

        if (fIsDynamicProbe) {
            String temp = fProbeEventNameText.getText();
            if (temp.isEmpty() ||
                fProbeText.getText().matches("\\s*") || //$NON-NLS-1$
                (!temp.matches("^[\\s]{0,}$") && !temp.matches("^[a-zA-Z0-9\\-\\_]{1,}$"))) { //$NON-NLS-1$ //$NON-NLS-2$
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_EnableEventsDialogTitle,
                        Messages.TraceControl_InvalidProbeNameError + " (" + temp + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$

                return false;
            }

            fProbeEventName = temp;
            // fProbeString will be validated by lttng-tools
            fProbeString = fProbeText.getText();
        }

        // initialize function string
        fFunctionEventName = null;
        fFunctionString = null;
        if (fIsDynamicFunctionProbe) {
            String functionTemp = fFunctionEventNameText.getText();
            if (functionTemp.isEmpty() ||
                functionTemp.matches("\\s*") || //$NON-NLS-1$
                (!functionTemp.matches("^[\\s]{0,}$") && !functionTemp.matches("^[a-zA-Z0-9\\-\\_]{1,}$"))) { //$NON-NLS-1$ //$NON-NLS-2$
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_EnableEventsDialogTitle,
                        Messages.TraceControl_InvalidProbeNameError + " (" + functionTemp + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$

                return false;
            }

            fFunctionEventName = functionTemp;
            // fFunctionString will be validated by lttng-tools
            fFunctionString = fFunctionText.getText();
        }

        return true;
    }

    /**
     * Creates tracepoints group.
     */
    private void createTracepointsGroup() {

        GridLayout layout;
        GridData data;
        Group tpMainGroup = new Group(this, SWT.SHADOW_NONE);
        tpMainGroup.setText(Messages.TraceControl_EnableEventsTracepointGroupName);
        layout = new GridLayout(2, false);
        tpMainGroup.setLayout(layout);
        data = new GridData(GridData.FILL_BOTH);
        tpMainGroup.setLayoutData(data);

        Composite buttonComposite = new Composite(tpMainGroup, SWT.NONE);
        layout = new GridLayout(1, true);
        buttonComposite.setLayout(layout);
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, true);
        buttonComposite.setLayoutData(data);

        fTracepointsActivateButton = new Button(buttonComposite, SWT.RADIO);
        fTracepointsActivateButton.setText(Messages.TraceControl_EnableGroupSelectionName);
        data = new GridData(GridData.FILL_HORIZONTAL);
        fTracepointsActivateButton.setLayoutData(data);
        fTracepointsActivateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setKernelEnablements(KernelGroupEnum.TRACEPOINTS);
            }
        });

        Group tracepointsGroup = new Group(tpMainGroup, SWT.SHADOW_NONE);
        layout = new GridLayout(1, true);
        tracepointsGroup.setLayout(layout);
        data = new GridData(GridData.FILL_BOTH);
        tracepointsGroup.setLayoutData(data);

        new FilteredTree(tracepointsGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), true) {
            @Override
            protected TreeViewer doCreateTreeViewer(Composite aparent, int style) {
                fTracepointsViewer = new CheckboxTreeViewer(aparent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
                fTracepointsViewer.getTree().setToolTipText(Messages.TraceControl_EnableEventsTracepointTreeTooltip);

                fTracepointsViewer.setContentProvider(new KernelContentProvider());
                fTracepointsViewer.setLabelProvider(new KernelLabelProvider());
                fTracepointsViewer.addCheckStateListener(new KernelCheckListener());
                fTracepointsViewer.setInput(fProviderGroup);

                fTracepointsViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
                return fTracepointsViewer;
            }
        };
    }

    /**
     * Creates syscalls group.
     */
    private void createSysCallsGroup() {
        GridLayout layout;
        GridData data;
        Group sysCallsMainGroup = new Group(this, SWT.SHADOW_NONE);
        sysCallsMainGroup.setText(Messages.TraceControl_EnableEventsSyscallName);
        sysCallsMainGroup.setToolTipText(Messages.TraceControl_EnableEventsSyscallTooltip);
        layout = new GridLayout(2, false);
        sysCallsMainGroup.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        sysCallsMainGroup.setLayoutData(data);

        Composite buttonComposite = new Composite(sysCallsMainGroup, SWT.NONE);
        layout = new GridLayout(1, false);
        buttonComposite.setLayout(layout);
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, true);
        buttonComposite.setLayoutData(data);

        fSysCallsActivateButton = new Button(buttonComposite, SWT.RADIO);
        fSysCallsActivateButton.setText(Messages.TraceControl_EnableGroupSelectionName);
        fSysCallsActivateButton.setToolTipText(Messages.TraceControl_EnableEventsSyscallTooltip);
        fSysCallsActivateButton.setSelection(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        fSysCallsActivateButton.setLayoutData(data);
        fSysCallsActivateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setKernelEnablements(KernelGroupEnum.SYSCALLS);
            }
        });
    }

    /**
     * Creates dynamic probe group.
     */
    private void createDynamicProbeGroup() {
        GridLayout layout;
        GridData data;
        Group probeMainGroup = new Group(this, SWT.SHADOW_NONE);
        probeMainGroup.setText(Messages.TraceControl_EnableEventsProbeGroupName);
        layout = new GridLayout(2, false);
        probeMainGroup.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        probeMainGroup.setLayoutData(data);

        Composite buttonComposite = new Composite(probeMainGroup, SWT.NONE);
        layout = new GridLayout(1, false);
        buttonComposite.setLayout(layout);
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, true);
        buttonComposite.setLayoutData(data);

        fProbeActivateButton = new Button(buttonComposite, SWT.RADIO);
        fProbeActivateButton.setText(Messages.TraceControl_EnableGroupSelectionName);
        fProbeActivateButton.setSelection(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        fProbeActivateButton.setLayoutData(data);
        fProbeActivateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setKernelEnablements(KernelGroupEnum.PROBE);
            }
        });

        Group probeGroup = new Group(probeMainGroup, SWT.SHADOW_NONE);
        layout = new GridLayout(4, true);
        probeGroup.setLayout(layout);
        probeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label probeNameLabel = new Label(probeGroup, SWT.LEFT);
        probeNameLabel.setText(Messages.TraceControl_EnableEventsEventNameLabel);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 1;
        probeNameLabel.setLayoutData(data);

        fProbeEventNameText = new Text(probeGroup, SWT.LEFT);
        fProbeEventNameText.setToolTipText(Messages.TraceControl_EnableEventsProbeEventNameTooltip);

        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        fProbeEventNameText.setLayoutData(data);

        Label probeLabel = new Label(probeGroup, SWT.LEFT);
        probeLabel.setText(Messages.TraceControl_EnableEventsProbeNameLabel);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 1;
        probeLabel.setLayoutData(data);

        fProbeText = new Text(probeGroup, SWT.LEFT);
        fProbeText.setToolTipText(Messages.TraceControl_EnableEventsProbeNameTooltip);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        fProbeText.setLayoutData(data);
    }

    /**
     * Creates dynamic function entry/return probe group.
     */
    private void createDynamicFunctionPropeGroup() {
        GridLayout layout;
        GridData data;
        Group functionMainGroup = new Group(this, SWT.SHADOW_NONE);
        functionMainGroup.setText(Messages.TraceControl_EnableEventsFucntionGroupName);
        layout = new GridLayout(2, false);
        functionMainGroup.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        functionMainGroup.setLayoutData(data);

        Composite buttonComposite = new Composite(functionMainGroup, SWT.NONE);
        layout = new GridLayout(1, false);
        buttonComposite.setLayout(layout);
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, true);
        buttonComposite.setLayoutData(data);

        fFunctionActivateButton = new Button(buttonComposite, SWT.RADIO);
        fFunctionActivateButton.setText(Messages.TraceControl_EnableGroupSelectionName);
        fFunctionActivateButton.setSelection(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        fFunctionActivateButton.setLayoutData(data);
        fFunctionActivateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setKernelEnablements(KernelGroupEnum.FUNCTION);
            }
        });

        Group functionGroup = new Group(functionMainGroup, SWT.SHADOW_NONE);
        layout = new GridLayout(4, true);
        functionGroup.setLayout(layout);
        functionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label functionNameLabel = new Label(functionGroup, SWT.LEFT);
        functionNameLabel.setText(Messages.TraceControl_EnableEventsEventNameLabel);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 1;
        functionNameLabel.setLayoutData(data);

        fFunctionEventNameText = new Text(functionGroup, SWT.LEFT);
        fFunctionEventNameText.setToolTipText(Messages.TraceControl_EnableEventsFunctionEventNameTooltip);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        fFunctionEventNameText.setLayoutData(data);

        Label functionLabel = new Label(functionGroup, SWT.LEFT);
        functionLabel.setText(Messages.TraceControl_EnableEventsFunctionNameLabel);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 1;
        functionLabel.setLayoutData(data);

        fFunctionText = new Text(functionGroup, SWT.LEFT);
        fFunctionText.setToolTipText(Messages.TraceControl_EnableEventsProbeNameTooltip);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        fFunctionText.setLayoutData(data);
    }

    /**
     * Enable/selects widgets depending on the group specified.
     * @param group - group to enable.
     */
    private void setKernelEnablements(KernelGroupEnum group) {
        fTracepointsActivateButton.setSelection(group == KernelGroupEnum.TRACEPOINTS);
        fTracepointsViewer.getTree().setEnabled(group == KernelGroupEnum.TRACEPOINTS);

        fSysCallsActivateButton.setSelection(group == KernelGroupEnum.SYSCALLS);

        fProbeActivateButton.setSelection(group == KernelGroupEnum.PROBE);
        fProbeEventNameText.setEnabled(group == KernelGroupEnum.PROBE);
        fProbeText.setEnabled(group == KernelGroupEnum.PROBE);

        fFunctionActivateButton.setSelection(group == KernelGroupEnum.FUNCTION);
        fFunctionEventNameText.setEnabled(group == KernelGroupEnum.FUNCTION);
        fFunctionText.setEnabled(group == KernelGroupEnum.FUNCTION);
    }

    // ------------------------------------------------------------------------
    // Local classes
    // ------------------------------------------------------------------------
    /**
     * Content provider for the tracepoints tree.
     */
    public static final class KernelContentProvider extends TraceControlContentProvider {
        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof TraceProviderGroup) {
                List<ITraceControlComponent> children = ((ITraceControlComponent)parentElement).getChildren(KernelProviderComponent.class);
                return children.toArray(new ITraceControlComponent[children.size()]);
            }
            if (parentElement instanceof ITraceControlComponent) {
                return ((ITraceControlComponent)parentElement).getChildren();
            }
            return new Object[0];
        }
    }

    /**
     * Content label for the tracepoints tree.
     */
    public static final class KernelLabelProvider extends TraceControlLabelProvider {
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
    }

    /**
     * Check state listener for the tracepoints tree.
     */
    public final class KernelCheckListener implements ICheckStateListener {
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
    }
}
