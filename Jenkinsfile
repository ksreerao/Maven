def mvnHome 
def remote = [:]
    remote.name = 'deploy'
    remote.host = '192.168.56.10'
    remote.user = 'root'
    remote.password = 'vagrant'
    remote.allowAnyHosts = true

pipeline {
    agent none
    stages {
        // def mvnHome
        stage ('Preparation') {
            agent {
                label 'GroupA'
            }
            steps {
                git branch: 'main', url: 'https://github.com/ksreerao/Maven.git'
                stash 'Source'
                script{
                    mvnHome = tool 'LocalMaven'
                }
            }
        }
        stage ('build'){
            agent {
                label 'GroupA'
            }
            steps {
                sh "'${mvnHome}/bin/mvn' clean package"
            }
            post {
                always {
                    junit '**target/surefire-reports/*.xml'
                    archiveArtifacts '**/*.war'
                    fingerprint '**/*.war'
                }
            }
        }
        stage ('Deploy-to-Stage'){
            agent {
                label 'GroupA'
            }
            // SSH-Steps-Plugin should be installed
            //SCP-Publisher Plugin (Optional)
            steps {
                //ssScript remote: remote. script: "abc.sh"
                sshPut remote: remote, from: 'target/java-maven-1.0.war', into: '/root/workspace/appServer/webapps'
            }
        }
        stage ('Integration-Test') {
            agent {
                label 'GroupA'
            }
            steps {
                parallel (
                    'intergration' : {
                        unstash 'Source'
                        sh "'${mvnHome}/bin/mvn' clean verify"
                    }, 'quality': {
                        unstash 'Source'
                        sh "'${mvnHome}/bin/mvn' clean test"
                    }
                )
            }
        }
        stage ('approve') {
            agent {
                label 'GroupA'
            }
            steps {
                timeout(time: 7, unit: 'DAYS') {
                    // some block
                    input message: 'Do you want to deploy?', submitter: 'admin'
                }
            }
        }
        stage ('Prod-Deploy') {
            agent {
                label 'GroupA'
            }
            steps {
                unstash 'Source'
                sh "'${mvnHome}/bin/mvn' clean package"
            }
            post {
                always {
                    archiveArtifacts '**/*.war'
                    fingerprint '**/*.war'
                }
            }
        }
    }
}

