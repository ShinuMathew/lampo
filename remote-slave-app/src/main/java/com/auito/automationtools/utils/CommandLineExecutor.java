package com.auito.automationtools.utils;

import static com.auito.automationtools.utils.StringUtils.streamToString;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.auito.automationtools.model.Platform;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CommandLineExecutor {

	private static final Platform PLATFORM = Platform.CURRENT_PLATFORM;

	private CommandLineExecutor() {
	}

	/**
	 * Run system commands and get response back.
	 *
	 * @param file {@link String}
	 * @param args {@link String}[] containing arguments that are to be passed to
	 *             executable
	 * @return {@link CommandLineResponse}
	 */
	public static CommandLineResponse execFile(final String file, final String... args) {
		if (isEmpty(file)) {
			return null;
		}

		switch (PLATFORM) {
		case LINUX:
		case MACINTOSH:
			return execCommand(mergeArrays(new String[] { "bash", file.trim() }, args));
		case WINDOWS:
			return execCommand(mergeArrays(new String[] { "cmd", "/c", file.trim() }, args));
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] mergeArrays(T[] arrA, T[] arrB) {
		return (T[]) Stream.of(arrA, arrB).flatMap(Stream::of).toArray();
	}

	private static boolean isEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	/**
	 * Run system commands and get response back.
	 *
	 * @param command {@link String}
	 * @return {@link CommandLineResponse}
	 */
	public static CommandLineResponse exec(final String command) {
		if (isEmpty(command)) {
			return null;
		}
		if (Platform.CURRENT_PLATFORM == Platform.WINDOWS) {
			return execCommand("cmd", "/c", command);
		}
		return execCommand("bash", "-c", command.trim());
	}

	public static CommandLineResponse execCommand(final String... command) {
		if (command == null || command.length == 0) {
			return null;
		}
		String _cmd = String.join(" ", command);
		log.debug("executing command : {}", _cmd);
		Process process = null;
		try {
			ProcessBuilder builder = new ProcessBuilder(command);
			CommandLineResponse response = new CommandLineResponse();
			if (Platform.CURRENT_PLATFORM != Platform.WINDOWS) {
				Map<String, String> env = builder.environment();
				env.put("PATH", env.get("PATH") + ":/usr/local/bin:" + System.getenv("HOME") + "/.linuxbrew/bin");
				process = builder.start();
			} else {
				Map<String, String> env = builder.environment();
				env.put("PATH", System.getenv("Path") == null ? System.getenv("PATH") : System.getenv("Path"));
				process = Runtime.getRuntime().exec("cmd /C " + String.join(" ", _cmd));
			}
			process.waitFor(60, TimeUnit.SECONDS);
			response.setStdOut(streamToString(process.getInputStream()).trim());
			response.setErrOut(streamToString(process.getErrorStream()).trim());
			response.setExitCode(process.exitValue());
			log.trace("response: {}", response);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	public static boolean killProcess(@NonNull String name) {
		return killProcess(name, false);
	}

	public static boolean killProcess(@NonNull String name, boolean killParent) {
		String cmd = killParent ? String.format(
				"ps -ef | grep -i '%s' | grep -v grep | awk '{print $2}' | tr -s '\\n' ' ' | xargs pkill -TERM -P",
				name)
				: String.format(
						"ps -ef | grep -i '%s' | grep -v stf | grep -v grep | awk '{print $2}' | tr -s '\\n' ' ' | xargs kill -9",
						name);
		return 0 == exec(cmd).getExitCode();
	}

	public static boolean killAppiumProcesses() {
		String cmd = "ps -ef | grep -v stf | grep -i 'appium' | grep -i 'node' | grep -i 'relaxed-security' | grep -v grep | awk '{print $2}' | tr -s '\\n' ' ' | xargs kill -9";
		return 0 == exec(cmd).getExitCode();
	}

	public static boolean killAppiumProcessesByDeviceId(@NonNull String deviceId) {
		String cmd = String.format(
				"ps -ef | grep -v stf | grep -i 'appium' | grep -i 'node' | grep -i 'relaxed-security' | grep -i '%s' | grep -v grep | awk '{print $2}' | tr -s '\\n' ' ' | xargs kill -9",
				deviceId);
		return 0 == exec(cmd).getExitCode();
	}

	public static boolean killProcessListeningAtPort(int port) {
		String cmd = String
				.format(Platform.CURRENT_PLATFORM == Platform.MACINTOSH ? "lsof -nti:%s | xargs kill -9"
						: "fuser -k %s/tcp", port);
		return 0 == exec(cmd).getExitCode();
	}

	public static boolean isPortListening(int port) {
		if (Platform.CURRENT_PLATFORM == Platform.WINDOWS) {
			return false;
		}
		String cmd = String
				.format(Platform.CURRENT_PLATFORM == Platform.MACINTOSH ? "lsof -nti:%s | wc -l"
						: "netstat -ntlp | grep LISTEN | grep %s | wc -l", port);
		return Integer.parseInt(exec(cmd).getStdOut().trim()) > 0;
	}

}