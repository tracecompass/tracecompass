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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceControlContentProvider;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceControlLabelProvider;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceProviderGroup;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.UstProviderComponent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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

/**
 * <p>
 * A composite for collecting information about UST events to be enabled.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class EnableUstEventsComposite extends Composite implements IEnableUstEvents  {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private enum GroupEnum { TRACEPOINTS, WILDCARD, LOGLEVEL }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * A button to enable/disable the tracepoints group
     */
    private Button fTracepointsActivateButton;
    /**
     * A tree viewer for diplaying and selection of available tracepoints.
     */
    private CheckboxTreeViewer fTracepointsViewer;
    /**
     * A button to enable/disable the wildcard group
     */
    private Button fWildcardActivateButton;
    /**
     * A Text field for the event's wildcard.
     */
    private Text fWildcardText;
    /**
     * A button to enable/disable the log level group
     */
    private Button fLogLevelActivateButton;
    /**
     * A Text field for the event name for the log level enablement.
     */
    private Text fLogLevelEventNameText;
    /**
     * A Combo box for selecting the log level.
     */
    private CCombo fLogLevelCombo;
    /**
     * A button for selecting the log level (range 0 to level).
     */
    private Button fLogLevelButton;
    /**
     * A button for selecting the specified log level only.
     */
    private Button fLogLevelOnlyButton;
    /**
     * The filter text
     */
    private Text fFilterText;

    /**
     * The referenced trace provider group containing the UST providers
     * component which contains a list of available tracepoints.
     */
    private final TraceProviderGroup fProviderGroup;
    /**
     * The flag indicating that tracepoints are selected.
     */
    private boolean fIsTracepoints;
    /**
     * The flag indicating that all tracepoints (across providers) are selected.
     */
    private boolean fIsAllTracepoints;
    /**
     * The list of tracepoints to be enabled.
     */
    private List<String> fSelectedEvents;
    /**
     * The flag indicating that all wildcard are selected..
     */
    private boolean fIsWildcard;
    /**
     * The wildcard if wildcard is selected.
     */
    private String fWildcard;
    /**
     *The flag indicating that all log level are selected.
     */
    private boolean fIsLogLevel;
    /**
     * The event name for the log level enablement.
     */
    private String fLogLevelEventName;
    /**
     * The type of the log level (loglevel or loglevel-only)
     */
    private LogLevelType fLogLevelType;
    /**
     * The actual selected log level.
     */
    private TraceLogLevel fLogLevel;
    /**
     * The filter expression
     */
    private String fFilterExpression;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param parent - a parent composite
     * @param style - a composite style
     * @param providerGroup - the trace provider group
     */
    public EnableUstEventsComposite(Composite parent, int style, TraceProviderGroup providerGroup) {
        super(parent, style);
        fProviderGroup = providerGroup;
    }

    // ------------------------------------------------------------------------
    // Accessors
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
    public List<String> getEventNames() {
        return new ArrayList<>(fSelectedEvents);
    }

    @Override
    public boolean isWildcard() {
        return fIsWildcard;
    }

    @Override
    public String getWildcard() {
        return fWildcard;
    }

    @Override
    public boolean isLogLevel() {
        return fIsLogLevel;
    }

    @Override
    public LogLevelType getLogLevelType() {
        return fLogLevelType;
    }

    @Override
    public TraceLogLevel getLogLevel() {
        return fLogLevel;
    }

    @Override
    public String getLogLevelEventName() {
        return fLogLevelEventName;
    }

    @Override
    public String getFilterExpression() {
        return fFilterExpression;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create the contents of this event composite
     */
    public void createContent() {

        // Tracepoints Group
        createTracepointsGroup();

        // Wildcard Group
        createWildCardGroup();

        // Log Level Group
        createLogLevelGroup();

        // Filter Group
        createFilterGroup();

        // Set default enablements
        setEnablements(GroupEnum.TRACEPOINTS);
    }

    /**
     * Validates the UST composite input data.
     *
     * @return true if configured data is valid and can be retrieved.
     */
    public boolean isValid() {

        fIsTracepoints = fTracepointsActivateButton.getSelection();
        fIsWildcard = fWildcardActivateButton.getSelection();
        fIsLogLevel = fLogLevelActivateButton.getSelection();

        // initialize tracepoint fields
        fIsAllTracepoints = false;
        fSelectedEvents = new ArrayList<>();
        if (fIsTracepoints) {
            fIsAllTracepoints = fTracepointsViewer.getChecked(fProviderGroup);
            Set<String> set = new HashSet<>();
            Object[] checkedElements = fTracepointsViewer.getCheckedElements();
            for (int i = 0; i < checkedElements.length; i++) {
                ITraceControlComponent component = (ITraceControlComponent)checkedElements[i];
                if (!set.contains(component.getName()) && (component instanceof BaseEventComponent)) {
                    set.add(component.getName());
                    fSelectedEvents.add(component.getName());
                }
            }
        }

        // initialize log level event name string
        fLogLevelType = LogLevelType.LOGLEVEL_NONE;
        fLogLevelEventName = null;

        if (fIsLogLevel) {
            if (fLogLevelButton.getSelection()) {
                fLogLevelType = LogLevelType.LOGLEVEL;
            } else if (fLogLevelOnlyButton.getSelection()) {
                fLogLevelType = LogLevelType.LOGLEVEL_ONLY;
            }

            String temp = fLogLevelEventNameText.getText();
            if (temp.isEmpty() ||
                temp.matches("\\s*") || //$NON-NLS-1$
                (!temp.matches("^[\\s]{0,}$") && !temp.matches("^[a-zA-Z0-9\\-\\_]{1,}$"))) { //$NON-NLS-1$ //$NON-NLS-2$
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_EnableEventsDialogTitle,
                        Messages.TraceControl_InvalidLogLevelEventNameError + " (" + temp + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$

                return false;
            }

            fLogLevelEventName = temp;

            TraceLogLevel[] levels = TraceLogLevel.values();
            int id = fLogLevelCombo.getSelectionIndex();

            if (id < 0) {
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_EnableEventsDialogTitle,
                        Messages.TraceControl_InvalidLogLevel + " (" + temp + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$

                return false;
            }
            fLogLevel = levels[id];
        }

        // initialize wildcard with null
        fWildcard = null;
        if (fIsWildcard) {
            String tempWildcard = fWildcardText.getText();
            if (tempWildcard.isEmpty() ||
                tempWildcard.matches("\\s*") || //$NON-NLS-1$
                (!tempWildcard.matches("^[\\s]{0,}$") && !tempWildcard.matches("^[a-zA-Z0-9\\-\\_\\*]{1,}$"))) { //$NON-NLS-1$ //$NON-NLS-2$
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_EnableEventsDialogTitle,
                        Messages.TraceControl_InvalidWildcardError + " (" + tempWildcard + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$

                return false;
            }

            fWildcard = tempWildcard;
        }

        // initialize filter with null
        fFilterExpression = null;
        if (fProviderGroup.isEventFilteringSupported()) {
            String tempFilter = fFilterText.getText();

            if(!tempFilter.isEmpty() && !tempFilter.matches("\\s*")) { //$NON-NLS-1$
                fFilterExpression = tempFilter;
            }
        }

        // validation successful -> call super.okPressed()
        return true;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Creates tracepoints group.
     */
    private void createTracepointsGroup() {
        Group tpMainGroup = new Group(this, SWT.SHADOW_NONE);
        tpMainGroup.setText(Messages.TraceControl_EnableEventsTracepointGroupName);
        GridLayout layout = new GridLayout(2, false);
        tpMainGroup.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
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
                setEnablements(GroupEnum.TRACEPOINTS);
            }
        });

        Group tpGroup = new Group(tpMainGroup, SWT.SHADOW_NONE);
        layout = new GridLayout(1, true);
        tpGroup.setLayout(layout);
        data = new GridData(GridData.FILL_BOTH);
        tpGroup.setLayoutData(data);

        fTracepointsViewer = new CheckboxTreeViewer(tpGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        fTracepointsViewer.getTree().setToolTipText(Messages.TraceControl_EnableEventsTracepointTreeTooltip);
        fTracepointsViewer.setContentProvider(new UstContentProvider());

        fTracepointsViewer.setLabelProvider(new UstLabelProvider());
        fTracepointsViewer.addCheckStateListener(new UstCheckStateListener());

        fTracepointsViewer.setInput(fProviderGroup.getParent());
        fTracepointsViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    /**
     * Creates wildcard group.
     */
    private void createWildCardGroup() {
        Group wildcardMainGroup = new Group(this, SWT.SHADOW_NONE);
        wildcardMainGroup.setText(Messages.TraceControl_EnableEventsWildcardGroupName);
        GridLayout layout = new GridLayout(2, false);
        wildcardMainGroup.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        wildcardMainGroup.setLayoutData(data);

        Composite buttonComposite = new Composite(wildcardMainGroup, SWT.NONE);
        layout = new GridLayout(1, false);
        buttonComposite.setLayout(layout);
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, true);
        buttonComposite.setLayoutData(data);

        fWildcardActivateButton = new Button(buttonComposite, SWT.RADIO);
        fWildcardActivateButton.setText(Messages.TraceControl_EnableGroupSelectionName);
        fWildcardActivateButton.setSelection(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        fWildcardActivateButton.setLayoutData(data);
        fWildcardActivateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setEnablements(GroupEnum.WILDCARD);
            }
        });

        Group wildcardGroup = new Group(wildcardMainGroup, SWT.SHADOW_NONE);
        layout = new GridLayout(3, true);
        wildcardGroup.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        wildcardGroup.setLayoutData(data);

        Label wildcardLabel = new Label(wildcardGroup, SWT.LEFT);
        wildcardLabel.setText(Messages.TraceControl_EnableEventsWildcardLabel);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        wildcardLabel.setLayoutData(data);

        fWildcardText = new Text(wildcardGroup, SWT.LEFT);
        fWildcardText.setToolTipText(Messages.TraceControl_EnableEventsWildcardTooltip);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        fWildcardText.setLayoutData(data);
    }

    /**
     * Creates log level group.
     */
    private void createLogLevelGroup() {
        Group logLevelMainGroup = new Group(this, SWT.SHADOW_NONE);
        logLevelMainGroup.setText(Messages.TraceControl_EnableEventsLogLevelGroupName);
        GridLayout layout = new GridLayout(2, false);
        logLevelMainGroup.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        logLevelMainGroup.setLayoutData(data);

        Composite buttonComposite = new Composite(logLevelMainGroup, SWT.NONE);
        layout = new GridLayout(1, false);
        buttonComposite.setLayout(layout);
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, true);
        buttonComposite.setLayoutData(data);

        fLogLevelActivateButton = new Button(buttonComposite, SWT.RADIO);
        fLogLevelActivateButton.setText(Messages.TraceControl_EnableGroupSelectionName);
        fLogLevelActivateButton.setSelection(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        fLogLevelActivateButton.setLayoutData(data);
        fLogLevelActivateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setEnablements(GroupEnum.LOGLEVEL);
            }
        });

        Group logLevelGroup = new Group(logLevelMainGroup, SWT.SHADOW_NONE);
        layout = new GridLayout(3, true);
        logLevelGroup.setLayout(layout);
        logLevelGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label logLevelEventNameLabel = new Label(logLevelGroup, SWT.LEFT);
        logLevelEventNameLabel.setText(Messages.TraceControl_EnableEventsEventNameLabel);

        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 1;
        logLevelEventNameLabel.setLayoutData(data);

        fLogLevelEventNameText = new Text(logLevelGroup, SWT.LEFT);
        fLogLevelEventNameText.setToolTipText(Messages.TraceControl_EnableEventsLoglevelEventNameTooltip);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        fLogLevelEventNameText.setLayoutData(data);

        TraceLogLevel[] levels = TraceLogLevel.values();

        String[] levelNames = new String[levels.length - 1];
        int k = 0;
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] != TraceLogLevel.LEVEL_UNKNOWN) {
                levelNames[k++] = levels[i].getInName();
            }
        }

        fLogLevelCombo = new CCombo(logLevelGroup, SWT.READ_ONLY);
        fLogLevelCombo.setItems(levelNames);
        fLogLevelCombo.setToolTipText(Messages.TraceControl_EnableEventsLogLevelTooltip);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 4;
        fLogLevelCombo.setLayoutData(data);

        fLogLevelButton = new Button(logLevelGroup, SWT.RADIO);
        fLogLevelButton.setText(Messages.TraceControl_EnableEventsLogLevelTypeName);
        fLogLevelButton.setToolTipText(Messages.TraceControl_EnableEventsLogLevelTypeTooltip);
        data = new GridData(GridData.FILL_BOTH);
        fLogLevelButton.setLayoutData(data);
        fLogLevelButton.setSelection(true);

        fLogLevelOnlyButton = new Button(logLevelGroup, SWT.RADIO);
        fLogLevelOnlyButton.setText(Messages.TraceControl_EnableEventsLogLevelOnlyTypeName);
        fLogLevelOnlyButton.setToolTipText(Messages.TraceControl_EnableEventsLogLevelOnlyTypeTooltip);
        data = new GridData(GridData.FILL_BOTH);
        fLogLevelButton.setLayoutData(data);
    }

    void createFilterGroup() {
        if (fProviderGroup.isEventFilteringSupported()) {
            Group filterMainGroup = new Group(this, SWT.SHADOW_NONE);
            filterMainGroup.setText(Messages.TraceControl_EnableEventsFilterGroupName);
            GridLayout layout = new GridLayout(3, false);
            filterMainGroup.setLayout(layout);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            filterMainGroup.setLayoutData(data);

            fFilterText = new Text(filterMainGroup, SWT.LEFT);
            fFilterText.setToolTipText(Messages.TraceControl_EnableEventsFilterTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            fFilterText.setLayoutData(data);
        }
    }

    /**
     * Enable/selects widgets depending on the group specified.
     * @param group - group to enable.
     */
    private void setEnablements(GroupEnum group) {

        // Enable/disable trace point items
        fTracepointsActivateButton.setSelection(group == GroupEnum.TRACEPOINTS);
        fTracepointsViewer.getTree().setEnabled(group == GroupEnum.TRACEPOINTS);

        // Enable/disable wildcard items
        fWildcardActivateButton.setSelection(group == GroupEnum.WILDCARD);
        fWildcardText.setEnabled(group == GroupEnum.WILDCARD);

        // Enable/disable log level items
        fLogLevelActivateButton.setSelection(group == GroupEnum.LOGLEVEL);
        fLogLevelEventNameText.setEnabled(group == GroupEnum.LOGLEVEL);
        fLogLevelCombo.setEnabled(group == GroupEnum.LOGLEVEL);
        fLogLevelButton.setEnabled(group == GroupEnum.LOGLEVEL);
        fLogLevelOnlyButton.setEnabled(group == GroupEnum.LOGLEVEL);
    }

    // ------------------------------------------------------------------------
    // Local classes
    // ------------------------------------------------------------------------
    /**
     * Content provider for the tracepoints tree.
     */
    public static final class UstContentProvider extends TraceControlContentProvider {
        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof TargetNodeComponent) {
                List<ITraceControlComponent> children = ((ITraceControlComponent)parentElement).getChildren(TraceProviderGroup.class);
                return children.toArray(new ITraceControlComponent[children.size()]);
            }
            if (parentElement instanceof TraceProviderGroup) {
                List<ITraceControlComponent> children = ((ITraceControlComponent)parentElement).getChildren(UstProviderComponent.class);
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
    public static final class UstLabelProvider extends TraceControlLabelProvider {
        @Override
        public Image getImage(Object element) {
            return null;
        }
        @Override
        public String getText(Object element) {
            if ((element != null) && (element instanceof TraceProviderGroup)) {
                return Messages.TraceControl_EnableEventsTracepointTreeAllLabel;
            }

            if ((element != null) && (element instanceof UstProviderComponent)) {
                return Messages.TraceControl_EnableEventsTracepointTreeAllLabel + " - " + ((UstProviderComponent)element).getName(); //$NON-NLS-1$
            }
            return super.getText(element);
        }
    }

    /**
     * Check state listener for the tracepoints tree.
     */
    public final class UstCheckStateListener implements ICheckStateListener {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            if (event.getChecked()) {
                if (event.getElement() instanceof TraceProviderGroup) {
                    fTracepointsViewer.setSubtreeChecked(event.getElement(), true);
                }
                if (event.getElement() instanceof UstProviderComponent) {
                    fTracepointsViewer.setSubtreeChecked(event.getElement(), true);
                }
            } else {
                if (event.getElement() instanceof TraceProviderGroup) {
                    fTracepointsViewer.setSubtreeChecked(event.getElement(), true);
                }
                if (event.getElement() instanceof UstProviderComponent) {
                    ITraceControlComponent component = (ITraceControlComponent) event.getElement();
                    fTracepointsViewer.setSubtreeChecked(event.getElement(), false);
                    fTracepointsViewer.setChecked(component.getParent(), false);
                } else {
                    ITraceControlComponent component = (ITraceControlComponent) event.getElement();
                    fTracepointsViewer.setChecked(component.getParent(), false);
                    fTracepointsViewer.setChecked(component.getParent().getParent(), false);
                }
            }
        }
    }
}
