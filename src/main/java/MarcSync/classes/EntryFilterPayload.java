package MarcSync.classes;

public class EntryFilterPayload {
    public EntryData filters;

    public EntryFilterPayload(EntryData entryData) {
        this.filters = entryData;
    }
}
