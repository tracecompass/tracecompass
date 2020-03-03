/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.examples.ui.views.data.provider;

import org.eclipse.tracecompass.examples.core.data.provider.ExampleTimeGraphDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph.BaseDataProviderTimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;

/**
 * An example of a data provider time graph view
 *
 * This class is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("restriction")
public class ExampleTimeGraphDataProviderView extends BaseDataProviderTimeGraphView {

    /** View ID. */
    public static final String ID = "org.eclipse.tracecompass.examples.dataprovider.tgview"; //$NON-NLS-1$

    /**
     * Default constructor
     */
    public ExampleTimeGraphDataProviderView() {
       super(ID, new BaseDataProviderTimeGraphPresentationProvider(), ExampleTimeGraphDataProvider.ID);
    }

}