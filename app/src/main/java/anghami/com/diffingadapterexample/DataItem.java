package anghami.com.diffingadapterexample;

/**
 * Created on 04/04/2018.
 */

public class DataItem {
    private final String id;
    private String description;

    public DataItem(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public boolean isSameItem(DataItem other) {
        return getId().equals(other.getId());
    }

    public boolean hasSameContent(DataItem other) {
        return getDescription().equals(other.getDescription());
    }

    public String getId() {
        if (id == null) {
            return "";
        }
        return id;
    }

    public String getDescription() {
        if (description == null) {
            return "";
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataItem copy() {
        return new DataItem(id, description);
    }
}
