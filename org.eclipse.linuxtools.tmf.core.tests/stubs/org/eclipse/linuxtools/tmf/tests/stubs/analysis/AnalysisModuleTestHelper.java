/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Guilliano Molaire - Implementation of requirements and valid trace types getters
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.analysis;

import java.util.Collections;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub2;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableList;

/**
 * Analysis Module Helper for the stub analysis source
 *
 * @author Geneviève Bastien
 */
public class AnalysisModuleTestHelper implements IAnalysisModuleHelper {

    /**
     * Enum to select an analysis module for this stub
     */
    public enum moduleStubEnum {
        /** Test analysis */
        TEST,
        /** Test analysis 2 */
        TEST2
    }

    private moduleStubEnum fModule;

    /**
     * Constructor
     *
     * @param module
     *            The type identifying the module for this helper
     */
    public AnalysisModuleTestHelper(moduleStubEnum module) {
        fModule = module;
    }

    @Override
    public String getId() {
        return fModule.name();
    }

    @Override
    public String getName() {
        return fModule.name();
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "";
    }

    @Override
    public String getHelpText(@NonNull ITmfTrace trace) {
        return "";
    }

    @Override
    public String getIcon() {
        return "";
    }

    @Override
    public Bundle getBundle() {
        return Platform.getBundle("org.eclipse.linuxtools.tmf.core.tests");
    }

    @Override
    public boolean appliesToTraceType(Class<? extends ITmfTrace> traceclass) {
        switch (fModule) {
        case TEST:
            return TmfTraceStub.class.isAssignableFrom(traceclass);
        case TEST2:
            return TmfTraceStub2.class.isAssignableFrom(traceclass);
        default:
            return false;
        }
    }

    @Override
    public IAnalysisModule newModule(ITmfTrace trace) throws TmfAnalysisException {
        IAnalysisModule module = null;
        switch (fModule) {
        case TEST:
            module = new TestAnalysis();
            module.setName(getName());
            module.setId(getId());
            module.setAutomatic(isAutomatic());
            module.setTrace(trace);
            break;
        case TEST2:
            module = new TestAnalysis2();
            module.setName(getName());
            module.setId(getId());
            module.setAutomatic(isAutomatic());
            module.setTrace(trace);
            break;
        default:
            break;

        }
        return module;
    }

    @Override
    public Iterable<Class<? extends ITmfTrace>> getValidTraceTypes() {
        return ImmutableList.<Class<? extends ITmfTrace>> of(
                TmfTraceStub.class,
                TmfTraceStub2.class);
    }

    @Override
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
        switch (fModule) {
        case TEST:
            return ImmutableList.of(
                    AnalysisRequirementFactory.REQUIREMENT_1,
                    AnalysisRequirementFactory.REQUIREMENT_3);
        case TEST2:
            return ImmutableList.of(
                    AnalysisRequirementFactory.REQUIREMENT_2,
                    AnalysisRequirementFactory.REQUIREMENT_3);
        default:
            return Collections.EMPTY_SET;
        }
    }
}
