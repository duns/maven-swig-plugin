// Copyright FreeHEP, 2006.
package org.freehep.maven.swig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.FileUtils;
import org.freehep.maven.nar.Linker;
import org.freehep.maven.nar.NarArtifact;
import org.freehep.maven.nar.NarManager;
import org.freehep.maven.nar.NarUtil;

/**
 * Compiles swg files using the swig compiler.
 * 
 * @goal generate
 * @description Compiles swg files using the swig compiler.
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @author <a href="Mark.Donszelmann@slac.stanford.edu">Mark Donszelmann</a>
 * @version $Id: src/main/java/org/freehep/maven/swig/SwigMojo.java 08b8f10711b3 2006/09/27 23:04:37 duns $
 */
public class SwigMojo extends AbstractMojo {
        
    /**
     * Enable C++ processing, same as -c++ option for swig.
     *
     * @parameter expression="${swig.cpp}" default-value="false"
     */
    private boolean cpp;              
        
    /**
     * Add include paths. By default the current directory is scanned.
     *
     * @parameter
     */
    private List includePaths;

    /**
     * List of warning numbers to be suppressed, same as -w option for swig.
     * 
     * @parameter expression="${swig.noWarn}"
     */
    private String noWarn;
    
    /**
     * The target directory into which to generate the output.
     *
     * @parameter expression="${project.build.directory}/swig"
     */
    private File targetDirectory;

    /**
     * The package name for the generated java files (fully qualified ex: org.freehep.jni).
     * 
     * @parameter expression="${swig.packageName}"
     */
    private String packageName;
    
    /**
     * The target directory into which to generate the java output, becomes -outdir option for swig.
     *
     * @parameter expression="${project.build.directory}/swig/java"
     */
    private String javaTargetDirectory;

    /**
     * The directory to look for swig files and swig include files.
     * Also added to -I flag when swig is run.
     * 
     * @parameter expression="${basedir}/src/main/swig"
     * @required
     */
    private String sourceDirectory;

    /**
     * The swig file to process, normally in source directory set by swigDirectory.
     *
     * @parameter
     * @required
     */
    private String source;

    /**
     * The Architecture for picking up swig,
     * Some choices are: "x86", "i386", "amd64", "ppc", "sparc", ...
     * Defaults to a derived value from ${os.arch}
     *
     * @parameter expression="${os.arch}"
     * @required
     */
    private String architecture;    

    /**
     * The Operating System for picking up swig.
     * Some choices are: "Windows", "Linux", "MacOSX", "SunOS", ...
     * Defaults to a derived value from ${os.name}
     *
     * @parameter expression=""
     */
    private String os;

    /**
     * The granularity in milliseconds of the last modification
     * date for testing whether a source needs recompilation
     *
     * @parameter expression="${idlj.staleMillis}" default-value="0"
     * @required
     */
    private int staleMillis;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

	/**
	 * Artifact handler
	 * 
	 * @component role="org.apache.maven.artifact.handler.ArtifactHandler"
	 * @required
	 * @readonly
	 */
    private ArtifactHandler artifactHandler;

	/**
	 * Artifact resolver, needed to download source jars for inclusion in
	 * classpath.
	 * 
	 * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
	 * @required
	 * @readonly
	 */
	private ArtifactResolver artifactResolver;

	/**
	 * Remote repositories which will be searched for source attachments.
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @required
	 * @readonly
	 */
	private List remoteArtifactRepositories;

    /**
     * To look up Archiver/UnArchiver implementations
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.manager.ArchiverManager}"
     * @required
     */
    private ArchiverManager archiverManager;

    public void execute() throws MojoExecutionException, MojoFailureException {
    	targetDirectory = new File(targetDirectory, cpp ? "c++" : "c");        
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }

        if (!FileUtils.fileExists(javaTargetDirectory)) {
            FileUtils.mkdir( javaTargetDirectory );
        }

        if (project != null) {
            project.addCompileSourceRoot(javaTargetDirectory);
        }

        if (!sourceDirectory.endsWith("/")) {
            sourceDirectory = sourceDirectory+"/";
        }
        
        // NOTE, since a project will just load this as a plugin, there is no way to look up
        // the org.swig:swig dependency, so we hardcode that in here.
        Linker linker = new Linker("g++");
        NarManager narManager = new NarManager(getLog(), localRepository, project, architecture, os, linker);
        Artifact swigNar = new DefaultArtifact("org.swig", "swig", VersionRange.createFromVersion("1.3.29-1-SNAPSHOT"), "compile", "jar", "", artifactHandler);
        swigNar = new NarArtifact(swigNar, narManager.getNarInfo(swigNar));

        List narArtifacts = new ArrayList();
        narArtifacts.add(swigNar);
        
        narManager.downloadAttachedNars(narArtifacts, remoteArtifactRepositories, artifactResolver, null);
        narManager.unpackAttachedNars(narArtifacts, archiverManager, null);

		File swig = new File(narManager.getNarFile(swigNar).getParentFile(), "nar");
		File swigInclude = new File(swig, "include");
		File swigJavaInclude = new File(swigInclude, "java");
		swig = new File(swig, "bin");
		swig = new File(swig, NarUtil.getAOL(architecture, os, linker, null));
		swig = new File(swig, "swig");
				
        // FIXME runs always. we could check for c++ output file ...
        getLog().info( "Running SWIG compiler on "+source+" ...");
        int error = runCommand(generateCommandLine(swig, swigInclude, swigJavaInclude));
        if (error != 0) {
        	throw new MojoFailureException("SWIG returned error code "+error);
        }
    }


    private String[] generateCommandLine(File swig, File swigInclude, File swigJavaInclude) throws MojoExecutionException {
        
        List cmdLine = new ArrayList();
        
        cmdLine.add(swig.toString());
        
        if (getLog().isDebugEnabled()) {
            cmdLine.add("-v");
        }
        
        // FIXME hardcoded
        cmdLine.add("-java");
        
        if (cpp) {
            cmdLine.add("-c++");
        }        

        // warnings
        if (noWarn != null) {
        	String noWarns[] = noWarn.split(",| ");
        	for (int i=0; i<noWarns.length; i++) {
        		cmdLine.add("-w"+noWarns[i]);
        	}
        }
        
        // output file
        String baseName = FileUtils.basename(source);
        cmdLine.add("-o");
        cmdLine.add((new File(targetDirectory, baseName+(cpp ? "cxx" : "c"))).toString());
        
        // package for java code
        if (packageName != null) {
        	cmdLine.add("-package");
        	cmdLine.add(packageName);
        }
        
        // output dir for java code
        cmdLine.add("-outdir");
        cmdLine.add(javaTargetDirectory);
        
        // user added include dirs
        if (includePaths != null) {
            for (Iterator i = includePaths.iterator(); i.hasNext(); ) {
                cmdLine.add("-I"+i.next());
            }
        }
        // default include dirs
        cmdLine.add("-I"+"src/main/include");
        cmdLine.add("-I"+sourceDirectory);

        // system swig include dirs
        cmdLine.add("-I"+swigJavaInclude.toString());
        cmdLine.add("-I"+swigInclude.toString());        
        
        // swig file
        cmdLine.add(sourceDirectory+source);
        
        getLog().info(cmdLine.toString());
        
        return (String[])cmdLine.toArray(new String[cmdLine.size()]);
    }   
    
    private int runCommand(String[] cmdLine) throws MojoExecutionException {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(cmdLine);
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), true);
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), false);
            
            errorGobbler.start();
            outputGobbler.start();
            return process.waitFor();
        } catch (Throwable e) {
            throw new MojoExecutionException("Could not launch " + cmdLine[0], e);
        }
    }
    
    class StreamGobbler extends Thread {
        InputStream is;
        boolean error;
        
        StreamGobbler(InputStream is, boolean error) {
            this.is = is;
            this.error = error;
        }
        
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (error) {
                        getLog().error(line);
                    } else {
                        getLog().debug(line);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }    
}