package com.theironyard.controllers;

import com.theironyard.dtos.*;
import com.theironyard.entities.GameState;
import com.theironyard.entities.Player;
import com.theironyard.services.GameStateRepository;
import com.theironyard.services.PlayerRepository;
import com.theironyard.utils.GameLogic;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;


import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by PiratePowWow on 4/5/16.
 */
@Controller
public class LiarsDiceController {
    public static SimpMessagingTemplate messenger;
    @Autowired
    public LiarsDiceController(SimpMessagingTemplate template) {
        messenger = template;
    }

    @Autowired
    PlayerRepository players;
    @Autowired
    GameStateRepository gameStates;
    @Autowired
    GameLogic gameLogic;
    @PostConstruct
    public void init() throws SQLException {
        Server.createWebServer().start();
    }

    /**
     *
     * @param myPlayerId
     * @return
     */
    @MessageMapping("/lobby/{myPlayerId}")
    public PlayerDtoSansGameState myPlayer(@DestinationVariable String myPlayerId) {
        Player myPlayer = players.findOne(myPlayerId);
        if(myPlayer != null) {
            PlayerDtoSansGameState player = new PlayerDtoSansGameState(myPlayer);
            if (player == null) {
                messenger.convertAndSend("/topic/lobby/error/" + myPlayerId, "Your player Id could Not be found");
            }
            System.out.println("Returning Player Object");
            return player;
        }
        messenger.convertAndSend("/topic/lobby/error/" + myPlayerId, "Your Player Could Not Be Found - Likely Because You Did Not Enter A Valid Roomcode");
        return null;
    }

    /**
     *
     * @param id
     * @return
     */
    @MessageMapping("/lobby/JoinGame/{roomCode}")
    @SendTo("/topic/playerList/{roomCode}")
    public HashMap joinGame(String id, @DestinationVariable String roomCode) {
        Player playerJoiningGame = players.findOne(id);
        if (playerJoiningGame == null){
            messenger.convertAndSend("/topic/lobby/error/" + id, "Your player Id could Not be found");
        }
        GameState gameState = playerJoiningGame.getGameState();
        roomCode = gameState.getRoomCode();
        PlayersDto playerDtos = new PlayersDto(players.findByGameStateOrderBySeatNum(gameState));
        HashMap playerListAndGameState = new HashMap();
        playerListAndGameState.put("playerList", playerDtos);
        playerListAndGameState.put("gameState", new GameStateDto(gameStates.findOne(roomCode), players));
        System.out.println("Joining Game");
        return playerListAndGameState;
    }

    /**
     *
     * @param id
     * @return
     */
    @MessageMapping("/lobby/rollDice/{roomCode}")
    @SendTo("/topic/playerList/{roomCode}")
    public HashMap rollDice(String id, @DestinationVariable String roomCode) {
        Player playerRollingDice = players.findOne(id);
        if (playerRollingDice == null){
            messenger.convertAndSend("/topic/lobby/error/" + id, "Your player Id could Not be found");
        }
        GameState gameState = playerRollingDice.getGameState();
        roomCode = gameState.getRoomCode();
        if(playerRollingDice.getDice() == null){
            playerRollingDice.setDice(gameLogic.rollDice());
            players.save(playerRollingDice);
        }else{
            messenger.convertAndSend("/topic/lobby/error/" + id, "You already have dice. Please reset the game if you wish to get new dice");
        }
        if (gameLogic.allDiceRolled(roomCode) && gameState.getLoserId() == null && gameState.getActivePlayerId() == null){
            gameLogic.setNextActivePlayer(roomCode);
        }else if(gameLogic.allDiceRolled(roomCode)&& gameState.getActivePlayerId() == null){
            gameState.setActivePlayerId(gameState.getLoserId());
            gameStates.save(gameState);
        }
        PlayersDto playerDtos = new PlayersDto(players.findByGameStateOrderBySeatNum(gameState));
        HashMap playerListAndGameState = new HashMap();
        playerListAndGameState.put("playerList", playerDtos);
        playerListAndGameState.put("gameState", new GameStateDto(gameStates.findOne(roomCode), players));
        messenger.convertAndSend("/topic/lobby/" + id, new PlayerDtoSansGameState(playerRollingDice));
        System.out.println("Rolling Dice");
        return playerListAndGameState;
    }

    /**
     *
     * @param stake
     * @return
     */
    @MessageMapping("/lobby/setStake/{roomCode}")
    @SendTo("/topic/playerList/{roomCode}")
    public HashMap setStake(HashMap<String, Object> stake, @DestinationVariable String roomCode) throws InterruptedException {
        String id = (String) stake.get("playerId");
        ArrayList<Integer> newStake = (ArrayList<Integer>) stake.get("newStake");
        Player playerSettingStake = players.findOne(id);
        if (newStake != null && newStake.size() == 2 && newStake.get(0) != null && newStake.get(1) != null) {
            if (newStake.get(0) > 0 && newStake.get(1) > 0 && newStake.get(1) < 7 && newStake.size() == 2) {
                if (playerSettingStake == null) {
                    messenger.convertAndSend("/topic/lobby/error/" + id, "Your player Id could Not be found");
                }
                GameState gameState = playerSettingStake.getGameState();
                roomCode = gameState.getRoomCode();
                if (gameState.getActivePlayerId() == null){
                    LiarsDiceController.messenger.convertAndSend("/topic/lobby/error/" + id, "No active player has been set because all players have not yet rolled their dice");
                }
                if (playerSettingStake.getDice() == null) {
                    gameLogic.setNextActivePlayer(roomCode);
                    PlayersDto playerDtos = new PlayersDto(players.findByGameStateOrderBySeatNum(gameState));
                    HashMap playerListAndGameState = new HashMap();
                    playerListAndGameState.put("playerList", playerDtos);
                    playerListAndGameState.put("gameState", new GameStateDto(gameStates.findOne(roomCode), players));
                    messenger.convertAndSend("/topic/lobby/error/" + id, "You must have Dice in order to raise the stake, your turn is being skipped");
                    System.out.println("Setting Stake");
                    return playerListAndGameState;
                }
                if (gameLogic.isActivePlayer(id) && gameLogic.isValidRaise(gameState, newStake)) {
                    playerSettingStake.setStake(newStake);
                    players.save(playerSettingStake);
                    gameLogic.setNextActivePlayer(roomCode);
                } else if (gameLogic.isActivePlayer(id)) {
                    messenger.convertAndSend("/topic/lobby/error/" + id, "You are the active player, but you did not submit a valid stake");
                } else if (gameLogic.isValidRaise(gameState, newStake)) {
                    messenger.convertAndSend("/topic/lobby/error/" + id, "The stake is valid, but you are not the active player");
                }
                PlayersDto playerDtos = new PlayersDto(players.findByGameStateOrderBySeatNum(gameState));
                HashMap playerListAndGameState = new HashMap();
                playerListAndGameState.put("playerList", playerDtos);
                playerListAndGameState.put("gameState", new GameStateDto(gameStates.findOne(roomCode), players));
                messenger.convertAndSend("/topic/lobby/" + id, new PlayerDtoSansGameState(playerSettingStake));
                System.out.println("Setting Stake");
                return playerListAndGameState;
            }
        }
        PlayersDto playerDtos = new PlayersDto(players.findByGameStateOrderBySeatNum(playerSettingStake.getGameState()));
        HashMap playerListAndGameState = new HashMap();
        playerListAndGameState.put("playerList", playerDtos);
        playerListAndGameState.put("gameState", new GameStateDto(gameStates.findOne(roomCode), players));
        messenger.convertAndSend("/topic/lobby/error/" + id, "Invalid Stake");
        System.out.println("Setting Stake");
        return playerListAndGameState;

    }

    /**
     *
     * @param id
     * @return
     */
    @MessageMapping("/lobby/resetGame/{roomCode}")
    @SendTo("/topic/playerList/{roomCode}")
    public HashMap resetGame(String id, @DestinationVariable String roomCode){
        Player playerRequestingReset = players.findOne(id);
        if (playerRequestingReset == null){
            messenger.convertAndSend("/topic/lobby/error/" + id, "Your player Id could Not be found");
        }
        GameState gameState = playerRequestingReset.getGameState();
        roomCode = gameState.getRoomCode();
        gameLogic.resetGameState(roomCode);
        GameState newGame = playerRequestingReset.getGameState();
        //gameLogic.setNextActivePlayer(roomCode);
        PlayersDto playerDtos = new PlayersDto(players.findByGameStateOrderBySeatNum(newGame));
        HashMap playerListAndGameState = new HashMap();
        playerListAndGameState.put("playerList", playerDtos);
        playerListAndGameState.put("gameState", new GameStateDto(gameStates.findOne(roomCode), players));
        System.out.println("Resetting Game");
        return playerListAndGameState;
    }

    /**
     *
     * @param id
     * @return
     */
    @MessageMapping("/lobby/callBluff/{roomCode}")
    @SendTo("/topic/loser/{roomCode}")
    public PlayerDto callBluff(String id, @DestinationVariable String roomCode) throws Exception {
        Player playerCallingBluff = players.findOne(id);
        if (playerCallingBluff == null){
            messenger.convertAndSend("/topic/lobby/error/" + id, "Your player Id could Not be found");
        }
        GameState gameState = playerCallingBluff.getGameState();
        if (gameState.getActivePlayerId() != null) {
            PlayerDto loserDto;
            if (gameLogic.isActivePlayer(id)) {
                Player loser = gameLogic.determineLoser(gameState);
                gameState = gameStates.findOne(gameState.getRoomCode());
                gameState.setLoserId(loser.getId());
                gameStates.save(gameState);
                loserDto = new PlayerDto(loser);
                gameLogic.resetGameState(roomCode);
                System.out.println("Calling Bluff");
                return loserDto;
            }
            gameState.setLoserId(playerCallingBluff.getId());
            gameStates.save(gameState);
            loserDto = new PlayerDto(playerCallingBluff);
            gameLogic.resetGameState(roomCode);
            System.out.println("Calling Bluff");
            return loserDto;
        }else{
            messenger.convertAndSend("/topic/lobby/error/" + id, "Waiting for players to roll dice");
            throw new Exception("Waiting for players to roll dice");
        }
    }
}
