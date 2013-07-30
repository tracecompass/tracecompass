/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.request;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;

/**
 *
 */
public class TmfDataRequestStub extends TmfDataRequest {

    /**
     * Default constructor
     *
     * @param dataType the request data type
     */
    public TmfDataRequestStub(final Class<? extends ITmfEvent> dataType) {
        super(dataType, 0, ALL_DATA, ExecutionType.FOREGROUND);
    }

    /**
     * @param dataType the request data type
     * @param index the initial event index
     */
    public TmfDataRequestStub(final Class<? extends ITmfEvent> dataType, final int index) {
        super(dataType, index, ALL_DATA, ExecutionType.FOREGROUND);
    }

    /**
     * @param dataType the request data type
     * @param index the initial event index
     * @param nbRequested the number of events requested
     */
    public TmfDataRequestStub(final Class<? extends ITmfEvent> dataType,
            final int index, final int nbRequested) {
        super(dataType, index, nbRequested, ExecutionType.FOREGROUND);
    }

    @Override
    public void handleData(final ITmfEvent data) {
        super.handleData(data);
    }

}
