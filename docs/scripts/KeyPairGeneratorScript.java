import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class KeyPairGeneratorScript {
	private static final int FIRST_PORT = 8000;
	private static final String PRIVATE_KEY_BASE_FILENAME = "HDSNotary_PrivateK_ID_";
	private static final String PUBLIC_KEY_BASE_FILENAME = "HDSNotary_PublicK_ID_";
	private static final String KEY_FACTORY_ALGORITHM = "RSA";
	private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Input the number of known nodes in the HDSNotary System.");
			System.exit(-1);
		}
		
		int nodesNumber = Integer.parseInt(args[0]);
		try {
			Key publicKey, privateKey;
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);

			int lastPort = nodesNumber + FIRST_PORT;
			for (int firstPort = FIRST_PORT; firstPort <= lastPort; firstPort++) {
				KeyPair keyPair = keyPairGenerator.generateKeyPair();
				publicKey = keyPair.getPublic();
				privateKey = keyPair.getPrivate();

				FileOutputStream out = new FileOutputStream(PRIVATE_KEY_BASE_FILENAME + firstPort + ".key");
				out.write(privateKey.getEncoded());

				out = new FileOutputStream(PUBLIC_KEY_BASE_FILENAME + firstPort + ".pub");
				out.write(publicKey.getEncoded());

				System.out.println("Generated KeyPair for node " + firstPort);
			}
			System.out.println("Done.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}