package org.finos.legend.engine.plan.execution.stores.spark.version;

public class SparkVersion implements Comparable<SparkVersion> {

    private int majorVersion;
    private int minorVersion;
    private int patchVersion;

    public SparkVersion(final String version) {
        String[] versionSplit = version.split("\\.");

        this.majorVersion = Integer.parseInt(versionSplit[0]);
        this.minorVersion = Integer.parseInt(versionSplit[1]);
        this.patchVersion = Integer.parseInt(versionSplit[2]);
    }

    public SparkVersion(int majorVersion, int minorVersion, int patchVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public int getPatchVersion() {
        return patchVersion;
    }

    public void setPatchVersion(int patchVersion) {
        this.patchVersion = patchVersion;
    }

    @Override
    public int compareTo(SparkVersion other) {
        if (majorVersion < other.getMajorVersion()) {
            return -1;
        } else if (majorVersion > other.getMajorVersion()) {
            return 1;
        } else {
            if (minorVersion < other.getMinorVersion()) {
                return -1;
            } else if (minorVersion > other.getMinorVersion()) {
                return 1;
            } else {
                return Integer.compare(patchVersion, other.getPatchVersion());
            }
        }
    }
}