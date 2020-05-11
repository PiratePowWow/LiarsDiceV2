package com.theironyard.dtos;

import java.util.ArrayList;

/**
 * Created by PiratePowWow on 4/14/16.
 */
public class Stake {
    private String playerId;
    private ArrayList<Integer> newStake;

    public Stake(String playerId, ArrayList<Integer> newStake) {
        this.playerId = playerId;
        this.newStake = newStake;
    }

    public Stake() {
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public ArrayList<Integer> getNewStake() {
        return newStake;
    }

    public void setNewStake(ArrayList<Integer> newStake) {
        this.newStake = newStake;
    }
}
