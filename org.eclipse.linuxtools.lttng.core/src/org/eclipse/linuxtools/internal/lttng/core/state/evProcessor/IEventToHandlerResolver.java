package org.eclipse.linuxtools.internal.lttng.core.state.evProcessor;

public interface IEventToHandlerResolver {

	/**
	 * 
	 * @return The Event Handler for received event before the State data model
	 *         is updated.
	 */
	public abstract ILttngEventProcessor getBeforeProcessor(String eventType);

	/**
	 * 
	 * @return The Event Handler for received event after the State data model
	 *         is updated.
	 */
	public abstract ILttngEventProcessor getAfterProcessor(String eventType);

	/**
	 * 
	 * @return The Event Handler after the complete read request is completed,
	 *         needed e.g. to draw the last state
	 */
	public abstract ILttngEventProcessor getfinishProcessor();

	/**
	 * 
	 * @return The Event Handler for received event in charge to update the
	 *         state. Only one handler is expected so other factories must not
	 *         override this method.
	 */
	public abstract ILttngEventProcessor getStateUpdaterProcessor(String eventType);

}