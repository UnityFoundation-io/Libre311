package app.model.service.servicedefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
public class ServiceDefinitionAttribute {

    private String code;
    private boolean variable;
    private AttributeDataType datatype;
    private boolean required;
    private String description;

    @JsonProperty("order")
    private int attributeOrder;

    @JsonProperty("datatype_description")
    private String datatypeDescription;

    @JacksonXmlElementWrapper(localName = "values")
    @JacksonXmlProperty(localName = "value")
    private List<AttributeValue> values;

    public ServiceDefinitionAttribute() {
    }

    public boolean isVariable() {
        return variable;
    }

    public void setVariable(boolean variable) {
        this.variable = variable;
    }

    public AttributeDataType getDatatype() {
        return datatype;
    }

    public void setDatatype(AttributeDataType datatype) {
        this.datatype = datatype;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDatatypeDescription() {
        return datatypeDescription;
    }

    public void setDatatypeDescription(String datatypeDescription) {
        this.datatypeDescription = datatypeDescription;
    }

    public int getAttributeOrder() {
        return attributeOrder;
    }

    public void setAttributeOrder(int attributeOrder) {
        this.attributeOrder = attributeOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AttributeValue> getValues() {
        return values;
    }

    public void setValues(List<AttributeValue> values) {
        this.values = values;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
