/*
 * This file is part of the OpenSCADA project
 * 
 * Copyright (C) 2006-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
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

package org.openscada.ae.server.http.monitor;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;

import org.eclipse.scada.core.Variant;
import org.eclipse.scada.sec.UserInformation;
import org.eclipse.scada.utils.lang.Pair;
import org.openscada.ae.Event;
import org.openscada.ae.Event.EventBuilder;
import org.openscada.ae.Event.Fields;
import org.openscada.ae.data.Severity;
import org.openscada.ae.event.EventProcessor;
import org.openscada.ae.filter.EventMatcher;
import org.openscada.ae.filter.internal.EventMatcherImpl;
import org.openscada.ae.monitor.common.AbstractConfiguration;
import org.openscada.ae.monitor.common.AbstractPersistentStateMonitor;
import org.openscada.ae.monitor.common.AbstractStateMonitor;
import org.openscada.ae.monitor.common.MessageDecorator;
import org.openscada.ca.ConfigurationDataHelper;
import org.osgi.framework.BundleContext;

public class EventMonitorImpl extends AbstractPersistentStateMonitor implements EventMonitor
{
    private EventMatcher matcher = null;

    private String monitorType = Messages.getString ( "EventMonitorImpl.tag.event" ); //$NON-NLS-1$

    private Configuration configuration;

    private static class Configuration extends AbstractConfiguration
    {
        boolean active;

        boolean requireAkn;

        Severity severity;

        boolean suppressEvents;

        public Configuration ( final Configuration currentConfiguration, final AbstractStateMonitor monitor )
        {
            super ( currentConfiguration, monitor );
            if ( currentConfiguration != null )
            {
                this.severity = currentConfiguration.severity;
                this.active = currentConfiguration.active;
                this.requireAkn = currentConfiguration.requireAkn;
                this.suppressEvents = currentConfiguration.suppressEvents;
            }
        }

        public void setSuppressEvents ( final UserInformation userInformation, final boolean suppressEvents )
        {
            this.suppressEvents = update ( userInformation, this.suppressEvents, suppressEvents );
        }

        public void setSeverity ( final UserInformation userInformation, final Severity severity )
        {
            this.severity = update ( userInformation, this.severity, severity );
        }

        public void setActive ( final UserInformation userInformation, final boolean active )
        {
            this.active = update ( userInformation, this.active, active );
        }

        public void setRequireAkn ( final UserInformation userInformation, final boolean requireAkn )
        {
            this.requireAkn = update ( userInformation, this.requireAkn, requireAkn );
        }
    }

    public EventMonitorImpl ( final BundleContext context, final Executor executor, final EventProcessor eventProcessor, final String id )
    {
        super ( id, EventMonitorFactory.FACTORY_ID, executor, context, null, eventProcessor );
    }

    @Override
    public void update ( final UserInformation userInformation, final Map<String, String> properties )
    {
        final ConfigurationDataHelper cfg = new ConfigurationDataHelper ( properties );

        setStringAttributes ( cfg.getPrefixed ( "info." ) ); //$NON-NLS-1$

        final Configuration c = new Configuration ( this.configuration, this );

        c.setActive ( userInformation, cfg.getBoolean ( "active", true ) ); //$NON-NLS-1$
        c.setRequireAkn ( userInformation, cfg.getBoolean ( "requireAkn", true ) ); //$NON-NLS-1$
        c.setSeverity ( userInformation, cfg.getEnum ( "severity", Severity.class, Severity.ALARM ) );
        c.setSuppressEvents ( userInformation, cfg.getBoolean ( "suppressEvents", false ) ); //$NON-NLS-1$

        setEventMatcher ( userInformation, cfg.getString ( "filter", "" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        setMonitorType ( userInformation, cfg.getString ( "monitorType", Messages.getString ( "EventMonitorImpl.tag.event" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$

        this.configuration = c;
        c.sendEvents ();

        setSuppressEvents ( c.suppressEvents );
        setOk ( Variant.NULL, System.currentTimeMillis () );
    }

    private void setEventMatcher ( final UserInformation userInformation, final String filter )
    {
        this.matcher = new EventMatcherImpl ( filter );
    }

    private void setMonitorType ( final UserInformation userInformation, final String monitorType )
    {
        this.monitorType = monitorType;
    }

    @Override
    protected void injectEventAttributes ( final EventBuilder builder )
    {
        super.injectEventAttributes ( builder );
        builder.attribute ( Fields.MONITOR_TYPE, this.monitorType );
    }

    @Override
    public synchronized Pair<Boolean, Event> evaluate ( final Event event )
    {
        if ( this.matcher != null )
        {
            if ( this.matcher.matches ( event ) )
            {
                final Variant message = makeMessage ( event );

                triggerFailure ( Variant.NULL, makeLong ( event.getSourceTimestamp () ), this.configuration.severity, this.configuration.requireAkn, new MessageDecorator ( message ) );

                final Event resultEvent = Event.create () //
                .event ( event ) //
                .attribute ( Fields.COMMENT, annotateCommentWithSource ( event ) ) //
                .attribute ( Fields.SOURCE, getId () ) //
                .attribute ( Fields.MONITOR_TYPE, this.monitorType )//
                .build ();
                return new Pair<Boolean, Event> ( true, resultEvent );
            }
        }
        return new Pair<Boolean, Event> ( false, event );
    }

    private Long makeLong ( final Date timestamp )
    {
        if ( timestamp == null )
        {
            return null;
        }
        return timestamp.getTime ();
    }

    private Variant makeMessage ( final Event event )
    {
        return event.getAttributes ().get ( Event.Fields.MESSAGE.getName () );
    }

    private Variant annotateCommentWithSource ( final Event event )
    {
        final StringBuilder sb = new StringBuilder ();
        final Variant originalComment = event.getField ( Fields.COMMENT );
        final Variant originalSource = event.getField ( Fields.SOURCE );
        boolean commentThere = false;
        if ( originalComment != null && originalComment.isString () && originalComment.asString ( "" ).length () > 0 ) //$NON-NLS-1$
        {
            commentThere = true;
            sb.append ( originalComment.asString ( "" ) ); //$NON-NLS-1$
        }
        if ( originalSource != null && originalSource.isString () && originalSource.asString ( "" ).length () > 0 ) //$NON-NLS-1$
        {
            if ( commentThere )
            {
                sb.append ( Messages.getString ( "EventMonitorImpl.delimiter" ) ); //$NON-NLS-1$
            }
            sb.append ( Messages.getString ( "EventMonitorImpl.string.originalSource" ) ); //$NON-NLS-1$
            sb.append ( originalSource.asString ( "" ) ); //$NON-NLS-1$
        }

        return Variant.valueOf ( sb.toString () );
    }

}
