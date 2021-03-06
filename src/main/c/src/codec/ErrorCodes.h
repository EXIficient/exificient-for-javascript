/*
 * Copyright (C) 2007-2016 Siemens AG
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*******************************************************************
 *
 * @author Daniel.Peintner.EXT@siemens.com
 * @version 2016-10-20 
 * @contact Joerg.Heuer@siemens.com
 *
 * <p>Code generated by EXIdizer</p>
 * <p>Schema: exi4js.xsd.</p>
 *
 *
 ********************************************************************/



/**
 * \file 	ErrorCodes.h
 * \brief 	Error Codes descriptions
 *
 */

#ifndef EXI_ERROR_CODES_H
#define EXI_ERROR_CODES_H

#ifdef __cplusplus
extern "C" {
#endif

#define EXI_ERROR_INPUT_STREAM_EOF -10
#define EXI_ERROR_OUTPUT_STREAM_EOF -11
#define EXI_ERROR_INPUT_FILE_HANDLE -12
#define EXI_ERROR_OUTPUT_FILE -13

#define EXI_ERROR_OUT_OF_BOUNDS -100
#define EXI_ERROR_OUT_OF_STRING_BUFFER -101
/*#define EXI_ERROR_OUT_OF_ASCII_BUFFER -102 */
#define EXI_ERROR_OUT_OF_BYTE_BUFFER -103
#define EXI_ERROR_OUT_OF_GRAMMAR_STACK -104
#define EXI_ERROR_OUT_OF_RUNTIME_GRAMMAR_STACK -105
#define EXI_ERROR_OUT_OF_QNAMES -106

#define EXI_ERROR_UNKOWN_GRAMMAR_ID -108
#define EXI_ERROR_UNKOWN_EVENT -109
#define EXI_ERROR_UNKOWN_EVENT_CODE -110
#define EXI_ERROR_UNEXPECTED_EVENT_LEVEL1 -111
#define EXI_ERROR_UNEXPECTED_EVENT_LEVEL2 -112

#define EXI_ERROR_UNEXPECTED_START_DOCUMENT -113
#define EXI_ERROR_UNEXPECTED_END_DOCUMENT -114
#define EXI_ERROR_UNEXPECTED_START_ELEMENT -115
#define EXI_ERROR_UNEXPECTED_START_ELEMENT_NS -116
#define EXI_ERROR_UNEXPECTED_START_ELEMENT_GENERIC -117
#define EXI_ERROR_UNEXPECTED_START_ELEMENT_GENERIC_UNDECLARED -118
#define EXI_ERROR_UNEXPECTED_END_ELEMENT -119
#define EXI_ERROR_UNEXPECTED_CHARACTERS -120
#define EXI_ERROR_UNEXPECTED_ATTRIBUTE -121
#define EXI_ERROR_UNEXPECTED_ATTRIBUTE_NS -122
#define EXI_ERROR_UNEXPECTED_ATTRIBUTE_GENERIC -123
#define EXI_ERROR_UNEXPECTED_ATTRIBUTE_GENERIC_UNDECLARED -124
#define EXI_ERROR_UNEXPECTED_ATTRIBUTE_XSI_TYPE -125
#define EXI_ERROR_UNEXPECTED_ATTRIBUTE_XSI_NIL -126
#define EXI_ERROR_UNEXPECTED_GRAMMAR_ID -127
#define EXI_ERROR_UNEXPECTED_ATTRIBUTE_MOVE_TO_CONTENT_RULE -128

#define EXI_UNSUPPORTED_NBIT_INTEGER_LENGTH -132
#define EXI_UNSUPPORTED_EVENT_CODE_CHARACTERISTICS -133
#define EXI_UNSUPPORTED_INTEGER_VALUE -134
#define EXI_NEGATIVE_UNSIGNED_INTEGER_VALUE -135
#define EXI_UNSUPPORTED_LIST_VALUE_TYPE -136
#define EXI_UNSUPPORTED_HEADER_COOKIE -137
#define EXI_UNSUPPORTED_HEADER_OPTIONS -138

#define EXI_UNSUPPORTED_GLOBAL_ATTRIBUTE_VALUE_TYPE -139
#define EXI_UNSUPPORTED_DATATYPE -140
#define EXI_UNSUPPORTED_STRING_VALUE_TYPE -141
#define EXI_UNSUPPORTED_INTEGER_VALUE_TYPE -142
#define EXI_UNSUPPORTED_DATETIME_TYPE -143
#define EXI_UNSUPPORTED_FRAGMENT_ELEMENT -144

#define EXI_UNSUPPORTED_GRAMMAR_LEARNING_CH -150

/* string values */
#define EXI_ERROR_STRINGVALUES_NOT_SUPPORTED -160
#define EXI_ERROR_STRINGVALUES_OUT_OF_ENTRIES -161
#define EXI_ERROR_STRINGVALUES_OUT_OF_MEMORY -162
#define EXI_ERROR_STRINGVALUES_OUT_OF_BOUND -163
#define EXI_ERROR_STRINGVALUES_CHARACTER -164

#define EXI_ERROR_UNEXPECTED_BYTE_VALUE -200


#define EXI_ERROR_CONVERSION_NO_ASCII_CHARACTERS -300
#define EXI_ERROR_CONVERSION_TYPE_TO_STRING -301


#define EXI_DEVIANT_SUPPORT_NOT_DEPLOYED -500

#ifdef __cplusplus
}
#endif


#endif /* EXI_ERROR_CODES_H */

