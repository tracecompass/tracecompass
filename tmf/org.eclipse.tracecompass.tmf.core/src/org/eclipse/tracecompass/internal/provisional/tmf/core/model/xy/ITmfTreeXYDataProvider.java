/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;

/**
 * Super interface to define providers which can return a tree and an XY series
 *
 * @param <M>
 *            the type of {@link ITmfTreeDataModel} that this
 *            {@link ITmfTreeDataProvider} must return
 * @author Loic Prieur-Drevon
 */
public interface ITmfTreeXYDataProvider<M extends ITmfTreeDataModel> extends ITmfXYDataProvider, ITmfTreeDataProvider<M> {

}
