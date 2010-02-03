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

package org.openscada.da.server.browser.common.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.openscada.da.core.Location;
import org.openscada.da.core.browser.Entry;
import org.openscada.da.core.server.browser.NoSuchFolderException;
import org.openscada.da.server.browser.common.FolderListener;

public class GroupFolder implements StorageBasedFolder
{
    private final Map<ItemDescriptor, GroupSubFolder> _itemList = new HashMap<ItemDescriptor, GroupSubFolder> ();

    private GroupProvider _groupProvider = null;

    private NameProvider _nameProvider = null;

    private GroupSubFolder _folder = null;

    public GroupFolder ( final GroupProvider groupProvider, final NameProvider nameProvider )
    {
        this._groupProvider = groupProvider;
        this._nameProvider = nameProvider;

        this._folder = new GroupSubFolder ( this._nameProvider );
    }

    synchronized public Entry[] list ( final Stack<String> path ) throws NoSuchFolderException
    {
        return this._folder.list ( path );
    }

    synchronized public void subscribe ( final Stack<String> path, final FolderListener listener, final Object tag ) throws NoSuchFolderException
    {
        this._folder.subscribe ( path, listener, tag );
    }

    synchronized public void unsubscribe ( final Stack<String> path, final Object tag ) throws NoSuchFolderException
    {
        this._folder.unsubscribe ( path, tag );
    }

    synchronized public void added ( final ItemDescriptor descriptor )
    {
        if ( this._itemList.containsKey ( descriptor ) )
        {
            return;
        }

        final String[] groupingArray = this._groupProvider.getGrouping ( descriptor );
        if ( groupingArray == null )
        {
            return;
        }

        final Location grouping = new Location ( groupingArray );

        final GroupSubFolder subFolder = this._folder.add ( grouping.getPathStack (), descriptor );

        if ( subFolder != null )
        {
            this._itemList.put ( descriptor, subFolder );
        }
    }

    synchronized public void removed ( final ItemDescriptor descriptor )
    {
        final GroupSubFolder folder = this._itemList.get ( descriptor );

        if ( folder == null )
        {
            return;
        }

        folder.remove ( descriptor );
        this._itemList.remove ( descriptor );
    }

    public void added ()
    {
        this._folder.added ();
    }

    public void removed ()
    {
        this._folder.removed ();
    }

}