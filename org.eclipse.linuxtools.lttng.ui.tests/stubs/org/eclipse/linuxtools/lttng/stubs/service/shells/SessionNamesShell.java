package org.eclipse.linuxtools.lttng.stubs.service.shells;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.lttng.ui.views.control.service.CommandResult;
import org.eclipse.linuxtools.lttng.ui.views.control.service.ICommandResult;

public class SessionNamesShell extends TestCommandShell {
    @SuppressWarnings("nls")
    @Override
    public ICommandResult executeCommand(String command, IProgressMonitor monitor, boolean checkReturnValue) throws ExecutionException {
        if ("lttng list ".equals(command)) {
            String[] output = new String[5];
            output[0] = String.valueOf("Available tracing sessions:");
            output[1] = String.valueOf("  1) mysession1 (/home/user/lttng-traces/mysession1-20120123-083928) [inactive]");
            output[2] = String.valueOf("  2) mysession (/home/user/lttng-traces/mysession-20120123-083318) [inactive]");
            output[3] = String.valueOf("");
            output[4] = String.valueOf(" Use lttng list <session_name> for more details");
            // test constructor with null pointer parameter
            CommandResult result = new CommandResult(0, null);
            // test setOutput!!!
            result.setOutput(output);
            return result; 
        } else if ("lttng list mysession".equals(command)) {
            List<String> list = new ArrayList<String>();
            list.add("Tracing session mysession: [active]");
            list.add("    Trace path: /home/user/lttng-traces/mysession-20120129-084256");
            list.add("");
            list.add("=== Domain: Kernel ===");
            list.add("");
            list.add("Channels:");
            list.add("-------------");
            list.add("- channel0: [enabled]");
            list.add("");
            list.add("    Attributes:");
            list.add("      overwrite mode: 0");
            list.add("      subbufers size: 262144");
            list.add("      number of subbufers: 4");
            list.add("      switch timer interval: 0");
            list.add("      read timer interval: 200");
            list.add("      output: splice()");
            list.add("");
            list.add("    Events:");
            list.add("      block_rq_remap (loglevel: TRACE_EMERG (0)) (type: tracepoint) [enabled]");
            list.add("      block_bio_remap (loglevel: TRACE_EMERG (0)) (type: tracepoint) [disabled]");
            list.add("");
            list.add("- channel1: [disabled]");
            list.add("");
            list.add("    Attributes:");
            list.add("      overwrite mode: 1");
            list.add("      subbufers size: 524288");
            list.add("      number of subbufers: 4");
            list.add("      switch timer interval: 100");
            list.add("      read timer interval: 400");
            list.add("      output: splice()");
            list.add("");
            list.add("    Events:");
            list.add("      None");
            list.add("");
            list.add("=== Domain: UST global ===");
            list.add("");
            list.add("Channels:");
            list.add("-------------");
            list.add("- mychannel1: [disabled]");
            list.add("");
            list.add("    Attributes:");
            list.add("     overwrite mode: 1");
            list.add("     subbufers size: 8192");
            list.add("      number of subbufers: 8");
            list.add("      switch timer interval: 200");
            list.add("      read timer interval: 100");
            list.add("      output: mmap()");
            list.add("");
            list.add("    Events:");
            list.add("      None");
            list.add("");
            list.add("- channel0: [enabled]");
            list.add("");
            list.add("    Attributes:");
            list.add("      overwrite mode: 0");
            list.add("      subbufers size: 4096");
            list.add("      number of subbufers: 4");
            list.add("      switch timer interval: 0");
            list.add("      read timer interval: 200");
            list.add("      output: mmap()");
            list.add("");
            list.add("    Events:");
            list.add("      ust_tests_hello:tptest_sighandler (loglevel: TRACE_DEBUG_LINE (13)) (type: tracepoint) [disabled]");
            list.add("      * (type: tracepoint) [enabled]");
            list.add("");
            return new CommandResult(0, list.toArray(new String[list.size()]));
        } else if ("lttng list -u".equals(command)) {
            List<String> list = new ArrayList<String>();
            list.add("UST events:");
            list.add("-------------");
            list.add("");
            list.add("PID: 9379 - Name: /home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello");
            list.add("    ust_tests_hello:tptest_sighandler (loglevel: TRACE_DEBUG_MODULE (10)) (type: tracepoint)");
            list.add("    ust_tests_hello:tptest (loglevel: TRACE_INFO (6)) (type: tracepoint)");
            list.add("");
            list.add("PID: 4852 - Name: /home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello");
            list.add("    ust_tests_hello:tptest_sighandler (loglevel: TRACE_WARNING (4)) (type: tracepoint)");
            list.add("    ust_tests_hello:tptest (loglevel: TRACE_DEBUG_FUNCTION (12)) (type: tracepoint)");
            list.add("");
            return new CommandResult(0, list.toArray(new String[list.size()]));
        } else if ("lttng list -k".equals(command)) {
            List<String> list = new ArrayList<String>();
            list.add("Kernel events:");
            list.add("-------------");
            list.add("      sched_kthread_stop (loglevel: TRACE_EMERG (0)) (type: tracepoint)");
            list.add("      sched_kthread_stop_ret (loglevel: TRACE_EMERG (0)) (type: tracepoint)");
            list.add("      sched_wakeup_new (loglevel: TRACE_EMERG (0)) (type: tracepoint)");
            list.add("");
            return new CommandResult(0, list.toArray(new String[list.size()]));
        } else if ("lttng list mysession1".equals(command)) {
            List<String> list = new ArrayList<String>();
            list.add("Tracing session mysession1: [inactive]");
            list.add("    Trace path: /home/user/lttng-traces/mysession1-20120203-133225");
            list.add("");
            return new CommandResult(0, list.toArray(new String[list.size()]));
        }

        String[] output = new String[1];
        output[0] = String.valueOf("Command not found");
        return new CommandResult(1, output);
    }
}
