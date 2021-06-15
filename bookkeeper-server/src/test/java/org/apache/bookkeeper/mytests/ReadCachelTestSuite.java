package org.apache.bookkeeper.mytests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ReadCachePutTest.class, //test case 1
    ReadCacheGetTest.class     //test case 2
})

public class ReadCachelTestSuite {
    //normally, this is an empty class
}
