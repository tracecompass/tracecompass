/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.DataDrivenStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * The class responsible to handle events
 *
 * FIXME: This class won't be necessary when original stateProvider analysis are
 * converted to fsms.
 *
 * @author Geneviève Bastien
 */
public class DataDrivenEventHandler implements IDataDrivenRuntimeObject {

    private static final Pattern WILDCARD_PATTERN = Pattern.compile("\\*"); //$NON-NLS-1$

    private final Pattern fEventName;
    private final List<DataDrivenAction> fStateChanges;

    /**
     * Constructor
     *
     * @param eventName
     *            The event name. The name will be converted to a regex pattern and
     *            any '*' character in the event name replace by a wildcard
     * @param actions
     *            The list of actions to execution for the event
     */
    public DataDrivenEventHandler(String eventName, List<DataDrivenAction> actions) {
        String name = WILDCARD_PATTERN.matcher(eventName).replaceAll(".*"); //$NON-NLS-1$
        fEventName = Pattern.compile(name);
        fStateChanges = actions;
    }

    private boolean appliesToEvent(ITmfEvent event) {
        String eventName = event.getName();
        return fEventName.matcher(eventName).matches();
    }

    /**
     * Handle the event, ie execute the actions if the event matches the name
     *
     * @param event
     *            The event to handle
     * @param scenarioInfo
     *            The scenario info
     * @param container
     *            The analysis data container
     */
    public void handleEvent(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, DataDrivenStateProvider container) {
        if (!appliesToEvent(event)) {
            return;
        }
        fStateChanges.forEach(change -> change.eventHandle(event, scenarioInfo, container));
    }

}
