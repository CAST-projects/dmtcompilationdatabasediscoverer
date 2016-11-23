package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import com.castsoftware.dmt.engine.project.IResourceReadOnly;
import com.castsoftware.dmt.engine.project.Profile;
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
    	String directoryName = "";
    	reader = new BufferedReader(new StringReader(projectContent), projectContent.length());

        try
        {
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
            	line = line.trim();
            	if ("{".equals(line))
            	{
            		// new
            		directoryName = "";
            	}
            	else if ("}".equals(line))
            	{
            		
            	}
            	else if (line.startsWith("\"command\":"))
            	{
            		
            	}
            	else if (line.startsWith("\"directory\":"))
            	{
            		String name = line.substring(line.indexOf(":") + 1).trim();
            		directoryName = name.substring(name.indexOf("\"") + 1, name.lastIndexOf("\""));
            		if (project.getName() == null)
            		{
                		int last = directoryName.lastIndexOf("/");
	            		String projectName = "";
	            		if (last < 0)
	            			projectName = directoryName;
	            		else
	            			projectName = directoryName.substring(last + 1);
	            		project.setName(projectName);
	                    project.addMetadata(IResourceReadOnly.METADATA_REFKEY, projectName);
            		}
            	}
            	else if (line.startsWith("\"file\":"))
            	{
            		String filename = line.substring(line.indexOf(":") + 1).trim();
            		filename = filename.substring(filename.indexOf("\"") + 1, filename.lastIndexOf("\""));
            		int languagId = 0;
            		if (filename.matches("^.*cpp$"))
                    	languagId = cPlusPlusLanguage;
                    else if (filename.matches("^.*c$"))
                    	languagId = cLanguageId;
                    else 
                    	languagId = cFamilyNotCompilableLanguage;
            		if (!"".equals(directoryName))
            		{
            			if (filename.startsWith(directoryName))
            				filename = filename.substring(directoryName.length() + 1);
            		}
                    if (project.getFileReference(filename) == null)
                    	project.addSourceFileReference(buildPackageRelativePath(project, filename), languagId);
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
}