/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.analysis;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Stub test provider for test state system analysis module
 *
 * @author Geneviève Bastien
 */
public class TestStateSystemProvider extends AbstractTmfStateProvider {

    private static final int VERSION = 1;
    private final String fString = "[]";
    private int fCount = 0;

    /**
     * Constructor
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     */
    public TestStateSystemProvider(ITmfTrace trace) {
        super(trace, TmfEvent.class, "Stub State System");
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new TestStateSystemProvider(this.getTrace());
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        /* Just need something to fill the state system */
        if (fString.equals(event.getContent().getValue())) {
            try {
                int quarkId = ss.getQuarkAbsoluteAndAdd("String");
                int quark = ss.getQuarkRelativeAndAdd(quarkId, fString);
                ss.modifyAttribute(event.getTimestamp().getValue(), TmfStateValue.newValueInt(fCount++), quark);
            } catch (TimeRangeException e) {

            } catch (AttributeNotFoundException e) {

            } catch (StateValueTypeException e) {

            }
        }
    }

}
