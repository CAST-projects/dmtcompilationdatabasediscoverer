package com.castsoftware.dmt.discoverer.cpp.compilationdatabase;

import org.junit.Rule;
import org.junit.Test;

import com.castsoftware.dmt.engine.discovery.IProjectsDiscoverer;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper.ProfileOrProjectTypeConfiguration;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper.ProfileOrProjectTypeConfiguration.LanguageConfiguration;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscoveryEngineTester;
import com.castsoftware.dmt.engine.project.Profile.ReferenceCollation;
import com.castsoftware.util.logger.Level;
import com.castsoftware.util.logger.LogRecorder;

/**
 * Tests for source files based projects discovery
 *
 */
public class CppCDUnitTest
{
    /** Per test log recorder and checker */
    @Rule
    public final LogRecorder logRecorder = new LogRecorder();

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
    public void unitTest_UT1() throws Throwable
    {
        logRecorder.setHighestAllowedLogLevel(Level.INFO);
        new CppCDUnitTestTester("UT1").go();
    }

    /**
     * Same test as UT1 with an extraction path on Windows which doesn't match with the directory in the json
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest_ERR1() throws Throwable
    {
        logRecorder.setHighestAllowedLogLevel(Level.ERROR);
        new CppCDUnitTestTester("ERR1").go();
        logRecorder.assertError("cast.dmt.discover.cpp.compilationdatabase.invalidRoot");
    }

    /**
     * Test discovery for arguments
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest_UT2() throws Throwable
    {
        new CppCDUnitTestTester("UT2").go();
    }

    /**
     * Test discovery for arguments c++
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest_UT3() throws Throwable
    {
        new CppCDUnitTestTester("UT3").go();
    }

    /**
     * Test discovery for arguments c++
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest_UT4() throws Throwable
    {
        new CppCDUnitTestTester("UT4").go();
    }

    /**
     * Test discovery for include path with .. in the middle
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest_UT5() throws Throwable
    {
        new CppCDUnitTestTester("UT5").go();
    }

    /**
     * Test discovery with AR link and files with command
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest_AR1() throws Throwable
    {
        new CppCDUnitTestTester("AR1").go();
    }

    /**
     * Test discovery with AR link and files with arguments
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest_AR2() throws Throwable
    {
        new CppCDUnitTestTester("AR2").go();
    }

    /**
     * Test discovery with link
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest_GPP1() throws Throwable
    {
        new CppCDUnitTestTester("GPP1").go();
    }

    /**
     * Test discovery with link
     * 
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest_HUAWEI() throws Throwable
    {
        new CppCDUnitTestTester("HUAWEI").go();
    }

    /**
     * Test discovery for ld command
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest_LD1() throws Throwable
    {
        logRecorder.setHighestAllowedLogLevel(Level.INFO);
        new CppCDUnitTestTester("LD1").go();
    }
}
