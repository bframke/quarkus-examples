package de.code.client.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

import static de.code.client.common.model.Constants.PATTERN_TYPE;
import static de.code.client.common.model.Constants.PATTERN_URL;

@JsonIgnoreProperties( ignoreUnknown = true )
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Event {

    @JsonProperty(required = true, value = "id")
    @NotNull(message = "Id must not be null!")
    @Pattern(regexp="^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    @NotBlank(message = "Id must not be empty and must not only consist of spaces")
    private String id;

    @JsonProperty(required = true, value = "type")
    @NotNull(message = "Type must not be null!")
    @NotBlank(message = "Type must not be empty and must not only consist of spaces")
    @Pattern(message="Type must contain only of a-z, A-Z, 0-9, '.', '-'", regexp = PATTERN_TYPE)
    private String type;

    @JsonProperty(required = true, value = "source")
    @NotNull(message = "Source must not be null!")
    @NotBlank(message = "Source must not be empty and must not only consist of spaces")
    @Pattern(message = "Source must be a URL", regexp = PATTERN_URL)
    private String source;

    @JsonProperty(required = true, value = "specversion")
    @NotEmpty(message = "SpecVersion must not be empty!")
    @NotBlank(message = "SpecVersion must not be empty and must not only consist of spaces")
    private String specversion;

    @JsonProperty(value = "datacontenttype")
    private String datacontenttype;

    @JsonProperty(value = "dataschema")
    @Pattern(message = "dataschema must be a URL", regexp = PATTERN_URL)
    private String dataschema;

    @JsonProperty(value = "dataref")
    @NotNull
    @Pattern (message="dataref must be a URL", regexp = PATTERN_URL)
    private String dataref;

    @JsonProperty(value = "time")
    private String time;

    @SerializedName("data")
    @JsonProperty(value = "data")
    @NotNull
    private Object data = null;

    public Event data(Object data) {
        this.data = data;
        return this;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSpecversion() {
        return specversion;
    }

    public void setSpecversion(String specversion) {
        this.specversion = specversion;
    }

    public String getDatacontenttype() {
        return datacontenttype;
    }

    public void setDatacontenttype(String datacontenttype) {
        this.datacontenttype = datacontenttype;
    }

    public String getDataschema() {
        return dataschema;
    }

    public void setDataschema(String dataschema) {
        this.dataschema = dataschema;
    }

    public String getDataref() {
        return dataref;
    }

    public void setDataref(String dataref) {
        this.dataref = dataref;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Event event = (Event) o;
        return Objects.equals(this.data, event.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Event {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
