pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven'
    }

    stages {
        stage('Build') {
            steps {
                bat 'mvn -B -DskipTests clean package'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Deploy') {
            when {
                beforeAgent true
                branch 'main'
            }
            environment {
                DEPLOY_DIR = 'C:\\deploy\\rateio-backend'
            }
            steps {
                bat '''
                    if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
                    copy /Y target\\*.jar "%DEPLOY_DIR%"
                '''
            }
        }
    }
}
