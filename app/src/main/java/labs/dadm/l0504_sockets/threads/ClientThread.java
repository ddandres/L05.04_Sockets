/*
 * Copyright (c) 2020. David de Andrés and Juan Carlos Ruiz, DISCA - UPV, Development of apps for mobile devices.
 */

package labs.dadm.l0504_sockets.threads;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import labs.dadm.l0504_sockets.R;
import labs.dadm.l0504_sockets.activities.SocketActivity;

public class ClientThread extends Thread {

    final private WeakReference<SocketActivity> reference;
    String serverAddress;
    Uri imageUri;
    boolean imageSent;

    public ClientThread(SocketActivity activity, String serverAddress, Uri imageUri) {
        super();
        reference = new WeakReference<>(activity);
        this.serverAddress = serverAddress;
        this.imageUri = imageUri;
    }

    @Override
    public void run() {
        imageSent = false;

        // Notifies the user, before starting the background task, that the image is about to be sent
        if (reference.get() != null) {
            reference.get().runOnUiThread(() -> reference.get().displayNotifications(SocketActivity.SENDING_IMAGE));
        }

        // Creates a Socket to connect to the Server device and sends an image.
        try {
            Socket socket = new Socket();
            /// Connects to the Server at the given IP address and port
            if (reference.get() != null) {
                socket.connect(
                        new InetSocketAddress(
                                InetAddress.getByName(serverAddress),
                                reference.get().getResources().getInteger(R.integer.port_number)
                        )
                );
                // Get the output channel through the Socket
                OutputStream os = socket.getOutputStream();
                // Get the input channel to image through its URI
                if (reference.get() != null) {
                    InputStream is = reference.get().getContentResolver().openInputStream(imageUri);
                    // Read and send the image in chunks of 1024 bytes
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = is.read(buffer)) != -1) {
                        os.write(buffer, 0, count);
                    }
                    os.flush();
                    // Close all the channels and Socket
                    is.close();
                }
                // Close all the channels and Socket
                os.close();
            }
            socket.close();

            imageSent = true;

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Notifies the user, after the task finishes, the result of the task
        if (reference.get() != null) {
            if (imageSent) {
                // The image was successuflly sent
                reference.get().runOnUiThread(() -> reference.get().displayNotifications(SocketActivity.IMAGE_SENT));

            } else {
                // There was some problem when transferring the image
                reference.get().runOnUiThread(() -> reference.get().displayNotifications(SocketActivity.IMAGE_NOT_SENT));
            }
        }
    }

}