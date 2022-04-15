def mvnHome 



pipeline {
    agent none
    stage {
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

    }
}

