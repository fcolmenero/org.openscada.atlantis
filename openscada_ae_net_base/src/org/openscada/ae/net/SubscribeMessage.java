/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006 inavare GmbH (http://inavare.com)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.openscada.ae.net;

import org.openscada.net.base.data.IntegerValue;
import org.openscada.net.base.data.LongValue;
import org.openscada.net.base.data.Message;
import org.openscada.net.base.data.StringValue;

public class SubscribeMessage
{
    private String _queryId = null;
    private long _listenerId = 0;
    private int _maxBatchSize = 0;
    private int _archiveSet = 0;

    public String getQueryId ()
    {
        return _queryId;
    }

    public void setQueryId ( String queryId )
    {
        _queryId = queryId;
    }
    
    public int getArchiveSet ()
    {
        return _archiveSet;
    }

    public void setArchiveSet ( int archiveSet )
    {
        _archiveSet = archiveSet;
    }

    public int getMaxBatchSize ()
    {
        return _maxBatchSize;
    }

    public void setMaxBatchSize ( int maxBatchSize )
    {
        _maxBatchSize = maxBatchSize;
    }
    
    public long getListenerId ()
    {
        return _listenerId;
    }

    public void setListenerId ( long listenerId )
    {
        _listenerId = listenerId;
    }

    public Message toMessage ()
    {
        Message message = new Message ( Messages.CC_SUBSCRIBE );
        message.getValues ().put ( "query-id", new StringValue ( _queryId ) );
        message.getValues ().put ( "listener-id", new LongValue ( _listenerId ) );
        message.getValues ().put ( "max-batch-size", new IntegerValue ( _maxBatchSize ) );
        message.getValues ().put ( "archive-set", new IntegerValue ( _archiveSet ) );
        return message;
    }
    
    public static SubscribeMessage fromMessage ( Message message )
    {
        SubscribeMessage subscribeMessage = new SubscribeMessage ();
        subscribeMessage.setQueryId ( message.getValues ().get ( "query-id" ).toString () );
        subscribeMessage.setListenerId ( ((LongValue)message.getValues ().get ( "listener-id" ) ).getValue () );
        if ( message.getValues ().containsKey ( "max-batch-size" ) )
            subscribeMessage.setMaxBatchSize ( ((IntegerValue)message.getValues ().get ( "max-batch-size" )).getValue () );
        if ( message.getValues ().containsKey ( "archive-set" ) )
            subscribeMessage.setArchiveSet ( ((IntegerValue)message.getValues ().get ( "archive-set" )).getValue () );
        return subscribeMessage;
    }

    
}