def mvnHome
def remote = [ : ]
	remote.name = 'deploy'
	remote.host = '192.168.122.1'
	//remote.user = 'root'
	//remote.password = 'datta'
	remote.allowAnyHosts = true
pipeline
{
	agent none
	stages
	{
		stage ('Preparation') 	
		{
			agent
			{
				label 'master'
			}
			steps 
			{
				git 'https://github.com/ksreerao/Maven.git'
				stash 'Source'
				script 
				{
					mvnHome = tool 'LocalMaven'
				}
			}
		}
		stage ('build') 
		{
			agent 
			{
				label 'master'
			}
			steps
			{
				sh ' ${master}/bin/mvn clean package '
			}
			post
			{
				always
				{
					junit 'target/surefire-reports/*.xml'
					archiveArtifacts '**/*.war'
					fingerprint '**/*.war'
				}
			}
		}
		stage('Deploy-to-Stage') 
		{
			agent
			{
				label 'master'
			}
			steps 
			{
				sshPut remote: remote, from: 'target/java-maven-1.0.war', into: '/workspace/nexusServer/webapps'
			}
		}
		stage ('Integration-Test') 
		{
			agent
			{
				label 'master'
			}
			steps 
			{
				parallel (
					'integration' : {
						unstash 'Source'
						sh ' ${mvnHome}/bin/mvn clean verify '
						},	'quality' : {
								unstash 'Source'
								sh ' ${mvnHome}/bin/mvn clean test '
							}
				)
			}
		}
		stage ('approve')
		{
			agent 
			{
				label 'master'
			}
			steps 
			{
				timeout(time: 7, unit: 'DAYS')
				{
					input message: 'Do you want to deploy?' , submitter: 'admin'
				}
			}
		}
		stage ('Prod-Deploy') 
		{
			agent 
			{
				label 'master'
			}
			steps 
			{	
				unstash 'Source'
				sh ' ${mvnHome}/bin/mvn clean package '
			}
			post
			{
				always
				{
					archiveArtifacts '**/*.war'
				}
			}
		}
	}
}
