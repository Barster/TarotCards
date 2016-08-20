	// Take a string and convert it to AMF format
	public static String getAMFHexString(String str) {
		// AMF uses "06" to specify a string is being implemented. The following byte
		// represents the length of the string in AMF
		String numOfBytes = (Integer.toHexString((byte) str.length() * 2 + 1));
		String hexString = stringToHex(str);
		
		if (numOfBytes.length() == 1) {
			return "0" + numOfBytes + hexString;
		}
		return numOfBytes + hexString;
	}
	
	// Take a raw string and convert it to the hex equivalent for AMF
	public static String stringToHex(String s) {
		int len = s.length();
		String data = "";
		
		// Go through each individual character and convert it to 2 digit hex
		for (int x = 0; x < len; x ++) {
			int charid = s.charAt(x);
			if (String.format("%02x", charid).length() == 1) {
				data = data + "0" + String.format("%02x", charid);
			} else {
				data = data + String.format("%02x", charid);
			}
		}
		return data;
	}
	
	// Once we have made our AMF string we need to convert it to a byte array in order to 
	// submit it to the server
	public static byte[] hexStringToByteArray(String s) {
		// Remove any potential spaces in the string
		s = s.replaceAll(" ", "");
	    int len = s.length();

	    byte[] data = new byte[len / 2];
	    // We skip through the string by 2s because each hex digit is represented by 2 digits
	    for (int i = 0; i < len; i += 2) {	
	    	// byte = (char at pos (i) converted to base 16, shift left by 4) + (char at pos (i+1) converted to base 16)
	    	data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
