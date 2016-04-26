/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.ondemand;

/**
 * Reports are the output of on-demand analysis ran on a particular trace.
 * Several runs of the same ODA should produce different report objects
 * (although their contents could be the same).
 *
 * They are meant to be standalone objects, but should normally keep track of
 * which analysis and trace created them.
 *
 * Unlike on-demand analyses, a report is specific to its trace.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public interface IOnDemandAnalysisReport {

    /**
     * Get the name of this report.
     *
     * @return The name of this report
     */
    String getName();
}
