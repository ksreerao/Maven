def mvnHome
def remote1 = [:]
    	remote1.name = 'deploy'
    	remote1.host = '192.168.122.1'
    	//remote1.user = 'root'
    	//remote1.password = 'vagrant'
	remote1.allowAnyHosts = true
/*def remote2 = [:]
	remote2.name = 'deploy2'
	remote2.host = '192.168.56.65'
    	remote2.user = 'ansible'
    	remote2.password = 'welcome'
    	remote2.allowAnyHosts = true*/
pipeline {
    
	agent none
	
	stages {
		//def mvnHome
		stage ('Preparation') {
		   
		    steps {
			    git 'https://github.com/ksreerao/Maven.git'
			    stash 'Source'
			    script{
			        mvnHome = tool 'LocalMaven'
			    }
		    }
		}
		stage ('Static Analysis'){
	
			steps {
				sh "'${mvnHome}/bin/mvn' clean cobertura:cobertura"			
			}
			post {
                success {
                    cobertura autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: 'target/site/cobertura/coverage.xml', conditionalCoverageTargets: '70, 0, 0', failUnhealthy: false, failUnstable: false, lineCoverageTargets: '80, 0, 0', maxNumberOfBuilds: 0, methodCoverageTargets: '80, 0, 0', onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: false
                }
            }
		}
		stage ('build'){
		
			steps {
				sh "'${mvnHome}/bin/mvn' clean package"			
			}
			post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts '**/*.war'
                    fingerprint '**/*.war'
                }
            }
		}
		stage('Deploy-to-Stage') {
		    
		    //SSH-Steps-Plugin should be installed
		    //SCP-Publisher Plugin (Optional)
		    steps {
		        //sshScript remote: remote, script: "abc.sh"  	
			sshPut remote: remote1, from: 'target/java-maven-1.0.war', into: '/home/admin/workspace/nexus/Server/webapps'

		    }
    	}
    	stage ('Integration-Test') {
	
			steps {
				parallel (
					'integration': { 
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
	
			steps {
				timeout(time: 7, unit: 'DAYS') {
					input message: 'Do you want to deploy?', submitter: 'admin'
				}
			}
		}
		stage ('Prod-Deploy') {
	
			steps {
				unstash 'Source'
				sh "'${mvnHome}/bin/mvn' clean package"				
			        
			      }
			post {
				always {
					archiveArtifacts '**/*.war'
				}
	             
			}
		}
    	
	}	
}
