package org.openscada.core.server.net;

import java.lang.management.ManagementFactory;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.openscada.core.info.StatisticEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ManagedConnection implements ManagedConnectionMXBean
{

    private final static Logger logger = LoggerFactory.getLogger ( ManagedConnection.class );

    private ObjectName name;

    public ManagedConnection ()
    {

    }

    public void setName ( final ObjectName name )
    {
        this.name = name;
    }

    public void dispose ()
    {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer ();
        try
        {
            mbs.unregisterMBean ( this.name );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to unregister MXBean", e );
        }
    }

    @Override
    public StatisticInformation[] getStatistics ()
    {
        final Collection<StatisticInformation> result = new ArrayList<StatisticInformation> ();
        for ( final StatisticEntry entry : getEntries () )
        {
            if ( entry.getValue () != null )
            {
                result.add ( new StatisticInformation ( entry.getLabel (), makeDouble ( entry.getValue ().getCurrent () ), makeDouble ( entry.getValue ().getMinimum () ), makeDouble ( entry.getValue ().getMaximum () ) ) );
            }
        }
        return result.toArray ( new StatisticInformation[result.size ()] );

    }

    private Double makeDouble ( final Number number )
    {
        if ( number == null )
        {
            return null;
        }
        else
        {
            return number.doubleValue ();
        }
    }

    protected abstract Collection<StatisticEntry> getEntries ();

    public static ManagedConnection register ( final ManagedConnection connection, final SocketAddress socketAddress )
    {
        try
        {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer ();
            logger.debug ( "Creatint name for: {}", socketAddress );

            final String remote = URLEncoder.encode ( socketAddress.toString (), "UTF-8" );

            final ObjectName name = new ObjectName ( "org.openscada.core.server.net", "remote", remote );
            connection.setName ( name );
            mbs.registerMBean ( connection, name );
            return connection;
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to register MXBean", e );
            return null;
        }

    }
}
