package org.apache.bookkeeper.mytests.parameters;


public class BufferedChannelWriteParameters {
    private int entrySize;
    private int writeBuffCapacity;
    private long unpersistedBytesBound;
    private Class<? extends Exception> expectedException;

    
    /**
     * @param dimensione dell'entry che verrà scritta sul buffer
     * @param writeBuffCapacity dimensione del buffer di scrittura
     * @param buffChanCapacity capacità del BufferChannel
     */
    public BufferedChannelWriteParameters( int writeBuffCapacity, int entrySize, long unpersistedBytesBound, Class<? extends Exception> expectedException) {
        this.writeBuffCapacity = writeBuffCapacity;
        this.entrySize = entrySize;
        this.unpersistedBytesBound = unpersistedBytesBound;
        this.expectedException = expectedException;
    }

    
    
	public long getUnpersistedBytesBound() {
		return unpersistedBytesBound;
	}



	public void setUnpersistedBytesBound(int unpersistedBytesBound) {
		this.unpersistedBytesBound = unpersistedBytesBound;
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
