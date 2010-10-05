package com.googlecode.jslintmavenplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class StreamingProcessExecutor {
    private boolean streamOutput;

    public StreamingProcessExecutor(boolean streamOutput) {
        this.streamOutput = streamOutput;
    }

    public StreamingProcessExecutor() {
        this(true);
    }

    public String execute(List<String> args) {
        StringBuffer buffer = new StringBuffer();
        try {
            Process process = create(args);
            BufferedReader inputReader = getInputStream(process);
            for (String line = inputReader.readLine(); line != null; line = inputReader.readLine()) {
                if (streamOutput) {
                    System.out.println(line);
                }
                buffer.append(line).append("\n");
            }

            process.waitFor();

            return buffer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Process create(List<String> commandLine) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(commandLine);
        pb.redirectErrorStream(true);

        if (MojoConfig.getInstance().isVerbose()) {
            System.out.println("Working dir is " + System.getProperty("user.dir"));
        }

        return pb.start();
    }

    private BufferedReader getInputStream(Process p) {
        InputStream inputStream = p.getInputStream();
        return new BufferedReader(new InputStreamReader(inputStream));
    }

}