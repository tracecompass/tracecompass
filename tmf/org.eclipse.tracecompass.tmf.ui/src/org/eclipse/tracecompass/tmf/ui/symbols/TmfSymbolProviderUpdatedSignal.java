/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.symbols;

import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;

/**
 * Symbol provider is updated. It will be fired when the symbol provider loads a
 * new mapping. This should be listened to by all views and viewers that need to
 * display items using the symbol map.
 * <p>
 * FIXME: move to core when possible
 *
 * @author Matthew Khouzam
 * @since 2.2
 */
public class TmfSymbolProviderUpdatedSignal extends TmfSignal {
    /**
     * Constructor
     *
     * @param source
     *            the symbol source
     */
    public TmfSymbolProviderUpdatedSignal(Object source) {
        super(source);
    }
}
