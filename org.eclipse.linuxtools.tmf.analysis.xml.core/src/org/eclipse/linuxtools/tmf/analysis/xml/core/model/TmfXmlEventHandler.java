/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This Class implements an EventHandler in the XML-defined state system
 *
 * <pre>
 * example:
 * <eventHandler eventName="eventName">
 *  <stateChange>
 *      ...
 *  </stateChange>
 *  <stateChange>
 *      ...
 *  </stateChange>
 * </eventHandler>
 * </pre>
 *
 * @author Florian Wininger
 */
public class TmfXmlEventHandler {

    /* list of states changes */
    private final List<TmfXmlStateChange> fStateChangeList = new ArrayList<>();
    private final String fName;
    private final IXmlStateSystemContainer fParent;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            XML event handler element
     * @param parent
     *            The state system container this event handler belongs to
     */
    public TmfXmlEventHandler(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fParent = parent;
        fName = node.getAttribute(TmfXmlStrings.HANDLER_EVENT_NAME);

        NodeList nodesChanges = node.getElementsByTagName(TmfXmlStrings.STATE_CHANGE);
        /* load state changes */
        for (int i = 0; i < nodesChanges.getLength(); i++) {
            TmfXmlStateChange stateChange = modelFactory.createStateChange((Element) nodesChanges.item(i), fParent);
            fStateChangeList.add(stateChange);
        }
    }

    private boolean appliesToEvent(ITmfEvent event) {
        String eventName = event.getType().getName();

        /* test for full name */
        if (eventName.equals(fName)) {
            return true;
        }

        /* test for the wildcard at the end */
        if ((fName.endsWith(TmfXmlStrings.WILDCARD) && eventName.startsWith(fName.replace(TmfXmlStrings.WILDCARD, TmfXmlStrings.NULL)))) {
            return true;
        }
        return false;
    }

    /**
     * If the event handler can handle the event, it applies all state changes
     * to modify the state system accordingly
     *
     * @param event
     *            The trace event to handle
     */
    public void handleEvent(@NonNull ITmfEvent event) {
        if (!appliesToEvent(event)) {
            return;
        }

        /* Process all state changes */
        for (TmfXmlStateChange stateChange : fStateChangeList) {
            try {
                stateChange.handleEvent(event);
            } catch (AttributeNotFoundException ae) {
                /*
                 * This would indicate a problem with the logic of the manager
                 * here, so it shouldn't happen.
                 */
                Activator.logError("Attribute not found", ae); //$NON-NLS-1$
            } catch (TimeRangeException tre) {
                /*
                 * This would happen if the events in the trace aren't ordered
                 * chronologically, which should never be the case ...
                 */
                Activator.logError("TimeRangeException caught in the state system's event manager.  Are the events in the trace correctly ordered?", tre); //$NON-NLS-1$
            } catch (StateValueTypeException sve) {
                /*
                 * This would happen if we were trying to push/pop attributes
                 * not of type integer. Which, once again, should never happen.
                 */
                Activator.logError("State value type error", sve); //$NON-NLS-1$
            }

        }

    }

}