/*
 * This file is part of the OpenSCADA project
 * 
 * Copyright (C) 2006-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
 * Copyright (C) 2013 Jens Reimann (ctron@dentrassi.de)
 *
 * OpenSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * OpenSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with OpenSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.da.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openscada.da.core.Location;
import org.openscada.da.core.browser.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderSyncController extends FolderWatcher
{
    private final static Logger logger = LoggerFactory.getLogger ( FolderSyncController.class );

    private final Set<FolderListener> listener = new HashSet<FolderListener> ();

    private final Connection connection;

    private boolean subscribed = false;

    public FolderSyncController ( final Connection connection, final Location location )
    {
        super ( location );
        this.connection = connection;
        this.connection.setFolderListener ( location, this );
    }

    public void addListener ( final FolderListener listener )
    {
        synchronized ( this )
        {
            if ( this.listener.add ( listener ) )
            {
                sync ();
            }
        }
        transmitCache ( listener );
    }

    public synchronized void removeListener ( final FolderListener listener )
    {
        if ( this.listener.remove ( listener ) )
        {
            sync ();
        }
    }

    public synchronized void sync ()
    {
        sync ( false );
    }

    public synchronized void resync ()
    {
        sync ( true );
    }

    private synchronized void sync ( final boolean force )
    {
        final boolean needSubscription = !this.listener.isEmpty ();

        if ( needSubscription != this.subscribed || force )
        {
            if ( needSubscription )
            {
                subscribe ();
            }
            else
            {
                unsubscribe ();
            }
        }
    }

    private synchronized void subscribe ()
    {
        logger.debug ( "subscribing to folder: {}", this.location );

        this.subscribed = true;

        try
        {
            this.connection.subscribeFolder ( this.location );
        }
        catch ( final Exception e )
        {
            handleError ( e );
        }
    }

    private synchronized void unsubscribe ()
    {
        logger.debug ( "unsubscribing from folder: {}", this.location );

        this.subscribed = false;

        try
        {
            this.connection.unsubscribeFolder ( this.location );
        }
        catch ( final Exception e )
        {
            handleError ( e );
        }
    }

    protected void handleError ( final Throwable e )
    {
        this.subscribed = false;
    }

    private void transmitCache ( final FolderListener listener )
    {
        listener.folderChanged ( Collections.unmodifiableList ( new ArrayList<Entry> ( this.cache.values () ) ), Collections.<String> emptySet (), true );
    }

    @Override
    public void folderChanged ( final Collection<Entry> added, final Collection<String> removed, final boolean full )
    {
        super.folderChanged ( added, removed, full );

        for ( final FolderListener listener : this.listener )
        {
            listener.folderChanged ( added, removed, full );
        }
    }

    public void disconnected ()
    {
        synchronized ( this )
        {
            this.subscribed = false;
            this.cache.clear ();
        }

        for ( final FolderListener listener : this.listener )
        {
            listener.folderChanged ( Collections.<Entry> emptyList (), Collections.<String> emptySet (), true );
        }
    }
}
