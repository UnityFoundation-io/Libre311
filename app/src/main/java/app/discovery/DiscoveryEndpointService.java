package app.discovery;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
@ConfigurationProperties("app")
public class DiscoveryEndpointService {

    @MapFormat(transformation = MapFormat.MapTransformation.NESTED)
    private Map<String, Object> discovery;

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryEndpointService.class);

    @EventListener
    public void parseDiscoveryConfig(ServerStartupEvent event) {
        LOG.info("Parsing of 'discovery' endpoint related information complete.");
    }

    public Discovery getDiscoveryInfo() {
        Discovery discovery = new Discovery();
        discovery.setChangeset((String) this.discovery.get("changeset"));
        discovery.setContact((String) this.discovery.get("contact"));
        discovery.setKeyService((String) this.discovery.get("key-service"));

        ArrayList<HashMap> endpoints = (ArrayList<HashMap>) this.discovery.get("endpoints");

        if (endpoints != null) {
            List<DiscoveryEndpoint> discoveryEndpointList = new ArrayList<>();

            for (HashMap endpoint : endpoints) {
                DiscoveryEndpoint discoveryEndpoint = new DiscoveryEndpoint();
                discoveryEndpoint.setSpecification((String) endpoint.get("specification"));
                discoveryEndpoint.setUrl((String) endpoint.get("url"));
                discoveryEndpoint.setChangeset((String) endpoint.get("changeset"));
                discoveryEndpoint.setType((String) endpoint.get("type"));
                discoveryEndpoint.setFormats((List<String>) endpoint.get("formats"));

                discoveryEndpointList.add(discoveryEndpoint);
            }

            discovery.setEndpoints(discoveryEndpointList);
        }


        return discovery;
    }

    public void setDiscovery(Map<String, Object> discovery) {
        this.discovery = discovery;
    }
}
