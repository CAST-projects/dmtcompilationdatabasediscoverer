package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import org.junit.Test;

import com.castsoftware.dmt.engine.discovery.IProjectsDiscoverer;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper.ProfileOrProjectTypeConfiguration;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper.ProfileOrProjectTypeConfiguration.LanguageConfiguration;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscoveryEngineTester;
import com.castsoftware.dmt.engine.project.Profile.ReferenceCollation;

/**
 * Tests for source files based projects discovery
 *
 */
public class CppCDUnitTest
{

    private static class CppCDUnitTestTester extends ProjectsDiscoveryEngineTester
    {
        CppCDUnitTestTester(String desc)
        {
            super(CppCDUnitTest.class, desc);
        }

        @Override
        protected IProjectsDiscoverer createTestDiscoverer()
        {
            return new CppCompilDBDiscoverer();
        }

        @Override
        protected void configureTestdiscoverer(ProjectsDiscovererWrapper discovererWrapper)
        {
            ProfileOrProjectTypeConfiguration projectTypeConfiguration = discovererWrapper.addProjectTypeConfiguration(
                "dmtdevmicrosofttechno.CppProject", "compile_commands.json", ReferenceCollation.WindowsNTFS, null);
            LanguageConfiguration cLanguage = projectTypeConfiguration.addLanguageConfiguration("CLanguage",
                "*.c;*.pc;*.ppc", ReferenceCollation.WindowsNTFS);
            cLanguage.addResourceTypeConfiguration("CHeaderLanguage", "*.h;*.ph", ReferenceCollation.WindowsNTFS, null,
                ReferenceCollation.WindowsNTFS);
            LanguageConfiguration cppLanguage = projectTypeConfiguration.addLanguageConfiguration(
                "CPlusPlusLanguage", "*.cpp;*.cc;*.cxx", ReferenceCollation.WindowsNTFS);
            cppLanguage.addResourceTypeConfiguration("CPlusPlusHeaderLanguage", "*.h", ReferenceCollation.WindowsNTFS,
                null, ReferenceCollation.WindowsNTFS);
            projectTypeConfiguration.addLanguageConfiguration("CFamilyNotCompilableLanguage",
                null, ReferenceCollation.WindowsNTFS);

            // projectOrigin
            discovererWrapper.configure("C/C++ Compilation Database project");
        }
    }

    /**
     * Test discovery for cc command
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest1() throws Throwable
    {
        new CppCDUnitTestTester("UT1").go();
    }

    /**
     * Test discovery for arguments
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest2() throws Throwable
    {
        new CppCDUnitTestTester("UT2").go();
    }
}
