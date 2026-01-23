package app.model.servicerequest;

import app.dto.servicerequest.ServiceRequestRemovalSuggestionDTO;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.Map;

@Repository
public interface ServiceRequestRemovalSuggestionRepository extends CrudRepository<ServiceRequestRemovalSuggestion, Long> {
    Page<ServiceRequestRemovalSuggestion> findAllByJurisdictionId(String jurisdictionId, Pageable pageable);

    @Query("update ServiceRequestRemovalSuggestion s set s.deleted = true where s.serviceRequestId = :serviceRequestId and s.jurisdictionId = :jurisdictionId")
    void deleteByServiceRequestIdAndJurisdictionId(Long serviceRequestId, String jurisdictionId);

    @Query("update ServiceRequestRemovalSuggestion s set s.deleted = true where s.id = :id and s.jurisdictionId = :jurisdictionId")
    void softDelete(Long id, String jurisdictionId);

    @Query("SELECT s.serviceRequestId as serviceRequestId, COUNT(s) as count FROM ServiceRequestRemovalSuggestion s WHERE s.serviceRequestId IN (:serviceRequestIds) AND s.deleted = false GROUP BY s.serviceRequestId")
    List<ServiceRequestRemovalSuggestionCount> countByServiceRequestIdIn(List<Long> serviceRequestIds);
}