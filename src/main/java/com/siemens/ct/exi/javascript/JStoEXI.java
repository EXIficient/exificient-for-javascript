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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.script.ScriptException;

import com.siemens.ct.exi.core.EXIBodyEncoder;
import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.EXIStreamEncoder;
import com.siemens.ct.exi.core.exceptions.EXIException;
import com.siemens.ct.exi.core.values.BooleanValue;
import com.siemens.ct.exi.core.values.FloatValue;
import com.siemens.ct.exi.core.values.IntegerValue;
import com.siemens.ct.exi.core.values.StringValue;

public class JStoEXI {

	PrintStream ps;
	EXIFactory ef;

	EXIBodyEncoder bodyEncoder;

	public JStoEXI() {
		this(JSConstants.EXI_FACTORY);
	}

	public JStoEXI(EXIFactory ef) {
		this.ef = ef;
		
		if(ef.getGrammars().isSchemaInformed()) {
			// schema-informed grammars (dedicated grammars in use)
		} else {
			// setup EXI schema/grammars
			ef.setGrammars(JSConstants.EXI_FOR_JS_GRAMMARS);
		}
		
	}

	public void setDebug(PrintStream ps) {
		this.ps = ps;
	}

	void println(String s) {
		if (ps != null) {
			ps.println(s);
		}
	}

	public void generate(InputStream is, OutputStream os) throws IOException, EXIException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
		StringBuilder stringBuilder = new StringBuilder();
		String content;
		while ((content = bufferedReader.readLine()) != null) {
			stringBuilder.append(content);
		}
		generate(stringBuilder.toString(), os);
	}

	public void generate(String jsonAST, OutputStream os) throws IOException, EXIException {
		if (jsonAST.length() < 10000) {
			println(jsonAST);
		} else {
			println("Characters " + jsonAST.length());
			println(jsonAST.substring(0, 10000) + " ...");
		}

		JsonReader reader = Json.createReader(new StringReader(jsonAST));
		JsonStructure js = reader.read();
		if (js instanceof JsonObject) {
			JsonObject jo = (JsonObject) js;
			handleProgram(jo, os);
		} else {
			throw new RuntimeException("Unexpected JsonArray");
		}
	}

	void handleProgram(JsonObject jo, OutputStream os) throws EXIException, IOException {
		EXIStreamEncoder streamEncoder = ef.createEXIStreamEncoder();
		bodyEncoder = streamEncoder.encodeHeader(os);
		bodyEncoder.encodeStartDocument();

		if (!jo.containsKey("type")) {
			throw new RuntimeException("No key type in Program");
		}
		JsonString js = jo.getJsonString("type");
		String type = js.getString();
		if ("Program".equals(type)) {
			println("<Program>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "Program", null);

			if (!jo.containsKey("body")) {
				throw new RuntimeException("No key type");
			}
			JsonArray ja = jo.getJsonArray("body");

			println("<body>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "body", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);

			Iterator<JsonValue> iter = ja.iterator();

			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					handleNode((JsonObject) jv);
				} else {
					throw new RuntimeException("Unexpected ValueType in body array: " + jv.getValueType());
				}
			}

			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</body>");
			bodyEncoder.encodeEndElement();
			println("</Program>");
			bodyEncoder.encodeEndElement();
		} else {
			throw new RuntimeException("Unexpected type: " + type);
		}

		bodyEncoder.encodeEndDocument();
		bodyEncoder.flush();
	}

	// interface ArrayExpression <: Expression {
	// type: "ArrayExpression";
	// elements: [ Expression | null ];
	// }
	void handleArrayExpression(JsonObject jo) throws EXIException, IOException {
		println("<ArrayExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "ArrayExpression", null);

		// elements: [ Expression | null ];
		{
			println("<elements>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "elements", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			JsonValue jvElements = jo.get("elements");
			if (jvElements.getValueType() != ValueType.ARRAY) {
				throw new RuntimeException("Unsupported ValueType " + jvElements.getValueType() + " for elements");
			}
			JsonArray jaProperties = (JsonArray) jvElements;
			Iterator<JsonValue> iter = jaProperties.iterator();
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					JsonObject joProperty = (JsonObject) jv;
					if (!joProperty.containsKey("type")) {
						// TODO not sure why Nashorn sometimes does not report
						// type properly!
						handleNode(joProperty, "Property");
					} else {
						handleNode(joProperty);
					}
				} else if (jv.getValueType() == ValueType.NULL) {
					println("<null>");
					bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
					println("</null>");
					bodyEncoder.encodeEndElement();
				} else {
					throw new RuntimeException("Unexpected ValueType in elements array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</elements>");
			bodyEncoder.encodeEndElement();
		}

		println("</ArrayExpression>");
		bodyEncoder.encodeEndElement();
	}

	// interface AssignmentExpression <: Expression {
	// type: "AssignmentExpression";
	// operator: AssignmentOperator;
	// left: Pattern;
	// right: Expression;
	// }
	void handleAssignmentExpression(JsonObject jo) throws EXIException, IOException {
		println("<AssignmentExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "AssignmentExpression", null);

		// operator: AssignmentOperator;
		println("<operator>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "operator", null);
		// println("<string>" + jo.getString("operator") + "</string>");
		// bodyEncoder.encodeStartElement(JSConstants.URI, "string", null);
		bodyEncoder.encodeCharacters(new StringValue(jo.getString("operator")));
		// bodyEncoder.encodeEndElement();
		println("</operator>");
		bodyEncoder.encodeEndElement();

		// left: Pattern;
		println("<left>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "left", null);
		JsonValue jvLeft = jo.get("left");
		if (jvLeft.getValueType() != ValueType.OBJECT) {
			throw new RuntimeException("Unsupported ValueType " + jvLeft.getValueType() + " for left");
		}
		handleNode((JsonObject) jvLeft);
		println("</left>");
		bodyEncoder.encodeEndElement();

		// right: Expression;
		println("<right>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "right", null);
		JsonValue jvRight = jo.get("right");
		if (jvRight.getValueType() != ValueType.OBJECT) {
			throw new RuntimeException("Unsupported ValueType " + jvRight.getValueType() + " for right");
		}
		handleNode((JsonObject) jvRight);
		println("</right>");
		bodyEncoder.encodeEndElement();

		println("</AssignmentExpression>");
		bodyEncoder.encodeEndElement();
	}

	void handleBinaryExpression(JsonObject jo) throws EXIException, IOException {
		println("<BinaryExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "BinaryExpression", null);

		
		bodyEncoder.encodeStartElement(JSConstants.URI, "operator", null);
		// enum BinaryOperator
		// println("<string>" + jo.getString("operator") + "</string>");
		// bodyEncoder.encodeStartElement(JSConstants.URI, "string", null);
		String sop = jo.getString("operator");
		println("<operator>" + sop + "</operator>");
		bodyEncoder.encodeCharacters(new StringValue(sop));
		// bodyEncoder.encodeEndElement();
		// println("</operator>");
		bodyEncoder.encodeEndElement();

		println("<left>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "left", null);
		JsonValue jvLeft = jo.get("left");
		if (jvLeft.getValueType() != ValueType.OBJECT) {
			throw new RuntimeException("Unsupported ValueType " + jvLeft.getValueType() + " for left");
		}
		handleNode((JsonObject) jvLeft);
		println("</left>");
		bodyEncoder.encodeEndElement();

		println("<right>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "right", null);
		JsonValue jvRight = jo.get("right");
		if (jvRight.getValueType() != ValueType.OBJECT) {
			throw new RuntimeException("Unsupported ValueType " + jvRight.getValueType() + " for right");
		}
		handleNode((JsonObject) jvRight);
		println("</right>");
		bodyEncoder.encodeEndElement();

		println("</BinaryExpression>");
		bodyEncoder.encodeEndElement();
	}

	void handleBlockStatement(JsonObject jo) throws EXIException, IOException {
		println("<BlockStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "BlockStatement", null);

		println("<body>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "body", null);
		println("<array>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
		JsonValue jvBody = jo.get("body");
		if (jvBody == null) {
			// TODO Nashorn seems to have it sometimes in another "block"
			JsonValue jv = jo.get("block");
			if (jv != null && jv.getValueType() == ValueType.OBJECT) {
				JsonObject jvo = (JsonObject) jv;
				jvBody = jvo.get("body");
			}
		}

		if (jvBody.getValueType() != ValueType.ARRAY) {
			throw new RuntimeException("Unsupported ValueType " + jvBody.getValueType() + " for body");
		}
		JsonArray jaBody = (JsonArray) jvBody;
		Iterator<JsonValue> iter = jaBody.iterator();
		while (iter.hasNext()) {
			JsonValue jv = iter.next();
			if (jv.getValueType() == ValueType.OBJECT) {
				handleNode((JsonObject) jv);
			} else {
				throw new RuntimeException("Unexpected ValueType in body array: " + jv.getValueType());
			}
		}
		println("</array>");
		bodyEncoder.encodeEndElement();
		println("</body>");
		bodyEncoder.encodeEndElement();

		println("</BlockStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface BreakStatement <: Statement {
	// type: "BreakStatement";
	// label: Identifier | null;
	// }
	void handleBreakStatement(JsonObject jo) throws EXIException, IOException {
		println("<BreakStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "BreakStatement", null);

		// label: Identifier | null;
		{
			JsonValue jvLabel = jo.get("label");
			if (jvLabel.getValueType() != ValueType.OBJECT && jvLabel.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvLabel.getValueType() + " for label");
			}
			println("<label>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "label", null);
			if (jvLabel.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvLabel);
			}
			println("</label>");
			bodyEncoder.encodeEndElement();
		}

		println("</BreakStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface CallExpression <: Expression {
	// type: "CallExpression";
	// callee: Expression;
	// arguments: [ Expression ];
	// }
	void handleCallExpression(JsonObject jo) throws EXIException, IOException {
		println("<CallExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "CallExpression", null);

		// callee: Expression;
		println("<callee>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "callee", null);
		JsonValue jvCallee = jo.get("callee");
		if (jvCallee.getValueType() != ValueType.OBJECT) {
			throw new RuntimeException("Unsupported ValueType " + jvCallee.getValueType() + " for callee");
		}
		handleNode((JsonObject) jvCallee);
		println("</callee>");
		bodyEncoder.encodeEndElement();

		// arguments: [ Expression ];
		println("<arguments>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "arguments", null);
		println("<array>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
		JsonValue jvArguments = jo.get("arguments");
		if (jvArguments.getValueType() != ValueType.ARRAY) {
			throw new RuntimeException("Unsupported ValueType " + jvArguments.getValueType() + " for arguments");
		}
		JsonArray jaArguments = (JsonArray) jvArguments;
		Iterator<JsonValue> iter = jaArguments.iterator();
		while (iter.hasNext()) {
			JsonValue jv = iter.next();
			if (jv.getValueType() == ValueType.OBJECT) {
				handleNode((JsonObject) jv);
			} else {
				throw new RuntimeException("Unexpected ValueType in arguments array: " + jv.getValueType());
			}
		}
		println("</array>");
		bodyEncoder.encodeEndElement();
		println("</arguments>");
		bodyEncoder.encodeEndElement();

		println("</CallExpression>");
		bodyEncoder.encodeEndElement();
	}

	// interface ContinueStatement <: Statement {
	// type: "ContinueStatement";
	// label: Identifier | null;
	// }
	void handleContinueStatement(JsonObject jo) throws EXIException, IOException {
		println("<ContinueStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "ContinueStatement", null);

		// label: Identifier | null;
		{
			JsonValue jvLabel = jo.get("label");
			
//			if (jvLabel.getValueType() != ValueType.OBJECT && jvLabel.getValueType() != ValueType.NULL) {
//				throw new RuntimeException("Unsupported ValueType " + jvLabel.getValueType() + " for label");
//			}
			
			println("<label>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "label", null);
			if (jvLabel.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				
				handleIdentifier2(jvLabel);
				// handleNode((JsonObject) jvLabel);
			}
			println("</label>");
			bodyEncoder.encodeEndElement();
		}

		println("</ContinueStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface CatchClause <: Node {
	// type: "CatchClause";
	// param: Pattern;
	// guard: Expression | null;
	// body: BlockStatement;
	// }
	void handleCatchClause(JsonObject jo) throws EXIException, IOException {
		println("<CatchClause>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "CatchClause", null);

		// param: Pattern;
		{
			println("<param>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "param", null);
			JsonValue jvParam = jo.get("param");
			if (jvParam.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvParam.getValueType() + " for param");
			}
			handleNode((JsonObject) jvParam);
			println("</param>");
			bodyEncoder.encodeEndElement();
		}

		// guard: Expression | null;
		{
			JsonValue jvGuard = jo.get("guard");
			if (jvGuard != null && jvGuard.getValueType() != ValueType.OBJECT
					&& jvGuard.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvGuard.getValueType() + " for guard");
			}
			println("<guard>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "guard", null);
			if (jvGuard == null || jvGuard.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvGuard);
			}
			println("</guard>");
			bodyEncoder.encodeEndElement();
		}

		// body: BlockStatement;
		{
			println("<body>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "body", null);
			JsonValue jvBody = jo.get("body");
			if (jvBody.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvBody.getValueType() + " for body");
			}
			handleNode((JsonObject) jvBody);
			println("</body>");
			bodyEncoder.encodeEndElement();
		}

		println("</CatchClause>");
		bodyEncoder.encodeEndElement();
	}

	// interface ConditionalExpression <: Expression {
	// type: "ConditionalExpression";
	// test: Expression;
	// alternate: Expression;
	// consequent: Expression;
	// }
	void handleConditionalExpression(JsonObject jo) throws EXIException, IOException {
		println("<ConditionalExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "ConditionalExpression", null);

		// test: Expression;
		{
			println("<test>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "test", null);
			JsonValue jvTest = jo.get("test");
			if (jvTest.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvTest.getValueType() + " for test");
			}
			handleNode((JsonObject) jvTest);
			println("</test>");
			bodyEncoder.encodeEndElement();
		}

		// alternate: Expression;
		{
			println("<alternate>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "alternate", null);
			JsonValue jvAlternate = jo.get("alternate");
			if (jvAlternate.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvAlternate.getValueType() + " for alternate");
			}
			handleNode((JsonObject) jvAlternate);
			println("</alternate>");
			bodyEncoder.encodeEndElement();
		}

		// consequent: Expression;
		{
			println("<consequent>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "consequent", null);
			JsonValue jvConsequent = jo.get("consequent");
			if (jvConsequent.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvConsequent.getValueType() + " for consequent");
			}
			handleNode((JsonObject) jvConsequent);
			println("</consequent>");
			bodyEncoder.encodeEndElement();
		}

		println("</ConditionalExpression>");
		bodyEncoder.encodeEndElement();
	}

	// interface DoWhileStatement <: Statement {
	// type: "DoWhileStatement";
	// body: Statement;
	// test: Expression;
	// }
	void handleDoWhileStatement(JsonObject jo) throws EXIException, IOException {
		println("<DoWhileStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "DoWhileStatement", null);

		// body: Statement;
		{
			println("<body>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "body", null);
			JsonValue jvBody = jo.get("body");
			if (jvBody.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvBody.getValueType() + " for body");
			}
			handleNode((JsonObject) jvBody);
			println("</body>");
			bodyEncoder.encodeEndElement();
		}

		// test: Expression;
		{
			println("<test>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "test", null);
			JsonValue jvTest = jo.get("test");
			if (jvTest.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvTest.getValueType() + " for test");
			}
			handleNode((JsonObject) jvTest);
			println("</test>");
			bodyEncoder.encodeEndElement();
		}

		println("</DoWhileStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface ExpressionStatement <: Statement {
	// type: "ExpressionStatement";
	// expression: Expression;
	// }
	void handleExpressionStatement(JsonObject jo) throws EXIException, IOException {
		println("<ExpressionStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "ExpressionStatement", null);

		println("<expression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "expression", null);
		JsonValue jvExpression = jo.get("expression");
		if (jvExpression.getValueType() != ValueType.OBJECT) {
			throw new RuntimeException("Unsupported ValueType " + jvExpression.getValueType() + " for expression");
		}
		handleNode((JsonObject) jvExpression);
		println("</expression>");
		bodyEncoder.encodeEndElement();

		println("</ExpressionStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface ForInStatement <: Statement {
	// type: "ForInStatement";
	// left: VariableDeclaration | Expression;
	// right: Expression;
	// body: Statement;
	// each: boolean;
	// }
	void handleForInStatement(JsonObject jo) throws EXIException, IOException {
		println("<ForInStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "ForInStatement", null);

		// left: VariableDeclaration | Expression;
		{
			println("<left>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "left", null);
			JsonValue jvLeft = jo.get("left");
			if (jvLeft.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvLeft.getValueType() + " for left");
			}
			handleNode((JsonObject) jvLeft);
			println("</left>");
			bodyEncoder.encodeEndElement();
		}

		// right: Expression;
		{
			println("<right>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "right", null);
			JsonValue jvRight = jo.get("right");
			if (jvRight.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvRight.getValueType() + " for right");
			}
			handleNode((JsonObject) jvRight);
			println("</right>");
			bodyEncoder.encodeEndElement();
		}

		// body: Statement;
		{
			println("<body>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "body", null);
			JsonValue jvBody = jo.get("body");
			if (jvBody.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvBody.getValueType() + " for body");
			}
			handleNode((JsonObject) jvBody);
			println("</body>");
			bodyEncoder.encodeEndElement();
		}

		// each: boolean;
		{
			JsonValue jvEach = jo.get("each");
			if (jvEach.getValueType() != ValueType.TRUE && jvEach.getValueType() != ValueType.FALSE) {
				throw new RuntimeException("Unsupported ValueType " + jvEach.getValueType() + " for each");
			}
			println("<each>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "each", null);
			if (jvEach.getValueType() == ValueType.TRUE) {
				println("true");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_TRUE);
			} else {
				println("false");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_FALSE);
			}
			println("</each>");
			bodyEncoder.encodeEndElement();
		}

		println("</ForInStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface ForStatement <: Statement {
	// type: "ForStatement";
	// init: VariableDeclaration | Expression | null;
	// test: Expression | null;
	// update: Expression | null;
	// body: Statement;
	// }
	void handleForStatement(JsonObject jo) throws EXIException, IOException {
		println("<ForStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "ForStatement", null);

		// init: VariableDeclaration | Expression | null;
		{
			JsonValue jvInit = jo.get("init");
			if (jvInit.getValueType() != ValueType.OBJECT && jvInit.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvInit.getValueType() + " for init");
			}
			println("<init>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "init", null);
			if (jvInit.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvInit);
			}
			println("</init>");
			bodyEncoder.encodeEndElement();
		}

		// test: Expression | null;
		{
			JsonValue jvTest = jo.get("test");
			if (jvTest.getValueType() != ValueType.OBJECT && jvTest.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvTest.getValueType() + " for test");
			}
			println("<test>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "test", null);
			if (jvTest.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvTest);
			}
			println("</test>");
			bodyEncoder.encodeEndElement();
		}

		// update: Expression | null;
		{
			JsonValue jvUpdate = jo.get("update");
			if (jvUpdate.getValueType() != ValueType.OBJECT && jvUpdate.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvUpdate.getValueType() + " for update");
			}
			println("<update>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "update", null);
			if (jvUpdate.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvUpdate);
			}
			println("</update>");
			bodyEncoder.encodeEndElement();
		}

		// body: Statement;
		{
			println("<body>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "body", null);
			JsonValue jvBody = jo.get("body");
			if (jvBody.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvBody.getValueType() + " for body");
			}
			handleNode((JsonObject) jvBody);
			println("</body>");
			bodyEncoder.encodeEndElement();
		}

		println("</ForStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface FunctionDeclaration <: Function, Declaration {
	// type: "FunctionDeclaration";
	// id: Identifier;
	// params: [ Pattern ];
	// defaults: [ Expression ];
	// rest: Identifier | null;
	// body: BlockStatement | Expression;
	// generator: boolean;
	// expression: boolean;
	// }
	void handleFunctionDeclaration(JsonObject jo) throws EXIException, IOException {
		println("<FunctionDeclaration>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "FunctionDeclaration", null);

		// id
		{
			JsonValue jvId = jo.get("id");
			if (jvId.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvId.getValueType() + " for id");
			}
			println("<id>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "id", null);
			handleNode((JsonObject) jvId);
			println("</id>");
			bodyEncoder.encodeEndElement();
		}

		// params
		{
			JsonValue jvParams = jo.get("params");
			if (jvParams.getValueType() != ValueType.ARRAY) {
				throw new RuntimeException("Unsupported ValueType " + jvParams.getValueType() + " for params");
			}
			println("<params>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "params", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			JsonArray jaParams = (JsonArray) jvParams;
			Iterator<JsonValue> iter = jaParams.iterator();
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					handleNode((JsonObject) jv);
				} else {
					throw new RuntimeException("Unexpected ValueType in params array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</params>");
			bodyEncoder.encodeEndElement();
		}

		// defaults: [ Expression ];
		{
			JsonValue jvDefaults = jo.get("defaults");
			if (jvDefaults.getValueType() != ValueType.ARRAY) {
				throw new RuntimeException("Unsupported ValueType " + jvDefaults.getValueType() + " for defaults");
			}
			println("<defaults>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "defaults", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			JsonArray jaDefaults = (JsonArray) jvDefaults;
			Iterator<JsonValue> iter = jaDefaults.iterator();
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					handleNode((JsonObject) jv);
				} else {
					throw new RuntimeException("Unexpected ValueType in defaults array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</defaults>");
			bodyEncoder.encodeEndElement();
		}

		// rest: Identifier | null;
		{
			JsonValue jvRest = jo.get("rest");
			if (jvRest.getValueType() != ValueType.OBJECT && jvRest.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvRest.getValueType() + " for rest");
			}
			println("<rest>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "rest", null);
			if (jvRest.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvRest); // Identifier
			}
			println("</rest>");
			bodyEncoder.encodeEndElement();
		}

		// body: BlockStatement | Expression;
		{
			JsonValue jvBody = jo.get("body");
			if (jvBody.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvBody.getValueType() + " for body");
			}
			println("<body>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "body", null);
			handleNode((JsonObject) jvBody);
			println("</body>");
			bodyEncoder.encodeEndElement();
		}

		// generator: boolean;
		{
			JsonValue jvGenerator = jo.get("generator");
			if (jvGenerator.getValueType() != ValueType.TRUE && jvGenerator.getValueType() != ValueType.FALSE) {
				throw new RuntimeException("Unsupported ValueType " + jvGenerator.getValueType() + " for generator");
			}
			println("<generator>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "generator", null);
			if (jvGenerator.getValueType() == ValueType.TRUE) {
				println("true");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_TRUE);
			} else {
				println("false");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_FALSE);
			}
			println("</generator>");
			bodyEncoder.encodeEndElement();
		}

		// expression: boolean;
		{
			JsonValue jvExpression = jo.get("expression");
			if (jvExpression.getValueType() != ValueType.TRUE && jvExpression.getValueType() != ValueType.FALSE) {
				throw new RuntimeException("Unsupported ValueType " + jvExpression.getValueType() + " for expression");
			}
			println("<expression>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "expression", null);
			if (jvExpression.getValueType() == ValueType.TRUE) {
				println("true");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_TRUE);
			} else {
				println("false");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_FALSE);
			}
			println("</expression>");
			bodyEncoder.encodeEndElement();
		}

		println("</FunctionDeclaration>");
		bodyEncoder.encodeEndElement();
	}

	// interface FunctionExpression <: Function, Expression {
	// type: "FunctionExpression";
	// id: Identifier | null;
	// params: [ Pattern ];
	// defaults: [ Expression ];
	// rest: Identifier | null;
	// body: BlockStatement | Expression;
	// generator: boolean;
	// expression: boolean;
	// }
	void handleFunctionExpression(JsonObject jo) throws EXIException, IOException {
		println("<FunctionExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "FunctionExpression", null);

		// id: Identifier | null;
		{
			JsonValue jvId = jo.get("id");
			if (jvId.getValueType() != ValueType.OBJECT && jvId.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvId.getValueType() + " for id");
			}
			println("<id>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "id", null);
			if (jvId.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvId);
			}
			println("</id>");
			bodyEncoder.encodeEndElement();
		}

		// params: [ Pattern ];
		{
			JsonValue jvParams = jo.get("params");
			if (jvParams.getValueType() != ValueType.ARRAY) {
				throw new RuntimeException("Unsupported ValueType " + jvParams.getValueType() + " for params");
			}
			println("<params>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "params", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			JsonArray jaParams = (JsonArray) jvParams;
			Iterator<JsonValue> iter = jaParams.iterator();
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					handleNode((JsonObject) jv);
				} else {
					throw new RuntimeException("Unexpected ValueType in params array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</params>");
			bodyEncoder.encodeEndElement();
		}

		// defaults: [ Expression ];
		{
			JsonValue jvDefaults = jo.get("defaults");
			if (jvDefaults.getValueType() != ValueType.ARRAY) {
				throw new RuntimeException("Unsupported ValueType " + jvDefaults.getValueType() + " for defaults");
			}
			println("<defaults>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "defaults", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			JsonArray jaDefaults = (JsonArray) jvDefaults;
			Iterator<JsonValue> iter = jaDefaults.iterator();
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					handleNode((JsonObject) jv);
				} else {
					throw new RuntimeException("Unexpected ValueType in defaults array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</defaults>");
			bodyEncoder.encodeEndElement();
		}

		// rest: Identifier | null;
		{
			JsonValue jvRest = jo.get("rest");
			if (jvRest.getValueType() != ValueType.OBJECT && jvRest.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvRest.getValueType() + " for rest");
			}
			println("<rest>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "rest", null);
			if (jvRest.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvRest); // Identifier
			}
			println("</rest>");
			bodyEncoder.encodeEndElement();
		}

		// body: BlockStatement | Expression;
		{
			JsonValue jvBody = jo.get("body");
			if (jvBody.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvBody.getValueType() + " for body");
			}
			println("<body>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "body", null);
			handleNode((JsonObject) jvBody);
			println("</body>");
			bodyEncoder.encodeEndElement();
		}

		// generator: boolean;
		{
			JsonValue jvGenerator = jo.get("generator");
			if (jvGenerator.getValueType() != ValueType.TRUE && jvGenerator.getValueType() != ValueType.FALSE) {
				throw new RuntimeException("Unsupported ValueType " + jvGenerator.getValueType() + " for generator");
			}
			println("<generator>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "generator", null);
			if (jvGenerator.getValueType() == ValueType.TRUE) {
				println("true");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_TRUE);
			} else {
				println("false");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_FALSE);
			}
			println("</generator>");
			bodyEncoder.encodeEndElement();
		}

		// expression: boolean;
		{
			JsonValue jvExpression = jo.get("expression");
			if (jvExpression.getValueType() != ValueType.TRUE && jvExpression.getValueType() != ValueType.FALSE) {
				throw new RuntimeException("Unsupported ValueType " + jvExpression.getValueType() + " for expression");
			}
			println("<expression>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "expression", null);
			if (jvExpression.getValueType() == ValueType.TRUE) {
				println("true");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_TRUE);
			} else {
				println("false");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_FALSE);
			}
			println("</expression>");
			bodyEncoder.encodeEndElement();
		}

		println("</FunctionExpression>");
		bodyEncoder.encodeEndElement();
	}

	void handleIdentifier(JsonObject jo) throws EXIException, IOException {
		println("<Identifier>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "Identifier", null);
		println("<name>" + jo.getString("name") + "</name>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "name", null);
		bodyEncoder.encodeCharacters(new StringValue(jo.getString("name")));
		bodyEncoder.encodeEndElement();
		println("</Identifier>");
		bodyEncoder.encodeEndElement();
	}
	
	protected void handleIdentifier2(JsonValue jv) throws EXIException, IOException {
		String name;
		if(jv.getValueType() == ValueType.STRING) {
			JsonString js = (JsonString) jv;
			name = js.getString();
		} else if(jv.getValueType() == ValueType.OBJECT) {
			JsonObject jo = (JsonObject) jv;
			name = jo.getString("name");
		} else {
			throw new RuntimeException("Unexpected ValueType in handleIdentifier2 : " + jv.getValueType());
		}
		
		println("<Identifier>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "Identifier", null);
		
		println("<name>" + name + "</name>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "name", null);
		bodyEncoder.encodeCharacters(new StringValue(name));
		bodyEncoder.encodeEndElement();
		
		println("</Identifier>");
		bodyEncoder.encodeEndElement();
	}

	// interface IfStatement <: Statement {
	// type: "IfStatement";
	// test: Expression;
	// consequent: Statement;
	// alternate: Statement | null;
	// }
	void handleIfStatement(JsonObject jo) throws EXIException, IOException {
		println("<IfStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "IfStatement", null);

		// test: Expression;
		{
			println("<test>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "test", null);
			JsonValue jvTest = jo.get("test");
			if (jvTest.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvTest.getValueType() + " for test");
			}
			handleNode((JsonObject) jvTest);
			println("</test>");
			bodyEncoder.encodeEndElement();
		}

		// consequent: Statement;
		{
			println("<consequent>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "consequent", null);
			JsonValue jvConsequent = jo.get("consequent");
			if (jvConsequent.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvConsequent.getValueType() + " for consequent");
			}
			handleNode((JsonObject) jvConsequent);
			println("</consequent>");
			bodyEncoder.encodeEndElement();
		}

		// alternate: Statement | null;
		{
			println("<alternate>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "alternate", null);
			JsonValue jvAlternate = jo.get("alternate");
			if (jvAlternate.getValueType() != ValueType.OBJECT && jvAlternate.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvAlternate.getValueType() + " for jalternate");
			}
			if (jvAlternate.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvAlternate);
			}

			println("</alternate>");
			bodyEncoder.encodeEndElement();

		}

		println("</IfStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface LabeledStatement <: Statement {
	// type: "LabeledStatement";
	// label: Identifier;
	// body: Statement;
	// }
	void handleLabeledStatement(JsonObject jo) throws EXIException, IOException {
		println("<LabeledStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "LabeledStatement", null);

		// label: Identifier;
		{
			println("<label>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "label", null);
			JsonValue jvLabel = jo.get("label");
			
			handleIdentifier2(jvLabel);
//			if (jvLabel.getValueType() != ValueType.OBJECT) {
//				throw new RuntimeException("Unsupported ValueType " + jvLabel.getValueType() + " for label");
//			}
//			handleNode((JsonObject) jvLabel);			
			
			println("</label>");
			bodyEncoder.encodeEndElement();
		}

		// body: Statement;
		{
			println("<body>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "body", null);
			JsonValue jvBody = jo.get("body");
			if (jvBody.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvBody.getValueType() + " for body");
			}
			handleNode((JsonObject) jvBody);
			println("</body>");
			bodyEncoder.encodeEndElement();
		}

		println("</LabeledStatement>");
		bodyEncoder.encodeEndElement();
	}

	void handleLiteral(JsonObject jo) throws EXIException, IOException {
		println("<Literal>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "Literal", null);
		println("<value>"); // value: string | boolean | null | number |
							// RegExp;
		bodyEncoder.encodeStartElement(JSConstants.URI, "value", null);
		JsonValue jv = jo.get("value");
		if (jv.getValueType() == ValueType.STRING) {
			JsonString jvs = (JsonString) jv;
			println("<string>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "string", null);
			println(jvs.getString());
			bodyEncoder.encodeCharacters(new StringValue(jvs.getString()));
			println("</string>");
			bodyEncoder.encodeEndElement();
		} else if (jv.getValueType() == ValueType.TRUE || jv.getValueType() == ValueType.FALSE) {
			println("<boolean>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "boolean", null);
			if (jv.getValueType() == ValueType.TRUE) {
				println("true");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_TRUE);
			} else {
				println("false");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_FALSE);
			}
			println("</boolean>");
			bodyEncoder.encodeEndElement();
		} else if (jv.getValueType() == ValueType.NUMBER) {
			JsonNumber jvn = (JsonNumber) jv;
			if (jvn.isIntegral()) {
				println("<integer>" + jvn.intValue() + "</integer>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "integer", null);
				bodyEncoder.encodeCharacters(IntegerValue.valueOf(jvn.intValue()));
				bodyEncoder.encodeEndElement();
			} else {
				println("<number>" + jvn.doubleValue() + "</number>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "number", null);
				bodyEncoder.encodeCharacters(FloatValue.parse(jvn.doubleValue()));
				bodyEncoder.encodeEndElement();
			}
		} else if (jv.getValueType() == ValueType.NULL) {
			println("<null>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
			println("</null>");
			bodyEncoder.encodeEndElement();
		} else {
			// TODO RegExp
			throw new RuntimeException("Unsupported ValueType " + jv.getValueType() + " in Value");
		}

		println("</value>");
		bodyEncoder.encodeEndElement();
		println("</Literal>");
		bodyEncoder.encodeEndElement();
	}

	// interface LogicalExpression <: Expression {
	// type: "LogicalExpression";
	// operator: LogicalOperator;
	// left: Expression;
	// right: Expression;
	// }
	void handleLogicalExpression(JsonObject jo) throws EXIException, IOException {
		println("<LogicalExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "LogicalExpression", null);

		// operator: LogicalOperator;
		{
			println("<operator>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "operator", null);
			JsonValue jvOperator = jo.get("operator");
			if (jvOperator.getValueType() != ValueType.STRING) {
				throw new RuntimeException("Unsupported ValueType " + jvOperator.getValueType() + " for operator");
			}
			JsonString js = (JsonString) jvOperator;
			println(js.getString());
			bodyEncoder.encodeCharacters(new StringValue(js.getString()));
			println("</operator>");
			bodyEncoder.encodeEndElement();
		}

		// left: Expression;
		{
			println("<left>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "left", null);
			JsonValue jvLeft = jo.get("left");
			if (jvLeft.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvLeft.getValueType() + " for left");
			}
			this.handleNode((JsonObject) jvLeft);
			println("</left>");
			bodyEncoder.encodeEndElement();
		}

		// right: Expression;
		{
			println("<right>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "right", null);
			JsonValue jvRight = jo.get("left");
			if (jvRight.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvRight.getValueType() + " for right");
			}
			this.handleNode((JsonObject) jvRight);
			println("</right>");
			bodyEncoder.encodeEndElement();
		}

		println("</LogicalExpression>");
		bodyEncoder.encodeEndElement();
	}

	// interface MemberExpression <: Expression {
	// type: "MemberExpression";
	// object: Expression;
	// property: Identifier | Expression;
	// computed: boolean;
	// }
	void handleMemberExpression(JsonObject jo) throws EXIException, IOException {
		println("<MemberExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "MemberExpression", null);

		// object: Expression;
		println("<object>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "object", null);
		JsonValue jvObject = jo.get("object");
		if (jvObject.getValueType() != ValueType.OBJECT) {
			throw new RuntimeException("Unsupported ValueType " + jvObject.getValueType() + " for object");
		}
		handleNode((JsonObject) jvObject);
		println("</object>");
		bodyEncoder.encodeEndElement();

		// property: Identifier | Expression;
		println("<property>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "property", null);
		JsonValue jvProperty = jo.get("property");
		if (jvProperty.getValueType() != ValueType.OBJECT && jvProperty.getValueType() != ValueType.STRING) {
			throw new RuntimeException("Unsupported ValueType " + jvProperty.getValueType() + " for property");
		}
		if (jvProperty.getValueType() == ValueType.STRING) {
			// Identifier
			println("<Identifier>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "Identifier", null);
			println("<name>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "name", null);
			JsonString js = (JsonString) jvProperty;
			bodyEncoder.encodeCharacters(new StringValue(js.getString()));
			println("</name>");
			bodyEncoder.encodeEndElement();
			println("</Identifier>");
			bodyEncoder.encodeEndElement();
		} else {
			handleNode((JsonObject) jvProperty);
		}

		println("</property>");
		bodyEncoder.encodeEndElement();

		// computed: boolean;
		println("<computed>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "computed", null);

		JsonValue jvComputed = jo.get("computed");
		if (jvComputed.getValueType() != ValueType.TRUE && jvComputed.getValueType() != ValueType.FALSE) {
			throw new RuntimeException("Unsupported ValueType " + jvComputed.getValueType() + " for computed");
		}
		if (jvComputed.getValueType() == ValueType.TRUE) {
			println("true");
			bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_TRUE);
		} else {
			println("false");
			bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_FALSE);
		}
		println("</computed>");
		bodyEncoder.encodeEndElement();

		println("</MemberExpression>");
		bodyEncoder.encodeEndElement();
	}

	// interface NewExpression <: Expression {
	// type: "NewExpression";
	// callee: Expression;
	// arguments: [ Expression ];
	// }
	void handleNewExpression(JsonObject jo) throws EXIException, IOException {
		println("<NewExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "NewExpression", null);

		// callee: Expression;
		{
			println("<callee>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "callee", null);
			JsonValue jvCallee = jo.get("callee");
			if (jvCallee.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvCallee.getValueType() + " for callee");
			}
			handleNode((JsonObject) jvCallee);
			println("</callee>");
			bodyEncoder.encodeEndElement();
		}

		// arguments: [ Expression ];
		{
			JsonValue jvArguments = jo.get("arguments");
			if (jvArguments.getValueType() != ValueType.ARRAY) {
				throw new RuntimeException("Unsupported ValueType " + jvArguments.getValueType() + " for arguments");
			}
			println("<arguments>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "arguments", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			JsonArray jaArguments = (JsonArray) jvArguments;
			Iterator<JsonValue> iter = jaArguments.iterator();
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					handleNode((JsonObject) jv);
				} else {
					throw new RuntimeException("Unexpected ValueType in arguments array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</arguments>");
			bodyEncoder.encodeEndElement();
		}

		println("</NewExpression>");
		bodyEncoder.encodeEndElement();
	}

	// interface ObjectExpression <: Expression {
	// type: "ObjectExpression";
	// properties: [ Property ];
	// }
	void handleObjectExpression(JsonObject jo) throws EXIException, IOException {
		println("<ObjectExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "ObjectExpression", null);

		// properties: [ Property ];
		{
			println("<properties>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "properties", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			JsonValue jvProperties = jo.get("properties");
			if (jvProperties.getValueType() != ValueType.ARRAY) {
				throw new RuntimeException("Unsupported ValueType " + jvProperties.getValueType() + " for properties");
			}
			JsonArray jaProperties = (JsonArray) jvProperties;
			Iterator<JsonValue> iter = jaProperties.iterator();
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					JsonObject joProperty = (JsonObject) jv;
					if (!joProperty.containsKey("type")) {
						// TODO not sure why Nashorn sometimes does not report
						// type properly!
						handleNode(joProperty, "Property");
					} else {
						handleNode(joProperty);
					}

				} else {
					throw new RuntimeException("Unexpected ValueType in body array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</properties>");
			bodyEncoder.encodeEndElement();
		}

		println("</ObjectExpression>");
		bodyEncoder.encodeEndElement();
	}

	// interface Property <: Node {
	// type: "Property";
	// key: Literal | Identifier;
	// value: Expression;
	// kind: "init" | "get" | "set";
	// }
	void handleProperty(JsonObject jo) throws EXIException, IOException {
		println("<Property>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "Property", null);

		// key: Literal | Identifier;
		{
			println("<key>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "key", null);
			JsonValue jvKey = jo.get("key");
			if (jvKey.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvKey.getValueType() + " for key");
			}
			handleNode((JsonObject) jvKey);
			println("</key>");
			bodyEncoder.encodeEndElement();
		}

		// value: Expression;
		{
			println("<value>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "value", null);
			JsonValue jvValue = jo.get("value");
			if (jvValue.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvValue.getValueType() + " for value");
			}
			handleNode((JsonObject) jvValue);
			println("</value>");
			bodyEncoder.encodeEndElement();
		}

		// kind: "init" | "get" | "set";
		{
			println("<kind>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "kind", null);
			JsonValue jvKind = jo.get("kind");
			if (jvKind.getValueType() != ValueType.STRING) {
				throw new RuntimeException("Unsupported ValueType " + jvKind.getValueType() + " for kind");
			}
			JsonString js = (JsonString) jvKind;
			println(js.getString());
			bodyEncoder.encodeCharacters(new StringValue(js.getString()));
			println("</kind>");
			bodyEncoder.encodeEndElement();
		}

		println("</Property>");
		bodyEncoder.encodeEndElement();
	}

	// interface ReturnStatement <: Statement {
	// type: "ReturnStatement";
	// argument: Expression | null;
	// }
	void handleReturnStatement(JsonObject jo) throws EXIException, IOException {
		println("<ReturnStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "ReturnStatement", null);

		// argument: Expression | null;
		{
			println("<argument>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "argument", null);
			JsonValue jvArgument = jo.get("argument");
			if (jvArgument.getValueType() != ValueType.OBJECT && jvArgument.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvArgument.getValueType() + " for argument");
			}
			if (jvArgument.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvArgument);
			}

			println("</argument>");
			bodyEncoder.encodeEndElement();

		}

		println("</ReturnStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface SwitchCase <: Node {
	// type: "SwitchCase";
	// test: Expression | null;
	// consequent: [ Statement ];
	// }
	void handleSwitchCase(JsonObject jo) throws EXIException, IOException {
		println("<SwitchCase>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "SwitchCase", null);

		// test: Expression | null;
		{
			JsonValue jsTest = jo.get("test");
			println("<init>"); // Expression | null;
			bodyEncoder.encodeStartElement(JSConstants.URI, "test", null);
			if (jsTest.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else if (jsTest.getValueType() == ValueType.OBJECT) {
				handleNode((JsonObject) jsTest);
			} else {
				throw new RuntimeException("Unsupported type " + jsTest.getValueType() + " for test");
			}
			println("</test>");
			bodyEncoder.encodeEndElement();
		}

		// consequent: [ Statement ];
		{
			println("<consequent>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "consequent", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			JsonValue jvConsequent = jo.get("consequent");
			if (jvConsequent.getValueType() != ValueType.ARRAY) {
				throw new RuntimeException("Unsupported ValueType " + jvConsequent.getValueType() + " for consequent");
			}
			JsonArray jaConsequent = (JsonArray) jvConsequent;
			Iterator<JsonValue> iter = jaConsequent.iterator();
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					handleNode((JsonObject) jv);
				} else {
					throw new RuntimeException("Unexpected ValueType in consequent array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</consequent>");
			bodyEncoder.encodeEndElement();
		}

		println("</SwitchCase>");
		bodyEncoder.encodeEndElement();
	}

	// interface SwitchStatement <: Statement {
	// type: "SwitchStatement";
	// discriminant: Expression;
	// cases: [ SwitchCase ];
	// lexical: boolean;
	// }
	void handleSwitchStatement(JsonObject jo) throws EXIException, IOException {
		println("<SwitchStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "SwitchStatement", null);

		// discriminant: Expression;
		{
			println("<discriminant>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "discriminant", null);
			JsonValue jvDiscriminant = jo.get("discriminant");
			if (jvDiscriminant.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException(
						"Unsupported ValueType " + jvDiscriminant.getValueType() + " for discriminant");
			}
			handleNode((JsonObject) jvDiscriminant);
			println("</discriminant>");
			bodyEncoder.encodeEndElement();
		}

		// cases: [ SwitchCase ];
		{
			println("<cases>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "cases", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			JsonValue jvCases = jo.get("cases");
			if (jvCases.getValueType() != ValueType.ARRAY) {
				throw new RuntimeException("Unsupported ValueType " + jvCases.getValueType() + " for cases");
			}
			JsonArray jaCases = (JsonArray) jvCases;
			Iterator<JsonValue> iter = jaCases.iterator();
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					handleNode((JsonObject) jv);
				} else {
					throw new RuntimeException("Unexpected ValueType in cases array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</cases>");
			bodyEncoder.encodeEndElement();
		}

		// lexical: boolean;
		{
			println("<lexical>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "lexical", null);

			JsonValue jvLexical = jo.get("lexical");
			// TODO optional?
			if (jvLexical != null && jvLexical.getValueType() != ValueType.TRUE
					&& jvLexical.getValueType() != ValueType.FALSE) {
				throw new RuntimeException("Unsupported ValueType " + jvLexical.getValueType() + " for lexical");
			}
			if (jvLexical == null || jvLexical.getValueType() == ValueType.FALSE) {
				println("false");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_FALSE);
			} else {
				println("true");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_TRUE);
			}
			println("</lexical>");
			bodyEncoder.encodeEndElement();
		}

		println("</SwitchStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface ThisExpression <: Expression {
	// type: "ThisExpression";
	// }
	void handleThisExpression(JsonObject jo) throws EXIException, IOException {
		println("<ThisExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "ThisExpression", null);

		println("</ThisExpression>");
		bodyEncoder.encodeEndElement();
	}

	// interface ThrowStatement <: Statement {
	// type: "ThrowStatement";
	// argument: Expression;
	// }
	void handleThrowStatement(JsonObject jo) throws EXIException, IOException {
		println("<ThrowStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "ThrowStatement", null);

		// argument: Expression;
		{
			println("<argument>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "argument", null);
			JsonValue jvArgument = jo.get("argument");
			if (jvArgument.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvArgument.getValueType() + " for argument");
			}
			handleNode((JsonObject) jvArgument);
			println("</argument>");
			bodyEncoder.encodeEndElement();
		}

		println("</ThrowStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface TryStatement <: Statement {
	// type: "TryStatement";
	// block: BlockStatement;
	// handler: CatchClause | null;
	// guardedHandlers: [ CatchClause ];
	// finalizer: BlockStatement | null;
	// }
	void handleTryStatement(JsonObject jo) throws EXIException, IOException {
		println("<TryStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "TryStatement", null);

		// block: BlockStatement;
		{
			println("<block>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "block", null);
			JsonValue jvBlock = jo.get("block");
			if (jvBlock.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvBlock.getValueType() + " for block");
			}
			handleNode((JsonObject) jvBlock);
			println("</block>");
			bodyEncoder.encodeEndElement();
		}

		// handler: CatchClause | null;
		{
			println("<handler>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "handler", null);
			JsonValue jvHandler = jo.get("handler");
			if (jvHandler.getValueType() != ValueType.OBJECT && jvHandler.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvHandler.getValueType() + " for handler");
			}
			if (jvHandler.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvHandler);
			}
			println("</handler>");
			bodyEncoder.encodeEndElement();
		}

		// guardedHandlers: [ CatchClause ];
		{
			println("<guardedHandlers>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "guardedHandlers", null);
			JsonValue jvGuardedHandlers = jo.get("guardedHandlers");
			if (jvGuardedHandlers.getValueType() != ValueType.ARRAY) {
				throw new RuntimeException(
						"Unsupported ValueType " + jvGuardedHandlers.getValueType() + " for guardedHandlers");
			}
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			JsonArray jaParams = (JsonArray) jvGuardedHandlers;
			Iterator<JsonValue> iter = jaParams.iterator();
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					handleNode((JsonObject) jv);
				} else {
					throw new RuntimeException("Unexpected ValueType in params array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</guardedHandlers>");
			bodyEncoder.encodeEndElement();
		}

		// finalizer: BlockStatement | null;
		{
			println("<finalizer>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "finalizer", null);
			JsonValue jvFinalizer = jo.get("finalizer");
			if (jvFinalizer.getValueType() != ValueType.OBJECT && jvFinalizer.getValueType() != ValueType.NULL) {
				throw new RuntimeException("Unsupported ValueType " + jvFinalizer.getValueType() + " for finalizer");
			}
			if (jvFinalizer.getValueType() == ValueType.NULL) {
				println("<null>");
				bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
				println("</null>");
				bodyEncoder.encodeEndElement();
			} else {
				handleNode((JsonObject) jvFinalizer);
			}
			println("</finalizer>");
			bodyEncoder.encodeEndElement();
		}

		println("</TryStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface UpdateExpression <: Expression {
	// type: "UpdateExpression";
	// operator: UpdateOperator;
	// argument: Expression;
	// prefix: boolean;
	// }
	void handleUpdateExpression(JsonObject jo) throws EXIException, IOException {
		println("<UpdateExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "UpdateExpression", null);

		// operator: UpdateOperator;
		{
			println("<operator>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "operator", null);
			JsonValue jvOperator = jo.get("operator");
			if (jvOperator.getValueType() != ValueType.STRING) {
				throw new RuntimeException("Unsupported ValueType " + jvOperator.getValueType() + " for operator");
			}
			JsonString js = (JsonString) jvOperator;
			println(js.getString());
			bodyEncoder.encodeCharacters(new StringValue(js.getString()));
			println("</operator>");
			bodyEncoder.encodeEndElement();
		}

		// argument: Expression;
		{
			println("<argument>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "argument", null);
			JsonValue jvArgument = jo.get("argument");
			if (jvArgument.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvArgument.getValueType() + " for argument");
			}
			handleNode((JsonObject) jvArgument);
			println("</argument>");
			bodyEncoder.encodeEndElement();
		}

		// prefix: boolean;
		{
			println("<prefix>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "prefix", null);

			JsonValue jvPrefix = jo.get("prefix");
			if (jvPrefix.getValueType() != ValueType.TRUE && jvPrefix.getValueType() != ValueType.FALSE) {
				throw new RuntimeException("Unsupported ValueType " + jvPrefix.getValueType() + " for prefix");
			}
			if (jvPrefix.getValueType() == ValueType.TRUE) {
				println("true");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_TRUE);
			} else {
				println("false");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_FALSE);
			}
			println("</prefix>");
			bodyEncoder.encodeEndElement();
		}

		println("</UpdateExpression>");
		bodyEncoder.encodeEndElement();
	}

	// interface UnaryExpression <: Expression {
	// type: "UnaryExpression";
	// operator: UnaryOperator;
	// prefix: boolean;
	// argument: Expression;
	// }
	void handleUnaryExpression(JsonObject jo) throws EXIException, IOException {
		println("<UnaryExpression>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "UnaryExpression", null);

		// operator: UnaryOperator;
		{
			println("<operator>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "operator", null);
			JsonValue jvOperator = jo.get("operator");
			if (jvOperator.getValueType() != ValueType.STRING) {
				throw new RuntimeException("Unsupported ValueType " + jvOperator.getValueType() + " for operator");
			}
			JsonString js = (JsonString) jvOperator;
			println(js.getString());
			bodyEncoder.encodeCharacters(new StringValue(js.getString()));
			println("</operator>");
			bodyEncoder.encodeEndElement();
		}

		// prefix: boolean;
		{
			println("<prefix>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "prefix", null);

			JsonValue jvPrefix = jo.get("prefix");
			if (jvPrefix.getValueType() != ValueType.TRUE && jvPrefix.getValueType() != ValueType.FALSE) {
				throw new RuntimeException("Unsupported ValueType " + jvPrefix.getValueType() + " for prefix");
			}
			if (jvPrefix.getValueType() == ValueType.TRUE) {
				println("true");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_TRUE);
			} else {
				println("false");
				bodyEncoder.encodeCharacters(BooleanValue.BOOLEAN_VALUE_FALSE);
			}
			println("</prefix>");
			bodyEncoder.encodeEndElement();
		}

		// argument: Expression;
		{
			println("<argument>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "argument", null);
			JsonValue jvArgument = jo.get("argument");
			if (jvArgument.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvArgument.getValueType() + " for argument");
			}
			handleNode((JsonObject) jvArgument);
			println("</argument>");
			bodyEncoder.encodeEndElement();
		}

		println("</UnaryExpression>");
		bodyEncoder.encodeEndElement();
	}

	void handleVariableDeclarator(JsonObject jo) throws EXIException, IOException {
		println("<VariableDeclarator>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "VariableDeclarator", null);

		JsonValue jsId = jo.get("id"); // Pattern
		if (jsId.getValueType() != ValueType.OBJECT) {
			throw new RuntimeException("Unsupported ValueType for id " + jsId.getValueType());
		}
		println("<id>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "id", null);
		handleNode((JsonObject) jsId);
		println("</id>");
		bodyEncoder.encodeEndElement();

		JsonValue jsInit = jo.get("init");
		println("<init>"); // Expression | null;
		bodyEncoder.encodeStartElement(JSConstants.URI, "init", null);
		if (jsInit.getValueType() == ValueType.NULL) {
			println("<null>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "null", null);
			println("</null>");
			bodyEncoder.encodeEndElement();
		} else if (jsInit.getValueType() == ValueType.OBJECT) {
			handleNode((JsonObject) jsInit);
		} else {
			throw new RuntimeException("Unsupported type " + jsInit.getValueType() + " for init");
		}
		println("</init>");
		bodyEncoder.encodeEndElement();

		println("</VariableDeclarator>");
		bodyEncoder.encodeEndElement();
	}

	void handleVariableDeclaration(JsonObject jo) throws EXIException, IOException {
		println("<VariableDeclaration>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "VariableDeclaration", null);

		JsonValue jsDecls = jo.get("declarations");
		if (jsDecls.getValueType() == ValueType.ARRAY) {
			JsonArray ja = (JsonArray) jsDecls;
			Iterator<JsonValue> iter = ja.iterator();
			println("<declarations>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "declarations", null);
			println("<array>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "array", null);
			while (iter.hasNext()) {
				JsonValue jv = iter.next();
				if (jv.getValueType() == ValueType.OBJECT) {
					handleNode((JsonObject) jv);
				} else {
					throw new RuntimeException("Unexpected ValueType in declarations array: " + jv.getValueType());
				}
			}
			println("</array>");
			bodyEncoder.encodeEndElement();
			println("</declarations>");
			bodyEncoder.encodeEndElement();
		} else {
			throw new RuntimeException("Unexpected ValueType in declarations: " + jsDecls.getValueType());
		}

		
		// kind seems to be optional
		if( jo.containsKey("kind")) {
			bodyEncoder.encodeStartElement(JSConstants.URI, "kind", null);
			// String s = jo.getString("kind", "var");
			String s = jo.getString("kind");
			println("<kind>" + s + "</kind>");	
			// println("<string>" + s + "</string>");
			// bodyEncoder.encodeStartElement(JSConstants.URI, "string", null);
			bodyEncoder.encodeCharacters(new StringValue(s));
			// bodyEncoder.encodeEndElement();
			// println("</kind>");
			bodyEncoder.encodeEndElement();
		}

		println("</VariableDeclaration>");
		bodyEncoder.encodeEndElement();
	}

	// interface WhileStatement <: Statement {
	// type: "WhileStatement";
	// test: Expression;
	// body: Statement;
	// }
	void handleWhileStatement(JsonObject jo) throws EXIException, IOException {
		println("<WhileStatement>");
		bodyEncoder.encodeStartElement(JSConstants.URI, "WhileStatement", null);

		// test: Expression;
		{
			println("<test>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "test", null);
			JsonValue jvTest = jo.get("test");
			if (jvTest.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvTest.getValueType() + " for test");
			}
			handleNode((JsonObject) jvTest);
			println("</test>");
			bodyEncoder.encodeEndElement();
		}

		// body: Statement;
		{
			println("<body>");
			bodyEncoder.encodeStartElement(JSConstants.URI, "body", null);
			JsonValue jvBody = jo.get("body");
			if (jvBody.getValueType() != ValueType.OBJECT) {
				throw new RuntimeException("Unsupported ValueType " + jvBody.getValueType() + " for body");
			}
			handleNode((JsonObject) jvBody);
			println("</body>");
			bodyEncoder.encodeEndElement();
		}

		println("</WhileStatement>");
		bodyEncoder.encodeEndElement();
	}

	// interface Expression <: Node, Pattern { }
	// TODO interface Pattern <: Node { } --> ObjectPattern or ArrayPattern
	void handleNode(JsonObject jo) throws EXIException, IOException {
		handleNode(jo, null);
	}

	void handleNode(JsonObject jo, String type) throws EXIException, IOException {
		if (!jo.containsKey("type") && type == null) {
			throw new RuntimeException("No key type in Node");
		}
		JsonString js = jo.getJsonString("type");
		if (js != null) {
			type = js.getString();
		}
		// String type = js.getString();

		switch (type) {
		case "ArrayExpression":
			this.handleArrayExpression(jo);
			break;
		case "AssignmentExpression":
			this.handleAssignmentExpression(jo);
			break;
		case "BinaryExpression":
			this.handleBinaryExpression(jo);
			break;
		case "BlockStatement":
			this.handleBlockStatement(jo);
			break;
		case "BreakStatement":
			this.handleBreakStatement(jo);
			break;
		case "CallExpression":
			this.handleCallExpression(jo);
			break;
		case "ContinueStatement":
			this.handleContinueStatement(jo);
			break;
		case "CatchClause":
			this.handleCatchClause(jo);
			break;
		case "ConditionalExpression":
			this.handleConditionalExpression(jo);
			break;
		case "DoWhileStatement":
			this.handleDoWhileStatement(jo);
			break;
		case "ExpressionStatement":
			this.handleExpressionStatement(jo);
			break;
		case "ForInStatement":
			this.handleForInStatement(jo);
			break;
		case "ForStatement":
			this.handleForStatement(jo);
			break;
		case "FunctionDeclaration":
			this.handleFunctionDeclaration(jo);
			break;
		case "FunctionExpression":
			this.handleFunctionExpression(jo);
			break;
		case "Identifier":
			this.handleIdentifier(jo);
			break;
		case "IfStatement":
			this.handleIfStatement(jo);
			break;
		case "LabeledStatement":
			this.handleLabeledStatement(jo);
			break;
		case "Literal":
			this.handleLiteral(jo);
			break;
		case "LogicalExpression":
			this.handleLogicalExpression(jo);
			break;
		case "MemberExpression":
			this.handleMemberExpression(jo);
			break;
		case "NewExpression":
			this.handleNewExpression(jo);
			break;
		case "ObjectExpression":
			this.handleObjectExpression(jo);
			break;
		case "Property":
			this.handleProperty(jo);
			break;
		case "ReturnStatement":
			this.handleReturnStatement(jo);
			break;
		case "SwitchCase":
			this.handleSwitchCase(jo);
			break;
		case "SwitchStatement":
			this.handleSwitchStatement(jo);
			break;
		case "ThisExpression":
			this.handleThisExpression(jo);
			break;
		case "ThrowStatement":
			this.handleThrowStatement(jo);
			break;
		case "TryStatement":
			this.handleTryStatement(jo);
			break;
		case "UpdateExpression":
			this.handleUpdateExpression(jo);
			break;
		case "UnaryExpression":
			this.handleUnaryExpression(jo);
			break;
		case "VariableDeclaration":
			this.handleVariableDeclaration(jo);
			break;
		case "VariableDeclarator":
			this.handleVariableDeclarator(jo);
			break;
		case "WhileStatement":
			this.handleWhileStatement(jo);
			break;
		default:
			throw new RuntimeException("Unsupported type " + type + " in " + jo + "!");
		}
	}

	public static void main(String[] args) throws IOException, ScriptException, EXIException {
		JStoEXI js2exi = new JStoEXI();
		js2exi.setDebug(System.out);

		String sin = "./src/test/resources/animals.js";
		// String sin = "./src/test/resources/browserDetection.js";
		// String sin = "./src/test/resources/xCryptic.app.js";
		// String sin = "./src/test/resources/jquery.js";
		// String sin = "./src/test/resources/jquery.min.js";
		// String sin = "./src/test/resources/angular2.js";
		// String sin = "./src/test/resources/angular2.min.js";
		// String sin = "./src/test/resources/react.js";
		// String sin = "./src/test/resources/react.min.js";
		String jsCode = new String(Files.readAllBytes(Paths.get(sin)));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		js2exi.generate(JStoAST.getAST(jsCode), baos);

		System.out.println("From JavaScript " + jsCode.length() + " Bytes to " + baos.size() + " in EXI4JS");
		
		if(true) {
			File f = File.createTempFile("foo", ".exi");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(baos.toByteArray());
			fos.close();
			System.out.println("Written to " + f);
		}
	}

}
