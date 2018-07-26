package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Compile
 */
public abstract class Compile {
	private String directory;
	private String filename;

    static int cLanguageId = 1;
    static int cHeaderLanguage = 1;
    static int cPlusPlusLanguage = 2;
    static int cPlusPlusHeaderLanguage = 1;
    static int cFamilyNotCompilableLanguage = 3;

    /**
     * @return the directory of the compile
     */
	public String getDirectory() {
		return directory;
	}

    /**
     * @param directory
     *            the directory of the compile
     */
	public void setDirectory(String directory) {
		this.directory = directory.replace("\\", "/");
	}

    /**
     * @return the full path of the file or the library
     */
	public String getFilename() {
		return filename;
	}

    /**
     * @param filename
     *            the full path of the file or the library
     *            when the path is relative, it's relative to the directory
     */
	public void setFilename(String filename) {
		this.filename = getRelativePath(getDirectory(), filename.replace("\\", "/"));
	}

    /**
     * @return the full path of the folder
     */
	public String getFolderPath() {
		int last = filename.lastIndexOf("/");
		if (last <= 0)
            return ".";
		return filename.substring(0, last);
	}

	/**
     * @return the full path of the file or the library
     */
	public String getRelativeFilename() {
		return getRelativePath(getDirectory(), filename.replace("\\", "/"));
	}

	protected static String getRelativePath(String directory, String file)
    {
    	if (file.length() <= 1)
    		return directory + "/" + file;
    	else if (file.startsWith("/"))
    	{
			return removeRelativePath(file, "/");
    	}
    	else if (":".equals(file.substring(1, 2)))
    	{
			//return removeRelativePath(file, "\\\\");
			return removeRelativePath(file, "/");
    	}
    	else
    	{
    		if (directory.startsWith("/"))
    			return removeRelativePath(directory + "/" + file, "/");
    		else if (":".equals(directory.substring(1, 2)))
    			//return removeRelativePath(directory + "\\" + file, "\\\\");
    			return removeRelativePath(directory + "/" + file, "/");
    		else
    			return removeRelativePath(directory + "/" + file, "/");
    	}
    }
    protected static String removeRelativePath(String path, String sep)
    {
    	List<String> list = new ArrayList<String>(Arrays.asList(path.split(sep)));
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
