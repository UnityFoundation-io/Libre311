package app.model.service.servicedefinition;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AttributeDataType {
    STRING, NUMBER, DATETIME, TEXT, SINGLEVALUELIST, MULTIVALUELIST;

    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase();
    }
}
