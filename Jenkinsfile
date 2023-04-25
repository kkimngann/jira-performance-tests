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
                - name: minio-cli
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
                    container('minio-cli') {
                        sh "mc alias set minio http://minio.minio.svc.cluster.local:9000 vJlIj3mKR4Df9ZHt 9qZLIDh5A14IciJfEcmwGAk9iVQxHt4L"
                        sh "mc mirror minio/selenium/jira-performance-test/.m2 /data &> /dev/null"
                    }
                }
            }
        }
        
        stage('performance test'){
            steps {
                script {
                    dir('examples/btf-test') {
                        container('maven') {
                            sh '''
                            mkdir -p .m2 && cp -rT /data ~/.m2 &> /dev/null
                            unset MAVEN_CONFIG && ./mvnw verify -DtestURI=https://jira-9.aandd.io/ -DadminUsername=admin -DadminPassword=12345678 -DnumberUsers=1 -DdurationMinute=5 || true
                            cp -rT ~/.m2 /data &> /dev/null
                            '''
                        }

                        container('minio-cli') {
                        sh "mc mirror /data minio/selenium/jira-performance-test --overwrite &> /dev/null"
                        }
                    }
                }
            }
        }

        // stage('publish report'){
        //     steps {
        //         script {
        //             container('allure') {
        //                 sh 'allure generate --clean -o allure-report'
        //             }

        //             def "blocks": [
        //                 [
        //                     "type": "section",
        //                     "text": [
        //                         "type": "mrkdwn",
        //                         "text": "*TEST FAILED*"
        //                     ]
        //                 ],
        //                 [
        //                     "type": "divider"
        //                 ],
        //                 [
        //                     "type": "section",
        //                     "text": [
        //                         "type": "mrkdwn",
        //                         "text": "Test in *${env.JOB_NAME}:${env.BUILD_NUMBER}* has been failed.\n\nMore info at:\n*Build URL:* ${env.BUILD_URL}/console\n*Allure Report:* ${env.BUILD_URL}/allure-report/"
        //                     ]
        //                 ]
        //             ]

        //             container('jq') {
        //                 sh 'jq -r ".suites[].cases[] | select(.status == \"failed\") | .attachments[].source" allure-resuls/*-result.json > failedTest.txt'
        //             }
        //             if failedTest.txt != null {
        //                 slackSend channel: 'selenium-notifications', blocks: blocks, teamDomain: 'agileops', tokenCredentialId: 'jenkins-slack', botUser: true
        //             }
        //         }
        //     }
        // }

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
        }
    }
}
