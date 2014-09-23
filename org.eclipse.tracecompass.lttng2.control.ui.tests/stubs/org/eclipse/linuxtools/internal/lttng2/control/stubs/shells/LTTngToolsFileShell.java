/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.stubs.shells;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.CommandResult;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.ICommandResult;

@SuppressWarnings("javadoc")
public class LTTngToolsFileShell extends TestCommandShell {

    // ------------------------------------------------------------------------
    // CONSTANTS
    // ------------------------------------------------------------------------
    private final static String SCENARIO_KEY = "<SCENARIO>";
    private final static String SCENARIO_END_KEY = "</SCENARIO>";
    private final static String INPUT_KEY = "<COMMAND_INPUT>";
    private final static String INPUT_END_KEY = "</COMMAND_INPUT>";
    private final static String RESULT_KEY = "<COMMAND_RESULT>";
    private final static String OUTPUT_KEY = "<COMMAND_OUTPUT>";
    private final static String OUTPUT_END_KEY = "</COMMAND_OUTPUT>";
    private final static String ERROR_OUTPUT_KEY = "<COMMAND_ERROR_OUTPUT>";
    private final static String ERROR_OUTPUT_END_KEY = "</COMMAND_ERROR_OUTPUT>";
    private final static String COMMENT_KEY = "#.*";

    private final static Pattern LTTNG_LIST_SESSION_PATTERN =  Pattern.compile("lttng\\s+list\\s+(.+)");
    private final static String LTTNG_LIST_PROVIDER_PATTERN = "lttng\\s+list\\s+(-u|-k)";

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private String fScenariofile;
    private String fScenario;

    private final Map<String, Map<String, ICommandResult>> fScenarioMap = new HashMap<>();
    private final Map<String, Integer> fSessionNameMap = new HashMap<>();

    /**
     * Parse a scenario file with the format:
     * <pre>
     * &lt;SCENARIO&gt;
     * ScenarioName
     *
     * &lt;COMMAND_INPUT&gt;
     * Command
     * &lt;/COMAND_INPUT&gt;
     *
     * &lt;COMMAND_RESULT&gt;
     * CommandResult
     * &lt;/COMMAND_RESULT&gt;
     *
     * &lt;COMMAND_OUTPUT&gt;
     * CommandOutput
     * &lt;COMMAND_ERROR_OUTPUT&gt;
     * CommandErrorOutput
     * &lt;/COMMAND_ERROR_OUTPUT&gt;
     * &lt;/COMMAND_OUTPUT&gt;
     *
     * &lt;/SCENARIO&gt;
     *
     * Where: ScenarioName - is the scenario name
     *        Command - the command line string
     *        CommandResult - the result integer of the command (0 for success, 1 for failure)
     *        ComandOutput - the command output string (multi-line possible)
     *        ComandErrorOutput - the command error output string (multi-line possible)
     *
     * Note: 1) There can be many scenarios per file
     *       2) There can be many (Command-CommandResult-CommandOutput) triples per scenario
     *       3) Lines starting with # will be ignored (comments)
     * <pre>
     * @param scenariofile - path to scenario file
     * @throws Exception
     */
    public synchronized void loadScenarioFile(String scenariofile) throws Exception {
        fScenariofile = scenariofile;

        // clean up map
        Collection<Map<String, ICommandResult>> values = fScenarioMap.values();
        for (Iterator<Map<String, ICommandResult>> iterator = values.iterator(); iterator.hasNext();) {
            Map<String, ICommandResult> map = iterator.next();
            map.clear();
        }
        fScenarioMap.clear();

        // load from file

        // Open the file
        try (FileInputStream fstream = new FileInputStream(fScenariofile);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));) {
            String strLine;

            // Read File Line by Line

            // Temporary map for generating instance numbers for lttng list
            // <session> commands.
            // The numbers are per scenario.
            Map<String, Integer> tmpSessionNameMap = new HashMap<>();
            while ((strLine = br.readLine()) != null) {

                // Ignore comments
                if (isComment(strLine)) {
                    continue;
                }

                if (SCENARIO_KEY.equals(strLine)) {
                    // scenario start

                    // Ignore comments
                    strLine = br.readLine();
                    while (isComment(strLine)) {
                        strLine = br.readLine();
                    }

                    String scenario = strLine;
                    Map<String, ICommandResult> commandMap = new HashMap<>();
                    fScenarioMap.put(scenario, commandMap);
                    List<String> output = null;
                    List<String> errorOutput = null;
                    String input = null;
                    boolean inOutput = false;
                    boolean inErrorOutput = false;
                    int result = 0;
                    tmpSessionNameMap.clear();
                    while ((strLine = br.readLine()) != null) {
                        // Ignore comments
                        if (isComment(strLine)) {
                            continue;
                        }

                        if (SCENARIO_END_KEY.equals(strLine)) {
                            // Scenario is finished
                            break;
                        }
                        if (INPUT_KEY.equals(strLine)) {
                            strLine = br.readLine();
                            // Ignore comments
                            while (isComment(strLine)) {
                                strLine = br.readLine();
                            }
                            // Read command
                            input = strLine;

                            // Handle instances of 'lttng list
                            // <session"-comamand
                            Matcher matcher = LTTNG_LIST_SESSION_PATTERN.matcher(strLine);
                            if (matcher.matches() && !input.matches(LTTNG_LIST_PROVIDER_PATTERN)) {
                                String sessionName = matcher.group(1).trim();
                                Integer i = tmpSessionNameMap.get(sessionName);
                                if (i != null) {
                                    i++;
                                } else {
                                    i = 0;
                                }
                                tmpSessionNameMap.put(sessionName, i);
                                input += String.valueOf(i);
                            }
                        } else if (INPUT_END_KEY.equals(strLine)) {
                            // Initialize output array
                            output = new ArrayList<>();
                            errorOutput = new ArrayList<>();
                        } else if (RESULT_KEY.equals(strLine)) {
                            strLine = br.readLine();
                            // Ignore comments
                            while (isComment(strLine)) {
                                strLine = br.readLine();
                            }
                            // Save result value
                            result = Integer.parseInt(strLine);
                        } else if (OUTPUT_END_KEY.equals(strLine)) {
                            // Save output/result in command map
                            if (output != null && errorOutput != null) {
                                commandMap.put(input, new CommandResult(result, output.toArray(new String[output.size()]), errorOutput.toArray(new String[errorOutput.size()])));
                            }
                            inOutput = false;
                        } else if (OUTPUT_KEY.equals(strLine)) {
                            // first line of output
                            inOutput = true;
                        } else if (ERROR_OUTPUT_KEY.equals(strLine)) {
                            // first line of output
                            inErrorOutput = true;
                        } else if (ERROR_OUTPUT_END_KEY.equals(strLine)) {
                            inErrorOutput = false;
                        } else if (inOutput) {
                            while (isComment(strLine)) {
                                strLine = br.readLine();
                            }
                            // lines of output/error output
                            if (errorOutput != null && inErrorOutput) {
                                errorOutput.add(strLine);
                            } else if (output != null) {
                                output.add(strLine);
                            }
                        }
                        // else {
                        // if (RESULT_END_KEY.equals(strLine)) {
                        // nothing to do
                        // }
                    }
                }
            }
        }
    }

    // Set the scenario to consider in executeCommand()
    public synchronized void setScenario(String scenario) {
        fScenario = scenario;
        fSessionNameMap.clear();
        if (!fScenarioMap.containsKey(fScenario)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
   public synchronized ICommandResult executeCommand(String command, IProgressMonitor monitor, boolean checkReturnValue) throws ExecutionException {
        Map<String, ICommandResult> commands = fScenarioMap.get(fScenario);
        String fullCommand = command;

        Matcher matcher = LTTNG_LIST_SESSION_PATTERN.matcher(command);
        if (matcher.matches() && !command.matches(LTTNG_LIST_PROVIDER_PATTERN)) {
            String sessionName = matcher.group(1).trim();
            Integer i = fSessionNameMap.get(sessionName);
            if (i != null) {
                i++;
            } else {
                i = 0;
            }
            fSessionNameMap.put(sessionName, i);
            fullCommand += String.valueOf(i);
        }

        if (commands.containsKey(fullCommand)) {
            return commands.get(fullCommand);
        }

        String[] output = new String[1];
        output[0] = String.valueOf("Command not found");
        CommandResult result = new CommandResult(0, null, null);
        // For verification of setters of class CommandResult
        result.setOutput(output);
        result.setErrorOutput(output);
        result.setResult(1);
        return result;
   }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private static boolean isComment(String line) {
        if (line == null) {
            throw new RuntimeException("line is null");
        }
        return line.matches(COMMENT_KEY);
    }
}
