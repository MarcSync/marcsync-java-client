package MarcSync.classes;

public class EntryUpdatePayload {
    public EntryData filters;
    public EntryData data;

    public EntryUpdatePayload(EntryData filters, EntryData data) {
        this.filters = filters;
        this.data = data;
    }
}