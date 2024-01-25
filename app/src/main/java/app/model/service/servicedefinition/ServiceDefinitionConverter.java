package app.model.service.servicedefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import javax.persistence.AttributeConverter;

@Singleton
public class ServiceDefinitionConverter implements
    AttributeConverter<ServiceDefinition, String>  {
  private final ObjectMapper objectMapper;

  public ServiceDefinitionConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String convertToDatabaseColumn(ServiceDefinition serviceDefinition) {
    if (serviceDefinition != null) {
      try {
        return objectMapper.writeValueAsString(serviceDefinition);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  @Override
  public ServiceDefinition convertToEntityAttribute(String s) {
    if (s != null && !s.isBlank()) {
      try {
        return objectMapper.readValue(s, ServiceDefinition.class);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }
}
