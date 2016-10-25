################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/codec/BitInputStream.c \
../src/codec/BitOutputStream.c \
../src/codec/ByteStream.c \
../src/codec/DecoderChannel.c \
../src/codec/DynamicMemory.c \
../src/codec/EXIHeaderDecoder.c \
../src/codec/EXIHeaderEncoder.c \
../src/codec/EXIforJSEXICoder.c \
../src/codec/EXIforJSEXIDecoder.c \
../src/codec/EXIforJSEXIEncoder.c \
../src/codec/EXIforJSQNames.c \
../src/codec/EncoderChannel.c \
../src/codec/MethodsBag.c \
../src/codec/StringNameTable.c \
../src/codec/StringValueTable.c \
../src/codec/UCSString.c 

OBJS += \
./src/codec/BitInputStream.o \
./src/codec/BitOutputStream.o \
./src/codec/ByteStream.o \
./src/codec/DecoderChannel.o \
./src/codec/DynamicMemory.o \
./src/codec/EXIHeaderDecoder.o \
./src/codec/EXIHeaderEncoder.o \
./src/codec/EXIforJSEXICoder.o \
./src/codec/EXIforJSEXIDecoder.o \
./src/codec/EXIforJSEXIEncoder.o \
./src/codec/EXIforJSQNames.o \
./src/codec/EncoderChannel.o \
./src/codec/MethodsBag.o \
./src/codec/StringNameTable.o \
./src/codec/StringValueTable.o \
./src/codec/UCSString.o 

C_DEPS += \
./src/codec/BitInputStream.d \
./src/codec/BitOutputStream.d \
./src/codec/ByteStream.d \
./src/codec/DecoderChannel.d \
./src/codec/DynamicMemory.d \
./src/codec/EXIHeaderDecoder.d \
./src/codec/EXIHeaderEncoder.d \
./src/codec/EXIforJSEXICoder.d \
./src/codec/EXIforJSEXIDecoder.d \
./src/codec/EXIforJSEXIEncoder.d \
./src/codec/EXIforJSQNames.d \
./src/codec/EncoderChannel.d \
./src/codec/MethodsBag.d \
./src/codec/StringNameTable.d \
./src/codec/StringValueTable.d \
./src/codec/UCSString.d 


# Each subdirectory must supply rules for building sources it contributes
src/codec/%.o: ../src/codec/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I"../src/codec" -I"../src/test" -I"../src/exiforjavascript" -Os -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


