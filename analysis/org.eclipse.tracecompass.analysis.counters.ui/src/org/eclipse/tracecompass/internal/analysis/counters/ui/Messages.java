/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.counters.ui;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle
 *
 * @author Mikael Ferland
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private Messages() {
    }

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.counters.ui.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String CounterView_CumulativeAction_Text;
    public static String CounterView_CumulativeAction_CumulativeTooltipText;
    public static String CounterView_CumulativeAction_DifferentialTooltipText;

}
