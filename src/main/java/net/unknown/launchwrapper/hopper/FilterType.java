package net.unknown.launchwrapper.hopper;

public enum FilterType {
    DISABLED("無効"),
    WHITELIST("ホワイトリスト"),
    BLACKLIST("ブラックリスト");

    private final String localizedName;

    FilterType(String localizedName) {
        this.localizedName = localizedName;
    }

    public String getLocalizedName() {
        return this.localizedName;
    }
}
