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

package org.eclipse.linuxtools.tmf.ui.views.events;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
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

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.events"; //$NON-NLS-1$

    private TmfExperiment<TmfEvent> fExperiment;
    private TmfEventsTable fEventsTable;
    private static final int DEFAULT_CACHE_SIZE = 100;
    private final int fCacheSize;
    private String fTitlePrefix;
    
	// ------------------------------------------------------------------------
    // Constructor
	// ------------------------------------------------------------------------

    public TmfEventsView(int cacheSize) {
    	super("TmfEventsView"); //$NON-NLS-1$
    	fCacheSize = cacheSize;
    }

    public TmfEventsView() {
    	this(DEFAULT_CACHE_SIZE);
    }

	// ------------------------------------------------------------------------
    // ViewPart
	// ------------------------------------------------------------------------

	@Override
    @SuppressWarnings("unchecked")
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
	@SuppressWarnings("nls")
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
        TmfExperiment<TmfEvent> exp = (TmfExperiment<TmfEvent>) signal.getExperiment();
        if (!exp.equals(fExperiment)) {
        	fExperiment = exp;
            setPartName(fTitlePrefix + " - " + fExperiment.getName()); //$NON-NLS-1$
            if (fEventsTable != null) {
            	fEventsTable.setTrace(fExperiment, false);
                fEventsTable.refreshBookmarks(fExperiment.getResource());
            }
        }
    }

	@SuppressWarnings("unchecked")
	@TmfSignalHandler
	public void experimentDisposed(TmfExperimentDisposedSignal<TmfEvent> signal) {
		// Clear the trace reference
		TmfExperiment<TmfEvent> experiment = (TmfExperiment<TmfEvent>) signal.getExperiment();
		if (experiment.equals(fExperiment)) {
			fEventsTable.setTrace(null, false);
		}

		TmfUiPlugin.getDefault().getWorkbench().getWorkbenchWindows()[0].getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				setPartName(fTitlePrefix);
			}
		});
	}

}