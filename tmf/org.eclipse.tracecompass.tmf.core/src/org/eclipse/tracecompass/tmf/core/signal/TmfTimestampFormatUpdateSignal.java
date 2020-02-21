/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

/**
 * The default timestamp/interval format was modified
 *
 * @author Francois Chouinard
 */
public class TmfTimestampFormatUpdateSignal extends TmfSignal {

    /**
     * @param source the signal source
     */
    public TmfTimestampFormatUpdateSignal(Object source) {
        super(source);
    }

}
