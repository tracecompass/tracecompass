/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
