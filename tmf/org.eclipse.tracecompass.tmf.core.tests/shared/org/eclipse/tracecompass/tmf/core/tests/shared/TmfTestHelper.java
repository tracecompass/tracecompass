/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
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

package org.eclipse.tracecompass.tmf.core.tests.shared;

import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;

/**
 * This class contains code for some common use cases of things that would be
 * illegal to do in normal code, but are useful if not necessary in unit tests,
 * like executing protected methods, using reflection.
 *
 * It may also serve as example for developers who want to do similar things in
 * less common use cases.
 *
 * @author Geneviève Bastien
 */
public class TmfTestHelper {

    /**
     * Calls the {@link TmfAbstractAnalysisModule#executeAnalysis} method of an
     * analysis module. This method does not return until the analysis is
     * completed and it returns the result of the method. It allows to execute
     * the analysis without requiring an Eclipse job and waiting for completion.
     *
     * Note that executing an analysis using this method will not automatically
     * execute the dependent analyses module. The execution of those modules is
     * left to the caller.
     *
     * @param module
     *            The analysis module to execute
     * @return The return value of the
     *         {@link TmfAbstractAnalysisModule#executeAnalysis} method
     */
    public static boolean executeAnalysis(IAnalysisModule module) {
        if (module instanceof TmfAbstractAnalysisModule) {
            try {
                Class<?>[] argTypes = new Class[] { IProgressMonitor.class };
                Method method = TmfAbstractAnalysisModule.class.getDeclaredMethod("executeAnalysis", argTypes);
                method.setAccessible(true);
                Boolean result = (Boolean) method.invoke(module, new NullProgressMonitor());
                // Set the module as completed, to avoid another call creating a job
                method = TmfAbstractAnalysisModule.class.getDeclaredMethod("setAnalysisCompleted", new Class[] { } );
                method.setAccessible(true);
                method.invoke(module);
                return result;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                fail(e.toString());
            }
        }
        throw new RuntimeException("This analysis module does not have a protected method to execute. Maybe it can be executed differently? Or it is not supported yet in this method?");
    }

}
