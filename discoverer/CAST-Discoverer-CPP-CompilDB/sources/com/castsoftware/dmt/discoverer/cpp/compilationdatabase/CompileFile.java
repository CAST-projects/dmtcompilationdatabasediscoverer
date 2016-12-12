package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompileFile extends Compile {

	CompileFile()
	{
		//NOP
	}
    public static class Macro
    {
    	private String key;
    	private String value;
    	Macro(String key, String value)
    	{
    		this.key = key;
    		this.setValue(value);
    	}
		public String getKey() {
			return key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
    }

	private String output;
	private int languageId = 0;
	private int languageHeaderId = 0;
	private List<String> includes = new ArrayList<String>();
	private List<Macro> macros = new ArrayList<Macro>();
	
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = getRelativePath(getDirectory(), output);
	}
	public int getLanguageId() {
		return languageId;
	}
	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}
	public int getLanguageHeaderId() {
		return languageHeaderId;
	}
	public void setLanguageHeaderId(int languageHeaderId) {
		this.languageHeaderId = languageHeaderId;
	}
	public List<String> getIncludes() {
		return includes;
	}
	public String getInclude(String include) {
		for (String i : includes)
		{
			if (include.equals(i))
				return i;
		}
		return null;
	}
	public void addInclude(String include) {
		this.includes.add(include);
	}
	public void setIncludes(List<String> includes) {
		this.includes = includes;
	}
	public List<Macro> getMacros() {
		return macros;
	}
	public Macro getMacro(String name) {
		for (Macro m : macros)
		{
			if (name.equals(m.getKey()))
				return m;
		}
		return null;
	}
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
    public void parseCommand(String command, int languageId, int languageHeaderId)
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
    		
    		addMacro(macro);
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
    		
    		addInclude(include);
    		//String includeRelativeRef = removeRelativePath(include);
    		//String directoryRef = buildPackageRelativePath(project, includeRelativeRef);
    		//if (project.getDirectoryReference(directoryRef) == null)
    		//	project.addDirectoryReference(directoryRef, languageId, languageHeaderId);
    		
	        pos1 = command.indexOf("-I",pos1 + 1);
    	}
    	pos1 = command.indexOf("-o");
    	if (pos1 > 0)
    	{
    		String output = "";
    		int pos2 = command.indexOf(".o", pos1);
    		if (pos2 > 0)
    		{
    			output = command.substring(pos1 + 3, pos2 + 2).trim();
    			setOutput(output);
    		}
    	}
    	return;
    }
}
