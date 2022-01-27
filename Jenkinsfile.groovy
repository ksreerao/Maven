{\rtf1\ansi\ansicpg1252\cocoartf2636
\cocoatextscaling0\cocoaplatform0{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww11520\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 def mvnHome\
def remote = [ : ]\
	remote.name = \'91deploy\'92\
	remote.host = \'92192.168.122.1\'92\
	//remote.user = \'91root\'92\
	//remote.password = \'91datta\'92\
	remote.allowAnyHosts = true\
pipeline\
\{\
	agent none\
	stages\
	\{\
		stage (\'91Preparation\'92) 	\
		\{\
			agent \
			\{\
				label \'91master\'92	\
			\}\
			steps \
			\{\
				git \'91https://github.com/ksreerao/Maven.git'	\
				stash \'92Source\'92\
				script \
				\{\
					mvnHome = tool \'91LocalMaven\'92\
				\}\
			\}\
		\}\
		stage (\'91build\'92) \
		\{\
			agent \
			\{\
				label \'91master\'92\
\
			\}\
			steps\
			\{\
				sh \'91 $\{master\}/bin/mvn clean package \'92\
			\}\
			post\
			\{\
				always\
				\{\
					junit \'91target/surefire-reports/*.xml\'92\
					archiveArtifacts \'91**/*.war\'92\
					fingerprint \'91**/*.war\'92\
				\}\
			\}\
		\}\
		stage(\'91Deploy-to-Stage\'92) \
		\{\
			agent\
			\{\
				label \'91master\'92\
			\}\
			steps \
			\{\
				sshPut remote: remote, from: \'91target/java-maven-1.0.war\'92, into: \'91/workspace/nexusServer/webapps\'92\
			\}\
		\}\
		stage (\'91Integration-Test\'92) \
		\{\
			agent\
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0
\cf0 			\{\
				label \'91master\'92\
			\}\
			steps \
			\{\
				parallel (\
					\'91integration\'92 : \{\
						unstash \'91Source\'92\
						sh \'91 $\{mvnHome\}/bin/mvn clean verify \'91\
						\},	\'91quality\'92 : \{\
								unstash \'91Source\'92\
								sh \'91 $\{mvnHome\}/bin/mvn clean test \'91\
							\}\
				)\
			\}\
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0
\cf0 		\}\
		stage (\'91approve\'92)\
		\{\
			agent \
			\{\
				label \'91master\'92\
			\}\
			steps \
			\{\
				timeout(time: 7, unit: \'91DAYS\'92 )\
				\{\
					input message: \'91Do you want to deploy?\'92 , submitter: \'91admin\'92\
				\}\
			\}\
		\}\
		stage (\'91Prod-Deploy\'92) \
		\{\
			agent \
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0
\cf0 			\{\
				label \'91master\'92\
			\}\
			steps \
			\{	\
				unstash \'92Source\'92\
				sh \'91 $\{mvnHome\}/bin/mvn clean package \'91\
			\}\
			post\
			\{\
				always\
				\{\
					archiveArtifacts \'91**/*.war\'92\
				\}\
			\}\
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0
\cf0 		\}\
	\}\
\}\
}