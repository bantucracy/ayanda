package sintulabs.p2p;

import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;

/**
 * Created by atul on 24/6/19.
 */

@RunWith(RobolectricTestRunner.class)
public class UtilsTest {



    @Before
    public void init(){


    }

    @Test
    public void TestintToByteArray(){

        byte[] b = {0,0,0,5};

        Assert.assertTrue(Arrays.equals(Utils.intToByteArray(5),b));

    }

    @Test
    public void TestbyteArrayToInt(){

        byte[] b = {0,0,0,5};
        Assert.assertEquals(Utils.byteArrayToInt(b),5);
    }





}
