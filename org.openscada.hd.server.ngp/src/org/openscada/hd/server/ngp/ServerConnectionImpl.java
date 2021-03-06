/*
 * This file is part of the openSCADA project
 * Copyright (C) 2011-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
 *
 * openSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * openSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with openSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.hd.server.ngp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.session.IoSession;
import org.openscada.core.InvalidSessionException;
import org.openscada.core.data.ErrorInformation;
import org.openscada.core.data.Response;
import org.openscada.core.server.ngp.ServiceServerConnection;
import org.openscada.hd.InvalidItemException;
import org.openscada.hd.ItemListListener;
import org.openscada.hd.Query;
import org.openscada.hd.QueryState;
import org.openscada.hd.data.HistoricalItemInformation;
import org.openscada.hd.data.QueryParameters;
import org.openscada.hd.data.ValueEntry;
import org.openscada.hd.data.ValueInformation;
import org.openscada.hd.data.message.ChangeQueryParameters;
import org.openscada.hd.data.message.CloseQuery;
import org.openscada.hd.data.message.CreateQuery;
import org.openscada.hd.data.message.CreateQueryFailure;
import org.openscada.hd.data.message.ListUpdate;
import org.openscada.hd.data.message.StartBrowse;
import org.openscada.hd.data.message.StopBrowse;
import org.openscada.hd.data.message.UpdateQueryData;
import org.openscada.hd.data.message.UpdateQueryParameters;
import org.openscada.hd.data.message.UpdateQueryState;
import org.openscada.hd.server.Service;
import org.openscada.hd.server.Session;
import org.openscada.utils.concurrent.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

public class ServerConnectionImpl extends ServiceServerConnection<Session, Service>
{

    private final static Logger logger = LoggerFactory.getLogger ( ServerConnectionImpl.class );

    private final Map<Long, QueryHandler> queries = new HashMap<Long, QueryHandler> ();

    private final ExecutorService queryDisposer;

    private final ItemListListener listener = new ItemListListener () {

        @Override
        public void listChanged ( final Set<HistoricalItemInformation> addedOrModified, final Set<String> removed, final boolean full )
        {
            handleListChanged ( addedOrModified, removed, full );
        }
    };

    public ServerConnectionImpl ( final IoSession session, final Service service )
    {
        super ( session, service );

        this.queryDisposer = Executors.newCachedThreadPool ( new NamedThreadFactory ( "org.openscada.hd.server.ngp.QueryDisposer" ) );
    }

    protected int getMaxDataSize ()
    {
        return Integer.getInteger ( "org.openscada.hd.server.ngp.maxDataSize", 1024 );
    }

    @Override
    public void dispose ()
    {
        super.dispose ();
        this.queryDisposer.shutdown ();
    }

    @Override
    public void messageReceived ( final Object message ) throws Exception
    {
        if ( message instanceof StartBrowse )
        {
            handleStartBrowse ();
        }
        else if ( message instanceof StopBrowse )
        {
            handelStopBrowse ();
        }
        else if ( message instanceof CloseQuery )
        {
            handleCloseQuery ( (CloseQuery)message );
        }
        else if ( message instanceof CreateQuery )
        {
            handleCreateQuery ( (CreateQuery)message );
        }
        else if ( message instanceof ChangeQueryParameters )
        {
            handleChangeQueryParameters ( (ChangeQueryParameters)message );
        }
        else
        {
            super.messageReceived ( message );
        }
    }

    private void handelStopBrowse ()
    {
        logger.debug ( "Stopping browser" );
        this.session.setItemListListener ( null );
    }

    private void handleStartBrowse ()
    {
        logger.debug ( "Starting browser" );
        this.session.setItemListListener ( this.listener );
    }

    protected void handleListChanged ( final Set<HistoricalItemInformation> addedOrModified, final Set<String> removed, final boolean full )
    {
        logger.debug ( "List changed - addedOrModified: {}, removed: {}, full: {}", new Object[] { addedOrModified, removed, full } );

        sendMessage ( new ListUpdate ( addedOrModified, removed, full ) );
    }

    // query stuff

    protected synchronized void handleChangeQueryParameters ( final ChangeQueryParameters message )
    {
        // get the query id
        final long queryId = message.getQueryId ();

        final QueryHandler handler = this.queries.get ( queryId );
        if ( handler != null )
        {
            handler.changeParameters ( message.getQueryParameters () );
        }
    }

    protected synchronized void handleCloseQuery ( final CloseQuery message )
    {
        final Profiler p = new Profiler ( "Close Query" );
        p.setLogger ( logger );

        p.start ( "init" );

        // get the query id
        final long queryId = message.getQueryId ();

        logger.info ( "Handle close query: {}", queryId );

        final QueryHandler handler;

        p.start ( "remove" );

        sendQueryState ( queryId, QueryState.DISCONNECTED );
        handler = this.queries.remove ( queryId );

        // close outside of lock
        if ( handler != null )
        {
            p.start ( "Close" );
            // throw it in the disposer queue ... the storage module takes too long
            this.queryDisposer.execute ( new Runnable () {

                @Override
                public void run ()
                {
                    logger.info ( "Disposing query {} ...", queryId );
                    handler.close ();
                    logger.info ( "Disposing query {} ... done!", queryId );
                }
            } );
        }

        p.stop ().log ();
    }

    protected void handleCreateQuery ( final CreateQuery message )
    {
        final Profiler p = new Profiler ( "Create query" );
        p.setLogger ( logger );

        // get the query id
        final long queryId = message.getQueryId ();

        logger.debug ( "Creating new query with id: {}", queryId );

        try
        {
            p.start ( "Prepare" );

            // get the query item
            final String itemId = message.getItemId ();
            // get the initial query parameters
            final QueryParameters parameters = message.getQueryParameters ();
            final boolean updateData = message.isUpdateData ();

            p.start ( "Make query" );
            makeQuery ( message, queryId, itemId, parameters, updateData );

            p.start ( "Finish" );
        }
        catch ( final Throwable e )
        {
            sendQueryState ( queryId, QueryState.DISCONNECTED );
        }
        finally
        {
            p.stop ().log ();
        }
    }

    private void makeQuery ( final CreateQuery message, final long queryId, final String itemId, final QueryParameters parameters, final boolean updateData ) throws InvalidSessionException, InvalidItemException
    {

        final QueryHandler handler;
        synchronized ( this )
        {
            if ( this.queries.containsKey ( queryId ) )
            {
                logger.warn ( "Duplicate query request: {}", queryId );
                sendMessage ( new CreateQueryFailure ( new Response ( message.getRequest () ), new ErrorInformation ( null, String.format ( "Duplicate query id: %s", queryId ), null ) ) );
                return;
            }

            // create the handler and set the query
            handler = new QueryHandler ( queryId, this );
            this.queries.put ( queryId, handler );
        }

        final Query query = this.service.createQuery ( this.session, itemId, parameters, handler, updateData );

        if ( query == null )
        {
            synchronized ( this )
            {
                // we already added the query .. so remove it here
                sendQueryState ( queryId, QueryState.DISCONNECTED );
                this.queries.remove ( queryId );
            }
        }
        else
        {
            logger.debug ( "Adding query: {}", queryId );
            handler.setQuery ( query );
        }
    }

    public void sendQueryData ( final long queryId, final int index, final Map<String, List<Double>> values, final List<ValueInformation> valueInformation )
    {
        synchronized ( this )
        {
            if ( !this.queries.containsKey ( queryId ) )
            {
                return;
            }

            final int len = valueInformation.size ();

            if ( len < getMaxDataSize () )
            {
                sendQueryDataPacket ( queryId, index, values, valueInformation );
            }
            else
            {
                logger.debug ( "Using split send: {}", getMaxDataSize () );
                int count = 0;

                do
                {
                    // get remaining items
                    final int size = Math.min ( len - count, getMaxDataSize () );

                    logger.debug ( "Sending - query-id: {}, index: {}, size: {}", new Object[] { queryId, count, size } );

                    // copy vi
                    final List<ValueInformation> vi = new ArrayList<ValueInformation> ( valueInformation.subList ( count, count + size ) );

                    // copy values
                    final Map<String, List<Double>> v = new HashMap<String, List<Double>> ();
                    for ( final Map.Entry<String, List<Double>> entry : values.entrySet () )
                    {
                        final List<Double> vs = new ArrayList<Double> ( entry.getValue ().subList ( count, count + size ) );
                        v.put ( entry.getKey (), vs );
                    }

                    sendQueryDataPacket ( queryId, count, v, vi );

                    count += getMaxDataSize ();
                } while ( count < len );
            }
        }
    }

    private void sendQueryDataPacket ( final long queryId, final int index, final Map<String, List<Double>> values, final List<ValueInformation> valueInformation )
    {

        final List<ValueEntry> data = new ArrayList<ValueEntry> ();
        for ( final Map.Entry<String, List<Double>> entry : values.entrySet () )
        {
            data.add ( new ValueEntry ( entry.getKey (), entry.getValue () ) );
        }

        sendMessage ( new UpdateQueryData ( queryId, index, valueInformation, data ) );
    }

    public synchronized void sendQueryParameters ( final long queryId, final QueryParameters parameters, final Set<String> valueTypes )
    {
        logger.debug ( "Sending query parameters: {} / {} / {}", new Object[] { queryId, parameters, valueTypes } );

        if ( !this.queries.containsKey ( queryId ) )
        {
            return;
        }

        sendMessage ( new UpdateQueryParameters ( queryId, parameters, valueTypes ) );
    }

    public synchronized void sendQueryState ( final long queryId, final QueryState state )
    {
        logger.debug ( "Sending query state: {} -> {}", new Object[] { queryId, state } );

        synchronized ( this )
        {
            if ( !this.queries.containsKey ( queryId ) )
            {
                logger.info ( "Query not found {}", queryId );
                return;
            }
            sendMessage ( new UpdateQueryState ( queryId, state.name () ) );
        }
    }

}
