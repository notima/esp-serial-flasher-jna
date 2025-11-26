import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.notima.energyintelligence.EspSerialFlasher;

/**
 * Test class for EspSerialFlasher.
 * 
 * ATTENTION: Ensure that the device is connected to /dev/ttyUSB0 before running these tests.
 */
public class TestFlasher {

    static final EspSerialFlasher flasher = new EspSerialFlasher();

    long startTime;
    long endTime;

    @BeforeAll
    static void testInitializePort() {
        flasher.portInit("/dev/ttyUSB0", 400000);
        System.out.println("Port initialized successfully.");
    }

    @Test
    void testConnectToTarget() {
        flasher.connectToTarget();
        System.out.println("Connected to target successfully.");
    }

    @Test
    void testGetTarget() {
        flasher.connectToTarget();
        int target = flasher.getTarget();
        System.out.println("Target: " + target);
    }

    @Test
    void testFlash() {
        flasher.connectToTarget();

        int bootloaderAddress = 0x1000;
        int partitionTableAddress = 0x8000;
        int applicationAddress = 0x10000;

        // Flash bootloader binary
        byte[] bootloader = TestUtils.loadResourceAsByteArray("/bin/bootloader.bin");
        flasher.flashBinary(bootloader, bootloaderAddress, 0x1000, new EspSerialFlasher.FlashProgressCallback() {
            @Override
            public void onErased() {
                System.out.println("Flash erased.");
                startTime = System.currentTimeMillis();
            }

            @Override
            public void onProgress(int progress) {
                System.out.printf("Flashing progress: %d%%%n", progress);
            }

            @Override
            public void onCompleted() {
                System.out.println("Flashing completed.");
                endTime = System.currentTimeMillis();
                System.out.printf("Flashing took %d ms.%n", (endTime - startTime));
            }
        });


        // Flash partition table binary
        byte[] partitionTable = TestUtils.loadResourceAsByteArray("/bin/partition-table.bin");
        flasher.flashBinary(partitionTable, partitionTableAddress, 0x1000, new EspSerialFlasher.FlashProgressCallback() {
            @Override
            public void onErased() {
                System.out.println("Flash erased.");
                startTime = System.currentTimeMillis();
            }

            @Override
            public void onProgress(int progress) {
                System.out.printf("Flashing progress: %d%%%n", progress);
            }

            @Override
            public void onCompleted() {
                System.out.println("Flashing completed.");
                endTime = System.currentTimeMillis();
                System.out.printf("Flashing took %d ms.%n", (endTime - startTime));
            }
        });

        // Flash application binary
        byte[] application = TestUtils.loadResourceAsByteArray("/bin/hello_world.bin");
        flasher.flashBinary(application, applicationAddress, 0x1000, new EspSerialFlasher.FlashProgressCallback() {
            @Override
            public void onErased() {
                System.out.println("Flash erased.");
                startTime = System.currentTimeMillis();
            }

            @Override
            public void onProgress(int progress) {
                System.out.printf("Flashing progress: %d%%%n", progress);
            }

            @Override
            public void onCompleted() {
                System.out.println("Flashing completed.");
                endTime = System.currentTimeMillis();
                System.out.printf("Flashing took %d ms.%n", (endTime - startTime));
            }
        });
    }

    @AfterAll
    static void testDeinitializePort() {
        flasher.portDeinit();
        System.out.println("Port deinitialized successfully.");
    }
}
