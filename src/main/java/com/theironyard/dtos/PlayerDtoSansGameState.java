package com.theironyard.dtos;

import com.theironyard.entities.Player;

import java.util.ArrayList;

/**
 * Created by PiratePowWow on 4/12/16.
 */
public class PlayerDtoSansGameState {
    private String id;
    private String name;
    private String roomCode;
    private ArrayList<Integer> dice;
    private ArrayList<Integer> stake;
    private int score;
    private int seatNum;

    public PlayerDtoSansGameState(Player player) {
        this.id = player.getId();
        this.name = player.getName();
        this.roomCode = player.getGameState() != null ? player.getGameState().getRoomCode() : null;
        this.dice = player.getDice();
        this.stake = player.getStake();
        this.score = player.getScore();
        this.seatNum = player.getSeatNum();
    }

    public PlayerDtoSansGameState() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public ArrayList<Integer> getDice() {
        return dice;
    }

    public void setDice(ArrayList<Integer> dice) {
        this.dice = dice;
    }

    public ArrayList<Integer> getStake() {
        return stake;
    }

    public void setStake(ArrayList<Integer> stake) {
        this.stake = stake;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }
}
