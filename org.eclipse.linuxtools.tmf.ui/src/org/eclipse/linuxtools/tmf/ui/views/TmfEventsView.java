/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Factored out events table
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.widgets.Composite;

/**
 * <b><u>TmfEventsView</u></b>
 * <p>
 *
 * TODO: Implement me. Please.
 * TODO: Handle column selection, sort, ... generically (nothing less...)
 * TODO: Implement hide/display columns
 */
public class TmfEventsView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.events";

    private TmfExperiment<TmfEvent> fExperiment;
    private TmfEventsTable fEventsTable;
    private static final int DEFAULT_CACHE_SIZE = 1000;
    private final int fCacheSize;
    private String fTitlePrefix;
    
	// ------------------------------------------------------------------------
    // Constructor
	// ------------------------------------------------------------------------

    public TmfEventsView(int cacheSize) {
    	super("TmfEventsView");
    	fCacheSize = cacheSize;
    }

    public TmfEventsView() {
    	this(DEFAULT_CACHE_SIZE);
    }

	// ------------------------------------------------------------------------
    // ViewPart
	// ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {
        fEventsTable = createEventsTable(parent, fCacheSize);

        fTitlePrefix = getTitle();
        
        // If an experiment is already selected, update the table
        fExperiment = (TmfExperiment<TmfEvent>) TmfExperiment.getCurrentExperiment();
        if (fExperiment != null) {
            experimentSelected(new TmfExperimentSelectedSignal<TmfEvent>(fEventsTable, fExperiment));
        }
    }

    @Override
    public void dispose() {
        if (fEventsTable != null) {
            fEventsTable.dispose();
        }
        super.dispose();
    }

    protected TmfEventsTable createEventsTable(Composite parent, int cacheSize) {
        return new TmfEventsTable(parent, cacheSize);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
	public void setFocus() {
        fEventsTable.setFocus();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
    	return "[TmfEventsView]";
    }

    // ------------------------------------------------------------------------
    // Signal handlers
	// ------------------------------------------------------------------------
    
	@SuppressWarnings("unchecked")
    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal<TmfEvent> signal) {
        // Update the trace reference
        fExperiment = (TmfExperiment<TmfEvent>) signal.getExperiment();
        setPartName(fTitlePrefix + " - " + fExperiment.getName());

        if (fEventsTable != null) {
            fEventsTable.setTrace(fExperiment);
        }
    }

}