pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'ccamccam2/java-app:latest'
        SONAR_HOST_URL = 'http://localhost:9000'
    }

    stages {

        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build (Java 17)') {
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-17'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test (Java 11)') {
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-11'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis (Java 11)') {
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-11'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps {
                sh '''
                mvn sonar:sonar \
                  -Dsonar.projectKey=java-app \
                  -Dsonar.host.url=http://localhost:9000 \
                  -Dsonar.login=admin \
                  -Dsonar.password=admin1
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE .'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                    echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                    docker push $DOCKER_IMAGE
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                export KUBECONFIG=/var/jenkins_home/kubeconfig
                kubectl apply -f deployment.yaml
                '''
            }
        }
    }
}
