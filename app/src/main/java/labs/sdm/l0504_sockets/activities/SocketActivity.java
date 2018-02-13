/*
 * Copyright (c) 2016. David de Andrés and Juan Carlos Ruiz, DISCA - UPV, Development of apps for mobile devices.
 */

package labs.sdm.l0504_sockets.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import labs.sdm.l0504_sockets.R;
import labs.sdm.l0504_sockets.tasks.ClientTask;
import labs.sdm.l0504_sockets.tasks.ServerTask;
import labs.sdm.l0504_sockets.utils.ImageUtils;

/*
* Transfers an image from a Client device to a Server device by means of a socket bound to port 9999.
* Both devices should be connected to the same network.
* Check http://developer.android.com/intl/es/tools/devices/emulator.html#connecting
* to connect different instances of the Android emulator.
* */
public class SocketActivity extends AppCompatActivity {

    // Hold reference to the View objects
    TextView tvAddress;
    ImageView ivServer;
    ImageView ivClient;
    EditText etAddress;

    // Hold reference to the asynchronous task in charge of managing the Server
    ServerTask task;

    // Hold reference to the URI identifying the location of the image to be sent
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        // Initialize the TabHost
        TabHost tabHost = findViewById(R.id.thSocket);
        tabHost.setup();

        // Create the Tab for the Server
        TabHost.TabSpec spec = tabHost.newTabSpec(getResources().getString(R.string.exchange_images_server));
        spec.setIndicator(getResources().getString(R.string.exchange_images_server));
        spec.setContent(R.id.llServerSocket);
        // Add the Server Tab to the TabHost
        tabHost.addTab(spec);

        // Create the Tab for the Client
        spec = tabHost.newTabSpec(getResources().getString(R.string.exchange_images_client));
        spec.setIndicator(getResources().getString(R.string.exchange_images_client));
        spec.setContent(R.id.llClientSocket);
        // Add the Client Tab to the TabHost
        tabHost.addTab(spec);

        // Keep reference to View objects
        tvAddress = findViewById(R.id.tvServerSocketAddress);
        ivServer = findViewById(R.id.ivServerSocketImage);
        ivClient = findViewById(R.id.ivClientSocketImage);
        etAddress = findViewById(R.id.etClientSocketAddress);
    }

    /*
    * Determines whether the device has got Internet connection.
    * */
    private boolean isConnected() {
        // Get a reference to the ConnectivityManager
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        // Get information about the default active data network
        NetworkInfo info = manager.getActiveNetworkInfo();
        // There will be connectivity when there is a default connected network
        return ((info != null) && info.isConnected());
    }

    /*
    * Handles the event for the server to start/stop receiving images
    * */
    public void toggleServer(View v) {
        ToggleButton button = (ToggleButton) v;
        // Server was stopped, so start it
        if (button.isChecked()) {
            // Check that network connectivity exists
            if (isConnected()) {
                // Launch the AsyncTask in charge of starting the Server
                task = new ServerTask();
                task.setParent(this);
                task.execute();
            }
            // Notify the user that the device has not got Internet connection
            else {
                Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
                // Uncheck the button as the Server is not running
                button.setChecked(false);
            }
        }
        // Server was up and running, so stop it
        else {
            // Cancel the task for the server to stop
            task.cancel(true);
            try {
                // Close the ServerSocket if it is active, as it could be blocked waiting
                // for new clients and will not stop just by cancelling the task
                if (task.getServer().isBound()) {
                    task.getServer().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /*
    * Gets the IP address of the device
    * */
    public String getIpAddress() {
        try {
            // Loop through all the available network interfaces
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {

                NetworkInterface networkInterface = (NetworkInterface) en.nextElement();
                // Loop through all the network addresses bound to that particular interface
                for (Enumeration enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    // Return the first IPv4 address that it is not the loopback address
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /*
    * Launches an implicit Intent to select an image available in the device
    * */
    public void selectImage(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    /*
    * Gets the URI associated to the selected image and displays that image on the Client UI
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check whether the operation was cancelled
        if (resultCode == Activity.RESULT_OK) {
            // Get the URI of the selected image
            imageUri = data.getData();
            // Sample the image and display on the Client UI
            ivClient.setImageBitmap(ImageUtils.sampleImage(this, ImageUtils.GET_IMAGE_FROM_URI, imageUri));
        }
    }

    /*
    * Send the image file from the Client to the Server
    * */
    public void sendImage(View v) {
        // Check that something has been entered as Server IP address and an image has been selected
        if ((!etAddress.getText().toString().isEmpty()) && (imageUri != null)) {
            // Check that network connectivity exists
            if (isConnected()) {
                // Launch the AsyncTask in charge of sending the image file
                ClientTask client = new ClientTask();
                client.setParent(this);
                client.execute(etAddress.getText().toString(), imageUri);
            } else {
                // Notify the user that the device has not got Internet connection
                Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            }
        }
        // Notify the user that it is required to enter the IP address and port of the Server
        else {
            Toast.makeText(this, R.string.not_data, Toast.LENGTH_SHORT).show();
        }
    }

    /*
    * Displays the Server IP address and a Toast to notify that the ServerSocket is up and running.
    * */
    public void notifyServerRunning() {
        tvAddress.setText(String.format(
                getResources().getString(R.string.exchange_images_address),
                getIpAddress()));
        Toast.makeText(this, R.string.message_server_on, Toast.LENGTH_SHORT).show();
    }

    public void notifyNewClient() {
    }

    /*
    * Displays the received image on the UI.
    * */
    public void displayReceivedImage(Bitmap bitmap) {
        ivServer.setImageBitmap(bitmap);
    }

    public static final int NEW_CLIENT = 0;
    public static final int SERVER_ERROR = 1;
    public static final int SERVER_DOWN = 2;
    public static final int SENDING_IMAGE = 3;
    public static final int IMAGE_SENT = 4;
    public static final int IMAGE_NOT_SENT = 5;

    /*
    * Displays a Toast to notify the user about different events.
    * */
    public void displayNotifications(int notification) {

        switch (notification) {

            // A new Client has been accepted
            case NEW_CLIENT:
                Toast.makeText(this, R.string.message_server_receiving_image, Toast.LENGTH_SHORT).show();
                break;

            // Some problem occurred with the Server.
            case SERVER_ERROR:
                Toast.makeText(this, R.string.message_server_error, Toast.LENGTH_SHORT).show();
                break;

            // The Server is down.
            case SERVER_DOWN:
                Toast.makeText(this, R.string.message_server_off, Toast.LENGTH_SHORT).show();
                break;

            // The Client is sending the image
            case SENDING_IMAGE:
                Toast.makeText(this, R.string.sending_image, Toast.LENGTH_SHORT).show();
                break;

            // The image has been sent
            case IMAGE_SENT:
                Toast.makeText(this, R.string.image_sent, Toast.LENGTH_SHORT).show();
                break;

            // The image has not been sent
            case IMAGE_NOT_SENT:
                Toast.makeText(this, R.string.image_not_sent, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}