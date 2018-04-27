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

import java.io.InputStream;

import com.siemens.ct.exi.core.CodingMode;
import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.EncodingOptions;
import com.siemens.ct.exi.core.FidelityOptions;
import com.siemens.ct.exi.core.exceptions.EXIException;
import com.siemens.ct.exi.core.grammars.Grammars;
import com.siemens.ct.exi.core.helpers.DefaultEXIFactory;
import com.siemens.ct.exi.grammars.GrammarFactory;

public class JSConstants {
	
	public static final String XSD_LOCATION = "/exi4js.xsd";
	public static Grammars EXI_FOR_JS_GRAMMARS;
	public static EXIFactory EXI_FACTORY;
	public static EXIFactory EXI_FACTORY_COMPRESSION;
	public static EXIFactory EXI_FACTORY_PRE_COMPRESSION;
	public static EXIFactory EXI_FACTORY_BYTE_PACKED;

	static {
		try {
			InputStream isXsd = JSConstants.class.getResourceAsStream(JSConstants.XSD_LOCATION);
			EXI_FOR_JS_GRAMMARS = GrammarFactory.newInstance().createGrammars(isXsd);
			
			EXI_FACTORY = DefaultEXIFactory.newInstance();
			EXI_FACTORY.setFidelityOptions(FidelityOptions.createStrict());
			EXI_FACTORY.setGrammars(JSConstants.EXI_FOR_JS_GRAMMARS); // use XML schema
			
			EXI_FACTORY_COMPRESSION = DefaultEXIFactory.newInstance();
			EXI_FACTORY_COMPRESSION.setFidelityOptions(FidelityOptions.createStrict());
			EXI_FACTORY_COMPRESSION.setGrammars(JSConstants.EXI_FOR_JS_GRAMMARS); // use XML schema
			EXI_FACTORY_COMPRESSION.setCodingMode(CodingMode.COMPRESSION); // use deflate compression for larger XML files
			EXI_FACTORY_COMPRESSION.getEncodingOptions().setOption(EncodingOptions.DEFLATE_COMPRESSION_VALUE, java.util.zip.Deflater.BEST_COMPRESSION);
			
			EXI_FACTORY_PRE_COMPRESSION = DefaultEXIFactory.newInstance();
			EXI_FACTORY_PRE_COMPRESSION.setFidelityOptions(FidelityOptions.createStrict());
			EXI_FACTORY_PRE_COMPRESSION.setGrammars(JSConstants.EXI_FOR_JS_GRAMMARS); // use XML schema
			EXI_FACTORY_PRE_COMPRESSION.setCodingMode(CodingMode.PRE_COMPRESSION); // use pre-compression for following generic compression
			
			EXI_FACTORY_BYTE_PACKED = DefaultEXIFactory.newInstance();
			EXI_FACTORY_BYTE_PACKED.setFidelityOptions(FidelityOptions.createStrict());
			EXI_FACTORY_BYTE_PACKED.setGrammars(JSConstants.EXI_FOR_JS_GRAMMARS); // use XML schema
			EXI_FACTORY_BYTE_PACKED.setCodingMode(CodingMode.BYTE_PACKED);
			
		} catch (EXIException e) {
			System.err.println("Not able to load EXI grammars from " + XSD_LOCATION);
		}
	}
	
	static final String URI = "urn:javascript";
	
}
