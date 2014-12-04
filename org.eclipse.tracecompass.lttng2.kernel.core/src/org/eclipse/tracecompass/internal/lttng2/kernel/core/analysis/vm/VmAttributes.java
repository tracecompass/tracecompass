/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm;

/**
 * Attributes used by the VM Analysis
 *
 * @author Mohamad Gebai
 */
@SuppressWarnings({"nls"})
public interface VmAttributes {

    /** First-level attributes */
    String VIRTUAL_MACHINES = "Virtual Machines";

    /** Sub-attributes for virtual CPUs */
    String STATUS = "Status";

}
