/**
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rx.operations;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.ThreadSafe;

import org.rx.reactive.Subscription;

/**
 * Thread-safe wrapper around ObservableSubscription that ensures unsubscribe can be called only once.
 */
@ThreadSafe
/* package */final class AtomicObservableSubscription implements Subscription {

    private AtomicReference<Subscription> actualSubscription = new AtomicReference<Subscription>();
    private AtomicBoolean unsubscribed = new AtomicBoolean(false);

    public AtomicObservableSubscription() {

    }

    public AtomicObservableSubscription(Subscription actualSubscription) {
        this.actualSubscription.set(actualSubscription);
    }

    /**
     * Set the actual subscription once it exists (if it wasn't available when constructed)
     * 
     * @param actualSubscription
     * @throws IllegalStateException
     *             if trying to set more than once (or use this method after setting via constructor)
     */
    public AtomicObservableSubscription setActual(Subscription actualSubscription) {
        if (!this.actualSubscription.compareAndSet(null, actualSubscription)) {
            throw new IllegalStateException("Can not set subscription more than once.");
        }
        return this;
    }

    @Override
    public void unsubscribe() {
        // get the real thing and set to null in an atomic operation so we will only ever call unsubscribe once
        Subscription actual = actualSubscription.getAndSet(null);
        // if it's not null we will unsubscribe
        if (actual != null) {
            actual.unsubscribe();
            unsubscribed.set(true);
        }
    }

    public boolean isUnsubscribed() {
        return unsubscribed.get();
    }
}
