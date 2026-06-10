package com.barberia.modules.modulo_agendamiento.services;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AgendamientoLockManagerTest {

    private final AgendamientoLockManager lockManager = new AgendamientoLockManager();

    @Test
    void acquire_obtieneLock() {
        AgendamientoLockManager.LockHandle handle = lockManager.acquire("test-key");
        assertNotNull(handle);
        assertEquals("test-key", handle.getKey());
        handle.close();
    }

    @Test
    void acquire_y_close_liberaLock() {
        AgendamientoLockManager.LockHandle handle = lockManager.acquire("test-key");
        handle.close();
        AgendamientoLockManager.LockHandle handle2 = lockManager.acquire("test-key");
        assertNotNull(handle2);
        handle2.close();
    }

    @Test
    void tryAcquire_conTimeout_exitoso() throws InterruptedException {
        AgendamientoLockManager.LockHandle handle = lockManager.tryAcquire("test-key", 100, TimeUnit.MILLISECONDS);
        assertNotNull(handle);
        assertEquals("test-key", handle.getKey());
        handle.close();
    }

    @Test
    void acquire_conKeyNulo_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> lockManager.acquire(null));
    }

    @Test
    void tryAcquire_conKeyNulo_lanzaExcepcion() {
        assertThrows(NullPointerException.class,
                () -> lockManager.tryAcquire(null, 100, TimeUnit.MILLISECONDS));
    }
}
