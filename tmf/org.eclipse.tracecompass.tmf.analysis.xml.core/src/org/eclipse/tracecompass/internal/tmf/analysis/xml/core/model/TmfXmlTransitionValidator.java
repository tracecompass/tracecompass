/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;

/**
 * This Class implements a transition input tree in the XML-defined state
 * system.
 *
 * TODO We should merge this class with the current TmfXmlCondition, that should
 * be kept as is for compatibility with current stateProvider
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlTransitionValidator implements ITmfXmlCondition {

    IXmlStateSystemContainer fParent;
    private final String fId;
    private final DataDrivenCondition fCondition;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this transition input
     * @param parent
     *            The state system container this transition input belongs to
     */
    public TmfXmlTransitionValidator(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fParent = parent;
        fId = node.getAttribute(TmfXmlStrings.ID);

        List<@Nullable Element> childElements = XmlUtils.getChildElements(node);
        Element child = NonNullUtils.checkNotNull(childElements.get(0));
        // Compile the child of the IF node
        childElements = XmlUtils.getChildElements(child);
        if (childElements.size() != 1) {
            // TODO: Validation message here
            Activator.logError("There should be only one element under this condition"); //$NON-NLS-1$
            throw new NullPointerException("Can't compile the condition"); //$NON-NLS-1$
        }
        Element subCondition = Objects.requireNonNull(childElements.get(0));
        fCondition = modelFactory.createCondition(subCondition, parent);
    }

    /**
     * Get the ID of this transition input
     *
     * @return The id of this transition input
     */
    public String getId() {
        return fId;
    }

    @Override
    public boolean test(ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) {
        if (scenarioInfo == null) {
            // Should never be null
            return false;
        }
        return fCondition.test(event, scenarioInfo, fParent);
    }
}
