import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
public class ErrorLogger {
    public static void logFileLineError(Path catalogFile, String faultyLine, Exception thrownException) {
        writeErrorEntry(catalogFile, "INVALID LINE", faultyLine, thrownException);
    }
    public static void logUserInputError(Path catalogFile, String invalidInput, Exception thrownException) {
        writeErrorEntry(catalogFile, "INVALID INPUT", invalidInput, thrownException);
    }
    private static void writeErrorEntry(Path catalogFile, String errorCategory, String relatedText, Exception e) {
        try {
            Path parentDirectory = catalogFile.getParent();
            if (parentDirectory == null) parentDirectory = Path.of(".");
            Path logFilePath = parentDirectory.resolve("errors.log");

            String logEntry =
                    "[" + LocalDateTime.now().withNano(0) + "] "
                            + errorCategory + ": "
                            + "\"" + relatedText + "\" - "
                            + e.getClass().getSimpleName() + ": "
                            + e.getMessage()
                            + System.lineSeparator();

            Files.write(logFilePath,
                    logEntry.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ioFailure) {
            System.out.println("Unable to write to errors.log");
        }}}