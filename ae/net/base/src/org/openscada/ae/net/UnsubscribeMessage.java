package org.openscada.ae.net;

import org.openscada.net.base.data.LongValue;
import org.openscada.net.base.data.Message;
import org.openscada.net.base.data.StringValue;

public class UnsubscribeMessage
{
    private String _queryId = null;
    private long _listenerId = 0;

    public String getQueryId ()
    {
        return _queryId;
    }

    public void setQueryId ( String queryId )
    {
        _queryId = queryId;
    }

    public long getListenerId ()
    {
        return _listenerId;
    }

    public void setListenerId ( long listenerId )
    {
        _listenerId = listenerId;
    }
    
    public Message toMessage ()
    {
        Message message = new Message ( Messages.CC_UNSUBSCRIBE );
        message.getValues ().put ( "query-id", new StringValue ( _queryId ) );
        message.getValues ().put ( "listener-id", new LongValue ( _listenerId ) );
        return message;
    }
    
    public static UnsubscribeMessage fromMessage ( Message message )
    {
        UnsubscribeMessage unsubscribeMessage = new UnsubscribeMessage ();
        unsubscribeMessage.setQueryId ( message.getValues ().get ( "query-id" ).toString () );
        unsubscribeMessage.setListenerId ( ((LongValue)message.getValues ().get ( "listener-id" ) ).getValue () );
        return unsubscribeMessage;
    }

}
