/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.core.latency.analyzer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.eclipse.linuxtools.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.lttng.core.util.EventsPair;

/**
 * <b><u>EventMatcher</u></b>
 * <p>
 * Event matching class. Saves events in a list and returns the previously saved event if the currently processed one is
 * its response, so that the latency can be computed by subtracting their respective timestamps.
 * 
 * @author Philippe Sawicki
 */
public class EventMatcher {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    /**
     * Class instance (Singleton pattern).
     */
    private static EventMatcher fInstance = null;

    /**
     * Stack abstraction, used to save the events in a list.
     */
    private StackWrapper fStack;

    /**
     * Match table, associates a request class to a response class.
     */
    private HashMap<String, String> fMatch;
    /**
     * Inverse match table, associates a response class to a request class.
     */
    private HashMap<String, String> fInverseMatch;

    /**
     * The number of events processed.
     */
    private int fProcessedEvents;
    /**
     * The number of events matched.
     */
    private int fMatchedEvents;
	
	/**
	 * Event types identification Strings.
	 */
	@SuppressWarnings("nls")
    public static String
		ADD_TO_PAGE_CACHE         = "add_to_page_cache",
		BIO_BACKMERGE             = "bio_backmerge",
		BIO_FRONTMERGE            = "bio_frontmerge",
		BIO_QUEUE                 = "bio_queue",
		BUFFER_WAIT_END           = "buffer_wait_end",
		BUFFER_WAIT_START         = "buffer_wait_start",
		CALL                      = "call",
		CLOSE                     = "close",
		CORE_MARKER_FORMAT        = "core_marker_format",
		CORE_MARKER_ID            = "core_marker_id",
		DEV_RECEIVE               = "dev_receive",
		DEV_XMIT                  = "dev_xmit",
		END_COMMIT                = "end_commit",
		EXEC                      = "exec",
		FILE_DESCRIPTOR           = "file_descriptor",
		GETRQ                     = "getrq",
		GETRQ_BIO                 = "getrq_bio",
		IDT_TABLE                 = "idt_table",
		INTERRUPT                 = "interrupt",
		IOCTL                     = "ioctl",
		IRQ_ENTRY                 = "irq_entry",
		IRQ_EXIT                  = "irq_exit",
		LIST_MODULE               = "list_module",
		LLSEEK                    = "llseek",
		LSEEK                     = "lseek",
		NAPI_COMPLETE             = "napi_complete",
		NAPI_POLL                 = "napi_poll",
		NAPI_SCHEDULE             = "napi_schedule",
		NETWORK_IPV4_INTERFACE    = "network_ipv4_interface",
		NETWORK_IP_INTERFACE      = "network_ip_interface",
		OPEN                      = "open",
		PAGE_FAULT_ENTRY          = "page_fault_entry",
		PAGE_FAULT_EXIT           = "page_fault_exit",
		PAGE_FAULT_GET_USER_ENTRY = "page_fault_get_user_entry",
		PAGE_FAULT_GET_USER_EXIT  = "page_fault_get_user_exit",
		PAGE_FREE                 = "page_free",
		PLUG                      = "plug",
		POLLFD                    = "pollfd",
		PREAD64                   = "pread64",
		PRINTF                    = "printf",
		PRINTK                    = "printk",
		PROCESS_EXIT              = "process_exit",
		PROCESS_FORK              = "process_fork",
		PROCESS_FREE              = "process_free",
		PROCESS_STATE             = "process_state",
		PROCESS_WAIT              = "process_wait",
		READ                      = "read",
		REMAP                     = "remap",
		REMOVE_FROM_PAGE_CACHE    = "remove_from_page_cache",
		RQ_COMPLETE_FS            = "rq_complete_fs",
		RQ_COMPLETE_PC            = "rq_complete_pc",
		RQ_INSERT_FS              = "rq_insert_fs",
		RQ_INSERT_PC              = "rq_insert_pc",
		RQ_ISSUE_FS               = "rq_issue_fs",
		RQ_ISSUE_PC               = "rq_issue_pc",
		RQ_REQUEUE_PC             = "rq_requeue_pc",
		SCHED_MIGRATE_TASK        = "sched_migrate_task",
		SCHED_SCHEDULE            = "sched_schedule",
		SCHED_TRY_WAKEUP          = "sched_try_wakeup",
		SCHED_WAKEUP_NEW_TASK     = "sched_wakeup_new_task",
		SELECT                    = "select",
		SEM_CREATE                = "sem_create",
		SEND_SIGNAL               = "send_signal",
		SHM_CREATE                = "shm_create",
		SLEEPRQ_BIO               = "sleeprq_bio",
		SOCKET_ACCEPT             = "socket_accept",
		SOCKET_BIND               = "socket_bind",
		SOCKET_CALL               = "socket_call",
		SOCKET_CONNECT            = "socket_connect",
		SOCKET_CREATE             = "socket_create",
		SOCKET_GETPEERNAME        = "socket_getpeername",
		SOCKET_GETSOCKNAME        = "socket_getsockname",
		SOCKET_GETSOCKOPT         = "socket_getsockopt",
		SOCKET_LISTEN             = "socket_listen",
		SOCKET_SETSOCKOPT         = "socket_setsockopt",
		SOCKET_SHUTDOWN           = "socket_shutdown",
		SOCKET_SOCKETPAIR         = "socket_socketpair",
		SOFTIRQ_ENTRY             = "softirq_entry",
		SOFTIRQ_EXIT              = "softirq_exit",
		SOFTIRQ_RAISE             = "softirq_raise",
		SOFTIRQ_VEC               = "softirq_vec",
		START_COMMIT              = "start_commit",
		STATEDUMP_END             = "statedump_end",
		SYS_CALL_TABLE            = "sys_call_table",
	    SYSCALL_ENTRY             = "syscall_entry",
	    SYSCALL_EXIT              = "syscall_exit",
	    TASKLET_LOW_ENTRY         = "tasklet_low_entry",
	    TASKLET_LOW_EXIT          = "tasklet_low_exit",
	    TCPV4_RCV                 = "tcpv4_rcv",
	    TIMER_ITIMER_EXPIRED      = "timer_itimer_expired",
	    TIMER_ITIMER_SET          = "timer_itimer_set",
	    TIMER_SET                 = "timer_set",
	    TIMER_TIMEOUT             = "timer_timeout",
	    TIMER_UPDATE_TIME         = "timer_update_time",
	    UDPV4_RCV                 = "udpv4_rcv",
	    UNPLUG_IO                 = "unplug_io",
	    UNPLUG_TIMER              = "unplug_timer",
	    VM_MAP                    = "vm_map",
	    VPRINTK                   = "vprintk",
	    WAIT_ON_PAGE_END          = "wait_on_page_end",
	    WAIT_ON_PAGE_START        = "wait_on_page_start",
	    WRITE                     = "write",
	    WRITEV                    = "writev";

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
	
    /**
     * Private constructor to defeat instantiation (Singleton pattern).
     */
    private EventMatcher() {
        fStack = new StackWrapper();
        fMatch = new HashMap<String, String>();
        fInverseMatch = new HashMap<String, String>();

        fProcessedEvents = 0;
        fMatchedEvents = 0;

        createMatchTable();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns an instance to the EventMatcher class (Singleton pattern).
     * @return An instance to the EventMatcher class (Singleton pattern).
     */
    public static EventMatcher getInstance() {
        if (fInstance == null)
            fInstance = new EventMatcher();
        return fInstance;
    }

    /**
     * Returns the number of events processed.
     * @return The number of events processed.
     */
    public int getNBProcessedEvents() {
        return fProcessedEvents;
    }

    /**
     * Returns the number of events matched.
     * @return The number of events matched.
     */
    public int getNBMatchedEvents() {
        return fMatchedEvents;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /**
     * Releases the instance to the EventMatcher class.
     */
    public static void releaseInstance() {
        fInstance = null;
    }

    /**
     * Creates the event matching table, linking a response class to a request class.
     */
    private void createMatchTable() {
        // Build the default matches
        fMatch.put(PAGE_FAULT_GET_USER_EXIT, PAGE_FAULT_GET_USER_ENTRY);
        fMatch.put(TASKLET_LOW_EXIT, TASKLET_LOW_ENTRY);
        fMatch.put(PAGE_FAULT_EXIT, PAGE_FAULT_ENTRY);
        fMatch.put(SYSCALL_EXIT, SYSCALL_ENTRY);
        fMatch.put(IRQ_EXIT, IRQ_ENTRY);
        fMatch.put(WRITE, READ);
        fMatch.put(CLOSE, OPEN);
        fMatch.put(BUFFER_WAIT_END, BUFFER_WAIT_START);
        fMatch.put(END_COMMIT, START_COMMIT);
        fMatch.put(WAIT_ON_PAGE_END, WAIT_ON_PAGE_START);

        // Build the inverse matches based on the matches
        Set<Entry<String, String>> pairs = fMatch.entrySet();
        Iterator<Entry<String, String>> it = pairs.iterator();
        while (it.hasNext()) {
            Entry<String, String> pair = it.next();
            fInverseMatch.put(pair.getValue(), pair.getKey());
        }
    }

    /**
     * Processes an event received: if it is identified as a response, try to get its request to remove it from the
     * list. If no request was saved, dismiss the current response. If it is a request, save it to the list of requests
     * waiting for a response.
     * @param event
     *            The event to identify, and maybe process if it is a response.
     * @return The request event associated with the current event (a response), or null if nothing was found (no
     *         request associated with this response, or the event to identify was a request that was added to the
     *         list).
     */
    public LttngEvent process(LttngEvent event) {
        fProcessedEvents++;

        String markerName = event.getMarkerName();
        if (fMatch.containsKey(markerName)) {
            String startEventType = fMatch.get(markerName);
            Stack<LttngEvent> events = fStack.getStackOf(startEventType);
            
            if (events != null) {
                for (int i = events.size() - 1; i >= 0; i--) {
                    LttngEvent request = events.get(i);

                    if (request.getCpuId() == event.getCpuId() && event.getTimestamp().getValue() > request.getTimestamp().getValue()) {
                        fStack.removeEvent(startEventType, request);
                        fMatchedEvents++;
                        return request;
                    }
                }
            }
            return null;
        } else {
            // Add only if there can later be a match for this request
            if (fMatch.containsValue(event.getMarkerName())) {
                fStack.put(event.clone());
            }
            return null;
        }
    }

    /**
     * Clears the stack content.
     */
    public void clearStack() {
        fStack.clear();

        // Reset the processed and matched events counter
        fProcessedEvents = 0;
        fMatchedEvents = 0;
    }

    /**
     * Resets all.
     */
    public void resetMatches() {
        fMatch.clear();
        fInverseMatch.clear();

        fStack.clear();

        // Reset the processed and matched events counter
        fProcessedEvents = 0;
        fMatchedEvents = 0;
    }

    /**
     * Returns the list of start events.
     * @return The list of start events.
     */
    public Collection<String> getStartEvents() {
        return fMatch.values();
    }

    /**
     * Returns the list of end events.
     * @return The list of end events.
     */
    public Set<String> getEndEvents() {
        return fMatch.keySet();
    }

    /**
     * Returns the alphabetically-sorted list of start/end events pairs.
     * @return The alphabetically-sorted list of start/end events pairs.
     */
    public EventsPair getEvents() {
        Vector<String> start = new Vector<String>(getStartEvents());
        Vector<String> end = new Vector<String>(fMatch.size());

        Collections.sort(start);
        for (int i = 0; i < start.size(); i++) {
            end.add(fInverseMatch.get(start.get(i)));
        }
        return new EventsPair(start, end);
    }

    /**
     * Adds a match to the list of events pairs.
     * @param startType
     *            The start event type.
     * @param endType
     *            The end event type.
     */
    public void addMatch(String startType, String endType) {
        fMatch.put(endType, startType);
        fInverseMatch.put(startType, endType);
    }

    /**
     * Removes a matched pair based on the their type.
     * 
     * <b>Note :</b> For now, only the pair's end type is used, since a type can only be either one start or one end.
     * This function takes both types to account for the future, if a pairing process ever becomes more complex.
     * 
     * @param startType
     *            The type of the pair's start type.
     * @param endType
     *            The type of the pair's end type.
     */
    public void removeMatch(String startType, String endType) {
        fMatch.remove(endType);
        fInverseMatch.remove(startType);
    }

    /**
     * Returns the list of all event possible types.
     * @return The list of all event possible types.
     */
    public Vector<String> getTypeList() {
        // Reserve some space for the 103 default event types.
        Vector<String> eventsList = new Vector<String>(103);

        eventsList.add(ADD_TO_PAGE_CACHE);
        eventsList.add(BIO_BACKMERGE);
        eventsList.add(BIO_FRONTMERGE);
        eventsList.add(BIO_QUEUE);
        eventsList.add(BUFFER_WAIT_END);
        eventsList.add(BUFFER_WAIT_START);
        eventsList.add(CALL);
        eventsList.add(CLOSE);
        eventsList.add(CORE_MARKER_FORMAT);
        eventsList.add(CORE_MARKER_ID);
        eventsList.add(DEV_RECEIVE);
        eventsList.add(DEV_XMIT);
        eventsList.add(END_COMMIT);
        eventsList.add(EXEC);
        eventsList.add(FILE_DESCRIPTOR);
        eventsList.add(GETRQ);
        eventsList.add(GETRQ_BIO);
        eventsList.add(IDT_TABLE);
        eventsList.add(INTERRUPT);
        eventsList.add(IOCTL);
        eventsList.add(IRQ_ENTRY);
        eventsList.add(IRQ_EXIT);
        eventsList.add(LIST_MODULE);
        eventsList.add(LLSEEK);
        eventsList.add(LSEEK);
        eventsList.add(NAPI_COMPLETE);
        eventsList.add(NAPI_POLL);
        eventsList.add(NAPI_SCHEDULE);
        eventsList.add(NETWORK_IPV4_INTERFACE);
        eventsList.add(NETWORK_IP_INTERFACE);
        eventsList.add(OPEN);
        eventsList.add(PAGE_FAULT_ENTRY);
        eventsList.add(PAGE_FAULT_EXIT);
        eventsList.add(PAGE_FAULT_GET_USER_ENTRY);
        eventsList.add(PAGE_FAULT_GET_USER_EXIT);
        eventsList.add(PAGE_FREE);
        eventsList.add(PLUG);
        eventsList.add(POLLFD);
        eventsList.add(PREAD64);
        eventsList.add(PRINTF);
        eventsList.add(PRINTK);
        eventsList.add(PROCESS_EXIT);
        eventsList.add(PROCESS_FORK);
        eventsList.add(PROCESS_FREE);
        eventsList.add(PROCESS_STATE);
        eventsList.add(PROCESS_WAIT);
        eventsList.add(READ);
        eventsList.add(REMAP);
        eventsList.add(REMOVE_FROM_PAGE_CACHE);
        eventsList.add(RQ_COMPLETE_FS);
        eventsList.add(RQ_COMPLETE_PC);
        eventsList.add(RQ_INSERT_FS);
        eventsList.add(RQ_INSERT_PC);
        eventsList.add(RQ_ISSUE_FS);
        eventsList.add(RQ_ISSUE_PC);
        eventsList.add(RQ_REQUEUE_PC);
        eventsList.add(SCHED_MIGRATE_TASK);
        eventsList.add(SCHED_SCHEDULE);
        eventsList.add(SCHED_TRY_WAKEUP);
        eventsList.add(SCHED_WAKEUP_NEW_TASK);
        eventsList.add(SELECT);
        eventsList.add(SEM_CREATE);
        eventsList.add(SEND_SIGNAL);
        eventsList.add(SHM_CREATE);
        eventsList.add(SLEEPRQ_BIO);
        eventsList.add(SOCKET_ACCEPT);
        eventsList.add(SOCKET_BIND);
        eventsList.add(SOCKET_CALL);
        eventsList.add(SOCKET_CONNECT);
        eventsList.add(SOCKET_CREATE);
        eventsList.add(SOCKET_GETPEERNAME);
        eventsList.add(SOCKET_GETSOCKNAME);
        eventsList.add(SOCKET_GETSOCKOPT);
        eventsList.add(SOCKET_LISTEN);
        eventsList.add(SOCKET_SETSOCKOPT);
        eventsList.add(SOCKET_SHUTDOWN);
        eventsList.add(SOCKET_SOCKETPAIR);
        eventsList.add(SOFTIRQ_ENTRY);
        eventsList.add(SOFTIRQ_EXIT);
        eventsList.add(SOFTIRQ_RAISE);
        eventsList.add(SOFTIRQ_VEC);
        eventsList.add(START_COMMIT);
        eventsList.add(STATEDUMP_END);
        eventsList.add(SYS_CALL_TABLE);
        eventsList.add(SYSCALL_ENTRY);
        eventsList.add(SYSCALL_EXIT);
        eventsList.add(TASKLET_LOW_ENTRY);
        eventsList.add(TASKLET_LOW_EXIT);
        eventsList.add(TCPV4_RCV);
        eventsList.add(TIMER_ITIMER_EXPIRED);
        eventsList.add(TIMER_ITIMER_SET);
        eventsList.add(TIMER_SET);
        eventsList.add(TIMER_TIMEOUT);
        eventsList.add(TIMER_UPDATE_TIME);
        eventsList.add(UDPV4_RCV);
        eventsList.add(UNPLUG_IO);
        eventsList.add(UNPLUG_TIMER);
        eventsList.add(VM_MAP);
        eventsList.add(VPRINTK);
        eventsList.add(WAIT_ON_PAGE_END);
        eventsList.add(WAIT_ON_PAGE_START);
        eventsList.add(WRITE);
        eventsList.add(WRITEV);

        return eventsList;
    }

    /**
     * Prints the stack content to the console.
     */
    public void print() {
        fStack.printContent();
    }
}