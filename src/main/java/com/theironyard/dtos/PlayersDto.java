package com.theironyard.dtos;

import com.theironyard.entities.Player;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by PiratePowWow on 4/12/16.
 */
public class PlayersDto {
    ArrayList<PlayerDto> playerDtos;

    public PlayersDto(ArrayList<Player> players) {
        ArrayList<PlayerDto> playersToBroadcast;
        playersToBroadcast = players.parallelStream().map(PlayerDto::new).collect(Collectors.toCollection(ArrayList<PlayerDto>::new));
        this.playerDtos = playersToBroadcast;
    }

    public PlayersDto() {
    }

    public ArrayList<PlayerDto> getPlayerDtos() {
        return playerDtos;
    }

    public void setPlayerDtos(ArrayList<PlayerDto> playerDtos) {
        this.playerDtos = playerDtos;
    }
}
