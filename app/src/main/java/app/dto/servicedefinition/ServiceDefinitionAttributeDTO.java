package app.dto.servicedefinition;

import app.model.servicedefinition.AttributeDataType;
import app.model.servicedefinition.ServiceDefinitionAttribute;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import java.util.List;
import java.util.stream.Collectors;

@Introspected
public class ServiceDefinitionAttributeDTO {

    private String code;
    private boolean variable;
    private AttributeDataType datatype;
    private boolean required;

    @JsonProperty("datatype_description")
    private String datatypeDescription;
    private int order;
    private String description;
    private List<AttributeValueDTO> values;


    public ServiceDefinitionAttributeDTO(ServiceDefinitionAttribute serviceDefinitionAttribute) {
        this.code = serviceDefinitionAttribute.getCode();
        this.variable = serviceDefinitionAttribute.isVariable();
        this.datatype = serviceDefinitionAttribute.getDatatype();
        this.required = serviceDefinitionAttribute.isRequired();
        this.datatypeDescription = serviceDefinitionAttribute.getDatatypeDescription();
        this.order = serviceDefinitionAttribute.getAttributeOrder();
        this.description = serviceDefinitionAttribute.getDescription();
//        if (serviceDefinitionAttribute.getValues() != null) {
//            this.values = serviceDefinitionAttribute.getValues()
//                    .stream().map(AttributeValueDTO::new)
//                    .collect(Collectors.toList());
//        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AttributeValueDTO> getValues() {
        return values;
    }

    public void setValues(List<AttributeValueDTO> values) {
        this.values = values;
    }
}
