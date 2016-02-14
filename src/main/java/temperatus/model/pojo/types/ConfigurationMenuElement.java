package temperatus.model.pojo.types;

/**
 * Created by alberto on 14/2/16.
 */
public class ConfigurationMenuElement {

    String title;
    String view;

    public ConfigurationMenuElement(String title, String view) {
        this.title = title;
        this.view = view;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    @Override
    public String toString() {
        return title;
    }
}
