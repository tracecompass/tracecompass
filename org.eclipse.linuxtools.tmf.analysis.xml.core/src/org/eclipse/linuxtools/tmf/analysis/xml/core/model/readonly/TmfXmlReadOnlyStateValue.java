/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.model.readonly;

import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlStateValue;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.w3c.dom.Element;

/**
 * Implements a state value is a read only mode. See {@link TmfXmlStateValue}
 * for the syntax of the state value.
 *
 * In read mode, a state value will typically be used to find a path to a value,
 * so the value is known and there is a path of attributes that should lead to
 * it.
 *
 * @author Geneviève Bastien
 */
public class TmfXmlReadOnlyStateValue extends TmfXmlStateValue {

    /**
     * Constructor where the path to the value is a list of state attributes
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param attributes
     *            The attributes representing the path to this value
     */
    public TmfXmlReadOnlyStateValue(TmfXmlReadOnlyModelFactory modelFactory, Element node,
            IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes) {
        super(modelFactory, node, container, attributes, null);
    }

    /**
     * Constructor where the path to the value is an event field
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param eventField
     *            The event field where to get the value
     */
    public TmfXmlReadOnlyStateValue(TmfXmlReadOnlyModelFactory modelFactory, Element node,
            IXmlStateSystemContainer container, String eventField) {
        super(modelFactory, node, container, Collections.EMPTY_LIST, eventField);
    }

}
