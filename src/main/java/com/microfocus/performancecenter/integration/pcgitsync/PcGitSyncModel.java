/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

/*
 * Takes all the parameter from the job in order to create a Sync object
 * */
package com.microfocus.performancecenter.integration.pcgitsync;

import com.microfocus.performancecenter.integration.pcgitsync.helper.YesOrNo;
import com.microfocus.performancecenter.integration.pcgitsync.helper.UploadScriptMode;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class PcGitSyncModel implements Serializable {


    private static final String EXPECTED_CREDENTIALSID_PARAMETER_NAME = "CREDENTIALSID";
    private static final String EXPECTED_CREDENTIALSPROXYID_PARAMETER_NAME = "CREDENTIALSPROXYID";

    private final String description;
    private final String pcServerName;
    private final String serverAndPort;
    private final boolean httpsProtocol;
    private final String credentialsId;
    private final String almDomain;
    private final String almProject;
    private final String proxyOutURL;
    private final String credentialsProxyId;
    private final String subjectTestPlan;
    private final UploadScriptMode uploadScriptMode;
    private final YesOrNo removeScriptFromPC;
    private final YesOrNo importTests;
    private final boolean authenticateWithToken;
    private String buildParameters;


    @DataBoundConstructor
    public PcGitSyncModel(String description, String pcServerName, String serverAndPort, boolean httpsProtocol,
                          String credentialsId, String almDomain, String almProject,
                          String proxyOutURL, String credentialsProxyId,
                          String subjectTestPlan, UploadScriptMode uploadScriptMode, YesOrNo removeScriptFromPC, YesOrNo importTests, boolean authenticateWithToken, String buildParameters) {
        this.description = description;
        this.pcServerName = pcServerName;
        this.serverAndPort = serverAndPort;
        this.httpsProtocol = httpsProtocol;
        this.credentialsId = credentialsId;
        this.almDomain = almDomain;
        this.almProject = almProject;
        this.proxyOutURL = proxyOutURL;
        this.credentialsProxyId = credentialsProxyId;
        this.subjectTestPlan = subjectTestPlan.replace("/", "\\").replaceFirst("\\\\$", "").replaceAll("\\$", "");
        this.uploadScriptMode = uploadScriptMode;
        this.removeScriptFromPC = removeScriptFromPC;
        this.importTests = importTests;
        this.buildParameters = "";
        this.authenticateWithToken = authenticateWithToken;
    }

    public String getDescription() {
        return this.description;
    }

    public String getPcServerName() {
        return this.pcServerName;
    }

    public String getPcServerName(boolean fromPcClient) {
        return fromPcClient?useParameterIfNeeded(buildParameters,this.pcServerName): getPcServerName();
    }

    public String getServerAndPort () {
        return this.serverAndPort;
    }

    public boolean getHttpsProtocol(){
        return this.httpsProtocol;
    }

    public String getAlmDomain() {
        return this.almDomain;
    }

    public String getAlmDomain(boolean fromPcClient) {
        return fromPcClient?useParameterIfNeeded(buildParameters,this.almDomain):getAlmDomain();
    }

    public String getAlmProject() {
        return this.almProject;
    }

    public String getAlmProject(boolean fromPcClient) {
        return fromPcClient?useParameterIfNeeded(buildParameters,this.almProject):getAlmProject();
    }

    public String getProxyOutURL(){
        return this.proxyOutURL;
    }

    public String getProxyOutURL(boolean fromPcClient){
        return fromPcClient?useParameterIfNeeded(buildParameters,this.proxyOutURL):getProxyOutURL();
    }

    public String getSubjectTestPlan() {
        if(this.subjectTestPlan != null && this.subjectTestPlan .length() > 0)
            return this.subjectTestPlan.substring(0, 1).toUpperCase() + this.subjectTestPlan.substring(1);
        else
            return this.subjectTestPlan;
    }

    public String getSubjectTestPlan(boolean fromPcClient){
        return fromPcClient?useParameterIfNeeded(buildParameters,getSubjectTestPlan()):getSubjectTestPlan();
    }

    public static List<UploadScriptMode> getUploadScriptModes() {
        return Arrays.asList(UploadScriptMode.values());
    }

    public static List<YesOrNo> getYesOrNo() {
        return Arrays.asList(YesOrNo.values());
    }

    public String getBuildParameters() {
        return this.buildParameters;
    }

    public void setBuildParameters(String buildParameters){
        this.buildParameters = buildParameters;
    }

    public String getCredentialsId() {

        return this.credentialsId;
    }

    public String getCredentialsId(boolean fromPcClient) {

        return fromPcClient ? useParameterForCredentialsIdIfNeeded(buildParameters,this.credentialsId, EXPECTED_CREDENTIALSID_PARAMETER_NAME):getCredentialsId();
    }

    public String getCredentialsProxyId() {

        return this.credentialsProxyId;
    }

    public String getCredentialsProxyId(boolean fromPcClient) {

        return fromPcClient ? useParameterForCredentialsIdIfNeeded(buildParameters,this.credentialsProxyId, EXPECTED_CREDENTIALSPROXYID_PARAMETER_NAME):getCredentialsProxyId();
    }


    public UploadScriptMode getUploadScriptMode() {

        return this.uploadScriptMode;
    }

    public YesOrNo getRemoveScriptFromPC() {
        return this.removeScriptFromPC;
    }

    public YesOrNo getImportTests(){
        return this.importTests;
    }

    @Override
    public String toString() {
        return String.format("%s", runParamsToString().substring(1));
    }

    public String runParamsToString() {
        return String.format("[PCServer='%s', HTTPSProtocol='%s', CredentialsId='%s', Domain='%s', Project='%s', " +
                        "proxy='%s', CredentialsProxyId='%s', subjectTestPlan = '%s', uploadScriptMode='%s', removeScriptFromPC='%s', importTests='%s', UseTokenForAuthentication= '%s']",
                pcServerName, httpsProtocol, credentialsId, almDomain, almProject,
                proxyOutURL, credentialsProxyId, subjectTestPlan, uploadScriptMode.getValue(), removeScriptFromPC.getValue(), importTests.getValue(), authenticateWithToken);
    }

    public String getProtocol(){
        if (!httpsProtocol)
            return "http";
        return "https";
    }

    //public boolean getAuthenticateWithToken(){ return this.authenticateWithToken; }

    public boolean isAuthenticateWithToken(){ return this.authenticateWithToken; }

    private static String useParameterIfNeeded (String buildParameters,String attribute){
        if (buildParameters!=null && attribute!=null && attribute.startsWith("$")) {
            String attributeParameter = attribute.replace("$", "").replace("{", "").replace("}", "");
            String[] buildParametersArray = buildParameters.replace("{", "").replace("}", "").split(",");
            for (String buildParameter : buildParametersArray) {
                if (buildParameter.trim().startsWith(attributeParameter + "=")) {
                    return buildParameter.trim().replace(attributeParameter + "=", "");
                }
            }
        }
        return attribute;
    }

    private String useParameterForCredentialsIdIfNeeded (String buildParameters, String credentialsId, String expectedcredentialsIdParameterName ){
        if (buildParameters!=null) {
            String[] buildParametersArray = buildParameters.replace("{", "").replace("}", "").split(",");
            for (String buildParameter : buildParametersArray) {
                if (buildParameter.trim().startsWith(expectedcredentialsIdParameterName + "=")) {
                    return buildParameter.trim().replace(expectedcredentialsIdParameterName + "=", "");
                }
            }
        }
        return credentialsId;
    }



}
