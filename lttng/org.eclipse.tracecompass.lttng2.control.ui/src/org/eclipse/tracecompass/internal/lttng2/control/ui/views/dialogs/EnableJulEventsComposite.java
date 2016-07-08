/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceJulLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.BaseLoggerComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceControlContentProvider;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceControlLabelProvider;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceProviderGroup;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.UstProviderComponent;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * A composite for collecting information about JUL events to be enabled.
 *
 * @author Bruno Roy
 */
public class EnableJulEventsComposite extends Composite implements IBaseEnableUstEvents {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private enum GroupEnum { LOGGERS }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The referenced trace provider group containing the JUL providers
     * component which contains a list of available tracepoints.
     */
    private final TraceProviderGroup fProviderGroup;
    /**
     * A button to enable/disable the loggers group
     */
    private Button fLoggersActivateButton;
    /**
     * A tree viewer for displaying and selection of available loggers.
     */
    private CheckboxTreeViewer fLoggersViewer;
    /**
     * The flag indicating that loggers are selected.
     */
    private boolean fIsLoggers;
    /**
     * The list of loggers to be enabled.
     */
    private List<String> fLoggers;
    /**
     * A button to enable/disable the log level group
     */
    private Button fLogLevelActivateButton;
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
     * The flag indicating that all log level are selected.
     */
    private boolean fIsLogLevel;
    /**
     * The flag indicating that all loggers (across providers) are selected.
     */
    private boolean fIsAllLoggers;
    /**
     * The type of the log level (loglevel or loglevel-only)
     */
    private LogLevelType fLogLevelType;
    /**
     * The selected log level.
     */
    private TraceJulLogLevel fLogLevel;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param parent
     *            a parent composite
     * @param style
     *            a composite style
     * @param providerGroup
     *            the trace provider group
     */
    public EnableJulEventsComposite(Composite parent, int style, TraceProviderGroup providerGroup) {
        super(parent, style);
        fProviderGroup = providerGroup;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public TraceJulLogLevel getLogLevel() {
        return fLogLevel;
    }

    @Override
    public LogLevelType getLogLevelType() {
        return fLogLevelType;
    }

    @Override
    public boolean isAllTracePoints() {
        return fIsAllLoggers;
    }

    @Override
    public List<String> getEventNames() {
        return new ArrayList<>(fLoggers);
    }

    @Override
    public boolean isLogLevel() {
        return fIsLogLevel;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create the contents of this event composite
     */
    public void createContent() {

        // Logger group
        createLoggersGroup();

        // Log Level Group
        createLogLevelGroup();

        // Set default enablements
        setEnablements(GroupEnum.LOGGERS);
    }

    /**
     * Validates the UST composite input data.
     *
     * @return true if configured data is valid and can be retrieved.
     */
    public boolean isValid() {

        fIsLoggers = fLoggersActivateButton.getSelection();
        fIsLogLevel = fLogLevelActivateButton.getSelection();

        // Initialize loggers fields
        fLoggers = new ArrayList<>();
        if (fIsLoggers) {
            Set<String> set = new HashSet<>();
            Object[] checkedElements = fLoggersViewer.getCheckedElements();
            int totalNbEvents = 0;
            for (int i = 0; i < checkedElements.length; i++) {
                ITraceControlComponent component = (ITraceControlComponent) checkedElements[i];
                if (component instanceof BaseLoggerComponent) {
                    totalNbEvents++;
                    if (!set.contains(component.getName())) {
                        set.add(component.getName());
                        fLoggers.add(component.getName());
                    }
                }

            }

            // verify if all events are selected
            int nbJulEvents = 0;
            List<ITraceControlComponent> comps = fProviderGroup.getChildren(UstProviderComponent.class);
            for (ITraceControlComponent comp : comps) {
                // We want the children of each UST provider
                ITraceControlComponent[] events = comp.getChildren();
                for (ITraceControlComponent event : events) {
                    if (event instanceof BaseLoggerComponent) {
                        nbJulEvents++;
                    }
                }

            }
            fIsAllLoggers = nbJulEvents == totalNbEvents;
        }

        // initialize log level event name string
        fLogLevelType = LogLevelType.LOGLEVEL_NONE;
        if (fIsLogLevel) {
            if (fLogLevelButton.getSelection()) {
                fLogLevelType = LogLevelType.LOGLEVEL;
            } else if (fLogLevelOnlyButton.getSelection()) {
                fLogLevelType = LogLevelType.LOGLEVEL_ONLY;
            }

            TraceJulLogLevel[] levels = TraceJulLogLevel.values();
            int id = fLogLevelCombo.getSelectionIndex();

            if (id < 0) {
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_EnableLoggersDialogTitle,
                        Messages.TraceControl_InvalidLogLevel);

                return false;
            }

            if (fLoggers.isEmpty()) {
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_EnableLoggersDialogTitle,
                        Messages.TraceControl_InvalidLogger);

                return false;
            }
            fLogLevel = levels[id];
        }

        // validation successful -> call super.okPressed()
        return true;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Creates loggers group.
     */
    private void createLoggersGroup() {
        // Create the loggers group
        Group loggersMainGroup = new Group(this, SWT.SHADOW_NONE);
        loggersMainGroup.setText(Messages.TraceControl_EnableEventsLoggerGroupName);
        GridLayout layout = new GridLayout(2, false);
        loggersMainGroup.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        loggersMainGroup.setLayoutData(data);

        // Create the Select button
        Composite buttonComposite = new Composite(loggersMainGroup, SWT.NONE);
        layout = new GridLayout(1, true);
        buttonComposite.setLayout(layout);
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, true);
        buttonComposite.setLayoutData(data);

        fLoggersActivateButton = new Button(buttonComposite, SWT.RADIO);
        fLoggersActivateButton.setText(Messages.TraceControl_EnableGroupSelectionName);
        data = new GridData(GridData.FILL_HORIZONTAL);
        fLoggersActivateButton.setLayoutData(data);
        fLoggersActivateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setEnablements(GroupEnum.LOGGERS);
            }
        });

        // Create the group for the tree
        Group loggersGroup = new Group(loggersMainGroup, SWT.SHADOW_NONE);
        layout = new GridLayout(1, true);
        loggersGroup.setLayout(layout);
        data = new GridData(GridData.FILL_BOTH);
        loggersGroup.setLayoutData(data);
        new FilteredTree(loggersGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), true) {
            @Override
            protected TreeViewer doCreateTreeViewer(Composite aparent, int style) {
                fLoggersViewer = new CheckboxTreeViewer(aparent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
                fLoggersViewer.getTree().setToolTipText(Messages.TraceControl_EnableEventsLoggerTreeTooltip);
                fLoggersViewer.setContentProvider(new JulContentProvider());

                fLoggersViewer.setLabelProvider(new JulLabelProvider());
                fLoggersViewer.addCheckStateListener(new JulCheckStateListener());

                fLoggersViewer.setInput(fProviderGroup.getParent());
                fLoggersViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
                return fLoggersViewer;
            }
        };
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

        fLogLevelActivateButton = new Button(buttonComposite, SWT.CHECK);
        fLogLevelActivateButton.setText(Messages.TraceControl_EnableGroupSelectionName);
        fLogLevelActivateButton.setSelection(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        fLogLevelActivateButton.setLayoutData(data);
        fLogLevelActivateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fLogLevelCombo.setEnabled(fLogLevelActivateButton.getSelection());
                fLogLevelButton.setEnabled(fLogLevelActivateButton.getSelection());
                fLogLevelOnlyButton.setEnabled(fLogLevelActivateButton.getSelection());
            }
        });

        Group logLevelGroup = new Group(logLevelMainGroup, SWT.SHADOW_NONE);
        layout = new GridLayout(2, true);
        logLevelGroup.setLayout(layout);
        logLevelGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

        TraceJulLogLevel[] levels = TraceJulLogLevel.values();

        String[] levelNames = new String[levels.length - 1];
        int k = 0;
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] != TraceJulLogLevel.LEVEL_UNKNOWN) {
                levelNames[k++] = levels[i].getInName();
            }
        }

        fLogLevelCombo = new CCombo(logLevelGroup, SWT.READ_ONLY);
        fLogLevelCombo.setItems(levelNames);
        fLogLevelCombo.setToolTipText(Messages.TraceControl_EnableEventsLogLevelTooltip);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 4;
        fLogLevelCombo.setLayoutData(data);

        // By default the combo box and the buttons are not enabled
        fLogLevelCombo.setEnabled(false);
        fLogLevelButton.setEnabled(false);
        fLogLevelOnlyButton.setEnabled(false);

    }

    /**
     * Enable/selects widgets depending on the group specified.
     *
     * @param group
     *            group to enable.
     */
    private void setEnablements(GroupEnum group) {

        // Enable/disable trace point items
        fLoggersActivateButton.setSelection(group == GroupEnum.LOGGERS);
        fLoggersViewer.getTree().setEnabled(group == GroupEnum.LOGGERS);
    }

    /**
     * Content provider for the loggers tree.
     */
    public static final class JulContentProvider extends TraceControlContentProvider {
        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof TargetNodeComponent) {
                List<ITraceControlComponent> children = ((ITraceControlComponent) parentElement).getChildren(TraceProviderGroup.class);
                return children.toArray(new ITraceControlComponent[children.size()]);
            }
            if (parentElement instanceof TraceProviderGroup) {
                List<ITraceControlComponent> ustProviderChildren = ((ITraceControlComponent) parentElement).getChildren(UstProviderComponent.class)
                        .stream().filter(comp -> ((UstProviderComponent) comp).getLoggerComponents(TraceDomainType.JUL).size() > 0)
                        .collect(Collectors.toList());
                return ustProviderChildren.toArray(new ITraceControlComponent[ustProviderChildren.size()]);
            }
            if (parentElement instanceof UstProviderComponent) {
                List<ITraceControlComponent> loggers = ((UstProviderComponent) parentElement).getLoggerComponents(TraceDomainType.JUL);
                return loggers.toArray(new ITraceControlComponent[loggers.size()]);
            }
            return new Object[0];
        }
    }

    /**
     * Content label for the loggers tree.
     */
    public static final class JulLabelProvider extends TraceControlLabelProvider {
        @Override
        public Image getImage(Object element) {
            return null;
        }

        @Override
        public String getText(Object element) {
            if ((element != null) && (element instanceof TraceProviderGroup)) {
                return Messages.TraceControl_EnableEventsTreeAllLabel;
            }

            if ((element != null) && (element instanceof UstProviderComponent)) {
                return Messages.TraceControl_EnableEventsTreeAllLabel + " - " + ((UstProviderComponent)element).getName(); //$NON-NLS-1$
            }
            return super.getText(element);
        }
    }

    /**
     * Check state listener for the loggers tree.
     */
    public final class JulCheckStateListener implements ICheckStateListener {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            if (event.getChecked()) {
                if (event.getElement() instanceof TraceProviderGroup) {
                    fLoggersViewer.setSubtreeChecked(event.getElement(), true);
                }
                if (event.getElement() instanceof UstProviderComponent) {
                    fLoggersViewer.setSubtreeChecked(event.getElement(), true);
                }
            } else {
                if (event.getElement() instanceof TraceProviderGroup) {
                    fLoggersViewer.setSubtreeChecked(event.getElement(), true);
                }
                if (event.getElement() instanceof UstProviderComponent) {
                    ITraceControlComponent component = (ITraceControlComponent) event.getElement();
                    fLoggersViewer.setSubtreeChecked(event.getElement(), false);
                    fLoggersViewer.setChecked(component.getParent(), false);
                } else {
                    ITraceControlComponent component = (ITraceControlComponent) event.getElement();
                    fLoggersViewer.setChecked(component.getParent(), false);
                    fLoggersViewer.setChecked(component.getParent().getParent(), false);
                }
            }
        }
    }
}
