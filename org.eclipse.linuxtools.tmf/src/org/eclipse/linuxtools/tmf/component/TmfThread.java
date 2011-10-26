package org.eclipse.linuxtools.tmf.component;

import org.eclipse.linuxtools.tmf.request.ITmfDataRequest.ExecutionType;

public class TmfThread extends Thread { // implements Comparator<ITmfDataRequest<?>> {

	private final ExecutionType fExecType;
	
	public TmfThread(ExecutionType execType) {
		fExecType = execType;
	}
	
	public ExecutionType getExecType() {
		return fExecType;
	}

	public void cancel() {
	}

}
