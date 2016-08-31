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

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.readwrite;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlStateAttribute;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool.QueueType;
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
public class TmfXmlReadWriteStateAttribute extends TmfXmlStateAttribute {

    private final Map<Integer, TmfAttributePool> fAttributePools = new HashMap<>();

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
    public TmfXmlReadWriteStateAttribute(TmfXmlReadWriteModelFactory modelFactory, Element attribute, IXmlStateSystemContainer container) {
        super(modelFactory, attribute, container);
    }

    @Override
    protected @Nullable ITmfStateSystemBuilder getStateSystem() {
        return (ITmfStateSystemBuilder) super.getStateSystem();
    }

    @Override
    protected int getQuarkAbsoluteAndAdd(String... path) throws AttributeNotFoundException {
        ITmfStateSystemBuilder ss = getStateSystem();
        if (ss == null) {
            throw new IllegalStateException("The state system hasn't been initialized yet"); //$NON-NLS-1$
        }
        return ss.getQuarkAbsoluteAndAdd(path);
    }

    @Override
    protected int getQuarkRelativeAndAdd(int startNodeQuark, String... path) throws AttributeNotFoundException {
        ITmfStateSystemBuilder ss = getStateSystem();
        if (ss == null) {
            throw new IllegalStateException("The state system hasn't been initialized yet"); //$NON-NLS-1$
        }
        return ss.getQuarkRelativeAndAdd(startNodeQuark, path);
    }

    @Override
    protected @Nullable TmfAttributePool getAttributePool(int startNodeQuark) {
        ITmfStateSystemBuilder ss = getStateSystem();
        if (ss == null) {
            throw new IllegalStateException("The state system hasn't been initialized yet"); //$NON-NLS-1$
        }
        TmfAttributePool pool = fAttributePools.get(startNodeQuark);
        if (pool == null) {
            pool = new TmfAttributePool(ss, startNodeQuark, QueueType.PRIORITY);
            fAttributePools.put(startNodeQuark, pool);
        }
        return pool;
    }

}
