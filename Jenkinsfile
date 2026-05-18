pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'tapesh134'
        DOCKER_CREDS = credentials('dockerhub-creds')
        SONAR_TOKEN = credentials('sonar_token')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    echo "Running SonarQube Analysis..."

                    sh """
                    mvn clean verify sonar:sonar \
                    -Dsonar.projectKey=job-portal \
                    -Dsonar.projectName=job-portal \
                    -Dsonar.host.url=http://host.docker.internal:9000 \
                    -Dsonar.login=${SONAR_TOKEN}
                    """
                }
            }
        }

        stage('Build & Push Services') {

            parallel {

                stage('Infrastructure & Gateway') {
                    steps {
                        script {
                            buildAndPush('Eureka-Server', 'eureka-server')
                            buildAndPush('Api-Gateway', 'api-gateway')
                        }
                    }
                }

                stage('Core Services 1') {
                    steps {
                        script {
                            buildAndPush('Auth-Service', 'auth-service')
                            buildAndPush('Profile-Service', 'profile-service')
                            buildAndPush('Job-Service', 'job-service')
                        }
                    }
                }

                stage('Core Services 2') {
                    steps {
                        script {
                            buildAndPush('Application-Service', 'application-service')
                            buildAndPush('Interview-Service', 'interview-service')
                            buildAndPush('Subscription-Service', 'subscription-service')
                        }
                    }
                }

                stage('Support & Web') {
                    steps {
                        script {
                            buildAndPush('Notification-Service', 'notification-service')
                            buildAndPush('Analytics-Service', 'analytics-service')
                            buildAndPush('JobPortal-Web', 'jobportal-web')
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
            cleanWs()
        }

        success {
            echo 'Pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed!'
        }
    }
}

def buildAndPush(String folderName, String imageName) {

    echo "================================================="
    echo "Building and Pushing: ${imageName}"
    echo "================================================="

    dir("${folderName}") {

        sh '''
            mvn clean package -DskipTests
        '''

        sh '''
            echo "$DOCKER_CREDS_PSW" | docker login -u "$DOCKER_CREDS_USR" --password-stdin
        '''

        sh """
            docker build -t ${DOCKERHUB_USERNAME}/${imageName}:latest .
        """

        sh """
            docker push ${DOCKERHUB_USERNAME}/${imageName}:latest
        """

        sh """
            docker rmi ${DOCKERHUB_USERNAME}/${imageName}:latest || true
        """
    }
}