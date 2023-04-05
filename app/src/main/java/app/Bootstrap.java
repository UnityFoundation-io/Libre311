package app;

import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.ServiceType;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.util.StringUtils;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Requires(property = "wemove.bootstrap.data.enabled", value = StringUtils.TRUE)
@ConfigurationProperties("wemove.bootstrap")
public class Bootstrap {

    @MapFormat(transformation = MapFormat.MapTransformation.NESTED)
    private Map<String, Object> data;

    private final ServiceRepository serviceRepository;
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);

    public Bootstrap(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @EventListener
    public void devData(ServerStartupEvent event) {
        if(data != null) {

            if(data.containsKey("services")) {
                ((List<Map<String, ?>>) data.get("services")).stream().forEach(orgMap -> {
                    String serviceName = (String) orgMap.get("serviceName");
                    Service service = new Service(serviceName);
                    service.setServiceCode((String) orgMap.get("serviceCode"));
                    service.setDescription((String) orgMap.get("description"));
                    service.setMetadata((boolean) orgMap.get("metadata"));
                    service.setType(ServiceType.valueOf(((String) orgMap.get("type")).toUpperCase()));

                    serviceRepository.save(service);
                });
            }
        }
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
