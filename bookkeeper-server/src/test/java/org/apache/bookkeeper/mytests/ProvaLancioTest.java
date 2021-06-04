package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.bookkeeper.bookie.SortedLedgerStorage;

import org.junit.Test;

import com.google.common.primitives.Bytes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class ProvaLancioTest {
	
	public ProvaLancioTest () {
		// Costruttore
	}
	
	@Test
	public void dummyTest() throws IOException {
		SortedLedgerStorage srl = new SortedLedgerStorage();
		ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 0x0D, 0X0A });
		srl.addEntry(buf);
		int parameter = 5;
		assertEquals(5, parameter);
	}
}
