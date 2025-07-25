package io.github.a5h73y.parkour.plugin;

import static io.github.a5h73y.parkour.other.ParkourConstants.AMOUNT_PLACEHOLDER;
import static io.github.a5h73y.parkour.other.ParkourConstants.COURSE_PLACEHOLDER;
import static io.github.a5h73y.parkour.other.ParkourConstants.ERROR_INVALID_AMOUNT;
import static io.github.a5h73y.parkour.other.ParkourConstants.ERROR_NO_EXIST;
import static io.github.a5h73y.parkour.other.ParkourConstants.ERROR_UNKNOWN_PLAYER;
import static org.bukkit.Bukkit.getServer;

import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.type.course.CourseConfig;
import io.github.a5h73y.parkour.utility.PluginUtils;
import io.github.a5h73y.parkour.utility.TranslationUtils;
import io.github.a5h73y.parkour.utility.ValidationUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

/**
 * Vault Economy integration.
 * When the EconomyAPI class is initialised, an attempt is made to connect to Vault / Economy.
 * If the outcome succeeds and a provider is found, economy will be enabled.
 * If Parkour does not link to an Economy plugin, all attempted purchases will be successful.
 */
public class EconomyApi extends PluginWrapper {

	private Economy economy;

	private boolean displayCurrencyName;

	public EconomyApi(Parkour parkour) {
		super(parkour);
	}

	@Override
	public String getPluginName() {
		return "Vault";
	}

	@Override
	protected void initialise() {
		super.initialise();

		if (isEnabled()) {
			if (parkour.getParkourConfig().getBoolean("Plugin.Vault.RegisterParkoins")) {
				getServer().getServicesManager().register(
						Economy.class, new ParkoinsVault(parkour), parkour, ServicePriority.Normal);
			}

			RegisteredServiceProvider<Economy> economyProvider =
					getServer().getServicesManager().getRegistration(Economy.class);

			if (economyProvider == null) {
				PluginUtils.log("[Economy] Failed to connect to Vault's Economy service. Disabling Economy.", 2);
				setEnabled(false);
				return;
			}

			economy = economyProvider.getProvider();
			displayCurrencyName = parkour.getParkourConfig().getBoolean("Other.Display.CurrencyName");
		}
	}

	public boolean isEconomyLinked() {
		return this.economy != null;
	}

	/**
	 * Reward the Player with an amount.
	 *
	 * @param player target player
	 * @param amount amount to reward
	 * @return transaction success
	 */
	public boolean rewardPlayer(Player player, double amount) {
		return isEconomyLinked() && this.economy.depositPlayer(player, amount).transactionSuccess();
	}

	/**
	 * Charge the Player an amount.
	 *
	 * @param player target player
	 * @param amount amount to charge
	 * @return transaction success
	 */
	public boolean chargePlayer(Player player, double amount) {
		return isEconomyLinked() && this.economy.withdrawPlayer(player, amount).transactionSuccess();
	}

	/**
	 * Check if the Player has the requested amount of currency.
	 *
	 * @param player target player
	 * @param amount amount required
	 * @return player has sufficient amount
	 */
	public boolean hasAmount(Player player, double amount) {
		return isEconomyLinked() && this.economy.has(player, amount);
	}

	/**
	 * Get the Economy Currency name.
	 *
	 * @return currency name
	 */
	public String getCurrencyName() {
		return !isEconomyLinked() || !ValidationUtils.isStringValid(economy.currencyNamePlural())
				? "" : " " + economy.currencyNamePlural();
	}

	/**
	 * Reward Player for completing Course.
	 * If Economy is enabled, give the Player the amount of currency earned.
	 *
	 * @param player target player
	 * @param courseName course name
	 */
	public void giveEconomyPrize(Player player, String courseName) {
		if (isEconomyLinked()) {
			double reward = parkour.getConfigManager().getCourseConfig(courseName).getEconomyFinishReward();

			if (reward > 0) {
				rewardPlayer(player, reward);

				player.sendMessage(TranslationUtils.getTranslation("Economy.Reward")
						.replace(AMOUNT_PLACEHOLDER, getAmount(reward))
						.replace(COURSE_PLACEHOLDER, courseName));
			}
		}
	}

	/**
	 * Get formatted value of amount.
	 * @param amount amount to format
	 * @return formatted amount
	 */
	public String getAmount(double amount) {
		StringBuilder sb = new StringBuilder(String.valueOf(amount));
		if (displayCurrencyName) {
			sb.append(getCurrencyName());
		}
		return sb.toString();
	}

	/**
	 * Display the Economy information.
	 *
	 * @param commandSender command sender
	 */
	public void displayEconomyInformation(CommandSender commandSender) {
		TranslationUtils.sendHeading("Economy Details", commandSender);
		TranslationUtils.sendValue(commandSender, "Enabled", String.valueOf(isEnabled()));

		if (isEnabled()) {
			TranslationUtils.sendValue(commandSender, "Economy", economy.getName());
			TranslationUtils.sendValue(commandSender, "Currency Name", getCurrencyName());
		}
	}

	/**
	 * Validate and Charge the Player for joining Course.
	 * Check if there is a Join Fee for the Course, and that the player has sufficient funds.
	 * If there is a Fee, check if it's a one-time Fee, charge the Player and continue.
	 *
	 * @param player requesting player
	 * @param courseName course name
	 * @return Player can join course
	 */
	public boolean validateAndChargeCourseJoin(Player player, String courseName) {
		boolean allowed = true;
		if (isEconomyLinked()) {
			double joinFee = parkour.getConfigManager().getCourseConfig(courseName).getEconomyJoiningFee();

			if (joinFee > 0) {
				boolean oneTimeFee = parkour.getConfigManager().getCourseConfig(courseName).isOneTimeFee();
				if (!oneTimeFee || !parkour.getPermissionVault().hasPaidOneTimeFee(player, courseName)) {
					if (!hasAmount(player, joinFee)) {
						player.sendMessage(TranslationUtils.getTranslation("Economy.Insufficient")
								.replace(AMOUNT_PLACEHOLDER, getAmount(joinFee))
								.replace(COURSE_PLACEHOLDER, courseName));
						allowed = false;

					} else {
						chargePlayer(player, joinFee);
						player.sendMessage(TranslationUtils.getTranslation("Economy.Fee")
								.replace(AMOUNT_PLACEHOLDER, getAmount(joinFee))
								.replace(COURSE_PLACEHOLDER, courseName));
						if (oneTimeFee) {
							parkour.getPermissionVault().setPaidOneTimeFee(player, courseName);
						}
					}
				}
			}
		}

		return allowed;
	}

	/**
	 * Process Economy Command input.
	 * Each of the valid commands will be processed based on input.
	 *
	 * @param commandSender command sender
	 * @param args command arguments
	 */
	public void processCommand(CommandSender commandSender, String... args) {
		if (!isEconomyLinked()) {
			TranslationUtils.sendValueTranslation("Error.PluginNotLinked", getPluginName(), commandSender);
			return;
		}

		switch (args[1].toLowerCase()) {
			case "setprize":
				processSetPrizeCommand(commandSender, args);
				break;

			case "setfee":
				processSetFeeCommand(commandSender, args);
				break;

			case "info":
				displayEconomyInformation(commandSender);
				break;

			case "add":
				processAddCommand(commandSender, args);
				break;

			case "deduct":
				processDeductCommand(commandSender, args);
				break;

			case "amount":
				processAmountCommand(commandSender, args);
				break;

			default:
				parkour.getParkourCommands().sendInvalidSyntax(commandSender, "economy");
		}
	}

	private void processSetPrizeCommand(CommandSender commandSender, String... args) {
		if (args.length != 4) {
			TranslationUtils.sendInvalidSyntax(commandSender, "econ", "setprize (course) (amount)");
			return;
		}

		if (!parkour.getCourseManager().doesCourseExist(args[2])) {
			TranslationUtils.sendValueTranslation(ERROR_NO_EXIST, args[2], commandSender);
			return;
		}

		if (!ValidationUtils.isPositiveDouble(args[3])) {
			TranslationUtils.sendTranslation(ERROR_INVALID_AMOUNT, commandSender);
			return;
		}

		parkour.getConfigManager().getCourseConfig(args[2]).setEconomyFinishReward(Double.parseDouble(args[3]));
		TranslationUtils.sendPropertySet(commandSender, "Economy Prize", args[2], args[3]);
	}

	private void processSetFeeCommand(CommandSender commandSender, String... args) {
		if (args.length < 4) {
			TranslationUtils.sendInvalidSyntax(commandSender, "econ", "setfee (course) (amount) [one-time-fee]");
			return;
		}

		if (!parkour.getCourseManager().doesCourseExist(args[2])) {
			TranslationUtils.sendValueTranslation(ERROR_NO_EXIST, args[2], commandSender);
			return;
		}

		if (!ValidationUtils.isPositiveDouble(args[3])) {
			TranslationUtils.sendTranslation(ERROR_INVALID_AMOUNT, commandSender);
			return;
		}

		boolean oneTimeFee = args.length == 5 && args[4].equalsIgnoreCase("true");
		CourseConfig config = parkour.getConfigManager().getCourseConfig(args[2]);
		config.setEconomyJoiningFee(Double.parseDouble(args[3]));
		config.setEconomyOneTimeFee(oneTimeFee);
		TranslationUtils.sendPropertySet(commandSender, "Join Fee", args[2], args[3]);
	}

	private void processAddCommand(CommandSender commandSender, String... args) {
		if (args.length != 4) {
			TranslationUtils.sendInvalidSyntax(commandSender, "econ", "add (player) (amount)");
			return;
		}

		OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[2]);

		if (!economy.hasAccount(targetPlayer)) {
			TranslationUtils.sendTranslation(ERROR_UNKNOWN_PLAYER, commandSender);
			return;
		}

		if (!ValidationUtils.isPositiveDouble(args[3])) {
			TranslationUtils.sendTranslation(ERROR_INVALID_AMOUNT, commandSender);
			return;
		}

		double amount = Double.parseDouble(args[3]);
		EconomyResponse response = economy.depositPlayer(targetPlayer, amount);
		if (response.transactionSuccess()) {
			TranslationUtils.sendMessage(commandSender, "Added &b" + getAmount(amount) + "&f. "
					+ args[2] + "'s new amount: &b" + getAmount(economy.getBalance(targetPlayer)));
		} else {
			TranslationUtils.sendMessage(commandSender, "Failed to add amount to Player.");
		}
	}

	private void processDeductCommand(CommandSender commandSender, String... args) {
		if (args.length != 4) {
			TranslationUtils.sendInvalidSyntax(commandSender, "econ", "deduct (player) (amount)");
			return;
		}

		OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[2]);

		if (!economy.hasAccount(targetPlayer)) {
			TranslationUtils.sendTranslation(ERROR_UNKNOWN_PLAYER, commandSender);
			return;
		}

		if (!ValidationUtils.isPositiveDouble(args[3])) {
			TranslationUtils.sendTranslation(ERROR_INVALID_AMOUNT, commandSender);
			return;
		}

		double amount = Double.parseDouble(args[3]);
		EconomyResponse response = economy.withdrawPlayer(targetPlayer, amount);
		if (response.transactionSuccess()) {
			TranslationUtils.sendMessage(commandSender, "Deducted &b" + getAmount(amount) + "&f. "
					+ args[2] + "'s new amount: &b" + getAmount(economy.getBalance(targetPlayer)));
		} else {
			TranslationUtils.sendMessage(commandSender, "Failed to deduct amount to Player.");
		}
	}

	private void processAmountCommand(CommandSender commandSender, String... args) {
		if (args.length != 3) {
			TranslationUtils.sendInvalidSyntax(commandSender, "econ", "amount (player)");
			return;
		}

		OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[2]);

		if (!economy.hasAccount(targetPlayer)) {
			TranslationUtils.sendTranslation(ERROR_UNKNOWN_PLAYER, commandSender);
			return;
		}

		TranslationUtils.sendMessage(commandSender, args[2] + "'s balance: &b"
				+ getAmount(economy.getBalance(targetPlayer)));
	}

	@Override
	public String getPluginDisplayName() {
		return "Vault (Economy)";
	}
}
