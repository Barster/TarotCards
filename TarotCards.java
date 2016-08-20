	/*
	 * Tarot card autoplayer for an online game that uses AMF encryption. The game is exactly
	 * like pairs, with the addition of questions in each level that much be answered in order
	 * to proceed to the next level.
	 */
	private String tarotData;
	private String tmp_flag;
	private String question;
	
	private int[] cardsInLevel = {6, 8, 10, 12, 12, 12, 14, 18, 24, 24};
	private int level;
	public boolean playing = false;
	
	// the flipped card IDs are stored as "flags"
	private int[] flags = new int[24];
	
	// Hashmap for storing clues and their solution ID
	private HashMap<String, Integer> cardAnswers = new HashMap<String, Integer>();
	
	private String[] clues = {"Place a card marked as 3 of Diamonds",
							  "Place a card marked as 3",
							  "Place a card lesser than 4",
							  "Place a card marked between 3 and 6",
							  "Place a card marked as 4 of Clubs",
							  "Place a card marked as 4",
							  "Place a card lesser than 5",
							  "Place the largest card of 2, 3, and 4",
							  "Place a card marked as 5",
							  "Place a card marked as 5 of Diamonds",
							  "Place a card marked as 6 of Hearts",
							  "Place a card marked as 6",
							  "Place a card greater than Q",
							  "Place a card marked as K of Hearts",
							  "Place a card with a hero in a black hat",
							  "Place a card marked as 7 of Clubs",
							  "Place a card marked as 7",
							  "Place a card marked as 8",
							  "Place a card with a hero in a purple hat",
							  "Place a card marked as 9 of Diamonds",
							  "Place a card marked as 9",
							  "Place a card marked as 10",
							  "Place the smallest card of J, Q, and K",
							  "Place a card with a Panda hero",
							  "Place a card marked as J",
							  "Place a card marked as Q",
							  "Place a card between 8 and 10",
							  "Place the largest card of 8, 9, and 10",
							  "Place a card with a male Hero"}; //
	
	private int[] clueIDs = {1001, 
							 1001,
							 1001,
							 1002,
							 1002,
							 1002,
							 1002,
							 1002,
							 1003,
							 1003,
							 1004,
							 1004,
							 1005,
							 1005,
							 1006,
							 1006,
							 1006,
							 1007,
							 1007,
							 1008,
							 1008,
							 1009,
							 1010,
							 1010,
							 1010,
							 1011,
							 1008,
							 1009,
							 1010};
	
	// Initialises the hash map for clues and their answer IDs
	public void initTarotMap() {
		cardAnswers.clear();
		
		for (int i= 0; i < clues.length; i++) {
			cardAnswers.put(clues[i], clueIDs[i]);
		}
	}
	
	public void startTarot() {
		byte[] request = Crypt.hexStringToByteArray("00 03 00 00 00 01 00 14 55 73 65 72 54 61 72 6f74 2e 73 74 61 72 74 54 61 72 6f 74 00 04 2f 3235 31 00 00 00 4f 0a 00 00 00 01 11 0a 0b 01 0f  76 65 72 73 69 6f 6e 06 " + Crypt.getAMFHexString(version) + " 05 74 73 05 " + Crypt.amfDouble(Double.parseDouble("" + ts)) + " 07 6b 65 79 06 01 0d 75 73 65 72 69 64 06 " + Crypt.getAMFHexString(userID) + " 01");
		tarotData = wrapper.bytePost(getServerURL(), request, "");
		playing = true;
	}
	
	// Open the game and get the current level
	public void viewTarot() {
		byte[] request = Crypt.hexStringToByteArray("00 03 00 00 00 01 00 0e 55 73 65 72 54 61 72 6f74 2e 76 69 65 77 00 03 2f 35 34 00 00 00 4f 0a00 00 00 01 11 0a 0b 01 0f 76 65 72 73 69 6f 6e06 " + Crypt.getAMFHexString(version) + " 07 6b65 79 06 01 0d 75 73 65 72 69 64 06 " + Crypt.getAMFHexString(userID) + " 05 74 73 05 " + Crypt.amfDouble(Double.parseDouble("" + ts)) + " 01");
		tarotData = wrapper.byteReturnPost(getServerURL(), request, "");
		level = getLevel(tarotData);

		System.out.println("Level: " + level);
	}
	
	// The main playing loop, finds and flips all matches and answers the clue questions
	public void playLevel() {
		int[] flips = new int[24];
		
		int cardID = 0;
		int numFlips = 0;
		int prevFlip = 0;
		int matches = 0;
		
		startTarot();
		
		// PLAYING LOOP
		for (int x = 0; x < cardsInLevel[level - 1]; x++) {
			String turnInfo = turnCard(level, x);
			cardID = getCardID(turnInfo);
			
			numFlips++;
			
			flips[x] = cardID;
			flags[x] = cardID;
			// Matched a pair with 2 consecutive flips
			if ( cardID == prevFlip && tmp_flag.compareTo("02") == 0) {
				System.out.println("MATCH FLIPPED!");
				flips[x - 1] = -1;
				flips[x]     = -1;
				matches++;
			}
			
			// CHECK THE FLIP IDS FOR MATCHES IF IT'S AN ODD FLIP
			if ( (numFlips % 2) > 0 ) {
				for (int y = 0; y < flips.length; y++) {
					// skip it if Y and X are same number
					if ((y == x && flips[x] != -1) || flips[y] == -1) { continue; }
					
					//MATCH!!
					if ( flips[x] == flips[y] && tmp_flag.compareTo("02") == 0) {
						System.out.println("Flipping match at pos " + y);
						matches++;
						turnCard(level, y);
						numFlips++;
						flips[x] = -1;
						flips[y] = -1;
						break;
					}
				}
			}
			
			prevFlip = cardID;
			System.out.println("Pos " + x + " ID: " + cardID);
		}
		
		// After the main playing loop we cycle back through and finish flipping any matches
		for (int i = 0; i < cardsInLevel[level - 1]; i++) {
			int id = flips[i];
			
			if (id == -1) {
				continue; 
			} else {
				for (int j = 0; j < flips.length; j++) {
					if (i == j) continue; // SKIP IF IT'S THE SAME INDEX
					if (j == -1) continue;// SKIP IF J = -1
					
					if (flips[i] == flips[j]) {
						System.out.println("Unclaimed match: Positions " + i + " and " + j);
						turnCard(level, i);
						flips[i] = -1;
						numFlips++;

						turnCard(level, j);
						flips[j] = -1;
						numFlips++;
						matches++;
						break;
					}
				}
			}
			
		}
		
		System.out.println("Made " + matches + " matches with " + numFlips + " flips");
		if (level > 1) {
			viewTarot();
			ArrayList<String> questions = getQuestions();
			playQuestions(questions);
		}
		playing = false;
		System.out.println("Completed playing game.");
	}
	
	// Used for resuming a game if it has already been started
	public void getFlags() {
		if (tarotData.contains("666c61670911")) {
			System.out.println("Playded before, getting flags...");
			String flagData = Parsing.getBetween(tarotData, "666c61670911", "11");
		}
	}
	
	// Gets the question from the level and converts it to a readable UTF string
	public String getQuestion() {
		try {
			// "Question" in hex
			if (tarotData.contains("7175657374696f6e")) {
				String questionData = Parsing.getBetween(tarotData, "7175657374696f6e", "1c040");
				questionData = questionData.substring(questionData.indexOf("06") + 4, questionData.indexOf("2e"));
				String question = Crypt.readAmfString(questionData);
				System.out.println("Question: " + question);
				System.out.println("Card ID: " + getQuestionAnswerPos(question));
				return question;
			}
		} catch (NullPointerException e) {
			System.out.println("no question found!");
		}
		return "none";
	}
	
	// After level 2 there is more than 1 question, so split them up and parse them
	public ArrayList<String> getQuestions() {
		viewTarot();
		ArrayList<String> questions = new ArrayList<String>();
		
		// "Question" in hex
		if (tarotData.contains("7175657374696f6e")) {
			String questionData = Parsing.getBetween(tarotData, "7175657374696f6e", "646179506f69");

			//0a0b01 is setting a new array in AMF, that's how we know where to split the hex data
			questions = Parsing.getAll(questionData, "0a0b01", "2e");
			
			// Go through the found questions and convert them from hex to utf so we can match it
			// with the correct card ID
			for (int i = 0; i < questions.size(); i++) {
				String formatt = questions.get(i);
				formatt = formatt.substring(formatt.indexOf("06") + 4, formatt.length());
				questions.set(i, Crypt.readAmfString(formatt));
				System.out.println("Question " + i + ": " + questions.get(i));
			}
		}
		
		return questions;
	}
	
	// Go through the flipped cards to find the one that matche the clues to solve the level
	public int getQuestionAnswerPos(String clue) {
		int id = cardAnswers.get(clue);
		for (int i = 0; i < flags.length; i++) {
			if (flags[i] == id) {
				return i;
			}
		}
		return -1;
	}
	
	// GETS CALLED BY viewTarot, DON'T USE ANYWHERE ELSE!
	// Gets the level we're currently on, maximum of 10 levels
	private int getLevel(String data) {
		String levelString = Parsing.getBetween(data,  "6c6576656c04", "73");
		
		levelString = levelString.substring(0, 2);
		if (levelString.compareTo("0a") == 0) return 10;
		return Integer.parseInt(levelString);
	}
	
	
	private void playQuestions(ArrayList<String> questions) {
		for (int i = 0; i < questions.size(); i++) {
			String question = questions.get(i);
			int pos = getQuestionAnswerPos(question);
			playClue(pos, question);
		}
	}
	
	private void playClue(int pos, String question) {
		
		System.out.println("Question: " + question);
		System.out.println("Clue position ID: " + pos);
		
		String data = "";
		byte [] request = Crypt.hexStringToByteArray("00 03 00 00 00 01 00 18 55 73 65 72 54 61 72 6f74 2e 74 75 72 6e 41 6e 73 77 65 72 43 61 72 6400 04 2f 33 34 38 00 00 00 77 0a 00 00 00 01 110a 0b 01 0d 75 73 65 72 69 64 06 " + Crypt.getAMFHexString(userID) + "05 74 73 05 " + Crypt.amfDouble(Double.parseDouble("" + ts)) + " 0f 76 65 7273 69 6f 6e 06 " + Crypt.getAMFHexString(version) + " 11 71 75 65 73 74 69 6f 6e 06 " + Crypt.getAMFHexString(question + ".") + " 03 70 04 " + Crypt.intToHex(pos) + " 07 6b 65 79 06 0101");
		data = wrapper.bytePost(getServerURL(), request, "");
		
		System.out.println(data);
	}
	
	private String turnCard(int level, int pos) {
		
		String Pos = "" + pos;
		
		if (pos >= 10) {
			Pos = Integer.toHexString(pos);
		} else {
			Pos = Integer.toHexString(pos);
		}
		
		String sPos = "";
		
		if (Pos.length() == 1) {
			sPos = "0" + Pos;
		} else {
			sPos = Pos;
		}
		
		String lvel = "" + level;
		String lvl = "";
		
		if (lvel.length() == 1) {
			lvl = "0" + lvel;
		}
		
		if (level == 10) {
			lvl = "0a";
		}
		
		System.out.println("Turning pos " + pos + " in level " + level);
		try {
			Thread.sleep(350);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		byte[] request = Crypt.hexStringToByteArray("00 03 00 00 00 01 00 12 55 73 65 72 54 61 72 6f74 2e 74 75 72 6e 43 61 72 64 00 04 2f 32 36 3600 00 00 5c 0a 00 00 00 01 11 0a 0b 01 07 6b 6579 06 01 0d 75 73 65 72 69 64 06 " + Crypt.getAMFHexString(userID) + "0b 6c 65 76 65 6c 04 " + lvl + " 0f 76 65 72 73 69 6f 6e06 " + Crypt.getAMFHexString(version) + " 05 7473 05 " + Crypt.amfDouble(Double.parseDouble("" + ts)) + " 05 70 31 04 " + sPos + " 01");
		tarotData = wrapper.byteReturnPost(getServerURL(), request, "");

		tmp_flag = Parsing.getBetween(tarotData, "746d705f666c616704", "05");
		System.out.println("tmp_flag: " + tmp_flag);
		return tarotData;
	}
	
	// Gets the ID of the card we just flipped
	private int getCardID(String data) {
		String id = Parsing.getBetween(data, "636172645f696e666f0609", "1165");
		System.out.println("ID string: " + id);
		
		String intID = "";
		
		// Since the ID is in amf format, we subtract 30 from the hex to get the decimal value
		for (int x = 0; x < id.length(); x += 2) {
			String digit = id.substring(x, x + 2);
			intID += (Integer.parseInt(digit) - 30);
		}
		
		return Integer.parseInt(intID);
	}
