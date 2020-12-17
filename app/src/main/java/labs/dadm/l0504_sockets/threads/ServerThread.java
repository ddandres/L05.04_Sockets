/*
 * Copyright (c) 2020. David de Andrés and Juan Carlos Ruiz, DISCA - UPV, Development of apps for mobile devices.
 */

package labs.dadm.l0504_sockets.threads;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;

import labs.dadm.l0504_sockets.R;
import labs.dadm.l0504_sockets.activities.SocketActivity;
import labs.dadm.l0504_sockets.utils.ImageUtils;

public class ServerThread extends Thread {

    private static final int DISPLAY_SERVER_DATA_AND_RUNNING = 0;
    private static final int DISPLAY_NEW_CLIENT = 1;
    private static final int DISPLAY_IMAGE = 2;
    private static final int DISPLAY_SERVER_ERROR = 3;

    // Hold reference to the ServerSocket (Server device)
    private ServerSocket server;
    // Hold reference to the Bitmap object to display on the UI
    private Bitmap bitmap;

    private boolean cancelled;

    // Hold reference to its parent activity
    final private WeakReference<SocketActivity> reference;

    public ServerThread(SocketActivity activity) {
        super();
        reference = new WeakReference<>(activity);
    }

    public ServerSocket getServer() {
        return server;
    }

    public void cancel() {
        this.cancelled = true;
    }

    /*
     * Starts a ServerSocket to accept connections and receive a new image.
     * It only supports one Client at a time.
     * */
    @Override
    public void run() {
        Socket socket;
        SocketActivity activity;

        cancelled = false;

        if (reference.get() != null) {
            try {
                // Bound a new ServerSocket to a given port
                server = new ServerSocket(reference.get().getResources().getInteger(R.integer.port_number));

                // Keep accepting new clients until the task is cancelled by the user
                while (!cancelled) {

                    // Display Server IP address and port and notify it is up and running
                    if (reference.get() != null) {
                        reference.get().runOnUiThread(() -> reference.get().notifyServerRunning());
                    }

                    // Block to wait for new Clients
                    socket = server.accept();

                    // Notify a new Client has been accepted
                    if (reference.get() != null) {
                        reference.get().runOnUiThread(() -> reference.get().notifyNewClient());
                    }

                    // Get the incoming image and save it on internal storage
                    receiveAndSaveImage(socket);
                    // Close the socket
                    socket.close();

                    // Sample the received image
                    if (reference.get() != null) {
                        bitmap = ImageUtils.sampleImage(reference.get(), ImageUtils.GET_IMAGE_FROM_FILE, "file_received.png");
                    }
                    // Display the image on the UI
                    if (reference.get() != null) {
                        reference.get().runOnUiThread(() -> reference.get().displayReceivedImage(bitmap));
                    }
                }
            } catch (IOException e) {
                // If the task was cancelled then this error is due to the ServerSocket being closed
                // while waiting for new clients. It is safe to dismiss it.
                // Otherwise display a notification to the user
                if (cancelled) {
                    if (reference.get() != null) {
                        reference.get().runOnUiThread(() -> reference.get().displayNotifications(SocketActivity.SERVER_ERROR));
                    }
                }
            }
        }

        // Updates the UI when the task finishes or it is cancelled
        if (reference.get() != null) {
            reference.get().runOnUiThread(() -> reference.get().displayNotifications(SocketActivity.SERVER_DOWN));
        }
    }

    /*
     * Receives the incoming image through the socket and saves it on internal storage.
     * */
    private void receiveAndSaveImage(Socket socket) {

        try {
            // Get the input channel
            InputStream is = socket.getInputStream();
            if (reference.get() != null) {
                // Get an output channel on internal storage
                FileOutputStream fos = reference.get().openFileOutput("file_received.png", Context.MODE_PRIVATE);
                // Read and write the incoming image in chunks of 1024 bytes
                byte[] buffer = new byte[1024];
                int count;
                while ((count = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }
                fos.flush();
                // Close all channels
                fos.close();
            }
            // Close all channels
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
