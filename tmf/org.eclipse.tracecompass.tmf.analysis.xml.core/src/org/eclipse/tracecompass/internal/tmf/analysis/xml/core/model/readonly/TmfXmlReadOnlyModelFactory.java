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

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.readonly;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlActionCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlConditionCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlStateValueCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlPatternEventHandler;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.w3c.dom.Element;

/**
 * Concrete factory for XML model elements in read only mode
 *
 * @author Geneviève Bastien
 */
public class TmfXmlReadOnlyModelFactory implements ITmfXmlModelFactory {

    private static @Nullable ITmfXmlModelFactory fInstance = null;

    /**
     * Get the instance of this model creator
     *
     * @return The {@link TmfXmlReadOnlyModelFactory} instance
     */
    public static synchronized ITmfXmlModelFactory getInstance() {
        ITmfXmlModelFactory instance = fInstance;
        if (instance == null) {
            instance = new TmfXmlReadOnlyModelFactory();
            fInstance = instance;
        }
        return instance;
    }

    @Override
    public DataDrivenValue createStateValue(Element node, IXmlStateSystemContainer container) {
        TmfXmlStateValueCu compile = Objects.requireNonNull(TmfXmlStateValueCu.compileValue(container.getAnalysisCompilationData(), node), "State value did not compile correctly"); //$NON-NLS-1$
        return compile.generate();
    }

    @Override
    public DataDrivenCondition createCondition(Element node, IXmlStateSystemContainer container) {
        TmfXmlConditionCu compile = Objects.requireNonNull(TmfXmlConditionCu.compile(container.getAnalysisCompilationData(), node), "Condition did not compile correctly"); //$NON-NLS-1$
        return compile.generate();
    }

    @Override
    public DataDrivenAction createStateChange(Element node, IXmlStateSystemContainer container) {
        TmfXmlActionCu compile = Objects.requireNonNull(TmfXmlActionCu.compile(container.getAnalysisCompilationData(), node), "State change did not compile correctly"); //$NON-NLS-1$
        return compile.generate();
    }

    @Override
    public TmfXmlPatternEventHandler createPatternEventHandler(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlPatternEventHandler(this, node, container);
    }

    @Override
    public String createTransitionValidator(Element node, IXmlStateSystemContainer container) {
        return Objects.requireNonNull(TmfXmlConditionCu.compileNamedCondition(container.getAnalysisCompilationData(), node), "Named condition did not compile correctly"); //$NON-NLS-1$
    }

    @Override
    public String createAction(Element node, IXmlStateSystemContainer container) {
        return Objects.requireNonNull(TmfXmlActionCu.compileNamedAction(container.getAnalysisCompilationData(), node), "Named action did not compile correctly"); //$NON-NLS-1$
    }

}
