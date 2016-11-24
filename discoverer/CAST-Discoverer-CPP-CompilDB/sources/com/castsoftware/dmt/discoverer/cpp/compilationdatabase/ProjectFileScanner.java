package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

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
    public static void scan(String relativeFilePath, Project project, String projectContent, int cLanguageId, int cHeaderLanguage, int cPlusPlusLanguage, int cPlusPlusHeaderLanguage, int cFamilyNotCompilableLanguage)
    {
    	String cFileExtensions = "*.c;*.pc;*.ppc";
    	BufferedReader reader = null;
    	String command = null;
    	String directory = null;
    	String file = null;
    	int languageId = 0;
    	int languageHeaderId = 0;
    	reader = new BufferedReader(new StringReader(projectContent), projectContent.length());

        try
        {
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
            	line = line.trim();
            	if ("{".equals(line))
            	{
            		// new
            		command = null;
            		directory = null;
            		file = null;
            		languageId = 0;
            		languageHeaderId = 0;
            	}
            	else if ("}".equals(line))
            	{
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

                    if (file.matches("^.*cpp$"))
            		{
                    	languageId = cPlusPlusLanguage;
                    	languageHeaderId = cPlusPlusHeaderLanguage;
            		}
                    else if (file.matches("^.*c$"))
                    {
                    	languageId = cLanguageId;
                    	languageHeaderId = cHeaderLanguage;
                    }
                    else 
                    	languageId = cFamilyNotCompilableLanguage;

            		if (!"".equals(directory))
            		{
            			if (file.startsWith(directory))
            				file = file.substring(directory.length() + 1);
            		}
                    if (project.getFileReference(file) == null)
                    	project.addSourceFileReference(buildPackageRelativePath(project, file), languageId);
            		
            		if (command != null)
            		{
            			if (!command.startsWith("cc"))
		        		{
            				Logging.warn("Bad command", "COMMAND", command);
		        		}
		        		else
		        		{
		        			parseCommand(project, command, languageId, languageHeaderId);
		        		}
            		}
            	}
            	else if (line.startsWith("\"command\":"))
            	{
            		String val = line.substring(line.indexOf(":") + 1).trim();
            		command = val.substring(val.indexOf("\"") + 1, val.lastIndexOf("\""));
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
        	reader = null;
        }
        return;
    }
    private static String buildPackageRelativePath(Project project, String projectPath)
    {
        if (new File(projectPath).isAbsolute() || projectPath.startsWith("/"))
            return projectPath;
        
        return Profile.buildPackageRelativePath(project.getName(), projectPath);
    }

    private static void parseCommand(Project project, String command, int languageId, int languageHeaderId)
    {
    	int pos1 = command.indexOf("-D");
    	while (pos1 > 0)
    	{
    		String macro = "";
    		int pos2 = command.indexOf(" ", pos1);
    		if (pos2 > 0)
    			macro = command.substring(pos1 + 2, pos2);
    		else
    			macro = command.substring(pos1 + 2);
    		
    		String macroName = "";
    		String macroValue = null;
    		if (macro.contains("="))
    		{
    			String [] values = macro.split("=");
    			macroName = values[0];
    			macroValue = values[1];
    		}
    		else
    		{
    			macroName = macro;
    		}
    		
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
	        pos1 = command.indexOf("-D",pos1 + 1);
    	}

    	pos1 = command.indexOf("-I");
    	while (pos1 > 0)
    	{
    		String include = "";
    		int pos2 = command.indexOf(" ", pos1);
    		if (pos2 > 0)
    			include = command.substring(pos1 + 2, pos2);
    		else
    			include = command.substring(pos1 + 2);
    		
    		String directoryRef = buildPackageRelativePath(project, include);
    		if (project.getDirectoryReference(directoryRef) == null)
    			project.addDirectoryReference(directoryRef, languageId, languageHeaderId);
    		
	        pos1 = command.indexOf("-I",pos1 + 1);
    	}
    	return;
    }
}