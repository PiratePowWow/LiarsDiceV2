package com.theironyard.services;

import com.theironyard.entities.GameState;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by PiratePowWow on 4/5/16.
 */
public interface GameStateRepository extends CrudRepository<GameState, String> {
}
