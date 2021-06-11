package org.apache.bookkeeper.mytests.parameters;

public class BufferedChannelWriteParameters {
    private int entrySize;
    private int writeBuffCapacity;
    private Class<? extends Exception> expectedException;

    
    /**
     * @param dimensione dell'entry che verrà scritta sul buffer
     * @param writeBuffCapacity dimensione del buffer di scrittura
     * @param buffChanCapacity capacità del BufferChannel
     */
    public BufferedChannelWriteParameters(int entrySize, int writeBuffCapacity, Class<? extends Exception> expectedException) {
    	this.entrySize = entrySize;
        this.writeBuffCapacity = writeBuffCapacity;
        this.expectedException = expectedException;
    }


    public int getWriteBuffCapacity() {
		return writeBuffCapacity;
	}

	public void setWriteBuffCapacity(int writeCapacity) {
		this.writeBuffCapacity = writeCapacity;
	}

    public Class<? extends Exception> getExpectedException() {
        return expectedException;
    }

	public int getEntrySize() {
		return entrySize;
	}

	public void setEntrySize(int entrySize) {
		this.entrySize = entrySize;
	}

	public void setExpectedException(Class<? extends Exception> expectedException) {
		this.expectedException = expectedException;
	}
}
