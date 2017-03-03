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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;

/**
 * Implementation of a basic transition in the XML file
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlBasicTransition {

    private static final Pattern WILDCARD_PATTERN = Pattern.compile("\\*"); //$NON-NLS-1$

    private final List<String> fCond;
    private final List<Pattern> fAcceptedEvents;


    /**
     * Constructor
     *
     * @param element
     *            the XML basic transition element
     */
    public TmfXmlBasicTransition(Element element) {
        final @NonNull String events = element.getAttribute(TmfXmlStrings.EVENT);
        fAcceptedEvents = new ArrayList<>();
        if (!events.isEmpty()) {
            for (String eventName : Arrays.asList(events.split(TmfXmlStrings.OR_SEPARATOR))) {
                String name = WILDCARD_PATTERN.matcher(eventName).replaceAll(".*"); //$NON-NLS-1$
                fAcceptedEvents.add(Pattern.compile(name));
            }
        }
        final @NonNull String conditions = element.getAttribute(TmfXmlStrings.COND);
        fCond = conditions.isEmpty() ? new ArrayList<>() : Arrays.asList(conditions.split(TmfXmlStrings.AND_SEPARATOR));
    }

    /**
     * Validate the transition with the current event
     *
     * @param event
     *            The active event
     * @param scenarioInfo
     *            The active scenario details.
     * @param tests
     *            The map of test in the XML file
     * @return true if the transition is validate false if not
     */
    public boolean test(ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo, Map<String, TmfXmlTransitionValidator> tests) {
        if (!validateEvent(event)) {
            return false;
        }

        for (String cond : fCond) {
            TmfXmlTransitionValidator test = tests.get(cond);
            if (test == null) {
                throw new IllegalStateException("Failed to find cond " + cond); //$NON-NLS-1$
            }
            if (!test.test(event, scenarioInfo)) {
                return false;
            }
        }
        return true;
    }

    private boolean validateEvent(ITmfEvent event) {
        String eventName = event.getName();

        if (fAcceptedEvents.isEmpty()) {
            return true;
        }
        /*
         * This validates the event name with the accepted regular expressions
         */
        for (Pattern nameRegex : fAcceptedEvents) {
            if (nameRegex.matcher(eventName).matches()) {
                return true;
            }
        }
        return false;
    }
}
