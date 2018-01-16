package com.siemens.ct.exi.javascript;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.siemens.ct.exi.core.exceptions.EXIException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for sample JS files
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JStoEXITest extends TestCase {

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public JStoEXITest(String testName) {
		super(testName);
	}

	PrintStream ps = System.out;

	private static boolean setUpIsDone = false;

	public void setUp() {
		// executed only once, before the first test
		if (!setUpIsDone) {
			ps.println(System.getProperty("java.version"));
			ps.println("JS-Name; JS; EXI;");
			setUpIsDone = true;
		}
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(JStoEXITest.class);
	}

	public void testAnimals() throws IOException, EXIException {
		_test("./src/test/resources/animals.js");
	}

	public void testBrowserDetection() throws IOException, EXIException {
		_test("./src/test/resources/browserDetection.js");
	}

	public void testXCryptic() throws IOException, EXIException {
		_test("./src/test/resources/xCryptic.app.js");
	}

	public void testJquery() throws IOException, EXIException {
		_test("./src/test/resources/jquery.js");
	}

	public void testJqueryMin() throws IOException, EXIException {
		_test("./src/test/resources/jquery.min.js");
	}

	public void testAngular2() throws IOException, EXIException {
		_test("./src/test/resources/angular2.js");
	}

	public void testAngular2Min() throws IOException, EXIException {
		_test("./src/test/resources/angular2.min.js");
	}

	public void testReact() throws IOException, EXIException {
		_test("./src/test/resources/react.js");
	}

	public void testReactMin() throws IOException, EXIException {
		_test("./src/test/resources/react.min.js");
	}

	protected void _test(String js) throws IOException, EXIException {
		String jsCode = new String(Files.readAllBytes(Paths.get(js)));
		JStoEXI js2exi = new JStoEXI();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		js2exi.generate(JStoAST.getAST(jsCode), baos);

		System.out.println(js + "; " + jsCode.length() + "; " + baos.size() + ";");
	}
}
