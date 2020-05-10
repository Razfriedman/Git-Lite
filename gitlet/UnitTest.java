package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static junit.framework.TestCase.assertEquals;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Raz Friedman
 */
public class UnitTest {

    /**
     * Run the JUnit tests in the loa package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /**
     * Test - blob obj
     */
    @Test
    public void testBlob() throws IOException {
        File testFile = new File("banana.txt");
        testFile.createNewFile();
        Blob test = new Blob(testFile);
        System.out.println(test.id());
        assertEquals("banana.txt", test.fileName());
    }

    /**
     * Test - commit obj
     */
    @Test
    public void testCommit() {
        File testFile = new File("banana.txt");
        Blob test = new Blob(testFile);
        HashMap<String, String> blobsForTest = new HashMap<>();
        blobsForTest.put(test.fileName(), test.id());
        Commit testC = new Commit("testy", "123",
                null, blobsForTest, new Date(0));
        assertEquals("Wed Dec 31 16:00:00 1969 -0800", testC.timestamp());
    }

    /**
     * Test - staging obj
     */
    @Test
    public void testStaging() {
        File testFile = new File("banana.txt");
        Blob test = new Blob(testFile);
        StagingArea stageTest = new StagingArea();
        stageTest.addBlobToAdd(test);
        stageTest.addBlobToRemove(test);
        assertEquals(stageTest.toRemove().get(test.fileName()), test.id());
    }

    /**
     * Test - head obj
     */
    @Test
    public void testHead() {
        File testFile = new File("banana.txt");
        Blob test = new Blob(testFile);
        HashMap<String, String> blobsForTest = new HashMap<>();
        blobsForTest.put(test.fileName(), test.id());
        Commit testC = new Commit("testy", "123",
                null, blobsForTest, new Date(0));
        Head testH = new Head();
        testH.setActiveBranch("master");
        testH.setCurrentCommit(testC.id());
        assertEquals(testH.getActiveBranch(), "master");
        assertEquals(testH.currentCommit(), testC.id());
    }
}
