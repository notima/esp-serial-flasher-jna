import java.io.IOException;
import java.io.InputStream;

public class TestUtils {
    public static byte[] loadResourceAsByteArray(String resourcePath) {
        try (InputStream is = TestUtils.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
    }
}
