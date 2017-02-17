/*
 * Copyright © 2017 Toori NL - All Rights Reserved 
 *
 * Created by Robert Berg, May 30, 2016
 * 
 */
package nl.toori.ant.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.LogLevel;
import org.apache.tools.ant.types.Mapper;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.OutputStyle;



/**
 * The Ant task that compiles SASS
 * @author roberg
 */
public class CompileTask extends Task {
	
	private final Compiler compiler = new Compiler();
	private final List<FileSet> filesets = new ArrayList<FileSet>();
	private final List<DirSet> includesPaths= new ArrayList<DirSet>();
	private Mapper mapper;
	private String todir;
	private OutputStyle outputStyle = OutputStyle.COMPRESSED;

	/**
	 * Adds a fileset of to be compiled sources
	 * @param fileset
	 */
	public void addFileset(FileSet fileset) {
		filesets.add(fileset);
	}

	/**
	 * Adds the include paths 
	 * @param includePath
	 */
	public void addIncludePaths(DirSet includePath) {
		includesPaths.add(includePath);
	}

	
	/**
	 * Adds a file mapper
	 * @param mapper
	 */
	public void addMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Set destination directory
	 * @param todir the dir where the files go
	 */
	public void setToDir(String todir) {
		this.todir = todir;
	}

	public void setOutputStyle(String name){
		this.outputStyle = OutputStyle.valueOf(name);
	}
		
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {
		validateRequired();

		for(FileSet fileset:filesets) {
			DirectoryScanner scanner = fileset.getDirectoryScanner(getProject());
			scanner.scan();
			File dir = scanner.getBasedir();

			String[] files = scanner.getIncludedFiles();
			
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i];
				String[] output = mapper.getImplementation().mapFileName(fileName);
				if (output != null) {
					try {
						compile(new File(dir, fileName),output,dir);

					} catch (IOException io) {
						log("Failed to compile file: " + fileName, LogLevel.ERR.getLevel());
					}
				}
			}
		}
	}

	/**
	 * Adds the include paths from the dir set to the options
	 * 
	 * @param options jsass options to add to
	 */
	private void addIncludesToOptions(Options options) {
		for(DirSet dirset:includesPaths) {
			DirectoryScanner scanner = dirset.getDirectoryScanner(getProject());
			File dir = scanner.getBasedir();

			String[] dirs = scanner.getIncludedDirectories();
			for (int i = 0; i < dirs.length; i++) {
				options.getIncludePaths().add(new File(dir,dirs[i]));
			}
		}
	}
	
	/**
	 * Validate if all required attributes are set.
	 *
	 * @throws BuildException the build exception
	 */
	private void validateRequired() throws BuildException {
		StringBuilder errorString = new StringBuilder();

		if (mapper == null)
			errorString.append("Mapper property is required\n");
		if (todir == null || "".equals(todir))
			errorString.append("Output directory is not specified\n");

		if (errorString.length()>0) {
			throw new BuildException(errorString.toString());
		}
	}

	
	/**
	 * Compile.
	 *
	 * @param source the source
	 * @param dest the dest
	 * @param sourcedir the sourcedir
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void compile(final File source, String[] files, File sourcedir) throws IOException {
	    URI inputFile = source.toURI();
	    for(String file:files) {
		    File dest = new File(todir,file);
		    URI outputFile = dest.toURI();
	
		    Options options = new Options();
	
		    options.setOutputStyle(outputStyle);
			log("Compile " + source.getName()+ " to " + outputFile.toString());
			
			addIncludesToOptions(options);
			try {
		      Output output = compiler.compileFile(inputFile, outputFile, options);
		      options.setIsIndentedSyntaxSrc(source.getName().endsWith(".sass"));

	    	  FileUtils.writeStringToFile(dest, output.getCss(),StandardCharsets.UTF_8);
		      
		    } catch (CompilationException e) {
		    	throw new BuildException("Error in "+source.getName()+ ": " + e.getErrorMessage(),e);
		    }
	    }
	}

}
