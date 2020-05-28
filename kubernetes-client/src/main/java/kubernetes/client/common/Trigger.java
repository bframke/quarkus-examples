package kubernetes.client.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize
public class Trigger {

    @JsonProperty("responseFilter")
    private List<String> responseFilter = null;

    @JsonProperty("selectionFilter")
    private Map<String,String> selectionFilter = null;

    public Trigger responseFilter(List<String> responseFilter) {
        this.responseFilter = responseFilter;
        return this;
    }

    public Trigger addResponseFilterItem(String responseFilterItem) {
        if (this.responseFilter == null) {
            this.responseFilter = new ArrayList<>();
        }
        this.responseFilter.add(responseFilterItem);
        return this;
    }

    public List<String> getResponseFilter() {
        return responseFilter;
    }

    public void setResponseFilter(List<String> responseFilter) {
        this.responseFilter = responseFilter;
    }

    public Trigger selectionFilter(Map<String,String> selectionFilter) {
        this.selectionFilter = selectionFilter;
        return this;
    }

    public Map<String,String> getSelectionFilter() {
        return selectionFilter;
    }

    public void setSelectionFilter(Map<String,String> selectionFilter) {
        this.selectionFilter = selectionFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Trigger trigger = (Trigger) o;
        return Objects.equals(this.responseFilter, trigger.responseFilter) &&
                Objects.equals(this.selectionFilter, trigger.selectionFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseFilter, selectionFilter);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Trigger {\n");

        sb.append("    responseFilter: ").append(toIndentedString(responseFilter)).append("\n");
        sb.append("    selectionFilter: ").append(toIndentedString(selectionFilter)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
