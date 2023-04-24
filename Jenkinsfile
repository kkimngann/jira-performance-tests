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
                - name: chromedriver
                  image: selenium/standalone-chrome:111.0.5563.146-chromedriver-111.0.5563.64
                  command:
                  - cat
                  tty: true
            '''
        }
    }

    stages {
        // stage('restore cache') {
        //     steps {
        //         script {
        //             container('minio-cli') {
        //                 sh "mc alias set minio http://minio.minio.svc.cluster.local:9000 vJlIj3mKR4Df9ZHt 9qZLIDh5A14IciJfEcmwGAk9iVQxHt4L"
        //                 sh "mc mirror minio/selenium/.m2 /data"
        //             }
        //         }
        //     }
        // }
        stage('performance test'){
            steps {
                script {
                    dir('examples/btf-test') {
                        container('chromedriver') {
                            sh './mvnw verify -DtestURI=https://jira-9.aandd.io/ -DadminUsername=admin -DadminPassword=12345678 -DnumberUsers=1 -DdurationMinute=5'
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

    // post {
    //     always {
    //         archiveArtifacts artifacts: 'allure-results/**/*'

    //         publishHTML (target : [allowMissing: false,
    //         alwaysLinkToLastBuild: true,
    //         keepAll: true,
    //         reportDir: 'allure-report',
    //         reportFiles: 'index.html',
    //         reportName: 'allure-report',
    //         reportTitles: '', 
    //         useWrapperFileDirectly: true])
    //     }
    // }
}
