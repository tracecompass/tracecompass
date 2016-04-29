/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.inputoutput;

import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * State values that are used in the io analysis event handlers.
 *
 * @author Houssem Daoud
 */
public interface StateValues {

    /* IO Operation type values */
    /** Value for write request */
    int WRITING_REQUEST = 1;
    /** Value for read requests */
    int READING_REQUEST = 2;

    /** State value for write requests */
    ITmfStateValue WRITING_REQUEST_VALUE = TmfStateValue.newValueInt(WRITING_REQUEST);
    /** State value for read requests */
    ITmfStateValue READING_REQUEST_VALUE = TmfStateValue.newValueInt(READING_REQUEST);
}
