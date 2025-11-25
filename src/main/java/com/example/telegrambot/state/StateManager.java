package com.example.telegrambot.state;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateManager {
    private static final StateManager INSTANCE = new StateManager();
    private final Map<Long, UserState> states = new ConcurrentHashMap<>();

    private StateManager() {}

    public static StateManager getInstance() {
        return INSTANCE;
    }

    public void setState(long userId, UserState state) {
        states.put(userId, state);
    }

    public UserState getState(long userId) {
        return states.getOrDefault(userId, UserState.NONE);
    }

    public void clearState(long userId) {
        states.remove(userId);
    }
}
