package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import org.apache.bookkeeper.bookie.storage.ldb.ReadCache;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

public class ReadCacheTest {
	ReadCache cache;
	ByteBuf entry;

	/*
	 * Permette di specificare le eccezioni attese
	 */
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	
	@Before
	public void configure() {
		cache = new ReadCache(UnpooledByteBufAllocator.DEFAULT, 10 * 1024);
		entry = Unpooled.wrappedBuffer(new byte[1024]);
	}

	public ReadCacheTest() {
	}

	@Test
	public void initializeReadCacheTest() {
		assertEquals(0, cache.count());
		assertEquals(0, cache.size());
		assertEquals(null, cache.get(0, 0));
	}

	@Test
	public void putTest() {
		cache.put(1, 0, entry); // Inseriamo una entry in cache con LEDGER_ID = 1, ENTRY_ID = 0
		assertEquals(1, cache.count()); // Vediamo che abbiamo esattamente un'entry
		assertEquals(1024, cache.size()); // Vediamo che la dimensione totale è data dalla dimensione dell'unica entry
											// aggiunta

		// TODO fare ciclo con insieme di entry passate con parametrized
	}

	@Test
	public void getTest() {
		cache.put(1, 0, entry);
		ByteBuf expectedValue = entry;
		ByteBuf actualValue = cache.get(1, 0);
		assertEquals(actualValue, expectedValue);
	}

	

	// TODO questo non sarà un vero e proprio test ma dovrà diventare risultato atteso quando con parametrized gli passeremo 
	// ledger id = -1
	@Test
	public void exceptionTest() {
		exceptionRule.expect(IllegalArgumentException.class);
		cache.put(-1, 0, entry);
		

	}
}
