pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                setBuildStatus("?", "PENDING");
                withMaven(
                        maven: 'maven-latest'
                ) {
                    dir("Utility") {
                        sh 'mvn -B  clean install'
                    }

                    dir("MatchingEngine") {
                        sh 'mvn -B  clean install'
                    }

                    dir("MatchingGateway") {
                        sh 'mvn -B  clean install'
                    }

                    dir("Accountant") {
                        sh 'mvn -B  clean install'
                    }

                    dir("EventLog") {
                        sh 'mvn -B  clean install'
                    }

                    dir("UserManagement") {
                        sh 'mvn -B  clean install'
                    }

                    dir("Wallet") {
                        sh 'mvn -B  clean install'
                    }

                    dir("Api") {
                        sh 'mvn -B  clean install'
                    }

                    dir("BlockchainGateway") {
                        sh 'mvn -B  clean install'
                    }
                }

            }
        }
        stage('Deliver') {
           environment {
              DATA = '/var/opex/runtime'
           }
           steps {
              dir("Deployment") {
                sh 'docker-compose build'
                sh 'docker-compose up -d'
              }
           }
        }
    }
    
    post {
        always {
            echo 'One way or another, I have finished'
        }
        success {
            echo ':)'
	        setBuildStatus(":)", "SUCCESS");
        }
        unstable {
            echo ':/'
	        setBuildStatus(":/", "UNSTABLE");
        }
        failure {
            echo ':('
	        setBuildStatus(":(", "FAILURE");
        }
        changed {
            echo 'Things were different before...'
        }
    }
}

void setBuildStatus(String message, String state) {
  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/opexdev/OPEX-Core"],
      contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
      errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
  ]);
}
