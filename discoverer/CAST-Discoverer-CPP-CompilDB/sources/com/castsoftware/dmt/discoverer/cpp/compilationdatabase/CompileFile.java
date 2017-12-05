package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * CompileFile detected in a json
 */
public class CompileFile extends Compile {

    CompileFile(String command, String type)
	{
        this.command = command;
        this.type = type;
	}

    /**
     * Macro used to do the compilation
     */
    public static class Macro
    {
    	private final String key;
    	private String value;
    	Macro(String key, String value)
    	{
    		this.key = key;
    		setValue(value);
    	}

        /**
         * @return macro key
         */
		public String getKey() {
			return key;
		}

        /**
         * @return macro value
         */
		public String getValue() {
			return value;
		}

        /**
         * @param value
         *            set the value of the macro
         */
		public void setValue(String value) {
			this.value = value;
		}
    }

	private String output;
	private int languageId = 0;
	private int languageHeaderId = 0;
	private String command;
	private final List<String> includes = new ArrayList<String>();
	private final List<Macro> macros = new ArrayList<Macro>();
	private String folder;
    private String type;
    private Boolean linked = false;

    @Override
	public void setFilename(String filename) {
		super.setFilename(filename);
	    if (filename.matches("^.*cpp$"))
	    {
	        setLanguageId(cPlusPlusLanguage);
	        setLanguageHeaderId(cPlusPlusHeaderLanguage);
	    }
	    else if (filename.matches("^.*c$"))
	    {
	        setLanguageId(cLanguageId);
	        setLanguageHeaderId(cHeaderLanguage);
	    }
	    else
	        setLanguageId(cFamilyNotCompilableLanguage);
	}
	
    /**
     * @return the output
     */
	public String getOutput() {
		return output;
	}

    /**
     * @param output
     *            the output (.o file)
     */
	public void setOutput(String output) {
        // this.output = getRelativePath(getDirectory(), output);
        this.output = output;
	}

    /**
     * @return the language ID
     */
	public int getLanguageId() {
		return languageId;
	}

    /**
     * @param languageId
     *            language ID corresponding to the file
     */
	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}

    /**
     * @return language ID corresponding to the file
     */
	public int getLanguageHeaderId() {
		return languageHeaderId;
	}

    /**
     * @param languageHeaderId
     *            language ID corresponding to the file
     */
	public void setLanguageHeaderId(int languageHeaderId) {
		this.languageHeaderId = languageHeaderId;
	}

    /**
     * @return command used for the compilation
     */
    public String getCommand()
    {
        return command;
    }

    /**
     * @param command
     *            command used for the compilation
     */
    public void setCommand(String command)
    {
        this.command = command;
    }

    /**
     * @return list of includes
     */
	public List<String> getIncludes() {
		return includes;
	}

    /**
     * @param include
     *            include to be added
     */
	public void addInclude(String include) {
		includes.add(include.replace("\\", "/"));
	}

    /**
     * @return list of macros
     */
	public List<Macro> getMacros() {
		return macros;
	}

    /**
     * @param name
     *            macro name
     * @return macro
     */
	public Macro getMacro(String name) {
		for (Macro m : macros)
		{
			if (name.equals(m.getKey()))
				return m;
		}
		return null;
	}

    /**
     * @param macro
     *            macro to add
     */
	public void addMacro(String macro) {
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

		if (getMacro(macroName) == null)
		{
			Macro m = new Macro(macroName, macroValue);
			macros.add(m);
		}
	}

    /**
     * @param commandline
     *            command used in the compilation. In that format, compiler followed by the list of arguments
     */
    public void parseCommand(String commandline)
    {
    	int pos1 = commandline.indexOf("-D");
    	while (pos1 > 0)
    	{
    		String macro = "";
    		int pos2 = commandline.indexOf(" ", pos1);
    		if (pos2 > 0)
    			macro = commandline.substring(pos1 + 2, pos2);
    		else
    			macro = commandline.substring(pos1 + 2);

    		addMacro(macro);
	        pos1 = commandline.indexOf("-D",pos1 + 1);
    	}

    	pos1 = commandline.indexOf("-I");
    	while (pos1 > 0)
    	{
    		String include = "";
    		int pos2 = commandline.indexOf(" ", pos1);
    		if (pos2 > 0)
    			include = commandline.substring(pos1 + 2, pos2);
    		else
    			include = commandline.substring(pos1 + 2);

    		addInclude(include);
    		//String includeRelativeRef = removeRelativePath(include);
    		//String directoryRef = buildPackageRelativePath(project, includeRelativeRef);
    		//if (project.getDirectoryReference(directoryRef) == null)
    		//	project.addDirectoryReference(directoryRef, languageId, languageHeaderId);

	        pos1 = commandline.indexOf("-I",pos1 + 1);
    	}
    	pos1 = commandline.indexOf("-o");
    	if (pos1 > 0)
    	{
    		String outputFile = "";
    		int pos2 = commandline.indexOf(".o", pos1);
    		if (pos2 > 0)
    		{
    			outputFile = commandline.substring(pos1 + 3, pos2 + 2).trim();
    			setOutput(outputFile);
    		}
    	}
    	return;
    }

    /**
     * @return folder to use as a grouping factor when no link command is found
     */
	public String getFolder() {
		return folder;
	}

    /**
     * @param folder
     *            folder to use as a grouping factor when no link command is found
     */
	public void setFolder(String folder) {
		this.folder = folder;
	}

    /**
     * @return command type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type
     *            command type
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return if the compile file is used in a link
     */
    public Boolean isLinked()
    {
        return linked;
    }

    /**
     * @param linked
     *            flag if the compile file is used in a link
     */
    public void setLinked(Boolean linked)
    {
        this.linked = linked;
    }
}
