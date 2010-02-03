package org.openscada.da.datasource.base;

import org.openscada.da.client.DataItemValue;
import org.openscada.da.datasource.DataSource;
import org.openscada.da.datasource.DataSourceListener;
import org.openscada.da.datasource.SingleDataSourceTracker;
import org.openscada.da.datasource.SingleDataSourceTracker.ServiceListener;
import org.openscada.utils.osgi.pool.ObjectPoolTracker;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of a data source which is based on another
 * data source.
 * @author Jens Reimann
 * @since 0.15.0
 *
 */
public abstract class AbstractDataSourceHandler extends AbstractDataSource
{

    private final static Logger logger = LoggerFactory.getLogger ( AbstractDataSourceHandler.class );

    private final ObjectPoolTracker poolTracker;

    private SingleDataSourceTracker tracker;

    private final ServiceListener serviceListener;

    private DataSource dataSource;

    private final DataSourceListener dataSourceListener;

    public AbstractDataSourceHandler ( final ObjectPoolTracker poolTracker )
    {
        this.poolTracker = poolTracker;
        this.serviceListener = new ServiceListener () {

            public void dataSourceChanged ( final DataSource dataSource )
            {
                AbstractDataSourceHandler.this.setDataSource ( dataSource );
            }
        };

        this.dataSourceListener = new DataSourceListener () {

            public void stateChanged ( final DataItemValue value )
            {
                AbstractDataSourceHandler.this.stateChanged ( value );
            }
        };
    }

    protected abstract void stateChanged ( DataItemValue value );

    protected synchronized void setDataSource ( final DataSource dataSource )
    {
        logger.debug ( "Set datasource: {}", dataSource );

        if ( this.dataSource != null )
        {
            this.dataSource.removeListener ( this.dataSourceListener );
            this.dataSource = null;
        }

        this.dataSource = dataSource;

        if ( this.dataSource != null )
        {
            this.dataSource.addListener ( this.dataSourceListener );
        }
    }

    protected synchronized DataSource getDataSource ()
    {
        return this.dataSource;
    }

    protected synchronized void setDataSource ( final String dataSourceId ) throws InvalidSyntaxException
    {
        logger.debug ( "Set datasource request: {}", dataSourceId );

        if ( this.tracker != null )
        {
            this.tracker.close ();
            this.tracker = null;
        }

        if ( dataSourceId != null )
        {
            this.tracker = new SingleDataSourceTracker ( this.poolTracker, dataSourceId, this.serviceListener );
            this.tracker.open ();
        }
    }

}