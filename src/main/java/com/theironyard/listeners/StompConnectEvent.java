package com.theironyard.listeners;

import com.theironyard.controllers.LiarsDiceController;
import com.theironyard.services.GameStateRepository;
import com.theironyard.utils.GameLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

/**
 * Created by PiratePowWow on 4/11/16.
 */
@Component
public class StompConnectEvent implements ApplicationListener<SessionConnectEvent> {
    @Autowired
    GameLogic gameLogic;
    @Autowired
    GameStateRepository gameStates;

    private final Log logger = LogFactory.getLog(StompConnectEvent.class);

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String roomCode =  sha.getNativeHeader("roomCode").get(0);
        String  name = sha.getNativeHeader("name").get(0);
        if(roomCode.equals("undefined")){
            gameLogic.createNewGame(name, sessionId);
        }else if (gameStates.findOne(roomCode) != null){
            gameLogic.addPlayer(name, roomCode, sessionId);
        }else{
            LiarsDiceController.messenger.convertAndSend("/topic/lobby/error/" + sessionId, "Invalid room code");
        }
        System.out.println("Connect event [sessionId: " + sessionId +"; name: "+ name + " ]");
        logger.debug("Connect event [sessionId: " + sessionId +"; name: "+ name + " ]");
    }
}
