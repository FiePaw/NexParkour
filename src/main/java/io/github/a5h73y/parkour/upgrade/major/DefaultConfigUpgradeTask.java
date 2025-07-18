package io.github.a5h73y.parkour.upgrade.major;

import io.github.a5h73y.parkour.upgrade.ParkourUpgrader;

public class DefaultConfigUpgradeTask extends TimedLegacyConfigUpgradeTask {

	public DefaultConfigUpgradeTask(ParkourUpgrader parkourUpgrader) {
		super(parkourUpgrader, parkourUpgrader.getDefaultConfig());
	}

	@Override
	protected String getTitle() {
		return "Default Config";
	}

	@Override
	protected boolean doWork() {
		updateConfigEntry("OnFinish.CompletedCourses.JoinMessage", "Other.Display.CompletedCourseJoinMessage");
		updateConfigEntry("Other.LogToFile", "Other.LogAdminTasksToFile");
		updateConfigEntry("Other.ParkourKit.ReplaceInventory", "ParkourKit.ReplaceInventory");
		updateConfigEntry("Other.ParkourKit.GiveSign", "ParkourKit.GiveSign");
		updateConfigEntry("Other.ParkourKit.LegacyGroundDetection", "ParkourKit.LegacyGroundDetection");
		updateConfigEntry("Other.Parkour.ChatRankPrefix.Enabled", "ParkourRankChat.Enabled");
		updateConfigEntry("Other.Parkour.ChatRankPrefix.OverrideChat", "ParkourRankChat.OverrideChat");

		getConfig().set("OnJoin.AllowViaCommand", null);
		getConfig().set("OnCourse.DieInLava", null);
		getConfig().set("OnCourse.DieInWater", null);
		getConfig().set("OnCourse.DieInVoid", null);
		getConfig().set("OnCourse.MaxFallTicks", null);
		getConfig().set("OnFinish.DefaultPrize", null);
		getConfig().set("OnFinish.CompletedCourses", null);
		getConfig().set("ParkourModes.Dropper", null);
		getConfig().set("ParkourModes.FreeCheckpoint", null);
		getConfig().set("DisplayTitle", null); // laziness
		getConfig().set("Other.ParkourKit", null);
		getConfig().set("Other.Parkour.ChatRankPrefix", null);
		getConfig().set("Lobby", null);

		// strings
		getParkourUpgrader().getStringsConfig().set("GUI.JoinCourses.Setup", null);
		getParkourUpgrader().getStringsConfig().set("GUI.CourseSettings.Setup", null);
		getParkourUpgrader().getStringsConfig().set("Parkour.Lobby", null);
		getParkourUpgrader().getStringsConfig().set("Parkour.LobbyOther", null);
		getParkourUpgrader().getStringsConfig().set("Parkour.Selected", null);
		getParkourUpgrader().getStringsConfig().set("Parkour.Deselected", null);
		getParkourUpgrader().getStringsConfig().set("Parkour.Accept", null);
		getParkourUpgrader().getStringsConfig().set("Error.Selected", null);
		return true;
	}
}
