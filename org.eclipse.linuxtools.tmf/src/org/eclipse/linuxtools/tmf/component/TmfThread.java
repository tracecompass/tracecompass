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
//	
//	public int compare(ITmfDataRequest<?> o1, ITmfDataRequest<?> o2) {
//		if (o1.getExecType() == o2.getExecType())
//			return 0;
//		if (o1.getExecType() == ExecutionType.SHORT)
//			return -1;
//		return 1;
//	}

}
