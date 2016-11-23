package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import com.castsoftware.dmt.engine.discovery.BasicProjectsDiscovererAdapter;
import com.castsoftware.dmt.engine.discovery.IProjectsDiscovererUtilities;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper.ProfileOrProjectTypeConfiguration.LanguageConfiguration;
import com.castsoftware.dmt.engine.project.IProfileReadOnly;
import com.castsoftware.dmt.engine.project.Project;
import com.castsoftware.util.logger.Logging;

/**
 * Basic discoverer for
 */
public class CppCompilDBDiscoverer extends BasicProjectsDiscovererAdapter
{
    /**
     * Default constructor used by the discovery engine
     */
    public CppCompilDBDiscoverer()
    {
    }

	@Override
    public void buildProject(String relativeFilePath, String content, Project project,
        IProjectsDiscovererUtilities projectsDiscovererUtilities)
    {
        String projectDescriptor = project.getMetadata(IProfileReadOnly.METADATA_DESCRIPTOR).getValue();
        if ((!projectDescriptor.equals("compile_commands.json")) || (!parseProjectFile(relativeFilePath, content, project, projectsDiscovererUtilities)))
            projectsDiscovererUtilities.deleteProject(project.getId());
    }

    private static boolean parseProjectFile(String relativeFilePath, String content, Project project, IProjectsDiscovererUtilities projectsDiscovererUtilities)
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

        ProjectFileScanner.scan(relativeFilePath, project, content, cLanguageId, cHeaderLanguage, cPlusPlusLanguage, cPlusPlusHeaderLanguage, cFamilyNotCompilableLanguage);

        return true;
    }
}
