# A Java wrapper for esp-serial-flasher

This Java library utilizes JNA (Java Native Access) to execute functions in the [esp-serial-flasher](https://github.com/Olverine/esp-serial-flasher) native C library. The specific fork of the library adds desktop OS compatibility.

## Public API

All public methods are located in the class `EspSerialFlasher`.

### Initialize serial port

`void portInit(String comPort, int baudRate)`

This method will initiliaze the serial port for communication with an ESP32. A `RuntimeException` will be thrown if the initialization fails.

### Connect to target

`void connectToTarget()`

This method will set the ESP32 into serial bootloader mode and establish a connection to the serial bootloader. A `RuntimeException` will be thrown if the connection fails.

### Get target

`int getTarget()`

Check what kind of device is connected. Possible return values are:

- `EspSerialFlasher.ESP8266_CHIP`
- `EspSerialFlasher.ESP32_CHIP`
- `EspSerialFlasher.ESP32S2_CHIP`
- `EspSerialFlasher.ESP32C3_CHIP`
- `EspSerialFlasher.ESP32S3_CHIP`
- `EspSerialFlasher.ESP32C2_CHIP`
- `EspSerialFlasher.ESP32C5_CHIP`
- `EspSerialFlasher.ESP32H2_CHIP`
- `EspSerialFlasher.ESP32C6_CHIP`
- `EspSerialFlasher.ESP32P4_CHIP`
- `EspSerialFlasher.ESP_MAX_CHIP`
- `EspSerialFlasher.ESP_UNKNOWN_CHIP`

### Flash binary data

`void flashBinary(byte[] binary, int address, int blockSize, FlashProgressCallback callback)`

Erase a flash region and write data to it. The size of the flash region is determined by the length of the `binary` parameter. The recommeded blocksize is `0x1000` (4KB).

The `FlashProgressCallback` declares the following functions:

- `void onErased()`: When the flash region has been successfully erased.
- `void onProgress(int progress)`: When a block of data has been written. The `progress` parameter contains the percentage (0-100) of the total data that has been written so far.
- `void onCompleted()`: When all data has been written successfully.

A `RuntimeException` will be thrown if flash erasure or writing fails.

### Resetting target

`void resetTarget()`

Resets (reboots) the ESP32 chip.

### Close serial port

`void portDeinit()`