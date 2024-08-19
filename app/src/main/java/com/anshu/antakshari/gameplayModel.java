package com.anshu.antakshari;

public class gameplayModel {
    String name;
    int points;
    String typingStatusStr;
    String profileUrl;
    int profileResource;
    public gameplayModel(String name, int points, String typingStatusStr,  int profileResource) {
        this.name = name;
        this.points = points;
        this.typingStatusStr = typingStatusStr;
        this.profileResource = profileResource;
    }
    public gameplayModel(String name, int points, String typingStatusStr, String profileUrl) {
        this.name = name;
        this.points = points;
        this.typingStatusStr = typingStatusStr;
        this.profileUrl = profileUrl;

    }

}
