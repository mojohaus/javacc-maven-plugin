package org.codehaus.mojo.javacc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file 
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied.  See the License for the 
 * specific language governing permissions and limitations 
 * under the License.
 */

import java.io.File;

import junit.framework.TestCase;

/**
 * Tests <code>UrlUtils</code>.
 * 
 * @author Benjamin Bentmann
 * @version $Id$
 */
public class UrlUtilsTest
    extends TestCase
{

    public void testGetResourceRootFile()
    {
        assertEquals( "C:/a dir".replace( '/', File.separatorChar ),
                      UrlUtils.getResourceRoot( "file:/C:/a%20dir/org/Foo.class", "/org/Foo.class" ) );
        assertEquals( "C:/a dir".replace( '/', File.separatorChar ),
                      UrlUtils.getResourceRoot( "file:/C:/a%20dir/org/Foo.class", "org/Foo.class" ) );
        assertEquals( "C:/a dir".replace( '/', File.separatorChar ),
                      UrlUtils.getResourceRoot( "file:/C:/a dir/org/Foo.class", "org/Foo.class" ) );
    }

    public void testGetResourceRootJarFile()
    {
        assertEquals( "C:/a dir/test-1.0.jar".replace( '/', File.separatorChar ),
                      UrlUtils.getResourceRoot( "jar:file:/C:/a%20dir/test-1.0.jar!/org/Foo.class", "/org/Foo.class" ) );
        assertEquals( "C:/a dir/test-1.0.jar".replace( '/', File.separatorChar ),
                      UrlUtils.getResourceRoot( "jar:file:/C:/a%20dir/test-1.0.jar!/org/Foo.class", "org/Foo.class" ) );
        assertEquals( "C:/a dir/test-1.0.jar".replace( '/', File.separatorChar ),
                      UrlUtils.getResourceRoot( "jar:file:/C:/a dir/test-1.0.jar!/org/Foo.class", "org/Foo.class" ) );
    }

    public void testGetResourceRootNullSafe()
    {
        assertNull( UrlUtils.getResourceRoot( null, "" ) );
    }

    public void testGetResourceRootUnknownProtocal()
    {
        assertNull( UrlUtils.getResourceRoot( "http://www.foo.bar/index.html", "index.html" ) );
    }

    public void testDecodeUrl()
    {
        assertEquals( "", UrlUtils.decodeUrl( "" ) );
        assertEquals( "foo", UrlUtils.decodeUrl( "foo" ) );
        assertEquals( "+", UrlUtils.decodeUrl( "+" ) );
        assertEquals( "% ", UrlUtils.decodeUrl( "%25%20" ) );
        assertEquals( "%20", UrlUtils.decodeUrl( "%2520" ) );
        assertEquals( "jar:file:/C:/dir/sub dir/1.0/foo-1.0.jar!/org/Bar.class",
                      UrlUtils.decodeUrl( "jar:file:/C:/dir/sub%20dir/1.0/foo-1.0.jar!/org/Bar.class" ) );
    }

    public void testDecodeUrlLenient()
    {
        assertEquals( " ", UrlUtils.decodeUrl( " " ) );
        assertEquals( "\u00E4\u00F6\u00FC\u00DF", UrlUtils.decodeUrl( "\u00E4\u00F6\u00FC\u00DF" ) );
        assertEquals( "%", UrlUtils.decodeUrl( "%" ) );
        assertEquals( "%2", UrlUtils.decodeUrl( "%2" ) );
        assertEquals( "%2G", UrlUtils.decodeUrl( "%2G" ) );
    }

    public void testDecodeUrlNullSafe()
    {
        assertNull( UrlUtils.decodeUrl( null ) );
    }

    public void testDecodeUrlEncodingUtf8()
    {
        assertEquals( "\u00E4\u00F6\u00FC\u00DF", UrlUtils.decodeUrl( "%C3%A4%C3%B6%C3%BC%C3%9F" ) );
    }

}
