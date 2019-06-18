#!/usr/bin/env groovy

/**
 * Send slack notification based on the NOTES and ISSUES parameters
 */
def call(String NOTES = "", String ISSUES = "") {
    def COLOR_MAP = ['SUCCESS': 'good', 'FAILURE': 'danger', 'UNSTABLE': 'danger', 'ABORTED': 'gray']
    if (NOTES != ""){
        NOTES_MESSAGE = "\n NOTES : ${NOTES}"
    }
    if (ISSUES != ""){
        ISSUES_MESSAGE = "\n *Related issues* :"
        def issuesList = ISSUES.split(" ")
        for (i = 0; i < issuesList.length; i++){
            issueId = issuesList[i]
            issueId = issueId.trim()
            def issue = jiraGetIssue idOrKey: issueId, site: 'JiraJenkins', failOnError: false
            if(!issue.successful){
                ISSUES_MESSAGE = ISSUES_MESSAGE + "\n *${issueId} - not found in the Jira issues*" +
                        "\n ___________"
                continue
            }
            def issueTypeUrl = issue.data.fields["issuetype"]["self"].toString()
            def jiraUrl = (issueTypeUrl =~ /https\:\/\/[a-zA-Z0-9\.\-]*/)
            def issueUrl = jiraUrl[0] + "/browse/" + issueId
            def summary = issue.data.fields["summary"].toString()
            def issueType = issue.data.fields["issuetype"]["name"].toString()
            def creator = issue.data.fields["creator"]["displayName"].toString()
            def creationDate = issue.data.fields["created"].split("\\+")[0].split("\\.")[0].replace("T", " ")
            def priority = issue.data.fields["priority"]["name"].toString()
            def status = issue.data.fields["status"]["name"].toString()
            ISSUES_MESSAGE = ISSUES_MESSAGE + "\n <${issueUrl}| ${issueType} - ${issueId} ${summary} >" +
                    "\n Created by *${creator}* at ${creationDate} " +
                    "\n *Priority*: ${priority} - *Status*: ${status} " +
                    "\n ___________"
        }
    }
    message = "*${currentBuild.currentResult} - ${env.JOB_NAME}* - *${currentBuild.durationString.replaceAll(' and counting','')}* - <${env.BUILD_URL}| #${env.BUILD_NUMBER}> " + NOTES_MESSAGE + ISSUES_MESSAGE
    slackSend (channel: "${SLACK_CHANNEL}", color: COLOR_MAP[currentBuild.currentResult], message: message)
}