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
 * Lami 'disk' value.
 *
 * A disk can be "sda". It may contain partitions.
 *
 * @author Philippe Proulx
 */
class LamiDisk extends LamiString {

    public LamiDisk(String value) {
        super(value);
    }

}
