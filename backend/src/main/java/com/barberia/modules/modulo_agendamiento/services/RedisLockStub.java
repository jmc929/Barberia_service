package com.barberia.modules.modulo_agendamiento.services;

import org.springframework.stereotype.Component;

@Component
public class RedisLockStub {

    public boolean acquire(String key, long ttlMillis) {
        // TODO: integrate Redis client
        return false;
    }

    public void release(String key) {
        // TODO: release redis lock
    }
}
