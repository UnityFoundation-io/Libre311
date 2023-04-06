package app.model.servicedefinition;

public enum AttributeDataType {
    STRING, NUMBER, DATETIME, TEXT, SINGLEVALUELIST, MULTIVALUELIST;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
