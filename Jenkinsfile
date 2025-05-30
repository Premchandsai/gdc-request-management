pipeline {
    agent any
    environment {
        IMAGE = 'premchand1/requests-management'
        DOCKER_CREDENTIALS_ID = 'dockerhub-creds'  // DockerHub credentials stored in Jenkins
        KUBECONFIG_CREDENTIALS_ID = 'kubeconfig-creds' // Optional: if you want Jenkins to use kubeconfig file credentials
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build Gradle') {
            steps {
                sh './gradlew clean build -x test' // optionally skip tests if you want
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    sh "docker build -t ${IMAGE}:latest ."
                }
            }
        }
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh 'echo $PASS | docker login -u $USER --password-stdin'
                    sh "docker push ${IMAGE}:latest"
                    sh 'docker logout'
                }
            }
        }
        stage('K8s Deploy') {
            steps {
                // Option 1: Use kubectl in environment (agent node has kubectl configured)
                sh 'kubectl apply -f secret.yaml'
                sh 'kubectl apply -f configmap.yaml'
                sh 'kubectl apply -f postgres-redis.yaml'
                sh 'kubectl apply -f zookeeper-kafka.yaml'
                sh 'sleep 30'  // Wait for infra readiness, can be optimized later
                sh 'kubectl apply -f requests-management.yaml'
                sh 'kubectl apply -f ingress.yaml'

                // Option 2: If you want to run kubectl with a kubeconfig credential (optional)
                // withCredentials([file(credentialsId: KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG')]) {
                //    sh 'kubectl apply -f secret.yaml'
                //    ...
                // }
            }
        }
    }
    post {
        success {
            echo 'Deployment succeeded!'
        }
        failure {
            echo 'Deployment failed!'
        }
        always {
            cleanWs()
        }
    }
}
