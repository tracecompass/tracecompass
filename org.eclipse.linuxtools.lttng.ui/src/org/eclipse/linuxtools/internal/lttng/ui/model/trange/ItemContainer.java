package org.eclipse.linuxtools.internal.lttng.ui.model.trange;

import org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;

public interface ItemContainer<T extends ITmfTimeAnalysisEntry> {

	/**
	 * Interface to add resources.
	 * 
	 * @param process
	 */
	public abstract void addItem(T newItem);

	// ========================================================================
	// Methods
	// ========================================================================
	/**
	 * Request a unique ID
	 * 
	 * @return Integer
	 */
	public abstract Integer getUniqueId();

	/**
	 * This method is intended for read only purposes in order to keep the
	 * internal data structure in Synch
	 * 
	 * @return
	 */
	public abstract T[] readItems();

	/**
	 * Clear the children information for resources related to a specific trace
	 * e.g. just before refreshing data with a new time range
	 * 
	 * @param traceId
	 */
	public abstract void clearChildren();

	/**
	 * Clear all resources items e.g. when a new experiment is selected
	 */
	public abstract void clearItems();

	/**
	 * Remove the resources related to a specific trace e.g. during trace
	 * removal
	 * 
	 * @param traceId
	 */
	public abstract void removeItems(String traceId);

}