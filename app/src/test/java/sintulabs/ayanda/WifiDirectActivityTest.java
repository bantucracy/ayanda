package sintulabs.ayanda;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import sample.BluetoothActivity;
import sample.LanActivity;
import sample.WifiDirectActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by atul.
 */

@RunWith(RobolectricTestRunner.class)

public class WifiDirectActivityTest {

    private WifiDirectActivity activity;
    private Button shareFile;
    private Button discover;


    @Before
    public void setup() {

        activity = Robolectric.setupActivity(WifiDirectActivity.class);
        shareFile = (Button) activity.findViewById(R.id.btnWdShareFile);
        discover = (Button) activity.findViewById(R.id.btnWdDiscover);
    }

    @Test
    public void shouldNotBeNull() {

        assertNotNull(activity);
    }

    @Test
    public void validateButton() {

        assertNotNull("Button could not be found", shareFile);
        assertEquals( View.VISIBLE,shareFile.getVisibility());

        assertNotNull("Button could not be found", discover);
        assertEquals( View.VISIBLE,discover.getVisibility());


    }

    @Test
    public void validateButtonContent() {

        assertTrue("Button contains incorrect text",
                "Share Photo (WD)".equals(shareFile.getText().toString()));

        assertTrue("Button contains incorrect text",
                "Discover (WD)".equals(discover.getText().toString()));
    }

    @Test
    public void menuButton(){
        //Bluetooth menu item
        Intent intent = new Intent(activity, BluetoothActivity.class);
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        shadowActivity.clickMenuItem(R.id.miBt);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertNotNull(shadowIntent);
        assertTrue(startedIntent.filterEquals(intent));

        //Lan menu item
        intent = new Intent(activity, LanActivity.class);
        shadowActivity = Shadows.shadowOf(activity);

        shadowActivity.clickMenuItem(R.id.miLan);

        startedIntent = shadowActivity.getNextStartedActivity();
        shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertNotNull(shadowIntent);
        assertTrue(startedIntent.filterEquals(intent));
    }

}
