package pl.wcja.yamc.utils;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomGenerator {

	public RandomGenerator() {
	}

	public static synchronized String getRandomString() {
		byte randBytes[] = new byte[16];
		getRandom().nextBytes(randBytes);
		String id = (new BigInteger(1, randBytes)).toString();
		return id;
	}

	private static SecureRandom getRandom() {
		if (random == null)
			try {
				random = SecureRandom.getInstance("SHA1PRNG");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		return random;
	}

	private static SecureRandom random = null;
}