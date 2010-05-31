package org.eclipse.linuxtools.lttng.request;

import org.eclipse.linuxtools.tmf.signal.TmfSignal;

public class RequestStartedSignal extends TmfSignal {

	LttngSyntEventRequest request;

	public RequestStartedSignal(LttngSyntEventRequest request) {
		super(request);
		this.request = request;
	}

	public LttngSyntEventRequest getRequest() {
		return request;
	}

}
