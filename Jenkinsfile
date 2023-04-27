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
                  volumeMounts:
                  - name: shared-data
                    mountPath: /data
                - name: minio
                  image: minio/mc
                  command:
                  - cat
                  tty: true
                  volumeMounts:
                  - name: shared-data
                    mountPath: /data
                volumes:
                - name: shared-data
                  emptyDir: {}
            '''
        }
    }

    stages {
        stage('restore cache') {
            steps {
                script {
                    container('minio') {
                        sh "mc alias set minio http://minio.minio.svc.cluster.local:9000 vJlIj3mKR4Df9ZHt 9qZLIDh5A14IciJfEcmwGAk9iVQxHt4L"
                        sh "mc mirror minio/jira-performance-test/ /data &> /dev/null || true"
                    }
                }
            }
        }

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

        stage('test jira performance'){
            steps {
                script {
                    sh 'mkdir -p .m2 && cp -rT /data ~/.m2 &> /dev/null || true'
                    dir('examples/btf-test') {
                        container('maven') {
                            sh "unset MAVEN_CONFIG && ./mvnw verify -DtestURI=${params.TEST_URI} -DadminUsername=${params.ADMIN_USERNAME} -DadminPassword=${params.ADMIN_PASSWORD} -DnumberUsers=${params.NUMBER_USERS} -DdurationMinute=${params.DURATION_TIME} || true"
                        }

                        sh 'cat virtual-users.log | sed -n \'/actionName/,/View Issue/p\' > virtual-users.csv'
                    }
                    sh 'cp -rT ~/.m2 /data &> /dev/null'
                    container('minio-cli') {
                        sh "mc mirror /data minio/jira-performance-test/.m2 --overwrite &> /dev/null"
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'examples/btf-test/**/*', onlyIfSuccessful: true

            publishHTML (target : [allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: 'examples/btf-test/target/jpt-workspace',
            reportFiles: '**/*.html*',
            reportName: 'jira performance reports',
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
                            "text": "Job *${env.JOB_NAME}* has been finished.\n\nMore info at:\n*Build URL:* ${env.BUILD_URL}console\n*Jira performance reports:* ${env.BUILD_URL}jira-performance-reports"
                        ]
                    ]
                ]
                
                slackSend channel: 'selenium-notifications', blocks: blocks, teamDomain: 'agileops', tokenCredentialId: 'jenkins-slack', botUser: true
            }
        }
    }
}
