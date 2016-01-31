package temperatus.model;

import temperatus.model.pojo.Position;

/**
 * Created by alberto on 31/1/16.
 */
public class RecordTableElement {

    private Position position;
    private String source;
    private int id;

    public RecordTableElement(int id, Position position, String source) {
        this.position = position;
        this.source = source;
        this.id = id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "RecordTableElement{" +
                "position=" + position +
                ", source='" + source + '\'' +
                ", id=" + id +
                '}';
    }
}
