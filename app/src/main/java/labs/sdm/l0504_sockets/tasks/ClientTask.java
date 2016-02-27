/*
 * Copyright (c) 2016. David de Andrés and Juan Carlos Ruiz, DISCA - UPV, Development of apps for mobile devices.
 */

package labs.sdm.l0504_sockets.tasks;

import android.net.Uri;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import labs.sdm.l0504_sockets.R;
import labs.sdm.l0504_sockets.activities.SocketActivity;

/*
* Sends an image to the Server device on a background thread using a Socket.
* Incoming parameters are a String representing the IP address of the Server device,
* and an URI to get access to the selected image.
* */
public class ClientTask extends AsyncTask<Object, Void, Boolean> {

    // Hold reference to its parent activity
    SocketActivity parent;

    public void setParent(SocketActivity parent) {
        this.parent = parent;
    }

    /*
    * Notifies the user, before starting the background task, that the image is about to be sent.
    * */
    @Override
    protected void onPreExecute() {
        parent.displayNotifications(SocketActivity.SENDING_IMAGE);
    }

    /*
    * Creates a Socket to connect to the Server device and sends an image.
    * */
    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            Socket socket = new Socket();
            /// Connects to the Server at the given IP address and port
            socket.connect(
                    new InetSocketAddress(
                            InetAddress.getByName((String) params[0]),
                            parent.getResources().getInteger(R.integer.port_number)
                    )
            );
            // Get the output channel through the Socket
            OutputStream os = socket.getOutputStream();
            // Get the input channel to image through its URI
            InputStream is = parent.getContentResolver().openInputStream((Uri) params[1]);
            // Read and send the image in chunks of 1024 bytes
            byte[] buffer = new byte[1024];
            int count;
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            os.flush();
            // Close all the channels and Socket
            is.close();
            os.close();
            socket.close();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
    * Notifies the user, after the task finishes, the result of the task.
    * */
    @Override
    protected void onPostExecute(Boolean result) {
        // The image was successuflly sent
        if (result) {
            parent.displayNotifications(SocketActivity.IMAGE_SENT);
        }
        // There was some problem when transferring the image
        else {
            parent.displayNotifications(SocketActivity.IMAGE_NOT_SENT);
        }
    }
}
