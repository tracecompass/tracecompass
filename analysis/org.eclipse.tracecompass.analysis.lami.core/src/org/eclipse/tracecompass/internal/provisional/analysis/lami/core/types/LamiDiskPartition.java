/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

/**
 * Lami 'disk partition' value.
 *
 * A disk partition is something like "sda2".
 *
 * @author Philippe Proulx
 */
class LamiDiskPartition extends LamiString {

    public LamiDiskPartition(String value) {
        super(value);
    }

}
