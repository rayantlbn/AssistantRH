package com.solvia.assistantrh.test.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Horloge fixe (Clock.fixed) que les tests peuvent déplacer pour simuler le passage du temps.
 */
public class TestClock extends Clock {

    private Clock delegate;

    public TestClock(Instant instant) {
        setInstant(instant);
    }

    public void setInstant(Instant instant) {
        this.delegate = Clock.fixed(instant, ZoneOffset.UTC);
    }

    @Override
    public ZoneId getZone() {
        return delegate.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return delegate.withZone(zone);
    }

    @Override
    public Instant instant() {
        return delegate.instant();
    }
}
