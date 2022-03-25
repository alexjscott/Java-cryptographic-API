import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;

public class Server {

	public static void main(String args[]) throws Exception {

		// Listen for connections
		int port = Integer.parseInt(args[0]);
		ServerSocket serverSocket = new ServerSocket(port);

		// Collection of posts
		ArrayList<ArrayList<String>> posts = new ArrayList<ArrayList<String>>();

		while(true) {

			Socket socket = serverSocket.accept();
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

			// send posts
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.writeObject(posts);

			// Read inputs
			String userid, date, message;
			byte[] sendersig;
			try {
				userid = objectInputStream.readUTF();
				date = objectInputStream.readUTF();
				message = objectInputStream.readUTF();
				sendersig = (byte[]) objectInputStream.readObject();
			} catch (EOFException eofException) {continue;}

			// read user pub key
			PublicKey pubKey = null;
			try {
				ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(userid + ".pub"));
				pubKey = (PublicKey)keyIn.readObject();
			} catch(FileNotFoundException fileNotFoundException) {continue;}

			// Verify signature
			Signature sig = Signature.getInstance("SHA1withRSA");
			String sigstr = userid+date+message;
			sig.initVerify(pubKey);
			sig.update(sigstr.getBytes());
			boolean b = sig.verify(sendersig);

			if (b) {
				System.out.println("Sender: " + userid + " (Signature accepted)");
				System.out.println("Date: " + date);
				System.out.println("Message: " + message); System.out.println("");

				// Create post
				ArrayList<String> post = new ArrayList<String>();
				post.add(userid);
				post.add(date);
				post.add(message);
				posts.add(post);
			}
			else {
				// Debug purposes
				System.out.println("Sender: " + userid + " (Signature rejected)");
				System.out.println("Date: " + date);
				System.out.println("Message: " + message); System.out.println("");
			}
		}
	}
}