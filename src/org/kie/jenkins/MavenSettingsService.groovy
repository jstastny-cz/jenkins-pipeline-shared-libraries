package org.kie.jenkins

class MavenSettingsService {

    def steps

    MavenSettingsConfig mavenSettingsConfig

    MavenSettingsService(def steps) {
        this(steps, new MavenSettingsConfig(steps))
    }

    MavenSettingsService(def steps, MavenSettingsConfig mavenSettingsConfig) {
        this.steps = steps
        this.mavenSettingsConfig = mavenSettingsConfig
    }

    String createSettingsFile() {
        String settingsFile = this.mavenSettingsConfig.settingsXmlPath
        if (this.mavenSettingsConfig.settingsXmlConfigFileId) {
            String settingsFilePath = steps.sh(returnStdout: true, script: 'mktemp --suffix -settings.xml').trim()
            steps.sh(returnStdout:true, script: "ls -l ${settingsFilePath}")
            steps.configFileProvider([steps.configFile(fileId: this.mavenSettingsConfig.settingsXmlConfigFileId, targetLocation: settingsFilePath, variable: 'MAVEN_SETTINGS_XML')]) {
                settingsFile = steps.env.'MAVEN_SETTINGS_XML'
            }
            steps.sh(returnStdout:true, script: "ls -l ${settingsFile}")
        }
        if (settingsFile) {
            this.mavenSettingsConfig.dependenciesRepositoriesInSettings.each { MavenSettingsUtils.setRepositoryInSettings(steps, settingsFile, it.key, it.value) }

            this.mavenSettingsConfig.disabledMirrorRepoInSettings.each {
                MavenSettingsUtils.disableMirrorForRepoInSettings(steps, settingsFile, it)
            }

            if (this.mavenSettingsConfig.disableSnapshotsInSettings) {
                MavenSettingsUtils.disableSnapshotsInSettings(steps, settingsFile)
            }

            this.mavenSettingsConfig.servers.each { MavenSettingsUtils.addServer(steps, settingsFile, it.id, it.username, it.password) }
            return settingsFile
        } else {
            return ''
        }
    }

}
