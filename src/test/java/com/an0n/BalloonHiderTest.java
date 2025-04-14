package com.an0n;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BalloonHiderTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BalloonHiderPlugin.class);
		RuneLite.main(args);
	}
}