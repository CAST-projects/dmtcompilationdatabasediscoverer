package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.io.File;

import com.castsoftware.dmt.engine.discovery.AdvancedProjectsDiscovererAdapter;
import com.castsoftware.dmt.engine.discovery.IProjectsDiscovererUtilities;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper.ProfileOrProjectTypeConfiguration.LanguageConfiguration;
import com.castsoftware.dmt.engine.foldertree.IMetadataInterpreter;
import com.castsoftware.dmt.engine.project.IProfileReadOnly;
import com.castsoftware.dmt.engine.project.Project;
import com.castsoftware.util.logger.Logging;

/**
 * Basic discoverer for
 */
public class CppCompilDBDiscoverer extends AdvancedProjectsDiscovererAdapter
{
	String connectionPath;
	String directoryId;
	String relativeDirectoryPath;
	
    /**
     * Default constructor used by the discovery engine
     */
    public CppCompilDBDiscoverer()
    {
    	connectionPath = "";
    	directoryId = null;
    	relativeDirectoryPath = null;
    }

    @Override
    public void startTree(String packageId, String packageName, String packageType, String versionId, String connectionPath)
    {
    	super.startTree(packageId, packageName, packageType, versionId, connectionPath);
    	this.connectionPath = connectionPath;
    	//To simulate a real extraction
    	this.connectionPath = "/usr1/soter";
    	//this.connectionPath = "/home/yle/msieve";
    }

    @Override
    public void startDirectory(String directoryId, String directoryName, String relativeDirectoryPath)
    {
    	super.startDirectory(directoryId, directoryName, relativeDirectoryPath);
    	this.directoryId = directoryId;
    	this.relativeDirectoryPath = relativeDirectoryPath;
    }

    @Override
    public boolean mustProcessFile(String fileName)
    {
    	return false;
        //return fileName.endsWith("compile_commands.json");
    }

    @Override
	public void startTextFile(String fileId, String fileName, long fileSize, String relativeFilePath, String content)
    {
    	File f = new File(connectionPath);
        Project project = getProjectsDiscovererUtilities().createInitialProject(fileId, f.getName(), "dmtdevmicrosofttechno.CppProject", fileId, directoryId);
        if (fileName.equals("compile_commands.json"))
        {
        	parseProjectFile(relativeDirectoryPath, content, project, getProjectsDiscovererUtilities());
        	if (project.getName() == null)
        		getProjectsDiscovererUtilities().deleteProject(project.getId());
        }
        else
        	getProjectsDiscovererUtilities().deleteProject(project.getId());
    }
    
    private boolean parseProjectFile(String relativeFilePath, String content, Project project, IProjectsDiscovererUtilities projectsDiscovererUtilities)
    {
    	int cLanguageId = -1;
    	int cHeaderLanguage = -1;
    	int cPlusPlusLanguage = -1;
    	int cPlusPlusHeaderLanguage = -1;
    	int cFamilyNotCompilableLanguage = -1;

        for (LanguageConfiguration languageConfiguration : projectsDiscovererUtilities.getProjectTypeConfiguration(project.getType()).getLanguageConfigurations())
        {
        	int languageId = languageConfiguration.getLanguageId();
            if ("CLanguage".equals(languageConfiguration.getLanguageName()))
            {
            	cLanguageId = languageId;
            	//TODO: change API to access to the resource languages
            	//for (ResourceTypeConfiguration resourceTypeConfiguration : languageConfiguration.getResourceTypeConfigurations())
            	//{
            	//	if ("CHeaderLanguage".equals(resourceTypeConfiguration.getLanguageName()))
            	//		cHeaderLanguage = languageId;
            	//	}
            	//}
                cHeaderLanguage = 1;
                continue;
            }
            else if ("CPlusPlusLanguage".equals(languageConfiguration.getLanguageName()))
            {
            	cPlusPlusLanguage = languageId;
            	//TODO: change API to access to the resource languages
                cPlusPlusHeaderLanguage = 1;
                continue;
            }
            else if ("CFamilyNotCompilableLanguage".equals(languageConfiguration.getLanguageName()))
            {
            	cFamilyNotCompilableLanguage = languageId;
            	continue;
            }
        }
        if (cLanguageId == -1) 
        {
            Logging.managedError("cast.dmt.discover.cpp.compilationdatabase.missingLanguage","LNG","CLanguage");
        }
        if (cPlusPlusLanguage == -1)
        {
            Logging.managedError("cast.dmt.discover.cpp.compilationdatabase.missingLanguage","LNG","CPlusPlusLanguage");
        }
        if (cFamilyNotCompilableLanguage == -1)
        {
            Logging.managedError("cast.dmt.discover.cpp.compilationdatabase.missingLanguage","LNG","CFamilyNotCompilableLanguage");
        }

        String path = connectionPath;
        ProjectFileScanner.scan(path, relativeFilePath, project, content, cLanguageId, cHeaderLanguage, cPlusPlusLanguage, cPlusPlusHeaderLanguage, cFamilyNotCompilableLanguage);

        return true;
    }
}
