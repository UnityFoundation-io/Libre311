package app.fixture;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import jakarta.inject.Inject;

public class JurisdictionRepositoryFixture {

    public static Jurisdiction DEFAULT_JURISDICTION = new Jurisdiction("stlma", 1L);


    @Inject
    public JurisdictionRepository jurisdictionRepository;

    public Jurisdiction saveJurisdiction(Jurisdiction jurisdiction){
        return jurisdictionRepository.save(jurisdiction);
    }
}
