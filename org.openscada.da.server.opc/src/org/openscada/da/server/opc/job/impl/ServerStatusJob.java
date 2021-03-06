/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2011 TH4 SYSTEMS GmbH (http://th4-systems.com)
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

package org.openscada.da.server.opc.job.impl;

import org.openscada.da.server.opc.connection.OPCModel;
import org.openscada.da.server.opc.job.JobResult;
import org.openscada.da.server.opc.job.ThreadJob;
import org.openscada.opc.dcom.da.OPCSERVERSTATUS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This job queries the server status
 * @author Jens Reimann &lt;jens.reimann@th4-systems.com&gt;
 *
 */
public class ServerStatusJob extends ThreadJob implements JobResult<OPCSERVERSTATUS>
{
    public static final long DEFAULT_TIMEOUT = 5000L;

    private final static Logger logger = LoggerFactory.getLogger ( ServerStatusJob.class );

    private final OPCModel model;

    private OPCSERVERSTATUS status;

    public ServerStatusJob ( final long timeout, final OPCModel model )
    {
        super ( timeout );
        this.model = model;
    }

    @Override
    protected void perform () throws Exception
    {
        logger.debug ( "Request server status" );
        this.status = this.model.getServer ().getStatus ();
    }

    public OPCSERVERSTATUS getStatus ()
    {
        return this.status;
    }

    @Override
    public OPCSERVERSTATUS getResult ()
    {
        return getStatus ();
    }

}
