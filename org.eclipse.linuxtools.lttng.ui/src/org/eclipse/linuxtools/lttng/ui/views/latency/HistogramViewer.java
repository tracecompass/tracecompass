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
package org.eclipse.linuxtools.lttng.ui.views.latency;

import org.eclipse.linuxtools.lttng.ui.views.latency.listeners.HistogramPaintListener;
import org.eclipse.linuxtools.lttng.ui.views.latency.listeners.TooltipListener;
import org.eclipse.linuxtools.lttng.ui.views.latency.listeners.ZoomListener;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramDataModel;
import org.eclipse.linuxtools.tmf.ui.views.histogram.IHistogramDataModel;
import org.eclipse.linuxtools.tmf.ui.views.histogram.IHistogramModelListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * <b><u>HistogramViewer</u></b>
 * <p>
 * 
 * Histogram viewer.
 * 
 * @author Philippe Sawicki
 */
public class HistogramViewer extends AbstractViewer implements IHistogramModelListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	/**
	 * Usable width for data plotting.
	 */
	protected int fUsableWidth;

	/**
	 * Latency histogram model.
	 */
	private HistogramDataModel fModel;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param parent The parent composite node.
	 * @param style The SWT style to use to render the view.
	 */
	public HistogramViewer(Composite parent, int style) {
		super(parent, style);
		
		// Register the paint listener
		fPaintListener = new HistogramPaintListener(this);
		addPaintListener(fPaintListener);
		
		// Register the zoom listener
		fZoomListener = new ZoomListener(this);
		addListener(SWT.MouseWheel, fZoomListener);
		
		// Register the mouse click listener
		fMouseTraceListener = new TooltipListener(this, (HistogramPaintListener)fPaintListener);
		addMouseTrackListener(fMouseTraceListener);
		
		fModel = new HistogramDataModel();
		fModel.addHistogramListener(this);
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
	    fModel.removeHistogramListener(this);
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
	    modelUpdated();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer#decreaseBarWidth()
	 */
	@Override
    public void decreaseBarWidth() {
	    fPaintListener.decreaseBarWitdh();
	    modelUpdated();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer#getModel()
	 */
	@Override
    public IHistogramDataModel getModel() {
        return fModel;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.IHistogramModelListener#modelUpdated()
	 */
    @Override
    public void modelUpdated() {
        
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
}