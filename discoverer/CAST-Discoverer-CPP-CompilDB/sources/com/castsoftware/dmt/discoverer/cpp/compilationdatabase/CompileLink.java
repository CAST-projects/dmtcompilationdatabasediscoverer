package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * compile used to link compile files
 */
public class CompileLink extends Compile {

    CompileLink(String command)
    {
        this.command = command;
	}

    private String command;
	private String linkname;
    private final List<String> outputs = new ArrayList<String>();
    private final List<CompileFile> compileFiles = new ArrayList<CompileFile>();

	@Override
    public void setFilename(String filename) {
		super.setFilename(filename);
		setLinkname(filename);
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
     * @return the list of outputs
     */
	public List<String> getOutputs() {
		return outputs;
	}

    /**
     * @param output
     *            the output to add to the list
     */
	public void addOutput(String output) {
        outputs.add(getRelativePath(getDirectory(), output));
        //outputs.add(output);
	}

    /**
     * @return list of compile files
     */
	public List<CompileFile> getCompileFiles() {
		return compileFiles;
	}

    /**
     * @param compileFiles
     *            list of compile files to link
     */
	public void setCompileFiles(List<CompileFile> compileFiles) {
		for (String output : outputs)
		{
			for (CompileFile cf : compileFiles)
			{
				if (output.equals(cf.getOutput()))
				{
					this.compileFiles.add(cf);
                    cf.setLinked(true);
					break;
				}
			}
		}
	}

    /**
     * @return name of the link
     */
	public String getLinkname() {
		return linkname;
	}

    /**
     * @param filename
     *            name of the link extracted from the filename
     */
	public void setLinkname(String filename) {
		int pos = filename.lastIndexOf("/");
		if (pos >= 0)
		{
			//this.linkname = filename.substring(pos + 1, filename.lastIndexOf("."));
			linkname = filename.substring(pos + 1);
		}
		else
			//this.linkname = filename.substring(0, filename.lastIndexOf("."));
			linkname = filename;

	}
}
