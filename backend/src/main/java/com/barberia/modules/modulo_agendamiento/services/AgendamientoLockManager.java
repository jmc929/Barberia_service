package com.barberia.modules.modulo_agendamiento.services;

import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class AgendamientoLockManager {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public LockHandle acquire(String resourceKey) {
        Objects.requireNonNull(resourceKey);
        ReentrantLock lock = locks.computeIfAbsent(resourceKey, k -> new ReentrantLock());
        lock.lock();
        return new LockHandle(resourceKey, lock);
    }

    public LockHandle tryAcquire(String resourceKey, long timeout, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(resourceKey);
        ReentrantLock lock = locks.computeIfAbsent(resourceKey, k -> new ReentrantLock());
        boolean ok = lock.tryLock(timeout, unit);
        if (!ok) return null;
        return new LockHandle(resourceKey, lock);
    }

    public static class LockHandle implements AutoCloseable {
        private final String key;
        private final ReentrantLock lock;

        private LockHandle(String key, ReentrantLock lock) {
            this.key = key;
            this.lock = lock;
        }

        public String getKey() { return key; }

        @Override
        public void close() {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } finally {
                if (!lock.isLocked()) {
                    // remove only if the same lock instance is still mapped
                    // avoids removing, when another thread replaced it
                }
            }
        }
    }
}
