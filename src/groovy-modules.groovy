def helloModules() {
    println("(\"===========================\")\n" +
            "Modules connected successfully\n" +
            "(\"============================\")\n")
}

def helloConfig() {
    println("(\"=========================\")\n" +
            "Config connected successfully\n" +
            "(\"==========================\")")
}

def source() {
    git credentialsId: 'bitbucket_sbt_ssh', url: 'ssh://git@stash.sigma.sbrf.ru:7999/caas_devops/resource_opt.git', branch: 'master'
    println("Download Git Completed!")
}

def getAction(String paramAction) {
    hours = "" + new Date().format("ddMMyyHHmm")
    hours = hours.substring(6, 8)
    println("Start time in hours: " + hours)

    try {
        if (paramAction == null) {
            paramAction = "none"
        }
    } catch (e) {
        println(e)
    }

    if (paramAction.equals("none")) {
        paramAction = hours.equals("19") ? "start" : "stop"
        println("Automatic start or start without parameters! Parameter is set to " + paramAction.toUpperCase())
    }
    println("Launch option ACTION = " + paramAction)
    return paramAction
}

def startAnsiblePlaybook(String playbook, String paramsAction) {
    println("MODULE - startAnsiblePlaybook Vars -- START")
    println("ansibleVaultKey = " + ansibleVaultKey)
    println("ansibleInstallation = " + ansibleInstallation)
    println("playbook = " + playbook)
    println("ACTION = " + paramsAction)


    dir("./ansible") {
        ansiColor('xterm') {
            withCredentials([file(credentialsId: ansibleVaultKey, variable: 'VAULT_TOKEN')]) {
                println("VAULT_TOKEN = " + VAULT_TOKEN)
                ansiblePlaybook(
                        installation: "${ansibleInstallation}",
                        inventory: "hosts",
                        playbook: playbook,
                        colorized: true,
                        tags: "${paramsAction}",
                        credentialsId: 'caas_devops',
                        extras: " --vault-password-file ${VAULT_TOKEN} --ssh-common-args='-o StrictHostKeyChecking=no' ")
            }
        }
    }
    println("MODULE - startAnsiblePlaybook Vars -- END")
}

def mailTo(String problem) {
    try {
        emailext attachLog: true,
                body: "Задача по нагрузке resource_opt_test - " +
                        "Статус: ${currentBuild.currentResult == null ? "INTERRUPTED" : currentBuild.currentResult}. " +
                        problem,
                compressLog: true,
                mimeType: 'text/html',
                subject: "Задача ${currentBuild.fullDisplayName} - " +
                        "Статус: ${currentBuild.currentResult == null ? "INTERRUPTED" : currentBuild.currentResult}. ",
                to: 'TIgoIsrapilova@sberbank.ru'
    } catch (e) {
        println("Не удалось отправить почту - " + e)
    }
}

return this
