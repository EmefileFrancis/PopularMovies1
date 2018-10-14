package com.emefilefrancis.popular_movies_1.Models;

/**
 * Created by SP on 10/12/2018.
 */

public class Trailer {
    private String id;
    private String key;
    private String name;

    public Trailer(String id, String key, String name) {
        this.id = id;
        this.key = key;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
