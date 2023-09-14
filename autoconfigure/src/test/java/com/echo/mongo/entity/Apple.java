package com.echo.mongo.entity;

import com.echo.mongo.index.CompoundIndex;
import com.echo.mongo.index.Indexed;
import com.echo.mongo.mapping.anno.Document;
import com.echo.mongo.mapping.anno.Field;
import com.echo.mongo.mapping.anno.Id;
import com.echo.mongo.model.Address;
import com.echo.mongo.model.Color;
import com.echo.mongo.model.Info;
import com.echo.mongo.model.Model;

import java.util.List;
import java.util.Map;

/**
 * @author: li-yuanwen
 */
@Document
@CompoundIndex(def = "{'n': 1, 'y': 1}")
public class Apple {

    @Id
    private long id;

    @Field(name = "n")
    @Indexed
    private String name;

    @Field(name = "c")
    private Color color;

    @Field(name = "a")
    private Address address;

    @Field(name = "i")
    private Map<Integer, Info> map;

    @Field(name = "m")
    private List<Model> list;

    @Field(name = "y")
    private int month;

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

    public Map<Integer, Info> getMap() {
        return map;
    }

    public void setMap(Map<Integer, Info> map) {
        this.map = map;
    }

    public List<Model> getList() {
        return list;
    }

    public void setList(List<Model> list) {
        this.list = list;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }
}
