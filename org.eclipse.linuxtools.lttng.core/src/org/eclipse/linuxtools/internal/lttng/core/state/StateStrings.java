/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *   Daniel U. Thibault (daniel.thibault@drdc-rddc.gc.ca) - 2011-06-21 Fixes
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.state;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Singleton
 * Establishes relationships of state-related strings. Since the strings and
 * relations are fixed, the elements are final i.e. just for reading.
 * 
 * @author alvaro
 */

@SuppressWarnings("nls")
public class StateStrings {

	// ========================================================================
	// Table data
	// =======================================================================
    public static final String LTTV_STATE_UNBRANDED = "";
	private static StateStrings instance = null;
	private final HashMap<String, Events> eventStrMap = new HashMap<String, Events>();
	private final HashMap<String, Events> stateTransEventMap = new HashMap<String, Events>();
	private final String[] syscall_names = new String[256];
	private final String[] trap_names = new String[256];
	private final String[] irq_names = new String[256];
	private final String[] soft_irq_names = new String[32];

	private StateStrings() {
		// Build a Hash map from string name to actual event structure
		for (Events event : Events.values()) {
			eventStrMap.put(event.inName, event);
			if (event.isStateTransition()) {
				stateTransEventMap.put(event.inName, event);
			}
		}

		// Build system call names
		for (int i = 0; i < 256; i++) {
			syscall_names[i] = "syscall " + i;
		}

		// Build trap names
		for (int i = 0; i < 256; i++) {
			trap_names[i] = "trap " + i;
		}

		// Build irq names
		for (int i = 0; i < 256; i++) {
			irq_names[i] = "irq " + i;
		}

		// Build softirq names
		for (int i = 0; i < 32; i++) {
			soft_irq_names[i] = "softirq " + i;
		}
	}

	public static StateStrings getInstance() {
		// Create singleton instance if not already done
		if (instance == null) {
			instance = new StateStrings();
		}
		return instance;
	}

	public enum Channels {
		LTT_CHANNEL_FD_STATE("fd_state"),           /* file descriptor state events */
		LTT_CHANNEL_GLOBAL_STATE("global_state"),   /* system state dump events */
		LTT_CHANNEL_IRQ_STATE("irq_state"),         /* interrupt request (IRQ) events */
		LTT_CHANNEL_MODULE_STATE("module_state"),   /* modules state events (list of modules) */
		LTT_CHANNEL_NETIF_STATE("netif_state"),     /* network interface events */
		LTT_CHANNEL_SOFTIRQ_STATE("softirq_state"), /* soft IRQs state events (soft IRQ vector table) */
		LTT_CHANNEL_SWAP_STATE("swap_state"),       /* swap state events */
		LTT_CHANNEL_SYSCALL_STATE("syscall_state"), /* system calls state events (system call table) */
		LTT_CHANNEL_TASK_STATE("task_state"),       /* process state events */
		LTT_CHANNEL_VM_STATE("vm_state"),           /* virtual memory events (virtual memory table) */
		LTT_CHANNEL_KPROBE_STATE("kprobe_state"),   /* kprobe events (kprobe table) */
		LTT_CHANNEL_FS("fs"),                       /* file system events */
		LTT_CHANNEL_KERNEL("kernel"),               /* kernel events */
		LTT_CHANNEL_MM("mm"),                       /* memory management events */
		LTT_CHANNEL_USERSPACE("userspace"),         /* user space tracing events */
		LTT_CHANNEL_BLOCK("block");                 /* block devices events */
                /* remaining channels: */
/*              LTT_CHANNEL_DEFAULT("default"),             /* null (i.e. no default) */
/*              LTT_CHANNEL_INPUT("input"),                 /* LTT control command inputs */
/*              LTT_CHANNEL_IPC("ipc"),	                    /* Inter Process Communication (IPC) events */
/*              LTT_CHANNEL_JBD2("jbd2"),                   /* Journaling Block Device (JBD) events (JBD2 fork for ext4 file system) */
/*              LTT_CHANNEL_METADATA("metadata"),           /* trace meta data */
/*              LTT_CHANNEL_NET("net"),                     /* networking events */
/*              LTT_CHANNEL_PM("pm"),                       /* power management events */
/*              LTT_CHANNEL_RCU("rcu"),                     /* Read-Copy-Update (RCU) events */

		private final String inName;

		private Channels(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

		}

	public enum Events {
		LTT_EVENT_SYSCALL_ENTRY("syscall_entry"), /* */
		LTT_EVENT_SYSCALL_EXIT("syscall_exit"), /* */
		LTT_EVENT_TRAP_ENTRY("trap_entry"), /* */
		LTT_EVENT_TRAP_EXIT("trap_exit"), /* */
		LTT_EVENT_PAGE_FAULT_ENTRY("page_fault_entry"), /* */
		LTT_EVENT_PAGE_FAULT_EXIT("page_fault_exit"), /* */
		LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY("page_fault_nosem_entry"), /* */
		LTT_EVENT_PAGE_FAULT_NOSEM_EXIT("page_fault_nosem_exit"), /* */
		LTT_EVENT_IRQ_ENTRY("irq_entry"), /* */
		LTT_EVENT_IRQ_EXIT("irq_exit"), /* */
		LTT_EVENT_SOFT_IRQ_RAISE("softirq_raise"), /* */
		LTT_EVENT_SOFT_IRQ_ENTRY("softirq_entry"), /* */
		LTT_EVENT_SOFT_IRQ_EXIT("softirq_exit"), /* */
		LTT_EVENT_SCHED_SCHEDULE("sched_schedule"), /* */
		LTT_EVENT_PROCESS_FORK("process_fork"), /* */
		LTT_EVENT_KTHREAD_CREATE("kthread_create"), /* */
		LTT_EVENT_PROCESS_EXIT("process_exit"), /* */
		LTT_EVENT_PROCESS_FREE("process_free"), /* */
		LTT_EVENT_EXEC("exec"), /* */
		LTT_EVENT_PROCESS_STATE("process_state"), /* */
		LTT_EVENT_STATEDUMP_END("statedump_end"), /* */
		LTT_EVENT_FUNCTION_ENTRY("function_entry"), /* */
		LTT_EVENT_FUNCTION_EXIT("function_exit"), /* */
		LTT_EVENT_THREAD_BRAND("thread_brand"), /* */
		LTT_EVENT_REQUEST_ISSUE("_blk_request_issue"), /* */
		LTT_EVENT_REQUEST_COMPLETE("_blk_request_complete"), /* */
		LTT_EVENT_LIST_INTERRUPT("interrupt"), /* */
		LTT_EVENT_SYS_CALL_TABLE("sys_call_table"), /* */
		LTT_EVENT_SOFTIRQ_VEC("softirq_vec"), /* */
		LTT_EVENT_KPROBE_TABLE("kprobe_table"), /* */
		LTT_EVENT_KPROBE("kprobe"); /* */

		private final String inName;
		private final HashSet<Fields> children = new HashSet<Fields>();
		private Channels parent = null;
		// Default value for the "expected to cause a state transition?" flag
		// Although most events do cause a state transition, the default is kept
		// false to make this important property explicit in the static initializer
		private boolean stateTransition = false;

		static {
			associate();
		}

		private Events(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

		public Channels getParent() {
			return this.parent;
		}

		public void setParent(Channels parent) {
			this.parent = parent;
		}

		public HashSet<Fields> getChildren() {
			return children;
		}

		public boolean isStateTransition() {
			return stateTransition;
		}

		private static void associate() {
			// SYSCALL_ENTRY can receive ip (?), syscall_id
			LTT_EVENT_SYSCALL_ENTRY.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_SYSCALL_ENTRY.stateTransition = true;
			LTT_EVENT_SYSCALL_ENTRY.getChildren().add(Fields.LTT_FIELD_SYSCALL_ID);

			//SYSCALL_EXIT
			LTT_EVENT_SYSCALL_EXIT.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_SYSCALL_EXIT.stateTransition = true;

			// TRAP_ENTRY
			LTT_EVENT_TRAP_ENTRY.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_TRAP_ENTRY.getChildren().add(Fields.LTT_FIELD_TRAP_ID);
			LTT_EVENT_TRAP_ENTRY.stateTransition = true;

			// TRAP_EXIT
			LTT_EVENT_TRAP_EXIT.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_TRAP_EXIT.stateTransition = true;

			// PAGE_FAULT_ENTRY
			LTT_EVENT_PAGE_FAULT_ENTRY.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_PAGE_FAULT_ENTRY.getChildren().add(Fields.LTT_FIELD_TRAP_ID);
			LTT_EVENT_PAGE_FAULT_ENTRY.stateTransition = true;

			// PAGE_FAULT_EXIT
			LTT_EVENT_PAGE_FAULT_EXIT.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_PAGE_FAULT_EXIT.stateTransition = true;

			// PAGE_FAULT_NOSEM_ENTRY
			LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY.getChildren().add(Fields.LTT_FIELD_TRAP_ID);
			LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY.stateTransition = true;

			// PAGE_FAULT_NOSEM_EXIT
			LTT_EVENT_PAGE_FAULT_NOSEM_EXIT.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_PAGE_FAULT_NOSEM_EXIT.getChildren().add(Fields.LTT_FIELD_TRAP_ID);
			LTT_EVENT_PAGE_FAULT_NOSEM_EXIT.stateTransition = true;

			// IRQ_ENTRY also receives fields kernel_mode, ip and handler (??)
			LTT_EVENT_IRQ_ENTRY.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_IRQ_ENTRY.getChildren().add(Fields.LTT_FIELD_IRQ_ID);
			LTT_EVENT_IRQ_ENTRY.stateTransition = true;

			// IRQ_EXIT
			LTT_EVENT_IRQ_EXIT.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_IRQ_EXIT.stateTransition = true;

			// SOFT_IRQ_RAISE
			LTT_EVENT_SOFT_IRQ_RAISE.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_SOFT_IRQ_RAISE.getChildren().add(Fields.LTT_FIELD_SOFT_IRQ_ID);
			LTT_EVENT_SOFT_IRQ_RAISE.stateTransition = true;

			// SOFT_IRQ_ENTRY
			LTT_EVENT_SOFT_IRQ_ENTRY.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_SOFT_IRQ_ENTRY.getChildren().add(Fields.LTT_FIELD_SOFT_IRQ_ID);
			LTT_EVENT_SOFT_IRQ_ENTRY.stateTransition = true;

			// SOFT_IRQ_EXIT
			LTT_EVENT_SOFT_IRQ_EXIT.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_SOFT_IRQ_EXIT.stateTransition = true;

			// SCHED_SCHEDULE
			LTT_EVENT_SCHED_SCHEDULE.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_SCHED_SCHEDULE.getChildren().add(Fields.LTT_FIELD_PREV_PID);
			LTT_EVENT_SCHED_SCHEDULE.getChildren().add(Fields.LTT_FIELD_NEXT_PID);
			LTT_EVENT_SCHED_SCHEDULE.getChildren().add(Fields.LTT_FIELD_PREV_STATE);
			LTT_EVENT_SCHED_SCHEDULE.stateTransition = true;

			// PROCESS_FORK
			LTT_EVENT_PROCESS_FORK.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_PROCESS_FORK.getChildren().add(Fields.LTT_FIELD_PARENT_PID);
			LTT_EVENT_PROCESS_FORK.getChildren().add(Fields.LTT_FIELD_CHILD_PID);
			LTT_EVENT_PROCESS_FORK.getChildren().add(Fields.LTT_FIELD_CHILD_TGID);
			LTT_EVENT_PROCESS_FORK.stateTransition = true;

			// KTHREAD_CREATE
			LTT_EVENT_KTHREAD_CREATE.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_KTHREAD_CREATE.getChildren().add(Fields.LTT_FIELD_PID);
			LTT_EVENT_KTHREAD_CREATE.stateTransition = true;

			// PROCESS_EXIT
			LTT_EVENT_PROCESS_EXIT.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_PROCESS_EXIT.getChildren().add(Fields.LTT_FIELD_PID);
			LTT_EVENT_PROCESS_EXIT.stateTransition = true;

			// PROCESS_FREE
			LTT_EVENT_PROCESS_FREE.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_PROCESS_FREE.getChildren().add(Fields.LTT_FIELD_PID);
			LTT_EVENT_PROCESS_FREE.stateTransition = true;

			// EVENT_EXEC
			LTT_EVENT_EXEC.setParent(Channels.LTT_CHANNEL_FS);
			LTT_EVENT_EXEC.getChildren().add(Fields.LTT_FIELD_FILENAME);
			LTT_EVENT_EXEC.stateTransition = true;

			// EVENT_PROCESS_STATE
			LTT_EVENT_PROCESS_STATE.setParent(Channels.LTT_CHANNEL_TASK_STATE);
			LTT_EVENT_PROCESS_STATE.getChildren().add(Fields.LTT_FIELD_PID);
			LTT_EVENT_PROCESS_STATE.getChildren().add(Fields.LTT_FIELD_PARENT_PID);
			LTT_EVENT_PROCESS_STATE.getChildren().add(Fields.LTT_FIELD_NAME);
			LTT_EVENT_PROCESS_STATE.getChildren().add(Fields.LTT_FIELD_TYPE);
			LTT_EVENT_PROCESS_STATE.getChildren().add(Fields.LTT_FIELD_MODE);
			LTT_EVENT_PROCESS_STATE.getChildren().add(Fields.LTT_FIELD_SUBMODE);
			LTT_EVENT_PROCESS_STATE.getChildren().add(Fields.LTT_FIELD_STATUS);
			LTT_EVENT_PROCESS_STATE.getChildren().add(Fields.LTT_FIELD_TGID);
			LTT_EVENT_PROCESS_STATE.stateTransition = true;

			// STATEDUMP_END
			LTT_EVENT_STATEDUMP_END.setParent(Channels.LTT_CHANNEL_GLOBAL_STATE);
			LTT_EVENT_STATEDUMP_END.stateTransition = true;

			// FUNCTION_ENTRY
			LTT_EVENT_FUNCTION_ENTRY.setParent(Channels.LTT_CHANNEL_USERSPACE);
			LTT_EVENT_FUNCTION_ENTRY.getChildren().add(Fields.LTT_FIELD_THIS_FN);
			LTT_EVENT_FUNCTION_ENTRY.getChildren().add(Fields.LTT_FIELD_CALL_SITE);
 			LTT_EVENT_FUNCTION_ENTRY.stateTransition = true;

			// FUNCTION_EXIT
			LTT_EVENT_FUNCTION_EXIT.setParent(Channels.LTT_CHANNEL_USERSPACE);
			LTT_EVENT_FUNCTION_EXIT.getChildren().add(Fields.LTT_FIELD_THIS_FN);
			LTT_EVENT_FUNCTION_EXIT.getChildren().add(Fields.LTT_FIELD_CALL_SITE);
 			LTT_EVENT_FUNCTION_EXIT.stateTransition = true;

			// THREAD_BRAND
			LTT_EVENT_THREAD_BRAND.setParent(Channels.LTT_CHANNEL_USERSPACE);
			LTT_EVENT_THREAD_BRAND.getChildren().add(Fields.LTT_FIELD_NAME);
 			LTT_EVENT_THREAD_BRAND.stateTransition = true;

			// REQUEST_ISSUE
			LTT_EVENT_REQUEST_ISSUE.setParent(Channels.LTT_CHANNEL_BLOCK);
			LTT_EVENT_REQUEST_ISSUE.getChildren().add(Fields.LTT_FIELD_MAJOR);
			LTT_EVENT_REQUEST_ISSUE.getChildren().add(Fields.LTT_FIELD_MINOR);
			LTT_EVENT_REQUEST_ISSUE.getChildren().add(Fields.LTT_FIELD_OPERATION);			
			LTT_EVENT_REQUEST_ISSUE.stateTransition = true;

			// REQUEST_COMPLETE
			LTT_EVENT_REQUEST_COMPLETE.setParent(Channels.LTT_CHANNEL_BLOCK);
			LTT_EVENT_REQUEST_COMPLETE.getChildren().add(Fields.LTT_FIELD_MAJOR);
			LTT_EVENT_REQUEST_COMPLETE.getChildren().add(Fields.LTT_FIELD_MINOR);
			LTT_EVENT_REQUEST_COMPLETE.getChildren().add(Fields.LTT_FIELD_OPERATION);
			LTT_EVENT_REQUEST_COMPLETE.stateTransition = true;

			// LIST_INTERRUPT
			LTT_EVENT_LIST_INTERRUPT.setParent(Channels.LTT_CHANNEL_IRQ_STATE);
			LTT_EVENT_LIST_INTERRUPT.getChildren().add(Fields.LTT_FIELD_ACTION);
			LTT_EVENT_LIST_INTERRUPT.getChildren().add(Fields.LTT_FIELD_IRQ_ID);
 			LTT_EVENT_LIST_INTERRUPT.stateTransition = true;

			// SYS_CALL_TABLE
			LTT_EVENT_SYS_CALL_TABLE.setParent(Channels.LTT_CHANNEL_SYSCALL_STATE);
			LTT_EVENT_SYS_CALL_TABLE.getChildren().add(Fields.LTT_FIELD_ID);
			LTT_EVENT_SYS_CALL_TABLE.getChildren().add(Fields.LTT_FIELD_ADDRESS);
			LTT_EVENT_SYS_CALL_TABLE.getChildren().add(Fields.LTT_FIELD_SYMBOL);
			LTT_EVENT_SYS_CALL_TABLE.stateTransition = true;

			// SOFTIRQ_VEC
			LTT_EVENT_SOFTIRQ_VEC.setParent(Channels.LTT_CHANNEL_SOFTIRQ_STATE);
			LTT_EVENT_SOFTIRQ_VEC.getChildren().add(Fields.LTT_FIELD_ID);
			LTT_EVENT_SOFTIRQ_VEC.getChildren().add(Fields.LTT_FIELD_ADDRESS);
			LTT_EVENT_SOFTIRQ_VEC.getChildren().add(Fields.LTT_FIELD_SYMBOL);
			LTT_EVENT_SOFTIRQ_VEC.stateTransition = true;

			// KPROBE_TABLE
			LTT_EVENT_KPROBE_TABLE.setParent(Channels.LTT_CHANNEL_KPROBE_STATE);
			LTT_EVENT_KPROBE_TABLE.getChildren().add(Fields.LTT_FIELD_IP);
			LTT_EVENT_KPROBE_TABLE.getChildren().add(Fields.LTT_FIELD_SYMBOL);
 			LTT_EVENT_KPROBE_TABLE.stateTransition = true;

			// KPROBE
			LTT_EVENT_KPROBE.setParent(Channels.LTT_CHANNEL_KERNEL);
			LTT_EVENT_KPROBE.getChildren().add(Fields.LTT_FIELD_IP);
//			LTT_EVENT_KPROBE.getChildren().add(Fields.LTT_FIELD_SYMBOL);
 			LTT_EVENT_KPROBE.stateTransition = true;
		}

	}

	public enum Fields {
		LTT_FIELD_SYSCALL_ID("syscall_id"), /* */
		LTT_FIELD_TRAP_ID("trap_id"), /* */
		LTT_FIELD_IRQ_ID("irq_id"), /* */
		LTT_FIELD_SOFT_IRQ_ID("softirq_id"), /* */
		LTT_FIELD_PREV_PID("prev_pid"), /* */
		LTT_FIELD_NEXT_PID("next_pid"), /* */
		LTT_FIELD_PREV_STATE("prev_state"), /* */
		LTT_FIELD_PARENT_PID("parent_pid"), /* */
		LTT_FIELD_CHILD_PID("child_pid"), /* */
		LTT_FIELD_PID("pid"), /* */
		LTT_FIELD_TGID("tgid"), /* */
		LTT_FIELD_CHILD_TGID("child_tgid"), /* */
		LTT_FIELD_FILENAME("filename"), /* */
		LTT_FIELD_NAME("name"), /* */
		LTT_FIELD_TYPE("type"), /* */
		LTT_FIELD_MODE("mode"), /* */
		LTT_FIELD_SUBMODE("submode"), /* */
		LTT_FIELD_STATUS("status"), /* */
		LTT_FIELD_THIS_FN("this_fn"), /* */
		LTT_FIELD_CALL_SITE("call_site"), /* */
		LTT_FIELD_MAJOR("major"), /* */
		LTT_FIELD_MINOR("minor"), /* */
		LTT_FIELD_OPERATION("direction"), /* */
		LTT_FIELD_ACTION("action"), /* */
		LTT_FIELD_ID("id"), /* */
		LTT_FIELD_ADDRESS("address"), /* */
		LTT_FIELD_SYMBOL("symbol"), /* */
		LTT_FIELD_IP("ip"); /* */

		private final String inName;

		private Fields(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}

	public enum CpuMode {
		LTTV_CPU_UNKNOWN("unknown"), /* */
		LTTV_CPU_IDLE("idle"), /* */
		LTTV_CPU_BUSY("busy"), /* */
		LTTV_CPU_IRQ("irq"), /* */
		LTTV_CPU_SOFT_IRQ("softirq"), /* */
		LTTV_CPU_TRAP("trap"); /* */

		private final String inName;

		private CpuMode(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}

	public enum IRQMode {
		LTTV_IRQ_UNKNOWN("unknown"), /* */
		LTTV_IRQ_IDLE("idle"), /* */
		LTTV_IRQ_BUSY("busy"); /* */

		private final String inName;

		private IRQMode(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}

	public enum SoftIRQMode {
		LTTV_SOFT_IRQ_UNKNOWN("unknown"), /* */
		LTTV_SOFT_IRQ_IDLE("idle"), /* */
		LTTV_SOFT_IRQ_PENDING("pending"), /* */
		LTTV_SOFT_IRQ_BUSY("busy"); /* */

		private final String inName;

		private SoftIRQMode(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}
	
	public enum BdevMode {
		LTTV_BDEV_UNKNOWN("unknown"), /* */
		LTTV_BDEV_IDLE("idle"), /* */
		LTTV_BDEV_BUSY_READING("busy_reading"), /* */
		LTTV_BDEV_BUSY_WRITING("busy_writing"); /* */

		private final String inName;

		private BdevMode(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}
	
	public enum TrapMode {
		LTTV_TRAP_UNKNOWN("unknown"), /* */
		LTTV_TRAP_IDLE("idle"), /* */
		LTTV_TRAP_BUSY("busy"); /* */

		private final String inName;

		private TrapMode(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}

	public enum ProcessType {
		LTTV_STATE_USER_THREAD("USER_THREAD"), /* */
		LTTV_STATE_KERNEL_THREAD("KERNEL_THREAD"); /* */

		private final String inName;

		private ProcessType(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}

	public enum ProcessStatus {
		LTTV_STATE_UNNAMED("UNNAMED"),     /* */
                LTTV_STATE_WAIT_FORK("WAIT_FORK"), /* */
		LTTV_STATE_WAIT_CPU("WAIT_CPU"), /* */
		LTTV_STATE_EXIT("EXIT"), /* */
		LTTV_STATE_ZOMBIE("ZOMBIE"), /* */
		LTTV_STATE_WAIT("WAIT"), /* */
		LTTV_STATE_RUN("RUN"), /* */
		LTTV_STATE_DEAD("DEAD"); /* */

		private final String inName;

		private ProcessStatus(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}

	public enum ExecutionSubMode {
		LTTV_STATE_SUBMODE_UNKNOWN("UNKNOWN"), /* */
		LTTV_STATE_SUBMODE_NONE("NONE"); /* */

		private final String inName;

		private ExecutionSubMode(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}

	public enum ExecutionMode {
		LTTV_STATE_MODE_UNKNOWN("MODE_UNKNOWN"), /* */
		LTTV_STATE_USER_MODE("USER_MODE"), /* */
		LTTV_STATE_SYSCALL("SYSCALL"), /* */
		LTTV_STATE_TRAP("TRAP"), /* */
		LTTV_STATE_IRQ("IRQ"), /* */
		LTTV_STATE_SOFT_IRQ("SOFTIRQ"); /* */

		private final String inName;

		private ExecutionMode(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}

	public enum GenStates {
		LTTV_STATE_TRACEFILES("tracefiles"), /* */
		LTTV_STATE_PROCESSES("processes"), /* */
		LTTV_STATE_PROCESS("process"), /* */
		LTTV_STATE_RUNNING_PROCESS("running_process"), /* */
		LTTV_STATE_EVENT("event"), /* */
		LTTV_STATE_SAVED_STATES("saved states"), /* */
		LTTV_STATE_SAVED_STATES_TIME("saved states time"), /* */
		LTTV_STATE_TIME("time"), /* */
		LTTV_STATE_HOOKS("saved state hooks"), /* */
		LTTV_STATE_NAME_TABLES("name tables"), /* */
		LTTV_STATE_TRACE_STATE_USE_COUNT("trace_state_use_count"), /* */
		LTTV_STATE_RESOURCE_CPUS("cpu count"), /* */
		LTTV_STATE_RESOURCE_IRQS("irq resource states"), /* */
		LTTV_STATE_RESOURCE_SOFT_IRQS("soft irq resource states"), /* */
		LTTV_STATE_RESOURCE_TRAPS("trap resource states"), /* */
		LTTV_STATE_RESOURCE_BLKDEVS("blkdevs resource states"); /* */

		private final String inName;

		private GenStates(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}

	}

	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * Return the map from event name strings to Events instances
	 * 
	 * @return the eventstrmap
	 */
	public HashMap<String, Events> getEventStrmap() {
		return eventStrMap;
	}

	/**
	 * Return the map from state transition event name strings to Events instances
	 * State transition events are the subset of events that have the attribute stateTransition == True.
	 * 
	 * @return the stateTransEventMap
	 */
	public HashMap<String, Events> getStateTransEventMap() {
		return stateTransEventMap;
	}

	/**
	 * @return the mapping from int (0..255) to system call names
	 */
	public String[] getSyscallNames() {
		return Arrays.copyOf(syscall_names, syscall_names.length);
	}

	/**
	 * @return the mapping from int (0..255) to trap names
	 */
	public String[] getTrapNames() {
		return Arrays.copyOf(trap_names, trap_names.length);
	}

	/**
	 * @return the mapping from int (0..255) to IRQ names
	 */
	public String[] getIrqNames() {
		return Arrays.copyOf(irq_names, irq_names.length);
	}

	/**
	 * @return the mapping from int (0..31) to soft IRQ names
	 */
	public String[] getSoftIrqNames() {
		return Arrays.copyOf(soft_irq_names, soft_irq_names.length);
	}

}
