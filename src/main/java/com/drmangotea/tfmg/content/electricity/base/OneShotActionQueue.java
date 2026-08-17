package com.drmangotea.tfmg.content.electricity.base;

import java.io.Serial;
import java.util.ArrayList;
import java.util.function.Consumer;

public final class OneShotActionQueue<E> extends ArrayList<E> {
    @Serial
    private static final long serialVersionUID = 1L;

    public OneShotActionQueue() {
    }

    public void forEach(Consumer<? super E> consumer) {
        if (!this.isEmpty()) {
            ArrayList<E> list = new ArrayList<>(this);
            this.clear();
            for(E value : list) {
                consumer.accept(value);
            }
        }
    }
}
