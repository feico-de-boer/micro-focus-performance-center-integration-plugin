/*
 *  Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 *  This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 *  Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 */

package com.microfocus.performancecenter.integration.pctestrun;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcException;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.RunState;
import static com.microfocus.adm.performancecenter.plugins.common.pcentities.RunState.COLLATING_RESULTS;
import static com.microfocus.adm.performancecenter.plugins.common.pcentities.RunState.CREATING_ANALYSIS_DATA;
import static com.microfocus.adm.performancecenter.plugins.common.pcentities.RunState.FINISHED;
import static com.microfocus.adm.performancecenter.plugins.common.pcentities.RunState.INITIALIZING;
import static com.microfocus.adm.performancecenter.plugins.common.pcentities.RunState.RUNNING;
import com.microfocus.adm.performancecenter.plugins.common.rest.PcRestProxy;


public class MockPcRestProxy extends PcRestProxy {

    private static Iterator<RunState> runState = initializeRunStateIterator();

    public MockPcRestProxy(String webProtocol, String pcServerName, boolean authenticateWithToken, String almDomain, String almProject, PrintStream logger) throws PcException {
        super(webProtocol, pcServerName, authenticateWithToken, almDomain, almProject, null, null, null);
    }

    private static Iterator<RunState> initializeRunStateIterator() {

        return Arrays.asList(INITIALIZING, RUNNING, COLLATING_RESULTS, CREATING_ANALYSIS_DATA, FINISHED).iterator();
    }

    @Override
    protected String executeRequest(HttpRequestBase request) throws PcException, ClientProtocolException,
            IOException {
        String requestUrl = request.getURI().toString();
        if (requestUrl.equals(String.format(AUTHENTICATION_LOGIN_URL, PcTestBase.WEB_PROTOCOL, PcTestBase.PC_SERVER_NAME))
                || requestUrl.equals(String.format(AUTHENTICATION_LOGOUT_URL,
                PcTestBase.WEB_PROTOCOL, PcTestBase.PC_SERVER_NAME))
                || requestUrl.equals(String.format(getBaseURL() + "/%s/%s/%s", RUNS_RESOURCE_NAME, PcTestBase.RUN_ID, PcTestBase.STOP_MODE))) {
            return "";
        } else if (requestUrl.equals(String.format(getBaseURL() + "/%s", RUNS_RESOURCE_NAME))
                || requestUrl.equals(String.format(getBaseURL() + "/%s/%s", RUNS_RESOURCE_NAME, PcTestBase.RUN_ID))) {
            return PcTestBase.runResponseEntity;
        } else if (requestUrl.equals(String.format(getBaseURL() + "/%s", TESTS_RESOURCE_NAME))
                || requestUrl.equals(String.format(getBaseURL() + "/%s/%s", TESTS_RESOURCE_NAME, PcTestBase.TEST_ID))) {
            return PcTestBase.testResponseEntity;
        } else if (requestUrl.equals(String.format(getBaseURL() + "/%s/%s", RUNS_RESOURCE_NAME, PcTestBase.RUN_ID_WAIT))) {
            String body = PcTestBase.runResponseEntity.replace("*", runState.next().value());
            if (!runState.hasNext())
                runState = initializeRunStateIterator();
            return body;
        } else if (requestUrl.equals(String.format(getBaseURL() + "/%s/%s/%s", RUNS_RESOURCE_NAME, PcTestBase.RUN_ID,
                RESULTS_RESOURCE_NAME))) {
            return PcTestBase.runResultsEntity;
        }
        throw new PcException(String.format("%s %s is not recognized by PC Rest Proxy", request.getMethod(), requestUrl));
    }

    /**
     * The report download in plugins-common 1.2.1 no longer routes through
     * executeRequest, so it cannot be intercepted there. Override the download
     * directly and copy the bundled test archive to the requested target path.
     */
    @Override
    public boolean GetRunResultData(int runId, int resultId, String localFilePath) throws PcException, IOException {
        String archiveResource = (resultId == Integer.parseInt(PcTestBase.NV_INSIGHTS_REPORT_ID))
                ? PcTestRunBuilder.pcNVInsightsReportArchiveName
                : PcTestRunBuilder.pcReportArchiveName;
        File sourceArchive = new File(getClass().getResource(archiveResource).getPath());
        Files.copy(sourceArchive.toPath(), Paths.get(localFilePath), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }
}
