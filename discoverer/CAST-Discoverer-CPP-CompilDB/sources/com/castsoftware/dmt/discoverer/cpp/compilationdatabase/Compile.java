package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Compile {
	private String directory;
	private String filename;

	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory.replace("\\", "/");
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = getRelativePath(getDirectory(), filename.replace("\\", "/"));
	}

    protected static String getRelativePath(String directory, String file)
    {
    	if (file.startsWith("/"))
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
