package com.anshu.antakshari;

public class rankListDataFirestore {
    String name;
    int index;
    int points;
    public rankListDataFirestore()
    {

    }
    public rankListDataFirestore(String name,int index,int points)
    {
        this.name=name;
        this.index=index;
        this.points=points;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public int getPoints() {
        return points;
    }
}
