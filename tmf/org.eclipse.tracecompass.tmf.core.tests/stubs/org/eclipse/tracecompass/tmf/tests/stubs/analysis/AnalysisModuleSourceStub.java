/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleSource;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.AnalysisModuleTestHelper.moduleStubEnum;

/**
 * Stub class for analysis module source
 *
 * @author Geneviève Bastien
 */
public class AnalysisModuleSourceStub implements IAnalysisModuleSource {

    @Override
    public Iterable<IAnalysisModuleHelper> getAnalysisModules() {
        List<IAnalysisModuleHelper> list = new ArrayList<>();
        IAnalysisModuleHelper helper = new AnalysisModuleTestHelper(moduleStubEnum.TEST);
        list.add(helper);
        helper = new AnalysisModuleTestHelper(moduleStubEnum.TEST2);
        list.add(helper);
        return list;
    }

}
