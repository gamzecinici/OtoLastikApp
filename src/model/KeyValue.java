package model;

public class KeyValue {
    private int id;
    private String name;

    public KeyValue(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // ComboBox'ta sadece ismi göster
    @Override
    public String toString() {
        return name;
    }
}
