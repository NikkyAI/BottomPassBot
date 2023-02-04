
rootProject.name = "BottomPassBot"

plugins {
    id("de.fayard.refreshVersions") version "0.51.0"
}

refreshVersions {
    extraArtifactVersionKeyRules(file("version_key_rules.txt"))
}
