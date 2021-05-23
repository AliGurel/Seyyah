package com.example.seyyah1.model;

import java.io.Serializable;

public class Yerler implements Serializable {
    // veritabanına kaydeilecek olan adresleri barındıran Sınıf modelimiz
    public String name;
    public Double latitude;
    public Double longitude;

    public Yerler(String name, Double latitude, Double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return this.name;
    }
}
