package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.bookkeeper.bookie.storage.ldb.ReadCache;
import org.apache.bookkeeper.mytests.parameters.ReadCachePutParameters;
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
public class ReadCachePutTest {
	static ReadCache cache;
	
	// Cache settings
	public static int ENTRY_SIZE = 1024;					// Ogni entry viene ha dimensione 1024 byte
	public static int MAX_ENTRIES = 50;						// Ogni cache può contenere al massimo 10 entry
	public static int CACHE_SIZE = ENTRY_SIZE*MAX_ENTRIES;	// Massimo numero di byte che può contenere la cache
	
	
	// Types of entries
	public static ByteBuf valid_entry;
	public static ByteBuf illegal_entry;
	public static ByteBuf null_entry;
	
	// Test parameters
	int ledgerId;
	int entryId;
	ByteBuf entry;
	ReadCachePutParameters input;
	
	// Aggiunto dopo il miglioramento della TestSuite
	public static int NUM_PUT = MAX_ENTRIES;
	public boolean doMultiplePut;
	
	// Permette di specificare l'eccezione attesa.
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	public ReadCachePutTest(ReadCachePutParameters input) {
		this.ledgerId = input.getLedgerId();
		this.entryId = input.getEntryId();
		this.entry = input.getEntry();
		this.doMultiplePut = input.doMultiplePut();
		if (input.getExpectedException()!=null) {
			expectedException.expect(input.getExpectedException());
		}
	}

	
	
	// Configuro l'ambiente di esecuzione istanziando una nuova ReadCache prima di ogni test.
	@Before
	public void configure() {
		UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
		cache = new ReadCache(allocator,CACHE_SIZE);
	}
	
	// Chiudo la cache dopo aver eseguito tutti i test case
	@AfterClass
	public static void closeCache() {
		cache.close();
	}
	
    @Parameters
    public static Collection<ReadCachePutParameters> getParameters() {
    	
		// Setup of the entries
		valid_entry = Unpooled.wrappedBuffer(new byte[ENTRY_SIZE]);
		illegal_entry = Unpooled.wrappedBuffer(new byte[CACHE_SIZE+1]);
		valid_entry.writerIndex(valid_entry.capacity());
		illegal_entry.writerIndex(illegal_entry.capacity());
		null_entry = null;
		
		
		List<ReadCachePutParameters> testInputs = new ArrayList<>();
		
		// ledgerId / entryId / entry
		testInputs.add(new ReadCachePutParameters(1,0,valid_entry,false,null));
		testInputs.add(new ReadCachePutParameters(0,1,illegal_entry,false,IndexOutOfBoundsException.class));
		testInputs.add(new ReadCachePutParameters(1,-1,null_entry,false,NullPointerException.class));
		testInputs.add(new ReadCachePutParameters(-1,1,valid_entry,false,IllegalArgumentException.class));
		testInputs.add(new ReadCachePutParameters(1,0,valid_entry,true,null));
		
		return testInputs;
    }
    
    
    
	@Test
	public void putTest() {
		int n=1;	
		
		if (this.doMultiplePut) {
			for (n=0;n<NUM_PUT;n++) {
				cache.put(ledgerId, n, entry);
			}
		}
		else {
			cache.put(ledgerId, entryId, entry);
		}
		
		assertEquals(n, cache.count()); 				// Vediamo che abbiamo esattamente n entry
		assertEquals(n*ENTRY_SIZE, cache.size()); 		// Vediamo che la dimensione totale è data dalla somma di tutte le entry aggiunte
	}
}
