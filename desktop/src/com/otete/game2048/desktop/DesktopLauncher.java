package com.otete.game2048.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.otete.game2048.App;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = (int) (LwjglApplicationConfiguration.getDesktopDisplayMode().height * 0.9f);
//		config.width = config.height / 16 * 9;
		config.width = config.height / 4 * 3;
//		config.width = config.height / 20 * 9;
		config.initialBackgroundColor = App.clBackground;
		config.title = "2048";
		config.resizable = false;
		config.y = 0;
		new LwjglApplication(new App(), config);
	}
}
