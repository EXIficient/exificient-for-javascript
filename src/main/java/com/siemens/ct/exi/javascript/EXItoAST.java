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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import com.siemens.ct.exi.core.EXIBodyDecoder;
import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.EXIStreamDecoder;
import com.siemens.ct.exi.core.context.QNameContext;
import com.siemens.ct.exi.core.exceptions.EXIException;
import com.siemens.ct.exi.core.grammars.event.EventType;
import com.siemens.ct.exi.core.values.FloatValue;
import com.siemens.ct.exi.core.values.IntegerValue;
import com.siemens.ct.exi.core.values.Value;
import com.siemens.ct.exi.core.values.ValueType;

public class EXItoAST {
	
	EXIFactory ef;
	
	public EXItoAST() {
		this(JSConstants.EXI_FACTORY);
	}
	
	public EXItoAST(EXIFactory ef) {
		this.ef = ef;
		
		if(ef.getGrammars().isSchemaInformed()) {
			// schema-informed grammars (dedicated grammars in use)
		} else {
			// setup EXI schema/grammars
			ef.setGrammars(JSConstants.EXI_FOR_JS_GRAMMARS);
		}
	}
	
	public void generate(InputStream is, OutputStream os) throws IOException, EXIException {
		EXIStreamDecoder streamDecoder = ef.createEXIStreamDecoder();
		EXIBodyDecoder bodyDecoder = streamDecoder.decodeHeader(is);
		
		JsonGenerator generator = Json.createGenerator(new OutputStreamWriter(os));
		
		EventType eventType;
		
		String prevLocalName = null;
		String prePrevLocalName = null;
		
		while ((eventType = bodyDecoder.next()) != null) {
			switch (eventType) {
			/* DOCUMENT */
			case START_DOCUMENT:
				bodyDecoder.decodeStartDocument();
				break;
			case END_DOCUMENT:
				bodyDecoder.decodeEndDocument();
				break;
			case START_ELEMENT:
			case START_ELEMENT_NS:
			case START_ELEMENT_GENERIC:
			case START_ELEMENT_GENERIC_UNDECLARED:
				// defer start element and keep on processing
				QNameContext se = bodyDecoder.decodeStartElement();
				
				String localName = se.getLocalName();
				System.out.println("localName = " + localName);
				
				if("init".equals(localName)) {
					System.err.println("XX");
				}
				
				switch(localName) {
				case "Program":
				case "VariableDeclaration":
				case "VariableDeclarator":
					// part of array or root?
					generator.writeStartObject();
					generator.write("type", localName);
					break;
				case "Literal":
				case "Identifier":
				case "BinaryExpression":
					// Do nothing, string/content comes afterwards, add type
					generator.write("type", localName);
					break;
				case "string":
				case "boolean":
				case "integer":
				case "number":
				case "RegExp":
					// TODO not sure about null
					// Do nothing, just surrounder
				case "kind":
					break;
				case "body":
				case "declarations":
					// Do nothing, array comes afterwards (see array)
					break;
				case "name":
				case "value":
				case "operator":
					// Do nothing, string/content comes afterwards
					break;
				case "array":
					// generator.writeStartArray();
					generator.writeStartArray(prevLocalName);
					break;
				default:
					generator.writeStartObject(localName);
				}
				prePrevLocalName = prevLocalName;
				prevLocalName = localName;
				break;
			case END_ELEMENT:
			case END_ELEMENT_UNDECLARED:
				// String eeQNameAsString = bodyDecoder.getElementQNameAsString();
				QNameContext eeQName = bodyDecoder.decodeEndElement();
				String eelocalName = eeQName.getLocalName();
				switch(eelocalName) {
				case "Literal":
				case "Identifier":
				case "BinaryExpression":
					//
				case "string":
				case "boolean":
				case "integer":
				case "number":
				case "RegExp":
					//
				case "kind":
					// 
				case "body":
				case "declarations":
					// 
				case "name":
				case "value":
				case "operator":
					// do nothing
					break;
				default:
					generator.writeEnd();	
				}
				break;
			case CHARACTERS:
			case CHARACTERS_GENERIC:
			case CHARACTERS_GENERIC_UNDECLARED:
				Value val = bodyDecoder.decodeCharacters();
				
				String vLocalName = prevLocalName;
				switch(vLocalName) {
				case "string":
				case "boolean":
				case "integer":
				case "number":
				case "RegExp":
					vLocalName = prePrevLocalName;
				}
				
				if(val.getValueType() == ValueType.FLOAT) {
					FloatValue fv = (FloatValue) val;
					generator.write(vLocalName, new BigDecimal(fv.getMantissa() + "E" + fv.getExponent()));
				} else if(val.getValueType() == ValueType.INTEGER) {
					IntegerValue iv = (IntegerValue) val;
					generator.write(vLocalName, iv.intValue());
				} else {
					generator.write(vLocalName, val.toString());
				}
				break;
			default:
				throw new RuntimeException("Unsupported eventType " + eventType);
				// break;
			}
		}
		
		generator.flush();
		
//		BufferedWriter writer = new BufferedWriter(new FileWriter(fCSS));
//		writer.write(sb.toString());
//		writer.close();
	}

	// TODO JSON comparison https://github.com/lukas-krecan/JsonUnit
	// http://stackoverflow.com/questions/2253750/compare-two-json-objects-in-java
	public static void main(String[] args) throws IOException, EXIException {
		// encode first JS to EXI4JS
		String sin = "./src/test/resources/animals.js";
		String jsCode = new String(Files.readAllBytes(Paths.get(sin)));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JStoEXI js2exi = new JStoEXI();
		js2exi.generate(JStoAST.getAST(jsCode), baos);

		System.out.println("From JavaScript " + jsCode.length() + " Bytes to " + baos.size() + " in EXI4JS");
		
		// reconstruct JS AST
		EXItoAST exi2ast = new EXItoAST();
		ByteArrayOutputStream baosAST = new ByteArrayOutputStream();
		exi2ast.generate(new ByteArrayInputStream(baos.toByteArray()), baosAST);
		
		System.out.println(baosAST.toString());
		
	}

}
