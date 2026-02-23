import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class LibraryBookTracker {
    private static final String ROW_FMT = "%-30s %-20s %-15s %5d%n";
    public static void main(String[] args) {
        Stats stats = new Stats(0, 0, 0, 0);
        Path catalogPath = null;
        try {
            if (args.length < 2) {
                InsufficientArgumentsException ex = new InsufficientArgumentsException(
                        "You must provide 2 arguments: <catalogFile.txt> <operation>");
                ErrorLogger.logUserInputError(Path.of("."), "NO ARGUMENTS", ex);
                throw ex;
            }
            String fileArg = args[0];
            if (!fileArg.endsWith(".txt")) {
                InvalidFileNameException ex = new InvalidFileNameException(
                        "Catalog file must end with .txt");
                catalogPath = Path.of(fileArg);
                ErrorLogger.logUserInputError(catalogPath, fileArg, ex);
                throw ex;
            }
            catalogPath = Paths.get(fileArg);
            ensureCatalogFileExists(catalogPath);

            List<Book> catalog = readValidBooks(catalogPath, stats);

            String op = args[1].trim();
            if (isExactly13Digits(op)) {
                List<Book> matches = searchByIsbn(catalog, op);

                if (matches.size() > 1) {
                    DuplicateISBNException ex = new DuplicateISBNException(
                            "More than one book found with ISBN: " + op);
                    ErrorLogger.logUserInputError(catalogPath, op, ex);
                    throw ex;
                }

                printHeader();
                if (matches.size() == 1) printBook(matches.get(0));
                stats.setSearchResults(matches.size());
                stats.setBooksAdded(0);

            } else if (looksLikeNewRecord(op)) {
                try {
                    Book newBook = BookParser.parseNewRecord(op);

                    for (Book b : catalog) {
                        if (b.getIsbn().equals(newBook.getIsbn())) {
                            DuplicateISBNException ex = new DuplicateISBNException(
                                    "ISBN already exists");
                            ErrorLogger.logUserInputError(catalogPath, op, ex);
                            throw ex;
                        }
                    }

                    catalog.add(newBook);
                    catalog.sort(Comparator.comparing(b -> b.getTitle().toLowerCase()));
                    writeCatalogFile(catalogPath, catalog);

                    printHeader();
                    printBook(newBook);

                    stats.setBooksAdded(1);
                    stats.setSearchResults(0);

                } catch (BookCatalogException e) {
                    stats.setErrorsEncountered(stats.getErrorsEncountered() + 1);
                    System.err.println("Error: " + e.getMessage());
                    ErrorLogger.logUserInputError(catalogPath, op, e);
                }

            } else if (op.contains(":") && op.split(":", -1).length != 4) {
                MalformedBookEntryException ex = new MalformedBookEntryException(
                        "New book record must be: title:author:isbn:copies");
                stats.setErrorsEncountered(stats.getErrorsEncountered() + 1);
                System.err.println("Error: " + ex.getMessage());
                ErrorLogger.logUserInputError(catalogPath, op, ex);

            } else {
                List<Book> matches = searchByTitleKeyword(catalog, op);
                printHeader();
                for (Book b : matches) printBook(b);
                stats.setSearchResults(matches.size());
                stats.setBooksAdded(0);
            }

        } catch (InvalidFileNameException | InsufficientArgumentsException e) {
            stats.setErrorsEncountered(stats.getErrorsEncountered() + 1);
            System.err.println("Error: " + e.getMessage());

        } catch (BookCatalogException e) {
            stats.setErrorsEncountered(stats.getErrorsEncountered() + 1);
            System.err.println("Error: " + e.getMessage());
            if (catalogPath != null) ErrorLogger.logUserInputError(catalogPath, args[1], e);

        } catch (IOException io) {
            stats.setErrorsEncountered(stats.getErrorsEncountered() + 1);
            System.err.println("I/O Error: " + io.getMessage());
            if (catalogPath != null) ErrorLogger.logUserInputError(catalogPath, "I/O", io);

        } catch (Exception ex) {
            stats.setErrorsEncountered(stats.getErrorsEncountered() + 1);
            System.err.println("Unexpected error: " + ex.getMessage());
            if (catalogPath != null) ErrorLogger.logUserInputError(catalogPath,
                    (args.length >= 2 ? args[1] : "N/A"), ex);

        } finally {
            System.out.println();
            System.out.println("=== Statistics ===");
            System.out.println("Valid records processed: " + stats.getValidRecordsProcessed());
            System.out.println("Search results: " + stats.getSearchResults());
            System.out.println("Books added: " + stats.getBooksAdded());
            System.out.println("Errors encountered: " + stats.getErrorsEncountered());
            System.out.println("Thank you for using the Library Book Tracker.");
        }
    }

    private static void ensureCatalogFileExists(Path catalogPath) throws IOException {
        Path parent = catalogPath.getParent();
        if (parent != null) Files.createDirectories(parent);
        if (!Files.exists(catalogPath)) Files.createFile(catalogPath);
    }
    private static List<Book> readValidBooks(Path catalogPath, Stats stats) throws IOException {
        List<Book> books = new ArrayList<>();
        List<String> lines = Files.readAllLines(catalogPath);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String trimmed = line.trim();

            try {
                Book b = BookParser.parseCatalogLine(trimmed);
                books.add(b);
                stats.setValidRecordsProcessed(stats.getValidRecordsProcessed() + 1);
            } catch (BookCatalogException e) {
                stats.setErrorsEncountered(stats.getErrorsEncountered() + 1);
                ErrorLogger.logFileLineError(catalogPath, trimmed, e);
            }
        }
        return books;
    }
    private static void writeCatalogFile(Path catalogPath, List<Book> catalog) throws IOException {
        List<String> out = new ArrayList<>();
        for (Book b : catalog) out.add(b.toCatalogLine());
        Files.write(catalogPath, out,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static boolean isExactly13Digits(String s) {
        return s != null && s.matches("\\d{13}");
    }
    private static boolean looksLikeNewRecord(String s) {
        return s != null && s.split(":", -1).length == 4;
    }
    private static List<Book> searchByTitleKeyword(List<Book> catalog, String keyword) {
        List<Book> results = new ArrayList<>();
        String k = keyword.toLowerCase();
        for (Book b : catalog) if (b.getTitle().toLowerCase().contains(k)) results.add(b);
        return results;
    }
    private static List<Book> searchByIsbn(List<Book> catalog, String isbn) {
        List<Book> results = new ArrayList<>();
        for (Book b : catalog) if (b.getIsbn().equals(isbn)) results.add(b);
        return results;
    }
    private static void printHeader() {
        System.out.printf("%-30s %-20s %-15s %5s%n", "Title", "Author", "ISBN", "Copy");
        System.out.println("--------------------------------------------------------------------------");
    }

    private static void printBook(Book b) {
        System.out.printf(ROW_FMT, b.getTitle(), b.getAuthor(), b.getIsbn(), b.getCopies());
    }
}