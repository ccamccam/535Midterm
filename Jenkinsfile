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
                    def jenkinsId = "63df38ec29f6081f577d682a542325ca1d6b8589033eca11156c31bd8a1e2402"

                    // 3. Start Alpine using the HOST network to reach the cluster
                    docker.image('alpine:latest').withRun("--network host --volumes-from ${jenkinsId}", "tail -f /dev/null") { c ->
                        sh """
                        # Install kubectl
                        docker exec -u 0 ${c.id} sh -c 'apk add --no-cache curl && curl -LO "https://dl.k8s.io/release/v1.28.0/bin/linux/amd64/kubectl" && chmod +x kubectl && mv kubectl /usr/local/bin/'
                        
                        # Apply the configuration using the host's localhost address (standard for Minikube on host )
                        cat deployment.yaml | docker exec -i ${c.id} sh -c 'export KUBECONFIG=/var/jenkins_home/.kube/config && kubectl apply -f - --server=https://127.0.0.1:8443 --insecure-skip-tls-verify=true --validate=false'
                        """
                    }
                }
            }
        }
    }
}
