public class Stats {

    private int validRecordsProcessed = 0;
    private int searchResults = 0;
    private int booksAdded = 0;
    private int errorsEncountered = 0;
    public Stats(int validRecordsProcessed, int searchResults, int booksAdded, int errorsEncountered) {
        this.validRecordsProcessed = validRecordsProcessed;
        this.searchResults = searchResults;
        this.booksAdded = booksAdded;
        this.errorsEncountered = errorsEncountered;
    }
    public int getValidRecordsProcessed() { return validRecordsProcessed; }
    public int getSearchResults() { return searchResults; }
    public int getBooksAdded() { return booksAdded; }
    public int getErrorsEncountered() { return errorsEncountered; }

    public void setValidRecordsProcessed(int v) { validRecordsProcessed = v; }
    public void setSearchResults(int v) { searchResults = v; }
    public void setBooksAdded(int v) { booksAdded = v; }
    public void setErrorsEncountered(int v) { errorsEncountered = v; }
}
