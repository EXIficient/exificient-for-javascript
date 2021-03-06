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
* \file 	MethodsBag.h
* \brief 	Method bag for bit and octet functions
*
*/

#ifndef METHODS_BAG_H
#define METHODS_BAG_H



#include <stdint.h>
#include <stddef.h>

/**
 * \brief  	Returns the number of bits to identify the characteristics.
 *
 * \param       characteristics	number of characteristics
 * \param       codingLength   	number of bits
 * \return                  	Error-Code <> 0
 *
 */
int exiGetCodingLength(size_t characteristics, size_t* codingLength);


/**
 * \brief  	Returns the least number of 7 bit-blocks that is needed to represent the passed integer value
 *
 *			Note: Returns 1 if passed parameter is 0.
 *
 * \param       n				integer value
 * \return                  	Error-Code <> 0
 *
 */
uint8_t numberOf7BitBlocksToRepresent(uint32_t n);


#endif

