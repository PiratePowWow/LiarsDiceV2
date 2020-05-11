package com.theironyard.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

/**
 * Created by PiratePowWow on 4/5/16.
 */
@Entity
@Table(name = "players")
public class Player {
    @Id
    private String id;
    @NotNull
    private String name;
    private ArrayList<Integer> dice;
    private ArrayList<Integer> stake;
    private int score = 0;
    @NotNull
    private int seatNum;
    @ManyToOne(fetch=FetchType.EAGER)
    GameState gameState;

    public Player(String id, String name, ArrayList<Integer> dice, ArrayList<Integer> stake, int score, int seatNum, GameState gameState) {
        this.id = id;
        this.name = name;
        this.dice = dice;
        this.stake = stake;
        this.score = score;
        this.seatNum = seatNum;
        this.gameState = gameState;
    }

    public Player(String id, String name, ArrayList<Integer> dice, ArrayList<Integer> stake, int score, int seatNum) {
        this.id = id;
        this.name = name;
        this.dice = dice;
        this.stake = stake;
        this.score = score;
        this.seatNum = seatNum;
    }

    public Player(String id, String name, int seatNum) {
        this.id = id;
        this.name = name;
        this.seatNum = seatNum;
    }

    public Player() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return id.equals(player.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
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

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
