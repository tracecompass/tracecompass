package org.eclipse.linuxtools.lttng.request;

import org.eclipse.linuxtools.tmf.signal.TmfSignal;

public class RequestCompletedSignal extends TmfSignal {

	LttngSyntEventRequest request;

	public RequestCompletedSignal(LttngSyntEventRequest request) {
		super(request);
		this.request = request;
	}

	public LttngSyntEventRequest getRequest() {
		return request;
	}

}
