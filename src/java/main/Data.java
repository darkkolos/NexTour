package main;

import java.io.Serializable;
import java.sql.Timestamp;

public class Data implements Serializable {

    static final long serialVersionUID = 1;

    private long id;
    private Timestamp data;

    public Data(long id, Timestamp data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public Timestamp getData() {
        return data;
    }
}
