package me.guendouz.googlefithistory;

import java.util.ArrayList;
import java.util.List;

public class Reward {

    private int id;
    private String name;
    private int imageId;
    private int steps;

    public Reward(int id, String name, int imageId, int steps) {
        this.id = id;
        this.name = name;
        this.imageId = imageId;
        this.steps = steps;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public static List<Reward> generateSampleRewards() {
        ArrayList<Reward> rewardArrayList = new ArrayList<>();
        rewardArrayList.add(new Reward(1, "Coffee Cup", R.drawable.coffee_cup, 1000));
        rewardArrayList.add(new Reward(2, "Power Bank", R.drawable.powerbank, 7000));
        return rewardArrayList;
    }
}
