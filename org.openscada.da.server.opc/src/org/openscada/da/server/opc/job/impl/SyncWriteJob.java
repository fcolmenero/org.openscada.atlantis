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
import org.openscada.opc.dcom.common.ResultSet;
import org.openscada.opc.dcom.da.WriteRequest;
import org.openscada.opc.dcom.da.impl.OPCSyncIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This method performs a sync write operation
 * @author Jens Reimann &lt;jens.reimann@th4-systems.com&gt;
 *
 */
public class SyncWriteJob extends ThreadJob implements JobResult<ResultSet<WriteRequest>>
{
    public static final long DEFAULT_TIMEOUT = 5000L;

    private final static Logger logger = LoggerFactory.getLogger ( SyncWriteJob.class );

    private final OPCModel model;

    private final WriteRequest[] writeRequests;

    private ResultSet<WriteRequest> result;

    public SyncWriteJob ( final long timeout, final OPCModel model, final WriteRequest[] writeRequests )
    {
        super ( timeout );
        this.model = model;
        this.writeRequests = writeRequests;
    }

    @Override
    protected void perform () throws Exception
    {
        logger.debug ( "Perform sync write" );

        final OPCSyncIO syncIo = this.model.getSyncIo ();
        if ( syncIo != null )
        {
            this.result = syncIo.write ( this.writeRequests );
        }
    }

    @Override
    public ResultSet<WriteRequest> getResult ()
    {
        return this.result;
    }
}
