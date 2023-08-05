package experiment.enums;

public enum ArtifactLevelEnum {
    LEVEL1, LEVEL2, LEVEL3, LEVEL4;
    public static ArtifactLevelEnum getType(String name) {
        switch (name) {
            case "level1":
                return LEVEL1;
            case "level2":
                return LEVEL2;
            case "level3":
                return LEVEL3;
            default:
                return null;
        }
    }
}
