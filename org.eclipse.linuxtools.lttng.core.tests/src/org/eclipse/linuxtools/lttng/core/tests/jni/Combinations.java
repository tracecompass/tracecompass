/**
 * 
 */
package org.eclipse.linuxtools.lttng.core.tests.jni;

import junit.framework.TestCase;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;


/**
 * @author alvaro
 * 
 */
@SuppressWarnings("nls")
public class Combinations extends TestCase {

	private final static boolean printLttDebug = false;
	
    private final static String tracepath="traceset/trace-618339events-1293lost-1cpu";
	private final static String eventName = "syscall_state";

	private final static Integer expect_syscall_entry = 195596;
	private final static Integer expect_syscall_exit = 195598;
	private final static Integer expect_core_marker_format = 177;
	private final static Integer expect_core_marker_id = 177;

	// private static final String LTT_EVENT_SYSCALL_ENTRY = "syscall_entry";
	// private static final String LTT_EVENT_SYSCALL_EXIT = "syscall_exit";
	// private static final String LTT_EVENT_TRAP_ENTRY = "trap_entry";
	// private static final String LTT_EVENT_TRAP_EXIT = "trap_exit";
	// private static final String LTT_EVENT_PAGE_FAULT_ENTRY =
	// "page_fault_entry";
	// private static final String LTT_EVENT_PAGE_FAULT_EXIT =
	// "page_fault_exit";
	// private static final String LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY =
	// "page_fault_nosem_entry";
	// private static final String LTT_EVENT_PAGE_FAULT_NOSEM_EXIT =
	// "page_fault_nosem_exit";
	// private static final String LTT_EVENT_IRQ_ENTRY = "irq_entry";
	// private static final String LTT_EVENT_IRQ_EXIT = "irq_exit";
	// private static final String LTT_EVENT_SOFT_IRQ_RAISE = "softirq_raise";
	// private static final String LTT_EVENT_SOFT_IRQ_ENTRY = "softirq_entry";
	// private static final String LTT_EVENT_SOFT_IRQ_EXIT = "softirq_exit";
	// private static final String LTT_EVENT_SCHED_SCHEDULE = "sched_schedule";
	// private static final String LTT_EVENT_PROCESS_FORK = "process_fork";
	// private static final String LTT_EVENT_KTHREAD_CREATE = "kthread_create";
	// private static final String LTT_EVENT_PROCESS_EXIT = "process_exit";
	// private static final String LTT_EVENT_PROCESS_FREE = "process_free";
	// private static final String LTT_EVENT_EXEC = "exec";
	// private static final String LTT_EVENT_PROCESS_STATE = "process_state";
	// private static final String LTT_EVENT_STATEDUMP_END = "statedump_end";
	// private static final String LTT_EVENT_FUNCTION_ENTRY = "function_entry";
	// private static final String LTT_EVENT_FUNCTION_EXIT = "function_exit";
	// private static final String LTT_EVENT_THREAD_BRAND = "thread_brand";
	// private static final String LTT_EVENT_REQUEST_ISSUE =
	// "_blk_request_issue";
	// private static final String LTT_EVENT_REQUEST_COMPLETE =
	// "_blk_request_complete";
	// private static final String LTT_EVENT_LIST_INTERRUPT = "interrupt";
	// private static final String LTT_EVENT_SYS_CALL_TABLE = "sys_call_table";
	// private static final String LTT_EVENT_SOFTIRQ_VEC = "softirq_vec";
	// private static final String LTT_EVENT_KPROBE_TABLE = "kprobe_table";
	// private static final String LTT_EVENT_KPROBE = "kprobe";

	// enum EventString {
	// syscall_entry, syscall_exit, trap_entry, trap_exit, page_fault_entry,
	// page_fault_exit, page_fault_nosem_entry, page_fault_nosem_exit,
	// irq_entry, irq_exit, softirq_raise, softirq_entry, softirq_exit,
	// sched_schedule, process_fork, kthread_create, process_exit, process_free,
	// exec, process_state, statedump_end, function_entry, function_exit,
	// thread_brand, _blk_request_issue, blk_request_complete, interrupt,
	// sys_call_table, softirq_vec, kprobe_table, kprobe
	// };

	enum EvStateTrans {
		syscall_entry, syscall_exit, trap_entry, trap_exit, page_fault_entry, page_fault_exit, page_fault_nosem_entry, page_fault_nosem_exit, irq_entry, irq_exit, softirq_raise, softirq_entry, softirq_exit, sched_schedule, process_fork, kthread_create, process_exit, process_free, exec, thread_brand
	};

	private static Set<String> stateSet;
	static {
		stateSet = new HashSet<String>();
		EvStateTrans[] stateArr = EvStateTrans.values();
		for (EvStateTrans event : stateArr) {
			stateSet.add(event.name());
		}
	}

	private JniEvent prepareEventToTest() {

		JniEvent tmpEvent = null;
		// This trace should be valid
		try {
			tmpEvent = JniTraceFactory.getJniTrace(tracepath, null, printLttDebug).requestEventByName(eventName);
		} catch (JniException e) {
		}

		return tmpEvent;
	}
	
	public void testGetSpecEventFields() {
		JniEvent event = prepareEventToTest();
		JniMarker dmarker = event.requestEventMarker();
		List<JniMarkerField> markers = dmarker.getMarkerFieldsArrayList();

		assertNotNull(markers);
		System.out.println("Markers: " + markers);

	}

	public void testEventsLoop() {
		JniTrace trace = null;
		JniEvent event = null;
		try {
			trace = JniTraceFactory.getJniTrace(tracepath, null, printLttDebug);
		} catch (JniException e) {
            fail("Could not open trace");
		}

		HashMap<String, Integer> eventCount = new HashMap<String, Integer>();
		while (true) {
			event = trace.readNextEvent();
			if (event == null) {
				break;
			}
			JniMarker dmarker = event.requestEventMarker();
			assertNotNull(dmarker);

			String name = dmarker.getName();

			if (eventCount.containsKey(name)) {
				Integer cnt = eventCount.get(name);
				eventCount.put(name, cnt + 1);
			} else {
				eventCount.put(name, 1);
				// Only print state transition states and it's format
				if (stateSet.contains(name)) {
					System.out.println("\nMarker name: " + name + "\nFields:");
					
					Object[] tmpMarkerFields = dmarker.getMarkerFieldsArrayList().toArray();
					String[] fields = new String[tmpMarkerFields.length];
					
					for (int pos = 0; pos < tmpMarkerFields.length; pos++) {
						fields[pos] = ((JniMarkerField) tmpMarkerFields[pos]).getField() + ":" + ((JniMarkerField) tmpMarkerFields[pos]).getFormat();
					}
					
					for (String field : fields) {
						System.out.println(field + " ");
					}
				}
			}
		}

		for (String state : eventCount.keySet()) {
			System.out.println(state + " : " + eventCount.get(state));
		}

		assertEquals("syscall_entry mismatch", expect_syscall_entry, eventCount
				.get(EvStateTrans.syscall_entry.name()));
		assertEquals("syscall_exit mismatch", expect_syscall_exit, eventCount
				.get(EvStateTrans.syscall_exit.name()));
		assertEquals("core_market_format mismatch", expect_core_marker_format,
				eventCount.get("core_marker_format"));
		assertEquals("core_market_id mismatch", expect_core_marker_id,
				eventCount.get("core_marker_id"));
	}

}
