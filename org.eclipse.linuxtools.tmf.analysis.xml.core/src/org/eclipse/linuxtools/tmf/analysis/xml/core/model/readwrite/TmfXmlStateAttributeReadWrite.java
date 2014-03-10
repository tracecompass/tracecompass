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

package org.eclipse.linuxtools.tmf.analysis.xml.core.model.readwrite;

import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlStateAttribute;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.w3c.dom.Element;

/**
 * Implements a state attribute in a read write mode. See
 * {@link TmfXmlStateAttribute} for the syntax of this attribute.
 *
 * In read-write mode, attributes that are requested but do not exist are added
 * to the state system.
 *
 * @author Geneviève Bastien
 */
public class TmfXmlStateAttributeReadWrite extends TmfXmlStateAttribute {

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param attribute
     *            The XML element corresponding to this attribute
     * @param container
     *            The state system container this state value belongs to
     */
    public TmfXmlStateAttributeReadWrite(TmfXmlModelFactoryReadWrite modelFactory, Element attribute, IXmlStateSystemContainer container) {
        super(modelFactory, attribute, container);
    }

    @Override
    protected ITmfStateSystemBuilder getStateSystem() {
        return (ITmfStateSystemBuilder) super.getStateSystem();
    }

    @Override
    protected int getQuarkAbsoluteAndAdd(String... path) throws AttributeNotFoundException {
        return getStateSystem().getQuarkAbsoluteAndAdd(path);
    }

    @Override
    protected int getQuarkRelativeAndAdd(int startNodeQuark, String... path) throws AttributeNotFoundException {
        return getStateSystem().getQuarkRelativeAndAdd(startNodeQuark, path);
    }

}
