/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
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

package org.openscada.hd.client.ngp;

import java.util.concurrent.ExecutorService;

import org.openscada.hd.Query;
import org.openscada.hd.QueryListener;
import org.openscada.hd.QueryState;
import org.openscada.hd.data.QueryParameters;

public class ErrorQueryImpl implements Query
{

    public ErrorQueryImpl ( final ExecutorService executor, final QueryListener listener )
    {
        executor.execute ( new Runnable () {
            @Override
            public void run ()
            {
                listener.updateState ( QueryState.DISCONNECTED );
            }
        } );
    }

    @Override
    public void close ()
    {
        // nothing to do
    }

    @Override
    public void changeParameters ( final QueryParameters parameters )
    {
    }

}
