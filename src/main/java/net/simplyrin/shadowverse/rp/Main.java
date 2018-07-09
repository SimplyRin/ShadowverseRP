package net.simplyrin.shadowverse.rp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.entities.DiscordBuild;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;

import net.simplyrin.threadpool.ThreadPool;

/**
 * Created by SimplyRin on 2018/07/08.
 *
 * Copyright (C) 2018 SimplyRin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Main {

	private static Main instance;

	private RichPresence.Builder presence;
	private IPCClient ipcClient;

	public static void main(String[] args) {
		instance = new Main();
		instance.run();
	}

	public void run() {
		this.startTask();
		this.addShutdownHook();
		this.keepAlive();

		System.out.println("Shadowverse IPC has ready! Have fun!");

		Scanner scanner = new Scanner(System.in);

		while(true) {
			String[] args = scanner.nextLine().split(" ");

			if(args[0].equalsIgnoreCase("/shutdown")) {
				System.exit(0);
			}

			if(args[0].equalsIgnoreCase("/details") || args[0].equalsIgnoreCase("/d")) {
				if(args.length > 1) {
					String details = "";
					for(int i = 1; i < args.length; i++) {
						details = details + args[i] + " ";
					}
					details.substring(0, details.length() - 1);
					this.presence.setDetails(details);
					this.ipcClient.sendRichPresence(this.presence.build());
					System.out.println("Details has been updated to '" + details + "'!");
					return;
				} else {
					System.out.println("Usage: /details <details>");
				}
			} else {
				System.out.println("Available commands");
				System.out.println("/shutdown - Closing Shadowverse IPC");
			}
		}
	}

	public void connect() {
		this.ipcClient = null;
		this.ipcClient = new IPCClient(465472223843713024L);
		try {
			this.ipcClient.connect(new DiscordBuild[0]);
		} catch (NoDiscordClientException e) {
			System.out.println("You don't have Discord Client!");
			System.exit(0);
			return;
		}
		this.presence = null;
		this.presence = new RichPresence.Builder();
		this.presence.setDetails("ランクマッチをプレイ中");
		this.presence.setStartTimestamp(OffsetDateTime.now());
		this.presence.setSmallImage("mini", "Shadowverse から逃げるな");
		this.presence.setLargeImage("shadowverse", "Shadowverse");
		this.ipcClient.sendRichPresence(this.presence.build());
	}

	public void disconnect() {
		if(this.ifNull) {
			this.ipcClient.close();
			this.ipcClient = null;
		}
	}

	private boolean ifNull = false;
	private boolean back = false;

	public void startTask() {
		ThreadPool.run(() -> {
			while(true) {
				boolean isShadowverse = this.isShadowverse();

				if(isShadowverse) {
					if(!this.back) {
						System.out.println("Shadowverse.exe の起動を検知しました。");
						this.connect();
						this.back = true;
						this.ifNull = true;
					}
				} else if(this.back) {
					System.out.println("Shadowverse.exe の停止を検知しました。");
					this.disconnect();
					this.back = false;
				}

				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public boolean isShadowverse() {
		ProcessBuilder processBuilder = new ProcessBuilder("tasklist");
        Process process;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			return false;
		}
		Scanner scanner = new Scanner(new InputStreamReader(process.getInputStream()));

		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();

			if(line.startsWith("Shadowverse.exe")) {
				return true;
			}
		}

		return false;
	}

	public void keepAlive() {
		ThreadPool.run(() -> {
			while(true) {
				try {
					Thread.sleep(Integer.MAX_VALUE);
				} catch (Exception e) {
				}
			}
		});
	}

	public void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting down...");
				Main.this.ipcClient.close();
			}
		});
	}

	public void println() {

	}

}
