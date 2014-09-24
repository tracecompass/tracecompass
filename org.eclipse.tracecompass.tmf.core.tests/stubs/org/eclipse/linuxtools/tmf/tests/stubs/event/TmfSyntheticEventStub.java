/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.event;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;

/**
 * <b><u>TmfSyntheticEventStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("javadoc")
public class TmfSyntheticEventStub extends TmfEvent {


    public TmfSyntheticEventStub(final ITmfEvent event) {
        super(event);
    }
    public TmfSyntheticEventStub(final TmfSyntheticEventStub other) {
        super(other);
    }

}
