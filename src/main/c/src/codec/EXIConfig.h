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
 * \file 	EXIConfig.h
 * \brief 	EXI Configurations for the EXI Codec
 *
 */

#ifndef EXI_CONFIG_H
#define EXI_CONFIG_H

#ifdef __cplusplus
extern "C" {
#endif


/** EXI stream - Option Byte Array */
#define BYTE_ARRAY 1
/** EXI stream - Option File */
#define FILE_STREAM 2
/** \brief 	EXI stream
 *
 * 			Byte array or file
 * */
#define EXI_STREAM FILE_STREAM





/** Memory allocation - static */
#define STATIC_ALLOCATION 1
/** Memory allocation - dynamic */
#define DYNAMIC_ALLOCATION  2
/** */
/** \brief 	Memory allocation mode
 *
 * 			static or dynamic memory allocation
 * */
#define MEMORY_ALLOCATION DYNAMIC_ALLOCATION



/** String representation ASCII */
#define STRING_REPRESENTATION_ASCII 1
/** String representation Universal Character Set (UCS) */
#define STRING_REPRESENTATION_UCS 2
/** */
/** \brief 	String representation mode
 *
 * 			ASCII or UCS
 * */
#define STRING_REPRESENTATION STRING_REPRESENTATION_UCS



/** Maximum number of cascading elements, XML tree depth */
#define EXI_ELEMENT_STACK_SIZE 128


#ifdef __cplusplus
}
#endif

#endif /* EXI_CONFIG_H */

