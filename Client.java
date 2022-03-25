import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.crypto.*;

public class Client {
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String userid = args[2];

		Socket socket = new Socket(host, port);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

		// read rsa key
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(userid + ".prv"));
		PrivateKey PrvKey = (PrivateKey) objectInputStream.readObject();

		// Read messages
		ObjectInputStream objectInputStream2 = new ObjectInputStream(socket.getInputStream());
		ArrayList<ArrayList<String>> posts = (ArrayList<ArrayList<String>>) objectInputStream2.readObject();

		if(posts.isEmpty()){System.out.println("There are 0 posts.\n");
		} else {
			int iterator = 0;
			int postSize = posts.size();
			System.out.println("There are " + postSize + " post(s).\n");

			while (posts.size() > iterator) {
				String senderuserid = posts.get(iterator).get(0);
				String date = posts.get(iterator).get(1);
				String message = posts.get(iterator).get(2);

				try {
					// Base64
					Base64.Decoder decoder = Base64.getDecoder();
					byte[] Base64msg = decoder.decode(message);

					try {
						// Decrypt RSA
						Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
						cipher.init(Cipher.DECRYPT_MODE, PrvKey);
						byte[] stringBytes = cipher.doFinal(Base64msg);
						String result = new String(stringBytes, "UTF8");

						System.out.println("Sender: " + senderuserid);
						System.out.println("Date: " + date);
						System.out.println("Message: " + result + "\n");

					} catch (BadPaddingException badPaddingException) {
						//System.out.println("BadPaddingException");
						System.out.println("Sender: " + senderuserid);
						System.out.println("Date: " + date);
						System.out.println("Message: " + message + "\n");
						}

				} catch (IllegalArgumentException illegalArgumentException) {
					//System.out.println("IllegalArgumentException");
					System.out.println("Sender: " + senderuserid);
					System.out.println("Date: " + date);
					System.out.println("Message: " + message + "\n");
					}

				iterator++ ;
			}
		}

		// Write message
		System.out.println("Do you want to add a post? [y/n]");
		BufferedReader brYesNO = new BufferedReader(new InputStreamReader(System.in));
		String yesNo = brYesNO.readLine().toLowerCase();
		System.out.println("");

		if(Objects.equals(yesNo, "y")){
			System.out.println("Enter the recipient userid (type \"all\" for posting without encryption):");
			BufferedReader brRecipient = new BufferedReader(new InputStreamReader(System.in));
			String recipient = brRecipient.readLine();
			System.out.println("");

			if (Objects.equals(recipient.toLowerCase(), "all")){
				System.out.println("Enter your message:");
				BufferedReader brAll = new BufferedReader(new InputStreamReader(System.in));
				String message = brAll.readLine();
				System.out.println("\n");

				// construct output
				Date timestamp = new Date();
				String strDate = timestamp.toString();
				String strSig = userid+strDate+message;

				// sign
				Signature sig = Signature.getInstance("SHA1withRSA");
				sig.initSign(PrvKey);
				sig.update(strSig.getBytes());
				byte[] signature = sig.sign();

				// send
				objectOutputStream.writeUTF(userid);
				objectOutputStream.writeUTF(strDate);
				objectOutputStream.writeUTF(message);
				objectOutputStream.writeObject(signature);

			} else {
				System.out.println("Enter your message:");
				BufferedReader brEncrypt = new BufferedReader(new InputStreamReader(System.in));
				String message = brEncrypt.readLine();
				System.out.println("");

				// convert to RSA
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(recipient + ".pub"));
				PublicKey Pubkey = (PublicKey)in.readObject();
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, Pubkey);
				byte[] raw = cipher.doFinal(message.getBytes("UTF8"));

				// Base64
				Base64.Encoder encoder = Base64.getEncoder();
				String base64msg = encoder.encodeToString(raw);

				// Construct output
				Date timestamp = new Date();
				String strDate = timestamp.toString();
				String strSig = userid+strDate+base64msg;

				// sign
				Signature sig = Signature.getInstance("SHA1withRSA");
				sig.initSign(PrvKey);
				sig.update(strSig.getBytes());
				byte[] signature = sig.sign();

				// send
				objectOutputStream.writeUTF(userid);
				objectOutputStream.writeUTF(strDate);
				objectOutputStream.writeUTF(base64msg);
				objectOutputStream.writeObject(signature);
			}
		} else {socket.close();}
	}
}
