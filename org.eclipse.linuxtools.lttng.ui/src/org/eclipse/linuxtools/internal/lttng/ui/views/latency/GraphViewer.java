/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *   Bernd Hufmann - Adapted to new model-view-controller design
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.latency;

import org.eclipse.linuxtools.internal.lttng.ui.views.latency.listeners.GraphMouseListener;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.listeners.GraphPaintListener;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.listeners.TimePointerListener;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.model.IGraphDataModel;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.model.IGraphModelListener;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.model.LatencyGraphModel;
import org.eclipse.swt.widgets.Composite;

/**
 * <b><u>GraphViewer</u></b>
 * <p>
 * Graph viewer.
 * 
 * @author Philippe Sawicki
 */
public class GraphViewer extends AbstractViewer implements IGraphModelListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Latency graph model
     */
    private LatencyGraphModel fModel;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param parent The parent composite node.
	 * @param style The SWT style to use to render the view.
	 */
	public GraphViewer(Composite parent, int style) {
		super(parent, style);

		// Register the paint listener
		fPaintListener = new GraphPaintListener(this);
		addPaintListener(fPaintListener);

		// Register the mouse track listener
		fMouseTraceListener = new TimePointerListener(this, (GraphPaintListener)fPaintListener);
		addMouseTrackListener(fMouseTraceListener);

		// Register mouse listener
		fMouseListener = new GraphMouseListener(this, (GraphPaintListener)fPaintListener);
		addMouseListener(fMouseListener);

		fModel = new LatencyGraphModel();
        fModel.addGraphModelListener(this);
	}

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer#dispose()
	 */
	@Override
	public void dispose() {
	    fModel.removeGraphModelListener(this);
	    fPaintListener.dispose();
	    super.dispose();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer#clear()
	 */
	@Override
	public void clear() {
		fPaintListener.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer#clearBackground()
	 */
	@Override
	public void clearBackground() {
		fPaintListener.clear();
	}

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer#increaseBarWidth()
     */
    @Override
    public void increaseBarWidth() {
        fPaintListener.increaseBarWitdh();
        graphModelUpdated();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer#decreaseBarWidth()
     */
    @Override
    public void decreaseBarWidth() {
        fPaintListener.decreaseBarWitdh();
        graphModelUpdated();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer#getModel()
     */
    @Override
    public IGraphDataModel getModel() {
        return fModel;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.model.IGraphModelListener#graphModelUpdated()
     */
    @Override
    public void graphModelUpdated() {
        if (!isDisposed() && getDisplay() != null) {
            getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!isDisposed()) {
                        redraw();
                    }
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.model.IGraphModelListener#currentEventUpdated()
     */
    @Override
    public void currentEventUpdated(long currentEventTime) {
        graphModelUpdated();
    }
}