package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.util.ArrayList;
import java.util.List;

public class CompileLink extends Compile {

	CompileLink() {
		//NOP
	}
	private String linkname;
	private List<String> outputs = new ArrayList<String>();
	private List<CompileFile> compileFiles = new ArrayList<CompileFile>();
	
	public void setFilename(String filename) {
		super.setFilename(filename);
		setLinkname(filename);
	}
	public List<String> getOutputs() {
		return outputs;
	}
	public void addOutput(String output) {
		this.outputs.add(getRelativePath(getDirectory(), output));
	}
	public List<CompileFile> getCompileFiles() {
		return compileFiles;
	}
	public void setCompileFiles(List<CompileFile> compileFiles) {
		for (String output : outputs)
		{
			for (CompileFile cf : compileFiles)
			{
				if (output.equals(cf.getOutput()))
				{
					this.compileFiles.add(cf);
					break;
				}
			}
		}
	}
	public String getLinkname() {
		return linkname;
	}
	public void setLinkname(String filename) {
		int pos = filename.lastIndexOf("/");
		if (pos >= 0)
		{
			this.linkname = filename.substring(pos + 1, filename.lastIndexOf("."));
		}
		
	}
}
