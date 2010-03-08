package org.eclipse.linuxtools.tmf.request;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TmfRequestExecutor implements Executor {

	private final Executor fExecutor;
	private final Queue<Runnable> fRequests = new LinkedBlockingQueue<Runnable>();
	private Runnable fRequest;
	
	public TmfRequestExecutor(Executor executor) {
		fExecutor = executor;
	}

	public TmfRequestExecutor() {
		this(Executors.newSingleThreadExecutor());
	}

	public void execute(final Runnable request) {
		fRequests.offer(new Runnable() {
			public void run() {
				try {
					request.run();
				} finally {
					scheduleNext();
				}
			}
		});
		if (fRequest == null) {
			scheduleNext();
		}
	}

	protected synchronized void scheduleNext() {
		if ((fRequest = fRequests.poll()) != null) {
			fExecutor.execute(fRequest);
		}
	}

	public synchronized void queueRequest(Runnable request) {
		fRequests.add(request);
		scheduleNext();
	}

}
