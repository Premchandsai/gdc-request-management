pipeline {
    agent any
    environment {
        IMAGE = 'premchand1/requests-management'
    }
    stages {
        stage('Build Gradle') {
            steps {
                sh './gradlew clean build'
            }
        }
        stage('Docker Build') {
            steps {
                sh "docker build -t $IMAGE:latest ."
            }
        }
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh 'echo $PASS | docker login -u $USER --password-stdin'
                    sh "docker push $IMAGE:latest"
                }
            }
        }
        stage('K8s Deploy') {
            steps {
                sh 'kubectl apply -f secret.yaml'
                sh 'kubectl apply -f configmap.yaml'
                sh 'kubectl apply -f postgres-redis.yaml'
                sh 'kubectl apply -f zookeeper-kafka.yaml'
                sh 'sleep 30'  // Wait for infrastructure
                sh 'kubectl apply -f requests-management.yaml'
                sh 'kubectl apply -f ingress.yaml'
            }
        }
    }
}