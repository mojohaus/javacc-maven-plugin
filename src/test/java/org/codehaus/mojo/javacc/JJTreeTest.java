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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests <code>JJTree</code> facade.
 *
 * @author Benjamin Bentmann
 * @version $Id$
 */
class JJTreeTest {

    @Test
    void toStringNullSafe() {
        JJTree tool = new JJTree();
        String string = tool.toString();
        assertNotNull(string);
        assertFalse(string.contains("null"));
    }

    @Test
    void settersNullSafe() {
        JJTree tool = new JJTree();
        tool.setInputFile(null);
        tool.setOutputDirectory(null);
        tool.setJdkVersion(null);
        tool.setStatic(null);
        tool.setBuildNodeFiles(null);
        tool.setMulti(null);
        tool.setNodeDefaultVoid(null);
        tool.setNodeFactory(null);
        tool.setNodePackage(null);
        tool.setNodePrefix(null);
        tool.setNodeScopeHook(null);
        tool.setNodeUsesParser(null);
        tool.setVisitor(null);
        tool.setVisitorException(null);
        tool.setLog(null);
    }

    @Test
    void getOutputFile() {
        File input = new File("Test.jjt").getAbsoluteFile();
        File outdir = new File("dir").getAbsoluteFile();

        JJTree tool = new JJTree();
        tool.setInputFile(input);
        tool.setOutputDirectory(outdir);
        File output = tool.getOutputFile();

        assertEquals(new File(outdir, "Test.jj"), output);
    }

    /**
     * JJTree and JavaCC keep their state in static fields, so concurrent in-process runs of either tool, as a
     * parallel Maven build makes them, must not overlap.
     */
    @Test
    void concurrentRunsDoNotInterfere(@TempDir Path tempDir) throws Exception {
        int threads = 8;
        int runsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<String>> results = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                final int id = t;
                results.add(executor.submit((Callable<String>) () -> {
                    start.await();
                    String failure = "";
                    for (int r = 0; r < runsPerThread; r++) {
                        File out = Files.createDirectories(tempDir.resolve("out-" + id + "-" + r))
                                .toFile();
                        try {
                            if (id % 2 == 0) {
                                JJTree tool = new JJTree();
                                tool.setInputFile(Paths.get("src/test/resources/Simple.jjt")
                                        .toAbsolutePath()
                                        .toFile());
                                tool.setOutputDirectory(out);
                                tool.run();
                            } else {
                                JavaCC tool = new JavaCC();
                                tool.setInputFile(Paths.get("src/test/resources/Parser1.jj")
                                        .toAbsolutePath()
                                        .toFile());
                                tool.setOutputDirectory(out);
                                tool.run();
                            }
                        } catch (Exception e) {
                            failure = String.valueOf(e.getCause() != null ? e.getCause() : e);
                        }
                    }
                    return failure;
                }));
            }
            start.countDown();
            for (Future<String> result : results) {
                assertEquals("", result.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
