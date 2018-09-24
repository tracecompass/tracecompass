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

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlConditionCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlStateChangeCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlStateValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlFsm;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlMapEntry;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlPatternEventHandler;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlPatternSegmentBuilder;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlState;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlStateTransition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlTransitionValidator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.w3c.dom.Element;

/**
 * Concrete factory for XML model elements in read write mode
 *
 * @author Geneviève Bastien
 */
public class TmfXmlReadWriteModelFactory implements ITmfXmlModelFactory {

    private static @Nullable ITmfXmlModelFactory fInstance = null;

    /**
     * Get the instance of this model creator
     *
     * @return The {@link TmfXmlReadWriteModelFactory} instance
     */
    public static synchronized ITmfXmlModelFactory getInstance() {
        ITmfXmlModelFactory instance = fInstance;
        if (instance == null) {
            instance = new TmfXmlReadWriteModelFactory();
            fInstance = instance;
        }
        return instance;
    }

    @Override
    public ITmfXmlStateAttribute createStateAttribute(Element attribute, IXmlStateSystemContainer container) {
        return new TmfXmlReadWriteStateAttribute(this, attribute, container);
    }

    @Override
    public ITmfXmlStateValue createStateValue(Element node, IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes) {
        return new TmfXmlReadWriteStateValue(this, node, container, attributes);
    }

    @Override
    public ITmfXmlStateValue createStateValue(Element node, IXmlStateSystemContainer container, String eventField) {
        return new TmfXmlReadWriteStateValue(this, node, container, eventField);
    }

    @Override
    public DataDrivenCondition createCondition(Element node, IXmlStateSystemContainer container) {
        TmfXmlConditionCu compile = TmfXmlConditionCu.compile(container.getAnalysisCompilationData(), node);
        if (compile == null)  {
            throw new NullPointerException("Condition did not compile correctly"); //$NON-NLS-1$
        }
        return compile.generate();
    }

    @Override
    public DataDrivenAction createStateChange(Element node, IXmlStateSystemContainer container) {
        TmfXmlStateChangeCu compile = TmfXmlStateChangeCu.compile(container.getAnalysisCompilationData(), node);
        if (compile == null)  {
            throw new NullPointerException("State change did not compile correctly"); //$NON-NLS-1$
        }
        return compile.generate();
    }

    @Override
    public TmfXmlLocation createLocation(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlLocation(this, node, container);
    }

    @Override
    public TmfXmlPatternEventHandler createPatternEventHandler(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlPatternEventHandler(this, node, container);
    }

    @Override
    public TmfXmlTransitionValidator createTransitionValidator(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlTransitionValidator(this, node, container);
    }

    @Override
    public TmfXmlAction createAction(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlAction(this, node, container);
    }

    @Override
    public TmfXmlFsm createFsm(Element node, IXmlStateSystemContainer container) {
        return TmfXmlFsm.create(this, node, container);
    }

    @Override
    public @NonNull TmfXmlState createState(Element node, IXmlStateSystemContainer container, @Nullable TmfXmlState parent) {
        return TmfXmlState.create(this, node, container, parent);
    }

    @Override
    public TmfXmlStateTransition createStateTransition(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlStateTransition(this, node, container);
    }

    @Override
    public TmfXmlPatternSegmentBuilder createPatternSegmentBuilder(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlPatternSegmentBuilder(this, node, container);
    }

    @Override
    public @NonNull TmfXmlMapEntry createMapEntry(@NonNull Element node, @NonNull IXmlStateSystemContainer container) {
        return new TmfXmlMapEntry(this, node, container);
    }
}
