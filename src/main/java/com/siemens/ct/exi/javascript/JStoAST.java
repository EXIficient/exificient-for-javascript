/*
 * Copyright (c) 2007-2018 Siemens AG
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */

package com.siemens.ct.exi.javascript;

import java.io.IOException;

import com.siemens.ct.exi.core.exceptions.EXIException;

import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.options.Options;

public class JStoAST {
	
	public static String getAST(String jsCode) throws IOException, EXIException {
		// http://sites.psu.edu/robertbcolton/2015/07/31/java-8-nashorn-script-engine/
		/*
		 * This JSON encoding provided by Nashorn is compliant with the
		 * community standard JavaScript JSON AST model popularized by Mozilla.
		 * https://developer.mozilla.org/en-US/docs/Mozilla/Projects/
		 * SpiderMonkey/Parser_API
		 */
		// Additionally the OpenJDK project is developing a public interface for
		// Java 9 that allows AST traversal in a more standard and user friendly
		// way.
		// String code = "function a() { var b = 5; } function c() { }";
		// String sin = "./data//jquery.min.js";
		// String sin = "./data/animals.js";
		// String code = new String(Files.readAllBytes(Paths.get(sin)));

		// ScriptEngine engine = new
		// ScriptEngineManager().getEngineByName("nashorn");
		// engine.eval("print('Hello World!');");
		// load jquery source file
		// engine.eval("load('" +
		// "https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js" +
		// "');");
		// engine.eval("load('" + sin + "');");

		Options options = new Options("nashorn");
		options.set("anon.functions", true);
		options.set("parse.only", true);
		options.set("scripting", true);

		ErrorManager errors = new ErrorManager();
		Context contextm = new Context(options, errors, Thread.currentThread().getContextClassLoader());
		Context.setGlobal(contextm.createGlobal());
		String jsonAST = ScriptUtils.parse(jsCode, "<unknown>", false);
		
		return jsonAST;
	}
}
