package org.codehaus.mojo.javacc;

import junit.framework.TestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;

/**
 * Test JavaCCUtil
 * 
 * @author pgier
 * 
 */
public class JavaCCUtilTest
    extends TestCase
{
    public void testGGetDeclaredPackage ()
        throws Exception
    {
        
        File testFile = ResourceExtractor.simpleExtractResources( getClass(), "/BasicParser.jj" );
        String packageName = JavaCCUtil.getDeclaredPackage( testFile );
        assertEquals( "The package name is not correct", "org/codehaus/mojo/javacc/test", packageName );
        
    }
}
