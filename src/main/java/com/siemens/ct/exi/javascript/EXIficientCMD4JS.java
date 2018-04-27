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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.siemens.ct.exi.core.CodingMode;
import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.exceptions.EXIException;
import com.siemens.ct.exi.main.cmd.CmdOption;

public class EXIficientCMD4JS {

	static final PrintStream ps = System.out;

	public static final String HELP = "-h";

	public static String ENCODE = "-encode";
	public static String DECODE = "-decode";
	
	public static final String INPUT = "-i";
	public static final String OUTPUT = "-o";

	public static final String CODING_BYTEPACKED = "-bytePacked";
	public static final String CODING_PRE_COMPRESSION = "-preCompression";
	public static final String CODING_COMPRESSION = "-compression";

	EXIFactory exiFactory;
	boolean inputParametersOK;
	CmdOption cmdOption;
	String input;
	String output;
	String outputXML;

	private static void printHeader() {
		ps.println("#########################################################################");
		ps.println("###   EXIficient for JavaScript                                      ###");
		ps.println("###   Command-Shell Options                                          ###");
		ps.println("#########################################################################");
	}

	private static void printHelp() {
		printHeader();

		ps.println();
		ps.println(" " + HELP
				+ "                               /* shows help */");
		ps.println();
		ps.println(" " + ENCODE);
		ps.println(" " + DECODE);
		ps.println();
		ps.println();
		ps.println(" " + INPUT + " <input-file>");
//		ps.println(" " + OUTPUT + " <output-file>");
		ps.println();
		ps.println(" " + CODING_BYTEPACKED);
		ps.println(" " + CODING_PRE_COMPRESSION);
		ps.println(" " + CODING_COMPRESSION);
		ps.println();
		ps.println("# Examples");
		ps.println(" " + ENCODE + " " + INPUT + " sample.js");
		ps.println(" " + DECODE + " " + INPUT + " sample.js.xml.exi");
	}

	protected static void printError(String msg) {
		ps.println("[ERROR] " + msg);
	}

	protected void parseArguments(String[] args) throws EXIException {
		// arguments that need to be set
		cmdOption = null;

		input = null;
		output = null;

		exiFactory = JSConstants.EXI_FACTORY;
		exiFactory.setCodingMode(CodingMode.BIT_PACKED);
		
		int indexArgument = 0;
		while (indexArgument < args.length) {
			String argument = args[indexArgument];

			// ### HELP ?
			if (HELP.equalsIgnoreCase(argument)) {
				printHelp();
				break;
			}
			// ### CODING_OPTIONS
			else if (ENCODE.equalsIgnoreCase(argument)) {
				cmdOption = CmdOption.encode;
			} else if (DECODE.equalsIgnoreCase(argument)) {
				cmdOption = CmdOption.decode;
			}
			// ### IO_OPTIONS
			else if (INPUT.equalsIgnoreCase(argument)) {
				assert ((indexArgument + 1) < args.length);
				indexArgument++;

				input = args[indexArgument];
//			} else if (OUTPUT.equalsIgnoreCase(argument)) {
//				assert ((indexArgument + 1) < args.length);
//				indexArgument++;
//
//				output = args[indexArgument];
			}
			// ### BYTEPACKED
			else if (CODING_BYTEPACKED.equalsIgnoreCase(argument)) {
				exiFactory.setCodingMode(CodingMode.BYTE_PACKED);
			}
			// ### PRE_COMPRESSION
			else if (CODING_PRE_COMPRESSION.equalsIgnoreCase(argument)) {
				exiFactory.setCodingMode(CodingMode.PRE_COMPRESSION);
			}
			// ### COMPRESSION
			else if (CODING_COMPRESSION.equalsIgnoreCase(argument)) {
				exiFactory.setCodingMode(CodingMode.COMPRESSION);
			} else {
				System.out.println("Unknown option '" + argument + "'");
			}

			indexArgument++;
		}

		// check input
		inputParametersOK = true;

		if (cmdOption == null) {
			inputParametersOK = false;
			printError("Missing coding option such as " + ENCODE + " and "
					+ DECODE);
		}

		if (input == null) {
			inputParametersOK = false;
			printError("Missing option -i");
		} else if (!(new File(input)).exists()) {
			inputParametersOK = false;
			printError("Not existing input parameter -i, \"" + input + "\"");
		} else {
			// ok
		}

		if (input != null && output == null) {
			// default output
			if (CmdOption.encode == cmdOption) {
				output = input + ".exi";

			} else {
				output = input + ".jsast";
			}
		}

		File fOutput = null;
		if (output == null) {
			inputParametersOK = false;
			printError("Missing output specification!");
		} else {
			fOutput = new File(output);

			if (fOutput.isDirectory()) {
				inputParametersOK = false;
				printError("Outputfile '" + output
						+ "' is unexpectedly a directory");
			} else {
				if (fOutput.exists()) {
					if (!fOutput.delete()) {
						inputParametersOK = false;
						printError("Existing outputfile '" + output
								+ "' could not be deleted.");
					}
				} else {
					// does not exits
					assert (!fOutput.exists());
					File parentDir = fOutput.getParentFile();
					if (parentDir != null && !parentDir.exists()) {
						if (!parentDir.mkdirs()) {
							inputParametersOK = false;
							printError("Output directories for file '" + output
									+ "' could not be created.");
						}
					}
				}
			}
		}
	}

	protected void decode(String input, EXIFactory exiFactory, String output)
			throws FileNotFoundException, EXIException, IOException {

		EXItoAST exi2ast = new EXItoAST(exiFactory);
		OutputStream osAST = new FileOutputStream(output);
		exi2ast.generate(new FileInputStream(input), osAST);
		osAST.flush();
		osAST.close();
	}

	protected void encode(String input, EXIFactory exiFactory, String output)
			throws TransformerConfigurationException, IOException,
			EXIException, SAXException {
		
		
		String jsCode = new String(Files.readAllBytes(Paths.get(input)));
		JStoEXI js2exi = new JStoEXI(exiFactory);
		OutputStream os = new FileOutputStream(output);
		String jsonAST = JStoAST.getAST(jsCode);
		js2exi.generate(jsonAST, os);
		os.flush();
		os.close();
		
		

	}

	protected void process() throws EXIException, TransformerException,
			IOException, SAXException {
		if (inputParametersOK) {
			// start coding
			switch (cmdOption) {
			case decode:
				decode(input, exiFactory, output);
				break;
			case encode:
				encode(input, exiFactory, output);
				break;
			default:
				printError("Unexptected command option " + cmdOption);
				break;
			}

		} else {
			// something not right
			// info messages (see above)
			printError("Input parameters were incorrect");
		}
	}

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			// show help
			printHelp();
		} else {
			EXIficientCMD4JS cmd = new EXIficientCMD4JS();
			try {
				cmd.parseArguments(args);
				cmd.process();
			} catch (Exception e) {
				printError(e.getLocalizedMessage() + e.getClass());
			}
		}
	}

}
