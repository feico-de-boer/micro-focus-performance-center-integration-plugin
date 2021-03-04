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

package com.microfocus.performancecenter.integration.common.helpers.services;

import com.microfocus.performancecenter.integration.common.helpers.constants.PcTestRunConstants;
import hudson.Extension;
import com.microfocus.performancecenter.integration.common.helpers.utils.AffectedFolder;
import com.microfocus.performancecenter.integration.common.helpers.utils.ModifiedType;
import com.microfocus.performancecenter.integration.common.helpers.utils.ModifiedFile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import com.microfocus.performancecenter.integration.common.helpers.utils.Helper;


@Extension
public class WorkspaceScripts {

    public SortedSet<AffectedFolder> getAllScriptsForUpload(Path workspace) throws IOException {
        SortedSet<AffectedFolder> result = new TreeSet<>();

        try ( Stream<Path> stream = Files.walk(workspace)) {
            stream
                    .filter(file -> !Files.isDirectory(file))
                    .filter(WorkspaceScripts::isScript)
                    .map(file -> new AffectedFolder(Helper.getParent(file), workspace))
                    .forEachOrdered(result::add);
        }

        return result;
    }

    public SortedSet<AffectedFolder> getAllAffectedFolders(Set<ModifiedFile> allModifiedFiles, Path workspace) {
        SortedSet<AffectedFolder> result = new TreeSet<>();

        allModifiedFiles.stream()
                .map(changedFile -> Helper.getParent(changedFile.getFullPath()))
                .map(parent -> new AffectedFolder(parent, workspace))
                .forEachOrdered(result::add);

        return result;
    }

    public SortedSet<AffectedFolder> getAllScriptsForDelete(Set<ModifiedFile> allModifiedFiles, Path workspace) {
        SortedSet<AffectedFolder> result = new TreeSet<>();

        allModifiedFiles.stream()
                .filter(modifiedFile -> modifiedFile.getModifiedType() == ModifiedType.DELETED)
                .filter(changedFile -> isScript(changedFile.getFullPath()))
                .map(changedFile -> Helper.getParent(changedFile.getFullPath()))
                .filter(parent -> !Files.exists(parent))
                .map(parent -> new AffectedFolder(parent, workspace))
                .forEachOrdered(result::add);

        return result;
    }

    public SortedSet<AffectedFolder> getAllScriptsForUpload(Set<AffectedFolder> allAffectedFolders, Path workspace) {
        SortedSet<AffectedFolder> result = new TreeSet<>();

        allAffectedFolders.stream()
                .map(affectedFolder -> getScriptFolderForSubfolder(affectedFolder.getFullPath(), workspace))
                .filter(Optional::isPresent)
                .map(scriptFolder -> new AffectedFolder(scriptFolder.get(), workspace))
                .forEachOrdered(result::add);

        return result;
    }

    // Suppose we have folder1/folder2/folder3 structure
    // And the only .usr file here is folder1/UC.usr (i.e. folders 2 and 3 do not contain it)
    // And we pass "folder3" as a subfolder parameter,
    // This method returns "folder1"
    private Optional<Path> getScriptFolderForSubfolder(Path subfolderFullPath, Path workspace) {
        if (subfolderFullPath == null || subfolderFullPath.equals(workspace)) {
            return Optional.empty();
        }

        Optional<Path> script;
        try {
            try (Stream<Path> stream = Files.list(subfolderFullPath)) {
                script = stream
                        .filter(WorkspaceScripts::isScript)
                        .findFirst();
            }
        } catch (IOException ioe) {
            script = Optional.empty();
        }

        if (script.isPresent()) {
            return Optional.of(subfolderFullPath);
        }

        return getScriptFolderForSubfolder(Helper.getParent(subfolderFullPath), workspace);
    }

    private static boolean isScript(Path fullPath) {
        return !Files.isDirectory(fullPath)
                && (fullPath.toString().toLowerCase(Locale.ROOT).endsWith(PcTestRunConstants.USR_EXTENSION)
                || fullPath.toString().toLowerCase(Locale.ROOT).endsWith(PcTestRunConstants.JMX_EXTENSION)
                || fullPath.toString().toLowerCase(Locale.ROOT).endsWith(PcTestRunConstants.GATLING_EXTENSION)
                || (fullPath.toString().toLowerCase(Locale.ROOT).endsWith(PcTestRunConstants.DEVWEB_MAIN_FILE) && isParentDirContainsRTS(fullPath))
                || (fullPath.toString().toLowerCase(Locale.ROOT).endsWith(PcTestRunConstants.SELENIUM_EXTENSION) && isFileContainSeleniumPackageReference(fullPath))
        );
    }

    private static boolean isParentDirContainsRTS(Path fullPath) {
        if(fullPath != null) {
            Path parentPath = fullPath.getParent();
            if(parentPath != null)
                return rstFinder(parentPath.toString()).length > 0;
        }
        return false;
    }

    private static boolean isFileContainSeleniumPackageReference(Path fullPath) {
        if(fullPath != null) {
            String javaFileContent = readLineByLineJava8(fullPath);
            if(javaFileContent != null && javaFileContent.toLowerCase().contains(PcTestRunConstants.SELENIUM_JAVA_CONTENT))
                return true;
        }
        return false;
    }

    private static String readLineByLineJava8(Path filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get(filePath.toString()), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private static File[] rstFinder(String dirName){
        File dir = new File(dirName);
        File[] noFiles = {};
        if(dir != null) {
            return dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return (filename.equalsIgnoreCase(PcTestRunConstants.DEVWEB_RTS_FILE));
                }
            });
        }
        return noFiles;
    }
}
