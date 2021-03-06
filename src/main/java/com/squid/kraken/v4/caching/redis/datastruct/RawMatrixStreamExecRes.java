package com.squid.kraken.v4.caching.redis.datastruct;

public class RawMatrixStreamExecRes {

	public byte[] getStreamedMatrix() {
		return streamedMatrix;
	}
	public void setStreamedMatrix(byte[] streamedMatrix) {
		this.streamedMatrix = streamedMatrix;
	}
	public long getExecutionTime() {
		return executionTime;
	}
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	public int getNbLines() {
		return nbLines;
	}
	public void setNbLines(int nbLines) {
		this.nbLines = nbLines;
	}
	public boolean hasMore() {
		return hasMore;
	}
	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}
	private byte[] streamedMatrix;
	private long executionTime;
	private int nbLines;
	private boolean hasMore;
	
}
