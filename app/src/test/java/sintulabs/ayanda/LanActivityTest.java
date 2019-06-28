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
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import sample.BluetoothActivity;
import sample.LanActivity;
import sample.LanActivity;
import sample.WifiDirectActivity;
import sintulabs.p2p.WifiDirect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by atul.
 */

@RunWith(RobolectricTestRunner.class)
public class LanActivityTest {

    private LanActivity activity;
    private Button btnLanAnnounce;
    private Button btnLanDiscover;


    @Before
    public void setup() {

        activity = Robolectric.setupActivity(LanActivity.class);
        btnLanAnnounce = (Button) activity.findViewById(R.id.btnLanAnnounce);
        btnLanDiscover = (Button) activity.findViewById(R.id.btnLanDiscover);
    }

    @Test
    public void shouldNotBeNull() {

        assertNotNull(activity);
    }

    @Test
    public void validateButton() {

        assertNotNull("Button could not be found", btnLanAnnounce);
        assertEquals( View.VISIBLE,btnLanAnnounce.getVisibility());

        assertNotNull("Button could not be found", btnLanDiscover);
        assertEquals( View.VISIBLE,btnLanDiscover.getVisibility());


    }

    @Test
    public void validateButtonContent() {

        assertTrue("Button contains incorrect text",
                "Share file".equals(btnLanAnnounce.getText().toString()));

        assertTrue("Button contains incorrect text",
                "Discover (LAN)".equals(btnLanDiscover.getText().toString()));
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

        //WifiDirect menu item
        intent = new Intent(activity, WifiDirectActivity.class);
        shadowActivity = Shadows.shadowOf(activity);

        shadowActivity.clickMenuItem(R.id.miWd);

        startedIntent = shadowActivity.getNextStartedActivity();
        shadowIntent = Shadows.shadowOf(startedIntent);
        Assert.assertNotNull(shadowIntent);
        assertTrue(startedIntent.filterEquals(intent));
    }
}
