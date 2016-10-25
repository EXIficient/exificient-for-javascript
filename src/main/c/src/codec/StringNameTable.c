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



#ifndef STRING_NAME_TABLE_C
#define STRING_NAME_TABLE_C

#include <string.h>

#include "StringNameTable.h"
#include "EXIforJSEXICoder.h"
#include "ErrorCodes.h"


#ifndef __GNUC__
#pragma warning( disable : 4100 ) /* warning unreferenced parameter 'runtimeTable' */
#endif /* __GNUC__ */
int exiGetUriSize(exi_name_table_prepopulated_t* prepopulatedTable, exi_name_table_runtime_t* runtimeTable, size_t* uriLength) {
	*uriLength = (prepopulatedTable->len + runtimeTable->addedUriEntries);
	return 0;
}
#ifndef __GNUC__
#pragma warning( default : 4100 ) /* warning unreferenced parameter 'runtimeTable' */
#endif /* __GNUC__ */


int exiGetLocalNameSize(exi_name_table_prepopulated_t* prepopulatedTable, exi_name_table_runtime_t* runtimeTable,
		size_t uriID, size_t* localNameLength) {
	int errn = 0;
	*localNameLength = 0;
	/* 1. pre-populated entries*/
	if (uriID < prepopulatedTable->len) {
		(*localNameLength) = (uint16_t)(*localNameLength + prepopulatedTable->localNames[uriID]);
	} else {
		/* range check */
		if (uriID >= ( prepopulatedTable->len + runtimeTable->addedUriEntries )) {
			errn = EXI_ERROR_OUT_OF_BOUNDS;
		}
	}
	/* 2. runtime entries */
	if (errn == 0 && runtimeTable->addedLocalNameEntries > 0 ) {
		unsigned int i;
		for(i=0; i<(runtimeTable->addedUriEntries+runtimeTable->addedLocalNameEntries); i++) {
			if ( runtimeTable->namePartitionsEntries[i].namePartitionType == EXI_NAME_PARTITION_LOCALNAME &&
					runtimeTable->namePartitionsEntries[i].entry.localNamePartition.uriID == uriID ) {
				(*localNameLength)++;
			}
		}
	}

	return errn;
}


/* inline */
/*
static int _max(int a, int b) {
	return (a > b) ? a : b;
}
*/

int exiInitNameTableRuntime(exi_name_table_runtime_t* runtimeTable) {
	/*runtimeTable->numberOfUsedCharacters = 0;*/
	runtimeTable->addedLocalNameEntries = 0;
	runtimeTable->addedUriEntries = 0;
	return 0;
}

int exiAddUri(exi_name_table_prepopulated_t* prepopulatedTable, exi_name_table_runtime_t* runtimeTable) {
	uint16_t index = (uint16_t)(runtimeTable->addedUriEntries + runtimeTable->addedLocalNameEntries);
	runtimeTable->namePartitionsEntries[index].namePartitionType = EXI_NAME_PARTITION_URI;
	runtimeTable->namePartitionsEntries[index].entry.uriPartition.uriID = (uint16_t)(prepopulatedTable->len + runtimeTable->addedUriEntries);
	runtimeTable->addedUriEntries++;
	return 0;
}

int exiAddLocalName(exi_name_table_prepopulated_t* prepopulatedTable, exi_name_table_runtime_t* runtimeTable, size_t uriID, size_t* localNameID) {
	uint16_t index = (uint16_t)(runtimeTable->addedUriEntries + runtimeTable->addedLocalNameEntries);
	int errn = exiGetLocalNameSize(prepopulatedTable, runtimeTable, uriID, localNameID);
	if (errn == 0) {
		runtimeTable->namePartitionsEntries[index].namePartitionType = EXI_NAME_PARTITION_LOCALNAME;
		runtimeTable->namePartitionsEntries[index].entry.localNamePartition.localNameID = *localNameID;
		runtimeTable->namePartitionsEntries[index].entry.localNamePartition.uriID = uriID;
		runtimeTable->addedLocalNameEntries++;
	}
	return errn;
}

#endif

