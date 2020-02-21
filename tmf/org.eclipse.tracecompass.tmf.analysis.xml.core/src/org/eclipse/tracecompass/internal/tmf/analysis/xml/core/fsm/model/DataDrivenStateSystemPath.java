/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * This class represents a path in the state system.
 *
 * @author Geneviève Bastien
 */
public class DataDrivenStateSystemPath implements IDataDrivenRuntimeObject {

    private final List<DataDrivenValue> fAttributes;
    private final IBaseQuarkProvider fQuarkProvider;

    /**
     * Constructor
     *
     * @param attributes
     *            The list of state values whose resolved value will be the
     *            attributes in the state system
     *                 */
    public DataDrivenStateSystemPath(List<DataDrivenValue> attributes) {
        this(attributes, IBaseQuarkProvider.IDENTITY_BASE_QUARK);
    }

    /**
     * Constructor
     *
     * @param attributes
     *            The list of state values whose resolved value will be the
     *            attributes in the state system
     * @param quarkProvider
     *            The provider for the base quark
     *
     */
    public DataDrivenStateSystemPath(List<DataDrivenValue> attributes, IBaseQuarkProvider quarkProvider) {
        fAttributes = attributes;
        fQuarkProvider = quarkProvider;
    }

    /**
     * Get the quark in the system for this path by resolving each value along
     * the path
     *
     * @param event
     *            The event to use, it can be null if called outside an event
     *            request.
     * @param baseQuark
     *            The original base quark, as obtained by the caller
     * @param scenarioInfo
     *            The scenario data information. It will be null if the event is
     *            null
     * @param container
     *            The analysis data container
     * @return The quark when the path is resolved.
     */
    public int getQuark(@Nullable ITmfEvent event, int baseQuark, @Nullable DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        int quark = fQuarkProvider.getBaseQuark(baseQuark, scenarioInfo);
        for (DataDrivenValue val : fAttributes) {
            Object value = val.getValue(event, quark, scenarioInfo, container);
            if (value == null) {
                Activator.logWarning("StateChange.handleEvent: A value is null: " + val); //$NON-NLS-1$
                return ITmfStateSystem.INVALID_ATTRIBUTE;
            }
            quark = container.getQuarkRelativeAndAdd(quark, String.valueOf(value));
            if (quark < 0) {
                Activator.logWarning("The attribute quark is invalid for event " + event + ": " + fAttributes);  //$NON-NLS-1$//$NON-NLS-2$
                break;
            }
        }
        return quark;
    }

    /**
     * Get the quark in the system for this path by resolving each value along
     * the path
     *
     * @param baseQuark
     *            The original base quark, as obtained by the caller
     * @param container
     *            The analysis data container
     * @return The quark when the path is resolved.
     */
    public int getQuark(int baseQuark, IAnalysisDataContainer container) {
        int quark = fQuarkProvider.getBaseQuark(baseQuark, null);
        for (DataDrivenValue val : fAttributes) {
            Object value = val.getValue(null, quark, null, container);
            if (value == null) {
                Activator.logWarning("State system path, a value is null for " + val + " from quark " + quark); //$NON-NLS-1$ //$NON-NLS-2$
                return ITmfStateSystem.INVALID_ATTRIBUTE;
            }
            ITmfStateSystem stateSystem = container.getStateSystem();
            quark = stateSystem.optQuarkRelative(quark, String.valueOf(value));
            if (quark < 0) {
                Activator.logWarning("The attribute quark is invalid: " + fAttributes);  //$NON-NLS-1$
                break;
            }
        }
        return quark;
    }

    @Override
    public String toString() {
        return "DataDrivenStateSystemPath: " + fAttributes; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fAttributes, fQuarkProvider);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DataDrivenStateSystemPath)) {
            return false;
        }
        DataDrivenStateSystemPath other = (DataDrivenStateSystemPath) obj;
        return Objects.equals(fAttributes, other.fAttributes) &&
                Objects.equals(fQuarkProvider, other.fQuarkProvider);
    }

}
