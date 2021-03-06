/*
 * This file is part of the openSCADA project
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

package org.openscada.hd.server.storage.hds;

import org.openscada.utils.lang.Immutable;

@Immutable
public class StorageConfiguration
{
    private final long timeSlice;

    private final int count;

    public StorageConfiguration ( final long timeSlice, final int count )
    {
        super ();
        this.timeSlice = timeSlice;
        this.count = count;
    }

    public int getCount ()
    {
        return this.count;
    }

    public long getTimeSlice ()
    {
        return this.timeSlice;
    }
}
