package app.servicerequest.download;

import app.servicedefinition.AttributeValueDTO;
import app.servicedefinition.ServiceDefinitionAttributeDTO;
import app.servicerequest.ServiceRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.server.types.files.StreamedFile;
import jakarta.inject.Singleton;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class CsvGenerator {


    public StreamedFile generateRequestListCsv(List<ServiceRequest> requests) throws MalformedURLException {
        List<ServiceRequestCsvRow> csvRows = mapToCsvRows(requests);
        File tmpFile = writeCsv(csvRows);
        return new StreamedFile(tmpFile.toURI().toURL()).attach(Instant.now() + ".csv");
    }
    
    private void printCsvRecord(CSVPrinter csvPrinter, ServiceRequestCsvRow csvRow) {
        try {
            csvPrinter.printRecord(
                    csvRow.getJurisdictionId(),
                    csvRow.getServiceName(),
                    csvRow.getGroup(),
                    csvRow.getServiceCode(),
                    csvRow.getId(),
                    csvRow.getServiceSubtype(),
                    csvRow.getDescription(),
                    csvRow.getMediaUrl(),
                    csvRow.getAddress(),
                    csvRow.getZipcode(),
                    csvRow.getLatitude(),
                    csvRow.getLongitude(),
                    csvRow.getFirstName(),
                    csvRow.getLastName(),
                    csvRow.getEmail(),
                    csvRow.getPhone(),
                    csvRow.getDateCreated(),
                    csvRow.getDateUpdated(),
                    csvRow.getClosedDate(),
                    csvRow.getAgencyResponsible(),
                    csvRow.getAgencyEmail(),
                    csvRow.getPriority(),
                    csvRow.getStatus(),
                    csvRow.getStatusNotes(),
                    csvRow.getServiceNotice()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private File writeCsv(List<ServiceRequestCsvRow> csvRows) {
        File tmpFile;
        Instant now = Instant.now();
        try {
            tmpFile = File.createTempFile(now.toString().replaceAll(":", ""), ".csv");
            try (Writer writer = new FileWriter(tmpFile)) {

                CSVFormat.Builder builder = CSVFormat.Builder.create(CSVFormat.DEFAULT);
                builder.setHeader(CsvHeaders.class);

                try (CSVPrinter csvPrinter = new CSVPrinter(writer, builder.build())) {
                    csvRows.forEach(csvRow -> printCsvRecord(csvPrinter, csvRow));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tmpFile;
    }


    private List<ServiceRequestCsvRow> mapToCsvRows(List<ServiceRequest> requests) {
        return requests.stream()
                .map(serviceRequest -> {
                    ServiceRequestCsvRow dto = new ServiceRequestCsvRow(serviceRequest);

                    if (serviceRequest.getAttributesJson() != null) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            ServiceDefinitionAttributeDTO[] serviceDefinitionAttributeDTOS =
                                    objectMapper.readValue(serviceRequest.getAttributesJson(), ServiceDefinitionAttributeDTO[].class);
                            List<String> values = Arrays.stream(serviceDefinitionAttributeDTOS)
                                    .flatMap(serviceDefinitionAttribute -> {
                                        if (serviceDefinitionAttribute.getValues() != null) {
                                            return serviceDefinitionAttribute.getValues().stream();
                                        }
                                        return Stream.of();
                                    })
                                    .map(AttributeValueDTO::getKey).collect(Collectors.toList());

                            dto.setServiceSubtype(String.join(",", values));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
