package org.apache.bookkeeper.mytests;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

import org.apache.bookkeeper.bookie.BufferedChannel;
import org.apache.bookkeeper.mytests.parameters.BufferedChannelWriteParameters;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

/**
 * Write all the data in src to the {@link FileChannel}. Note that this function
 * can buffer or re-order writes based on the implementation. These writes will
 * be flushed to the disk only when flush() is invoked.
 *
 * @param src The source ByteBuffer which contains the data to be written.
 * @throws IOException if a write operation fails.
 */

@RunWith(Parameterized.class)

public class BufferedChannelWriteTest {
	
	private static final String TMP_DIR = "tmp";
	private static final String LOG_FILE = "BfcWriteFile";
	private static final boolean DELETE_LOG = true; 		// Utilizzato per il debugging
	
	private static final int ENTRY_SIZE = 4;
	private static final int WRITE_BUF_CAPACITY = 8;

	private BufferedChannel bufferedChannel; 	// Offre un livello di bufferizzazione prima di scrivere effettivamente sul FileChannel
	private ByteBuf srcBuffer; 					// ByteBuffer sorgente che contiene i dati che saranno scritti nel BufferedChannel
	private int entrySize;						// Dimensione dell'entry usata per la scrittura
	private int writeBuffCapacity;				// Capacità assegnata al writeBuffer (e al readBuffer)
	

	private FileChannel fileChannel; 			// Canale per leggere, scrivere e manipolare i file.
	private RandomAccessFile randomAccess;		// File utilizzato per istanziare il FileChannel
	private byte[] randomBytes;					// Array di byte generato randomicamente per testare la scrittura

	

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	public BufferedChannelWriteTest(BufferedChannelWriteParameters testInput) {
		this.entrySize = testInput.getEntrySize();
		this.writeBuffCapacity = testInput.getWriteBuffCapacity();
		if (testInput.getExpectedException() != null) {
			this.expectedException.expect(testInput.getExpectedException());
		}
	}

	@Parameterized.Parameters
	public static Collection<BufferedChannelWriteParameters> getParameters() {
		List<BufferedChannelWriteParameters> testInputs = new ArrayList<>();

		testInputs.add(new BufferedChannelWriteParameters(ENTRY_SIZE, WRITE_BUF_CAPACITY, null));
		testInputs.add(new BufferedChannelWriteParameters(WRITE_BUF_CAPACITY, ENTRY_SIZE, null));
		// TODO aggiungere altri parametri

		return testInputs;
	}

	@BeforeClass
	static public void configureEnvironment() {

		// Se non esiste, creo la directory tmp in cui verrà inserito il file di log
		if (!Files.exists(Paths.get(TMP_DIR))) {
			File tmp = new File(TMP_DIR);
			tmp.mkdir();
		}
	}

	@Before
	public void configure() throws IOException {
		
    	File directory = new File(TMP_DIR);
        File logTestFile = File.createTempFile(LOG_FILE, ".log",directory);
        if (DELETE_LOG) {
        	logTestFile.deleteOnExit();
        }
        
        randomAccess = new RandomAccessFile(logTestFile, "rw");
        FileChannel fc = randomAccess.getChannel();
		
        this.fileChannel = fc;
        System.out.println("FileChannel Position: " + this.fileChannel.position());
		
		
		// Genero una nuova entry della dimensione specificata
		this.srcBuffer = generateRandomEntry(this.entrySize);
		System.out.println("Bytes random generati (srcBuffer): " + Arrays.toString(this.randomBytes));
	}

	@After
	public void clear() {
		// Chiudo canali e file aperti
		try {
			this.fileChannel.close();
			this.randomAccess.close();
			this.bufferedChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	@Test
	public void WriteTest() throws Exception {
		UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
		bufferedChannel = new BufferedChannel(allocator, fileChannel, writeBuffCapacity);
		System.out.println("Creato BufferedChannel di dimensione: " + writeBuffCapacity);
		
		// Scrivo tutti i byte di srcBuffer all'interno del bufferedChannel
		System.out.println("Provo a scrivere srcBuffer di dimensione: " + entrySize);
		System.out.println("BufferedChannel Position (before write): " + this.bufferedChannel.position());
		bufferedChannel.write(srcBuffer);
		System.out.println("BufferedChannel Position (after write): " + this.bufferedChannel.position());

		// Controlla se la scrittura è stata effettuata nel modo corretto
		int numBytesInWriteBuff = 0;
		int numBytesInFileChannel = 0;
		
		/*
		 *  Se l'entry è più grande della capacità del WriteBuffer ho:
		 *  	writeBuffer pieno
		 *  	fileChannel contenente i bytes in eccesso dell'entry
		 *  
		 *  Altrimenti nel writeBuffer avrò tutti i byte dell'entry
		 */
		System.out.println("EntrySize: " + entrySize);
		System.out.println("WriteBuff: " + writeBuffCapacity);
		if (entrySize > writeBuffCapacity) {
			numBytesInWriteBuff = writeBuffCapacity;
			numBytesInFileChannel = entrySize - writeBuffCapacity;
		}
		else {
			numBytesInWriteBuff = entrySize;
		}

		// Creo un array di bytes per inserire i bytes dentro il WriteBuffer
		byte[] bytesInWriteBuff = new byte[numBytesInWriteBuff];
		
		// Scrivo i bytes nel WriteBuffer in un array di bytes bytesInWriteBuf
		this.bufferedChannel.writeBuffer.getBytes(0, bytesInWriteBuff);
		
		System.out.println("Bytes presenti nel WriteBuffer: "+Arrays.toString(bytesInWriteBuff));
		

		// Otengo i byte che dovrebbero essere nel WriteBuf come il totale dei bytes random generati togliendo il numero di bytes che sono in writeBuf
		byte[] expectedBytes = Arrays.copyOfRange(this.randomBytes, this.randomBytes.length - numBytesInWriteBuff, this.randomBytes.length);
		System.out.println("Bytes attesi che siano nel WriteBuffer: "+Arrays.toString(expectedBytes));

		// Verifico che i bytes scritti sul bufferedChannel siano correttamente contenuti nel WriteBuffer
		Assert.assertEquals(Arrays.toString(expectedBytes),Arrays.toString(bytesInWriteBuff));
		
		
		System.out.println("Numero Bytes attesi nel FileChannel: " + numBytesInFileChannel);
		ByteBuffer buff = ByteBuffer.allocate(numBytesInFileChannel);

		
		// Scrivo i bytes nel file channel all'interno del ByteBuffer buff
		this.fileChannel.position(0);
		this.fileChannel.read(buff);
		System.out.println("FileChannel Position (after read): " + this.fileChannel.position());
		byte[] bytesInFileChannel = buff.array();
		
		expectedBytes = Arrays.copyOfRange(this.randomBytes, 0, numBytesInFileChannel);
		System.out.println("FileChannel Position (after copy): " + this.fileChannel.position());
		System.out.println("Bytes nel FileChannel: "+Arrays.toString(bytesInFileChannel));
		System.out.println("Bytes attesi nel FileChannel: "+Arrays.toString(expectedBytes));

		
		Assert.assertEquals(Arrays.toString(expectedBytes), Arrays.toString(bytesInFileChannel));
		System.out.println("FileChannel Position (after assert): " + this.fileChannel.position());
		
		// Controllo che la posizione del File Channel sia corretta
		Assert.assertEquals(numBytesInFileChannel, this.fileChannel.position());
		
		// Controllo che la posizione del Buffered Channel sia corretta
		Assert.assertEquals(entrySize, this.bufferedChannel.position());
	}
	
	
	
	/*
	 * Genera un' entry ByteBuf contenente un numero 'size' di bytes randomici
	 */
	private ByteBuf generateRandomEntry(int size) {
		// Genero un numero random di bytes e li inserisco dentro l'array 'bytes'
		this.randomBytes = new byte[size];
		Random rd = new Random();
		rd.nextBytes(randomBytes);
		
		// Scrivo i bytes generati all'interno del ByteBuf
		ByteBuf bb = Unpooled.buffer();
		bb.writeBytes(randomBytes);
		return bb;
	}
}
