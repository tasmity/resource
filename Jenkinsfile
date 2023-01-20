import groovy.transform.Field
import groovy.xml.MarkupBuilder
@Library('DPMPipelineUtils@1.4') _

def groovyModules = 'src/groovy-modules.groovy'
def configFileName = 'params/nt.properties'
def paramsAction = ""
def problem = ""

// parameters
properties([
    parameters([
        choice(
            choices: ['none', 'start', 'stop'],
            name: 'ACTION',
            description: 'Ручной запуск')
    ])
])


// Builds
pipeline {
    agent {
        node {
            label "Linux_Default"
        }
    }
    options {
        buildDiscarder(logRotator(daysToKeepStr: '15', numToKeepStr: '5', artifactDaysToKeepStr: '15', artifactNumToKeepStr: '5'))
        timestamps()
    }
    tools {
        oc 'oc-latest'
    }

    stages {
        stage ("Подключение модулей") {
            steps {
                script {
                    groovyModules = load groovyModules
                    groovyModules.helloModules()
                    load configFileName
                    groovyModules.helloConfig()
                }
            }
        }
        stage ("Получение параметра запуска") {
            steps {
                script {
                    paramsAction = groovyModules.getAction(params.ACTION);
                    sh "echo ${paramsAction}"
                }
            }
        }
        stage ("Загрузка с Git") {
            steps {
                script {
                    groovyModules.source()
                    sh "ls -la"
                }
            }
        }
        stage ("Запуск Ansible") {
            steps {
                script {
                    try {
                        groovyModules.startAnsiblePlaybook("load_serv.yml", paramsAction)
                    } catch (e) {
                        problem = "Работа ansible завершилась с ошибкой. Проветре доступност хостов (лог выполнения задачи приложен к письму)."
                        groovyModules.mailTo(problem)
                    }
                }
            }
        }
    }
    post {
        failure {
            script {
                echo("Build failure")
                problem = "Задача не выполнена!!! (лог выполнения задачи приложен к письму)."
                groovyModules.mailTo(problem)
            }
        }
        cleanup {
            cleanWs()
        }
    }
}
