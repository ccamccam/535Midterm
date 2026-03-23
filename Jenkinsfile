pipeline {
    agent any

    environment {
        SONARQUBE_SERVER = 'http://sonar:9000'
        SONAR_PROJECT_KEY = 'java-app'
        DOCKER_IMAGE = 'ccamccam2/java-app:latest'
    }

    stages {
        stage('Checkout Pipeline Repo') {
            steps {
                checkout scm
            }
        }

        stage('Checkout App') {
            steps {
                dir('app') {
                    git branch: 'main', url: 'https://github.com/spring-guides/gs-spring-boot'
                }
            }
        }

        stage('Build') {
            steps {
                dir('app/complete') {
                    script {
                        docker.image('maven:3.9-eclipse-temurin-17').inside {
                            sh './mvnw clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Test') {
            steps {
                dir('app/complete') {
                    script {
                        docker.image('maven:3.9-eclipse-temurin-17').inside {
                            sh './mvnw test'
                        }
                    }
                }
            }
        }

        stage('Static Code Analysis') {
            steps {
                dir('app/complete') {
                    script {
                        docker.image('maven:3.9-eclipse-temurin-17').inside {
                            sh """
                            ./mvnw sonar:sonar \
                              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                              -Dsonar.host.url=${SONARQUBE_SERVER} \
                              -Dsonar.login=admin \
                              -Dsonar.password=Passadminword00
                            """
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('app/complete') {
                    script {
                        sh '''
                        cat > Dockerfile <<EOF
                        FROM eclipse-temurin:17-jre
                        WORKDIR /app
                        COPY target/*.jar app.jar
                        EXPOSE 8080
                        ENTRYPOINT ["java","-jar","app.jar"]
                        EOF
                        '''
                        sh "docker build -t ${DOCKER_IMAGE} ."
                    }
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    sh 'docker login -u ccamccam2 -p Password122'
                    sh "docker push ${DOCKER_IMAGE}"
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh """
                    sed -i 's|ccamccam2/java-app:latest|${DOCKER_IMAGE}|g' deployment.yaml
                    kubectl apply -f deployment.yaml
                    """
                }
            }
        }
    }
}
