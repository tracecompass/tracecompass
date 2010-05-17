/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.state;


/**
 * This class is an extension of Data Request which includes specific references
 * i.e. a status listener to indicate the start and end of the request
 * 
 * @author alvaro
 * 
 */
public class StateDataRequest {
	// //
	// ========================================================================
	// // Data
	// //
	// =======================================================================
	// private Vector<IStateDataRequestListener> listeners = new
	// Vector<IStateDataRequestListener>();
	// private String transactionId = ""; /* optional user's attribute */
	// private StateManager manager = null;
	// private long numOfEvents = 0;
	// private boolean broadcast = false;
	// private boolean clearDataInd = false;
	// //
	// ========================================================================
	// // Constructors
	// //
	// =======================================================================
	// /**
	// * @param range
	// * @param offset
	// * @param nbEvents
	// * @param maxBlockSize
	// * @param listener
	// */
	// public StateDataRequest(TmfTimeRange range, long offset, int nbEvents,
	// int maxBlockSize, IStateDataRequestListener listener,
	// StateManager manager) {
	//		
	// super(range, nbEvents, maxBlockSize);
	// //super(0, nbEvents, maxBlockSize);
	// this.manager = manager;
	// if (listener != null && !listeners.contains(listener)) {
	// listeners.add(listener);
	// }
	// }
	//
	// /**
	// * @param range
	// * @param offset
	// * @param nbEvents
	// * @param maxBlockSize
	// * @param listener
	// * @param transactionID
	// * optional use by user application
	// */
	// public StateDataRequest(TmfTimeRange range, long offset, int nbEvents,
	// int maxBlockSize, IStateDataRequestListener listener,
	// String transactionID, StateManager manager) {
	//
	// this(range, offset, nbEvents, maxBlockSize, listener, manager);
	// this.transactionId = transactionID;
	// }
	//
	// //
	// ========================================================================
	// // Methods
	// //
	// =======================================================================
	//
	// /**
	// * Trigger the start to process this request right after the notification
	// to
	// * the interested listeners
	// *
	// * @param experiment
	// * @param broadcast
	// * true: All views, false: only to registered listeners
	// */
	// public void startRequestInd(TmfExperiment experiment, boolean broadcast,
	// boolean waitForCompletion) {
	// if (broadcast) {
	// // Notify all state views.
	// this.broadcast = broadcast;
	// // TmfSignalManager.dispatchSignal(new RequestStartedSignal(this));
	// }
	// // else {
	// // // Notify specific state views
	// // for (IStateDataRequestListener listener : listeners) {
	// // listener.processingStarted(new RequestStartedSignal(this));
	// // }
	// // }
	//
	// // trigger the start to process this request
	// experiment.processRequest(this, waitForCompletion);
	// }
	//
	// /**
	// * to be called by the handleCompletion in superclass method, notifies the
	// * interested listeners. i.e. if the request start indicated broadcast,
	// the
	// * completion will also be broadcasted otherwise only registered listeners
	// * will be notified.
	// */
	// public void notifyCompletion() {
	// if (broadcast) {
	// // Notify all state views.
	// TraceDebug.debug("request completed" + this.toString());
	// TmfSignalManager.dispatchSignal(new RequestCompletedSignal(this));
	// } else {
	// // Notify specific state views
	// for (IStateDataRequestListener listener : listeners) {
	// listener.processingCompleted(new RequestCompletedSignal(this));
	// }
	// }
	// }
	//
	// public void notifyStarting() {
	// if (broadcast) {
	// // Notify all state views.
	// TraceDebug.debug("request started" + this.toString());
	// TmfSignalManager.dispatchSignal(new RequestStartedSignal(this));
	// } else {
	// for (IStateDataRequestListener listener : listeners) {
	// listener.processingStarted(new RequestStartedSignal(this));
	// }
	// }
	// }
	//
	// public String getTransactionId() {
	// return transactionId;
	// }
	//
	// public StateManager getStateManager() {
	// return this.manager;
	// }
	//
	// // public IStateDataRequestListener getListener() {
	// // return listener;
	// // }
	//
	// public void addListener(IStateDataRequestListener listener) {
	// if (!listeners.contains(listener)) {
	// listeners.add(listener);
	// }
	// }
	//
	// public void removeListener(IStateDataRequestListener listener) {
	// if (listener != null) {
	// listeners.remove(listener);
	// }
	// }
	//
	// /**
	// * @param numOfEvents
	// * the numOfEvents to set
	// */
	// public void setNumOfEvents(long numOfEvents) {
	// this.numOfEvents = numOfEvents;
	// }
	//
	// /**
	// * @return the numOfEvents
	// */
	// public long getNumOfEvents() {
	// return numOfEvents;
	// }
	//
	// /**
	// * @param clearAllData
	// * indicates the need to clear all previous data e.g. a new
	// * experiment selection
	// */
	// public void setclearDataInd(boolean clearAllData) {
	// this.clearDataInd = clearAllData;
	// }
	//
	// /**
	// * Returns indication - clearing of all existing data model is required
	// e.g
	// * from the selection of a new experiment
	// *
	// * @return
	// */
	// public boolean isclearDataInd() {
	// return clearDataInd;
	// }
	//
	// /**
	// * Compare the time range with a Data Request.
	// *
	// * @param trange
	// * @return
	// */
	// public boolean equalTime(StateDataRequest otherRequest) {
	// TmfTimeRange trange = otherRequest.getRange();
	// TmfTimeRange myTimeRange = getRange();
	// TmfTimestamp myStartTime = myTimeRange.getStartTime();
	// TmfTimestamp myEndTime = myTimeRange.getEndTime();
	//		
	// if (myStartTime.equals(trange.getStartTime())) {
	// if (myEndTime.equals(trange.getEndTime())) {
	// return true;
	// }
	// }
	//		
	// return false;
	// }
}
