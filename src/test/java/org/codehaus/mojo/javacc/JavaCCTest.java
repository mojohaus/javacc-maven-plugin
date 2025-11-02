package org.codehaus.mojo.javacc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests <code>JavaCC</code> facade.
 *
 * @author Benjamin Bentmann
 * @version $Id$
 */
class JavaCCTest {

    @Test
    void toStringNullSafe() {
        JavaCC tool = new JavaCC();
        String string = tool.toString();
        assertNotNull(string);
        assertFalse(string.contains("null"));
    }

    @Test
    void settersNullSafe() {
        JavaCC tool = new JavaCC();
        tool.setInputFile(null);
        tool.setOutputDirectory(null);
        tool.setJdkVersion(null);
        tool.setStatic(null);
        tool.setBuildParser(null);
        tool.setBuildTokenManager(null);
        tool.setCacheTokens(null);
        tool.setChoiceAmbiguityCheck(null);
        tool.setCommonTokenAction(null);
        tool.setDebugLookAhead(null);
        tool.setDebugParser(null);
        tool.setDebugTokenManager(null);
        tool.setErrorReporting(null);
        tool.setForceLaCheck(null);
        tool.setIgnoreCase(null);
        tool.setJavaUnicodeEscape(null);
        tool.setKeepLineColumn(null);
        tool.setLookAhead(null);
        tool.setOtherAmbiguityCheck(null);
        tool.setSanityCheck(null);
        tool.setTokenManagerUsesParser(null);
        tool.setUnicodeInput(null);
        tool.setUserCharStream(null);
        tool.setUserTokenManager(null);
        tool.setLog(null);
    }
}
