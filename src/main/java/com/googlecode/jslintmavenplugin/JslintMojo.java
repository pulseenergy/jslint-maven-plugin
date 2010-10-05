package com.googlecode.jslintmavenplugin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @requiresDependencyResolution test
 * @goal test
 * @phase test
 */
public class JslintMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    /**
     * @parameter expression="${plugin.artifacts}"
     */
    private List<Artifact> dependencies;

    /**
     * @parameter expression="${skipTests}" default-value=false
     */
    private boolean skipTests;

    /**
     * @parameter expression="${jar}" default-value=""
     */
    private String jar;

    /**
     * A comma-separated list of options to pass to jslint4java.
     * 
     * @parameter expression="${options}" default-value=""
     */
    private String options;

    /**
     * @parameter expression="${predef}" default-value=""
     */
    private String predef;

    /**
     * A comma-separated list of directories that contain the Javascript files.
     *
     * @parameter expression="${src}" default-value=""
     */
    private String src;
    
    /**
     * A comma-separated list of directories to exclude
     * Added by DZH.
     * @parameter expression="${exclude}" default-value=""
     */
    private String exclude;

    /**
     * Show me what you're doing.
     *
     * @parameter expression="${verbose}" default-value=false
     */
    private boolean verbose;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        setupConfig();

        if (skipTests) {
            getLog().info("Tests are skipped");
            return;
        }

        printBanner();

        List<String> lines = runJslint();
        int numErrors = lines.size();
        if (numErrors > 0) {
            throw new MojoExecutionException(String.format("Jslint found %s error(s)!", numErrors));
        }
    }

    private List<String> runJslint() {
        StreamingProcessExecutor executor = new StreamingProcessExecutor();
        String results = executor.execute(buildArgumentsList());

        if (StringUtils.isBlank(results)) {
            return newArrayList();
        }
        
        return newArrayList(results.split("\n"));
    }

    private void setupConfig() {
        MojoConfig config = MojoConfig.getInstance();
        config.setVerbose(verbose);
    }

    private void printBanner() {
        System.out
                .println("\n" +
                        "----------------------------------\n" +
                        "J S  L I N T\n" +
                        "----------------------------------\n"
                );
    }

    private List<String> buildArgumentsList() {
        List<String> args = newArrayList();
        args.add("java");
        args.add("-jar");
        args.add(jar);
        args.addAll(buildOptionsList());
        args.addAll(buildPredefList());

        for (File file : getAllSourceFiles()) {
            args.add(file.getAbsolutePath());
        }

        if (MojoConfig.getInstance().isVerbose()) {
            System.out.println("Running: " + StringUtils.join(args, " "));
        }

        return args;
    }

    @SuppressWarnings({"unchecked"})
    private Collection<File> getAllSourceFiles() {

        List<String> srcDirs = newArrayList();
        if (src.contains(",")) {
            srcDirs.addAll(Arrays.asList(src.split(",")));
        } else {
            srcDirs.add(src);
        }
        
        List<String> excludeDirs = newArrayList();
        if (exclude.contains(",")) {
      	  excludeDirs.addAll(Arrays.asList(exclude.split(",")));
        } else {
      	  excludeDirs.add(exclude);
        }

        Collection<File> files = newArrayList();
        for (String file : srcDirs) {
      	  files.addAll(FileUtils.listFiles(new File(file), new String[] { "js" }, true));
        }
        /* modified by DZH; we don't want external js files to break the build */
        for (String file : excludeDirs) {
            files.removeAll(FileUtils.listFiles(new File(file), new String[] { "js" }, true));
        }
        return files;
    }

    private List<String> buildOptionsList() {
        List<String> options = newArrayList();

        if (StringUtils.isNotEmpty(this.options)) {
            List<String> parts = Arrays.asList(this.options.split(","));
            for (String part : parts) {
                options.add(String.format("--%s", part));
            }
        }

        return options;
    }

    private List<String> buildPredefList() {
        List<String> defs = newArrayList();
        if (StringUtils.isNotEmpty(predef)) {
            defs.add(String.format("--predef %s", predef));
        }
        return defs;
    }

}
