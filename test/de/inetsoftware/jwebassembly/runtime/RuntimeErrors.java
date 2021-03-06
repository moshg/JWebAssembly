/*
 * Copyright 2017 - 2020 Volker Berlin (i-net software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.inetsoftware.jwebassembly.runtime;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.inetsoftware.jwebassembly.ScriptEngine;
import de.inetsoftware.jwebassembly.WasmException;
import de.inetsoftware.jwebassembly.WasmRule;
import de.inetsoftware.jwebassembly.api.annotation.Export;
import de.inetsoftware.jwebassembly.api.annotation.Import;

/**
 * @author Volker Berlin
 */
@RunWith( Parameterized.class )
public class RuntimeErrors {

    private final ScriptEngine script;

    public RuntimeErrors( ScriptEngine script ) {
        this.script = script;
    }

    @Parameters( name = "{0}" )
    public static Collection<ScriptEngine[]> data() {
        return ScriptEngine.testParams();
    }

    private void compileErrorTest( String expectedMessge, Class<?> classes ) throws IOException {
        WasmRule wasm = new WasmRule( classes );
        try {
            wasm.compile();
            fail( "Exception expected with: " + expectedMessge );
        } catch( WasmException ex ) {
            assertTrue( "Wrong error message: " + ex.getMessage(), ex.getMessage().contains( expectedMessge ) );
        } finally {
            wasm.delete();
        }

    }

    @Test
    public void nonStaticExport() throws IOException {
        compileErrorTest( "Export method must be static:", NonStaticExport.class );
    }

    static class NonStaticExport {
        @Export
        float function() {
            return 1;
        }
    }

    @Test
    public void nonStaticImport() throws IOException {
        compileErrorTest( "Import method must be static:", NonStaticImport.class );
    }

    static class NonStaticImport {
        @Import( module = "m", name = "n" )
        float function() {
            return 1;
        }
    }

    @Test
    public void nativeMethod() throws IOException {
        compileErrorTest( "Abstract or native method can not be used:", NativeMethod.class );
    }

    static class NativeMethod {
        @Export
        native static float function();
    }

    @Test
    public void lambdas() throws IOException {
        compileErrorTest( "InvokeDynamic/Lambda is not supported.", LambdaMethod.class );
    }

    static class LambdaMethod {
        private static int counter;

        @Export
        static void runnable() {
            Runnable run = () -> counter++;
            run.run();
        }
    }

    @Test
    public void interfaceCall() throws IOException {
        compileErrorTest( "Interface calls are not supported.", InterfaceMethod.class );
    }

    static class InterfaceMethod {
        @Export
        static int runnable() {
            List list = new ArrayList();
            return list.size();
        }
    }

    @Test
    public void classConatnt() throws IOException {
        compileErrorTest( "Class constants are not supported.", ClassConstant.class );
    }

    static class ClassConstant {
        @Export
        static Object runnable() {
            Class elemtentType = Integer.class;
            return java.lang.reflect.Array.newInstance(elemtentType, 42);
        }
    }
}
