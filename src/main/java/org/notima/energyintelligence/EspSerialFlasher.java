package org.notima.energyintelligence;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class EspSerialFlasher {

    private interface EspSerialFlasherLib extends Library {
        EspSerialFlasherLib INSTANCE = Native.load("esp_flasher", EspSerialFlasherLib.class);

        public static final int ESP_LOADER_SUCCESS = 0;                /*!< Success */
        public static final int ESP_LOADER_ERROR_FAIL = 1;             /*!< Unspecified error */
        public static final int ESP_LOADER_ERROR_TIMEOUT = 2;          /*!< Timeout elapsed */
        public static final int ESP_LOADER_ERROR_IMAGE_SIZE = 3;       /*!< Image size to flash is larger than flash size */
        public static final int ESP_LOADER_ERROR_INVALID_MD5 = 4;      /*!< Computed and received MD5 does not match */
        public static final int ESP_LOADER_ERROR_INVALID_PARAM = 5;    /*!< Invalid parameter passed to function */
        public static final int ESP_LOADER_ERROR_INVALID_TARGET = 6;   /*!< Connected target is invalid */
        public static final int ESP_LOADER_ERROR_UNSUPPORTED_CHIP = 7; /*!< Attached chip is not supported */
        public static final int ESP_LOADER_ERROR_UNSUPPORTED_FUNC = 8; /*!< Function is not supported on attached target */
        public static final int ESP_LOADER_ERROR_INVALID_RESPONSE = 9; /*!< Internal error */

        public static final int ESP8266_CHIP = 0;
        public static final int ESP32_CHIP   = 1;
        public static final int ESP32S2_CHIP = 2;
        public static final int ESP32C3_CHIP = 3;
        public static final int ESP32S3_CHIP = 4;
        public static final int ESP32C2_CHIP = 5;
        public static final int ESP32C5_CHIP = 6;
        public static final int ESP32H2_CHIP = 7;
        public static final int ESP32C6_CHIP = 8;
        public static final int ESP32P4_CHIP = 9;
        public static final int ESP_MAX_CHIP = 10;
        public static final int ESP_UNKNOWN_CHIP = 10;

        public class LoaderLinuxConfig extends Structure {
            public String device;
            public int baudrate;

            @Override
            protected List<String> getFieldOrder() {
                return Arrays.asList("device", "baudrate");
            }

            public static class ByReference extends LoaderLinuxConfig implements Structure.ByReference {}
        }

        public class EspLoaderConnectArgs extends Structure {
            public int sync_timeout;
            public int trials;

            @Override
            protected List<String> getFieldOrder() {
                return Arrays.asList("sync_timeout", "trials");
            }

            public static class ByReference extends EspLoaderConnectArgs implements Structure.ByReference {}
        }

        int loader_port_linux_init(LoaderLinuxConfig.ByReference config);
        void loader_port_deinit();
        int esp_loader_connect(EspLoaderConnectArgs.ByReference connect_args);
        int esp_loader_get_target();
        int esp_loader_flash_start(int offset, int image_size, int block_size);
        int esp_loader_flash_write(Pointer payload, int size);
        void esp_loader_reset_target();
    }

    public EspSerialFlasher() {
        System.out.printf("Library file: %s%n", NativeLibrary.getInstance("esp_flasher").getFile());
    }

    public void portInit(String comPort, int baudRate) {
        EspSerialFlasherLib.LoaderLinuxConfig.ByReference config = new EspSerialFlasherLib.LoaderLinuxConfig.ByReference();
        config.device = comPort;
        config.baudrate = baudRate;

        int err = 1;

        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (OS.contains("mac") || OS.contains("darwin")) {
            throw new RuntimeException("Mac OS not supported yet.");
        } else if (OS.contains("win")) {
            throw new RuntimeException("Windows not supported yet.");
        } else if (OS.contains("nux")) {
            err = EspSerialFlasherLib.INSTANCE.loader_port_linux_init(config);
        } else {
            throw new RuntimeException("Unsupported OS: " + OS);
        }

        if(err != EspSerialFlasherLib.ESP_LOADER_SUCCESS) {
            throw new RuntimeException("Failed to initialize port, Error: " + getErrorString(err));
        }
    }

    public void portDeinit() {
        EspSerialFlasherLib.INSTANCE.loader_port_deinit();
    }

    public void connectToTarget() {
        EspSerialFlasherLib.EspLoaderConnectArgs.ByReference args = new EspSerialFlasherLib.EspLoaderConnectArgs.ByReference();
        args.sync_timeout = 100;
        args.trials = 10;

        int err = EspSerialFlasherLib.INSTANCE.esp_loader_connect(args);
        if(err != EspSerialFlasherLib.ESP_LOADER_SUCCESS) {
            throw new RuntimeException("Failed to connect to target, Error: " + getErrorString(err));
        }
    }

    public int getTarget() {
        return EspSerialFlasherLib.INSTANCE.esp_loader_get_target();
    }

    public void flashBinary(byte[] binary, int address, int blockSize, FlashProgressCallback callback) {

        int err = EspSerialFlasherLib.INSTANCE.esp_loader_flash_start(address, binary.length, blockSize);
        if(err != EspSerialFlasherLib.ESP_LOADER_SUCCESS) {
            throw new RuntimeException("Erasing flash failed with error: " + getErrorString(err));
        }
        callback.onErased();

        int size = binary.length;
        int offset = 0;
        while (size > 0) {
            int toRead = Math.min(blockSize, size);

            Memory block = new Memory(blockSize);
            block.write(0, binary, offset, toRead);

            // Fill the rest with 0xFF
            for (int i = toRead; i < blockSize; i++) {
                block.setByte(i, (byte)0xFF);
            }

            err = EspSerialFlasherLib.INSTANCE.esp_loader_flash_write(block, toRead);
            if(err != EspSerialFlasherLib.ESP_LOADER_SUCCESS) {
                throw new RuntimeException("Flashing failed with error: " + getErrorString(err));
            }
            offset += toRead;
            size -= toRead;
            callback.onProgress(offset * 100 / binary.length);
        }
        callback.onCompleted();
    }

    public void resetTarget() {
        EspSerialFlasherLib.INSTANCE.esp_loader_reset_target();
    }

    private String getErrorString(int error){
        String[] mapping = {
            "NONE", "UNKNOWN", "TIMEOUT", "IMAGE SIZE",
            "INVALID MD5", "INVALID PARAMETER", "INVALID TARGET",
            "UNSUPPORTED CHIP", "UNSUPPORTED FUNCTION", "INVALID RESPONSE"
        };

        return mapping[error];
    }

    public interface FlashProgressCallback {
        void onErased();
        void onProgress(int progress);
        void onCompleted();
    }
}
