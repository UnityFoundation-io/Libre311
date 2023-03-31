package app;

import app.dto.service.ServiceDTO;
import app.dto.service.ServiceList;
import app.service.ServiceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import javax.validation.Valid;
import java.util.List;

@Controller("/api")
public class RootController {

    private final ServiceService serviceService;

    public RootController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @Get(uris = {"/services", "/services.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public List<ServiceDTO> indexJson(@Valid Pageable pageable) {
        return serviceService.findAll(pageable);
    }

    @Get("/services.xml")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public String indexXml(@Valid Pageable pageable) throws JsonProcessingException {
        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        ServiceList serviceList = new ServiceList(serviceService.findAll(pageable));

        return xmlMapper.writeValueAsString(serviceList);
    }
}
