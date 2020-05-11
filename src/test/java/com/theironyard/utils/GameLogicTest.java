package com.theironyard.utils;

import com.theironyard.LiarsDiceApplication;
import com.theironyard.entities.GameState;
import com.theironyard.entities.Player;
import com.theironyard.services.GameStateRepository;
import com.theironyard.services.PlayerRepository;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by PiratePowWow on 4/5/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LiarsDiceApplication.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GameLogicTest {
    @Autowired
    GameStateRepository gameStates;
    @Autowired
    PlayerRepository players;
    @Autowired
    GameLogic gameLogic;
    @After
    public void clearDatabase(){
        players.deleteAll();
        gameStates.deleteAll();
    }


    @Test
    public void testRollDice() throws Exception {
        ArrayList<Integer> dice = gameLogic.rollDice();
        int count = 0;
        for(Integer die : dice){
            if (die < 7 && die > 0){
                count ++;
            }
        }
        assertTrue(count == 5);
    }

    @Test
    public void testIsValidRaise() throws Exception {
        GameState newGame = new GameState(gameLogic.makeRoomCode());
        Player bob = new Player(java.util.UUID.randomUUID().toString(), "Bob", gameLogic.rollDice(), new ArrayList<Integer>(Arrays.asList(3, 4)), 1, 2, newGame);
        Player tim = new Player(java.util.UUID.randomUUID().toString(), "Tim", gameLogic.rollDice(), new ArrayList<Integer>(Arrays.asList(5, 4)), 1, 2, newGame);
        newGame.setLastPlayerId(bob.getId());
        newGame.setActivePlayerId(tim.getId());
        gameStates.save(newGame);
        players.save(bob);
        players.save(tim);
        ArrayList<Integer> allDice = new ArrayList<>();
        ArrayList<Player> playersInGame = players.findByGameState(newGame);
        for (Player player: playersInGame) {
            allDice.addAll(player.getDice());
        }
        assertTrue(!gameLogic.isValidRaise(newGame, bob.getStake()));
        assertTrue(gameLogic.isValidRaise(newGame, tim.getStake()));
    }

    @Test
    public void testSetNextActivePlayer() throws Exception {
        GameState newGame = new GameState(gameLogic.makeRoomCode());
        String roomCode = newGame.getRoomCode();
        Player bob = new Player(java.util.UUID.randomUUID().toString(), "Bob", gameLogic.rollDice(), new ArrayList<Integer>(Arrays.asList(3, 4)), 1, 2, newGame);
        Player tim = new Player(java.util.UUID.randomUUID().toString(), "Tim", gameLogic.rollDice(), new ArrayList<Integer>(Arrays.asList(5, 4)), 1, 3, newGame);
//        newGame.setLastPlayerId(bob.getId());
//        newGame.setActivePlayerId(tim.getId());
        gameStates.save(newGame);
        players.save(bob);
        players.save(tim);
        gameLogic.setNextActivePlayer(roomCode);
        assertTrue(gameStates.findOne(roomCode).getActivePlayerId().equals(bob.getId()));
        gameLogic.setNextActivePlayer(roomCode);
        assertTrue(gameStates.findOne(roomCode).getActivePlayerId().equals(tim.getId()));
        gameLogic.setNextActivePlayer(roomCode);
        assertTrue(gameStates.findOne(roomCode).getActivePlayerId().equals(bob.getId()));
    }

    @Test
    public void testDetermineLoser() throws Exception {
        GameState newGame = new GameState(gameLogic.makeRoomCode());
        Player bob = new Player(java.util.UUID.randomUUID().toString(), "Bob", new ArrayList<Integer>(Arrays.asList(3, 4, 5, 2, 6)), new ArrayList<Integer>(Arrays.asList(3, 4)), 1, 2, newGame);
        Player tim = new Player(java.util.UUID.randomUUID().toString(), "Tim", new ArrayList<Integer>(Arrays.asList(3, 4, 5, 5, 5)), new ArrayList<Integer>(Arrays.asList(5, 4)), 1, 3, newGame);
        newGame.setLastPlayerId(bob.getId());
        newGame.setActivePlayerId(tim.getId());
        gameStates.save(newGame);
        players.save(bob);
        players.save(tim);
        assertTrue(gameLogic.determineLoser(newGame).getName().equals("Bob") );
    }

    @Test
    public void testMakeRoomCode() throws Exception {
        int i;
        boolean uniqueCode = false;
        for (i = 0; i < 1000; i++) {
            String newRoomCode = gameLogic.makeRoomCode();
            assertTrue(newRoomCode.length() == 4);
            if (gameStates.findOne(newRoomCode) != null){
                uniqueCode = false;
                break;
            }else{
                GameState newGame = new GameState(newRoomCode);
                Player bob = new Player(java.util.UUID.randomUUID().toString(), "Bob", gameLogic.rollDice(), new ArrayList<Integer>(Arrays.asList(3, 4)), 1, 2, newGame);
                Player tim = new Player(java.util.UUID.randomUUID().toString(), "Tim", gameLogic.rollDice(), new ArrayList<Integer>(Arrays.asList(5, 4)), 1, 2, newGame);
                newGame.setLastPlayerId(bob.getId());
                newGame.setActivePlayerId(tim.getId());
                gameStates.save(newGame);
                players.save(bob);
                players.save(tim);
                uniqueCode = true;
            }
        }
        assertTrue(uniqueCode);
    }

    @Test
    public void testResetGameState() throws Exception {
        String roomCode = "XXXX";
        GameState gameState = new GameState(roomCode);
        Player bob = new Player(java.util.UUID.randomUUID().toString(), "Bob", gameLogic.rollDice(), new ArrayList<Integer>(Arrays.asList(3, 4)), 1, 2, gameState);
        Player tim = new Player(java.util.UUID.randomUUID().toString(), "Tim", gameLogic.rollDice(), new ArrayList<Integer>(Arrays.asList(5, 4)), 1, 2, gameState);
        gameState.setActivePlayerId(bob.getId());
        gameState.setLastPlayerId(tim.getId());
        gameStates.save(gameState);
        players.save(bob);
        players.save(tim);
        ArrayList<Player> playerList = (ArrayList<Player>) players.findAll();
        GameState oldGame = gameStates.findOne(roomCode);
        assertTrue(oldGame.getRoomCode().equals(roomCode));
        assertTrue(playerList.size() == 2);
        gameLogic.resetGameState(roomCode);
        assertTrue(gameStates.findOne(roomCode).getActivePlayerId() == null);
        assertTrue(((ArrayList<Player>) players.findAll()).get(0).getDice() == null);
        assertTrue(((ArrayList<Player>) players.findAll()).get(1).getStake() == null);
    }
}