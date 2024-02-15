package app.dto.jurisdiction;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Introspected
public class PatchJurisdictionDTO {

    @NotEmpty
    private String name;

    // derived from https://regexr.com/39cgj
    @JsonProperty("primary_color")
    @Pattern(regexp = "(?:#|0x)(?:[a-f0-9]{3}|[a-f0-9]{6})\\b|(?:rgb|hsl)a?\\([^\\)]*\\)", flags = {Pattern.Flag.CASE_INSENSITIVE})
    private String primaryColor;

    @JsonProperty("primary_hover_color")
    @Pattern(regexp = "(?:#|0x)(?:[a-f0-9]{3}|[a-f0-9]{6})\\b|(?:rgb|hsl)a?\\([^\\)]*\\)", flags = {Pattern.Flag.CASE_INSENSITIVE})
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
