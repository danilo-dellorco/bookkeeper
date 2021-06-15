package org.apache.bookkeeper.mytests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	BufferedChannelWriteTest.class, //test case 1
    BufferedChannelReadTest.class     //test case 2
})

public class BufferedChannelTestSuite {
    //normally, this is an empty class
}
