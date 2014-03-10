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

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.ITmfXmlStateValue;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlCondition;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlEventHandler;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlStateChange;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteModelFactory;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.w3c.dom.Element;

/**
 * Concrete factory for XML model elements in read only mode
 *
 * @author Geneviève Bastien
 */
public class TmfXmlReadOnlyModelFactory implements ITmfXmlModelFactory {

    private static ITmfXmlModelFactory fInstance = null;

    /**
     * Get the instance of this model creator
     *
     * @return The {@link TmfXmlReadWriteModelFactory} instance
     */
    @NonNull
    public static synchronized ITmfXmlModelFactory getInstance() {
        ITmfXmlModelFactory instance = fInstance;
        if (instance == null) {
            instance = new TmfXmlReadOnlyModelFactory();
            fInstance = instance;
        }
        return instance;
    }

    @Override
    public ITmfXmlStateAttribute createStateAttribute(Element attribute, IXmlStateSystemContainer container) {
        return new TmfXmlReadOnlyStateAttribute(this, attribute, container);
    }

    @Override
    public ITmfXmlStateValue createStateValue(Element node, IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes) {
        return new TmfXmlReadOnlyStateValue(this, node, container, attributes);
    }

    @Override
    public ITmfXmlStateValue createStateValue(Element node, IXmlStateSystemContainer container, String eventField) {
        return new TmfXmlReadOnlyStateValue(this, node, container, eventField);
    }

    @Override
    public TmfXmlCondition createCondition(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlCondition(this, node, container);
    }

    @Override
    public TmfXmlEventHandler createEventHandler(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlEventHandler(this, node, container);
    }

    @Override
    public TmfXmlStateChange createStateChange(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlStateChange(this, node, container);
    }

    @Override
    public TmfXmlLocation createLocation(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlLocation(this, node, container);
    }

}
