def call(String imageName, String imageTag) {

    stage('Build Image') {
        dir('DemoApp') {
            sh "docker build -t ${imageName}:${imageTag} ."
        }
    }

    stage('Scan Image') {
        sh "trivy image ${imageName}:${imageTag}"
    }

    stage('Push Image') {
        sh "docker push ${imageName}:${imageTag}"
    }

    stage('Delete Local Image') {
        sh "docker rmi ${imageName}:${imageTag}"
    }
}
