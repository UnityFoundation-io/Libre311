package app.dto.jurisdiction;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Introspected
public class PatchJurisdictionDTO {

    @NotEmpty
    private String name;

    @JsonProperty("primary_color")
    @Pattern(regexp = "(\\d+),?\\s?(\\.?\\d+%?),?\\s?(\\.?\\d+%?)")
    private String primaryColor;

    @JsonProperty("primary_hover_color")
    @Pattern(regexp = "(\\d+),?\\s?(\\.?\\d+%?),?\\s?(\\.?\\d+%?)")
    private String primaryHoverColor;

    @JsonProperty("logo_media_url")
    private String logoMediaUrl;

    public PatchJurisdictionDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getPrimaryHoverColor() {
        return primaryHoverColor;
    }

    public void setPrimaryHoverColor(String primaryHoverColor) {
        this.primaryHoverColor = primaryHoverColor;
    }

    public String getLogoMediaUrl() {
        return logoMediaUrl;
    }

    public void setLogoMediaUrl(String logoMediaUrl) {
        this.logoMediaUrl = logoMediaUrl;
    }
}
