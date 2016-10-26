package com.siemens.ct.exi.javascript;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;

import com.siemens.ct.exi.exceptions.EXIException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static net.javacrumbs.jsonunit.JsonAssert.*;
import static net.javacrumbs.jsonunit.core.Option.*;

/**
 * Unit test for round-tripping JS (AST) files
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JSRoundtripTest extends TestCase {

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public JSRoundtripTest(String testName) {
		super(testName);
	}
	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(JSRoundtripTest.class);
	}

	public void testAnimals() throws IOException, EXIException {
		_test("./src/test/resources/animals.js");
	}

//	public void testBrowserDetection() throws IOException, EXIException {
//		_test("./src/test/resources/browserDetection.js");
//	}
//
//	public void testXCryptic() throws IOException, EXIException {
//		_test("./src/test/resources/xCryptic.app.js");
//	}
//
//	public void testJquery() throws IOException, EXIException {
//		_test("./src/test/resources/jquery.js");
//	}
//
//	public void testJqueryMin() throws IOException, EXIException {
//		_test("./src/test/resources/jquery.min.js");
//	}
//
//	public void testAngular2() throws IOException, EXIException {
//		_test("./src/test/resources/angular2.js");
//	}
//
//	public void testAngular2Min() throws IOException, EXIException {
//		_test("./src/test/resources/angular2.min.js");
//	}
//
//	public void testReact() throws IOException, EXIException {
//		_test("./src/test/resources/react.js");
//	}
//
//	public void testReactMin() throws IOException, EXIException {
//		_test("./src/test/resources/react.min.js");
//	}

	protected void _test(String js) throws IOException, EXIException {
		String jsCode = new String(Files.readAllBytes(Paths.get(js)));
		JStoEXI js2exi = new JStoEXI();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String jsonAST = JStoAST.getAST(jsCode);
		js2exi.generate(jsonAST, baos);

		
		// reconstruct JS AST
		EXItoJSAST exi2ast = new EXItoJSAST();
		ByteArrayOutputStream baosAST = new ByteArrayOutputStream();
		exi2ast.generate(new ByteArrayInputStream(baos.toByteArray()), baosAST);
		
		System.out.println("from: " + jsonAST);
		System.out.println("to: " + baosAST.toString());
		
		assertJsonEquals("{\"test\":[1,2,3]}", 
			    "{\"test\":[3,2,1]}",
			    when(IGNORING_ARRAY_ORDER));
		
		assertJsonEquals(jsonAST, baosAST.toString());
		
//		assertJsonEquals("{\"test\":[1,2,3]}",
//                "{\"test\":  [3,2,1]}"); // ,when(IGNORING_ARRAY_ORDER)
		
		// 	// TODO JSON comparison https://github.com/lukas-krecan/JsonUnit
		// http://stackoverflow.com/questions/2253750/compare-two-json-objects-in-java
	}
}
