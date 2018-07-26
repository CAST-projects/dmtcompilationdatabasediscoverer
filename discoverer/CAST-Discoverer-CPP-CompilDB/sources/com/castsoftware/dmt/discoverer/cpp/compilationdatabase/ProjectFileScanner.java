package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.castsoftware.dmt.engine.project.Project;
import com.castsoftware.util.logger.Logging;

/**
 * Scanner for json file
 */
public class ProjectFileScanner
{
	static String sep = "";

    enum CCompileCommands
    {
		CC("cc")
		,GCC("gcc");
		private String name = "";

        CCompileCommands(String name)
        {
			this.name = name;
		}
		@Override
        public String toString(){
			return name;
		}

        public static String getConfigName()
        {
            return "cc";
        }
    }

    enum CPPCompileCommands
    {
        CPP("c++")
        ,
        GPP("g++");
        private String name = "";

        CPPCompileCommands(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }

        public static String getConfigName()
        {
            return "cxx";
        }
    }

    enum LinkCommands
    {
        AR("ar")
        ,
        LD("ld");
        private String name = "";

        LinkCommands(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
    // static int cLanguageId, cHeaderLanguage, cPlusPlusLanguage, cPlusPlusHeaderLanguage, cFamilyNotCompilableLanguage;

    private ProjectFileScanner()
    {
        // NOP
    }

    /**
     * Scan a compile_config.json file and add info to the project.
     *
     * @param config
     *            the config options to load in this file
     * @param configFilePath
     *            the path to the compile_config.json file
     * @param configContent
     *            the file content to scan.
     * @return {@code true} if no error was encountered during scanning. {@code false} otherwise.
     */
    public static CompileConfig scanConfig(String config, String configFilePath, String configContent)
    {
    	BufferedReader reader = null;
        CompileConfig compileConfig = null;

        try
        {
            reader = new BufferedReader(new StringReader(configContent), configContent.length());

            String firstLine = reader.readLine().trim();
        	if ((firstLine != null) && ("{".equals(firstLine)))
        	{
                int numline = 1;
        		Boolean isInside = true;
                // identify the bloc config { }
                Boolean isConfig = false;
                // identify the required config
                Boolean isCommandConfig = false;

                // identify the bloc include_paths [ ]
                Boolean isInIncludePaths = false;
                Boolean isInDefines = false;
                String defineName = null;
                String defineValue = null;
	            for (String readline = reader.readLine(); readline != null; readline = reader.readLine())
	            {
	            	String line = readline.trim();
                    numline++;
	            	if (isInside)
	            	{
                        if (line.contains("}"))
                        {
                            if (isInDefines)
                            {
                                if (isCommandConfig && defineName != null && defineValue != null)
                                    compileConfig.addDefine(defineName, defineValue);
                                defineName = null;
                                defineValue = null;
                            }
                            else if (isConfig)
                                isConfig = false;
                            else if (isInside)
                                isInside = false;
                            else
                                // out the main bloc
                                Logging.detail("cast.dmt.discover.cpp.compilationdatabase.lineoutside", "LINE",
                                    String.valueOf(numline), "FILE", configFilePath);
                            continue;
                        }
                        else if (isConfig && line.startsWith("]"))
		            	{
                            if (isInIncludePaths)
                                isInIncludePaths = false;
                            else if (isInDefines)
                                isInDefines = false;
                            else
                            {
                                isInIncludePaths = false;
                                isInDefines = false;
                            }
                            continue;
		            	}
		            	else if (line.contains(": {"))
		            	{
		            		isConfig = true;
		            		String configId = line.substring(line.indexOf("\"") + 1).trim();
		            		configId = configId.substring(0, configId.indexOf("\""));
		            		if (configId.equals(config))
                            {
		            			isCommandConfig = true;
                                compileConfig = new CompileConfig();
                            }
		            		else
                                // if (command.equals(ArgumentTypes.CPP.toString()) && "cxx".equals(config))
                                // isCommandConfig = true;
                                // else
		            				isCommandConfig = false;
		            	}
		            	else if (isConfig && line.contains("}"))
		            	{
                            if (isInDefines)
                            {
                                if (isCommandConfig && defineName != null && defineValue != null)
                                    compileConfig.addDefine(defineName, defineValue);
                                defineName = null;
                                defineValue = null;
                            }
                            else
                                isConfig = false;
		            	}
                        else if (isConfig && line.startsWith("\"include_paths\""))
		            	{
		            		isInIncludePaths = true;
		            	}
                        else if (isConfig && line.startsWith("\"defines\""))
                        {
                            isInDefines = true;
                        }
                        else
                        {
                            if (isCommandConfig)
                            {
                                if (isInIncludePaths)
                                {
                                    String include = line.substring(line.indexOf("\"") + 1).trim();
                                    include = PathHelper.removeRelativePath(include.substring(0, include.indexOf("\"")).trim());
                                    compileConfig.addInclude_path(include);
                                }
                                else if (isInDefines)
                                {
                                    if (line.startsWith("\"name\":"))
                                    {
                                        String val = line.substring(line.indexOf("\"", 7) + 1).trim();
                                        defineName = val.substring(0, val.length() - 2);
                                    }
                                    else if (line.startsWith("\"value\":"))
                                    {
                                        String val = line.substring(line.indexOf("\"", 8) + 1).trim();
                                        defineValue = val.substring(0, val.length() - 1);
                                        if (defineValue.startsWith("\\\""))
                                        	defineValue = defineValue.substring(2, defineValue.length() - 2);
                                    }
                                }
                            }
                        }
	            	}
	            }
        	}
        	else
        	{
                Logging.warn("cast.dmt.discover.cpp.compilationdatabase.notJsonFormat", "PATH", configFilePath);
        	}
        }
        catch (IllegalArgumentException e)
        {
            Logging.managedError(e, "cast.dmt.discover.cpp.compilationdatabase.ioExceptionInConfigParsing", "PATH",
                configFilePath);
        }
        catch (IOException e)
        {
            Logging.managedError(e, "cast.dmt.discover.cpp.compilationdatabase.ioExceptionInConfigParsing", "PATH",
                configFilePath);
        }
        finally
        {
        	try {
				reader.close();
			} catch (IOException e) {
                Logging.managedError(e, "cast.dmt.discover.cpp.compilationdatabase.ioExceptionInConfigParsing", "PATH",
                    configFilePath);
			}
        }
        return compileConfig;
    }

    /**
     * Scan a compile_commands.json file and add info to the project.
     *
     * @param connectionPath
     *            the project file interpreter
     * @param relativeFilePath
     *            the path to the project file used for reference
     * @param project
     *            the file content to scan.
     * @param projectContent
     *            the file content to scan.
     * @param compileFiles
     *            the list of compileFile found in the file.
     * @param compileLinks
     *            the list of compileLink found in the file.
     * @return {@code true} if no error was encountered during scanning. {@code false} otherwise.
     */
    public static Boolean scan(String connectionPath, String relativeFilePath, Project project, String projectContent,
        List<CompileFile> compileFiles, List<CompileLink> compileLinks)
    {
    	Boolean scanFailed = false;
    	BufferedReader reader = null;
    	String command = null;
    	Boolean isArguments = false;
    	Boolean isInArguments = false;
    	Boolean isNextOutput = false;
    	Boolean isNextInclude = false;
    	String includeType = null;
    	Boolean isArgumentC = false;
        String directory = null;
    	String file = null;
    	String output = null;
        List<String> outputs = null;
        CompileFile compileFile = null;
        CompileLink compileLink = null;

    	setSeparator(connectionPath);

    	reader = new BufferedReader(new StringReader(projectContent), projectContent.length());

        try
        {
        	String firstLine = reader.readLine().trim();
        	if ((firstLine != null) && ("[".equals(firstLine)))
        	{
            	Boolean isInside = true;
            	int numline = 1;
	            for (String readline = reader.readLine(); readline != null; readline = reader.readLine())
	            {
	            	String line = readline.trim();
	            	numline++;
	            	if (isInside)
	            	{
		            	if ("{".equals(line))
		            	{
		            		// new
		            		command = null;
		            		isArguments = false;
		            		isInArguments = false;
		            		isNextOutput = false;
		            		isNextInclude = false;
		            		includeType = null;
		            		isArgumentC = false;
		            		output = null;
		            		outputs = new ArrayList<String>();
		            		directory = null;
		            		file = null;
		            		compileFile = null;
		            		compileLink = null;
		            	}
		            	else if (line.startsWith("}"))
		            	{
                            if (isArguments == true)
		            		{
                                if (compileLink != null)
                                {
			            			compileLink.setLinkname(file);
			            			compileLink.setDirectory(directory);
			            			compileLink.setFilename(file);
                                    for (String s : outputs)
                                        compileLink.addOutput(s);
			            			compileLinks.add(compileLink);
                                    compileLink = null;
			            		}
                                else if (compileFile != null)
                                {
			                        compileFile.setDirectory(directory);
			                        compileFile.setFilename(file);

                                    if (output == null)
                                    	if (isArgumentC)
                                    	{
                                    		int i = 1;
                                    		//output = directory + "/" + getFilename(file) + ".o";
                                    		compileFile.setOutput(getFilename(file) + ".o");
                                    	}
                                    	else
                                    		Logging.warn("cast.dmt.discover.cpp.compilationdatabase.missingOutput", "FILE",
                                    				compileFile.getFilename());
                                    else
                                        compileFile.setOutput(output);

			                		compileFiles.add(compileFile);
                                    compileFile = null;
			            		}
			            		else
                                    Logging.warn("cast.dmt.discover.cpp.compilationdatabase.invalidCommand", "LINE",
                                        String.valueOf(numline), "FILE", connectionPath);
		            		}
                            else if (command != null)
                            {
                                if (compileFile != null)
                                {
                                    compileFile.setDirectory(directory);
                                    compileFile.setFilename(file);
                                    compileFile.parseCommand(command);

                                    compileFiles.add(compileFile);
                                    compileFile = null;
                                }
                                else
                                    Logging.warn("cast.dmt.discover.cpp.compilationdatabase.notSupportedCommand", "PATH",
                                        connectionPath, "LINE", String.valueOf(numline));
                            }
                            outputs = null;
		            	}
		            	else if (line.startsWith("\"arguments\": ["))
		            	{
		            		isArguments = true;
		            		isInArguments = true;
		            		isNextOutput = false;
		            		isNextInclude = false;
		            		includeType = null;
		            	}
		            	else if (line.startsWith("]"))
		            	{
		            		if (line.equals("]"))
		            			isInside = false;
                            else if (isInArguments)
		            		{
			            		isInArguments = false;
			            		isNextOutput = false;
			            		isNextInclude = false;
			            		includeType = null;
		            		}
		            	}
		            	else if (isInArguments)
		            	{
                            if (compileFile != null)
                            {
                            	if (line.startsWith("\"-c"))
                            		isArgumentC = true;
                            	else if (line.startsWith("\"-D"))
                                {
                                    compileFile.addMacro(line.substring(3, line.indexOf("\"", 2)));
                                }
                                else if (line.startsWith("\"-I"))
                                {
                                	compileFile.addInclude(line.substring(3, line.indexOf("\"", 2)), "I");
                                }
                                else if (line.startsWith("\"-isystem"))
                                {
                                	isNextInclude = true;
                                	includeType = "system";
                                }
                                else if (line.startsWith("\"-iquote"))
                                {
                                	isNextInclude = true;
                                	includeType = "quote";
                                }
                                else if (line.startsWith("\"-idirafter"))
                                {
                                	isNextInclude = true;
                                	includeType = "dirafter";
                                }
                                else if (isNextInclude)
                                {
                                	compileFile.addInclude(line.substring(1, line.indexOf("\"", 2)), includeType);
                                	isNextInclude = false;
                                	includeType = null;
                                }
                                else if (line.startsWith("\"-o"))
                                {
                                    isNextOutput = true;
                                }
                                else if (isNextOutput)
                                {
                                    output = line.substring(1, line.indexOf("\"", 2));
                                    isNextOutput = false;
                                    // to set the output, wait for the directory
                                }
                            }
                            else if (compileLink != null)
                            {
                                if (line.contains(".o"))
                                    outputs.add(line.substring(1, line.indexOf("\"", 2)));
                            }
                            else
                            {
                                if (line.equals("\"" + CCompileCommands.CC.toString() + "\",")
                                    || line.endsWith("/" + CCompileCommands.CC.toString() + "\","))
                                {
                                    compileFile = new CompileFile(CCompileCommands.CC.toString(),
                                        CCompileCommands.getConfigName());
                                }
                                else if (line.equals("\"" + CCompileCommands.GCC.toString() + "\",")
                                         || line.endsWith("/" + CCompileCommands.GCC.toString() + "\","))
                                {
                                    compileFile = new CompileFile(CCompileCommands.GCC.toString(),
                                        CCompileCommands.getConfigName());
                                }
                                else if (line.equals("\"" + CPPCompileCommands.CPP.toString() + "\",")
                                         || line.endsWith("/" + CPPCompileCommands.CPP.toString() + "\","))
                                {
                                    compileFile = new CompileFile(CPPCompileCommands.CPP.toString(),
                                        CPPCompileCommands.getConfigName());
                                }
                                else if (line.equals("\"" + CPPCompileCommands.GPP.toString() + "\",")
                                        || line.endsWith("/" + CPPCompileCommands.GPP.toString() + "\","))
                               {
                                   compileFile = new CompileFile(CPPCompileCommands.GPP.toString(),
                                       CPPCompileCommands.getConfigName());
                               }
                                else if (line.equals("\"" + LinkCommands.AR.toString() + "\",")
                                         || line.endsWith("/" + LinkCommands.AR.toString() + "\","))
                                {
                                    compileLink = new CompileLink(LinkCommands.AR.toString());
                                }
                                else if (line.equals("\"" + LinkCommands.LD.toString() + "\",")
                                         || line.endsWith("/" + LinkCommands.LD.toString() + "\","))
                                {
                                    compileLink = new CompileLink(LinkCommands.LD.toString());
                                }
                            }
		            	}
		            	else if (line.startsWith("\"command\":"))
		            	{
		            		String val = line.substring(line.indexOf(":") + 1).trim();
		            		command = val.substring(val.indexOf("\"") + 1, val.lastIndexOf("\""));
		            		int pos = command.indexOf(" ");
		            		if (pos > 0)
		            		{
                                String commandType = command.substring(0, pos);
                                if (commandType.equals(CCompileCommands.CC.toString())
                                    || commandType.endsWith("/" + CCompileCommands.CC.toString()))
                                {
                                    compileFile = new CompileFile(CCompileCommands.CC.toString(),
                                        CCompileCommands.getConfigName());
                                }
                                else if (commandType.equals(CCompileCommands.GCC.toString())
                                         || commandType.endsWith("/" + CCompileCommands.GCC.toString()))
                                {
                                    compileFile = new CompileFile(CCompileCommands.GCC.toString(),
                                        CCompileCommands.getConfigName());
                                }
                                else if (commandType.equals(CPPCompileCommands.CPP.toString())
                                         || commandType.endsWith("/" + CPPCompileCommands.CPP.toString()))
                                {
                                    compileFile = new CompileFile(CPPCompileCommands.CPP.toString(),
                                        CPPCompileCommands.getConfigName());
                                }
                                else if (commandType.equals(CPPCompileCommands.GPP.toString())
                                         || commandType.endsWith("/" + CPPCompileCommands.GPP.toString()))
                                {
                                    compileFile = new CompileFile(CPPCompileCommands.GPP.toString(),
                                        CPPCompileCommands.getConfigName());
                                }
                                else if (commandType.equals(LinkCommands.AR.toString())
                                         || commandType.endsWith("/" + LinkCommands.AR.toString()))
                                {
                                    compileLink = new CompileLink(LinkCommands.AR.toString());
                                }
                                else if (commandType.equals(LinkCommands.LD.toString())
                                         || commandType.endsWith("/" + LinkCommands.LD.toString()))
                                {
                                    compileLink = new CompileLink(LinkCommands.LD.toString());
                                }
                                else
                                {
                                    //
                                    Logging.warn("cast.dmt.discover.cpp.compilationdatabase.notSupportedCommand", "PATH",
                                        project.getPath() + "/compile_commands.json", "LINE", numline);
                                }
		            		}
		            		else
                                Logging.warn("cast.dmt.discover.cpp.compilationdatabase.notSupportedCommand", "PATH",
                                    project.getPath() + "/compile_commands.json", "LINE", numline);
		            	}
		            	else if (line.startsWith("\"directory\":"))
		            	{
		            		String name = line.substring(line.indexOf(":") + 1).trim();
		            		directory = name.substring(name.indexOf("\"") + 1, name.lastIndexOf("\""));

		            		if (!directory.startsWith(connectionPath))
		            		{
                                Logging.managedError("cast.dmt.discover.cpp.compilationdatabase.invalidRoot", "DIR", directory, "ROOT",
                                    connectionPath);
                                scanFailed = true;
                                break;
		            		}
		            	}
		            	else if (line.startsWith("\"file\":"))
		            	{
		            		file = line.substring(line.indexOf(":") + 1).trim();
		            		file = file.substring(file.indexOf("\"") + 1, file.lastIndexOf("\""));
		            	}
		            }

	            }
        	}
        	else
        	{
        		Logging.warn("cast.dmt.discover.cpp.compilationdatabase.notJsonFormat", "PATH", project.getPath() + "/compile_commands.json");
                scanFailed = true;
        	}
        }
        catch (IOException e)
        {
        	scanFailed = true;
            Logging.managedError(e, "cast.dmt.discover.cpp.compilationdatabase.ioExceptionInProjectParsing", "PATH", relativeFilePath);
        }
        finally
        {
        	try {
				reader.close();
			} catch (IOException e) {
				Logging.managedError(e, "cast.dmt.discover.cpp.compilationdatabase.ioExceptionInProjectParsing", "PATH", relativeFilePath);
			}
        }
        return scanFailed;
    }
    private static String getFilename(String filename)
    {
    	String shortFilenameString;
    	int pos = filename.lastIndexOf("/");
    	if (pos > 0)
    		shortFilenameString = filename.substring(pos + 1);
    	else
    	{
    		pos = filename.lastIndexOf("\\");
    		if (pos > 0)
    			shortFilenameString = filename.substring(pos + 1);
    		else
    			shortFilenameString = filename;
    	}
    	pos = shortFilenameString.lastIndexOf(".");
    	if (pos > 0)
    		shortFilenameString = shortFilenameString.substring(0, pos);
    	return shortFilenameString;
    }

    private static void setSeparator(String rootPath)
    {
        if (rootPath.startsWith("/"))
            sep = "/";
        else
            sep = "\\";
    }
}

