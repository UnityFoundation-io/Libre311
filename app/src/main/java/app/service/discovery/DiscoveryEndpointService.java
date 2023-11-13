package app.service.discovery;

import app.dto.discovery.DiscoveryDTO;
import app.dto.discovery.DiscoveryEndpointDTO;
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

    public DiscoveryDTO getDiscoveryInfo() {
        DiscoveryDTO discoveryDTO = new DiscoveryDTO();
        discoveryDTO.setChangeset((String) discovery.get("changeset"));
        discoveryDTO.setContact((String) discovery.get("contact"));
        discoveryDTO.setKeyService((String) discovery.get("key-service"));

        ArrayList<HashMap> endpoints = (ArrayList<HashMap>) discovery.get("endpoints");

        if (endpoints != null) {
            List<DiscoveryEndpointDTO> discoveryEndpointDTOList = new ArrayList<>();

            for (HashMap endpoint : endpoints) {
                DiscoveryEndpointDTO discoveryEndpointDTO = new DiscoveryEndpointDTO();
                discoveryEndpointDTO.setSpecification((String) endpoint.get("specification"));
                discoveryEndpointDTO.setUrl((String) endpoint.get("url"));
                discoveryEndpointDTO.setChangeset((String) endpoint.get("changeset"));
                discoveryEndpointDTO.setType((String) endpoint.get("type"));
                discoveryEndpointDTO.setFormats((List<String>) endpoint.get("formats"));

                discoveryEndpointDTOList.add(discoveryEndpointDTO);
            }

            discoveryDTO.setEndpoints(discoveryEndpointDTOList);
        }


        return discoveryDTO;
    }

    public void setDiscovery(Map<String, Object> discovery) {
        this.discovery = discovery;
    }
}
