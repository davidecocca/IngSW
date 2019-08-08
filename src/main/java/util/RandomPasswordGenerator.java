package util;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

public class RandomPasswordGenerator {

	private static final String ERROR_CODE = "error";

	public static String generateRandomPassword(int length, boolean upperCase, boolean digits, boolean symbols) {
		PasswordGenerator gen = new PasswordGenerator();
		String password;
		
		if (upperCase && digits && symbols) {
			password = gen.generatePassword(length, lowerRule(), upperRule(), digitsRule(), symbolsRule());
		}
		
		else if (upperCase && digits && !symbols) {
			password = gen.generatePassword(length, lowerRule(), upperRule(), digitsRule());
		}
		
		else if (upperCase && symbols && !digits) {
			password = gen.generatePassword(length, lowerRule(), upperRule(), symbolsRule());
		}
		
		else if (digits && symbols && !upperCase) {
			password = gen.generatePassword(length, lowerRule(), digitsRule(), symbolsRule());
		}
		
		else if (upperCase && !digits && !symbols) {
			password = gen.generatePassword(length, lowerRule(), upperRule());
		}
		
		else if (digits && !upperCase && ! symbols) {
			password = gen.generatePassword(length, lowerRule(), digitsRule());
		}
		
		else if (symbols && !upperCase && !digits) {
			password = gen.generatePassword(length, lowerRule(), symbolsRule());
		}
		
		else {
			password = gen.generatePassword(length, lowerRule());
		}

		return password;
	}

	//Lower case rule
	private static CharacterRule lowerRule() {
		//Set a minimum of 2 lower case characters
		CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
		CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
		lowerCaseRule.setNumberOfCharacters(2);
		return lowerCaseRule;
	}

	//Upper case rule
	private static CharacterRule upperRule() {
		//Set a minimum of 2 upper case characters
		CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
		CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
		upperCaseRule.setNumberOfCharacters(2);
		return upperCaseRule;
	}

	//Digits rule
	private static CharacterRule digitsRule() {
		//Set a minimum of 2 digit
		CharacterData digitChars = EnglishCharacterData.Digit;
		CharacterRule digitRule = new CharacterRule(digitChars);
		digitRule.setNumberOfCharacters(2);
		return digitRule;
	}

	//Symbols rule
	private static CharacterRule symbolsRule() {
		//Set a minimum of 2 special characters
		CharacterData specialChars = new CharacterData() {
			public String getErrorCode() {
				return ERROR_CODE;
			}

			public String getCharacters() {
				return "!@#$%^&*()_+";
			}
		};
		CharacterRule splCharRule = new CharacterRule(specialChars);
		splCharRule.setNumberOfCharacters(2);
		return splCharRule;
	}
}
