/*
 * Copyright (c) 2016. David de Andrés and Juan Carlos Ruiz, DISCA - UPV, Development of apps for mobile devices.
 */

package labs.sdm.l0504_sockets.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import labs.sdm.l0504_sockets.R;
import labs.sdm.l0504_sockets.activities.SocketActivity;
import labs.sdm.l0504_sockets.utils.ImageUtils;

/*
    * Starts a ServerSocket listening for new Clients on port 9999.
    * It will update the UI with notifications related to the current state of any connection.
    * Upon accepting a Client, it will receive, store, and display the transferred image.
    * It will only accept a Client at a time.
    * Another AsyncTask/Thread will be required to handle multiple Clients.
    * */
public class ServerTask extends AsyncTask<Void, Integer, Void> {

    private static final int DISPLAY_SERVER_DATA_AND_RUNNING = 0;
    private static final int DISPLAY_NEW_CLIENT = 1;
    private static final int DISPLAY_IMAGE = 2;
    private static final int DISPLAY_SERVER_ERROR = 3;

    // Hold reference to the ServerSocket (Server device)
    private ServerSocket server;
    // Hold reference to the Bitmap object to display on the UI
    private Bitmap bitmap;

    public ServerSocket getServer() {
        return server;
    }

    // Hold reference to its parent activity
    private SocketActivity parent;

    public void setParent(SocketActivity parent) {
        this.parent = parent;
    }

    /*
    * Starts a ServerSocket to accept connections and receive a new image.
    * It only supports one Client at a time.
    * */
    @Override
    protected Void doInBackground(Void... params) {

        Socket socket;
        try {
            // Bound a new ServerSocket to a given port
            server = new ServerSocket(parent.getResources().getInteger(R.integer.port_number));

            // Keep accepting new clients until the task is cancelled by the user
            while (!isCancelled()) {

                // Display Server IP address and port and notify it is up and running
                publishProgress(DISPLAY_SERVER_DATA_AND_RUNNING);
                // Block to wait for new Clients
                socket = server.accept();

                // Notify a new Client has been accepted
                publishProgress(DISPLAY_NEW_CLIENT);
                // Get the incoming image and save it on internal storage
                receiveAndSaveImage(socket);
                // Close the socket
                socket.close();

                // Sample the received image
                bitmap = ImageUtils.sampleImage(parent, ImageUtils.GET_IMAGE_FROM_FILE, "file_received.png");
                // Display the image on the UI
                publishProgress(DISPLAY_IMAGE);
            }
        } catch (IOException e) {
            // If the task was cancelled then this error is due to the ServerSocket being closed
            // while waiting for new clients. It is safe to dismiss it.
            // Otherwise display a notification to the user
            if (!isCancelled()) {
                publishProgress(DISPLAY_SERVER_ERROR);
            }
        }
        return null;
    }

    /*
    * Receives the incoming image through the socket and saves it on internal storage.
    * */
    private void receiveAndSaveImage(Socket socket) {

        try {
            // Get the input channel
            InputStream is = socket.getInputStream();
            // Get an output channel on internal storage
            FileOutputStream fos = parent.openFileOutput("file_received.png", Context.MODE_PRIVATE);
            // Read and write the incoming image in chunks of 1024 bytes
            byte[] buffer = new byte[1024];
            int count;
            while ((count = is.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
            // Close all channels
            is.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * Updates the UI as the state of the Server changes
    * */
    @Override
    protected void onProgressUpdate(Integer... values) {

        switch (values[0]) {

            // ServerSocket up and running
            case DISPLAY_SERVER_DATA_AND_RUNNING:
                parent.notifyServerRunning();
                break;

            // New client accepted
            case DISPLAY_NEW_CLIENT:
                parent.notifyNewClient();
                break;

            // Image received
            case DISPLAY_IMAGE:
                parent.displayReceivedImage(bitmap);
                break;

            // Some error occurred during the connection
            case DISPLAY_SERVER_ERROR:
                parent.displayNotifications(SocketActivity.SERVER_ERROR);
                break;
        }
    }

    /*
    * Updates the UI when the task finishes
    * */
    @Override
    protected void onPostExecute(Void result) {
        // Notifies the user that the Server is down
        parent.displayNotifications(SocketActivity.SERVER_DOWN);
    }

    /*
    * Updates the UI when the task finishes in case it was cancelled
    * */
    @Override
    protected void onCancelled() {
        // Notifies the user that the Server is down
        parent.displayNotifications(SocketActivity.SERVER_DOWN);
    }

}
