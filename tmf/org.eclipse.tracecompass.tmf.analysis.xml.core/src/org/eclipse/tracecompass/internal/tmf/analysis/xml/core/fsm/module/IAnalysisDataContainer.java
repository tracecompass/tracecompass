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

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;

/**
 * Interface that all XML defined objects who provide, use or contain data
 * containers such as state systems or segment store must implement in order to
 * use the state model elements in the
 * {@link org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model}
 * package
 *
 * TODO: This class is now really linked with a state system and have
 * state-systemy methods. Try to make it more generic, so that other DBs can
 * implement this as well.
 *
 * @author Geneviève Bastien
 */
public interface IAnalysisDataContainer {

    /**
     * Error quark, value taken when a state system quark query is in error.
     */
    int ERROR_QUARK = -2;

    /**
     * Get the state system managed by this XML object
     *
     * @return The state system
     */
    ITmfStateSystem getStateSystem();

    /**
     * Get a mapping group with requested ID. The mapping group should never be null
     * as it was validated at compile-time
     *
     * @param id
     *            The ID of the mapping group
     * @return The mapping group
     */
    DataDrivenMappingGroup getMappingGroup(String id);

    /**
     * Get an attribute pool starting at the requested quark
     *
     * @param startNodeQuark
     *            The quark of the attribute to get the pool for
     * @return The attribute pool starting at the requested quark
     */
    default @Nullable TmfAttributePool getAttributePool(int startNodeQuark) {
        return null;
    }

    /**
     * Basic quark-retrieving method. Pass an attribute in parameter as an array of
     * strings, the matching quark will be returned. If the attribute does not
     * exist, it will add the quark to the state system if the context allows it.
     * Otherwise a negative value will be returned.
     *
     * See {@link ITmfStateSystemBuilder#getQuarkAbsoluteAndAdd(String...)}
     *
     * @param path
     *            Full path to the attribute
     * @return The quark for this attribute
     */
    default int getQuarkAbsoluteAndAdd(String... path) {
        ITmfStateSystem stateSystem = getStateSystem();
        int quark = stateSystem.optQuarkAbsolute(path);
        if (quark == ITmfStateSystem.INVALID_ATTRIBUTE && (stateSystem instanceof ITmfStateSystemBuilder)) {
            quark = ((ITmfStateSystemBuilder) stateSystem).getQuarkAbsoluteAndAdd(path);
        }
        return quark;
    }

    /**
     * Quark-retrieving method, but the attribute is queried starting from the
     * startNodeQuark. If the attribute does not exist, it will add it to the state
     * system if the context allows it. Otherwise a negative value will be returned.
     *
     * See {@link ITmfStateSystemBuilder#getQuarkRelativeAndAdd(int, String...)}
     *
     * @param startNodeQuark
     *            The quark of the attribute from which 'path' originates.
     * @param path
     *            Relative path to the attribute
     * @return The quark for this attribute
     */
    default int getQuarkRelativeAndAdd(int startNodeQuark, String... path) {
        ITmfStateSystem stateSystem = getStateSystem();
        int quark = stateSystem.optQuarkRelative(startNodeQuark, path);
        if (quark == ITmfStateSystem.INVALID_ATTRIBUTE && (stateSystem instanceof ITmfStateSystemBuilder)) {
            quark = ((ITmfStateSystemBuilder) stateSystem).getQuarkRelativeAndAdd(startNodeQuark, path);
        }
        return quark;
    }

    /**
     * Add a future state to the state provider associated with this container.
     * The state change will occur when the analysis has reached the specified
     * time.
     *
     * @param time
     *            The time at which to add the state. It has to be later than
     *            the current time of the analysis
     * @param state
     *            The value the state will take at time
     * @param quark
     *            The quark for which to add the state change
     */
    default void addFutureState(long time, @Nullable Object state, int quark) {
        throw new UnsupportedOperationException("Implementations should override this method"); //$NON-NLS-1$
    }

    /**
     * Set the script engine by name. This method should only be implemented by
     * analysis container. This avoid to create a new script engine every time
     * we need to evaluate a new expression.
     *
     * @param name
     *            the name of the script engine
     *
     * @param engine
     *            The script engine use to evaluate expressions
     */
    default void setScriptengine(String name, ScriptEngine engine) {
        // Do nothing. This method should be overridden by child classes who need those
    }

    /**
     * Get the container script engine
     *
     * @param name
     *            the name of the script engine
     *
     * @return The script engine used to evaluate expressions
     */
    default @Nullable ScriptEngine getScriptEngine(String name) {
        // Return null as default value. This method should be overridden by child classes who need those
        return null;
    }
}
