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
                expression { env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'main' }
            }
            environment {
                // Expoe DOCKERHUB_USR e DOCKERHUB_PSW, que o jib-maven-plugin le no pom.xml.
                DOCKERHUB = credentials('dockerhub')
            }
            steps {
                // A tag com o numero do build permite voltar a uma versao especifica; "latest"
                // acompanha sempre a main.
                bat 'mvn -B -DskipTests compile jib:build -Djib.to.tags=%BUILD_NUMBER%,latest'
            }
        }
    }
}
