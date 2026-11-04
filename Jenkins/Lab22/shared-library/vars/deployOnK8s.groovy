def call(String imageName, String imageTag) {

    stage('Deploy on Kubernetes') {

        sh """
            sed -i 's|image: saratarek1/demoapp:latest|image: ${imageName}:${imageTag}|' deployment.yaml

            kubectl apply -f deployment.yaml

            kubectl rollout status deployment/ivolveapp -n ivolve
        """
    }
}
