package sintulabs.p2p;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowBluetoothAdapter;
import org.robolectric.shadows.ShadowIntent;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by atul on 25/6/19.
 */

@RunWith(RobolectricTestRunner.class)
public class BluetoothTest {


    private Bluetooth bluetooth;

    @Before
    public void init(){

        bluetooth = new Bluetooth(RuntimeEnvironment.application, new IBluetooth(){
            @Override
            public void actionDiscoveryStarted(Intent intent) {

            }

            @Override
            public void actionDiscoveryFinished(Intent intent) {

            }

            @Override
            public void stateChanged(Intent intent) {

            }

            @Override
            public void scanModeChange(Intent intent) {

            }

            @Override
            public void actionFound(Intent intent) {

            }

            @Override
            public void dataRead(byte[] bytes, int numRead) {

            }

            @Override
            public void connected(BluetoothDevice device) {

            }
        });

    }

    @Test
    public void TestisSupported(){
        BluetoothAdapter bluetoothAdapter = ShadowBluetoothAdapter.getDefaultAdapter();
        Boolean t = bluetoothAdapter != null;
        Assert.assertEquals(t,bluetooth.isSupported());
    }

    @Test
    public void TestisEnabled(){
        BluetoothAdapter bluetoothAdapter = ShadowBluetoothAdapter.getDefaultAdapter();
        Assert.assertEquals(bluetoothAdapter.isEnabled(),bluetooth.isEnabled());
    }

    @Test
    public void Testenable(){

        Activity launcherActivity = new Activity();
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        launcherActivity.startActivity(intent);

        ShadowActivity shadowActivity = Shadows.shadowOf(launcherActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        assertNotNull(shadowIntent);

        System.out.println(startedIntent.getAction());
        assertTrue(startedIntent.filterEquals(intent));

    }

    @Test
    public void Testannounce(){
        Activity launcherActivity = new Activity();
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        launcherActivity.startActivity(discoverableIntent);

        ShadowActivity shadowActivity = Shadows.shadowOf(launcherActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        assertNotNull(shadowIntent);

        System.out.println(startedIntent.getAction());
        System.out.println(startedIntent.getIntExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500));
        assertTrue(startedIntent.filterEquals(discoverableIntent));
    }




}
