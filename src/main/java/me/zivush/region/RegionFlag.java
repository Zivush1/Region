package me.zivush.region;

public class RegionFlag {

    public enum State {
        NONE,
        WHITELIST,
        EVERYONE
    }

    private String name;
    private State state;

    public RegionFlag(String name, State state) {
        this.name = name;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
