package com.siemens.ct.exi.javascript;

import java.io.InputStream;

import com.siemens.ct.exi.CodingMode;
import com.siemens.ct.exi.EXIFactory;
import com.siemens.ct.exi.EncodingOptions;
import com.siemens.ct.exi.FidelityOptions;
import com.siemens.ct.exi.GrammarFactory;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.grammars.Grammars;
import com.siemens.ct.exi.helpers.DefaultEXIFactory;

public class JSConstants {
	
	public static final String XSD_LOCATION = "/exi4js.xsd";
	public static Grammars EXI_FOR_CSS_GRAMMARS;
	public static EXIFactory EXI_FACTORY;
	public static EXIFactory EXI_FACTORY_COMPRESSION;
	public static EXIFactory EXI_FACTORY_PRE_COMPRESSION;

	static {
		try {
			InputStream isXsd = JSConstants.class.getResourceAsStream(JSConstants.XSD_LOCATION);
			EXI_FOR_CSS_GRAMMARS = GrammarFactory.newInstance().createGrammars(isXsd);
			
			EXI_FACTORY = DefaultEXIFactory.newInstance();
			EXI_FACTORY.setFidelityOptions(FidelityOptions.createStrict());
			EXI_FACTORY.setGrammars(JSConstants.EXI_FOR_CSS_GRAMMARS); // use XML schema
			
			EXI_FACTORY_COMPRESSION = DefaultEXIFactory.newInstance();
			EXI_FACTORY_COMPRESSION.setFidelityOptions(FidelityOptions.createStrict());
			EXI_FACTORY_COMPRESSION.setGrammars(JSConstants.EXI_FOR_CSS_GRAMMARS); // use XML schema
			EXI_FACTORY_COMPRESSION.setCodingMode(CodingMode.COMPRESSION); // use deflate compression for larger XML files
			EXI_FACTORY_COMPRESSION.getEncodingOptions().setOption(EncodingOptions.DEFLATE_COMPRESSION_VALUE, java.util.zip.Deflater.BEST_COMPRESSION);
			
			EXI_FACTORY_PRE_COMPRESSION = DefaultEXIFactory.newInstance();
			EXI_FACTORY_PRE_COMPRESSION.setFidelityOptions(FidelityOptions.createStrict());
			EXI_FACTORY_PRE_COMPRESSION.setGrammars(JSConstants.EXI_FOR_CSS_GRAMMARS); // use XML schema
			EXI_FACTORY_PRE_COMPRESSION.setCodingMode(CodingMode.PRE_COMPRESSION); // use pre-compression for following generic compression
			
		} catch (EXIException e) {
			System.err.println("Not able to load EXI grammars from " + XSD_LOCATION);
		}
	}
	
	static final String URI = "urn:javascript";
	
}
