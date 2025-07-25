package io.github.a5h73y.parkour.utility;

import org.bukkit.command.CommandSender;

/**
 * Validation related utility methods.
 */
public class ValidationUtils {

	/**
	 * Validate if the input is a valid String.
	 *
	 * @param input text
	 * @return input is a valid String
	 */
	public static boolean isStringValid(String input) {
		return input != null && !input.trim().isEmpty();
	}

	/**
	 * Validate if the input is a valid Integer.
	 *
	 * @param input text
	 * @return input is an Integer
	 */
	public static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (Exception ignored) {
			// it cannot be parsed as an integer
		}
		return false;
	}

	/**
	 * Validate if the input is a positive Integer.
	 * "1" = true, "Hi" = false, "-1" = false
	 *
	 * @param input text
	 * @return input is numeric and positive
	 */
	public static boolean isPositiveInteger(String input) {
		return input != null && isInteger(input) && Integer.parseInt(input) >= 0;
	}

	/**
	 * Validate if the input is a valid Double.
	 *
	 * @param input text
	 * @return input is a Double
	 */
	public static boolean isDouble(String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch (Exception ignored) {
			// it cannot be parsed as a double
		}
		return false;
	}

	/**
	 * Validate if the input is a positive Double.
	 *
	 * @param input text
	 * @return input is numeric and positive
	 */
	public static boolean isPositiveDouble(String input) {
		return input != null && isDouble(input) && Double.parseDouble(input) >= 0;
	}

	public static boolean isUuidFormat(String input) {
		return input != null && input.split("-").length == 5;
	}

	/**
	 * Validate the length of the arguments before allowing it to be processed further.
	 *
	 * @param commandSender command sender
	 * @param args command arguments
	 * @param required required args length
	 * @return whether the arguments match the criteria
	 */
	public static boolean validateArgs(CommandSender commandSender, String[] args, int required) {
		return validateArgs(commandSender, args, required, required);
	}

	/**
	 * Validate the range of the arguments before allowing it to be processed further.
	 *
	 * @param commandSender command sender
	 * @param args command arguments
	 * @param minimum minimum args length
	 * @param maximum maximum args length
	 * @return whether the arguments match the criteria
	 */
	public static boolean validateArgs(CommandSender commandSender, String[] args, int minimum, int maximum) {
		if (args.length > maximum) {
			TranslationUtils.sendValueTranslation("Error.TooMany", String.valueOf(maximum), commandSender);
			TranslationUtils.sendValueTranslation("Help.Command", args[0].toLowerCase(), false, commandSender);
			return false;

		} else if (args.length < minimum) {
			TranslationUtils.sendValueTranslation("Error.TooLittle", String.valueOf(minimum), commandSender);
			TranslationUtils.sendValueTranslation("Help.Command", args[0].toLowerCase(), false, commandSender);
			return false;
		}
		return true;
	}

	private ValidationUtils() {}
}
