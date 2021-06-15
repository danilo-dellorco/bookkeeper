package org.apache.bookkeeper.mytests.parameters;

import org.junit.rules.ExpectedException;

public class BufferedChannelReadParameters {
	int testNum;			// Parametro di debug
	int fileSize;
	int startIndex;
	int readLenght;
	int buffSize;
	Class<? extends Exception> expectedException;
	

	public BufferedChannelReadParameters(int testNum, int fileSize, int startIndex, int readLenght, int buffSize, Class<? extends Exception> expectedException) {
		this.testNum = testNum;
		this.fileSize = fileSize;
		this.startIndex = startIndex;
		this.readLenght = readLenght;
		this.buffSize = buffSize;
		this.expectedException = expectedException;
	}
	
	

	public int getTestNum() {
		return testNum;
	}



	public void setTestNum(int testNum) {
		this.testNum = testNum;
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
