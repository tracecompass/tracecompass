/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.LttngStrings;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.StateValues;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * This is the state change input plugin for TMF's state system which handles
 * the LTTng 2.0 kernel traces in CTF format.
 *
 * It uses the reference handler defined in CTFKernelHandler.java.
 *
 * @author alexmont
 *
 */
public class LttngKernelStateProvider extends AbstractTmfStateProvider {

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 4;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     */
    public LttngKernelStateProvider(CtfTmfTrace trace) {
        super(trace, CtfTmfEvent.class, "LTTng Kernel"); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        /* We can only set up the locations once the state system is assigned */
        super.assignTargetStateSystem(ssb);
    }

    @Override
    public LttngKernelStateProvider getNewInstance() {
        return new LttngKernelStateProvider((CtfTmfTrace) this.getTrace());
    }

    @Override
    protected void eventHandle(ITmfEvent ev) {
        /*
         * AbstractStateChangeInput should have already checked for the correct
         * class type
         */
        final CtfTmfEvent event = (CtfTmfEvent) ev;

        final ITmfEventField content = event.getContent();
        final String eventName = event.getType().getName();
        final long ts = event.getTimestamp().getValue();

        try {
            /* Shortcut for the "current CPU" attribute node */
            final Integer currentCPUNode = ss.getQuarkRelativeAndAdd(getNodeCPUs(), String.valueOf(event.getCPU()));

            /*
             * Shortcut for the "current thread" attribute node. It requires
             * querying the current CPU's current thread.
             */
            int quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
            ITmfStateValue value = ss.queryOngoingState(quark);
            int thread = value.isNull() ? -1 : value.unboxInt();
            final Integer currentThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(), String.valueOf(thread));

            /*
             * Feed event to the history system if it's known to cause a state
             * transition.
             */
            switch (eventName) {

            case LttngStrings.EXIT_SYSCALL:
            /* Fields: int64 ret */
            {
                /* Clear the current system call on the process */
                quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
                value = TmfStateValue.nullValue();
                ss.modifyAttribute(ts, value, quark);

                /* Put the process' status back to user mode */
                quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
                value = StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE;
                ss.modifyAttribute(ts, value, quark);

                /* Put the CPU's status back to user mode */
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
                ss.modifyAttribute(ts, value, quark);
            }
                break;

            case LttngStrings.IRQ_HANDLER_ENTRY:
            /* Fields: int32 irq, string name */
            {
                Integer irqId = ((Long) content.getField(LttngStrings.IRQ).getValue()).intValue();

                /* Mark this IRQ as active in the resource tree.
                 * The state value = the CPU on which this IRQ is sitting */
                quark = ss.getQuarkRelativeAndAdd(getNodeIRQs(), irqId.toString());
                value = TmfStateValue.newValueInt(event.getCPU());
                ss.modifyAttribute(ts, value, quark);

                /* Change the status of the running process to interrupted */
                quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
                value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
                ss.modifyAttribute(ts, value, quark);

                /* Change the status of the CPU to interrupted */
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                value = StateValues.CPU_STATUS_IRQ_VALUE;
                ss.modifyAttribute(ts, value, quark);
            }
                break;

            case LttngStrings.IRQ_HANDLER_EXIT:
            /* Fields: int32 irq, int32 ret */
            {
                Integer irqId = ((Long) content.getField(LttngStrings.IRQ).getValue()).intValue();

                /* Put this IRQ back to inactive in the resource tree */
                quark = ss.getQuarkRelativeAndAdd(getNodeIRQs(), irqId.toString());
                value = TmfStateValue.nullValue();
                ss.modifyAttribute(ts, value, quark);

                /* Set the previous process back to running */
                setProcessToRunning(ts, currentThreadNode);

                /* Set the CPU status back to running or "idle" */
                cpuExitInterrupt(ts, currentCPUNode, currentThreadNode);
            }
                break;

            case LttngStrings.SOFTIRQ_ENTRY:
            /* Fields: int32 vec */
            {
                Integer softIrqId = ((Long) content.getField(LttngStrings.VEC).getValue()).intValue();

                /* Mark this SoftIRQ as active in the resource tree.
                 * The state value = the CPU on which this SoftIRQ is processed */
                quark = ss.getQuarkRelativeAndAdd(getNodeSoftIRQs(), softIrqId.toString());
                value = TmfStateValue.newValueInt(event.getCPU());
                ss.modifyAttribute(ts, value, quark);

                /* Change the status of the running process to interrupted */
                quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
                value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
                ss.modifyAttribute(ts, value, quark);

                /* Change the status of the CPU to interrupted */
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                value = StateValues.CPU_STATUS_SOFTIRQ_VALUE;
                ss.modifyAttribute(ts, value, quark);
            }
                break;

            case LttngStrings.SOFTIRQ_EXIT:
            /* Fields: int32 vec */
            {
                Integer softIrqId = ((Long) content.getField(LttngStrings.VEC).getValue()).intValue();

                /* Put this SoftIRQ back to inactive (= -1) in the resource tree */
                quark = ss.getQuarkRelativeAndAdd(getNodeSoftIRQs(), softIrqId.toString());
                value = TmfStateValue.nullValue();
                ss.modifyAttribute(ts, value, quark);

                /* Set the previous process back to running */
                setProcessToRunning(ts, currentThreadNode);

                /* Set the CPU status back to "busy" or "idle" */
                cpuExitInterrupt(ts, currentCPUNode, currentThreadNode);
            }
                break;

            case LttngStrings.SOFTIRQ_RAISE:
            /* Fields: int32 vec */
            {
                Integer softIrqId = ((Long) content.getField(LttngStrings.VEC).getValue()).intValue();

                /* Mark this SoftIRQ as *raised* in the resource tree.
                 * State value = -2 */
                quark = ss.getQuarkRelativeAndAdd(getNodeSoftIRQs(), softIrqId.toString());
                value = StateValues.SOFT_IRQ_RAISED_VALUE;
                ss.modifyAttribute(ts, value, quark);
            }
                break;

            case LttngStrings.SCHED_SWITCH:
            /*
             * Fields: string prev_comm, int32 prev_tid, int32 prev_prio, int64 prev_state,
             *         string next_comm, int32 next_tid, int32 next_prio
             */
            {
                Integer prevTid = ((Long) content.getField(LttngStrings.PREV_TID).getValue()).intValue();
                Long prevState = (Long) content.getField(LttngStrings.PREV_STATE).getValue();
                String nextProcessName = (String) content.getField(LttngStrings.NEXT_COMM).getValue();
                Integer nextTid = ((Long) content.getField(LttngStrings.NEXT_TID).getValue()).intValue();

                Integer formerThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(), prevTid.toString());
                Integer newCurrentThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(), nextTid.toString());

                /* Set the status of the process that got scheduled out. */
                quark = ss.getQuarkRelativeAndAdd(formerThreadNode, Attributes.STATUS);
                if (prevState != 0) {
                    value = StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE;
                } else {
                    value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                }
                ss.modifyAttribute(ts, value, quark);

                /* Set the status of the new scheduled process */
                setProcessToRunning(ts, newCurrentThreadNode);

                /* Set the exec name of the new process */
                quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.EXEC_NAME);
                value = TmfStateValue.newValueString(nextProcessName);
                ss.modifyAttribute(ts, value, quark);

                /* Make sure the PPID and system_call sub-attributes exist */
                ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
                ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.PPID);

                /* Set the current scheduled process on the relevant CPU */
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
                value = TmfStateValue.newValueInt(nextTid);
                ss.modifyAttribute(ts, value, quark);

                /* Set the status of the CPU itself */
                if (nextTid > 0) {
                    /* Check if the entering process is in kernel or user mode */
                    quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
                    if (ss.queryOngoingState(quark).isNull()) {
                        value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
                    } else {
                        value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
                    }
                } else {
                    value = StateValues.CPU_STATUS_IDLE_VALUE;
                }
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                ss.modifyAttribute(ts, value, quark);
            }
                break;

            case LttngStrings.SCHED_PROCESS_FORK:
            /* Fields: string parent_comm, int32 parent_tid,
             *         string child_comm, int32 child_tid */
            {
                // String parentProcessName = (String) event.getFieldValue("parent_comm");
                String childProcessName = (String) content.getField(LttngStrings.CHILD_COMM).getValue();
                // assert ( parentProcessName.equals(childProcessName) );

                Integer parentTid = ((Long) content.getField(LttngStrings.PARENT_TID).getValue()).intValue();
                Integer childTid = ((Long) content.getField(LttngStrings.CHILD_TID).getValue()).intValue();

                Integer parentTidNode = ss.getQuarkRelativeAndAdd(getNodeThreads(), parentTid.toString());
                Integer childTidNode = ss.getQuarkRelativeAndAdd(getNodeThreads(), childTid.toString());

                /* Assign the PPID to the new process */
                quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.PPID);
                value = TmfStateValue.newValueInt(parentTid);
                ss.modifyAttribute(ts, value, quark);

                /* Set the new process' exec_name */
                quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.EXEC_NAME);
                value = TmfStateValue.newValueString(childProcessName);
                ss.modifyAttribute(ts, value, quark);

                /* Set the new process' status */
                quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.STATUS);
                value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                ss.modifyAttribute(ts, value, quark);

                /* Set the process' syscall name, to be the same as the parent's */
                quark = ss.getQuarkRelativeAndAdd(parentTidNode, Attributes.SYSTEM_CALL);
                value = ss.queryOngoingState(quark);
                if (value.isNull()) {
                    /*
                     * Maybe we were missing info about the parent? At least we
                     * will set the child right. Let's suppose "sys_clone".
                     */
                    value = TmfStateValue.newValueString(LttngStrings.SYS_CLONE);
                }
                quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.SYSTEM_CALL);
                ss.modifyAttribute(ts, value, quark);
            }
                break;

            case LttngStrings.SCHED_PROCESS_EXIT:
            /* Fields: string comm, int32 tid, int32 prio */
                break;

            case LttngStrings.SCHED_PROCESS_FREE:
            /* Fields: string comm, int32 tid, int32 prio */
            /*
             * A sched_process_free will always happen after the sched_switch
             * that will remove the process from the cpu for the last time. So
             * this is when we should delete everything wrt to the process.
             */
            {
                Integer tid = ((Long) content.getField(LttngStrings.TID).getValue()).intValue();
                /*
                 * Remove the process and all its sub-attributes from the
                 * current state
                 */
                quark = ss.getQuarkRelativeAndAdd(getNodeThreads(), tid.toString());
                ss.removeAttribute(ts, quark);
            }
                break;

            case LttngStrings.STATEDUMP_PROCESS_STATE:
            /* Fields:
             * int32 type, int32 mode, int32 pid, int32 submode, int32 vpid,
             * int32 ppid, int32 tid, string name, int32 status, int32 vtid */
            {
                int tid = ((Long) content.getField(LttngStrings.TID).getValue()).intValue();
                int pid = ((Long) content.getField(LttngStrings.PID).getValue()).intValue();
                int ppid = ((Long) content.getField(LttngStrings.PPID).getValue()).intValue();
                int status = ((Long) content.getField(LttngStrings.STATUS).getValue()).intValue();
                String name = (String) content.getField(LttngStrings.NAME).getValue();
                /*
                 * "mode" could be interesting too, but it doesn't seem to be
                 * populated with anything relevant for now.
                 */

                int curThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(), String.valueOf(tid));

                /* Set the process' name */
                quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.EXEC_NAME);
                if (ss.queryOngoingState(quark).isNull()) {
                    /* If the value didn't exist previously, set it */
                    value = TmfStateValue.newValueString(name);
                    ss.modifyAttribute(ts, value, quark);
                }

                /* Set the process' PPID */
                quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.PPID);
                if (ss.queryOngoingState(quark).isNull()) {
                    if (pid == tid) {
                        /* We have a process. Use the 'PPID' field. */
                        value = TmfStateValue.newValueInt(ppid);
                    } else {
                        /* We have a thread, use the 'PID' field for the parent. */
                        value = TmfStateValue.newValueInt(pid);
                    }
                    ss.modifyAttribute(ts, value, quark);
                }

                /* Set the process' status */
                quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.STATUS);
                if (ss.queryOngoingState(quark).isNull()) {
                     /* "2" here means "WAIT_FOR_CPU", and "5" "WAIT_BLOCKED" in the LTTng kernel. */
                    if (status == 2) {
                        value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                    } else if (status == 5) {
                        value = StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE;
                    } else {
                        value = StateValues.PROCESS_STATUS_UNKNOWN_VALUE;
                    }
                    ss.modifyAttribute(ts, value, quark);
                }
            }
                break;

            case LttngStrings.SCHED_WAKEUP:
            case LttngStrings.SCHED_WAKEUP_NEW:
            /* Fields (same fields for both types):
             * string comm, int32 pid, int32 prio, int32 success,
             * int32 target_cpu */
            {
                final int tid = ((Long) content.getField(LttngStrings.TID).getValue()).intValue();
                final int threadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(), String.valueOf(tid));

                /*
                 * The process indicated in the event's payload is now ready to
                 * run. Assign it to the "wait for cpu" state, but only if it
                 * was not already running.
                 */
                quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.STATUS);
                int status = ss.queryOngoingState(quark).unboxInt();

                if (status != StateValues.PROCESS_STATUS_RUN_SYSCALL &&
                    status != StateValues.PROCESS_STATUS_RUN_USERMODE) {
                    value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                    ss.modifyAttribute(ts, value, quark);
                }
            }
                break;

            default:
            /* Other event types not covered by the main switch */
            {
                if (eventName.startsWith(LttngStrings.SYSCALL_PREFIX)
                        || eventName.startsWith(LttngStrings.COMPAT_SYSCALL_PREFIX)) {
                    /*
                     * This is a replacement for the old sys_enter event. Now
                     * syscall names are listed into the event type
                     */

                    /* Assign the new system call to the process */
                    quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
                    value = TmfStateValue.newValueString(eventName);
                    ss.modifyAttribute(ts, value, quark);

                    /* Put the process in system call mode */
                    quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
                    value = StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE;
                    ss.modifyAttribute(ts, value, quark);

                    /* Put the CPU in system call (kernel) mode */
                    quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                    value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
                    ss.modifyAttribute(ts, value, quark);
                }
            }
                break;
            } // End of big switch

        } catch (AttributeNotFoundException ae) {
            /*
             * This would indicate a problem with the logic of the manager here,
             * so it shouldn't happen.
             */
            ae.printStackTrace();

        } catch (TimeRangeException tre) {
            /*
             * This would happen if the events in the trace aren't ordered
             * chronologically, which should never be the case ...
             */
            System.err.println("TimeRangeExcpetion caught in the state system's event manager."); //$NON-NLS-1$
            System.err.println("Are the events in the trace correctly ordered?"); //$NON-NLS-1$
            tre.printStackTrace();

        } catch (StateValueTypeException sve) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            sve.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Convenience methods for commonly-used attribute tree locations
    // ------------------------------------------------------------------------

    private int getNodeCPUs() {
        return ss.getQuarkAbsoluteAndAdd(Attributes.CPUS);
    }

    private int getNodeThreads() {
        return ss.getQuarkAbsoluteAndAdd(Attributes.THREADS);
    }

    private int getNodeIRQs() {
        return ss.getQuarkAbsoluteAndAdd(Attributes.RESOURCES, Attributes.IRQS);
    }

    private int getNodeSoftIRQs() {
        return ss.getQuarkAbsoluteAndAdd(Attributes.RESOURCES, Attributes.SOFT_IRQS);
    }

    // ------------------------------------------------------------------------
    // Advanced state-setting methods
    // ------------------------------------------------------------------------

    /**
     * When we want to set a process back to a "running" state, first check
     * its current System_call attribute. If there is a system call active, we
     * put the process back in the syscall state. If not, we put it back in
     * user mode state.
     */
    private void setProcessToRunning(long ts, int currentThreadNode)
            throws AttributeNotFoundException, TimeRangeException,
            StateValueTypeException {
        int quark;
        ITmfStateValue value;

        quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
        if (ss.queryOngoingState(quark).isNull()) {
            /* We were in user mode before the interruption */
            value = StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE;
        } else {
            /* We were previously in kernel mode */
            value = StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE;
        }
        quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
        ss.modifyAttribute(ts, value, quark);
    }

    /**
     * Similar logic as above, but to set the CPU's status when it's coming out
     * of an interruption.
     */
    private void cpuExitInterrupt(long ts, int currentCpuNode, int currentThreadNode)
            throws StateValueTypeException, AttributeNotFoundException,
            TimeRangeException {
        int quark;
        ITmfStateValue value;

        quark = ss.getQuarkRelativeAndAdd(currentCpuNode, Attributes.CURRENT_THREAD);
        if (ss.queryOngoingState(quark).unboxInt() > 0) {
            /* There was a process on the CPU */
            quark = ss.getQuarkRelative(currentThreadNode, Attributes.SYSTEM_CALL);
            if (ss.queryOngoingState(quark).isNull()) {
                /* That process was in user mode */
                value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
            } else {
                /* That process was in a system call */
                value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
            }
        } else {
            /* There was no real process scheduled, CPU was idle */
            value = StateValues.CPU_STATUS_IDLE_VALUE;
        }
        quark = ss.getQuarkRelativeAndAdd(currentCpuNode, Attributes.STATUS);
        ss.modifyAttribute(ts, value, quark);
    }
}
