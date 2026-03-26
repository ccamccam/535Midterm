FROM jenkins/jenkins:lts

USER root

# Install tools
RUN apt-get update && apt-get install -y \
    curl \
    ca-certificates \
    gnupg \
    lsb-release \
    apt-transport-https \
    docker.io \
    && curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" \
    && install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl \
    && rm -f kubectl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ARG DOCKER_GID=136
RUN groupadd -g $DOCKER_GID docker || true
RUN usermod -aG docker jenkins

USER jenkins
