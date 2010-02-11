package org.eclipse.linuxtools.lttng.state;

import org.eclipse.linuxtools.tmf.signal.TmfSignal;

public class RequestCompletedSignal extends TmfSignal {

	StateDataRequest request;

	public RequestCompletedSignal(StateDataRequest request) {
		super(request);
		this.request = request;
	}

	public StateDataRequest getRequest() {
		return request;
	}

}
