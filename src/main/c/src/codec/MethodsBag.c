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



#ifndef METHODS_BAG_C
#define METHODS_BAG_C

#include "MethodsBag.h"
#include "ErrorCodes.h"

static const uint16_t smallLengths[] = { 0, 0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4,
		4, 4, 4 };

int exiGetCodingLength(size_t characteristics, size_t* codingLength) {
	/* Note: we could use range expressions in switch statements but those are non-standard */
	/* e.g., case 1 ... 5: */
	int errn = 0;
	if (characteristics < 17) {
		*codingLength = smallLengths[characteristics];
	} else if (characteristics < 33) {
		/* 17 .. 32 */
		*codingLength = 5;
	} else if (characteristics < 65) {
		/* 33 .. 64 */
		*codingLength = 6;
	} else if (characteristics < 129) {
		/* 65 .. 128 */
		*codingLength = 7;
	} else if (characteristics < 257) {
		/* 129 .. 256 */
		*codingLength = 8;
	} else if (characteristics < 513) {
		/* 257 .. 512 */
		*codingLength = 9;
	} else if (characteristics < 1025) {
		/* 513 .. 1024 */
		*codingLength = 10;
	} else if (characteristics < 2049) {
		/* 1025 .. 2048 */
		*codingLength = 11;
	} else if (characteristics < 4097) {
		/* 2049 .. 4096 */
		*codingLength = 12;
	} else if (characteristics < 8193) {
		/* 4097 .. 8192 */
		*codingLength = 13;
	} else if (characteristics < 16385) {
		/* 8193 .. 16384 */
		*codingLength = 14;
	} else if (characteristics < 32769) {
		/* 16385 .. 32768 */
		*codingLength = 15;
	} else {
		/* 32769 .. 65536 */
		*codingLength = 16;
	}
	return errn;
}


uint8_t numberOf7BitBlocksToRepresent(uint32_t n) {
	/* assert (n >= 0); */

	/* 7 bits */
	if (n < 128) {
		return 1;
	}
	/* 14 bits */
	else if (n < 16384) {
		return 2;
	}
	/* 21 bits */
	else if (n < 2097152) {
		return 3;
	}
	/* 28 bits */
	else if (n < 268435456) {
		return 4;
	}
	/* 35 bits */
	else {
		/* int, 32 bits */
		return 5;
	}
}



#endif

