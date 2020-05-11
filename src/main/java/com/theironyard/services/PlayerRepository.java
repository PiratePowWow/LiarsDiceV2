package com.theironyard.services;

import com.theironyard.entities.GameState;
import com.theironyard.entities.Player;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by PiratePowWow on 4/5/16.
 */
public interface PlayerRepository extends CrudRepository<Player, String> {
    ArrayList<Player> findByGameState(GameState gameState);
    ArrayList<Player> findByGameStateOrderBySeatNum(GameState gameState);
    @Query(value = "Select players.dice from players where players.game_state_roomcode=?1 and dice is not null", nativeQuery = true)
    ArrayList<ArrayList<Integer>> findDiceByGameState(String roomcode);
}
