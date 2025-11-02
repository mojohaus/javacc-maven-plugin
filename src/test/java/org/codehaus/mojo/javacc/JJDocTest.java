package org.codehaus.mojo.javacc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests <code>JJDoc</code> facade.
 *
 * @author Benjamin Bentmann
 * @version $Id$
 */
class JJDocTest {

    @Test
    void toStringNullSafe() {
        JJDoc tool = new JJDoc();
        String string = tool.toString();
        assertNotNull(string);
        assertFalse(string.contains("null"));
    }

    @Test
    void settersNullSafe() {
        JJDoc tool = new JJDoc();
        tool.setInputFile(null);
        tool.setOutputFile(null);
        tool.setOneTable(null);
        tool.setText(null);
        tool.setLog(null);
    }
}
