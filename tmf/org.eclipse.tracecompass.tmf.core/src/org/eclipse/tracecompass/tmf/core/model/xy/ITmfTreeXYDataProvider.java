/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.xy;

import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;

/**
 * Super interface to define providers which can return a tree and an XY series
 *
 * @param <M>
 *            the type of {@link ITmfTreeDataModel} that this
 *            {@link ITmfTreeDataProvider} must return
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public interface ITmfTreeXYDataProvider<M extends ITmfTreeDataModel> extends ITmfXYDataProvider, ITmfTreeDataProvider<M> {

}
