package com.anshu.antakshari;

public class rankListDataFirestore {
    String name;
    int points;
    public rankListDataFirestore()
    {

    }
    public rankListDataFirestore(String name,int points)
    {
        this.name=name;
        this.points=points;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }
}
