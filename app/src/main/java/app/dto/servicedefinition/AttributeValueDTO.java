package app.dto.servicedefinition;

import app.model.servicedefinition.AttributeValue;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class AttributeValueDTO {

    private String key;
    private String name;

    public AttributeValueDTO(AttributeValue attributeValue) {
        this.key = attributeValue.getKey();
        this.name = attributeValue.getName();
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
