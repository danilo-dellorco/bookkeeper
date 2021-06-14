package org.apache.bookkeeper.mytests.parameters;

public class BufferedChannelReadParameters {
	int fileSize;
	int buffSize;
	int readLenght;
	
	public BufferedChannelReadParameters(int fileSize, int buffSize, int readLenght) {
		this.fileSize = fileSize;
		this.buffSize = buffSize;
		this.readLenght = readLenght;
	}	
	
}
