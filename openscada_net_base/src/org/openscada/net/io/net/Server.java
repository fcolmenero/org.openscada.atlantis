/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2008 inavare GmbH (http://inavare.com)
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

package org.openscada.net.io.net;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.openscada.net.base.ConnectionHandlerFactory;
import org.openscada.net.io.IOProcessor;
import org.openscada.net.io.ServerSocket;
import org.openscada.net.io.SocketConnection;
import org.openscada.net.utils.MessageCreator;

public class Server implements Runnable
{
    private ConnectionHandlerFactory factory = null;

    private IOProcessor processor = null;

    @SuppressWarnings ( "unused" )
    private ServerSocket serverSocket = null;

    public Server ( final ConnectionHandlerFactory factory, final int port ) throws IOException
    {
        this ( factory, new IOProcessor (), port );
    }

    public Server ( final ConnectionHandlerFactory factory, final IOProcessor processor, final int port ) throws IOException
    {
        this.factory = factory;
        this.processor = processor;

        this.serverSocket = new ServerSocket ( this.processor, new InetSocketAddress ( port ), new ServerSocket.ConnectionFactory () {

            public void accepted ( final SocketConnection connection )
            {
                final ServerConnection newConnection = new ServerConnection ( Server.this.factory.createConnectionHandler (), connection );
                newConnection.connected ();
                newConnection.sendMessage ( MessageCreator.createPing () );
            }
        } );
    }

    public void start ()
    {
        this.processor.start ();
    }

    public void run ()
    {
        this.processor.run ();
    }
}
