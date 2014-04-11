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

package org.eclipse.linuxtools.tmf.core.tests.shared;

import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;

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
                Object obj = method.invoke(module, new NullProgressMonitor());
                return (Boolean) obj;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                fail(e.toString());
            }
        }
        throw new RuntimeException("This analysis module does not have a protected method to execute. Maybe it can be executed differently? Or it is not supported yet in this method?");
    }

}
