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
        // stage('restore cache') {
        //     steps {
        //         script {
        //             container('minio-cli') {
        //                 sh "mc alias set minio http://minio.minio.svc.cluster.local:9000 vJlIj3mKR4Df9ZHt 9qZLIDh5A14IciJfEcmwGAk9iVQxHt4L"
        //                 sh "mc mirror minio/selenium/jira-performance-test/.m2 /data &> /dev/null"
        //             }
        //         }
        //     }
        // }
        
        stage('performance test'){
            steps {
                script {
                    dir('examples/btf-test') {
                        container('maven') {
                            sh 'unset MAVEN_CONFIG && ./mvnw verify -DtestURI=https://jira-9.aandd.io/ -DadminUsername=admin -DadminPassword=12345678 -DnumberUsers=1 -DdurationMinute=5 || true'
                        }
                    }
                }
            }
        }
    }

    // post {
    //     always {
    //         archiveArtifacts artifacts: 'examples/btf-test/target/jpt-workspace/**/*'

    //         publishHTML (target : [allowMissing: false,
    //         alwaysLinkToLastBuild: true,
    //         keepAll: true,
    //         reportDir: 'examples/btf-test/target/jpt-workspace/',
    //         reportFiles: 'mean-latency-chart.html',
    //         reportName: 'mean-latency-chart',
    //         reportTitles: '', 
    //         useWrapperFileDirectly: true])
            
    //         script {
    //             def blocks = [
    //                 [
    //                     "type": "section",
    //                     "text": [
    //                         "type": "mrkdwn",
    //                         "text": "*TEST FINISHED*"
    //                     ]
    //                 ],
    //                 [
    //                     "type": "divider"
    //                 ],
    //                 [
    //                     "type": "section",
    //                     "text": [
    //                         "type": "mrkdwn",
    //                         "text": "Job *${env.JOB_NAME}* has been finished.\n\nMore info at:\n*Build URL:* ${env.BUILD_URL}console\n*Mean latency report:* ${env.BUILD_URL}mean-latency-chart"
    //                     ]
    //                 ]
    //             ]
                
    //             slackSend channel: 'selenium-notifications', blocks: blocks, teamDomain: 'agileops', tokenCredentialId: 'jenkins-slack', botUser: true
    //         }
    //     }
    // }
}
