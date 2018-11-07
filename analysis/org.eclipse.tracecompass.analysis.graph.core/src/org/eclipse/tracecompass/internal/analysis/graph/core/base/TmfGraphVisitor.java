/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francis Giraldeau - Initial implementation and API
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.base;

import org.eclipse.tracecompass.analysis.graph.core.base.ITmfGraphVisitor;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;

/**
 * An empty implementation of the graph visitor
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public class TmfGraphVisitor implements ITmfGraphVisitor {

    @Override
    public void visitHead(TmfVertex node) {
        // Do nothing
    }

    @Override
    public void visit(TmfVertex node) {
        // Do nothing
    }

    @Override
    public void visit(TmfEdge edge, boolean horizontal) {
        // Do nothing
    }

}
