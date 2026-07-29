package me.ddggdd135.guguslimefunlib.libraries.version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Bukkit;

public enum Version {
    v1_16_R1,
    v1_16_R2,
    v1_16_R3,
    v1_17_R1,
    v1_18_R1,
    v1_18_R2,
    v1_19_R1,
    v1_19_R2,
    v1_19_R3,
    v1_20_R1,
    v1_20_R2,
    v1_20_R3(4),
    v1_20_R4(5),
    v1_21_R1,
    v1_21_R2,
    v1_21_R3,
    v1_21_R4(5),
    v1_21_R5(6, 7),
    v1_21_R6(8),
    v1_21_R7(9),
    v1_21_R8(10, 11),
    v1_22_R1,
    v1_22_R2,
    v1_22_R3,
    v1_23_R1,
    v1_23_R2,
    v1_23_R3,
    UNKNOWN;

    @Getter
    private Integer value;

    private int[] minorVersions = null;

    @Getter
    private final String shortVersion;

    private static int subVersion = 0;
    private static Version current = null;
    private static Integer fallbackCurrentValue = null;
    private static MinecraftPlatform platform = null;

    static {
        getCurrent();
    }

    Version(int... versions) {
        this();
        minorVersions = versions;
    }

    Version() {
        try {
            this.value = Integer.valueOf(this.name().replaceAll("[^\\d.]", ""));
        } catch (Exception e) {
        }
        shortVersion = this.name().substring(0, this.name().length() - 3);
    }

    public static boolean isPaperBranch() {
        switch (getPlatform()) {
            case mohist:
                break;
            case purpur:
            case folia:
            case paper:
            case pufferfish:
                return true;
        }
        return false;
    }

    public String getShortFormated() {
        if (this == UNKNOWN) {
            return deconvertVersion(getCurrentVersionValue()) + ".x";
        }
        return shortVersion.replace("v", "").replace("_", ".") + ".x";
    }

    public String getFormated() {
        if (this == UNKNOWN) {
            return deconvertVersion(getCurrentVersionValue());
        }
        return shortVersion.replace("v", "").replace("_", ".") + "." + subVersion;
    }

    public static boolean isPaper() {
        return getPlatform().equals(MinecraftPlatform.paper)
                || getPlatform().equals(MinecraftPlatform.folia)
                || getPlatform().equals(MinecraftPlatform.purpur);
    }

    public static boolean isFolia() {
        return getPlatform().equals(MinecraftPlatform.folia);
    }

    public static boolean isPurpur() {
        return getPlatform().equals(MinecraftPlatform.purpur);
    }

    public static MinecraftPlatform getPlatform() {
        if (platform != null) return platform;

        if (Bukkit.getVersion().toLowerCase().contains("mohist")) {
            platform = MinecraftPlatform.mohist;
            return platform;
        }

        if (Bukkit.getVersion().toLowerCase().contains("arclight")) {
            platform = MinecraftPlatform.arclight;
            return platform;
        }

        if (Bukkit.getVersion().toLowerCase().contains("purpur")) {
            platform = MinecraftPlatform.purpur;
            return platform;
        }

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            platform = MinecraftPlatform.folia;
            return platform;
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            platform = MinecraftPlatform.paper;
            return platform;
        } catch (ClassNotFoundException e) {
        }

        return platform;
    }

    public static Version getCurrent() {
        if (current != null) return current;
        String[] v = Bukkit.getServer().getClass().getPackage().getName().split("\\.");

        try {
            String vr = Bukkit.getBukkitVersion().split("-", 2)[0];
            String[] split = vr.split("\\.");
            if (split.length <= 2) subVersion = 0;
            else {
                subVersion = Integer.parseInt(split[2]);
            }
        } catch (Throwable e) {
        }

        String vv = v[v.length - 1];
        for (Version one : values()) {
            if (one.name().equalsIgnoreCase(vv)) {
                current = one;
                break;
            }
        }

        if (current == null) {
            String ve = Bukkit.getBukkitVersion().split("-", 2)[0];
            main:
            for (Version one : values()) {
                if (one.name().equalsIgnoreCase(ve)) {
                    current = one;
                    break;
                }
                List<String> cleanVersion = one.getMinorVersions();
                for (String cv : cleanVersion) {
                    if (ve.equalsIgnoreCase(cv)) {
                        current = one;
                        break main;
                    }
                }
            }
        }

        if (current == null) {
            String ve = Bukkit.getBukkitVersion().split("-", 2)[0];
            for (Version one : values()) {
                if (ve.startsWith(one.getSimplifiedVersion())) {
                    current = one;
                    Bukkit.getConsoleSender()
                            .sendMessage("§c[RykenSlimeCustomizer] §eServer version detection needs aditional update");
                    break;
                }
            }
        }

        // Fallback: if no enum entry matches, compute a comparable value from the Bukkit version
        // string directly and use the UNKNOWN sentinel. This handles new versioning schemes
        // (e.g. "26.2") that don't have corresponding enum entries yet.
        if (current == null) {
            fallbackCurrentValue = computeValueFromBukkitVersion();
            if (fallbackCurrentValue != null) {
                current = UNKNOWN;
                UNKNOWN.value = fallbackCurrentValue;
                Bukkit.getConsoleSender()
                        .sendMessage("§c[GuguSlimefunLib] §eUnknown server version: "
                                + Bukkit.getBukkitVersion()
                                + ". Using fallback comparison — plugin may need an update.");
            }
        }

        return current;
    }

    /**
     * Computes a comparable integer value from the raw Bukkit version string.
     * Returns null if parsing fails.
     */
    private static Integer computeValueFromBukkitVersion() {
        try {
            String v = Bukkit.getBukkitVersion().split("-", 2)[0];
            return convertVersion(v);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the comparable version value for the current server.
     * When {@link #getCurrent()} returns null (unknown version), this falls back to a value
     * computed directly from {@code Bukkit.getBukkitVersion()}, ensuring comparisons never NPE.
     */
    private static int getCurrentVersionValue() {
        if (current != null) {
            Integer v = current.getValue();
            return v != null ? v : 0;
        }
        if (fallbackCurrentValue != null) {
            return fallbackCurrentValue;
        }
        fallbackCurrentValue = computeValueFromBukkitVersion();
        return fallbackCurrentValue != null ? fallbackCurrentValue : 0;
    }

    public boolean isLower(Version version) {
        Integer selfValue = getValue();
        Integer otherValue = version.getValue();
        if (selfValue == null || otherValue == null) return false;
        return selfValue < otherValue;
    }

    public boolean isHigher(Version version) {
        Integer selfValue = getValue();
        Integer otherValue = version.getValue();
        if (selfValue == null || otherValue == null) return false;
        return selfValue > otherValue;
    }

    public boolean isEqualOrLower(Version version) {
        Integer selfValue = getValue();
        Integer otherValue = version.getValue();
        if (selfValue == null || otherValue == null) return false;
        return selfValue <= otherValue;
    }

    public boolean isEqualOrHigher(Version version) {
        Integer selfValue = getValue();
        Integer otherValue = version.getValue();
        if (selfValue == null || otherValue == null) return false;
        return selfValue >= otherValue;
    }

    public static boolean isCurrentEqualOrHigher(Version v) {
        return getCurrentVersionValue() >= v.getValue();
    }

    public static boolean isCurrentHigher(Version v) {
        return getCurrentVersionValue() > v.getValue();
    }

    public static boolean isCurrentLower(Version v) {
        return getCurrentVersionValue() < v.getValue();
    }

    public static boolean isCurrentEqualOrLower(Version v) {
        return getCurrentVersionValue() <= v.getValue();
    }

    public static boolean isCurrentEqual(Version v) {
        Integer targetValue = v.getValue();
        if (targetValue == null) return false;
        return getCurrentVersionValue() == targetValue;
    }

    public static boolean isCurrentSubEqualOrHigher(int subVersion) {
        return Version.subVersion >= subVersion;
    }

    public static boolean isCurrentSubHigher(int subVersion) {
        return Version.subVersion > subVersion;
    }

    public static boolean isCurrentSubLower(int subVersion) {
        return Version.subVersion < subVersion;
    }

    public static boolean isCurrentSubEqualOrLower(int subVersion) {
        return Version.subVersion <= subVersion;
    }

    public static boolean isCurrentSubEqual(int subVersion) {
        return Version.subVersion == subVersion;
    }

    /**
     * Converts a Minecraft version string to a comparable integer.
     *
     * <p>For old-scheme versions ({@code "1.X.Y"}), segments are padded to 2 digits and
     * concatenated — e.g. {@code "1.21.11"} → {@code 12111}.
     *
     * <p>For new-scheme versions ({@code "YY.R"} where YY ≥ 26), a compound value is
     * computed that is guaranteed to be higher than any old-scheme value — e.g.
     * {@code "26.2"} → {@code 260200}.
     */
    public static Integer convertVersion(String v) {
        v = v.replaceAll("[^\\d.]", "");
        if (!v.contains(".")) {
            try {
                return Integer.parseInt(v);
            } catch (Exception e) {
                return 0;
            }
        }

        String[] parts = v.split("\\.");
        if (parts.length < 2) {
            try {
                return Integer.parseInt(v);
            } catch (Exception e) {
                return 0;
            }
        }

        int first;
        try {
            first = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return 0;
        }

        if (first == 1) {
            // Old Minecraft versioning: "1.X.Y"
            // Preserve original behavior — pad ALL segments (including "1") to 2 digits.
            StringBuilder lVersion = new StringBuilder();
            for (String one : parts) {
                String s = one;
                if (s.length() == 1) s = "0" + s;
                lVersion.append(s);
            }

            try {
                return Integer.parseInt(lVersion.toString());
            } catch (Exception e) {
                return 0;
            }
        } else {
            // New Minecraft versioning (≥ 2026): "YY.R[.H]"
            // Compute year*10000 + drop*100 + hotfix, which is always > any old-scheme value.
            // Non-numeric or empty segments (e.g. "build" in Paper's "26.2.build.84") are
            // safely skipped — only the first successfully parsed value in each position counts.
            int year = first;
            int drop = 0;
            int hotfix = 0;
            for (int i = 1; i < parts.length && i <= 2; i++) {
                String part = parts[i];
                if (part.isEmpty()) continue;
                try {
                    int val = Integer.parseInt(part);
                    if (i == 1) drop = val;
                    else hotfix = val;
                } catch (NumberFormatException ignored) {
                    // Non-numeric segment (e.g. "build") — stop parsing further
                    break;
                }
            }
            return year * 10000 + drop * 100 + hotfix;
        }
    }

    public static String deconvertVersion(Integer v) {

        StringBuilder version = new StringBuilder();

        String vs = String.valueOf(v);

        while (!vs.isEmpty()) {
            int subv;
            try {
                if (vs.length() > 2) {
                    subv = Integer.parseInt(vs.substring(vs.length() - 2));
                    version.insert(0, "." + subv);
                } else {
                    subv = Integer.parseInt(vs);
                    version.insert(0, subv);
                }
            } catch (Throwable ignored) {
            }

            if (vs.length() > 2) vs = vs.substring(0, vs.length() - 2);
            else break;
        }

        return version.toString();
    }

    private String getSimplifiedVersion() {
        return this.name().substring(1).replace("_", ".").split("R", 2)[0];
    }

    public List<String> getMinorVersions() {

        if (minorVersions == null) return new ArrayList<>();

        return Arrays.stream(minorVersions)
                .mapToObj(version -> getSimplifiedVersion() + version)
                .collect(Collectors.toList());
    }
}
