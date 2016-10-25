/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.process;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.common.core.Activator;

/**
 * Common utility methods for launching external processes and retrieving their
 * output.
 *
 * @author Alexandre Montplaisir
 * @since 2.2
 */
public final class ProcessUtils {

    private ProcessUtils() {}

    /**
     * Simple output-getting command. Cannot be cancelled, and will return null
     * if the external process exits with a non-zero return code.
     *
     * @param command
     *            The command (executable + arguments) to launch
     * @return The process's standard output upon completion
     */
    public static @Nullable List<String> getOutputFromCommand(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);

            Process p = builder.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));) {
                List<String> output = new LinkedList<>();

                /*
                 * We must consume the output before calling Process.waitFor(),
                 * or else the buffers might fill and block the external program
                 * if there is a lot of output.
                 */
                String line = br.readLine();
                while (line != null) {
                    output.add(line);
                    line = br.readLine();
                }

                int ret = p.waitFor();
                return (ret == 0 ? output : null);
            }
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    /**
     * Interface defining what do to with a process's output. For use with
     * {@link #getOutputFromCommandCancellable}.
     */
    @FunctionalInterface
    public static interface OutputReaderFunction {

        /**
         * Handle the output of the process. This can include reporting progress
         * to the monitor, and pre-processing the returned output.
         *
         * @param reader
         *            A buffered reader to the process's standard output.
         *            Managed internally, so you do not need to
         *            {@link BufferedReader#close()} it.
         * @param monitor
         *            The progress monitor. Implementation should check
         *            periodically if it is cancelled to end processing early.
         *            The monitor's start and end will be managed, but progress
         *            can be reported via the {@link IProgressMonitor#worked}
         *            method. The total is 1000 work units.
         * @return The process's output
         * @throws IOException
         *             If there was a read error. Letting throw all exception
         *             from the {@link BufferedReader} is recommended.
         */
        List<String> readOutput(BufferedReader reader, IProgressMonitor monitor) throws IOException;
    }

    /**
     * Cancellable output-getting command. The processing, as well as the
     * external process itself, can be stopped by cancelling the passed progress
     * monitor.
     *
     * @param command
     *            The command (executable + arguments) to execute
     * @param monitor
     *            The progress monitor to check for cancellation and optionally
     *            progress
     * @param mainTaskName
     *            The main task name of the job
     * @param readerFunction
     *            What to do with the output. See {@link OutputReaderFunction}.
     * @return The process's standard output, upon normal completion
     * @throws CoreException
     *             If a problem happened with the execution of the external
     *             process. It can be reported to the user with the help of an
     *             ErrorDialog.
     */
    public static List<String> getOutputFromCommandCancellable(List<String> command,
            IProgressMonitor monitor,
            String mainTaskName,
            OutputReaderFunction readerFunction)
            throws CoreException {

        CancellableRunnable cancellerRunnable = null;
        Thread cancellerThread = null;

        try {
            monitor.beginTask(mainTaskName, 1000);

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(false);

            Process p = checkNotNull(builder.start());

            cancellerRunnable = new CancellableRunnable(p, monitor);
            cancellerThread = new Thread(cancellerRunnable);
            cancellerThread.start();

            try (BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(p.getInputStream()));) {

                List<String> lines = readerFunction.readOutput(stdoutReader, monitor);

                int ret = p.waitFor();

                if (monitor.isCanceled()) {
                    /* We were interrupted by the canceller thread. */
                    IStatus status = new Status(IStatus.CANCEL, Activator.instance().getPluginId(), null);
                    throw new CoreException(status);
                }

                if (ret != 0) {
                    /*
                     * Something went wrong running the external process. We
                     * will gather the stderr and report it to the user.
                     */
                    BufferedReader stderrReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    List<String> stderrOutput = stderrReader.lines().collect(Collectors.toList());

                    MultiStatus status = new MultiStatus(Activator.instance().getPluginId(),
                            IStatus.ERROR, Messages.ProcessUtils_ErrorDuringExecution, null);
                    for (String str : stderrOutput) {
                        status.add(new Status(IStatus.ERROR, Activator.instance().getPluginId(), str));
                    }
                    if (stderrOutput.isEmpty()) {
                        /*
                         * At least say "no output", so an error message actually
                         * shows up.
                         */
                        status.add(new Status(IStatus.ERROR, Activator.instance().getPluginId(), Messages.ProcessUtils_ErrorNoOutput));
                    }
                    throw new CoreException(status);
                }

                return lines;
            }

        } catch (IOException | InterruptedException e) {
            IStatus status = new Status(IStatus.ERROR, Activator.instance().getPluginId(), Messages.ProcessUtils_ExecutionInterrupted, e);
            throw new CoreException(status);

        } finally {
            if (cancellerRunnable != null) {
                cancellerRunnable.setFinished();
            }
            if (cancellerThread != null) {
                try {
                    cancellerThread.join();
                } catch (InterruptedException e) {
                }
            }

            monitor.done();
        }
    }

    /**
     * Internal wrapper class that allows forcibly stopping a {@link Process}
     * when its corresponding progress monitor is cancelled.
     */
    private static class CancellableRunnable implements Runnable {

        private final Process fProcess;
        private final IProgressMonitor fMonitor;

        private boolean fIsFinished = false;

        public CancellableRunnable(Process process, IProgressMonitor monitor) {
            fProcess = process;
            fMonitor = monitor;
        }

        public void setFinished() {
            fIsFinished = true;
        }

        @Override
        public void run() {
            try {
                while (!fIsFinished) {
                    Thread.sleep(500);
                    if (fMonitor.isCanceled()) {
                        fProcess.destroy();
                        return;
                    }
                }
            } catch (InterruptedException e) {
            }
        }

    }
}
