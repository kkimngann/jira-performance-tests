pipeline {
    agent {
        kubernetes {
        yaml '''
            apiVersion: v1
            kind: Pod
            metadata:
                labels:
                    jenkin-job: selenium
            spec:
                containers:
                - name: maven
                  image: markhobson/maven-chrome:jdk-11
                  command:
                  - cat
                  tty: true
            '''
        }
    }

    // environment {
    //     TEST_URI = "https://jira-9.aandd.io/"
    //     ADMIN_USERNAME = "admin"
    //     ADMIN_PASSWORD = "12345678"
    // }

    stages {
        stage('setup parameters') {
            steps {
                script { 
                    properties([
                        parameters([
                            text(
                                defaultValue: 'https://jira-9.aandd.io',
                                name: 'TEST_URI'
                            ),
                            text(
                                defaultValue: 'admin', 
                                name: 'ADMIN_USERNAME'
                            ),
                            text(
                                defaultValue: '12345678', 
                                name: 'ADMIN_PASSWORD'
                            ),
                            text(
                                defaultValue: '1', 
                                name: 'NUMBER_USERS'
                            ),
                            text(
                                defaultValue: '5', 
                                name: 'DURATION_TIME', 
                            )
                        ])
                    ])
                }
            }
        }

        stage('performance test'){
            steps {
                script {
                    dir('examples/btf-test') {
                        container('maven') {
                            // sh "unset MAVEN_CONFIG && ./mvnw verify -DtestURI=${params.TEST_URI} -DadminUsername=${params.ADMIN_USERNAME} -DadminPassword=${params.ADMIN_PASSWORD} -DnumberUsers=${params.NUMBER_USERS} -DdurationMinute=${params.DURATION_TIMES} || true"
                            sh "unset MAVEN_CONFIG && ./mvnw verify -DtestURI=https://jira-9.aandd.io/ -DadminUsername=admin -DadminPassword=12345678 -DnumberUsers=1 -DdurationMinute=5"
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'examples/btf-test/target/jpt-workspace/*'

            publishHTML (target : [allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: 'examples/btf-test/target/jpt-workspace/',
            reportFiles: 'mean-latency-chart.html',
            reportName: 'mean-latency-chart',
            reportTitles: '', 
            useWrapperFileDirectly: true])
            
            script {
                def blocks = [
                    [
                        "type": "section",
                        "text": [
                            "type": "mrkdwn",
                            "text": "*TEST FINISHED*"
                        ]
                    ],
                    [
                        "type": "divider"
                    ],
                    [
                        "type": "section",
                        "text": [
                            "type": "mrkdwn",
                            "text": "Job *${env.JOB_NAME}* has been finished.\n\nMore info at:\n*Build URL:* ${env.BUILD_URL}console\n*Mean latency report:* ${env.BUILD_URL}mean-latency-chart"
                        ]
                    ]
                ]
                
                slackSend channel: 'selenium-notifications', blocks: blocks, teamDomain: 'agileops', tokenCredentialId: 'jenkins-slack', botUser: true
            }
        }
    }
}
