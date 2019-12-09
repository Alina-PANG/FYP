package fyp.app.db;

import javax.persistence.*;

@Entity
@Table
@SequenceGenerator(name="seq", initialValue=1)
public class Component {
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq")
    @Id
    private long id;

    private int time;
    private String inputFile;
    private int size;
    private int index;

    public Component() {
    }

    public Component(int time, String inputFile, int size, int index) {
        this.time = time;
        this.inputFile = inputFile;
        this.size = size;
        this.index = index;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
