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
import sample.WifiDirectActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by atul.
 */

@RunWith(RobolectricTestRunner.class)
public class BluetoothActivityTest {

    private BluetoothActivity activity;
    private Button btnAnnounce;
    private Button btnDiscover;


    @Before
    public void setup() {

        activity = Robolectric.setupActivity(BluetoothActivity.class);
        btnAnnounce = (Button) activity.findViewById(R.id.btnBtAnnounce);
        btnDiscover = (Button) activity.findViewById(R.id.btnBtDiscover);
    }

    @Test
    public void shouldNotBeNull() {

        assertNotNull(activity);
    }

    @Test
    public void validateButton() {

        assertNotNull("Button could not be found", btnAnnounce);
        assertEquals( View.VISIBLE,btnAnnounce.getVisibility());

        assertNotNull("Button could not be found", btnDiscover);
        assertEquals( View.VISIBLE,btnDiscover.getVisibility());


    }

    @Test
    public void validateButtonContent() {

        assertTrue("Button contains incorrect text",
                "Announce (BT)".equals(btnAnnounce.getText().toString()));

        assertTrue("Button contains incorrect text",
                "Discover (BT)".equals(btnDiscover.getText().toString()));
    }

    @Test
    public void menuButton(){
        //WifiDirect menu item
        Intent intent = new Intent(activity, WifiDirectActivity.class);
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        shadowActivity.clickMenuItem(R.id.miWd);

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
