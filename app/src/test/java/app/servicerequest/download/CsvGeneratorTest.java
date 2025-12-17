package app.servicerequest.download;

import app.model.servicerequest.ServiceRequest;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
class CsvGeneratorTest {

    @Inject
    private CsvGenerator csvGenerator;

    @Test
    void generateRequestListCsv_withEmptyList_shouldReturnCsvWithHeadersOnly() throws IOException {
        // given
        List<ServiceRequest> requests = new ArrayList<>();

        // when
        StreamedFile streamedFile = csvGenerator.generateRequestListCsv(requests);

        // then
        assertNotNull(streamedFile);

        String csvContent = new BufferedReader(new InputStreamReader(streamedFile.toInputStream()))
                .lines().collect(Collectors.joining("\n"));

        String expectedHeaders = "jurisdictionId,serviceName,group,serviceCode,id,serviceSubtype,description,mediaUrl,address,zipcode,latitude,longitude,firstName,lastName,email,phone,dateCreated,dateUpdated,closedDate,agencyResponsible,agencyEmail,priority,status,statusNotes,serviceNotice";
        assertEquals(expectedHeaders, csvContent);
    }
}
