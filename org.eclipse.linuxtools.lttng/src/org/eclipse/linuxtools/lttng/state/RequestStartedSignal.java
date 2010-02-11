package org.eclipse.linuxtools.lttng.state;

import org.eclipse.linuxtools.tmf.signal.TmfSignal;

public class RequestStartedSignal extends TmfSignal {

	StateDataRequest request;

	public RequestStartedSignal(StateDataRequest request) {
		super(request);
		this.request = request;
	}

	public StateDataRequest getRequest() {
		return request;
	}

}
