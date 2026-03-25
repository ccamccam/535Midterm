pipeline {
    agent any

    environment {
        SONARQUBE_SERVER = 'http://172.18.0.3:9000'
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
                        sh 'until curl -sf http://172.18.0.3:9000 > /dev/null; do echo "Waiting for SonarQube..."; sleep 10; done'
                        docker.image('maven:3.9-eclipse-temurin-17').inside('--network ci_network') {
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
                        echo "FROM eclipse-temurin:17-jre" > Dockerfile
                        echo "WORKDIR /app" >> Dockerfile
                        echo "COPY target/*.jar app.jar" >> Dockerfile
                        echo "EXPOSE 8080" >> Dockerfile
                        echo 'ENTRYPOINT ["java","-jar","app.jar"]' >> Dockerfile
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
                    sh "sed -i 's|ccamccam2/java-app:latest|${DOCKER_IMAGE}|g' deployment.yaml"
                    sh """
                    # Download kubectl
                    curl -LO "https://dl.k8s.io/release/v1.28.0/bin/linux/amd64/kubectl"
                    chmod +x kubectl
                    
                    # Extract the token directly from the config file
                    # This avoids all certificate path issues
                    USER_TOKEN=\$(grep "token:" /var/jenkins_home/.kube/config | awk '{print \$2}' | head -n 1 )
                    
                    # Run apply using the token and the Gateway IP we just verified
                    ./kubectl apply -f deployment.yaml \
                      --server=https://172.18.0.1:8443 \
                      --token="\$USER_TOKEN" \
                      --insecure-skip-tls-verify=true \
                      --validate=false
                    """
                }
            }
        }
    }
}
