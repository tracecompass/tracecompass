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
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * This action will reset the value of each stored values to a null
 * {@link ITmfStateValue} in the state system
 */
public class ResetStoredFieldsAction implements ITmfXmlAction {

    private final IXmlStateSystemContainer fParent;

    /**
     * Constructor
     *
     * @param parent
     *            The state system container this action belongs to
     */
    public ResetStoredFieldsAction(IXmlStateSystemContainer parent) {
        fParent = parent;
    }

    @Override
    public void execute(ITmfEvent event, TmfXmlScenarioInfo scenarioInfo) {
        if (fParent instanceof XmlPatternStateProvider) {
            for (Entry<String, String> entry : ((XmlPatternStateProvider) fParent).getStoredFields().entrySet()) {
                ((XmlPatternStateProvider) fParent).getHistoryBuilder().resetStoredFields(fParent, entry.getValue(), scenarioInfo, event);
            }
        }
    }
}
