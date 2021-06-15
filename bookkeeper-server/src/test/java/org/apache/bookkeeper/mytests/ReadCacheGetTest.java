package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.bookkeeper.bookie.storage.ldb.ReadCache;
import org.apache.bookkeeper.mytests.parameters.ReadCacheParameters;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;


@RunWith(Parameterized.class)
public class ReadCacheGetTest {
	static ReadCache cache;
	
	// Cache settings
	public static int ENTRY_SIZE = 1024;					// Ogni entry viene ha dimensione 1024 byte
	public static int MAX_ENTRIES = 10;						// Ogni cache può contenere al massimo 10 entry
	public static int CACHE_SIZE = ENTRY_SIZE*MAX_ENTRIES;	// Massimo numero di byte che può contenere l'entry
	
	// Types of entries
	public static ByteBuf valid_entry;
	public static ByteBuf illegal_entry;
	public static ByteBuf null_entry;
	
	// Test parameters
	int ledgerId;
	int entryId;
	ByteBuf entry;
	
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	public ReadCacheGetTest(ReadCacheParameters input) {
		this.ledgerId = input.getLedgerId();
		this.entryId = input.getEntryId();
		this.entry = input.getEntry();
		if (input.getExpectedException()!=null) {
			expectedException.expect(input.getExpectedException());
		}
	}
	
    @Parameters
    public static Collection<ReadCacheParameters> getParameters() {
    	
		// Setup of the entries
		valid_entry = Unpooled.wrappedBuffer(new byte[ENTRY_SIZE]);
		illegal_entry = Unpooled.wrappedBuffer(new byte[CACHE_SIZE+1]);
		null_entry = null;
		
		
		List<ReadCacheParameters> testInputs = new ArrayList<>();
		
		// ledgerId / entryId / entry
		testInputs.add(new ReadCacheParameters(1,0,valid_entry,null));
		testInputs.add(new ReadCacheParameters(0,1,illegal_entry,IndexOutOfBoundsException.class));
		testInputs.add(new ReadCacheParameters(1,-1,null_entry,NullPointerException.class));
		testInputs.add(new ReadCacheParameters(-1,1,valid_entry,IllegalArgumentException.class));
		
		return testInputs;
    }

	
	
	// Configuro l'ambiente di esecuzione istanziando una nuova ReadCache ed inserendo un'entry da ottenere
	@Before
	public void configure() {
		UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
		cache = new ReadCache(allocator,CACHE_SIZE);
		cache.put(ledgerId, entryId, entry);
	}
	
	
	// Chiudo la cache dopo aver eseguito tutti i test case
	@AfterClass
	public static void closeCache() {
		cache.close();
	}
	
	@Test
	public void getTest() {
		
		ByteBuf expectedValue = entry;
		ByteBuf actualValue = cache.get(ledgerId, entryId);
		assertEquals(expectedValue, actualValue);			// Verifichiamo che il valore ottenuto dal get è uguale a quello inserito con il put
		
		actualValue = cache.get(ledgerId+1, entryId+1);
		assertEquals(null,actualValue);						// Verifichiamo che il get su un'entry non presente in cache ritorna null
	}
}
