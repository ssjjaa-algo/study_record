# pipeline

```yaml
pipeline {
    agent any

    environment {
        imagename = "docker build로 만들 이미지 이름"
        registryCredential = 'Docker Hub Credential ID'
        dockerImage = ''
    }

    stages {
        stage('Prepare') {
          steps {
            echo 'Clonning Repository'
            git url: 'Github Repository SSH Url(git@github.com로 시작)',
              branch: 'Clone 받아올 Branch 이름',
              credentialsId: 'Github Credential ID -> github'
            }
            post {
             success { 
               echo 'Successfully Cloned Repository'
             }
           	 failure {
               error 'This pipeline stops here...'
             }
          }
        }

        stage('Bulid Gradle') {
          steps {
            echo 'Bulid Gradle'
            dir('.'){
                sh './gradlew clean build'
            }
          }
          post {
            failure {
              error 'This pipeline stops here...'
            }
          }
        }
        
        stage('Bulid Docker') {
          steps {
            echo 'Bulid Docker'
            script {
                dockerImage = docker.build imagename
            }
          }
          post {
            failure {
              error 'This pipeline stops here...'
            }
          }
        }

        stage('Push Docker') {
          steps {
            echo 'Push Docker'
            script {
                docker.withRegistry( '', registryCredential) {
                    dockerImage.push() 
                }
            }
          }
          post {
            failure {
              error 'This pipeline stops here...'
            }
          }
        }
        
        stage('Docker Run') {
            steps {
                echo 'Pull Docker Image & Docker Image Run'
                sshagent (credentials: ['SSH Credential ID -> ssh']) {
                    sh "ssh -o StrictHostKeyChecking=no [Spring Boot Server username]@[Spring Boot Server IP 주소] 'docker pull [도커이미지 이름]'" 
                    sh "ssh -o StrictHostKeyChecking=no [Spring Boot Server username]@[Spring Boot Server IP 주소] 'docker ps -q --filter name=[컨테이너 이름] | grep -q . && docker rm -f \$(docker ps -aq --filter name=[컨테이너 이름])'"
                    sh "ssh -o StrictHostKeyChecking=no [Spring Boot Server username]@[Spring Boot Server IP 주소] 'docker run -d --name [컨테이너 이름] -p 8080:8080 [도커이미지 이름]'"
                }
            }
        }
    }
    post {
        success {
            slackSend (channel: '#채널명', color: '#00FF00', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
        failure {
            slackSend (channel: '#채널명', color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
    }
	}
```
