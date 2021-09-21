package me.brennan.electrum.model;

/**
 * @author Brennan
 * @since 9/21/21
 **/
public class Height {
    private int lastHeight;

    public Height(int lastHeight) {
        this.lastHeight = lastHeight;
    }

    public void setLastHeight(int lastHeight) {
        this.lastHeight = lastHeight;
    }

    public int getLastHeight() {
        return lastHeight;
    }
}
