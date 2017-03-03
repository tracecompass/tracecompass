/*******************************************************************************
 * Copyright (c) 2016 Ericsson
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class implements a state tree described in XML-defined pattern
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlState {

    /** The initial state ID */
    public static final String INITIAL_STATE_ID = "#initial"; //$NON-NLS-1$
    private final String fId;
    private final IXmlStateSystemContainer fContainer;
    private final List<TmfXmlStateTransition> fTransitions;
    private @Nullable TmfXmlState fparent;
    private List<String> fOnEntryActions;
    private List<String> fOnExitActions;
    //TODO Sub-state are not yet supported.
    private Map<String, TmfXmlState> fChildren;
    private @Nullable TmfXmlStateTransition fInitialTransition;
    private @Nullable String fInitialStateId;
    private @Nullable String fFinalStateId;
    private Type fType;

    /**
     * Enum for the type of state
     */
    public enum Type {
        /**
         * Final state type
         */
        FINAL,
        /**
         * Initial state type
         */
        INITIAL,
        /**
         * Fail state type, the pattern has failed to match
         */
        FAIL,
        /**
         * This is the normal state type, for states that are not the first,
         * final or failing state
         */
        DEFAULT
    }

    private TmfXmlState(IXmlStateSystemContainer container, Type type, String id, @Nullable TmfXmlState parent, List<@NonNull TmfXmlStateTransition> transitions, Map<@NonNull String, @NonNull TmfXmlState> children, List<String> onentryActions, List<String> onexitActions) {
        fContainer = container;
        fType = type;
        fId = id;
        fparent = parent;
        fTransitions = transitions;
        fChildren = children;
        fOnEntryActions = onentryActions;
        fOnExitActions = onexitActions;
    }

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this state
     * @param container
     *            The state system container this state definition belongs to
     * @param parent
     *            The parent state of this state
     * @return The new {@link TmfXmlState}
     */
    public static TmfXmlState create(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container, @Nullable TmfXmlState parent) {
        Type type = getStateType(node);
        String id = node.getAttribute(TmfXmlStrings.ID);
        List<TmfXmlStateTransition> transitions = getTransitions(modelFactory, container, node);

        NodeList nodesOnentry = node.getElementsByTagName(TmfXmlStrings.ONENTRY);
        List<String> onentryActions = nodesOnentry.getLength() > 0 ? Arrays.asList(((Element) nodesOnentry.item(0)).getAttribute(TmfXmlStrings.ACTION).split(TmfXmlStrings.AND_SEPARATOR)) : Collections.EMPTY_LIST;

        NodeList nodesOnexit = node.getElementsByTagName(TmfXmlStrings.ONEXIT);
        List<String> onexitActions = nodesOnexit.getLength() > 0 ? Arrays.asList(((Element) nodesOnexit.item(0)).getAttribute(TmfXmlStrings.ACTION).split(TmfXmlStrings.AND_SEPARATOR)) : Collections.EMPTY_LIST;

        TmfXmlState state = new TmfXmlState(container, type, id, parent, transitions, new HashMap<>(), onentryActions, onexitActions);
        initState(state, modelFactory, container, node);

        return state;
    }

    private static void getFinalState(TmfXmlState parentState, ITmfXmlModelFactory modelFactory, IXmlStateSystemContainer container, Element node) {
        NodeList nodesFinal = node.getElementsByTagName(TmfXmlStrings.FINAL);
        String finalStateId = null;
        if (nodesFinal.getLength() > 0) {
            final Element finalElement = NonNullUtils.checkNotNull((Element) nodesFinal.item(0));
            finalStateId = nodesFinal.getLength() > 0 ? finalElement.getAttribute(TmfXmlStrings.ID) : null;
            TmfXmlState finalState = modelFactory.createState(finalElement, container, parentState);
            parentState.getChildren().put(finalState.getId(), finalState);
        }
        parentState.fFinalStateId = finalStateId;
    }

    private static void getSubStates(TmfXmlState parentState, ITmfXmlModelFactory modelFactory, IXmlStateSystemContainer container, Element node) {
        String initial = node.getAttribute(TmfXmlStrings.INITIAL);
        TmfXmlStateTransition initialTransition = null;
        if (initial.isEmpty()) {
            NodeList nodesInitial = node.getElementsByTagName(TmfXmlStrings.INITIAL);
            if (nodesInitial.getLength() == 1) {
                final @NonNull Element transitionElement = NonNullUtils.checkNotNull((Element) ((Element) nodesInitial.item(0)).getElementsByTagName(TmfXmlStrings.TRANSITION).item(0));
                initialTransition = modelFactory.createStateTransition(transitionElement, container);
                initial = initialTransition.getTarget();
            }
        }

        NodeList nodesState = node.getElementsByTagName(TmfXmlStrings.STATE);
        for (int i = 0; i < nodesState.getLength(); i++) {
            TmfXmlState child = modelFactory.createState(NonNullUtils.checkNotNull((Element) nodesState.item(i)), container, parentState);
            parentState.getChildren().put(child.getId(), child);

            if (i == 0 && initial.isEmpty()) {
                initial = child.getId();
            }
        }
        parentState.fInitialStateId = initial.isEmpty() ? null : initial;
        parentState.fInitialTransition = initialTransition;
    }

    private static void initState(TmfXmlState state, ITmfXmlModelFactory modelFactory, IXmlStateSystemContainer container, Element node) {
        getSubStates(state, modelFactory, container, node);
        getFinalState(state, modelFactory, container, node);
    }

    /**
     * Get the List of transitions for this state
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this state definition
     * @return The list of transitions
     */
    private static List<@NonNull TmfXmlStateTransition> getTransitions(ITmfXmlModelFactory modelFactory, IXmlStateSystemContainer container, Element node) {
        List<@NonNull TmfXmlStateTransition> transitions = new ArrayList<>();
        NodeList nodesTransition = node.getElementsByTagName(TmfXmlStrings.TRANSITION);
        for (int i = 0; i < nodesTransition.getLength(); i++) {
            final Element element = (Element) nodesTransition.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlStateTransition transition = modelFactory.createStateTransition(element, container);
            transitions.add(transition);
        }
        return transitions;
    }

    /**
     * Get the state type from its XML definition
     * @param node
     *            The XML definition of the state
     * @return The state type
     */
    private static Type getStateType(Element node) {
        switch (node.getNodeName()) {
        case TmfXmlStrings.FINAL:
            return Type.FINAL;
        case TmfXmlStrings.INITIAL:
            return Type.INITIAL;
        case TmfXmlStrings.ABANDON:
            return Type.FAIL;
        case TmfXmlStrings.STATE:
        default:
            return Type.DEFAULT;
        }
    }

    /**
     * Get the state id
     *
     * @return The state id
     */
    public String getId() {
        return fId;
    }

    /**
     * Get the container
     *
     * @return The container
     */
    public IXmlStateSystemContainer getContainer() {
        return fContainer;
    }

    /**
     * The list of transitions of this state
     *
     * @return The list of transitions
     */
    public List<TmfXmlStateTransition> getTransitionList() {
        return fTransitions;
    }

    /**
     * Get the actions to execute when entering this state, in an array
     *
     * @return The array of actions
     */
    public List<String> getOnEntryActions() {
        return fOnEntryActions;
    }

    /**
     * Get the actions to execute when leaving this state, in an array
     *
     * @return The array of actions
     */
    public List<String> getOnExitActions() {
        return fOnExitActions;
    }

    /**
     * Get children states of this state into a map
     *
     * @return The map of children state
     */
    public Map<String, TmfXmlState> getChildren() {
        return fChildren;
    }

    /**
     * Get the initial transition of this state
     *
     * @return The initial transition
     */
    public @Nullable TmfXmlStateTransition getInitialTransition() {
        return fInitialTransition;
    }

    /**
     * Get the initial state ID
     *
     * @return The initial state ID
     */
    public @Nullable String getInitialStateId() {
        return fInitialStateId;
    }

    /**
     * Get the final state ID
     *
     * @return The final state ID
     */
    public @Nullable String getFinalStateId() {
        return fFinalStateId;
    }

    /**
     * Get the parent state
     *
     * @return The parent state
     */
    public @Nullable TmfXmlState getParent() {
        return fparent;
    }

    /**
     * Get the type of this state
     *
     * @return The type of the state
     */
    public Type getType() {
        return fType;
    }
}
