/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.Map.Entry;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * This action will update the value of the stored fields in the state system
 * based on the current event data.
 *
 * @author Jean-Christian Kouame
 */
public class UpdateStoredFieldsAction implements ITmfXmlAction {

    private final IXmlStateSystemContainer fParent;

    /**
     * Constructor
     *
     * @param parent
     *            The state system container this action belongs to
     */
    public UpdateStoredFieldsAction(IXmlStateSystemContainer parent) {
        fParent = parent;
    }

    @Override
    public void execute(ITmfEvent event, TmfXmlScenarioInfo scenarioInfo) {
        if (fParent instanceof XmlPatternStateProvider) {
            for (Entry<String, String> entry : ((XmlPatternStateProvider) fParent).getStoredFields().entrySet()) {
                ITmfEventField eventField = event.getContent().getField(entry.getKey());
                ITmfStateValue stateValue = null;
                if (eventField != null) {
                    final String alias = entry.getValue();
                    Object field = eventField.getValue();
                    if (field instanceof String) {
                        stateValue = TmfStateValue.newValueString((String) field);
                    } else if (field instanceof Long) {
                        stateValue = TmfStateValue.newValueLong(((Long) field).longValue());
                    } else if (field instanceof Integer) {
                        stateValue = TmfStateValue.newValueInt(((Integer) field).intValue());
                    } else if (field instanceof Double) {
                        stateValue = TmfStateValue.newValueDouble(((Double) field).doubleValue());
                    }
                    if (stateValue == null) {
                        throw new IllegalStateException("State value is null. Invalid type."); //$NON-NLS-1$
                    }
                    ((XmlPatternStateProvider) fParent).getHistoryBuilder().updateStoredFields(fParent, alias, stateValue, scenarioInfo, event);
                }
            }
        }
    }
}
