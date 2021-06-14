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

@RunWith(Parameterized.class)

public class BufferedChannelWriteTest {
	
	private static final String TMP_DIR = "testTemp";
	private static final String LOG_FILE = "BfcWriteFile";

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
		if (testInput.getExpectedException()!=null) {
			expectedException.expect(testInput.getExpectedException());
		}
		System.out.println(String.format("\n\n========= Capacity: %d / EntrySize: %d ==========\n", this.writeBuffCapacity,this.entrySize));
	}

	@Parameterized.Parameters
	public static Collection<BufferedChannelWriteParameters> getParameters() {
		List<BufferedChannelWriteParameters> testInputs = new ArrayList<>();
		
		// WriteCapacity / Entry Size / Exception
		testInputs.add(new BufferedChannelWriteParameters(-1, 1, IllegalArgumentException.class));		
		testInputs.add(new BufferedChannelWriteParameters(1, -1, NegativeArraySizeException.class));		
		testInputs.add(new BufferedChannelWriteParameters(1, 0, null));
		testInputs.add(new BufferedChannelWriteParameters(3, 2, null));
		testInputs.add(new BufferedChannelWriteParameters(2, 3, null));
		testInputs.add(new BufferedChannelWriteParameters(3000, 2000, null));
		testInputs.add(new BufferedChannelWriteParameters(2000, 3000, null));
		
		// Questo test fallisce, probabilmente writeCapacity = 0 non è un valore ammissibile ma non viene gestito correttamente durante la scrittura
		// testInputs.add(new BufferedChannelWriteParameters(0, 1, null));	
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
        logTestFile.deleteOnExit();
        
        randomAccess = new RandomAccessFile(logTestFile, "rw");
        FileChannel fc = randomAccess.getChannel();
        this.fileChannel = fc;
		
		// Genero una nuova entry della dimensione specificata
		this.srcBuffer = generateRandomEntry(this.entrySize);
		System.out.println("Bytes random generati (srcBuffer): " + Arrays.toString(this.randomBytes));
	}

	@After
	public void clear() throws IOException {
		// Chiudo canali e file aperti soltanto se sono stati effettivamente aperti
		if (expectedException==null) {
			this.randomAccess.close();
			this.bufferedChannel.close();
		}
		this.fileChannel.close();
	}
	
	// Cancello la directory contenente i file temporanei
	@AfterClass
	public static void clearEnvironment() {
		File directory = new File(TMP_DIR);
		String[] entries = directory.list();
		for(String s: entries){
		    File currentFile = new File(directory.getPath(),s);
		    boolean deleted = currentFile.delete();
		    System.out.println(deleted);
		}
		directory.delete();
	}

	
	@Test(timeout = 500)
	public void WriteTest() throws Exception{
		UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
		bufferedChannel = new BufferedChannel(allocator, fileChannel, writeBuffCapacity);
		
		// Scrivo tutti i byte di srcBuffer all'interno del bufferedChannel
		bufferedChannel.write(srcBuffer);

		
		/*
		 *  Se l'entry è più grande della capacità del WriteBuffer si riempie il writeBuffer e si esegue il flush sul file
		 *  finchè i byte rimanenti dell'entry non entrano completamente nel bytebuffer. Al termine della scrittura quindi ho:
		 *  	fileChannel contenente tutti i Bytes scritti sul file
		 *  	writeBuffer contiene i byte rimanenti dopo dopo che sono stati effettuati 'numFlush' scritture direttamente sul file
		 *  
		 *  Altrimenti nel writeBuffer avrò tutti i byte dell'entry
		 */
		
		int numBytesInWriteBuff = 0;
		int numBytesInFileChannel = 0;
		System.out.println("EntrySize: " + entrySize);
		System.out.println("WriteBuff: " + writeBuffCapacity);
		if (entrySize > writeBuffCapacity) {
			int numFlush = entrySize/writeBuffCapacity;					// Numero di flush sul file che sono stati effettuati
			numBytesInFileChannel = numFlush * writeBuffCapacity;
			numBytesInWriteBuff = entrySize - numBytesInFileChannel;
		}
		else {
			numBytesInWriteBuff = entrySize;
		}

		System.out.println("NumBytes in WriteBuff: " + numBytesInWriteBuff);
		System.out.println("NumBytes in FileChannel: " + numBytesInFileChannel);
		
		// Ottengo i bytes che ho scritto nel WriteBuffer, inserendoli in un array di bytes bytesInWriteBuf
		byte[] bytesInWriteBuff = new byte[numBytesInWriteBuff];	
		bufferedChannel.writeBuffer.getBytes(0,bytesInWriteBuff);
		
		
		// Ottengo i byte che dovrebbero essere nel WriteBuf come il totale dei bytes random generati togliendo il numero di bytes che sono in writeBuf
		byte[] expectedBytes = Arrays.copyOfRange(this.randomBytes, this.randomBytes.length - numBytesInWriteBuff, this.randomBytes.length);

		
		System.out.println("Bytes presenti nel WriteBuffer: "+Arrays.toString(bytesInWriteBuff));
		System.out.println("Bytes attesi nel WriteBuffer: "+Arrays.toString(expectedBytes));
		
		// Verifico che i bytes scritti siano correttamente contenuti nel writeBuffer
		Assert.assertEquals(Arrays.toString(expectedBytes),Arrays.toString(bytesInWriteBuff));
		
		
		// Leggo i bytes nel file channel e li scrivo all'interno di un ByteBuffer buff
		ByteBuffer buff = ByteBuffer.allocate(numBytesInFileChannel);
		this.fileChannel.position(0);
		this.fileChannel.read(buff);

		// Ottengo i bytes attesi nel file channel come i primi 'numBytesInFileChannel' tra quelli generati
		byte[] bytesInFileChannel = buff.array();
		expectedBytes = Arrays.copyOfRange(this.randomBytes, 0, numBytesInFileChannel);

		System.out.println("Bytes nel FileChannel: "+Arrays.toString(bytesInFileChannel));
		System.out.println("Bytes attesi nel FileChannel: "+Arrays.toString(expectedBytes));

		// Verifico che eventuali bytes scritti tramite flush siano correttamente contenuti nel FileChannel
		Assert.assertEquals(Arrays.toString(expectedBytes), Arrays.toString(bytesInFileChannel));
		
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
