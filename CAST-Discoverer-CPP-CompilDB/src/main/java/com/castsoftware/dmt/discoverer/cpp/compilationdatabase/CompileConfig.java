package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * common compilation instructions provided in the compile_config.json file
 */
public class CompileConfig
{
    private final List<String> include_paths = new ArrayList<String>();
    private final Map<String, String> defines = new HashMap<String, String>();

    /**
     * @return list of include path
     */
    public List<String> getInclude_paths()
    {
        return include_paths;
    }

    /**
     * @param include_path
     *            path to add in the include
     */
    public void addInclude_path(String include_path)
    {
        include_paths.add(include_path);
    }

    /**
     * @return list of define
     */
    public Map<String, String> getDefines()
    {
        return defines;
    }

    /**
     * @param name
     *            define name
     * @param value
     *            define value
     */
    public void addDefine(String name, String value)
    {
        defines.put(name, value);
    }
}
