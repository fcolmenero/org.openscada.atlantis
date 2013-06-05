/*
 * This file is part of the OpenSCADA project
 * 
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

package org.openscada.da.server.exporter;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMapUtil;

public abstract class AbstractHiveFactory implements HiveFactory
{
    protected Object getConfigurationData ( final HiveConfigurationType configuration )
    {
        for ( FeatureMap.Entry entry : configuration.getAny () )
        {
            if ( entry.getValue () instanceof EObject )
            {
                return entry.getValue ();
            }
            else if ( FeatureMapUtil.isText ( entry ) )
            {
                return entry.getValue ();
            }
            else if ( FeatureMapUtil.isCDATA ( entry ) )
            {
                return entry.getValue ();
            }
        }
        return null;
    }

}