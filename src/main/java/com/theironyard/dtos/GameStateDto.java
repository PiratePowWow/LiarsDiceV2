package com.theironyard.dtos;

import com.theironyard.entities.GameState;
import com.theironyard.services.PlayerRepository;

/**
 * Created by PiratePowWow on 4/5/16.
 */
public class GameStateDto {
    private String roomCode;
    private Integer activePlayerSeatNum;
    private Integer lastPlayerSeatNum;

    public GameStateDto(GameState gameState, PlayerRepository players) {
        this.roomCode = gameState.getRoomCode();
        this.activePlayerSeatNum = gameState.getActivePlayerId() == null ? null : players.findOne(gameState.getActivePlayerId()).getSeatNum();
        this.lastPlayerSeatNum = gameState.getLastPlayerId() == null ? null : players.findOne(gameState.getLastPlayerId()).getSeatNum();
    }

    public GameStateDto() {
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public Integer getActivePlayerSeatNum() {
        return activePlayerSeatNum;
    }

    public void setActivePlayerSeatNum(Integer activePlayerSeatNum) {
        this.activePlayerSeatNum = activePlayerSeatNum;
    }

    public Integer getLastPlayerSeatNum() {
        return lastPlayerSeatNum;
    }

    public void setLastPlayerSeatNum(Integer lastPlayerSeatNum) {
        this.lastPlayerSeatNum = lastPlayerSeatNum;
    }
}