package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.castsoftware.dmt.engine.project.Profile;
import com.castsoftware.dmt.engine.project.Project;

// ESCA-JAVA0014:
/**
 * xxx
 */
public class PathHelper
{
    /**
     * @param rootPath
     *            xxx
     * @param file
     *            xxx
     * @return xxx
     */
    public static String getRelativeConnectionPath(String rootPath, String file)
    {
        if (!isFullPath(file))
        	return null;
        
        if (!file.startsWith(rootPath))
        	return null;
        
        String relativeFilepath = file.substring(rootPath.length() + 1);
        return removeRelativePath(relativeFilepath);
    }

    /**
     * @param project
     *            xxx
     * @param path
     *            xxx
     * @param relativeFilePath
     *            xxx
     * @param directory
     *            xxx
     * @param file
     *            xxx
     * @return xxx
     */
    public static String getRelativeConnectionPath(Project project, String path, String relativeFilePath,
        String directory, String file)
    {
        if (isFullPath(file))
        {
            if (file.startsWith(path))
            {
                String relativeFile = file.substring(path.length() + 1);
                if (relativeFilePath != null)
                {
                    if (relativeFile.startsWith(relativeFilePath) && (relativeFile.length() > (relativeFilePath.length() + 1)))
                    {
                        relativeFile = relativeFile.substring(relativeFilePath.length() + 1);
                        String fileRelativeRef = removeRelativePath(relativeFile);
                        return buildPackageRelativePath(project, fileRelativeRef);
                    }
                    return removeRelativePath(relativeFile);
                }
                // return buildPackageRelativePath(project, relativeFile);
                return removeRelativePath(relativeFile);
            }
            return removeRelativePath(file);
        }
        if (directory.startsWith(path))
        {
            String relativeDirectory = "";
            if (directory.length() > path.length())
                relativeDirectory = directory.substring(path.length() + 1);

            if (relativeFilePath == null)
            {
                String fileRelativeRef = (relativeDirectory.equals("")
                    ? removeRelativePath(file) : removeRelativePath(relativeDirectory + "/" + file));
                return buildPackageRelativePath(project, fileRelativeRef);
            }
            if (relativeDirectory.length() == 0)
            {
                return buildPackageRelativePath(project, file);
            }
            else if (relativeDirectory.startsWith(relativeFilePath))
            {
                relativeDirectory = (relativeDirectory.equals("") ? relativeDirectory
                        .substring(relativeFilePath.length() + 1) : relativeDirectory
                        .substring(relativeFilePath.length() + 1));
                String fileRelativeRef = removeRelativePath(relativeDirectory + "/" + file);
                return buildPackageRelativePath(project, fileRelativeRef);
            }
            return removeRelativePath(directory + "/" + file);
        }
        return removeRelativePath(directory + "/" + file);
    }

    /**
     * @param project
     *            xxx
     * @param projectPath
     *            xxx
     * @return xxx
     */
    public static String buildPackageRelativePath(Project project, String projectPath)
    {
        if (new File(projectPath).isAbsolute() || projectPath.startsWith("/"))
            return projectPath;

        return Profile.buildPackageRelativePath(project.getName(), projectPath);
    }

    private static Boolean isFullPath(String file)
    {
        if (file.startsWith("/"))
            return true;
        if (file.length() > 1)
        {
            if (":".equals(file.substring(1, 2)))
                return true;
            if ("\\\\".equals(file.substring(1, 2)))
                return true;
        }

        return false;
    }

    /**
     * @param path
     *            the path to transform
     * @return the path without unnecessary ..
     */
    public static String removeRelativePath(String path)
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
