/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleSource;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.AnalysisModuleTestHelper.moduleStubEnum;

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
        helper = new AnalysisModuleTestHelper(moduleStubEnum.TESTCTF);
        list.add(helper);
        return list;
    }

}
