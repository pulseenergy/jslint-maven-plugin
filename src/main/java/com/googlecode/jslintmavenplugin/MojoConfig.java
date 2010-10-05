package com.googlecode.jslintmavenplugin;

public class MojoConfig {

    private boolean verbose;
    private static MojoConfig instance;

    private MojoConfig() { }

    public static MojoConfig getInstance() {
        if (instance == null) {
            instance = new MojoConfig();
        }
        return instance;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
