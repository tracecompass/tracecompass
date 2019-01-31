/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.w3c.dom.Element;

/**
 * This Class implements a state transition tree in the XML-defined state
 * system.
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlStateTransition extends TmfXmlBasicTransition {

    private static final String SAVED_STORED_FIELDS_ACTION_STRING = TmfXmlStrings.CONSTANT_PREFIX + TmfXmlPatternEventHandler.SAVE_STORED_FIELDS_STRING;
    private static final String CLEAR_STORED_FIELDS_ACTION_STRINGS = TmfXmlStrings.CONSTANT_PREFIX + TmfXmlPatternEventHandler.CLEAR_STORED_FIELDS_STRING;

    private final String fTarget;
    private final List<String> fAction;
    private final boolean fStoredFieldsToBeSaved;
    private final boolean fStoredFieldsToBeCleared;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this state transition
     * @param container
     *            The state system container this state transition belongs to
     */
    public TmfXmlStateTransition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        super(node);
        String target = node.getAttribute(TmfXmlStrings.TARGET);
        if (target.isEmpty()) {
            throw new IllegalStateException("No target state has been specified."); //$NON-NLS-1$
        }
        fTarget = target;
        String action = node.getAttribute(TmfXmlStrings.ACTION);
        List<String> actions = action.equals(TmfXmlStrings.NULL) ? Collections.emptyList() : Arrays.asList(action.split(TmfXmlStrings.AND_SEPARATOR));
        fStoredFieldsToBeSaved = (node.getAttribute(TmfXmlStrings.SAVE_STORED_FIELDS).equals(TmfXmlStrings.EMPTY_STRING) ? false : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.SAVE_STORED_FIELDS)));
        fStoredFieldsToBeCleared = (node.getAttribute(TmfXmlStrings.CLEAR_STORED_FIELDS).equals(TmfXmlStrings.EMPTY_STRING) ? false : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.CLEAR_STORED_FIELDS)));
        fAction = new ArrayList<>();
        if (fStoredFieldsToBeSaved) {
            fAction.add(SAVED_STORED_FIELDS_ACTION_STRING);
        }
        fAction.addAll(actions);
        if (fStoredFieldsToBeCleared) {
            fAction.add(CLEAR_STORED_FIELDS_ACTION_STRINGS);
        }
    }

    /**
     * The next state of the state machine this state transition belongs to.
     *
     * @return the next state this transition try to reach
     */
    public String getTarget() {
        return fTarget;
    }

    /**
     * The action to be executed when the input of this state transition is
     * validated
     *
     * @return the action to execute if the input is validate
     */
    public List<String> getAction() {
        return fAction;
    }

    /**
     * Tell if the stored fields have to be saved at this step of the scenario
     *
     * @return If the stored fields have to be saved or not
     */
    public boolean isStoredFieldsToBeSaved() {
        return fStoredFieldsToBeSaved;
    }

    /**
     * Tell if the stored fields have to be cleared at this moment of this scenario
     *
     * @return If the stored fields have to cleared or not
     */
    public boolean isStoredFieldsToBeCleared() {
        return fStoredFieldsToBeCleared;
    }
}
