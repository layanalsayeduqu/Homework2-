public class BookParser {
    public static Book parseCatalogLine(String line) throws BookCatalogException {
        return parseFourFields(line);
    }
    public static Book parseNewRecord(String record) throws BookCatalogException {
        return parseFourFields(record);
    }
    private static Book parseFourFields(String text) throws BookCatalogException {
        String[] parts = text.split(":", -1);

        if (parts.length != 4) {
            throw new MalformedBookEntryException(
                    "Book entry must contain exactly 4 fields separated by ':'.");
        }

        String title = parts[0].trim();
        String author = parts[1].trim();
        String isbn = parts[2].trim();
        String copiesStr = parts[3].trim();

        if (title.isEmpty()) {
            throw new MalformedBookEntryException("Title cannot be empty.");
        }

        if (author.isEmpty()) {
            throw new MalformedBookEntryException("Author cannot be empty.");
        }

        if (!isExactly13Digits(isbn)) {
            throw new InvalidISBNException("ISBN must contain exactly 13 digits.");
        }

        int copies;
        try {
            copies = Integer.parseInt(copiesStr);
        } catch (NumberFormatException e) {
            throw new MalformedBookEntryException("Copies must be a valid integer.");
        }

        if (copies <= 0) {
            throw new MalformedBookEntryException(
                    "Copies must be a positive integer greater than zero.");
        }

        return new Book(title, author, isbn, copies);
    }
    private static boolean isExactly13Digits(String s) {
        if (s == null || s.length() != 13) return false;
        for (int i = 0; i < 13; i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }}
