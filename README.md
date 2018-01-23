# *Ayanda*

**Ayanda**  Ayanda is an Open Source Android Library that makes it easy to discover nearby devices and share files
through a simple API. Ayanda is meant to detect nearby devices using WiFi and Bluetooth technolgy.

## Usage
 The example app in shows how to use Ayanda to discover devices on the local network, through Wifi Direct
and through Bluetooth classic. Actions to be taken when nearby devices are discovered by each discovery method
are defined by user defined interfaces.

```java
/* Ex: Discovering Devices on Local Network */

// Define how to respond when nearby devices are discovered on network
ILan iLan = new ILan() {
    @Override
    public void deviceListChanged() {
        // Clear the list of discovered peers
        peers.clear();
        peerNames.clear();
        peersAdapter.clear();
        // update list with all peers within range
        peers.addAll(a.lanGetDeviceList());
        for (int i = 0; i < peers.size(); i++) {
            Lan.Device d = (Lan.Device) peers.get(i);
            peersAdapter.add(d.getName());
        }
    }
};

a = new Ayanda(this, null, iLan, null);
a.lanDiscover();



/* Discovering nearby devices by WifiDirect */

a = new Ayanda(this, null, null, new IWifiDirect() {
        @Override
        public void wifiP2pStateChangedAction(Intent intent) {

        }

        // Decide what to do when the list of nearby devices changes
        @Override
        public void wifiP2pPeersChangedAction() {
            peers.clear();
            peers.addAll(a.wdGetDevicesDiscovered() );
            peerNames.clear();
            for (int i = 0; i < peers.size(); i++) {
                WifiP2pDevice device = (WifiP2pDevice) peers.get(i);
                peersAdapter.add(device.deviceName);
            }
        }

        @Override
        public void wifiP2pConnectionChangedAction(Intent intent) {
        }

        @Override
        public void wifiP2pThisDeviceChangedAction(Intent intent) {
        }
    });
    
a.wdDiscover();
```
See Example App in the app folder for more implementation details.



## License

    Copyright [2017] [Sabelo Mhlambi]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
