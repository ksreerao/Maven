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
				git branch: 'main', url: 'https://github.com/ksreerao/Maven.git'
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
				sh "'${mvnHome}/bin/mvn' clean package"
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
				deploy adapters: [tomcat8(credentialsId: '11b0eb45-0da9-46cd-9c4d-016c43f2ef3d', path: '', url: 'http://192.168.122.1:8090')], contextPath: null, war: '**/*.war'			}
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
						sh "'${mvnHome}/bin/mvn' clean verify "
						},	'quality' : {
								unstash 'Source'
								sh "'${mvnHome}/bin/mvn' clean test "
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
				sh "'${mvnHome}/bin/mvn' clean package "
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
