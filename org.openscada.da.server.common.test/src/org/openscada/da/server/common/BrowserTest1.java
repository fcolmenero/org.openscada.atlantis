/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2010 TH4 SYSTEMS GmbH (http://th4-systems.com)
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

package org.openscada.da.server.common;

import java.util.HashMap;

import org.eclipse.scada.core.Variant;
import org.junit.Test;
import org.openscada.da.server.browser.common.query.IDNameProvider;
import org.openscada.da.server.browser.common.query.ItemDescriptor;
import org.openscada.da.server.browser.common.query.SplitGroupProvider;

public class BrowserTest1
{

    @Test
    public void test1 ()
    {
        final SplitGroupProvider sgp = new SplitGroupProvider ( new IDNameProvider (), "\\.", 0, 2 );

        final ItemDescriptor desc = new ItemDescriptor ( new DataItemInputCommon ( "this.is.the.id" ), new HashMap<String, Variant> () );
        for ( int i = 0; i < 1000 * 1000; i++ )
        {
            sgp.getGrouping ( desc );
        }
    }
}
