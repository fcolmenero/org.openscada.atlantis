package org.openscada.ae.server.storage.postgres.internal;

import org.openscada.ae.Event;
import org.openscada.ae.server.storage.StoreListener;

public class StoreTask
{

    private final StoreListener listener;

    private final Event eventToStore;

    private final boolean storeInErrorQueue;

    public StoreTask ( final StoreListener listener, final Event eventToStore, final boolean storeInErrorQueue )
    {
        this.listener = listener;
        this.eventToStore = eventToStore;
        this.storeInErrorQueue = storeInErrorQueue;
    }

    public StoreListener getListener ()
    {
        return listener;
    }

    public Event getEventToStore ()
    {
        return eventToStore;
    }

    public boolean isStoreInErrorQueue ()
    {
        return storeInErrorQueue;
    }
}
