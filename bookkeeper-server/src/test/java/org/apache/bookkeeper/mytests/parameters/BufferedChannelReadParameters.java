package org.apache.bookkeeper.mytests.parameters;

public class BufferedChannelReadParameters {
	int fileSize;
	int startIndex;
	int readLenght;
	int buffSize;
	boolean doWrite;
	Class<? extends Exception> expectedException;
	

	public BufferedChannelReadParameters(int fileSize, int startIndex, int readLenght, int buffSize, boolean doWrite, Class<? extends Exception> expectedException) {
		this.fileSize = fileSize;
		this.startIndex = startIndex;
		this.readLenght = readLenght;
		this.buffSize = buffSize;
		this.doWrite = doWrite;
		this.expectedException = expectedException;
	}

	public boolean doWrite() {
		return doWrite;
	}

	public void setDoWrite(boolean doWrite) {
		this.doWrite = doWrite;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	
	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getBuffSize() {
		return buffSize;
	}

	public void setBuffSize(int buffSize) {
		this.buffSize = buffSize;
	}

	public int getReadLenght() {
		return readLenght;
	}

	public void setReadLenght(int readLenght) {
		this.readLenght = readLenght;
	}

	public Class<? extends Exception> getExpectedException() {
		return expectedException;
	}

	public void setExpectedException(Class<? extends Exception> expectedException) {
		this.expectedException = expectedException;
	}	
	
	
	
}
