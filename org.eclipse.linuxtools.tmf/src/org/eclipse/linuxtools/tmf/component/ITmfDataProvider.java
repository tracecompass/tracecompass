package org.eclipse.linuxtools.tmf.component;

import org.eclipse.linuxtools.tmf.event.TmfData;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;

public interface ITmfDataProvider<T extends TmfData> {

    /**
     * Queues the request for processing.
     * 
     * If the request can't be serviced, it will fail (i.e. isFailed() will be set).
     * 
	 * @param request The request to process
	 */
	public void sendRequest(TmfDataRequest<T> request);

}
