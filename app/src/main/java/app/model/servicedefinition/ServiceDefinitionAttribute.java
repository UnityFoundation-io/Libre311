package app.model.servicedefinition;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "service_definition_attributes")
public class ServiceDefinitionAttribute {

    @Id
    private String code;

    @ManyToOne
    @JoinColumn(name = "service_definition_id", nullable = false)
    private ServiceDefinition serviceDefinition;

    private boolean variable;
    private AttributeDataType datatype;
    private boolean required;
    private String datatypeDescription;
    private int attributeOrder; // order is a sql keyword
    private String description;

    @ElementCollection
    @CollectionTable(name = "attribute_values",
    joinColumns = @JoinColumn(name = "service_definition_attributes_id"))
    private List<AttributeValue> values;

    public ServiceDefinitionAttribute() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ServiceDefinition getServiceDefinition() {
        return serviceDefinition;
    }

    public void setServiceDefinition(ServiceDefinition serviceDefinition) {
        this.serviceDefinition = serviceDefinition;
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
}
