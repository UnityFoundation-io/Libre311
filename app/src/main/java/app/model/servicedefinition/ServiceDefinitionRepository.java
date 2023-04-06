package app.model.servicedefinition;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface ServiceDefinitionRepository extends PageableRepository<ServiceDefinition, String> {

}
