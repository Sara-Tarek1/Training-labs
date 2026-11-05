def call(String imageName, String imageTag) {

    stage('Build Image') {
        dir('DemoApp') {
            sh "docker build -t ${imageName}:${imageTag} ."
        }
    }

    stage('Scan Image') {
        sh "trivy --timeout 15m image ${imageName}:${imageTag}"
    }

    stage('Push Image') {
        withCredentials([
            usernamePassword(
                credentialsId: 'dockerhub-credentials',
                usernameVariable: 'DOCKERHUB_USERNAME',
                passwordVariable: 'DOCKERHUB_PASSWORD'
            )
        ]) {
            sh """
                printf '%s' "\$DOCKERHUB_PASSWORD" | docker login -u "\$DOCKERHUB_USERNAME" --password-stdin
                docker push ${imageName}:${imageTag}
                docker logout
            """
        }
    }

    stage('Delete Local Image') {
        sh "docker rmi ${imageName}:${imageTag}"
    }
}

