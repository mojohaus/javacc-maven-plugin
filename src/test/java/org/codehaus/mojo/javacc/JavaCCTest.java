package org.codehaus.mojo.javacc;

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

    /**
     * JavaCC keeps its state in static fields, so concurrent runs, as a parallel Maven build makes them, must not
     * overlap.
     */
    @Test
    void concurrentRunsDoNotInterfere(@TempDir Path tempDir) throws Exception {
        int threads = 8;
        int runsPerThread = 5;
        String[] grammars = {"Parser1.jj", "Parser2.jj"};
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
                        JavaCC tool = new JavaCC();
                        tool.setInputFile(Paths.get("src/test/resources", grammars[id % grammars.length])
                                .toAbsolutePath()
                                .toFile());
                        tool.setOutputDirectory(out);
                        try {
                            tool.run();
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
