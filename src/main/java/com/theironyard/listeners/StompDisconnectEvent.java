package com.theironyard.listeners;

import com.theironyard.controllers.LiarsDiceController;
import com.theironyard.dtos.GameStateDto;
import com.theironyard.dtos.PlayerDto;
import com.theironyard.dtos.PlayerDtoSansGameState;
import com.theironyard.dtos.PlayersDto;
import com.theironyard.entities.GameState;
import com.theironyard.entities.Player;
import com.theironyard.services.GameStateRepository;
import com.theironyard.services.PlayerRepository;
import com.theironyard.utils.GameLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;

/**
 * Created by PiratePowWow on 4/11/16.
 */
@Component
public class StompDisconnectEvent implements ApplicationListener<SessionDisconnectEvent> {
    @Autowired
    GameLogic gameLogic;
    @Autowired
    GameStateRepository gameStates;
    @Autowired
    PlayerRepository players;

    private final Log logger = LogFactory.getLog(StompDisconnectEvent.class);

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        Player disconnectingPlayer = players.findOne(sessionId);
        if (disconnectingPlayer != null) {
            GameState gameState = disconnectingPlayer.getGameState();
            String roomCode = gameState.getRoomCode();
            gameLogic.dropPlayer(sessionId);
            if (gameStates.findOne(roomCode) != null) {
//                if (gameState.getActivePlayerId() != null) {
//                    if (gameState.getActivePlayerId().equals(sessionId)) {
//                        gameLogic.setNextActivePlayer(gameState.getRoomCode());
//                        gameState = gameStates.findOne(roomCode);
//                    }
//                }
                PlayerDto disconnectingLoser = new PlayerDto(disconnectingPlayer);
                LiarsDiceController.messenger.convertAndSend("/topic/loser/" + roomCode, disconnectingLoser);
                gameLogic.resetGameState(roomCode);
                gameState = gameStates.findOne(roomCode);
                PlayersDto playerDtos = new PlayersDto(players.findByGameStateOrderBySeatNum(gameState));
                HashMap playerListAndGameState = new HashMap();
                playerListAndGameState.put("playerList", playerDtos);
                playerListAndGameState.put("gameState", new GameStateDto(gameState, players));
                LiarsDiceController.messenger.convertAndSend("/topic/playerList/" + roomCode, playerListAndGameState);
            }
        }
        System.out.println("Disconnect event [sessionId:" + sessionId + "]");
        logger.debug("Disconnect event [sessionId: " + sessionId + "]");
    }
}
