package org.eclipse.linuxtools.internal.lttng.core.request;

import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;

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
