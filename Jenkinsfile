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
                bat 'mvn test jacoco:report'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    
                    jacoco(
                        execPattern: 'target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java'
                    )
                }
            }
        }

       stage('Deploy') {
           when {
               beforeAgent true
               expression { env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'main' }
           }
           environment {
               DOCKERHUB = credentials('dockerhub')
               RAILWAY_TOKEN = credentials('railway-token')
           }
           steps {
               bat 'mvn -B -DskipTests compile jib:build -Djib.to.tags=%BUILD_NUMBER%,latest'
               bat 'railway redeploy --service c14-rateio --yes'
           }
       }
    }
}
