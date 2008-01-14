package org.codehaus.mojo.javacc;

import java.io.File;

import junit.framework.TestCase;

/**
 * Test JavaCCUtil
 * 
 * @author pgier
 * 
 */
public class JavaCCUtilTest
    extends TestCase
{
    public void testGetDeclaredPackage ()
        throws Exception
    {
        File testFile = new File(getClass().getResource( "/BasicParser.jj" ).getFile());
        String packageName = JavaCCUtil.getDeclaredPackage( testFile );
        packageName = packageName.replace( File.separatorChar, '.' );
        assertEquals( "The package name is not correct", "org.codehaus.mojo.javacc.test", packageName );
    }
}
