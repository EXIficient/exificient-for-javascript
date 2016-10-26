package com.siemens.ct.exi.javascript;

import java.io.IOException;

import com.siemens.ct.exi.exceptions.EXIException;

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
