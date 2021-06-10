package org.apache.bookkeeper.mytests.parameters;

public class BufferedChannelWriteParameters {
    private boolean isEmptyFile;
    private Integer writeBufSize;
    private Integer buffChanCapacity;
    private Class<? extends Exception> expectedException;

    
    /**
     * @param writeBufSize dimensione del buffer di scrittura
     * @param buffChanCapacity capacità del BufferChannel
     */
    public BufferedChannelWriteParameters(boolean isEmptyFile, Integer writeBufSize, Integer buffChanCapacity,Class<? extends Exception> expectedException) {
        this.isEmptyFile = isEmptyFile;
        this.writeBufSize = writeBufSize;
        this.buffChanCapacity = buffChanCapacity;
        this.expectedException = expectedException;
    }

    public boolean isEmptyFile() {
        return isEmptyFile;
    }

    public Integer getWriteBufSize() {
        return writeBufSize;
    }

    public Integer getBuffChanCapacity() {
        return buffChanCapacity;
    }

    public Class<? extends Exception> getExpectedException() {
        return expectedException;
    }
}
