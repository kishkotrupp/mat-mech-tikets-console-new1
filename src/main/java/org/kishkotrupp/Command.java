package org.kishkotrupp;

public enum Command {
    START("/start"),
    LIST("/list"),
    HELP("/help"),
    BACK("back"),
    MENU_TICKETS("menu:tickets"),
    MENU_LECTURES("menu:lectures"),
    SUBJ("subj:"),
    FILE("file:"),
    RATE("rate:");

    private final String value;

    Command(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Command fromString(String text) {
        for (Command cmd : values()) {
            if (text.startsWith(cmd.value)) {
                return cmd;
            }
        }
        return null;
    }
}
