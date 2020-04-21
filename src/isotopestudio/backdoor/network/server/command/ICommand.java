package isotopestudio.backdoor.network.server.command;

import java.util.Scanner;

import isotopestudio.backdoor.network.server.command.commands.InfoCommand;

public interface ICommand {

	public static ICommand[] commands = new ICommand[] {
			new InfoCommand()
	};

	public abstract void handle(String[] args);

	public abstract String getCommand();

	public abstract String getDescription();

	public static void command(String line) {
		String[] args = line.split(" ");
		String target = args[0];

		System.out.println(line);

		for (ICommand command : commands) {
			if (command.getCommand().equalsIgnoreCase(target)) {
				String[] arguments = new String[args.length - 1];
				if (args.length > 1) {
					for (int i = 1; i < args.length; i++) {
						arguments[i - 1] = args[i];
					}
				}
				command.handle(arguments);
				return;
			}
		}
		System.err.println("This command is unknown");
	}

	public static Thread listenJavaConsole() {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				Scanner java_console_scanner = new Scanner(System.in);
				String line = null;
				while ((line = java_console_scanner.nextLine()) != null) {
					command(line);
				}
				java_console_scanner.close();
			}
		});
	}
}
