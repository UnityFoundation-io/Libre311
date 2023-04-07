package app.model.service.servicedefinition;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class AttributeValue {
    private String key;
    private String name;

    public AttributeValue() {
    }

    public AttributeValue(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
