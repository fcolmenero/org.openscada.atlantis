package org.openscada.ae.client;

import java.util.Date;

import org.openscada.ae.BrowserListener;
import org.openscada.ae.QueryListener;

public interface Connection extends org.openscada.core.client.Connection
{
    // Conditions
    public void setConditionListener ( String conditionQueryId, ConditionListener listener );

    // Event - online
    public void setEventListener ( String eventQueryId, EventListener listener );

    // Event - offline
    public void createQuery ( String queryType, String queryData, QueryListener listener );

    public void addBrowserListener ( BrowserListener listener );

    public void removeBrowserListener ( BrowserListener listener );

    /**
     * Acknowledge the condition if the akn state was reached at or before the provided timestamp
     * @param conditionId the id of the condition
     * @param aknTimestamp the timestamp up to which the state may be acknowledged
     */
    public void acknowledge ( String conditionId, Date aknTimestamp );
}