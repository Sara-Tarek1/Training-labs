def call() {
    stage('Build App') {
        dir('DemoApp') {
            sh 'mvn clean test package'
        }
    }
}
