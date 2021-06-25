package org.apache.bookkeeper.mytests;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

import org.apache.bookkeeper.bookie.BufferedChannel;
import org.apache.bookkeeper.mytests.parameters.BufferedChannelReadParameters;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@RunWith(Parameterized.class)
public class BufferedChannelReadTest {

	private static final String TMP_DIR = "testTemp";
	private static final String TMP_FILE = "BfcReadFile";

	private int fileSize;
	private int startIndex;
	private int readLength;
	private int buffSize;
	
    private FileChannel fileChannel;
    private BufferedChannel bufferedChannel;
    private byte[] randomBytes;
    
    // Aggiunto dopo miglioramento TestSuite
    private boolean doWrite;
    private boolean noException = true;


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public BufferedChannelReadTest(BufferedChannelReadParameters testInputs) {
    	this.fileSize = testInputs.getFileSize();
    	this.startIndex = testInputs.getStartIndex();
    	this.readLength = testInputs.getReadLenght();
    	this.buffSize = testInputs.getBuffSize();
    	this.doWrite = testInputs.doWrite();
    	if (testInputs.getExpectedException() != null) {
    		expectedException.expect(testInputs.getExpectedException());
    		this.noException = false;
    	}
    }



    @Parameterized.Parameters
    public static Collection<BufferedChannelReadParameters> getTestParameters() {
        List<BufferedChannelReadParameters> testInputs = new ArrayList<>();
        
        // File Size / Start Index / Read Length / Buffer Size / Expected Exception
        testInputs.add(new BufferedChannelReadParameters(-1, 0, 0, 0, false, NegativeArraySizeException.class));
        testInputs.add(new BufferedChannelReadParameters(0, -1, 0, 0, false, ArrayIndexOutOfBoundsException.class));
        testInputs.add(new BufferedChannelReadParameters(0, 0, -1, 0, false, IllegalArgumentException.class));
        testInputs.add(new BufferedChannelReadParameters(0, 0, 0, -1, false, IllegalArgumentException.class));
        testInputs.add(new BufferedChannelReadParameters(0, 0, 0, 0, false ,null));
        testInputs.add(new BufferedChannelReadParameters(1, 0, 2, 1, false, IOException.class));
        testInputs.add(new BufferedChannelReadParameters(3, 0, 2, 2, false, null));
        testInputs.add(new BufferedChannelReadParameters(3, 1, 2, 2, false, null));
        
        // Aggiunto dopo miglioramento TestSuite
        testInputs.add(new BufferedChannelReadParameters(5, 9, 3, 2, true, IOException.class));
        
        // Aggiunto dopo mutation testing
        testInputs.add(new BufferedChannelReadParameters(5, 1, 6, 0, false, IOException.class));
        
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
		generateRandomFile(this.fileSize);
    	Path filePath = Paths.get(TMP_DIR,TMP_FILE);
        this.fileChannel = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        this.fileChannel.position(this.fileChannel.size());
        
    	UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
    	this.bufferedChannel = new BufferedChannel(allocator, this.fileChannel, this.buffSize);
    	
        // Aggiunto dopo il miglioramento della TestSuite
        if (this.doWrite) {
        	writeInWriteBuff();
        }
    }

    
	@After
	public void clear() throws IOException {
		// Chiudo canali e file aperti soltanto se sono stati effettivamente aperti
		if (this.noException) {
			this.bufferedChannel.clear();
			this.bufferedChannel.close();
			this.fileChannel.close();
		}
	}
    
	// Cancello la directory contenente i file temporanei
	@AfterClass
	public static void clearEnvironment() {
		File directory = new File(TMP_DIR);
		String[] entries = directory.list();
		for(String s: entries){
		    File currentFile = new File(directory.getPath(),s);
		    currentFile.delete();
		}
		directory.delete();
	}
	
    @Test
    public void ReadTest() throws Exception {
        ByteBuf readDestBuff = Unpooled.buffer();
        // Imposto la dimensione del buffer di destinazione della read pari al numero di bytes che voglio leggere
        readDestBuff.capacity(readLength);
        
        // Read ritorna il numero di byte letti dal bufferedChannel
        int numReadBytes = this.bufferedChannel.read(readDestBuff, this.startIndex,this.readLength);
        System.out.println("Eseguita lettura | Num Bytes letti: " + numReadBytes);
        
        // Ottengo l'array dei bytes letti tramite il buffer di destinazione passato alla read
        byte[] bytesRead = readDestBuff.array();         
        
        // Calcolo il numero di bytes attesi in base a readLength,fileSize e startIndex
        int numBytesExpected = 0;
        if (this.fileSize - this.startIndex >= this.readLength) {
        	numBytesExpected = this.readLength;
        }
        else {
        	numBytesExpected =  this.randomBytes.length - this.startIndex - this.readLength;
        }
        byte[] expectedBytes = Arrays.copyOfRange(this.randomBytes, this.startIndex, this.startIndex + numBytesExpected);

        Assert.assertEquals(Arrays.toString(expectedBytes), Arrays.toString(bytesRead));
    }
    
    
    /*
     * Aggiunto dopo il miglioramento della TestSuite
	 * Permette di scrivere direttamente sul writeBuffer prima di effettuare la lettura
     */
    private void writeInWriteBuff() throws IOException {
        ByteBuf writeBuf = Unpooled.buffer();
        writeBuf.writeBytes(this.randomBytes);
        this.bufferedChannel.write(writeBuf);
    }
    
    
    /*
     *  Genera un file di dimensione 'size' contenente bytes generati randomicamente
     */
	private void generateRandomFile(int size) throws IOException {
		this.randomBytes = new byte[size];
		Random rd = new Random();
		rd.nextBytes(randomBytes);
		
        FileOutputStream fileStream = new FileOutputStream(TMP_DIR+"/"+TMP_FILE);
        fileStream.write(this.randomBytes);
        fileStream.close();
	}


}
