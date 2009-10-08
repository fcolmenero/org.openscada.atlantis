package org.openscada.hd.server.storage.datatypes;

/**
 * This class handles a long value for being storaged in a storage channel.
 * @author Ludwig Straub
 */
public class LongValue extends BaseValue
{
    /** Value to be handled. */
    private long value;

    /**
     * Fully initializing constructor.
     * @param time time stamp of the data
     * @param qualityIndicator quality information of the data
     * @param value value to be handled
     */
    public LongValue ( final long time, final double qualityIndicator, final long value )
    {
        super ( time, qualityIndicator );
        this.value = value;
    }

    /**
     * This method returns the value to be handled.
     * @return value to be handled
     */
    public long getValue ()
    {
        return value;
    }

    /**
     * This method sets the value to be handled.
     * @param value value to be handled
     */
    public void setValue ( final long value )
    {
        this.value = value;
    }

    /**
     * @see org.openscada.hd.server.storage.datatypes
     */
    public BaseValue createNewValue ( final long time, final double qualityIndicator )
    {
        return new LongValue ( time, qualityIndicator, 0 );
    }
}
