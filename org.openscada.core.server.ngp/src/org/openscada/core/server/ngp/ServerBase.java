/*
 * This file is part of the openSCADA project
 * 
 * Copyright (C) 2011-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
 * Copyright (C) 2013 Jens Reimann (ctron@dentrassi.de)
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

package org.openscada.core.server.ngp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.openscada.core.server.common.NetworkHelper;
import org.openscada.protocol.ngp.common.FilterChainBuilder;
import org.openscada.protocol.ngp.common.ProtocolConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerBase
{

    private final static Logger logger = LoggerFactory.getLogger ( ServerBase.class );

    private final NioSocketAcceptor acceptor;

    private final Collection<InetSocketAddress> addresses;

    private final FilterChainBuilder chainBuilder;

    public ServerBase ( final Collection<InetSocketAddress> addresses, final ProtocolConfigurationFactory protocolConfigurationFactory ) throws Exception
    {
        this.addresses = addresses;

        this.acceptor = new NioSocketAcceptor ();
        this.acceptor.setReuseAddress ( true );

        this.chainBuilder = new FilterChainBuilder ( false );
        this.chainBuilder.setLoggerName ( ServerBase.class.getName () + ".protocol" );

        this.acceptor.setFilterChainBuilder ( this.chainBuilder );
        this.acceptor.setHandler ( new ServerBaseHandler ( this, protocolConfigurationFactory.createConfiguration ( false ) ) );
    }

    public Set<InetSocketAddress> start () throws IOException
    {
        logger.info ( "Starting server for: {}", this.addresses );

        this.acceptor.bind ( this.addresses );

        return NetworkHelper.getLocalAddresses ( this.acceptor );
    }

    public void stop ()
    {
        this.acceptor.unbind ();
    }

    public void dispose ()
    {
        this.acceptor.dispose ();
    }

    public abstract ServerConnection createNewConnection ( final IoSession session );
}
