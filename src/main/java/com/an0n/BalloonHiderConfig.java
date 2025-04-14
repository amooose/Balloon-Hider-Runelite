package com.an0n;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(BalloonHiderConfig.GROUP)
public interface BalloonHiderConfig extends Config
{
	String GROUP = "BalloonHider";
	@ConfigItem(
		keyName = "hideBasket",
		name = "Hide Basket",
		description = "Hides the basket along with the balloon."
	)
	default boolean hideBasket()
	{
		return false;
	}
}
