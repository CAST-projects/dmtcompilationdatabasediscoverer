package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.castsoftware.dmt.discoverer.cpp.compilationdatabase.CompileFile.Macro;
import com.castsoftware.dmt.engine.discovery.IProjectsDiscovererUtilities;
import com.castsoftware.dmt.engine.project.IResourceReadOnly;
import com.castsoftware.dmt.engine.project.Profile;
import com.castsoftware.dmt.engine.project.Profile.Option;
import com.castsoftware.dmt.engine.project.Project;
import com.castsoftware.util.logger.Logging;

/**
 * Scanner for json file
 */
public class ProjectFileScanner
{
	static String sep = "";
	enum ArgumentTypes {
		CC("cc")
		,AR("ar")
		,CPP("c++");
		private String name = "";
		ArgumentTypes(String name){
			this.name = name;
		}
		public String toString(){
			return name;
		}
	}
	;
	
    private ProjectFileScanner()
    {
        // NOP
    }

    /**
     * Scan a compile_commands.json file and add info to the project.
     *
     * @param interpreter
     *            the project file interpreter
     * @param projectFilePath
     *            the path to the project file used for reference
     * @param projectContent
     *            the file content to scan.
     * @return {@code true} if no error was encountered during scanning. {@code false} otherwise.
     */
    public static void scan(String connectionPath, String relativeFilePath, Project project, String projectContent, IProjectsDiscovererUtilities projectsDiscovererUtilities, int cLanguageId, int cHeaderLanguage, int cPlusPlusLanguage, int cPlusPlusHeaderLanguage, int cFamilyNotCompilableLanguage)
    {
    	BufferedReader reader = null;
    	String command = null;
    	Boolean isArguments = false;
    	Boolean isInArguments = false;
    	Boolean isNextOutput = false;
    	ArgumentTypes argumentType = null;
    	List<String> includes = new ArrayList<String>();
    	String directory = null;
    	String file = null;
    	String output = null;
    	List<String> outputs = new ArrayList<String>();
    	int languageId = 0;
    	int languageHeaderId = 0;
    	List<CompileFile> compileFiles = new ArrayList<CompileFile>();
		CompileFile compileFile = null;
    	List<CompileLink> compileLinks = new ArrayList<CompileLink>();
		CompileLink compileLink = null;
    	
    	setSeparator(connectionPath);
    	
    	reader = new BufferedReader(new StringReader(projectContent), projectContent.length());

        try
        {
            for (String readline = reader.readLine(); readline != null; readline = reader.readLine())
            {
            	String line = readline.trim();
            	if ("{".equals(line))
            	{
            		// new
            		command = null;
            		isArguments = false;
            		isInArguments = false;
            		isNextOutput = false;
            		output = null;
            		argumentType = null;
            		directory = null;
            		file = null;
            		languageId = 0;
            		languageHeaderId = 0;
            		compileFile = null;
            		compileLink = null;
            	}
            	else if (line.startsWith("}"))
            	{
            		if (argumentType.equals(ArgumentTypes.AR))
            		{
            			compileLink.setLinkname(file);
            			compileLink.setDirectory(directory);
            			compileLink.setFilename(file);
            			for (String s : outputs)
            				compileLink.addOutput(s);
            			compileLinks.add(compileLink);
            		}
            		else if (argumentType.equals(ArgumentTypes.CC) || argumentType.equals(ArgumentTypes.CPP))
            		{
                		if ((command == null) && (!isArguments))
                		{
                			Logging.warn("Not supported format: no command and no arguments");
                			continue;
                		}
                        if (file.matches("^.*cpp$"))
                		{
                        	compileFile.setLanguageId(cPlusPlusLanguage);
                        	compileFile.setLanguageHeaderId(cPlusPlusHeaderLanguage);
                		}
                        else if (file.matches("^.*c$"))
                        {
                        	compileFile.setLanguageId(cLanguageId);
                        	compileFile.setLanguageHeaderId(cHeaderLanguage);
                        }
                        else 
                        	compileFile.setLanguageId(cFamilyNotCompilableLanguage);

                        compileFile.setDirectory(directory);
                        compileFile.setFilename(file);
                		
                		if (command != null)
                			compileFile.parseCommand(command, languageId, languageHeaderId);
                		else
                			compileFile.setOutput(output);

                		compileFiles.add(compileFile);
            		}
            		else
            		{
                		if (command != null)
                		{
            				Logging.warn("Bad command", "COMMAND", command);
                		}
            		}
            	}
            	else if (line.startsWith("\"arguments\": ["))
            	{
            		isArguments = true;
            		isInArguments = true;
            		isNextOutput = false;
            		//if (line.length() > 14)
            		//	arguments = line.substring(15);
            	}
            	else if (line.startsWith("]"))
            	{
            		if (isInArguments)
            		{
	            		isInArguments = false;
	            		isNextOutput = false;
	            		if (argumentType.equals(ArgumentTypes.AR))
	            		{
	            			
	            		}
	            		else if (argumentType.equals(ArgumentTypes.CC) || argumentType.equals(ArgumentTypes.CPP))
	            		{
	            			
	            		}
	            		else
	            		{
	            			
	            		}
            		}
            	}
            	else if (isInArguments)
            	{
            		if (line.contains("\"" + ArgumentTypes.CC.toString() + "\""))
            		{
            			argumentType = ArgumentTypes.CC;
            			compileFile = new CompileFile();
            		}
            		else if (line.contains("\"" + ArgumentTypes.AR.toString() + "\""))
            		{
            			argumentType = ArgumentTypes.AR;
            			compileLink = new CompileLink();
            		}
            		else if (line.contains("\"" + ArgumentTypes.CPP.toString() + "\""))
            		{
            			argumentType = ArgumentTypes.CPP;
            			compileFile = new CompileFile();
            		}
            		else
            		{
            			if (argumentType.equals(ArgumentTypes.AR))
            			{
            				if (line.contains(".o"))
            					outputs.add(line.substring(1,line.indexOf("\"",2)));
            			}
            			else
            			{
            				if (line.startsWith("\"-D"))
	                		{
	                			compileFile.addMacro(line.substring(3,line.indexOf("\"",2)));
	                		}
	                		else if (line.startsWith("\"-I"))
	                		{
	                			compileFile.addInclude(line.substring(3,line.indexOf("\"",2)));
	                			//includes.add(line.substring(3,line.indexOf("\"",2)));
	                		}
	                		else if (line.startsWith("\"-o"))
	                		{
	                			isNextOutput = true;
	                		}
	                		else if (isNextOutput)
	                		{
	                			output = line.substring(1,line.indexOf("\"",2));
	                			isNextOutput = false;
	                		}
            			}
            		}
            	}
            	else if (line.startsWith("\"command\":"))
            	{
            		String val = line.substring(line.indexOf(":") + 1).trim();
            		command = val.substring(val.indexOf("\"") + 1, val.lastIndexOf("\""));
            		if (command.startsWith(ArgumentTypes.CC.toString() + " "))
            		{
            			argumentType = ArgumentTypes.CC;
            			compileFile = new CompileFile();
            		}
            		else if (command.startsWith("\"" + ArgumentTypes.AR.toString() + " "))
            		{
            			argumentType = ArgumentTypes.AR;
            			compileLink = new CompileLink();
            		}
            		else if (command.startsWith("\"" + ArgumentTypes.CPP.toString() + " "))
            		{
            			argumentType = ArgumentTypes.CPP;
            			compileFile = new CompileFile();
            		}
            	}
            	else if (line.startsWith("\"directory\":"))
            	{
            		String name = line.substring(line.indexOf(":") + 1).trim();
            		directory = name.substring(name.indexOf("\"") + 1, name.lastIndexOf("\""));
            	}
            	else if (line.startsWith("\"file\":"))
            	{
            		file = line.substring(line.indexOf(":") + 1).trim();
            		file = file.substring(file.indexOf("\"") + 1, file.lastIndexOf("\""));
            	}
            	//project.addMetadata("pchGenerator", pchGeneratorPath);
                //project.setName(name);
                //project.addMetadata(IResourceReadOnly.METADATA_REFKEY, name);
                //if (project.getOption(macroName) == null)
                //    project.addOption(macroName, macroValue);
                //if (project.getMetadata(macroName) == null)
                //    project.addMetadata(macroName, macroValue);
                //if (project.getFileReference(fileRef) == null)
                //{
                //    project.addSourceFileReference(fileRef, languageId);
                //}
                //if (project.getDirectoryReference(directoryRef) == null)
                //    project.addDirectoryReference(directoryRef, languageId, resourceTypeId);
                //}
                //if (project.getDirectoryReference(directoryRef) == null)
                //    project.addDirectoryReference(directoryRef, languageId, resourceTypeId);
                //}
            }

            if (compileLinks.size() > 0)
            {
            	// a project per link
            	for (CompileLink cl : compileLinks)
            	{
            		// identfiy the selected compileFile
            		cl.setCompileFiles(compileFiles);
            		
            		String id = project.getId() + "." + cl.getFilename();
            		Project p = projectsDiscovererUtilities.createInitialProject(id, cl.getLinkname(), project.getType(), id, project.getPath());
            		p.addMetadata(IResourceReadOnly.METADATA_REFKEY, cl.getLinkname());
            		p.addOutputContainer(cl.getFilename(), 0);
            		for (CompileFile cf : cl.getCompileFiles())
            		{
            			String fileRef = getRelativeConnectionPath(p, connectionPath, relativeFilePath, cf.getDirectory(), cf.getFilename());
        	            if (p.getFileReference(fileRef) == null)
                			p.addSourceFileReference(fileRef, cf.getLanguageId());
    		            
        	            for (Macro macro : cf.getMacros())
    		            	addMacro(p, macro.getKey(), macro.getValue());
            			
        	            for (String include : cf.getIncludes())
            			{
            				String includeRef = getRelativeConnectionPath(p, connectionPath, relativeFilePath, cf.getDirectory(), include);
        					if (p.getDirectoryReference(includeRef) == null && p.getResourceReference(includeRef) == null)
        						p.addDirectoryReference(includeRef, cf.getLanguageId(), cf.getLanguageHeaderId());
            			}
            		}
            	}
            	projectsDiscovererUtilities.deleteProject(project.getId());
            }
            else
            {
            	// no link: create a default project with all files
	            if (project.getName() == null)
	    		{
	        		int last = directory.lastIndexOf("/");
	        		String projectName = "";
	        		if (last < 0)
	        			projectName = directory;
	        		else
	        			projectName = directory.substring(last + 1);
	        		project.setName(projectName);
	                project.addMetadata(IResourceReadOnly.METADATA_REFKEY, projectName);
	    		}
	            for (CompileFile cf : compileFiles)
	            {
		            String fileRef = getRelativeConnectionPath(project, connectionPath, relativeFilePath, cf.getDirectory(), cf.getFilename());
		            if (project.getFileReference(fileRef) == null)
		            	project.addSourceFileReference(fileRef, cf.getLanguageId());

		            for (Macro macro : cf.getMacros())
		            	addMacro(project, macro.getKey(), macro.getValue());

		            for (String include : cf.getIncludes())
					{
						String includeRef = getRelativeConnectionPath(project, connectionPath, relativeFilePath, cf.getDirectory(), include);
						if (project.getDirectoryReference(includeRef) == null && project.getResourceReference(includeRef) == null)
							project.addDirectoryReference(includeRef, cf.getLanguageId(), cf.getLanguageHeaderId());
					}
	            }
            }
        }
        catch (IOException e)
        {
            Logging.managedError(e, "cast.dmt.discover.cpp.compilationdatabase.ioExceptionInProjectParsing", "PATH", relativeFilePath);
        }
        finally
        {
        	try {
				reader.close();
			} catch (IOException e) {
				Logging.managedError(e, "cast.dmt.discover.cpp.compilationdatabase.ioExceptionInProjectParsing", "PATH", relativeFilePath);
			}
        }
        return;
    }
    private static String buildPackageRelativePath(Project project, String projectPath)
    {
        if (new File(projectPath).isAbsolute() || projectPath.startsWith("/"))
            return projectPath;
        
        return Profile.buildPackageRelativePath(project.getName(), projectPath);
    }

    private static void addMacro(Project project, String macroName, String macroValue)
    {
		Option o = project.getOption(macroName);
        if (o == null)
            project.addOption(macroName, macroValue);
        else
        {
        	if (macroValue == null && o.getValue() != null)
        		Logging.warn("A", "MACRO", macroName, "VALUE", o.getValue());
        	if (macroValue != null && !macroValue.equals(o.getValue()))
        		Logging.warn("B", "MACRO", macroName, "VALUE1", macroValue, "VALUE2", o.getValue());
        }	
        if (project.getMetadata(macroName) == null)
            project.addMetadata(macroName, macroValue);
    	return;
    }
    
    private static void setSeparator(String rootPath)
    {
    	if (rootPath.startsWith("/"))
    		sep = "/";
        else
        	sep = "\\";
    }

    private static String getRelativeConnectionPath(Project project, String connectionPath, String relativeFilePath, String directory, String file)
    {
    	if (file.startsWith("/"))
    	{
    		if (file.startsWith(connectionPath))
    		{
    			String relativeFile = file.substring(connectionPath.length() + 1);
    			if (relativeFilePath != null)
    			{
	    			if (relativeFile.startsWith(relativeFilePath) && (relativeFile.length() > (relativeFilePath.length() + 1)))
	    			{
	    				relativeFile = relativeFile.substring(relativeFilePath.length() + 1);
	    				String fileRelativeRef = removeRelativePath(relativeFile);
						return buildPackageRelativePath(project, fileRelativeRef);
	    			}
	    			else
	    				return removeRelativePath(relativeFile);
    			}
    			else
    				return buildPackageRelativePath(project, relativeFile);
    		}
    		else
    			return removeRelativePath(file);
    	}
    	else
    	{
    		if (directory.startsWith(connectionPath))
	    	{
	    		String relativeDirectory = "";
	    		if (directory.length() > connectionPath.length())
	    			relativeDirectory = directory.substring(connectionPath.length() + 1);
	    			
	    		if (relativeFilePath == null)
	    		{
	    			String fileRelativeRef = (relativeDirectory.equals("") ? removeRelativePath(file) : removeRelativePath(relativeDirectory + "/" + file));
    				return buildPackageRelativePath(project, fileRelativeRef);	    			
	    		}
	    		else
	    		{
	    			if (relativeDirectory.startsWith(relativeFilePath))
	    			{
		    			relativeDirectory = (relativeDirectory.equals("") ? relativeDirectory.substring(relativeFilePath.length() + 1) : relativeDirectory.substring(relativeFilePath.length() + 1));
		    			String fileRelativeRef = removeRelativePath(relativeDirectory + "/" + file);
	    				return buildPackageRelativePath(project, fileRelativeRef);
		    		}
		    		else
		    			return removeRelativePath(directory + "/" + file);
	    		}
	    	}
			return removeRelativePath(directory + "/" + file);
    	}
    }
    
    private static String removeRelativePath(String path)
    {
    	List<String> list = new ArrayList<String>(Arrays.asList(path.split("/")));
    	List<String> relativeList = new ArrayList<String>();
    	
    	for (int i = 0; i < list.size(); i++)
    	{
    		String fld1 = list.get(i);
    		if ("..".equals(fld1))
    		{
    			if (relativeList.size() > 0 && !"..".equals(relativeList.get(relativeList.size() - 1)))
    				relativeList.remove(relativeList.size() - 1);
    			else
    				relativeList.add(fld1);
    		}
    		else
    			relativeList.add(fld1);
    	}
		String relativePath = "";
    	for (int i = 0; i < relativeList.size(); i++)
    	{
    		if (i > 0)
    			relativePath += "/";
    		relativePath += relativeList.get(i);
    	}
    	return relativePath;
    }
}

