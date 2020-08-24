/*
 * Copyright (C) 2020 Infinite Automation Systems Inc. All rights reserved.
 */

package com.infiniteautomation.mango.graaljs;

import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.oracle.truffle.js.runtime.JSContextOptions;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.FileSystem;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class CustomFileSystemTest {
    /**
     * https://github.com/graalvm/graaljs/issues/338
     * @throws IOException
     */
    @Test
    public void testLoadHttp() throws IOException {
        FileSystem fs = new DelegateFileSystem(FileSystem.newDefaultFileSystem());
        Path sourceFile = Files.createTempFile("load-http-test", ".js");
        Files.write(sourceFile, Collections.singletonList("load('https://abc:8443/xyz.js'); foo;"));

        try (Context cx = Context.newBuilder(JavaScriptLanguage.ID)
                .allowExperimentalOptions(true)
                .allowPolyglotAccess(PolyglotAccess.ALL)
                .allowIO(true)
                .fileSystem(fs)
                .option(JSContextOptions.LOAD_FROM_URL_NAME, "true").build()) {

            Value v = cx.eval(Source.newBuilder(JavaScriptLanguage.ID, sourceFile.toFile()).build());
        }
    }

    /**
     * https://github.com/graalvm/graaljs/issues/257
     * @throws IOException
     */
    @Test
    public void testImportHttp() throws IOException {
        FileSystem fs = new DelegateFileSystem(FileSystem.newDefaultFileSystem());
        Path sourceFile = Files.createTempFile("import-http-test", ".mjs");
        Files.write(sourceFile, Collections.singletonList("import {foo} from 'https://abc:8443/xyz.js'; foo;"));

        try (Context cx = Context.newBuilder(JavaScriptLanguage.ID)
                .allowExperimentalOptions(true)
                .allowPolyglotAccess(PolyglotAccess.ALL)
                .allowIO(true)
                .fileSystem(fs)
                .option(JSContextOptions.LOAD_FROM_URL_NAME, "true").build()) {

            Value v = cx.eval(Source.newBuilder(JavaScriptLanguage.ID, sourceFile.toFile()).build());
        }
    }
}
