/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.stubs.shells;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.remote.core.stubs.shells.TestCommandShell;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandInput;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandOutputListener;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandResult;

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

    private final static Pattern LTTNG_LIST_SESSION_PATTERN = Pattern.compile("lttng\\s+list\\s+(.+)");
    private final static String LTTNG_LIST_PROVIDER_PATTERN = "lttng\\s+list\\s+(-u|-k|-j|-l|-p).*";

    private final static Pattern LTTNG_LIST_SESSION_MI_PATTERN = Pattern.compile("lttng\\s+--mi xml\\s+list\\s+(.+)");
    private final static String LTTNG_LIST_PROVIDER_MI_PATTERN = "lttng\\s+--mi xml\\s+list\\s+(-u|-k|-j|-l|-p).*";

    private final static String LTTNG_USER_HOME_PATTERN = "\\$\\{userhome\\}";
    private final static String LTTNG_WORKSPACE_PATTERN = "\\$\\{workspace\\}";
    private final static String SESSION_NAME_PATTERN = "\\$\\{sessionname\\}";

    private final static String USER_HOME = System.getProperty("user.home");
    private final static String WORKSPACE_HOME;

    private final static Pattern LTTNG_SAVE_MI_PATTERN = Pattern.compile("lttng\\s+--mi xml\\s+save\\s+-f");

    private final static String PROFILE_PATH_STRING = USER_HOME + '/' + ".lttng" + '/' + "sessions";

    static {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        //get location of workspace (java.io.File)
        File workspaceDirectory = workspace.getRoot().getLocation().toFile();
        WORKSPACE_HOME = workspaceDirectory.toString();
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private String fScenariofile;
    private String fScenario;
    private String fProfileName = null;
    private File fProfileFile = null;
    private String fSessionName = null;

    private final Map<String, Map<String, ICommandResult>> fScenarioMap = new HashMap<>();
    private final Map<String, Integer> fSessionNameMap = new HashMap<>();

    /**
     * Parse a scenario file with the format:
     *
     * <pre>
     * &lt;SCENARIO&gt;
     * ScenarioName
     *
     * &lt;COMMAND_INPUT&gt;
     * Command
     * &lt;/COMMAND_INPUT&gt;
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
     *        CommandOutput - the command output string (multi-line possible)
     *        CommandErrorOutput - the command error output string (multi-line possible)
     *
     * Note: 1) There can be many scenarios per file
     *       2) There can be many (Command-CommandResult-CommandOutput) triples per scenario
     * 3) Lines starting with # will be ignored (comments)
     *
     * <pre>
     * @param scenariofile - path to scenario file
     */
    public synchronized void loadScenarioFile(String scenariofile) {
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

                            // Update userhome
                            input = input.replaceAll(LTTNG_USER_HOME_PATTERN, Matcher.quoteReplacement(USER_HOME));

                            // Update workspace
                            input = input.replaceAll(LTTNG_WORKSPACE_PATTERN, Matcher.quoteReplacement(WORKSPACE_HOME));

                            // Update session variable
                            if (fSessionName != null) {
                                input = input.replaceAll(SESSION_NAME_PATTERN, Matcher.quoteReplacement(fSessionName));
                            }

                            // Handle instances of 'lttng list
                            // <session"-command
                            Matcher matcher = LTTNG_LIST_SESSION_PATTERN.matcher(strLine);
                            Matcher miMatcher = LTTNG_LIST_SESSION_MI_PATTERN.matcher(strLine);

                            if (matcher.matches() && !input.matches(LTTNG_LIST_PROVIDER_PATTERN)) {
                                String sessionName = matcher.group(1).trim();
                                input += updateSessionMap(tmpSessionNameMap, input, sessionName);
                            } else if (miMatcher.matches() && !input.matches(LTTNG_LIST_PROVIDER_MI_PATTERN)) {
                                String sessionName = miMatcher.group(1).trim();
                                input += updateSessionMap(tmpSessionNameMap, input, sessionName);
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
                                commandMap.put(input, createCommandResult(result,
                                        output.toArray(new @NonNull String[output.size()]),
                                        errorOutput.toArray(new @NonNull String[errorOutput.size()])));
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

                            // Update userhome
                            strLine = strLine.replaceAll(LTTNG_USER_HOME_PATTERN, Matcher.quoteReplacement(USER_HOME));

                            // Update workspace
                            strLine = strLine.replaceAll(LTTNG_WORKSPACE_PATTERN, Matcher.quoteReplacement(WORKSPACE_HOME));

                            // Update session variable
                            if (fSessionName != null) {
                                strLine = strLine.replaceAll(SESSION_NAME_PATTERN, Matcher.quoteReplacement(fSessionName));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String updateSessionMap(Map<String, Integer> tmpSessionNameMap, String input, String sessionName) {
        Integer i = tmpSessionNameMap.get(sessionName);
        if (i != null) {
            i++;
        } else {
            i = 0;
        }
        tmpSessionNameMap.put(sessionName, i);
        return String.valueOf(i);
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
    public synchronized ICommandResult executeCommand(ICommandInput command, IProgressMonitor monitor) throws ExecutionException {
        return executeCommand(command, monitor, null);
    }

    @Override
    public synchronized ICommandResult executeCommand(ICommandInput command, IProgressMonitor monitor, ICommandOutputListener listener) throws ExecutionException {
        Map<String, ICommandResult> commands = checkNotNull(fScenarioMap.get(fScenario));
        String commandLine = command.toString();
        String fullCommand = commandLine;

        Matcher matcher = LTTNG_LIST_SESSION_PATTERN.matcher(commandLine);
        Matcher miMatcher = LTTNG_LIST_SESSION_MI_PATTERN.matcher(commandLine);
        if (matcher.matches() && !commandLine.matches(LTTNG_LIST_PROVIDER_PATTERN)) {
            String sessionName = matcher.group(1).trim();
            fullCommand += updateSessionMap(fSessionNameMap, fullCommand, sessionName);
        } else if (miMatcher.matches() && !commandLine.matches(LTTNG_LIST_PROVIDER_MI_PATTERN)) {
            String sessionName = miMatcher.group(1).trim();
            fullCommand += updateSessionMap(fSessionNameMap, fullCommand, sessionName);
        }

        if (commands.containsKey(fullCommand)) {
            Matcher saveMatcher = LTTNG_SAVE_MI_PATTERN.matcher(fullCommand);
            if (fProfileName != null && saveMatcher.matches()) {
                try {
                    createProfileFile();
                } catch (IOException e) {
                    throw new ExecutionException("Profile file can't be created", e);
                }
            }
            return checkNotNull(commands.get(fullCommand));
        }

        @NonNull String[] output = new @NonNull String[1];
        output[0] = String.valueOf("Command not found");
        ICommandResult result = createCommandResult(1, output, output);
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

    private void createProfileFile() throws IOException {
        if (fProfileName != null) {
            File path = new File(PROFILE_PATH_STRING);
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    throw new RuntimeException();
                }
            }
            File profileFile = new File(PROFILE_PATH_STRING + '/' + fProfileName + ".lttng");
            if (!profileFile.exists()) {
                try (PrintWriter writer = new PrintWriter(profileFile)) {
                    writer.println("This file is created by JUnit test using " + LTTngToolsFileShell.class.getCanonicalName());
                    writer.println("Can be deleted!");
                    writer.close();
                }
            }
            fProfileFile = profileFile;
        }
    }

    public void setProfileName(String profileName) {
        fProfileName = profileName;
    }

    public void deleteProfileFile() {
        if (fProfileFile != null && fProfileFile.exists()) {
            fProfileFile.delete();
        }
    }

    public void setSessionName(String sessionName) {
        fSessionName = sessionName;
    }


}
