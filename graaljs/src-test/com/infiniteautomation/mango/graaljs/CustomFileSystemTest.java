/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.graaljs;

import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.oracle.truffle.js.runtime.JSContextOptions;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.FileSystem;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class CustomFileSystemTest {

    @Test
    public void testLoadHttpCustomFsWithMapping() throws IOException {
        Path fileToLoad = Files.createTempFile("file-to-load", ".js");
        Files.write(fileToLoad, Collections.singletonList("foo = 41;"));

        FileSystem fs = new DelegateFileSystem(FileSystem.newDefaultFileSystem(),
                Collections.singletonMap("https://abc.example.org/xyz.js", fileToLoad));
        Path sourceFile = Files.createTempFile("load-http-test", ".js");
        Files.write(sourceFile, Collections.singletonList("load('https://abc.example.org/xyz.js'); foo;"));

        try (Context cx = Context.newBuilder(JavaScriptLanguage.ID)
                .allowExperimentalOptions(true)
                .allowIO(true)
                .fileSystem(fs)
                .option(JSContextOptions.LOAD_FROM_URL_NAME, "true").build()) {

            Value v = cx.eval(Source.newBuilder(JavaScriptLanguage.ID, sourceFile.toFile()).build());
            Assert.assertEquals(41, v.asInt());
        }
    }

    @Test
    @Ignore // https://github.com/graalvm/graaljs/issues/338
    public void testLoadHttpCustomFs() throws IOException {
        FileSystem fs = new DelegateFileSystem(FileSystem.newDefaultFileSystem(), Collections.emptyMap());
        Path sourceFile = Files.createTempFile("load-http-test", ".js");
        Files.write(sourceFile, Collections.singletonList("load('https://abc.example.org/xyz.js'); foo;"));

        try (Context cx = Context.newBuilder(JavaScriptLanguage.ID)
                .allowExperimentalOptions(true)
                .allowIO(true)
                .fileSystem(fs)
                .option(JSContextOptions.LOAD_FROM_URL_NAME, "true").build()) {

            Value v = cx.eval(Source.newBuilder(JavaScriptLanguage.ID, sourceFile.toFile()).build());
            Assert.assertEquals(41, v.asInt());
        } catch (PolyglotException e) {
            Assert.assertEquals("EvalError: abc.example.org", e.getMessage());
        }
    }

    @Test
    public void testLoadHttpDefaultFs() throws IOException {
        Path sourceFile = Files.createTempFile("load-http-test", ".js");
        Files.write(sourceFile, Collections.singletonList("load('https://abc.example.org/xyz.js'); foo;"));

        try (Context cx = Context.newBuilder(JavaScriptLanguage.ID)
                .allowExperimentalOptions(true)
                .allowIO(true)
                .option(JSContextOptions.LOAD_FROM_URL_NAME, "true").build()) {

            Value v = cx.eval(Source.newBuilder(JavaScriptLanguage.ID, sourceFile.toFile()).build());
            Assert.assertEquals(41, v.asInt());
        } catch (PolyglotException e) {
            Assert.assertEquals("EvalError: abc.example.org", e.getMessage());
        }
    }

    @Test
    public void testLoadHttpRealUrlDefaultFs() throws IOException {
        Path sourceFile = Files.createTempFile("load-http-test", ".js");
        Files.write(sourceFile, Collections.singletonList("module = {}; load('https://unpkg.com/is-number'); module.exports(1);"));

        try (Context cx = Context.newBuilder(JavaScriptLanguage.ID)
                .allowExperimentalOptions(true)
                .allowIO(true)
                .option(JSContextOptions.LOAD_FROM_URL_NAME, "true").build()) {

            Value v = cx.eval(Source.newBuilder(JavaScriptLanguage.ID, sourceFile.toFile()).build());
            Assert.assertTrue(v.asBoolean());
        }
    }

    @Test
    @Ignore // https://github.com/graalvm/graaljs/issues/338
    public void testLoadHttpRealUrlCustomFs() throws IOException {
        FileSystem fs = new DelegateFileSystem(FileSystem.newDefaultFileSystem(), Collections.emptyMap());
        Path sourceFile = Files.createTempFile("load-http-test", ".js");
        Files.write(sourceFile, Collections.singletonList("module = {}; load('https://unpkg.com/is-number'); module.exports(1);"));

        try (Context cx = Context.newBuilder(JavaScriptLanguage.ID)
                .allowExperimentalOptions(true)
                .allowIO(true)
                .fileSystem(fs)
                .option(JSContextOptions.LOAD_FROM_URL_NAME, "true").build()) {

            Value v = cx.eval(Source.newBuilder(JavaScriptLanguage.ID, sourceFile.toFile()).build());
            Assert.assertTrue(v.asBoolean());
        }
    }

    @Test
    @Ignore // https://github.com/graalvm/graaljs/issues/257
    public void testImportHttpCustomFsWithMapping() throws IOException {
        Path fileToImport = Files.createTempFile("file-to-import", ".mjs");
        Files.write(fileToImport, Collections.singletonList("export const foo = 42;"));

        FileSystem fs = new DelegateFileSystem(FileSystem.newDefaultFileSystem(),
                Collections.singletonMap("https://abc.example.org/xyz.mjs", fileToImport));
        Path sourceFile = Files.createTempFile("import-http-test", ".mjs");
        Files.write(sourceFile, Collections.singletonList("import {foo} from 'https://abc.example.org/xyz.mjs'; foo;"));

        try (Context cx = Context.newBuilder(JavaScriptLanguage.ID)
                .allowIO(true)
                .fileSystem(fs)
                .build()) {

            Value v = cx.eval(Source.newBuilder(JavaScriptLanguage.ID, sourceFile.toFile()).build());
            Assert.assertEquals(42, v.asInt());
        }
    }
}
