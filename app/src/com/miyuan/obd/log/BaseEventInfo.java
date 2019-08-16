package com.miyuan.obd.log;

/**
 * @author baimi
 */
public class BaseEventInfo<E extends Enum<?>> implements IEventInfo<E> {

    private E event;

    @Override
    public E getEvent() {
        return this.event;
    }

    @Override
    public void setEvent(E event) {
        this.event = event;
    }

}
