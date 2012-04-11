/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/

 package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * <b><u>ITimeAnalysisWidget</u></b>
 * <p>
 *
 * TODO: Implement me. Please.
 */
public interface ITimeAnalysisViewer extends ITmfViewer {

    public enum TimeFormat {
		RELATIVE, ABSOLUTE
	};

	/**
	 * @param e
	 */
	public void controlResized(ControlEvent e);

	/**
	 * 
	 * @param traceArr
	 * @param start
	 *            Specifies a fixed start time to the information to be
	 *            displayed
	 * @param end
	 *            Specifies a fixed end time to the information to be displayed
	 * @param updateTimeBounds
	 *            If True - Time Range boundaries update is required
	 */
	public abstract void display(ITmfTimeAnalysisEntry[] traceArr, long start,
			long end, boolean updateTimeBounds);

	/**
	 * The start and End time are taken from the limits used by the children
	 * events
	 * 
	 * @param traceArr
	 */
	public abstract void display(ITmfTimeAnalysisEntry[] traceArr);

	public void addWidgetSelectionListner(ITmfTimeSelectionListener listener);

	public void addWidgetTimeScaleSelectionListner(
			ITmfTimeScaleSelectionListener listener);

	public void filterTraces();

	public ITmfTimeAnalysisEntry getSelectedTrace();

	public ISelection getSelection();

	public void groupTraces(boolean on);

	public boolean isInFocus();

	public void removeWidgetSelectionListner(ITmfTimeSelectionListener listener);

	public void removeWidgetTimeScaleSelectionListner(
			ITmfTimeScaleSelectionListener listener);

	public void resetStartFinishTime();

	public void selectNextEvent();

	public void selectPrevEvent();

	public void selectNextTrace();

	public void selectPrevTrace();

	public void showLegend();

	public void zoomIn();

	public void zoomOut();

	public void setSelectedTime(long time, boolean ensureVisible, Object source);

	public void setSelectedEvent(ITimeEvent event, Object Source);

	public void setSelectedTraceTime(ITmfTimeAnalysisEntry trace, long time, Object Source);

	public void setSelectVisTimeWindow(long time0, long time1, Object Source);

	public void setAcceptSelectionAPIcalls(boolean acceptCalls);

	public void setTimeCalendarFormat(boolean toAbsoluteCaltime);

	public boolean isCalendarFormat();

	public boolean isVisibleVerticalScroll();

	public void setVisibleVerticalScroll(boolean visibleVerticalScroll);

	public int getBorderWidth();

	public void setBorderWidth(int borderWidth);

	public int getHeaderHeight();

	public void setHeaderHeight(int headerHeight);

	public int getItemHeight();

	public void setItemHeight(int rowHeight);

    public void setMinimumItemWidth(int width);

	public void resizeControls();

	public void setSelectedTrace(ITmfTimeAnalysisEntry trace);

	public ISelection getSelectionTrace();

	public void setNameWidthPref(int width);

	public int getNameWidthPref(int width);

	public void addFilterSelectionListner(ITmfTimeFilterSelectionListener listener);

	public void removeFilterSelectionListner(
			ITmfTimeFilterSelectionListener listener);

	public int getTimeSpace();

	public void itemUpdate(ITmfTimeAnalysisEntry parent, TimeEvent item);

	public Control getControl();

	public ISelectionProvider getSelectionProvider();

	/**
	 * <p>
	 * Provide the possibility to control the wait cursor externally
	 * </p>
	 * <p>
	 * e.g. data requests in progress
	 * </p>
	 * 
	 * @param waitInd
	 *            - true change to wait cursor
	 */
	public void waitCursor(boolean waitInd);

    public void setFocus();

    /**
     * Update the time bounds without changing the visible range
     * 
     * @param beginTime
     * @param endTime
     */
    public void setTimeBounds(long beginTime, long endTime);

    /**
     * Get the horizontal scrollbar
     * @return the horizontal scrollbar
     */
    public ScrollBar getHorizontalBar();

    /**
     * Get the vertical scrollbar
     * @return the vertical scrollbar
     */
    public ScrollBar getVerticalBar();

    /**
     * Set the top index
     * @param index the top index
     */
    public void setTopIndex(int index);

}