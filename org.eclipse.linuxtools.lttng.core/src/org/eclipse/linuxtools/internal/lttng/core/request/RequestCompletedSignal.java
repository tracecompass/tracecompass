package org.eclipse.linuxtools.internal.lttng.core.request;

import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;

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
