package com.theironyard.dtos;

import com.theironyard.entities.Player;

import java.util.ArrayList;

/**
 * Created by PiratePowWow on 4/5/16.
 */
public class PlayerDto {
    private String name;
    private String roomCode;
    private ArrayList<Integer> stake;
    private int seatNum;
    private int score = 0;
    private boolean isDiceRolled;

    public PlayerDto(String name, String roomCode, ArrayList<Integer> stake, int seatNum, int score, boolean isDiceRolled) {
        this.name = name;
        this.roomCode = roomCode;
        this.stake = stake;
        this.seatNum = seatNum;
        this.score = score;
        this.isDiceRolled = isDiceRolled;
    }

    public PlayerDto(Player player) {
        this.name = player.getName();
        this.roomCode = player.getGameState() == null? null : player.getGameState().getRoomCode();
        this.stake = player.getStake();
        this.seatNum = player.getSeatNum();
        this.score = player.getScore();
        this.isDiceRolled = player.getDice() != null;
    }

    public PlayerDto() {
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isDiceRolled() {
        return isDiceRolled;
    }

    public void setDiceRolled(boolean diceRolled) {
        isDiceRolled = diceRolled;
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

    public ArrayList<Integer> getStake() {
        return stake;
    }

    public void setStake(ArrayList<Integer> stake) {
        this.stake = stake;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }
}
