package com.echo.entity;

import com.echo.model.Address;
import com.echo.model.Color;
import com.echo.mongo.mapping.anno.Document;
import com.echo.mongo.mapping.anno.Field;
import com.echo.mongo.mapping.anno.Id;

/**
 * @author: li-yuanwen
 */
@Document
public class Apple {

    @Id
    private long id;

    @Field(name = "n")
    private String name;

    @Field(name = "c")
    private Color color;

    @Field(name = "a")
    private Address address;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
